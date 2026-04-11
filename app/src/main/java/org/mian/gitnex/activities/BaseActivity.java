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
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.AccountContext;
import org.mian.gitnex.notifications.Notifications;

/**
 * @author mmarif
 */
public abstract class BaseActivity extends AppCompatActivity {

	protected TinyDB tinyDB;
	protected Context ctx = this;
	protected Context appCtx;
	private int localUiVersion = AppUIStateManager.getUiVersion();

	@Override
	public void onCreate(Bundle savedInstanceState) {

		int themeChoice =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_THEME_KEY));
		applyAppTheme(themeChoice);

		applyFontOverlay();

		super.onCreate(savedInstanceState);

		this.appCtx = getApplicationContext();
		this.tinyDB = TinyDB.getInstance(appCtx);

		Notifications.startWorker(this);
	}

	private void applyAppTheme(int themeChoice) {
		switch (themeChoice) {
			case 0:
				setTheme(R.style.AppTheme);
				break;
			case 1:
				setTheme(R.style.AppThemeLight);
				break;
			case 2:
				if (isAutoDarkActive()) setTheme(R.style.AppTheme);
				else setTheme(R.style.AppThemeLight);
				break;
			case 3:
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if (isAutoDarkActive()) setTheme(R.style.AppTheme);
				else setTheme(R.style.AppThemeRetro);
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
	}

	private void applyFontOverlay() {
		int fontIndex =
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_FONT_KEY));
		switch (fontIndex) {
			case 0:
				setTheme(R.style.FontRoboto);
				break;
			case 2:
				setTheme(R.style.FontSourceCode);
				break;
			case 3:
				break; // system default
			default:
				setTheme(R.style.FontManrope);
				break;
		}
	}

	private boolean isAutoDarkActive() {
		return TimeHelper.timeBetweenHours(
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY)),
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY)),
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY)),
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY)));
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		String[] localeSetting =
				AppDatabaseSettings.getSettingsValue(newBase, AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");
		String langCode =
				localeSetting[0].equals("0") ? Locale.getDefault().getLanguage() : localeSetting[1];

		super.attachBaseContext(AppUtil.setAppLocale(newBase, langCode));
	}

	public AccountContext getAccount() {
		return ((MainApplication) getApplication()).currentAccount;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (isFinishing() || isDestroyed()) return;

		if (BiometricManager.from(this)
						.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
				== BiometricManager.BIOMETRIC_SUCCESS) {
			if (Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									this, AppDatabaseSettings.APP_BIOMETRIC_KEY))
					&& !Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									this, AppDatabaseSettings.APP_BIOMETRIC_LIFE_CYCLE_KEY))) {
				startActivity(new Intent(this, BiometricUnlock.class));
			}
		}

		if (localUiVersion < AppUIStateManager.getUiVersion()) {
			localUiVersion = AppUIStateManager.getUiVersion();
			recreate();
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
	}
}
