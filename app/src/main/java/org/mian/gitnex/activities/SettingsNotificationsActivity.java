package org.mian.gitnex.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.flag.BubbleFlag;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author M M Arif
 * @author opyale
 */
public class SettingsNotificationsActivity extends BaseActivity {

	private ActivitySettingsNotificationsBinding viewBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsNotificationsBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		viewBinding.pollingDelaySelected.setText(
				String.format(
						getString(R.string.pollingDelaySelectedText),
						tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay)));
		viewBinding.chooseColorState.setCardBackgroundColor(
				tinyDB.getInt("notificationsLightColor", Color.GREEN));

		viewBinding.enableNotificationsMode.setChecked(
				tinyDB.getBoolean("notificationsEnabled", true));
		viewBinding.enableLightsMode.setChecked(
				tinyDB.getBoolean("notificationsEnableLights", false));
		viewBinding.enableVibrationMode.setChecked(
				tinyDB.getBoolean("notificationsEnableVibration", false));

		if (!viewBinding.enableNotificationsMode.isChecked()) {
			AppUtil.setMultiVisibility(
					View.GONE,
					viewBinding.chooseColorFrame,
					viewBinding.enableLightsFrame,
					viewBinding.enableVibrationFrame,
					viewBinding.pollingDelayFrame);
		}

		if (!viewBinding.enableLightsMode.isChecked()) {
			viewBinding.chooseColorFrame.setVisibility(View.GONE);
		}

		viewBinding.enableNotificationsMode.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("notificationsEnabled", isChecked);

					if (isChecked) {
						Notifications.startWorker(ctx);
						AppUtil.setMultiVisibility(
								View.VISIBLE,
								viewBinding.chooseColorFrame,
								viewBinding.enableLightsFrame,
								viewBinding.enableVibrationFrame,
								viewBinding.pollingDelayFrame);
					} else {
						Notifications.stopWorker(ctx);
						AppUtil.setMultiVisibility(
								View.GONE,
								viewBinding.chooseColorFrame,
								viewBinding.enableLightsFrame,
								viewBinding.enableVibrationFrame,
								viewBinding.pollingDelayFrame);
					}

					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});
		viewBinding.enableNotificationsFrame.setOnClickListener(
				v ->
						viewBinding.enableNotificationsMode.setChecked(
								!viewBinding.enableNotificationsMode.isChecked()));

		// polling delay
		viewBinding.pollingDelayFrame.setOnClickListener(
				v -> {
					NumberPicker numberPicker = new NumberPicker(ctx);
					numberPicker.setMinValue(Constants.minimumPollingDelay);
					numberPicker.setMaxValue(Constants.maximumPollingDelay);
					numberPicker.setValue(
							tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay));
					numberPicker.setWrapSelectorWheel(true);

					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.pollingDelayDialogHeaderText)
									.setMessage(
											getString(R.string.pollingDelayDialogDescriptionText))
									.setCancelable(true)
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											getString(R.string.okButton),
											(dialog, which) -> {
												tinyDB.putInt(
														"pollingDelayMinutes",
														numberPicker.getValue());

												Notifications.stopWorker(ctx);
												Notifications.startWorker(ctx);

												viewBinding.pollingDelaySelected.setText(
														String.format(
																getString(
																		R.string
																				.pollingDelaySelectedText),
																numberPicker.getValue()));
												Toasty.success(
														appCtx,
														getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.setView(numberPicker);
					materialAlertDialogBuilder.create().show();
				});

		// lights switcher
		viewBinding.enableLightsMode.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (!isChecked) {
						viewBinding.chooseColorFrame.setVisibility(View.GONE);
					} else {
						viewBinding.chooseColorFrame.setVisibility(View.VISIBLE);
					}

					tinyDB.putBoolean("notificationsEnableLights", isChecked);
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});
		viewBinding.enableLightsFrame.setOnClickListener(
				v ->
						viewBinding.enableLightsMode.setChecked(
								!viewBinding.enableLightsMode.isChecked()));

		// lights color chooser
		viewBinding.chooseColorFrame.setOnClickListener(
				v -> {
					ColorPickerDialog.Builder builder =
							new ColorPickerDialog.Builder(this)
									.setPreferenceName("colorPickerDialogLabels")
									.setPositiveButton(
											getString(R.string.okButton),
											(ColorEnvelopeListener)
													(envelope, clicked) -> {
														tinyDB.putInt(
																"notificationsLightColor",
																envelope.getColor());
														viewBinding.chooseColorState
																.setCardBackgroundColor(
																		envelope.getColor());
													})
									.attachAlphaSlideBar(true)
									.attachBrightnessSlideBar(true)
									.setBottomSpace(16);

					builder.getColorPickerView().setFlagView(new BubbleFlag(this));

					builder.getColorPickerView().setLifecycleOwner(this);
					builder.show();
				});

		// vibration switcher
		viewBinding.enableVibrationMode.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("notificationsEnableVibration", isChecked);
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});
		viewBinding.enableVibrationFrame.setOnClickListener(
				v ->
						viewBinding.enableVibrationMode.setChecked(
								!viewBinding.enableVibrationMode.isChecked()));
	}
}
