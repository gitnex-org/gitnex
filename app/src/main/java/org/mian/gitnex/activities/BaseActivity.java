package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.Locale;
import java.util.concurrent.Executor;
import org.mian.gitnex.R;
import org.mian.gitnex.core.MainApplication;
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

		switch (tinyDB.getInt("themeId", 6)) {
			case 0:
				setTheme(R.style.AppTheme);
				break;
			case 1:
				setTheme(R.style.AppThemeLight);
				break;
			case 2:
				if (TimeHelper.timeBetweenHours(
						tinyDB.getInt("darkThemeTimeHour", 18),
						tinyDB.getInt("lightThemeTimeHour", 6),
						tinyDB.getInt("darkThemeTimeMinute", 0),
						tinyDB.getInt("lightThemeTimeMinute", 0))) {

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
						tinyDB.getInt("darkThemeTimeHour", 18),
						tinyDB.getInt("lightThemeTimeHour", 6),
						tinyDB.getInt("darkThemeTimeMinute", 0),
						tinyDB.getInt("lightThemeTimeMinute", 0))) {

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

		String locale = tinyDB.getString("locale");
		if (locale.isEmpty()) {
			AppUtil.setAppLocale(getResources(), Locale.getDefault().getLanguage());
		} else {
			AppUtil.setAppLocale(getResources(), locale);
		}

		Notifications.startWorker(appCtx);
	}

	public void onResume() {
		super.onResume();

		if (tinyDB.getBoolean("biometricStatus", false)
				&& !tinyDB.getBoolean("biometricLifeCycle")) {

			Executor executor = ContextCompat.getMainExecutor(this);

			BiometricPrompt biometricPrompt =
					new BiometricPrompt(
							this,
							executor,
							new BiometricPrompt.AuthenticationCallback() {

								@Override
								public void onAuthenticationError(
										int errorCode, @NonNull CharSequence errString) {

									super.onAuthenticationError(errorCode, errString);

									// Authentication error, close the app
									finish();
								}

								// Authentication succeeded, continue to app
								@Override
								public void onAuthenticationSucceeded(
										@NonNull BiometricPrompt.AuthenticationResult result) {
									super.onAuthenticationSucceeded(result);
									tinyDB.putBoolean("biometricLifeCycle", true);
								}

								// Authentication failed, close the app
								@Override
								public void onAuthenticationFailed() {
									super.onAuthenticationFailed();
								}
							});

			BiometricPrompt.PromptInfo biometricPromptBuilder =
					new BiometricPrompt.PromptInfo.Builder()
							.setTitle(getString(R.string.biometricAuthTitle))
							.setSubtitle(getString(R.string.biometricAuthSubTitle))
							.setNegativeButtonText(getString(R.string.cancelButton))
							.build();

			biometricPrompt.authenticate(biometricPromptBuilder);
		}
	}

	public AccountContext getAccount() {
		return ((MainApplication) getApplication()).currentAccount;
	}
}
