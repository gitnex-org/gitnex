package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.core.MainApplication;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.AccountContext;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author M M Arif
 */
public abstract class BaseActivity extends AppCompatActivity {

	protected TinyDB tinyDB;

	protected Context ctx = this;
	protected Context appCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.appCtx = getApplicationContext();
		this.tinyDB = TinyDB.getInstance(appCtx);

		switch (Integer.parseInt(
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_THEME_KEY))) {
			case 0:
				setTheme(R.style.AppTheme);
				break;
			case 1:
				setTheme(R.style.AppThemeLight);
				break;
			case 2:
				if (TimeHelper.timeBetweenHours(
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY)))) {

					setTheme(R.style.AppTheme);
				} else {

					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if (TimeHelper.timeBetweenHours(
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY)))) {

					setTheme(R.style.AppTheme);
				} else {

					setTheme(R.style.AppThemeRetro);
				}
				break;
			case 5:
				setTheme(R.style.AppThemePitchBlack);
				break;
			case 7:
				setTheme(R.style.AppThemeSystemPitchBlack);
				break;
			case 8:
				setTheme(R.style.AppThemeDynamicSystem);
				break;
			case 9:
				setTheme(R.style.AppThemeCodebergDark);
				break;
			default:
				setTheme(R.style.AppThemeSystem);
				break;
		}

		String[] locale =
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");

		if (locale[0].equals("0")) {
			AppUtil.setAppLocale(getResources(), Locale.getDefault().getLanguage());
		} else {
			AppUtil.setAppLocale(getResources(), locale[1]);
		}

		Notifications.startWorker(ctx);
	}

	public void onResume() {
		super.onResume();

		if (BiometricManager.from(ctx)
						.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
				== BiometricManager.BIOMETRIC_SUCCESS) {

			if (Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_BIOMETRIC_KEY))
					&& !Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_BIOMETRIC_LIFE_CYCLE_KEY))) {

				Intent unlockIntent = new Intent(ctx, BiometricUnlock.class);
				ctx.startActivity(unlockIntent);
			}
		}
	}

	public AccountContext getAccount() {
		return ((MainApplication) getApplication()).currentAccount;
	}
}
