package org.mian.gitnex.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import java.util.LinkedHashMap;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsAppearanceBinding;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author M M Arif
 */
public class SettingsAppearanceActivity extends BaseActivity {

	private static String[] customFontList;
	private static int customFontSelectedChoice;
	private static String[] themeList;
	private static int themeSelectedChoice;
	private static int langSelectedChoice;
	private static String[] fragmentTabsAnimationList;
	private static int fragmentTabsAnimationSelectedChoice;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsAppearanceBinding activitySettingsAppearanceBinding =
				ActivitySettingsAppearanceBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsAppearanceBinding.getRoot());

		LinkedHashMap<String, String> lang = new LinkedHashMap<>();
		lang.put("sys", getString(R.string.settingsLanguageSystem));
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

		String lightMinute =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY));
		String lightHour =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY));
		if (lightMinute.length() == 1) {
			lightMinute = "0" + lightMinute;
		}
		if (lightHour.length() == 1) {
			lightHour = "0" + lightHour;
		}

		String darkMinute =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY));
		String darkHour =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY));
		if (darkMinute.length() == 1) {
			darkMinute = "0" + darkMinute;
		}
		if (darkHour.length() == 1) {
			darkHour = "0" + darkHour;
		}

		fragmentTabsAnimationSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_TABS_ANIMATION_KEY));
		customFontSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_FONT_KEY));
		themeSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_KEY));

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
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_COUNTER_KEY)));

		// counter badge switcher
		activitySettingsAppearanceBinding.switchCounterBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx, String.valueOf(isChecked), AppDatabaseSettings.APP_COUNTER_KEY);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		activitySettingsAppearanceBinding.counterBadgeFrame.setOnClickListener(
				v ->
						activitySettingsAppearanceBinding.switchCounterBadge.setChecked(
								!activitySettingsAppearanceBinding.switchCounterBadge.isChecked()));

		// show labels in lists(issues, pr) - default is color dots
		activitySettingsAppearanceBinding.switchLabelsInListBadge.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_LABELS_IN_LIST_KEY)));

		activitySettingsAppearanceBinding.switchLabelsInListBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_LABELS_IN_LIST_KEY);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
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
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_THEME_KEY);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceTheme.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
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
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_FONT_KEY);

												new Handler()
														.postDelayed(
																() -> {
																	AppUtil.typeface =
																			null; // reset typeface
																	FontsOverride.setDefaultFont(
																			this);
																	SettingsFragment.refreshParent =
																			true;
																	this.recreate();
																	this.overridePendingTransition(
																			0, 0);
																},
																1000);

												dialogInterfaceCustomFont.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
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
											(dialogInterfaceTabsAnimation, i) -> {
												fragmentTabsAnimationSelectedChoice = i;
												activitySettingsAppearanceBinding
														.fragmentTabsAnimationFrameSelected.setText(
														fragmentTabsAnimationList[i]);
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														String.valueOf(i),
														AppDatabaseSettings.APP_TABS_ANIMATION_KEY);

												SettingsFragment.refreshParent = true;
												this.recreate();
												this.overridePendingTransition(0, 0);
												dialogInterfaceTabsAnimation.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});

					materialAlertDialogBuilder.create().show();
				});

		// language selector dialog
		activitySettingsAppearanceBinding.helpTranslate.setOnClickListener(
				v12 ->
						AppUtil.openUrlInBrowser(
								this, getResources().getString(R.string.crowdInLink)));

		String[] locale =
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");
		langSelectedChoice = Integer.parseInt(locale[0]);
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
												AppDatabaseSettings.updateSettingsValue(
														ctx,
														i + "|" + selectedLanguage,
														AppDatabaseSettings.APP_LOCALE_KEY);

												SettingsFragment.refreshParent = true;
												this.overridePendingTransition(0, 0);
												dialogInterface.dismiss();
												SnackBar.success(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.settingsSave));
												this.recreate();
											});

					materialAlertDialogBuilder.create().show();
				});
	}

	public void lightTimePicker() {

		int hour =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY));
		int minute =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY));

		MaterialTimePicker materialTimePicker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();

		materialTimePicker.addOnPositiveButtonClickListener(
				selection -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(materialTimePicker.getHour()),
							AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY);
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(materialTimePicker.getMinute()),
							AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY);
					SettingsFragment.refreshParent = true;
					overridePendingTransition(0, 0);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
					recreate();
				});

		materialTimePicker.show(getSupportFragmentManager(), "fragmentManager");
	}

	public void darkTimePicker() {

		int hour =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY));
		int minute =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY));

		MaterialTimePicker materialTimePicker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();

		materialTimePicker.addOnPositiveButtonClickListener(
				selection -> {
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(materialTimePicker.getHour()),
							AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY);
					AppDatabaseSettings.updateSettingsValue(
							ctx,
							String.valueOf(materialTimePicker.getMinute()),
							AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY);
					SettingsFragment.refreshParent = true;
					overridePendingTransition(0, 0);
					SnackBar.success(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.settingsSave));
					recreate();
				});

		materialTimePicker.show(getSupportFragmentManager(), "fragmentManager");
	}

	private static String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");

		String[] multiCodeLang = langCode.split("-");
		String countryCode;
		if (langCode.contains("-")) {
			langCode = multiCodeLang[0];
			countryCode = multiCodeLang[1];
		} else {
			countryCode = "";
		}

		Locale translated = new Locale(langCode, countryCode);
		return String.format(
				"%s (%s)",
				translated.getDisplayName(translated), translated.getDisplayName(english));
	}
}
