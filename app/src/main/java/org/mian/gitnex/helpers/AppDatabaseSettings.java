package org.mian.gitnex.helpers;

import android.content.Context;
import org.mian.gitnex.database.api.AppSettingsApi;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.models.AppSettings;

/**
 * @author M M Arif
 */
public class AppDatabaseSettings {

	public static String APP_THEME_KEY = "app_theme";
	public static String APP_THEME_DEFAULT = "6";
	public static String APP_THEME_AUTO_LIGHT_HOUR_KEY = "app_theme_auto_light_hour";
	public static String APP_THEME_AUTO_LIGHT_HOUR_DEFAULT = "6";
	public static String APP_THEME_AUTO_LIGHT_MIN_KEY = "app_theme_auto_light_min";
	public static String APP_THEME_AUTO_LIGHT_MIN_DEFAULT = "0";
	public static String APP_THEME_AUTO_DARK_HOUR_KEY = "app_theme_auto_dark_hour";
	public static String APP_THEME_AUTO_DARK_HOUR_DEFAULT = "18";
	public static String APP_THEME_AUTO_DARK_MIN_KEY = "app_theme_auto_dark_min";
	public static String APP_THEME_AUTO_DARK_MIN_DEFAULT = "0";
	public static String APP_FONT_KEY = "app_font";
	public static String APP_FONT_DEFAULT = "3";
	public static String APP_TABS_ANIMATION_KEY = "app_tabs_animation";
	public static String APP_TABS_ANIMATION_DEFAULT = "0";
	public static String APP_LOCALE_KEY = "app_locale";
	public static String APP_LOCALE_KEY_DEFAULT = "0|sys";
	public static String APP_COUNTER_KEY = "app_counter_badges";
	public static String APP_COUNTER_DEFAULT = "true";
	public static String APP_LABELS_IN_LIST_KEY = "app_labels_in_list";
	public static String APP_LABELS_IN_LIST_DEFAULT = "false";
	public static String APP_LINK_HANDLER_KEY = "app_link_handler";
	public static String APP_LINK_HANDLER_DEFAULT = "0";
	public static String APP_HOME_SCREEN_KEY = "app_home_screen";
	public static String APP_HOME_SCREEN_DEFAULT = "0";
	public static String APP_CUSTOM_BROWSER_KEY = "app_custom_browser_tab";
	public static String APP_CUSTOM_BROWSER_DEFAULT = "true";
	public static String APP_DRAFTS_DELETION_KEY = "app_drafts_deletion";
	public static String APP_DRAFTS_DELETION_DEFAULT = "true";
	public static String APP_CRASH_REPORTS_KEY = "app_crash_reports";
	public static String APP_CRASH_REPORTS_DEFAULT = "false";
	public static String APP_CE_SYNTAX_HIGHLIGHT_KEY = "app_ce_syntax_highlight";
	public static String APP_CE_SYNTAX_HIGHLIGHT_DEFAULT = "0";
	public static String APP_CE_INDENTATION_KEY = "app_ce_indentation";
	public static String APP_CE_INDENTATION_DEFAULT = "0";
	public static String APP_CE_TABS_WIDTH_KEY = "app_ce_tabs_width";
	public static String APP_CE_TABS_WIDTH_DEFAULT = "1";
	public static String APP_NOTIFICATIONS_KEY = "app_notifications";
	public static String APP_NOTIFICATIONS_DEFAULT = "true";
	public static String APP_NOTIFICATIONS_DELAY_KEY = "app_notifications_delay";
	public static String APP_NOTIFICATIONS_DELAY_DEFAULT = "0";
	public static String APP_BIOMETRIC_KEY = "app_biometric";
	public static String APP_BIOMETRIC_DEFAULT = "false";
	public static String APP_BIOMETRIC_LIFE_CYCLE_KEY = "app_biometric_life_cycle";
	public static String APP_BIOMETRIC_LIFE_CYCLE_DEFAULT = "false";
	public static String APP_DATA_CACHE_KEY = "app_data_cache";
	public static String APP_DATA_CACHE_DEFAULT = "1";
	public static String APP_DATA_CACHE_SIZE_KEY = "app_data_cache_size";
	public static String APP_DATA_CACHE_SIZE_DEFAULT = "100 MB";
	public static String APP_IMAGES_CACHE_KEY = "app_images_cache";
	public static String APP_IMAGES_CACHE_DEFAULT = "1";
	public static String APP_IMAGES_CACHE_SIZE_KEY = "app_images_cache_size";
	public static String APP_IMAGES_CACHE_SIZE_DEFAULT = "100 MB";
	public static String APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY =
			"app_user_profile_hide_email_lang";
	public static String APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_DEFAULT = "false";
	public static String APP_USER_HIDE_EMAIL_IN_NAV_KEY = "app_user_hide_email_nav";
	public static String APP_USER_HIDE_EMAIL_IN_NAV_DEFAULT = "false";

