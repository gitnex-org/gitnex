package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.settings.RepositoryGlobal;
import org.mian.gitnex.databinding.ActivityAdministrationBinding;
import org.mian.gitnex.databinding.BottomSheetGlobalRepositorySettingsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class AdministrationActivity extends BaseActivity {

	private ActivityAdministrationBinding binding;
	private BottomSheetDialog settingsBottomSheet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAdministrationBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setupListeners();
		handleIntentActions();
	}

	private void setupListeners() {
		binding.systemUsersFrame.setOnClickListener(
				v -> startActivity(new Intent(this, AdminGetUsersActivity.class)));

		binding.adminCronFrame.setOnClickListener(
				v -> startActivity(new Intent(this, AdminCronTasksActivity.class)));

		binding.unadoptedReposFrame.setOnClickListener(
				v -> startActivity(new Intent(this, AdminUnadoptedReposActivity.class)));

		binding.adminRepositoryFrame.setOnClickListener(v -> showRepositorySettings());
	}

	private void handleIntentActions() {
		String action = getIntent().getStringExtra("giteaAdminAction");
		if (action != null) {
			switch (action) {
				case "users" -> startActivity(new Intent(this, AdminGetUsersActivity.class));
				case "monitor" -> startActivity(new Intent(this, AdminCronTasksActivity.class));
			}
		}
	}

	private void showRepositorySettings() {
		settingsBottomSheet = new BottomSheetDialog(this);
		BottomSheetGlobalRepositorySettingsBinding sheetBinding =
				BottomSheetGlobalRepositorySettingsBinding.inflate(LayoutInflater.from(this));
		settingsBottomSheet.setContentView(sheetBinding.getRoot());

		loadRepositorySettings(sheetBinding);
		settingsBottomSheet.show();
	}

	private void loadRepositorySettings(BottomSheetGlobalRepositorySettingsBinding sheetBinding) {
		Call<RepositoryGlobal> call =
				ApiRetrofitClient.getInstance(this).getRepositoryGlobalSettings();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<RepositoryGlobal> call,
							@NonNull Response<RepositoryGlobal> response) {
						if (response.isSuccessful() && response.body() != null) {
							updateSettingsUI(sheetBinding, response.body());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<RepositoryGlobal> call, @NonNull Throwable t) {
						// Handle failure
					}
				});
	}

	private void updateSettingsUI(
			BottomSheetGlobalRepositorySettingsBinding sheetBinding, RepositoryGlobal settings) {
		sheetBinding.forksDisabledValue.setText(
				settings.isForksDisabled() ? "Disabled" : "Enabled");
		sheetBinding.migrationsDisabledValue.setText(
				settings.isMigrationsDisabled() ? "Disabled" : "Enabled");
		sheetBinding.httpGitDisabledValue.setText(
				settings.isHttpGitDisabled() ? "Disabled" : "Enabled");
		sheetBinding.lfsDisabledValue.setText(settings.isLfsDisabled() ? "Disabled" : "Enabled");
		sheetBinding.mirrorsDisabledValue.setText(
				settings.isMirrorsDisabled() ? "Disabled" : "Enabled");
		sheetBinding.timeTrackingDisabledValue.setText(
				settings.isTimeTrackingDisabled() ? "Disabled" : "Enabled");
		sheetBinding.starsDisabledValue.setText(
				settings.isStarsDisabled() ? "Disabled" : "Enabled");
	}
}
