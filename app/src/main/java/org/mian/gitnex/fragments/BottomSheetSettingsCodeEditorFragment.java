package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetSettingsCodeEditorBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author mmarif
 */
public class BottomSheetSettingsCodeEditorFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsCodeEditorBinding binding;
	private static int colorSelectedChoice;
	private static int indentationSelectedChoice;
	private static int indentationTabsSelectedChoice;
	private static String[] indentationList;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsCodeEditorBinding.inflate(inflater, container, false);

		indentationList = getResources().getStringArray(R.array.ceIndentation);
		colorSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_CE_SYNTAX_HIGHLIGHT_KEY));
		indentationSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_CE_INDENTATION_KEY));
		indentationTabsSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY));

		setColorChipSelection(colorSelectedChoice);
		setIndentationChipSelection(indentationSelectedChoice);
		setIndentationTabsChipSelection(indentationTabsSelectedChoice);

		updateTabsWidthVisibility();

		binding.ceColorChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getColorChipPosition(checkedIds.get(0));
						if (newSelection != colorSelectedChoice) {
							colorSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_CE_SYNTAX_HIGHLIGHT_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.indentationChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getIndentationChipPosition(checkedIds.get(0));
						if (newSelection != indentationSelectedChoice) {
							indentationSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_CE_INDENTATION_KEY);
							updateTabsWidthVisibility();
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.indentationTabsChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					if (checkedIds.size() == 1) {
						int newSelection = getIndentationTabsChipPosition(checkedIds.get(0));
						if (newSelection != indentationTabsSelectedChoice) {
							indentationTabsSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY);
							SettingsFragment.refreshParent = true;
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		return binding.getRoot();
	}

	private void setColorChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipColorDark.setChecked(true);
				break;
			case 1:
				binding.chipColorLight.setChecked(true);
				break;
		}
	}

	private int getColorChipPosition(int checkedId) {
		if (checkedId == R.id.chipColorDark) return 0;
		if (checkedId == R.id.chipColorLight) return 1;
		return colorSelectedChoice;
	}

	private void setIndentationChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipIndentSpaces.setChecked(true);
				break;
			case 1:
				binding.chipIndentTabs.setChecked(true);
				break;
		}
	}

	private int getIndentationChipPosition(int checkedId) {
		if (checkedId == R.id.chipIndentSpaces) return 0;
		if (checkedId == R.id.chipIndentTabs) return 1;
		return indentationSelectedChoice;
	}

	private void setIndentationTabsChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chipTabs2.setChecked(true);
				break;
			case 1:
				binding.chipTabs4.setChecked(true);
				break;
			case 2:
				binding.chipTabs6.setChecked(true);
				break;
			case 3:
				binding.chipTabs8.setChecked(true);
				break;
		}
	}

	private int getIndentationTabsChipPosition(int checkedId) {
		if (checkedId == R.id.chipTabs2) return 0;
		if (checkedId == R.id.chipTabs4) return 1;
		if (checkedId == R.id.chipTabs6) return 2;
		if (checkedId == R.id.chipTabs8) return 3;
		return indentationTabsSelectedChoice;
	}

	private void updateTabsWidthVisibility() {
		boolean isTabsSelected =
				indentationList[indentationSelectedChoice].startsWith(
						getString(R.string.ceIndentationTabs));
		binding.indentationTabsSelectionFrame.setVisibility(
				isTabsSelected ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
