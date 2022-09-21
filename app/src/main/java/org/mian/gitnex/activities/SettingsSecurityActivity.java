package org.mian.gitnex.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.biometric.BiometricManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsSecurityBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;

/**
 * @author M M Arif
 */
public class SettingsSecurityActivity extends BaseActivity {

	private static String[] cacheSizeDataList;
	private static int cacheSizeDataSelectedChoice = 0;
	private static String[] cacheSizeImagesList;
	private static int cacheSizeImagesSelectedChoice = 0;
	private View.OnClickListener onClickListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsSecurityBinding activitySettingsSecurityBinding =
				ActivitySettingsSecurityBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsSecurityBinding.getRoot());

		ImageView closeActivity = activitySettingsSecurityBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		TextView cacheSizeDataSelected =
				activitySettingsSecurityBinding.cacheSizeDataSelected; // setter for data cache size
		TextView cacheSizeImagesSelected =
				activitySettingsSecurityBinding
						.cacheSizeImagesSelected; // setter for images cache size
		TextView clearCacheSelected =
				activitySettingsSecurityBinding.clearCacheSelected; // setter for clear cache

		LinearLayout certsFrame = activitySettingsSecurityBinding.certsFrame;
		LinearLayout cacheSizeDataFrame =
				activitySettingsSecurityBinding.cacheSizeDataSelectionFrame;
		LinearLayout cacheSizeImagesFrame =
				activitySettingsSecurityBinding.cacheSizeImagesSelectionFrame;
		LinearLayout clearCacheFrame = activitySettingsSecurityBinding.clearCacheSelectionFrame;

		SwitchMaterial switchBiometric = activitySettingsSecurityBinding.switchBiometric;

		cacheSizeDataList = getResources().getStringArray(R.array.cacheSizeList);
		cacheSizeImagesList = getResources().getStringArray(R.array.cacheSizeList);

		cacheSizeDataSelected.setText(
				tinyDB.getString(
						"cacheSizeStr", getString(R.string.cacheSizeDataSelectionSelectedText)));
		cacheSizeImagesSelected.setText(
				tinyDB.getString(
						"cacheSizeImagesStr",
						getString(R.string.cacheSizeImagesSelectionSelectedText)));

		if (cacheSizeDataSelectedChoice == 0) {

			cacheSizeDataSelectedChoice = tinyDB.getInt("cacheSizeId");
		}

		if (cacheSizeImagesSelectedChoice == 0) {

			cacheSizeImagesSelectedChoice = tinyDB.getInt("cacheSizeImagesId");
		}

		switchBiometric.setChecked(tinyDB.getBoolean("biometricStatus", false));

		// biometric switcher
		switchBiometric.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {

						BiometricManager biometricManager = BiometricManager.from(ctx);
						KeyguardManager keyguardManager =
								(KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);

						if (!keyguardManager.isDeviceSecure()) {

							switch (biometricManager.canAuthenticate(
									BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
								case BiometricManager.BIOMETRIC_SUCCESS:
									tinyDB.putBoolean("biometricStatus", true);
									Toasty.success(
											appCtx,
											getResources().getString(R.string.settingsSave));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
								case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
								case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
								case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
									tinyDB.putBoolean("biometricStatus", false);
									switchBiometric.setChecked(false);
									Toasty.error(
											appCtx,
											getResources()
													.getString(R.string.biometricNotSupported));
									break;
								case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
									tinyDB.putBoolean("biometricStatus", false);
									switchBiometric.setChecked(false);
									Toasty.error(
											appCtx,
											getResources()
													.getString(R.string.biometricNotAvailable));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
									tinyDB.putBoolean("biometricStatus", false);
									switchBiometric.setChecked(false);
									Toasty.info(
											appCtx,
											getResources().getString(R.string.enrollBiometric));
									break;
							}
						} else {

							tinyDB.putBoolean("biometricStatus", true);
							Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
						}
					} else {

						tinyDB.putBoolean("biometricStatus", false);
						Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
					}
				});

		activitySettingsSecurityBinding.biometricFrame.setOnClickListener(
				v -> switchBiometric.setChecked(!switchBiometric.isChecked()));

		// clear cache setter
		File cacheDir = appCtx.getCacheDir();
		clearCacheSelected.setText(
				FileUtils.byteCountToDisplaySize((int) FileUtils.sizeOfDirectory(cacheDir)));

		// clear cache
		clearCacheFrame.setOnClickListener(
				v1 -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.clearCacheDialogHeader)
									.setMessage(
											getResources()
													.getString(R.string.clearCacheDialogMessage))
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											R.string.menuDeleteText,
											(dialog, which) -> {
												try {

													FileUtils.deleteDirectory(cacheDir);
													FileUtils.forceMkdir(cacheDir);
													this.recreate();
													this.overridePendingTransition(0, 0);
												} catch (IOException e) {

													Log.e("SettingsSecurity", e.toString());
												}
											});

					materialAlertDialogBuilder.create().show();
				});

		// cache size images selection dialog
		cacheSizeImagesFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.cacheSizeImagesDialogHeader)
									.setCancelable(cacheSizeImagesSelectedChoice != -1)
									.setSingleChoiceItems(
											cacheSizeImagesList,
											cacheSizeImagesSelectedChoice,
											(dialogInterfaceTheme, i) -> {
												cacheSizeImagesSelectedChoice = i;
												cacheSizeImagesSelected.setText(
														cacheSizeImagesList[i]);
												tinyDB.putString(
														"cacheSizeImagesStr",
														cacheSizeImagesList[i]);
												tinyDB.putInt("cacheSizeImagesId", i);

												dialogInterfaceTheme.dismiss();
												Toasty.success(
														appCtx,
														getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// cache size data selection dialog
		cacheSizeDataFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.cacheSizeDataDialogHeader)
									.setCancelable(cacheSizeDataSelectedChoice != -1)
									.setSingleChoiceItems(
											cacheSizeDataList,
											cacheSizeDataSelectedChoice,
											(dialogInterfaceTheme, i) -> {
												cacheSizeDataSelectedChoice = i;
												cacheSizeDataSelected.setText(cacheSizeDataList[i]);
												tinyDB.putString(
														"cacheSizeStr", cacheSizeDataList[i]);
												tinyDB.putInt("cacheSizeId", i);

												dialogInterfaceTheme.dismiss();
												Toasty.success(
														appCtx,
														getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// certs deletion
		certsFrame.setOnClickListener(
				v1 -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.settingsCertsPopupTitle)
									.setMessage(
											getResources()
													.getString(R.string.settingsCertsPopupMessage))
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											R.string.menuDeleteText,
											(dialog, which) -> {
												appCtx.getSharedPreferences(
																MemorizingTrustManager
																		.KEYSTORE_NAME,
																Context.MODE_PRIVATE)
														.edit()
														.remove(MemorizingTrustManager.KEYSTORE_KEY)
														.apply();
												AppUtil.logout(this);
											});

					materialAlertDialogBuilder.create().show();
				});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}
}
