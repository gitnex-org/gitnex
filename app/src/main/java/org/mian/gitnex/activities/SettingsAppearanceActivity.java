package org.mian.gitnex.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import java.util.LinkedHashMap;
import java.util.Locale;
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

	private static String[] customFontList;
	private static int customFontSelectedChoice = 0;
	private static String[] themeList;
	private static int themeSelectedChoice = 0;
	private static int langSelectedChoice = 0;
	private static String[] fragmentTabsAnimationList;
	private static int fragmentTabsAnimationSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsAppearanceBinding activitySettingsAppearanceBinding =
				ActivitySettingsAppearanceBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsAppearanceBinding.getRoot());

		LinkedHashMap<String, String> lang = new LinkedHashMap<>();
		lang.put("", getString(R.string.settingsLanguageSystem));
		for (String langCode : getResources().getStringArray(R.array.languages)) {
			lang.put(langCode, getLanguageDisplayName(langCode));
		}

		customFontList = getResources().getStringArray(R.array.fonts);

		fragmentTabsAnimationList = getResources().getStringArray(R.array.fragmentTabsAnimation);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || "S".equals(Build.VERSION.CODENAME)) {
			themeList = getResources().getStringArray(R.array.themesAndroid12);
		} else {
			themeList = getResources().getStringArray(R.array.themes);
		}

		activitySettingsAppearanceBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		String lightMinute = String.valueOf(tinyDB.getInt("lightThemeTimeMinute"));
		String lightHour = String.valueOf(tinyDB.getInt("lightThemeTimeHour"));
		if (lightMinute.length() == 1) {
			lightMinute = "0" + lightMinute;
		}
		if (lightHour.length() == 1) {
			lightHour = "0" + lightHour;
		}

		String darkMinute = String.valueOf(tinyDB.getInt("darkThemeTimeMinute"));
		String darkHour = String.valueOf(tinyDB.getInt("darkThemeTimeHour"));
		if (darkMinute.length() == 1) {
			darkMinute = "0" + darkMinute;
		}
		if (darkHour.length() == 1) {
			darkHour = "0" + darkHour;
		}

		fragmentTabsAnimationSelectedChoice = tinyDB.getInt("fragmentTabsAnimationId", 0);
		customFontSelectedChoice = tinyDB.getInt("customFontId", 1);
		themeSelectedChoice = tinyDB.getInt("themeId", 6); // use system theme as default

		activitySettingsAppearanceBinding.lightThemeSelectedTime.setText(
				ctx.getResources()
						.getString(R.string.settingsThemeTimeSelectedHint, lightHour, lightMinute));
		activitySettingsAppearanceBinding.darkThemeSelectedTime.setText(
				ctx.getResources()
						.getString(R.string.settingsThemeTimeSelectedHint, darkHour, darkMinute));
		activitySettingsAppearanceBinding.customFontSelected.setText(
				customFontList[customFontSelectedChoice]);
		activitySettingsAppearanceBinding.themeSelected.setText(themeList[themeSelectedChoice]);
		activitySettingsAppearanceBinding.fragmentTabsAnimationFrameSelected.setText(
				fragmentTabsAnimationList[fragmentTabsAnimationSelectedChoice]);

		if (themeList[themeSelectedChoice].startsWith("Auto")) {
			activitySettingsAppearanceBinding.darkThemeTimeSelectionFrame.setVisibility(
					View.VISIBLE);
			activitySettingsAppearanceBinding.lightThemeTimeSelectionFrame.setVisibility(
					View.VISIBLE);
		} else {
			activitySettingsAppearanceBinding.darkThemeTimeSelectionFrame.setVisibility(View.GONE);
			activitySettingsAppearanceBinding.lightThemeTimeSelectionFrame.setVisibility(View.GONE);
		}

		activitySettingsAppearanceBinding.switchCounterBadge.setChecked(
				tinyDB.getBoolean("enableCounterBadges", true));

		// counter badge switcher
		activitySettingsAppearanceBinding.switchCounterBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("enableCounterBadges", isChecked);
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});
		activitySettingsAppearanceBinding.counterBadgeFrame.setOnClickListener(
				v ->
						activitySettingsAppearanceBinding.switchCounterBadge.setChecked(
								!activitySettingsAppearanceBinding.switchCounterBadge.isChecked()));

		// show labels in lists(issues, pr) - default is color dots
		activitySettingsAppearanceBinding.switchLabelsInListBadge.setChecked(
				tinyDB.getBoolean("showLabelsInList", false));

		activitySettingsAppearanceBinding.switchLabelsInListBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					tinyDB.putBoolean("showLabelsInList", isChecked);
					Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				});
		activitySettingsAppearanceBinding.labelsInListFrame.setOnClickListener(
				v ->
						activitySettingsAppearanceBinding.switchLabelsInListBadge.setChecked(
								!activitySettingsAppearanceBinding.switchLabelsInListBadge
										.isChecked()));

		// theme selection dialog
		activitySettingsAppearanceBinding.themeSelectionFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.themeSelectorDialogTitle)
									.setSingleChoiceItems(
											themeList,
											themeSelectedChoice,
											(dialogInterfaceTheme, i) -> {
												themeSelectedChoice = i;
												activitySettingsAppearanceBinding.themeSelected
														.setText(themeList[i]);
												tinyDB.putInt("themeId", i);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceTheme.dismiss();
												Toasty.success(
														appCtx,
														getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		activitySettingsAppearanceBinding.lightThemeTimeSelectionFrame.setOnClickListener(
				view -> lightTimePicker());

		activitySettingsAppearanceBinding.darkThemeTimeSelectionFrame.setOnClickListener(
				view -> darkTimePicker());

		// custom font dialog
		activitySettingsAppearanceBinding.customFontFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.settingsCustomFontSelectorDialogTitle)
									.setCancelable(customFontSelectedChoice != -1)
									.setSingleChoiceItems(
											customFontList,
											customFontSelectedChoice,
											(dialogInterfaceCustomFont, i) -> {
												customFontSelectedChoice = i;
												activitySettingsAppearanceBinding.customFontSelected
														.setText(customFontList[i]);
												tinyDB.putInt("customFontId", i);
												AppUtil.typeface = null; // reset typeface
												FontsOverride.setDefaultFont(this);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceCustomFont.dismiss();
												Toasty.success(
														appCtx,
														appCtx.getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// fragment tabs animation dialog
		activitySettingsAppearanceBinding.fragmentTabsAnimationFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.fragmentTabsAnimationHeader)
									.setCancelable(fragmentTabsAnimationSelectedChoice != -1)
									.setSingleChoiceItems(
											fragmentTabsAnimationList,
											fragmentTabsAnimationSelectedChoice,
											(dialogInterfaceCustomFont, i) -> {
												fragmentTabsAnimationSelectedChoice = i;
												activitySettingsAppearanceBinding
														.fragmentTabsAnimationFrameSelected.setText(
														fragmentTabsAnimationList[i]);
												tinyDB.putInt("fragmentTabsAnimationId", i);
												AppUtil.typeface = null; // reset typeface
												FontsOverride.setDefaultFont(this);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceCustomFont.dismiss();
												Toasty.success(
														appCtx,
														appCtx.getResources()
																.getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// language selector dialog
		activitySettingsAppearanceBinding.helpTranslate.setOnClickListener(
				v12 ->
						AppUtil.openUrlInBrowser(
								this, getResources().getString(R.string.crowdInLink)));

		langSelectedChoice = tinyDB.getInt("langId");
		activitySettingsAppearanceBinding.tvLanguageSelected.setText(
				lang.get(lang.keySet().toArray(new String[0])[langSelectedChoice]));

		// language dialog
		activitySettingsAppearanceBinding.langFrame.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.settingsLanguageSelectorDialogTitle)
									.setCancelable(langSelectedChoice != -1)
									.setNeutralButton(getString(R.string.cancelButton), null)
									.setSingleChoiceItems(
											lang.values().toArray(new String[0]),
											langSelectedChoice,
											(dialogInterface, i) -> {
												String selectedLanguage =
														lang.keySet().toArray(new String[0])[i];
												tinyDB.putInt("langId", i);
												tinyDB.putString("locale", selectedLanguage);

												SettingsFragment.refreshParent = true;
												this.overridePendingTransition(0, 0);
												dialogInterface.dismiss();
												Toasty.success(
														appCtx,
														getResources()
																.getString(R.string.settingsSave));
												this.recreate();
											});

					materialAlertDialogBuilder.create().show();
				});
	}

	public void lightTimePicker() {

		TinyDB db = TinyDB.getInstance(ctx);

		int hour = db.getInt("lightThemeTimeHour");
		int minute = db.getInt("lightThemeTimeMinute");

		MaterialTimePicker materialTimePicker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();

		materialTimePicker.addOnPositiveButtonClickListener(
				selection -> {
					db.putInt("lightThemeTimeHour", materialTimePicker.getHour());
					db.putInt("lightThemeTimeMinute", materialTimePicker.getMinute());
					SettingsFragment.refreshParent = true;
					overridePendingTransition(0, 0);
					Toasty.success(ctx, getString(R.string.settingsSave));
					recreate();
				});

		materialTimePicker.show(getSupportFragmentManager(), "fragmentManager");
	}

	public void darkTimePicker() {

		TinyDB db = TinyDB.getInstance(ctx);

		int hour = db.getInt("darkThemeTimeHour");
		int minute = db.getInt("darkThemeTimeMinute");

		MaterialTimePicker materialTimePicker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();

		materialTimePicker.addOnPositiveButtonClickListener(
				selection -> {
					db.putInt("darkThemeTimeHour", materialTimePicker.getHour());
					db.putInt("darkThemeTimeMinute", materialTimePicker.getMinute());
					SettingsFragment.refreshParent = true;
					overridePendingTransition(0, 0);
					Toasty.success(ctx, getString(R.string.settingsSave));
					recreate();
				});

		materialTimePicker.show(getSupportFragmentManager(), "fragmentManager");
	}

	private static String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");
		Locale translated = new Locale(langCode);
		return String.format(
				"%s (%s)",
				translated.getDisplayName(translated), translated.getDisplayName(english));
	}
}
