package org.mian.gitnex.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import androidx.appcompat.app.AlertDialog;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsNotificationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.notifications.Notifications;

/**
 * Template Author M M Arif
 * Author opyale
 */

public class SettingsNotificationsActivity extends BaseActivity {

	private ActivitySettingsNotificationsBinding viewBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivitySettingsNotificationsBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		View.OnClickListener onClickListener = viewClose -> finish();

		viewBinding.close.setOnClickListener(onClickListener);

		viewBinding.pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay)));
		viewBinding.chooseColorState.setCardBackgroundColor(tinyDB.getInt("notificationsLightColor", Color.GREEN));

		viewBinding.enableNotificationsMode.setChecked(tinyDB.getBoolean("notificationsEnabled", true));
		viewBinding.enableLightsMode.setChecked(tinyDB.getBoolean("notificationsEnableLights", true));
		viewBinding.enableVibrationMode.setChecked(tinyDB.getBoolean("notificationsEnableVibration", true));

		viewBinding.enableNotificationsMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("notificationsEnabled", isChecked);

			if(isChecked) {
				Notifications.startWorker(ctx);
			} else {
				Notifications.stopWorker(ctx);
			}

			Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

		});

		// polling delay
		viewBinding.pollingDelayFrame.setOnClickListener(v -> {

			NumberPicker numberPicker = new NumberPicker(ctx);
			numberPicker.setMinValue(Constants.minimumPollingDelay);
			numberPicker.setMaxValue(Constants.maximumPollingDelay);
			numberPicker.setValue(tinyDB.getInt("pollingDelayMinutes", Constants.defaultPollingDelay));
			numberPicker.setWrapSelectorWheel(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(getString(R.string.pollingDelayDialogHeaderText));
			builder.setMessage(getString(R.string.pollingDelayDialogDescriptionText));

			builder.setCancelable(true);
			builder.setPositiveButton(getString(R.string.okButton), (dialog, which) -> {

				tinyDB.putInt("pollingDelayMinutes", numberPicker.getValue());

				Notifications.stopWorker(ctx);
				Notifications.startWorker(ctx);

				viewBinding.pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), numberPicker.getValue()));
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.setView(numberPicker);
			builder.create().show();

		});

		// lights switcher
		viewBinding.enableLightsMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("notificationsEnableLights", isChecked);
			Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

		});

		// lights color chooser
		viewBinding.chooseColorFrame.setOnClickListener(v -> {

			ColorPicker colorPicker = new ColorPicker(SettingsNotificationsActivity.this);
			colorPicker.setColor(tinyDB.getInt("notificationsLightColor", Color.GREEN));
			colorPicker.setCallback(color -> {

				tinyDB.putInt("notificationsLightColor", color);
				viewBinding.chooseColorState.setCardBackgroundColor(color);
				colorPicker.dismiss();
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
			});

			colorPicker.show();

		});

		// vibration switcher
		viewBinding.enableVibrationMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("notificationsEnableVibration", isChecked);
			Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

		});

	}

}
