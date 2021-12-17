package org.mian.gitnex.activities;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsSecurityBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import java.io.File;
import java.io.IOException;
import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

/**
 * Author M M Arif
 */

public class SettingsSecurityActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static String[] cacheSizeDataList;
	private static int cacheSizeDataSelectedChoice = 0;

	private static String[] cacheSizeImagesList;
	private static int cacheSizeImagesSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsSecurityBinding activitySettingsSecurityBinding = ActivitySettingsSecurityBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsSecurityBinding.getRoot());

		ImageView closeActivity = activitySettingsSecurityBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		TextView cacheSizeDataSelected = activitySettingsSecurityBinding.cacheSizeDataSelected; // setter for data cache size
		TextView cacheSizeImagesSelected = activitySettingsSecurityBinding.cacheSizeImagesSelected; // setter for images cache size
		TextView clearCacheSelected = activitySettingsSecurityBinding.clearCacheSelected; // setter for clear cache

		LinearLayout certsFrame = activitySettingsSecurityBinding.certsFrame;
		LinearLayout cacheSizeDataFrame = activitySettingsSecurityBinding.cacheSizeDataSelectionFrame;
		LinearLayout cacheSizeImagesFrame = activitySettingsSecurityBinding.cacheSizeImagesSelectionFrame;
		LinearLayout clearCacheFrame = activitySettingsSecurityBinding.clearCacheSelectionFrame;

		SwitchMaterial switchBiometric = activitySettingsSecurityBinding.switchBiometric;

		cacheSizeDataList = getResources().getStringArray(R.array.cacheSizeList);
		cacheSizeImagesList = getResources().getStringArray(R.array.cacheSizeList);

		if(!tinyDB.getString("cacheSizeStr").isEmpty()) {

			cacheSizeDataSelected.setText(tinyDB.getString("cacheSizeStr"));
		}

		if(!tinyDB.getString("cacheSizeImagesStr").isEmpty()) {

			cacheSizeImagesSelected.setText(tinyDB.getString("cacheSizeImagesStr"));
		}

		if(cacheSizeDataSelectedChoice == 0) {

			cacheSizeDataSelectedChoice = tinyDB.getInt("cacheSizeId");
		}

		if(cacheSizeImagesSelectedChoice == 0) {

			cacheSizeImagesSelectedChoice = tinyDB.getInt("cacheSizeImagesId");
		}

		switchBiometric.setChecked(tinyDB.getBoolean("biometricStatus"));

		// biometric switcher
		switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

				if(isChecked) {

					BiometricManager biometricManager = BiometricManager.from(ctx);
					KeyguardManager keyguardManager = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);

					if (!keyguardManager.isDeviceSecure()) {

						switch(biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {

							case BiometricManager.BIOMETRIC_SUCCESS:

								tinyDB.putBoolean("biometricStatus", true);
								Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
								break;
							case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
							case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
							case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
							case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:

								tinyDB.putBoolean("biometricStatus", false);
								switchBiometric.setChecked(false);
								Toasty.error(appCtx, getResources().getString(R.string.biometricNotSupported));
								break;
							case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:

								tinyDB.putBoolean("biometricStatus", false);
								switchBiometric.setChecked(false);
								Toasty.error(appCtx, getResources().getString(R.string.biometricNotAvailable));
								break;
							case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:

								tinyDB.putBoolean("biometricStatus", false);
								switchBiometric.setChecked(false);
								Toasty.info(appCtx, getResources().getString(R.string.enrollBiometric));
								break;
						}
					}
					else {

						tinyDB.putBoolean("biometricStatus", true);
						Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
					}
				}
				else {

					tinyDB.putBoolean("biometricStatus", false);
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				}
			}
			else {

				tinyDB.putBoolean("biometricStatus", false);
				Toasty.success(appCtx, getResources().getString(R.string.biometricNotSupported));
			}

		});

		activitySettingsSecurityBinding.biometricFrame.setOnClickListener(v -> switchBiometric.setChecked(!switchBiometric.isChecked()));

		// clear cache setter
		File cacheDir = appCtx.getCacheDir();
		clearCacheSelected.setText(FileUtils.byteCountToDisplaySize((int) FileUtils.sizeOfDirectory(cacheDir)));

		// clear cache
		clearCacheFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			builder.setTitle(getResources().getString(R.string.clearCacheDialogHeader));
			builder.setMessage(getResources().getString(R.string.clearCacheDialogMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				try {

					FileUtils.deleteDirectory(cacheDir);
					FileUtils.mkdir(cacheDir.getAbsolutePath());
					this.recreate();
					this.overridePendingTransition(0, 0);
				}
				catch (IOException e) {

					Log.e("SettingsSecurity", e.toString());
				}
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();

		});

		// cache size images selection dialog
		cacheSizeImagesFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.cacheSizeImagesDialogHeader));
			tsBuilder.setCancelable(cacheSizeImagesSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(cacheSizeImagesList, cacheSizeImagesSelectedChoice, (dialogInterfaceTheme, i) -> {

				cacheSizeImagesSelectedChoice = i;
				cacheSizeImagesSelected.setText(cacheSizeImagesList[i]);
				tinyDB.putString("cacheSizeImagesStr", cacheSizeImagesList[i]);
				tinyDB.putInt("cacheSizeImagesId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		// cache size data selection dialog
		cacheSizeDataFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.cacheSizeDataDialogHeader));
			tsBuilder.setCancelable(cacheSizeDataSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(cacheSizeDataList, cacheSizeDataSelectedChoice, (dialogInterfaceTheme, i) -> {

				cacheSizeDataSelectedChoice = i;
				cacheSizeDataSelected.setText(cacheSizeDataList[i]);
				tinyDB.putString("cacheSizeStr", cacheSizeDataList[i]);
				tinyDB.putInt("cacheSizeId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		// certs deletion
		certsFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			builder.setTitle(getResources().getString(R.string.settingsCertsPopupTitle));
			builder.setMessage(getResources().getString(R.string.settingsCertsPopupMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				appCtx.getSharedPreferences(MemorizingTrustManager.KEYSTORE_NAME, Context.MODE_PRIVATE).edit().remove(MemorizingTrustManager.KEYSTORE_KEY).apply();

				tinyDB.putBoolean("loggedInMode", false);
				tinyDB.remove("basicAuthPassword");
				tinyDB.putBoolean("basicAuthFlag", false);

				Intent loginActivityIntent = new Intent().setClass(appCtx, LoginActivity.class);
				loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appCtx.startActivity(loginActivityIntent);
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}
}
