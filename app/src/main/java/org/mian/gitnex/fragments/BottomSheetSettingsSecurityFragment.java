package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetSettingsSecurityBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;

/**
 * @author mmarif
 */
public class BottomSheetSettingsSecurityFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsSecurityBinding binding;
	private static String[] cacheSizeDataList;
	private static int cacheSizeDataSelectedChoice;
	private static String[] cacheSizeImagesList;
	private static int cacheSizeImagesSelectedChoice;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsSecurityBinding.inflate(inflater, container, false);

		cacheSizeDataList = getResources().getStringArray(R.array.cacheSizeList);
		cacheSizeImagesList = getResources().getStringArray(R.array.cacheSizeList);

		cacheSizeDataSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_DATA_CACHE_KEY));
		cacheSizeImagesSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_IMAGES_CACHE_KEY));

		setCacheSizeDataChipSelection(cacheSizeDataSelectedChoice);
		setCacheSizeImagesChipSelection(cacheSizeImagesSelectedChoice);
		binding.switchBiometric.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_BIOMETRIC_KEY)));

		File cacheDir = requireContext().getCacheDir();
		binding.clearCacheButton.setText(
				getString(
						R.string.clear_cache_button_text,
						FileUtils.byteCountToDisplaySize(
								(int) FileUtils.sizeOfDirectory(cacheDir))));

		binding.switchBiometric.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (!isChecked) {
						AppDatabaseSettings.updateSettingsValue(
								requireContext(), "false", AppDatabaseSettings.APP_BIOMETRIC_KEY);
						SnackBar.success(
								requireContext(),
								requireActivity().findViewById(android.R.id.content),
								getString(R.string.settingsSave));
						return;
					}

					BiometricManager biometricManager = BiometricManager.from(requireContext());

					int result =
							biometricManager.canAuthenticate(
									BiometricManager.Authenticators.BIOMETRIC_STRONG
											| BiometricManager.Authenticators.DEVICE_CREDENTIAL);

					switch (result) {
						case BiometricManager.BIOMETRIC_SUCCESS:
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									"true",
									AppDatabaseSettings.APP_BIOMETRIC_KEY);
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
							break;

						case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
							binding.switchBiometric.setChecked(false);
							SnackBar.info(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.enrollBiometric));
							break;

						case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
						case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
						case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
							binding.switchBiometric.setChecked(false);
							SnackBar.error(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.biometricNotSupported));
							break;

						case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
						default:
							binding.switchBiometric.setChecked(false);
							SnackBar.error(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.biometricNotAvailable));
							break;
					}
				});

		binding.biometricFrame.setOnClickListener(
				v -> binding.switchBiometric.setChecked(!binding.switchBiometric.isChecked()));

		binding.cacheSizeDataChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getCacheSizeDataChipPosition(checkedIds.get(0));
						if (newSelection != cacheSizeDataSelectedChoice) {
							cacheSizeDataSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									cacheSizeDataList[newSelection],
									AppDatabaseSettings.APP_DATA_CACHE_SIZE_KEY);
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_DATA_CACHE_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.cacheSizeImagesChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getCacheSizeImagesChipPosition(checkedIds.get(0));
						if (newSelection != cacheSizeImagesSelectedChoice) {
							cacheSizeImagesSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									cacheSizeImagesList[newSelection],
									AppDatabaseSettings.APP_IMAGES_CACHE_SIZE_KEY);
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_IMAGES_CACHE_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.clearCacheButton.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder dialog =
							new MaterialAlertDialogBuilder(requireContext())
									.setTitle(R.string.clearCacheDialogHeader)
									.setMessage(getString(R.string.clearCacheDialogMessage))
									.setNeutralButton(
											R.string.cancelButton, (d, which) -> d.dismiss())
									.setPositiveButton(
											R.string.menuDeleteText,
											(d, which) -> {
												try {
													File cacheDir1 = requireContext().getCacheDir();
													FileUtils.deleteDirectory(cacheDir1);
													FileUtils.forceMkdir(cacheDir1);
													requireActivity().recreate();
													requireActivity()
															.overridePendingTransition(0, 0);
												} catch (IOException ignored) {
												}
											});
					dialog.show();
				});

		binding.deleteCertsButton.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder dialog =
							new MaterialAlertDialogBuilder(requireContext())
									.setTitle(R.string.settingsCertsPopupTitle)
									.setMessage(getString(R.string.settingsCertsPopupMessage))
									.setNeutralButton(
											R.string.cancelButton, (d, which) -> d.dismiss())
									.setPositiveButton(
											R.string.menuDeleteText,
											(d, which) -> {
												requireContext()
														.getSharedPreferences(
																MemorizingTrustManager
																		.KEYSTORE_NAME,
																Context.MODE_PRIVATE)
														.edit()
														.remove(MemorizingTrustManager.KEYSTORE_KEY)
														.apply();
												AppUtil.logout(requireContext());
											});
					dialog.show();
				});

		return binding.getRoot();
	}

	private void setCacheSizeDataChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipDataCache0.setChecked(true);
				break;
			case 1:
				binding.chipDataCache1.setChecked(true);
				break;
			case 2:
				binding.chipDataCache2.setChecked(true);
				break;
			case 3:
				binding.chipDataCache3.setChecked(true);
				break;
		}
	}

	private int getCacheSizeDataChipPosition(int checkedId) {
		if (checkedId == R.id.chipDataCache0) return 0;
		if (checkedId == R.id.chipDataCache1) return 1;
		if (checkedId == R.id.chipDataCache2) return 2;
		if (checkedId == R.id.chipDataCache3) return 3;
		return cacheSizeDataSelectedChoice;
	}

	private void setCacheSizeImagesChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipImagesCache0.setChecked(true);
				break;
			case 1:
				binding.chipImagesCache1.setChecked(true);
				break;
			case 2:
				binding.chipImagesCache2.setChecked(true);
				break;
			case 3:
				binding.chipImagesCache3.setChecked(true);
				break;
		}
	}

	private int getCacheSizeImagesChipPosition(int checkedId) {
		if (checkedId == R.id.chipImagesCache0) return 0;
		if (checkedId == R.id.chipImagesCache1) return 1;
		if (checkedId == R.id.chipImagesCache2) return 2;
		if (checkedId == R.id.chipImagesCache3) return 3;
		return cacheSizeImagesSelectedChoice;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
