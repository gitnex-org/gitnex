package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsNotificationsBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author M M Arif
 * @author opyale
 */
public class SettingsNotificationsActivity extends BaseActivity {

	private ActivitySettingsNotificationsBinding viewBinding;
	private static String[] pollingDelayList;
	private static int pollingDelayListSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsNotificationsBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		viewBinding.enableNotificationsMode.setChecked(
				tinyDB.getBoolean("notificationsEnabled", true));

		if (!viewBinding.enableNotificationsMode.isChecked()) {
			AppUtil.setMultiVisibility(View.GONE, viewBinding.pollingDelayFrame);
		}

		viewBinding.enableNotificationsMode.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("notificationsEnabled", isChecked);

					if (isChecked) {
						Notifications.startWorker(ctx);
						AppUtil.setMultiVisibility(View.VISIBLE, viewBinding.pollingDelayFrame);
					} else {
						Notifications.stopWorker(ctx);
						AppUtil.setMultiVisibility(View.GONE, viewBinding.pollingDelayFrame);
					}

					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		viewBinding.enableNotificationsFrame.setOnClickListener(
				v ->
						viewBinding.enableNotificationsMode.setChecked(
								!viewBinding.enableNotificationsMode.isChecked()));

		// polling delay
		pollingDelayList = getResources().getStringArray(R.array.notificationsPollingDelay);
		pollingDelayListSelectedChoice = tinyDB.getInt("notificationsPollingDelayId");
		viewBinding.pollingDelaySelected.setText(pollingDelayList[pollingDelayListSelectedChoice]);

		viewBinding.pollingDelayFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.pollingDelayDialogHeaderText)
									.setSingleChoiceItems(
											pollingDelayList,
											pollingDelayListSelectedChoice,
											(dialogInterfaceColor, i) -> {
												pollingDelayListSelectedChoice = i;
												viewBinding.pollingDelaySelected.setText(
														pollingDelayList[
																pollingDelayListSelectedChoice]);
												tinyDB.putInt("notificationsPollingDelayId", i);

												Notifications.stopWorker(ctx);
												Notifications.startWorker(ctx);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceColor.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});
	}
}
