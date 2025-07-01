package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetSettingsGeneralBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author mmarif
 */
public class BottomSheetSettingsGeneralFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsGeneralBinding binding;
	private static int homeScreenSelectedChoice;
	private static int defaultLinkHandlerScreenSelectedChoice;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsGeneralBinding.inflate(inflater, container, false);

		homeScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_HOME_SCREEN_KEY));
		defaultLinkHandlerScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_LINK_HANDLER_KEY));

		setHomeScreenChipSelection(homeScreenSelectedChoice);
		setLinkHandlerChipSelection(defaultLinkHandlerScreenSelectedChoice);
		binding.switchTabs.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY)));
		binding.crashReportsSwitch.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_CRASH_REPORTS_KEY)));

		binding.homeScreenChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getHomeScreenChipPosition(checkedIds.get(0));
						if (newSelection != homeScreenSelectedChoice) {
							homeScreenSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_HOME_SCREEN_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.linkHandlerChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getLinkHandlerChipPosition(checkedIds.get(0));
						if (newSelection != defaultLinkHandlerScreenSelectedChoice) {
							defaultLinkHandlerScreenSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_LINK_HANDLER_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.customTabsFrame.setOnClickListener(
				v -> binding.switchTabs.setChecked(!binding.switchTabs.isChecked()));
		binding.switchTabs.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.enableSendReports.setOnClickListener(
				v ->
						binding.crashReportsSwitch.setChecked(
								!binding.crashReportsSwitch.isChecked()));
		binding.crashReportsSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_CRASH_REPORTS_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		return binding.getRoot();
	}

	private void setHomeScreenChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipHomeScreen0.setChecked(true);
				break;
			case 1:
				binding.chipHomeScreen1.setChecked(true);
				break;
			case 2:
				binding.chipHomeScreen2.setChecked(true);
				break;
			case 3:
				binding.chipHomeScreen3.setChecked(true);
				break;
			case 4:
				binding.chipHomeScreen4.setChecked(true);
				break;
			case 5:
				binding.chipHomeScreen5.setChecked(true);
				break;
			case 6:
				binding.chipHomeScreen6.setChecked(true);
				break;
			case 7:
				binding.chipHomeScreen7.setChecked(true);
				break;
			case 8:
				binding.chipHomeScreen8.setChecked(true);
				break;
			case 9:
				binding.chipHomeScreen9.setChecked(true);
				break;
			case 10:
				binding.chipHomeScreen10.setChecked(true);
				break;
			case 11:
				binding.chipHomeScreen11.setChecked(true);
				break;
		}
	}

	private int getHomeScreenChipPosition(int checkedId) {
		if (checkedId == R.id.chipHomeScreen0) return 0;
		if (checkedId == R.id.chipHomeScreen1) return 1;
		if (checkedId == R.id.chipHomeScreen2) return 2;
		if (checkedId == R.id.chipHomeScreen3) return 3;
		if (checkedId == R.id.chipHomeScreen4) return 4;
		if (checkedId == R.id.chipHomeScreen5) return 5;
		if (checkedId == R.id.chipHomeScreen6) return 6;
		if (checkedId == R.id.chipHomeScreen7) return 7;
		if (checkedId == R.id.chipHomeScreen8) return 8;
		if (checkedId == R.id.chipHomeScreen9) return 9;
		if (checkedId == R.id.chipHomeScreen10) return 10;
		if (checkedId == R.id.chipHomeScreen11) return 11;
		return homeScreenSelectedChoice;
	}

	private void setLinkHandlerChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipLinkHandler0.setChecked(true);
				break;
			case 1:
				binding.chipLinkHandler1.setChecked(true);
				break;
			case 2:
				binding.chipLinkHandler2.setChecked(true);
				break;
			case 3:
				binding.chipLinkHandler3.setChecked(true);
				break;
			case 4:
				binding.chipLinkHandler4.setChecked(true);
				break;
		}
	}

	private int getLinkHandlerChipPosition(int checkedId) {
		if (checkedId == R.id.chipLinkHandler0) return 0;
		if (checkedId == R.id.chipLinkHandler1) return 1;
		if (checkedId == R.id.chipLinkHandler2) return 2;
		if (checkedId == R.id.chipLinkHandler3) return 3;
		if (checkedId == R.id.chipLinkHandler4) return 4;
		return defaultLinkHandlerScreenSelectedChoice;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
