package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityUnlockBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;

/**
 * @author M M Arif
 */
public class BiometricUnlock extends AppCompatActivity {

	protected Context ctx = this;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityUnlockBinding activityUnlockBinding =
				ActivityUnlockBinding.inflate(getLayoutInflater());
		setContentView(activityUnlockBinding.getRoot());
	}

	public void onResume() {
		super.onResume();

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

								MainActivity.closeActivity = true;
								finish();
							}

							// Authentication succeeded, continue to app
							@Override
							public void onAuthenticationSucceeded(
									@NonNull BiometricPrompt.AuthenticationResult result) {
								super.onAuthenticationSucceeded(result);
								AppDatabaseSettings.updateSettingsValue(
										getApplicationContext(),
										"true",
										AppDatabaseSettings.APP_BIOMETRIC_LIFE_CYCLE_KEY);
								finish();
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