	public static void initDefaultSettings(Context ctx) {

		AppSettingsApi appSettingsApi = BaseApi.getInstance(ctx, AppSettingsApi.class);
		assert appSettingsApi != null;

		if (appSettingsApi.fetchSettingCountByKey(APP_THEME_KEY) == 0) {
			appSettingsApi.insertNewSetting(APP_THEME_KEY, APP_THEME_DEFAULT, APP_THEME_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_THEME_AUTO_LIGHT_HOUR_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_THEME_AUTO_LIGHT_HOUR_KEY,
					APP_THEME_AUTO_LIGHT_HOUR_DEFAULT,
					APP_THEME_AUTO_LIGHT_HOUR_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_THEME_AUTO_LIGHT_MIN_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_THEME_AUTO_LIGHT_MIN_KEY,
					APP_THEME_AUTO_LIGHT_MIN_DEFAULT,
					APP_THEME_AUTO_LIGHT_MIN_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_THEME_AUTO_DARK_HOUR_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_THEME_AUTO_DARK_HOUR_KEY,
					APP_THEME_AUTO_DARK_HOUR_DEFAULT,
					APP_THEME_AUTO_DARK_HOUR_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_THEME_AUTO_DARK_MIN_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_THEME_AUTO_DARK_MIN_KEY,
					APP_THEME_AUTO_DARK_MIN_DEFAULT,
					APP_THEME_AUTO_DARK_MIN_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_FONT_KEY) == 0) {
			appSettingsApi.insertNewSetting(APP_FONT_KEY, APP_FONT_DEFAULT, APP_FONT_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_TABS_ANIMATION_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_TABS_ANIMATION_KEY, APP_TABS_ANIMATION_DEFAULT, APP_TABS_ANIMATION_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_LOCALE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_LOCALE_KEY, APP_LOCALE_KEY_DEFAULT, APP_LOCALE_KEY_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_COUNTER_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_COUNTER_KEY, APP_COUNTER_DEFAULT, APP_COUNTER_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_LABELS_IN_LIST_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_LABELS_IN_LIST_KEY, APP_LABELS_IN_LIST_DEFAULT, APP_LABELS_IN_LIST_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_LINK_HANDLER_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_LINK_HANDLER_KEY, APP_LINK_HANDLER_DEFAULT, APP_LINK_HANDLER_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_HOME_SCREEN_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_HOME_SCREEN_KEY, APP_HOME_SCREEN_DEFAULT, APP_HOME_SCREEN_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_CUSTOM_BROWSER_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_CUSTOM_BROWSER_KEY, APP_CUSTOM_BROWSER_DEFAULT, APP_CUSTOM_BROWSER_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_DRAFTS_DELETION_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_DRAFTS_DELETION_KEY,
					APP_DRAFTS_DELETION_DEFAULT,
					APP_DRAFTS_DELETION_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_CRASH_REPORTS_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_CRASH_REPORTS_KEY, APP_CRASH_REPORTS_DEFAULT, APP_CRASH_REPORTS_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_CE_SYNTAX_HIGHLIGHT_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_CE_SYNTAX_HIGHLIGHT_KEY,
					APP_CE_SYNTAX_HIGHLIGHT_DEFAULT,
					APP_CE_SYNTAX_HIGHLIGHT_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_CE_INDENTATION_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_CE_INDENTATION_KEY, APP_CE_INDENTATION_DEFAULT, APP_CE_INDENTATION_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_CE_TABS_WIDTH_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_CE_TABS_WIDTH_KEY, APP_CE_TABS_WIDTH_DEFAULT, APP_CE_TABS_WIDTH_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_NOTIFICATIONS_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_NOTIFICATIONS_KEY, APP_NOTIFICATIONS_DEFAULT, APP_NOTIFICATIONS_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_NOTIFICATIONS_DELAY_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_NOTIFICATIONS_DELAY_KEY,
					APP_NOTIFICATIONS_DELAY_DEFAULT,
					APP_NOTIFICATIONS_DELAY_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_BIOMETRIC_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_BIOMETRIC_KEY, APP_BIOMETRIC_DEFAULT, APP_BIOMETRIC_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_BIOMETRIC_LIFE_CYCLE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_BIOMETRIC_LIFE_CYCLE_KEY,
					APP_BIOMETRIC_LIFE_CYCLE_DEFAULT,
					APP_BIOMETRIC_LIFE_CYCLE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_DATA_CACHE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_DATA_CACHE_KEY, APP_DATA_CACHE_DEFAULT, APP_DATA_CACHE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_DATA_CACHE_SIZE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_DATA_CACHE_SIZE_KEY,
					APP_DATA_CACHE_SIZE_DEFAULT,
					APP_DATA_CACHE_SIZE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_IMAGES_CACHE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_IMAGES_CACHE_KEY, APP_IMAGES_CACHE_DEFAULT, APP_IMAGES_CACHE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_IMAGES_CACHE_SIZE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_IMAGES_CACHE_SIZE_KEY,
					APP_IMAGES_CACHE_SIZE_DEFAULT,
					APP_IMAGES_CACHE_SIZE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY,
					APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_DEFAULT,
					APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_DEFAULT);
		}
		if (appSettingsApi.fetchSettingCountByKey(APP_USER_HIDE_EMAIL_IN_NAV_KEY) == 0) {
			appSettingsApi.insertNewSetting(
					APP_USER_HIDE_EMAIL_IN_NAV_KEY,
					APP_USER_HIDE_EMAIL_IN_NAV_DEFAULT,
					APP_USER_HIDE_EMAIL_IN_NAV_DEFAULT);
		}

		if (appSettingsApi.fetchSettingCountByKey("prefsMigration") == 0) {
			appSettingsApi.insertNewSetting("prefsMigration", "true", "true");
		}
	}

