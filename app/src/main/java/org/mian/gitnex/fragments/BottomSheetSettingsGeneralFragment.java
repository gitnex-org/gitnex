package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetSettingsGeneralBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class BottomSheetSettingsGeneralFragment extends BottomSheetDialogFragment {

	private BottomsheetSettingsGeneralBinding binding;
	private static int homeScreenSelectedChoice;
	private static int defaultLinkHandlerScreenSelectedChoice;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL, R.style.Custom_BottomSheet);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetSettingsGeneralBinding.inflate(inflater, container, false);

		homeScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_HOME_SCREEN_KEY));
		defaultLinkHandlerScreenSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_LINK_HANDLER_KEY));

		binding.urlPromptSwitch.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_URL_PROMPT_KEY)));

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
							AppUIStateManager.invalidateUI();
							Toasty.show(requireContext(), getString(R.string.settingsSave));
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
							AppUIStateManager.invalidateUI();
							Toasty.show(requireContext(), getString(R.string.settingsSave));
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
					Toasty.show(requireContext(), getString(R.string.settingsSave));
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
					Toasty.show(requireContext(), getString(R.string.settingsSave));
				});

		// URL Prompt switch listener
		binding.urlPromptFrame.setOnClickListener(
				v -> binding.urlPromptSwitch.setChecked(!binding.urlPromptSwitch.isChecked()));

		binding.urlPromptSwitch.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_URL_PROMPT_KEY);
					Toasty.show(requireContext(), getString(R.string.settingsSave));
				});

		return binding.getRoot();
	}

	private void setHomeScreenChipSelection(int position) {
		int[] chipIds = {R.id.chipHomeScreen0, R.id.chipHomeScreen1, R.id.chipHomeScreen2};
		if (position >= 0 && position < chipIds.length) {
			binding.homeScreenChipGroup.check(chipIds[position]);
		}
	}

	private int getHomeScreenChipPosition(int checkedId) {
		if (checkedId == R.id.chipHomeScreen0) return 0;
		if (checkedId == R.id.chipHomeScreen1) return 1;
		if (checkedId == R.id.chipHomeScreen2) return 2;
		return 0;
	}

	private void setLinkHandlerChipSelection(int position) {
		int chipId =
				switch (position) {
					case 1 -> R.id.chipLinkHandler1;
					case 2 -> R.id.chipLinkHandler2;
					case 3 -> R.id.chipLinkHandler3;
					default -> R.id.chipLinkHandler0;
				};
		binding.linkHandlerChipGroup.check(chipId);
	}

	private int getLinkHandlerChipPosition(int checkedId) {
		if (checkedId == R.id.chipLinkHandler1) return 1;
		if (checkedId == R.id.chipLinkHandler2) return 2;
		if (checkedId == R.id.chipLinkHandler3) return 3;
		return 0;
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
