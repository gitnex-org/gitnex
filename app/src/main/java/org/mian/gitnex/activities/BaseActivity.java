package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.notifications.Notifications;
import java.util.Locale;
import java.util.concurrent.Executor;

/**
 * Author M M Arif
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

		switch(tinyDB.getInt("themeId")) {

			case 1:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeLight);
				break;
			case 2:

				if(TimeHelper.timeBetweenHours(tinyDB.getInt("darkThemeTimeHour"), tinyDB.getInt("lightThemeTimeHour"), tinyDB.getInt("darkThemeTimeMinute"), tinyDB.getInt("lightThemeTimeMinute"))) {

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if(TimeHelper.timeBetweenHours(tinyDB.getInt("darkThemeTimeHour"), tinyDB.getInt("lightThemeTimeHour"), tinyDB.getInt("darkThemeTimeMinute"), tinyDB.getInt("lightThemeTimeMinute"))) {

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeRetro);
				}
				break;
			case 5:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppThemePitchBlack);
				break;
			default:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppTheme);

		}

		String locale = tinyDB.getString("locale");
		if (locale.isEmpty()) {
			AppUtil.setAppLocale(getResources(), Locale.getDefault().getLanguage());
		}
		else {
			AppUtil.setAppLocale(getResources(), locale);
		}

		Notifications.startWorker(appCtx);
	}

	public void onResume() {
		super.onResume();

		if(tinyDB.getBoolean("biometricStatus") && !tinyDB.getBoolean("biometricLifeCycle")) {

			Executor executor = ContextCompat.getMainExecutor(this);

			BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {

				@Override
				public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {

					super.onAuthenticationError(errorCode, errString);

					// Authentication error, close the app
					if(errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
						errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
						finish();
					}
				}

				// Authentication succeeded, continue to app
				@Override public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) { super.onAuthenticationSucceeded(result); tinyDB.putBoolean("biometricLifeCycle", true); }

				// Authentication failed, close the app
				@Override public void onAuthenticationFailed() { super.onAuthenticationFailed(); }

			});

			BiometricPrompt.PromptInfo biometricPromptBuilder = new BiometricPrompt.PromptInfo.Builder()
				.setTitle(getString(R.string.biometricAuthTitle))
				.setSubtitle(getString(R.string.biometricAuthSubTitle))
				.setNegativeButtonText(getString(R.string.cancelButton)).build();

			biometricPrompt.authenticate(biometricPromptBuilder);

		}
	}
}


