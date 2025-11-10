package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.mian.gitnex.activities.AdminCronTasksActivity;
import org.mian.gitnex.activities.AdminGetUsersActivity;
import org.mian.gitnex.activities.AdminUnadoptedReposActivity;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.settings.RepositoryGlobal;
import org.mian.gitnex.databinding.BottomSheetGlobalRepositorySettingsBinding;
import org.mian.gitnex.databinding.FragmentAdministrationBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class AdministrationFragment extends Fragment {

	private FragmentAdministrationBinding binding;
	private BottomSheetDialog settingsBottomSheet;

	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		binding = FragmentAdministrationBinding.inflate(inflater, container, false);

		binding.systemUsersFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminGetUsersActivity.class)));

		binding.adminCronFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminCronTasksActivity.class)));

		binding.unadoptedReposFrame.setOnClickListener(
				v1 -> startActivity(new Intent(getContext(), AdminUnadoptedReposActivity.class)));

		binding.adminRepositoryFrame.setOnClickListener(v -> showRepositorySettings());

		String action = requireActivity().getIntent().getStringExtra("giteaAdminAction");
		if (action != null) {
			if (action.equals("users")) {
				startActivity(new Intent(getContext(), AdminGetUsersActivity.class));
			} else if (action.equals("monitor")) {
				startActivity(new Intent(getContext(), AdminCronTasksActivity.class));
			}
		}

		return binding.getRoot();
	}

	private void showRepositorySettings() {
		settingsBottomSheet = new BottomSheetDialog(requireContext());
		BottomSheetGlobalRepositorySettingsBinding sheetBinding =
				BottomSheetGlobalRepositorySettingsBinding.inflate(
						LayoutInflater.from(requireContext()));
		settingsBottomSheet.setContentView(sheetBinding.getRoot());

		loadRepositorySettings(sheetBinding);

		settingsBottomSheet.show();
	}

	private void loadRepositorySettings(BottomSheetGlobalRepositorySettingsBinding sheetBinding) {
		Call<RepositoryGlobal> call =
				ApiRetrofitClient.getInstance(requireContext()).getRepositoryGlobalSettings();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<RepositoryGlobal> call,
							@NonNull Response<RepositoryGlobal> response) {
						if (response.isSuccessful() && response.body() != null) {
							RepositoryGlobal settings = response.body();
							updateSettingsUI(sheetBinding, settings);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<RepositoryGlobal> call, @NonNull Throwable t) {}
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
