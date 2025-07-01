package org.mian.gitnex.fragments;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import java.util.LinkedHashMap;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetSettingsAppearanceBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author mmarif
 */
public class BottomSheetSettingsAppearanceFragment extends BottomSheetDialogFragment {

	private static final String TAG = "BottomSheetSettingsAppearance";
	private BottomSheetSettingsAppearanceBinding binding;
	private static int customFontSelectedChoice;
	private static String[] themeList;
	private static int themeSelectedChoice;
	private static int langSelectedChoice;
	private static int fragmentTabsAnimationSelectedChoice;
	private LinkedHashMap<String, String> lang;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsAppearanceBinding.inflate(inflater, container, false);

		lang = new LinkedHashMap<>();
		lang.put("sys", getString(R.string.settingsLanguageSystem));
		for (String langCode : getResources().getStringArray(R.array.languages)) {
			lang.put(langCode, getLanguageDisplayName(langCode));
		}

		String[] customFontList = getResources().getStringArray(R.array.fonts);
		String[] fragmentTabsAnimationList =
				getResources().getStringArray(R.array.fragmentTabsAnimation);
		themeList =
				Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || "S".equals(Build.VERSION.CODENAME)
						? getResources().getStringArray(R.array.themesAndroid12)
						: getResources().getStringArray(R.array.themes);

		customFontSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_FONT_KEY));
		themeSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_THEME_KEY));
		fragmentTabsAnimationSelectedChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_TABS_ANIMATION_KEY));
		String[] locale =
				AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");
		langSelectedChoice = Integer.parseInt(locale[0]);

		String lightMinute =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY));
		String lightHour =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY));
		lightMinute = lightMinute.length() == 1 ? "0" + lightMinute : lightMinute;
		lightHour = lightHour.length() == 1 ? "0" + lightHour : lightHour;

		String darkMinute =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY));
		String darkHour =
				String.valueOf(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY));
		darkMinute = darkMinute.length() == 1 ? "0" + darkMinute : darkMinute;
		darkHour = darkHour.length() == 1 ? "0" + darkHour : darkHour;

		for (int i = 0; i < themeList.length; i++) {
			Chip chip = (Chip) inflater.inflate(R.layout.chip_item, binding.themeChipGroup, false);
			chip.setId(View.generateViewId());
			chip.setText(themeList[i]);
			chip.setCheckable(true);
			chip.setClickable(true);
			chip.setFocusable(true);
			if (i == themeSelectedChoice) chip.setChecked(true);
			binding.themeChipGroup.addView(chip);
		}

		for (int i = 0; i < customFontList.length; i++) {
			Chip chip =
					(Chip) inflater.inflate(R.layout.chip_item, binding.customFontChipGroup, false);
			chip.setId(View.generateViewId());
			chip.setText(customFontList[i]);
			chip.setCheckable(true);
			chip.setClickable(true);
			chip.setFocusable(true);
			if (i == customFontSelectedChoice) chip.setChecked(true);
			binding.customFontChipGroup.addView(chip);
		}

		for (int i = 0; i < fragmentTabsAnimationList.length; i++) {
			Chip chip =
					(Chip)
							inflater.inflate(
									R.layout.chip_item,
									binding.fragmentTabsAnimationChipGroup,
									false);
			chip.setId(View.generateViewId());
			chip.setText(fragmentTabsAnimationList[i]);
			chip.setCheckable(true);
			chip.setClickable(true);
			chip.setFocusable(true);
			if (i == fragmentTabsAnimationSelectedChoice) chip.setChecked(true);
			binding.fragmentTabsAnimationChipGroup.addView(chip);
		}

		binding.lightThemeSelectedTime.setText(
				getResources()
						.getString(R.string.settingsThemeTimeSelectedHint, lightHour, lightMinute));
		binding.darkThemeSelectedTime.setText(
				getResources()
						.getString(R.string.settingsThemeTimeSelectedHint, darkHour, darkMinute));
		binding.tvLanguageSelected.setText(
				lang.get(lang.keySet().toArray(new String[0])[langSelectedChoice]));
		binding.switchCounterBadge.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_COUNTER_KEY)));
		binding.switchHideEmailLangInProfile.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY)));
		binding.switchHideEmailNavDrawer.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_USER_HIDE_EMAIL_IN_NAV_KEY)));
		binding.switchLabelsInListBadge.setChecked(
				Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_LABELS_IN_LIST_KEY)));

		binding.lightThemeTimeSelectionFrame.setVisibility(
				themeList[themeSelectedChoice].startsWith("Auto") ? View.VISIBLE : View.GONE);
		binding.darkThemeTimeSelectionFrame.setVisibility(
				themeList[themeSelectedChoice].startsWith("Auto") ? View.VISIBLE : View.GONE);

		binding.themeChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					Log.d(TAG, "Theme chip checked: " + checkedIds);
					if (checkedIds.size() == 1) {
						int newSelection = getThemeChipPosition(checkedIds.get(0));
						Log.d(TAG, "Theme new selection: " + newSelection);
						if (newSelection != themeSelectedChoice) {
							themeSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_THEME_KEY);
							binding.lightThemeTimeSelectionFrame.setVisibility(
									themeList[newSelection].startsWith("Auto")
											? View.VISIBLE
											: View.GONE);
							binding.darkThemeTimeSelectionFrame.setVisibility(
									themeList[newSelection].startsWith("Auto")
											? View.VISIBLE
											: View.GONE);
							SettingsFragment.refreshParent = true;
							requireActivity().recreate();
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.lightThemeTimeSelectionFrame.setOnClickListener(v -> lightTimePicker());
		binding.darkThemeTimeSelectionFrame.setOnClickListener(v -> darkTimePicker());

		binding.customFontChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					Log.d(TAG, "Font chip checked: " + checkedIds);
					if (checkedIds.size() == 1) {
						int newSelection = getCustomFontChipPosition(checkedIds.get(0));
						Log.d(TAG, "Font new selection: " + newSelection);
						if (newSelection != customFontSelectedChoice) {
							customFontSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_FONT_KEY);
							new Handler()
									.postDelayed(
											() -> {
												AppUtil.typeface = null; // reset typeface
												FontsOverride.setDefaultFont(requireContext());
												SettingsFragment.refreshParent = true;
												requireActivity().recreate();
											},
											1000);
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.fragmentTabsAnimationChipGroup.setOnCheckedStateChangeListener(
				(group, checkedIds) -> {
					Log.d(TAG, "Tabs animation chip checked: " + checkedIds);
					if (checkedIds.size() == 1) {
						int newSelection = getFragmentTabsAnimationChipPosition(checkedIds.get(0));
						Log.d(TAG, "Tabs animation new selection: " + newSelection);
						if (newSelection != fragmentTabsAnimationSelectedChoice) {
							fragmentTabsAnimationSelectedChoice = newSelection;
							AppDatabaseSettings.updateSettingsValue(
									requireContext(),
									String.valueOf(newSelection),
									AppDatabaseSettings.APP_TABS_ANIMATION_KEY);
							SettingsFragment.refreshParent = true;
							requireActivity().recreate();
							SnackBar.success(
									requireContext(),
									requireActivity().findViewById(android.R.id.content),
									getString(R.string.settingsSave));
						}
					}
				});

		binding.counterBadgeFrame.setOnClickListener(
				v ->
						binding.switchCounterBadge.setChecked(
								!binding.switchCounterBadge.isChecked()));
		binding.switchCounterBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_COUNTER_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.hideEmailLangInProfileFrame.setOnClickListener(
				v ->
						binding.switchHideEmailLangInProfile.setChecked(
								!binding.switchHideEmailLangInProfile.isChecked()));
		binding.switchHideEmailLangInProfile.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.hideEmailNavDrawerFrame.setOnClickListener(
				v ->
						binding.switchHideEmailNavDrawer.setChecked(
								!binding.switchHideEmailNavDrawer.isChecked()));
		binding.switchHideEmailNavDrawer.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_USER_HIDE_EMAIL_IN_NAV_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.labelsInListFrame.setOnClickListener(
				v ->
						binding.switchLabelsInListBadge.setChecked(
								!binding.switchLabelsInListBadge.isChecked()));
		binding.switchLabelsInListBadge.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(isChecked),
							AppDatabaseSettings.APP_LABELS_IN_LIST_KEY);
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});

		binding.langFrame.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder builder =
							new MaterialAlertDialogBuilder(requireContext())
									.setTitle(R.string.settingsLanguageSelectorDialogTitle)
									.setCancelable(langSelectedChoice != -1)
									.setNeutralButton(R.string.cancelButton, null)
									.setSingleChoiceItems(
											lang.values().toArray(new String[0]),
											langSelectedChoice,
											(dialog, i) -> {
												String selectedLanguage =
														lang.keySet().toArray(new String[0])[i];
												AppDatabaseSettings.updateSettingsValue(
														requireContext(),
														i + "|" + selectedLanguage,
														AppDatabaseSettings.APP_LOCALE_KEY);
												SettingsFragment.refreshParent = true;
												requireActivity().recreate();
												dialog.dismiss();
												SnackBar.success(
														requireContext(),
														requireActivity()
																.findViewById(android.R.id.content),
														getString(R.string.settingsSave));
											});
					builder.create().show();
				});

		binding.helpTranslate.setOnClickListener(
				v ->
						AppUtil.openUrlInBrowser(
								requireContext(), getResources().getString(R.string.crowdInLink)));

		return binding.getRoot();
	}

	private void lightTimePicker() {
		int hour =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY));
		int minute =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY));
		MaterialTimePicker picker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();
		picker.addOnPositiveButtonClickListener(
				v -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(picker.getHour()),
							AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY);
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(picker.getMinute()),
							AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY);
					SettingsFragment.refreshParent = true;
					requireActivity().recreate();
					String minuteStr =
							picker.getMinute() < 10
									? "0" + picker.getMinute()
									: String.valueOf(picker.getMinute());
					String hourStr =
							picker.getHour() < 10
									? "0" + picker.getHour()
									: String.valueOf(picker.getHour());
					binding.lightThemeSelectedTime.setText(
							getResources()
									.getString(
											R.string.settingsThemeTimeSelectedHint,
											hourStr,
											minuteStr));
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		picker.show(getParentFragmentManager(), "lightTimePicker");
	}

	private void darkTimePicker() {
		int hour =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(),
								AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY));
		int minute =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								requireContext(), AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY));
		MaterialTimePicker picker =
				new MaterialTimePicker.Builder().setHour(hour).setMinute(minute).build();
		picker.addOnPositiveButtonClickListener(
				v -> {
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(picker.getHour()),
							AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY);
					AppDatabaseSettings.updateSettingsValue(
							requireContext(),
							String.valueOf(picker.getMinute()),
							AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY);
					SettingsFragment.refreshParent = true;
					requireActivity().recreate();
					String minuteStr =
							picker.getMinute() < 10
									? "0" + picker.getMinute()
									: String.valueOf(picker.getMinute());
					String hourStr =
							picker.getHour() < 10
									? "0" + picker.getHour()
									: String.valueOf(picker.getHour());
					binding.darkThemeSelectedTime.setText(
							getResources()
									.getString(
											R.string.settingsThemeTimeSelectedHint,
											hourStr,
											minuteStr));
					SnackBar.success(
							requireContext(),
							requireActivity().findViewById(android.R.id.content),
							getString(R.string.settingsSave));
				});
		picker.show(getParentFragmentManager(), "darkTimePicker");
	}

	private int getThemeChipPosition(int checkedId) {
		for (int i = 0; i < binding.themeChipGroup.getChildCount(); i++) {
			Chip chip = (Chip) binding.themeChipGroup.getChildAt(i);
			if (chip.getId() == checkedId) return i;
		}
		return themeSelectedChoice;
	}

	private int getCustomFontChipPosition(int checkedId) {
		for (int i = 0; i < binding.customFontChipGroup.getChildCount(); i++) {
			Chip chip = (Chip) binding.customFontChipGroup.getChildAt(i);
			if (chip.getId() == checkedId) return i;
		}
		return customFontSelectedChoice;
	}

	private int getFragmentTabsAnimationChipPosition(int checkedId) {
		for (int i = 0; i < binding.fragmentTabsAnimationChipGroup.getChildCount(); i++) {
			Chip chip = (Chip) binding.fragmentTabsAnimationChipGroup.getChildAt(i);
			if (chip.getId() == checkedId) return i;
		}
		return fragmentTabsAnimationSelectedChoice;
	}

	private String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");
		String[] multiCodeLang = langCode.split("-");
		String countryCode = langCode.contains("-") ? multiCodeLang[1] : "";
		langCode = multiCodeLang[0];
		Locale translated = new Locale(langCode, countryCode);
		return String.format(
				"%s (%s)",
				translated.getDisplayName(translated), translated.getDisplayName(english));
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