	public static String getSettingsValue(Context ctx, String key) {

		AppSettingsApi appSettingsApi = BaseApi.getInstance(ctx, AppSettingsApi.class);
		assert appSettingsApi != null;
		AppSettings appSettings = appSettingsApi.fetchSettingByKey(key);
		return appSettings.getSettingValue();
	}

	public static void updateSettingsValue(Context ctx, String val, String key) {

		AppSettingsApi appSettingsApi = BaseApi.getInstance(ctx, AppSettingsApi.class);
		assert appSettingsApi != null;
		appSettingsApi.updateSettingValueByKey(val, key);
	}

	// remove this in the upcoming releases (5.5 or up)
	public static void prefsMigration(Context ctx) {

		TinyDB tinyDB = TinyDB.getInstance(ctx);

		if (tinyDB.checkForExistingPref("themeId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("themeId")),
					AppDatabaseSettings.APP_THEME_KEY);
			tinyDB.remove("themeId");
		}

		if (tinyDB.checkForExistingPref("lightThemeTimeHour")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("lightThemeTimeHour")),
					AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY);
			tinyDB.remove("lightThemeTimeHour");
		}
		if (tinyDB.checkForExistingPref("lightThemeTimeMinute")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("lightThemeTimeMinute")),
					AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY);
			tinyDB.remove("lightThemeTimeMinute");
		}

		if (tinyDB.checkForExistingPref("darkThemeTimeHour")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("darkThemeTimeHour")),
					AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY);
			tinyDB.remove("darkThemeTimeHour");
		}
		if (tinyDB.checkForExistingPref("darkThemeTimeMinute")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("darkThemeTimeMinute")),
					AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY);
			tinyDB.remove("darkThemeTimeMinute");
		}

		if (tinyDB.checkForExistingPref("customFontId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("customFontId")),
					AppDatabaseSettings.APP_FONT_KEY);
			tinyDB.remove("customFontId");
		}

		if (tinyDB.checkForExistingPref("fragmentTabsAnimationId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("fragmentTabsAnimationId")),
					AppDatabaseSettings.APP_TABS_ANIMATION_KEY);
			tinyDB.remove("fragmentTabsAnimationId");
		}

		if (tinyDB.checkForExistingPref("locale")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					tinyDB.getInt("langId") + "|" + tinyDB.getString("locale"),
					AppDatabaseSettings.APP_LOCALE_KEY);
			tinyDB.remove("locale");
			tinyDB.remove("langId");
		}

		if (tinyDB.checkForExistingPref("ceColorId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("ceColorId")),
					AppDatabaseSettings.APP_CE_SYNTAX_HIGHLIGHT_KEY);
			tinyDB.remove("ceColorId");
		}
		if (tinyDB.checkForExistingPref("ceIndentationId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("ceIndentationId")),
					AppDatabaseSettings.APP_CE_INDENTATION_KEY);
			tinyDB.remove("ceIndentationId");
		}
		if (tinyDB.checkForExistingPref("ceIndentationTabsId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("ceIndentationTabsId")),
					AppDatabaseSettings.APP_CE_TABS_WIDTH_KEY);
			tinyDB.remove("ceIndentationTabsId");
		}

		if (tinyDB.checkForExistingPref("homeScreenId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("homeScreenId")),
					AppDatabaseSettings.APP_HOME_SCREEN_KEY);
			tinyDB.remove("homeScreenId");
		}

		if (tinyDB.checkForExistingPref("defaultScreenId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("defaultScreenId")),
					AppDatabaseSettings.APP_LINK_HANDLER_KEY);
			tinyDB.remove("defaultScreenId");
		}

		if (tinyDB.checkForExistingPref("enableCounterBadges")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("enableCounterBadges")),
					AppDatabaseSettings.APP_COUNTER_KEY);
			tinyDB.remove("enableCounterBadges");
		}

		if (tinyDB.checkForExistingPref("showLabelsInList")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("showLabelsInList")),
					AppDatabaseSettings.APP_LABELS_IN_LIST_KEY);
			tinyDB.remove("showLabelsInList");
		}

		if (tinyDB.checkForExistingPref("notificationsEnabled")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("notificationsEnabled")),
					AppDatabaseSettings.APP_NOTIFICATIONS_KEY);
			tinyDB.remove("notificationsEnabled");
		}
		if (tinyDB.checkForExistingPref("notificationsPollingDelayId")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getInt("notificationsPollingDelayId")),
					AppDatabaseSettings.APP_NOTIFICATIONS_DELAY_KEY);
			tinyDB.remove("notificationsPollingDelayId");
		}

		if (tinyDB.checkForExistingPref("biometricStatus")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("biometricStatus")),
					AppDatabaseSettings.APP_BIOMETRIC_KEY);
			tinyDB.remove("biometricStatus");
		}
		if (tinyDB.checkForExistingPref("biometricLifeCycle")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("biometricLifeCycle")),
					AppDatabaseSettings.APP_BIOMETRIC_LIFE_CYCLE_KEY);
			tinyDB.remove("biometricLifeCycle");
		}

		if (tinyDB.checkForExistingPref("useCustomTabs")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("useCustomTabs")),
					AppDatabaseSettings.APP_CUSTOM_BROWSER_KEY);
			tinyDB.remove("useCustomTabs");
		}

		if (tinyDB.checkForExistingPref("crashReportingEnabled")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("crashReportingEnabled")),
					AppDatabaseSettings.APP_CRASH_REPORTS_KEY);
			tinyDB.remove("crashReportingEnabled");
		}

		if (tinyDB.checkForExistingPref("draftsCommentsDeletionEnabled")) {
			AppDatabaseSettings.updateSettingsValue(
					ctx,
					String.valueOf(tinyDB.getBoolean("draftsCommentsDeletionEnabled")),
					AppDatabaseSettings.APP_DRAFTS_DELETION_KEY);
			tinyDB.remove("draftsCommentsDeletionEnabled");
		}

		AppDatabaseSettings.updateSettingsValue(ctx, "false", "prefsMigration");
	}
}
