package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.gitnex.tea4j.v2.models.CreateRepoOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.license.License;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateRepoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class CreateRepoActivity extends BaseActivity {

	final List<String> reservedRepoNames = Arrays.asList(".", "..");
	final Pattern reservedRepoPatterns = Pattern.compile("\\.(git|wiki)$");
	List<String> organizationsList = new ArrayList<>();
	List<String> issueLabelsList = new ArrayList<>();
	List<String> licenseDisplayList = new ArrayList<>();
	List<String> licenseKeyList = new ArrayList<>();
	List<String> gitignoreList = new ArrayList<>();
	private ActivityCreateRepoBinding activityCreateRepoBinding;
	private String loginUid;
	private String selectedOwner;
	private String selectedIssueLabels;
	private String selectedLicense;
	private String selectedGitignore;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateRepoBinding = ActivityCreateRepoBinding.inflate(getLayoutInflater());
		setContentView(activityCreateRepoBinding.getRoot());

		loginUid = getAccount().getAccount().getUserName();
		getOrganizations(loginUid);

		activityCreateRepoBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		MenuItem attachment = activityCreateRepoBinding.topAppBar.getMenu().getItem(0);
		attachment.setVisible(false);
		MenuItem markdown = activityCreateRepoBinding.topAppBar.getMenu().getItem(1);
		markdown.setVisible(false);

		getLicenses();

		issueLabelsList.add(getString(R.string.advanced));
		issueLabelsList.add(getString(R.string.defaultText));
		getIssueLabels();

		getGitignoreTemplates();

		activityCreateRepoBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processNewRepo();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void processNewRepo() {

		String newRepoName =
				Objects.requireNonNull(activityCreateRepoBinding.newRepoName.getText()).toString();
		String newRepoDesc =
				Objects.requireNonNull(activityCreateRepoBinding.newRepoDescription.getText())
						.toString();
		boolean newRepoAccess = activityCreateRepoBinding.newRepoPrivate.isChecked();
		boolean repoAsTemplate = activityCreateRepoBinding.setAsTemplate.isChecked();
		String defaultBranch =
				Objects.requireNonNull(activityCreateRepoBinding.defaultBranch.getText())
						.toString();

		if (!newRepoDesc.isEmpty()) {

			if (newRepoDesc.length() > 255) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.repoDescError));
				return;
			}
		}

		if (newRepoName.isEmpty()) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.repoNameErrorEmpty));
		} else if (!AppUtil.checkStrings(newRepoName)) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.repoNameErrorInvalid));
		} else if (reservedRepoNames.contains(newRepoName)) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.repoNameErrorReservedName));
		} else if (reservedRepoPatterns.matcher(newRepoName).find()) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.repoNameErrorReservedPatterns));
		} else if (defaultBranch.equalsIgnoreCase("")) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.repoDefaultBranchError));
		} else if (selectedOwner == null) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.repoOwnerError));
		} else {

			createNewRepository(
					loginUid,
					newRepoName,
					newRepoDesc,
					selectedOwner,
					newRepoAccess,
					defaultBranch,
					repoAsTemplate);
		}
	}

	private void createNewRepository(
			String loginUid,
			String repoName,
			String repoDesc,
			String selectedOwner,
			boolean isPrivate,
			String defaultBranch,
			boolean repoAsTemplate) {

		CreateRepoOption createRepository = new CreateRepoOption();
		createRepository.setAutoInit(true);
		createRepository.setDescription(repoDesc);
		createRepository.setPrivate(isPrivate);
		createRepository.setReadme("Default");
		createRepository.setName(repoName);
		createRepository.setDefaultBranch(defaultBranch);
		createRepository.setIssueLabels(selectedIssueLabels);
		createRepository.setTemplate(repoAsTemplate);
		createRepository.setLicense(selectedLicense);

		if (selectedGitignore != null && !selectedGitignore.isEmpty()) {
			createRepository.setGitignores(selectedGitignore);
		}

		Call<Repository> call;
		if (selectedOwner.equals(loginUid)) {

			call = RetrofitClient.getApiInterface(ctx).createCurrentUserRepo(createRepository);
		} else {

			call =
					RetrofitClient.getApiInterface(ctx)
							.createOrgRepo(selectedOwner, createRepository);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull retrofit2.Response<Repository> response) {

						if (response.code() == 201) {

							MainActivity.reloadRepos = true;
							OrganizationDetailActivity.updateOrgFABActions = true;

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.repoCreated));

							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 409) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.repoExistsError));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {}
				});
	}

	private void getIssueLabels() {

		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(
						CreateRepoActivity.this, R.layout.list_spinner_items, issueLabelsList);

		activityCreateRepoBinding.issueLabels.setAdapter(adapter);

		activityCreateRepoBinding.issueLabels.setOnItemClickListener(
				(parent, view, position, id) ->
						selectedIssueLabels = issueLabelsList.get(position));
	}

	private void getLicenses() {
		Call<List<License>> call = ApiRetrofitClient.getInstance(ctx).getLicenses();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<License>> call,
							@NonNull retrofit2.Response<List<License>> response) {

						if (response.isSuccessful() && response.body() != null) {
							List<License> licenses = response.body();
							if (!licenses.isEmpty()) {
								licenseDisplayList.clear();
								licenseKeyList.clear();

								licenseDisplayList.add(getString(R.string.no_license));
								licenseKeyList.add("");

								for (License license : licenses) {
									licenseDisplayList.add(license.getName());
									licenseKeyList.add(license.getKey());
								}

								setupLicenseDropdown();
							} else {
								hideLicenseDropdown();
							}
						} else {
							hideLicenseDropdown();
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<License>> call, @NonNull Throwable t) {
						hideLicenseDropdown();
					}
				});
	}

	private void setupLicenseDropdown() {
		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(
						CreateRepoActivity.this, R.layout.list_spinner_items, licenseDisplayList);

		activityCreateRepoBinding.licenses.setAdapter(adapter);

		activityCreateRepoBinding.licenses.setOnItemClickListener(
				(parent, view, position, id) -> {
					if (position == 0) {
						selectedLicense = null;
					} else {
						selectedLicense = licenseKeyList.get(position);
					}
				});

		activityCreateRepoBinding.licenses.setText(licenseDisplayList.get(0), false);
		selectedLicense = null;
	}

	private void hideLicenseDropdown() {
		activityCreateRepoBinding.licenseFrame.setVisibility(View.GONE);
		selectedLicense = null;
	}

	private void getGitignoreTemplates() {
		Call<List<String>> call = ApiRetrofitClient.getInstance(ctx).getGitignoreTemplates();

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<String>> call,
							@NonNull retrofit2.Response<List<String>> response) {

						if (response.isSuccessful() && response.body() != null) {
							List<String> templates = response.body();
							if (!templates.isEmpty()) {
								Collections.sort(templates);
								gitignoreList.addAll(templates);

								gitignoreList.add(0, getString(R.string.no_template));

								setupGitignoreDropdown();
							} else {
								hideGitignoreDropdown();
							}
						} else {
							hideGitignoreDropdown();
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {
						hideGitignoreDropdown();
					}
				});
	}

	private void setupGitignoreDropdown() {
		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(
						CreateRepoActivity.this, R.layout.list_spinner_items, gitignoreList);

		activityCreateRepoBinding.gitignoreTemplates.setAdapter(adapter);

		activityCreateRepoBinding.gitignoreTemplates.setOnItemClickListener(
				(parent, view, position, id) -> {
					if (position == 0) {
						selectedGitignore = null;
					} else {
						selectedGitignore = gitignoreList.get(position);
					}
				});

		activityCreateRepoBinding.gitignoreTemplates.setText(gitignoreList.get(0), false);
		selectedGitignore = null;
	}

	private void hideGitignoreDropdown() {
		activityCreateRepoBinding.gitignoreFrame.setVisibility(View.GONE);
		selectedGitignore = null;
	}

	private void getOrganizations(final String userLogin) {

		Call<List<Organization>> call =
				RetrofitClient.getApiInterface(ctx).orgListCurrentUserOrgs(1, 100);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Organization>> call,
							@NonNull retrofit2.Response<List<Organization>> response) {

						if (response.code() == 200) {

							int organizationId = 0;

							List<Organization> organizationsList_ = response.body();

							organizationsList.add(userLogin);
							assert organizationsList_ != null;

							if (!organizationsList_.isEmpty()) {

								for (int i = 0; i < organizationsList_.size(); i++) {

									if (getIntent().getStringExtra("orgName") != null
											&& !"".equals(getIntent().getStringExtra("orgName"))) {
										if (Objects.equals(
												getIntent().getStringExtra("orgName"),
												organizationsList_.get(i).getUsername())) {
											organizationId = i + 1;
										}
									}

									organizationsList.add(organizationsList_.get(i).getUsername());
								}
							}

							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											CreateRepoActivity.this,
											R.layout.list_spinner_items,
											organizationsList);

							activityCreateRepoBinding.ownerSpinner.setAdapter(adapter);

							activityCreateRepoBinding.ownerSpinner.setOnItemClickListener(
									(parent, view, position, id) ->
											selectedOwner = organizationsList.get(position));

							if (getIntent().getBooleanExtra("organizationAction", false)
									&& organizationId != 0) {

								int selectOwnerById = organizationId;
								new Handler(Looper.getMainLooper())
										.postDelayed(
												() -> {
													activityCreateRepoBinding.ownerSpinner.setText(
															organizationsList.get(selectOwnerById),
															false);
													selectedOwner =
															organizationsList.get(selectOwnerById);
												},
												500);
								getIntent().removeExtra("organizationAction");
							}

						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Organization>> call, @NonNull Throwable t) {}
				});
	}
}
