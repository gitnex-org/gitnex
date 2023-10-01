package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
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
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateRepoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateRepoActivity extends BaseActivity {

	// https://github.com/go-gitea/gitea/blob/52cfd2743c0e85b36081cf80a850e6a5901f1865/models/repo.go#L964-L967
	final List<String> reservedRepoNames = Arrays.asList(".", "..");
	final Pattern reservedRepoPatterns = Pattern.compile("\\.(git|wiki)$");
	List<String> organizationsList = new ArrayList<>();
	List<String> issueLabelsList = new ArrayList<>();
	List<String> licenseList = new ArrayList<>();
	private ActivityCreateRepoBinding activityCreateRepoBinding;
	private String loginUid;
	private String selectedOwner;
	private String selectedIssueLabels;
	private String selectedLicense;

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

		String[] licenses = getResources().getStringArray(R.array.licenses);
		Collections.addAll(licenseList, licenses);
		getLicenses();

		issueLabelsList.add(getString(R.string.advanced));
		issueLabelsList.add(getString(R.string.defaultText));
		getIssueLabels();

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

		if (!newRepoDesc.equals("")) {

			if (newRepoDesc.length() > 255) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.repoDescError));
				return;
			}
		}

		if (newRepoName.equals("")) {

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

		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(
						CreateRepoActivity.this, R.layout.list_spinner_items, licenseList);

		activityCreateRepoBinding.licenses.setAdapter(adapter);

		activityCreateRepoBinding.licenses.setOnItemClickListener(
				(parent, view, position, id) -> selectedLicense = licenseList.get(position));
	}

	private void getOrganizations(final String userLogin) {

		Call<List<Organization>> call =
				RetrofitClient.getApiInterface(ctx).orgListCurrentUserOrgs(1, 50);

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

							if (organizationsList_.size() > 0) {

								for (int i = 0; i < organizationsList_.size(); i++) {

									if (getIntent().getStringExtra("orgName") != null
											&& !"".equals(getIntent().getStringExtra("orgName"))) {
										if (getIntent()
												.getStringExtra("orgName")
												.equals(organizationsList_.get(i).getUsername())) {
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
