package org.mian.gitnex.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsAppearanceBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author M M Arif
 */

public class SettingsAppearanceActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static String[] timeList;
	private static int timeSelectedChoice = 0;

	private static String[] customFontList;
	private static int customFontSelectedChoice = 0;

	private static String[] themeList;
	private static int themeSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsAppearanceBinding activitySettingsAppearanceBinding = ActivitySettingsAppearanceBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsAppearanceBinding.getRoot());

		ImageView closeActivity = activitySettingsAppearanceBinding.close;

		LinearLayout timeFrame = activitySettingsAppearanceBinding.timeFrame;
		LinearLayout customFontFrame = activitySettingsAppearanceBinding.customFontFrame;
		LinearLayout themeFrame = activitySettingsAppearanceBinding.themeSelectionFrame;
		LinearLayout lightTimeFrame = activitySettingsAppearanceBinding.lightThemeTimeSelectionFrame;
		LinearLayout darkTimeFrame = activitySettingsAppearanceBinding.darkThemeTimeSelectionFrame;

		SwitchMaterial counterBadgesSwitch = activitySettingsAppearanceBinding.switchCounterBadge;

		timeList = getResources().getStringArray(R.array.timeFormats);
		customFontList = getResources().getStringArray(R.array.fonts);
		themeList = getResources().getStringArray(R.array.themes);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		String lightMinute = String.valueOf(tinyDB.getInt("lightThemeTimeMinute"));
		String lightHour = String.valueOf(tinyDB.getInt("lightThemeTimeHour"));
		if(lightMinute.length() == 1) lightMinute = "0" + lightMinute;
		if(lightHour.length() == 1) lightHour = "0" + lightHour;

		String darkMinute = String.valueOf(tinyDB.getInt("darkThemeTimeMinute"));
		String darkHour = String.valueOf(tinyDB.getInt("darkThemeTimeHour"));
		if(darkMinute.length() == 1) darkMinute = "0" + darkMinute;
		if(darkHour.length() == 1) darkHour = "0" + darkHour;

		timeSelectedChoice = tinyDB.getInt("timeId");
		customFontSelectedChoice = tinyDB.getInt("customFontId", 1);
		themeSelectedChoice = tinyDB.getInt("themeId", 6); // use system theme as default

		activitySettingsAppearanceBinding.lightThemeSelectedTime.setText(ctx.getResources().getString(R.string.settingsThemeTimeSelectedHint, lightHour,
			lightMinute));
		activitySettingsAppearanceBinding.darkThemeSelectedTime.setText(ctx.getResources().getString(R.string.settingsThemeTimeSelectedHint, darkHour,
			darkMinute));
		activitySettingsAppearanceBinding.tvDateTimeSelected.setText(timeList[timeSelectedChoice]);
		activitySettingsAppearanceBinding.customFontSelected.setText(customFontList[customFontSelectedChoice]);
		activitySettingsAppearanceBinding.themeSelected.setText(themeList[themeSelectedChoice]);

		if(themeList[themeSelectedChoice].startsWith("Auto")) {
			darkTimeFrame.setVisibility(View.VISIBLE);
			lightTimeFrame.setVisibility(View.VISIBLE);
		}
		else {
			darkTimeFrame.setVisibility(View.GONE);
			lightTimeFrame.setVisibility(View.GONE);
		}

		counterBadgesSwitch.setChecked(tinyDB.getBoolean("enableCounterBadges", true));

		// counter badge switcher
		counterBadgesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("enableCounterBadges", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
		activitySettingsAppearanceBinding.counterBadgeFrame.setOnClickListener(v -> counterBadgesSwitch.setChecked(!counterBadgesSwitch.isChecked()));

		// show labels in lists(issues, pr) - default is color dots
		activitySettingsAppearanceBinding.switchLabelsInListBadge.setChecked(tinyDB.getBoolean("showLabelsInList", false));

		activitySettingsAppearanceBinding.switchLabelsInListBadge.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("showLabelsInList", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});
		activitySettingsAppearanceBinding.labelsInListFrame.setOnClickListener(v -> activitySettingsAppearanceBinding.switchLabelsInListBadge.setChecked(!activitySettingsAppearanceBinding.switchLabelsInListBadge.isChecked()));

		// theme selection dialog
		themeFrame.setOnClickListener(view -> {

			MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.themeSelectorDialogTitle)
				.setSingleChoiceItems(themeList, themeSelectedChoice, (dialogInterfaceTheme, i) -> {

					themeSelectedChoice = i;
					activitySettingsAppearanceBinding.themeSelected.setText(themeList[i]);
					tinyDB.putInt("themeId", i);

					SettingsFragment.refreshParent = true;
					this.recreate();
					this.overridePendingTransition(0, 0);
					dialogInterfaceTheme.dismiss();
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});

			materialAlertDialogBuilder.create().show();
		});

		lightTimeFrame.setOnClickListener(view -> {
			LightTimePicker timePicker = new LightTimePicker();
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		darkTimeFrame.setOnClickListener(view -> {
			DarkTimePicker timePicker = new DarkTimePicker();
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.settingsCustomFontSelectorDialogTitle)
				.setCancelable(customFontSelectedChoice != -1)
				.setSingleChoiceItems(customFontList, customFontSelectedChoice, (dialogInterfaceCustomFont, i) -> {

					customFontSelectedChoice = i;
					activitySettingsAppearanceBinding.customFontSelected.setText(customFontList[i]);
					tinyDB.putInt("customFontId", i);
					AppUtil.typeface = null; // reset typeface
					FontsOverride.setDefaultFont(this);

					SettingsFragment.refreshParent = true;
					this.recreate();
					this.overridePendingTransition(0, 0);
					dialogInterfaceCustomFont.dismiss();
					Toasty.success(appCtx, appCtx.getResources().getString(R.string.settingsSave));
				});

			materialAlertDialogBuilder.create().show();
		});

		// time and date dialog
		timeFrame.setOnClickListener(view -> {

			MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.settingsTimeSelectorDialogTitle)
				.setCancelable(timeSelectedChoice != -1)
				.setSingleChoiceItems(timeList, timeSelectedChoice, (dialogInterfaceTime, i) -> {

					timeSelectedChoice = i;
					activitySettingsAppearanceBinding.tvDateTimeSelected.setText(timeList[i]);
					tinyDB.putInt("timeId", i);

					switch(i) {
						case 0:
							tinyDB.putString("dateFormat", "pretty");
							break;
						case 1:
							tinyDB.putString("dateFormat", "normal");
							break;
					}

					dialogInterfaceTime.dismiss();
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});

			materialAlertDialogBuilder.create().show();
		});

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

	public static class LightTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		TinyDB db = TinyDB.getInstance(getContext());

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("lightThemeTimeHour");
			int minute = db.getInt("lightThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			db.putInt("lightThemeTimeHour", hourOfDay);
			db.putInt("lightThemeTimeMinute", minute);
			SettingsFragment.refreshParent = true;
			requireActivity().overridePendingTransition(0, 0);
			this.dismiss();
			Toasty.success(requireActivity().getApplicationContext(), requireContext().getResources().getString(R.string.settingsSave));
			requireActivity().recreate();
		}
	}

	public static class DarkTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		TinyDB db = TinyDB.getInstance(getContext());

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("darkThemeTimeHour");
			int minute = db.getInt("darkThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		@Override
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			db.putInt("darkThemeTimeHour", hourOfDay);
			db.putInt("darkThemeTimeMinute", minute);
			SettingsFragment.refreshParent = true;
			requireActivity().overridePendingTransition(0, 0);
			this.dismiss();
			Toasty.success(requireActivity().getApplicationContext(), requireContext().getResources().getString(R.string.settingsSave));
			requireActivity().recreate();
		}
	}

}
