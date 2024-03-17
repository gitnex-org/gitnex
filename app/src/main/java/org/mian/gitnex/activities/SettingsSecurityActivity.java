package org.mian.gitnex.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.biometric.BiometricManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsSecurityBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;

/**
 * @author M M Arif
 */
public class SettingsSecurityActivity extends BaseActivity {

	private static String[] cacheSizeDataList;
	private static int cacheSizeDataSelectedChoice;
	private static String[] cacheSizeImagesList;
	private static int cacheSizeImagesSelectedChoice;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsSecurityBinding activitySettingsSecurityBinding =
				ActivitySettingsSecurityBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsSecurityBinding.getRoot());

		activitySettingsSecurityBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		cacheSizeDataList = getResources().getStringArray(R.array.cacheSizeList);
		cacheSizeImagesList = getResources().getStringArray(R.array.cacheSizeList);

		cacheSizeDataSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_DATA_CACHE_KEY));
		cacheSizeImagesSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_IMAGES_CACHE_KEY));

		activitySettingsSecurityBinding.cacheSizeDataSelected.setText(
				cacheSizeDataList[cacheSizeDataSelectedChoice]);
		activitySettingsSecurityBinding.cacheSizeImagesSelected.setText(
				cacheSizeImagesList[cacheSizeImagesSelectedChoice]);

		activitySettingsSecurityBinding.switchBiometric.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_BIOMETRIC_KEY)));

		// biometric switcher
		activitySettingsSecurityBinding.switchBiometric.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {

						BiometricManager biometricManager = BiometricManager.from(ctx);
						KeyguardManager keyguardManager =
								(KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);

						if (!keyguardManager.isDeviceSecure()) {

							switch (biometricManager.canAuthenticate(
									BIOMETRIC_STRONG | DEVICE_CREDENTIAL)) {
								case BiometricManager.BIOMETRIC_SUCCESS:
									AppDatabaseSettings.updateSettingsValue(
											ctx, "true", AppDatabaseSettings.APP_BIOMETRIC_KEY);
									SnackBar.success(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.settingsSave));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
								case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
								case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
								case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:
									AppDatabaseSettings.updateSettingsValue(
											ctx, "false", AppDatabaseSettings.APP_BIOMETRIC_KEY);
									activitySettingsSecurityBinding.switchBiometric.setChecked(
											false);
									SnackBar.error(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.biometricNotSupported));
									break;
								case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
									AppDatabaseSettings.updateSettingsValue(
											ctx, "false", AppDatabaseSettings.APP_BIOMETRIC_KEY);
									activitySettingsSecurityBinding.switchBiometric.setChecked(
											false);
									SnackBar.error(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.biometricNotAvailable));
									break;
								case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
									AppDatabaseSettings.updateSettingsValue(
											ctx, "false", AppDatabaseSettings.APP_BIOMETRIC_KEY);
									activitySettingsSecurityBinding.switchBiometric.setChecked(
											false);
									SnackBar.info(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.enrollBiometric));
									break;
							}
						} else {

							AppDatabaseSettings.updateSettingsValue(
									ctx, "true", AppDatabaseSettings.APP_BIOMETRIC_KEY);
							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					} else {

						AppDatabaseSettings.updateSettingsValue(
								ctx, "false", AppDatabaseSettings.APP_BIOMETRIC_KEY);
						SnackBar.success(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.settingsSave));
					}
				});

		activitySettingsSecurityBinding.biometricFrame.setOnClickListener(
				v ->
						activitySettingsSecurityBinding.switchBiometric.setChecked(
								!activitySettingsSecurityBinding.switchBiometric.isChecked()));

		// clear cache setter
		File cacheDir = appCtx.getCacheDir();
		activitySettingsSecurityBinding.clearCacheSelected.setText(
				FileUtils.byteCountToDisplaySize((int) FileUtils.sizeOfDirectory(cacheDir)));

		// clear cache
		activitySettingsSecurityBinding.clearCacheSelectionFrame.setOnClickListener(
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

													// Log.e("SettingsSecurity", e.toString());
												}
											});

					materialAlertDialogBuilder.create().show();
				});

		// cache size images selection dialog
		activitySettingsSecurityBinding.cacheSizeImagesSelectionFrame.setOnClickListener(
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
												activitySettingsSecurityBinding
														.cacheSizeImagesSelected.setText(
														cacheSizeImagesList[i]);

												AppDatabaseSettings.updateSettingsValue(
														ctx,
														cacheSizeImagesList[i],
														AppDatabaseSettings
																.APP_IMAGES_CACHE_SIZE_KEY);
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_IMAGES_CACHE_KEY);

												dialogInterfaceTheme.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// cache size data selection dialog
		activitySettingsSecurityBinding.cacheSizeDataSelectionFrame.setOnClickListener(
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
												activitySettingsSecurityBinding
														.cacheSizeDataSelected.setText(
														cacheSizeDataList[i]);

												AppDatabaseSettings.updateSettingsValue(
														ctx,
														cacheSizeDataList[i],
														AppDatabaseSettings
																.APP_DATA_CACHE_SIZE_KEY);
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_DATA_CACHE_KEY);

												dialogInterfaceTheme.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// certs deletion
		activitySettingsSecurityBinding.certsFrame.setOnClickListener(
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
}
