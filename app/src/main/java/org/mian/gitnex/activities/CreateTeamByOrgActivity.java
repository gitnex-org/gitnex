package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateTeamOption;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateTeamByOrgBinding;
import org.mian.gitnex.fragments.OrganizationTeamsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateTeamByOrgActivity extends BaseActivity {

	private final String[] permissionList = {"Read", "Write", "Admin"};
	public int permissionSelectedChoice = -1;
	private final String[] accessControlsList =
			new String[] {
				"Code",
				"Issues",
				"Pull Request",
				"Releases",
				"Wiki",
				"External Wiki",
				"External Issues"
			};
	private List<String> pushAccessList;
	private ActivityCreateTeamByOrgBinding activityCreateTeamByOrgBinding;

	private final boolean[] selectedAccessControlsTrueFalse =
			new boolean[] {false, false, false, false, false, false, false};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateTeamByOrgBinding =
				ActivityCreateTeamByOrgBinding.inflate(getLayoutInflater());
		setContentView(activityCreateTeamByOrgBinding.getRoot());

		MenuItem attachment = activityCreateTeamByOrgBinding.topAppBar.getMenu().getItem(0);
		MenuItem markdown = activityCreateTeamByOrgBinding.topAppBar.getMenu().getItem(1);
		attachment.setVisible(false);
		markdown.setVisible(false);

		activityCreateTeamByOrgBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		activityCreateTeamByOrgBinding.teamPermission.setOnClickListener(
				view -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilderPerm =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.newTeamPermission)
									.setCancelable(permissionSelectedChoice != -1)
									.setSingleChoiceItems(
											permissionList,
											permissionSelectedChoice,
											(dialogInterface, i) -> {
												permissionSelectedChoice = i;
												activityCreateTeamByOrgBinding.teamPermission
														.setText(permissionList[i]);

												switch (permissionList[i]) {
													case "Read":
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setVisibility(
																View.VISIBLE);
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setText(
																R.string.newTeamPermissionRead);
														break;
													case "Write":
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setVisibility(
																View.VISIBLE);
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setText(
																R.string.newTeamPermissionWrite);
														break;
													case "Admin":
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setVisibility(
																View.VISIBLE);
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setText(
																R.string.newTeamPermissionAdmin);
														break;
													default:
														activityCreateTeamByOrgBinding
																.teamPermissionDetail.setVisibility(
																View.GONE);
														break;
												}

												dialogInterface.dismiss();
											});

					materialAlertDialogBuilderPerm.create().show();
				});

		activityCreateTeamByOrgBinding.teamAccessControls.setOnClickListener(
				v -> {
					activityCreateTeamByOrgBinding.teamAccessControls.setText("");
					activityCreateTeamByOrgBinding.teamAccessControlsArray.setText("");
					pushAccessList = Arrays.asList(accessControlsList);

					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setMultiChoiceItems(
											accessControlsList,
											selectedAccessControlsTrueFalse,
											(dialog, which, isChecked) -> {})
									.setTitle(R.string.newTeamAccessControls)
									.setPositiveButton(
											R.string.okButton,
											(dialog, which) -> {
												int selectedVal = 0;
												while (selectedVal
														< selectedAccessControlsTrueFalse.length) {
													boolean value =
															selectedAccessControlsTrueFalse[
																	selectedVal];

													String repoCode = "";
													if (selectedVal == 0) {
														repoCode = "repo.code";
													}
													if (selectedVal == 1) {
														repoCode = "repo.issues";
													}
													if (selectedVal == 2) {
														repoCode = "repo.pulls";
													}
													if (selectedVal == 3) {
														repoCode = "repo.releases";
													}
													if (selectedVal == 4) {
														repoCode = "repo.wiki";
													}
													if (selectedVal == 5) {
														repoCode = "repo.ext_wiki";
													}
													if (selectedVal == 6) {
														repoCode = "repo.ext_issues";
													}

													if (value) {

														activityCreateTeamByOrgBinding
																.teamAccessControls.setText(
																getString(
																		R.string
																				.newTeamPermissionValues,
																		activityCreateTeamByOrgBinding
																				.teamAccessControls
																				.getText(),
																		pushAccessList.get(
																				selectedVal)));
														activityCreateTeamByOrgBinding
																.teamAccessControlsArray.setText(
																getString(
																		R.string
																				.newTeamPermissionValuesFinal,
																		activityCreateTeamByOrgBinding
																				.teamAccessControlsArray
																				.getText(),
																		repoCode));
													}

													selectedVal++;
												}

												String data =
														String.valueOf(
																activityCreateTeamByOrgBinding
																		.teamAccessControls
																		.getText());
												if (!data.equals("")) {

													activityCreateTeamByOrgBinding
															.teamAccessControls.setText(
															data.substring(0, data.length() - 2));
												}

												String dataArray =
														String.valueOf(
																activityCreateTeamByOrgBinding
																		.teamAccessControlsArray
																		.getText());

												if (!dataArray.equals("")) {

													activityCreateTeamByOrgBinding
															.teamAccessControlsArray.setText(
															dataArray.substring(
																	0, dataArray.length() - 2));
												}
											});

					materialAlertDialogBuilder.create().show();
				});

		activityCreateTeamByOrgBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processCreateTeam();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void processCreateTeam() {

		final String orgName = getIntent().getStringExtra("orgName");

		String newTeamName =
				Objects.requireNonNull(activityCreateTeamByOrgBinding.teamName.getText())
						.toString();
		String newTeamDesc =
				Objects.requireNonNull(activityCreateTeamByOrgBinding.teamDesc.getText())
						.toString();
		String newTeamPermission =
				Objects.requireNonNull(activityCreateTeamByOrgBinding.teamPermission.getText())
						.toString()
						.toLowerCase();
		String newTeamAccessControls =
				activityCreateTeamByOrgBinding.teamAccessControlsArray.getText().toString();

		if (newTeamName.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.teamNameEmpty));
			return;
		}

		if (!AppUtil.checkStringsWithAlphaNumericDashDotUnderscore(newTeamName)) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.teamNameError));
			return;
		}

		if (!newTeamDesc.equals("")) {

			if (!AppUtil.checkStrings(newTeamDesc)) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.teamDescError));
				return;
			}

			if (newTeamDesc.length() > 100) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.teamDescLimit));
				return;
			}
		}

		if (newTeamPermission.equals("")) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.teamPermissionEmpty));
			return;
		}

		List<String> newTeamAccessControls_ =
				new ArrayList<>(Arrays.asList(newTeamAccessControls.split(",")));

		for (int i = 0; i < newTeamAccessControls_.size(); i++) {

			newTeamAccessControls_.set(i, newTeamAccessControls_.get(i).trim());
		}

		createNewTeamCall(
				orgName, newTeamName, newTeamDesc, newTeamPermission, newTeamAccessControls_);
	}

	private void createNewTeamCall(
			String orgName,
			String newTeamName,
			String newTeamDesc,
			String newTeamPermission,
			List<String> newTeamAccessControls) {

		CreateTeamOption createNewTeamJson = new CreateTeamOption();
		createNewTeamJson.setName(newTeamName);
		createNewTeamJson.setDescription(newTeamDesc);
		switch (newTeamPermission) {
			case "Read":
				createNewTeamJson.setPermission(CreateTeamOption.PermissionEnum.READ);
				break;
			case "Write":
				createNewTeamJson.setPermission(CreateTeamOption.PermissionEnum.WRITE);
				break;
			case "Admin":
				createNewTeamJson.setPermission(CreateTeamOption.PermissionEnum.ADMIN);
				break;
		}
		createNewTeamJson.setUnits(newTeamAccessControls);

		Call<Team> call3 =
				RetrofitClient.getApiInterface(ctx).orgCreateTeam(orgName, createNewTeamJson);

		call3.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Team> call, @NonNull retrofit2.Response<Team> response2) {

						if (response2.isSuccessful()) {

							if (response2.code() == 201) {

								OrganizationTeamsFragment.resumeTeams = true;

								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.teamCreated));
								new Handler().postDelayed(() -> finish(), 3000);
							}
						} else if (response2.code() == 404) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.apiNotFound));
						} else if (response2.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Team> call, @NonNull Throwable t) {}
				});
	}
}
