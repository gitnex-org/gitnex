package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetSettingsNotificationsBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author mmarif
 */
public class BottomSheetSettingsNotificationsFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsNotificationsBinding binding;
	private static int pollingDelayListSelectedChoice;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsNotificationsBinding.inflate(inflater, container, false);

		// Initialize polling delay
		pollingDelayListSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY));
		setChipSelection(pollingDelayListSelectedChoice);

		// Enable notifications switch
		binding.enableNotificationsMode.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_NOTIFICATIONS_KEY)));

		if (!binding.enableNotificationsMode.isChecked()) {
			AppUtil.setMultiVisibility(View.GONE, binding.pollingDelayFrame);
		}

		binding.enableNotificationsMode.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_NOTIFICATIONS_KEY);

					if (isChecked) {
						Notifications.startWorker(requireContext());
						AppUtil.setMultiVisibility(View.VISIBLE, binding.pollingDelayFrame);
					} else {
						Notifications.stopWorker(requireContext());
						AppUtil.setMultiVisibility(View.GONE, binding.pollingDelayFrame);
					}

					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.enableNotificationsFrame.setOnClickListener(
				v ->
						binding.enableNotificationsMode.setChecked(
								!binding.enableNotificationsMode.isChecked()));

		// Polling delay selection
		binding.pollingDelayChipGroup.setOnCheckedChangeListener(
				(group, checkedId) -> {
					int newSelection = getChipPosition(checkedId);
					if (newSelection != pollingDelayListSelectedChoice) {
						pollingDelayListSelectedChoice = newSelection;
						AppDatabaseSettings.updateSettingsValue(
								requireContext(),
								String.valueOf(newSelection),
								AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY);

						Notifications.stopWorker(requireContext());
						Notifications.startWorker(requireContext());

						SettingsFragment.refreshParent = true;
						SnackBar.success(
								requireContext(),
								requireActivity().findViewById(android.R.id.content),
								getString(R.string.settingsSave));
					}
				});

		return binding.getRoot();
	}

	private void setChipSelection(int position) {
		switch (position) {
			case 0:
				binding.chip15Minutes.setChecked(true);
				break;
			case 1:
				binding.chip30Minutes.setChecked(true);
				break;
			case 2:
				binding.chip45Minutes.setChecked(true);
				break;
			case 3:
				binding.chip1Hour.setChecked(true);
				break;
		}
	}

	private int getChipPosition(int checkedId) {
		if (checkedId == R.id.chip15Minutes) return 0;
		if (checkedId == R.id.chip30Minutes) return 1;
		if (checkedId == R.id.chip45Minutes) return 2;
		if (checkedId == R.id.chip1Hour) return 3;
		return pollingDelayListSelectedChoice; // Fallback to current selection
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // Prevent memory leaks
	}
}
