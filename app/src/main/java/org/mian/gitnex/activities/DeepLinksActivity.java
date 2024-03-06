package org.mian.gitnex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityDeeplinksBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private boolean accountFound = false;
	private Intent mainIntent;
	private Intent issueIntent;
	private Intent repoIntent;
	private Intent orgIntent;
	private Intent userIntent;
	private Uri data;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		mainIntent = new Intent(ctx, MainActivity.class);
		issueIntent = new Intent(ctx, IssueDetailActivity.class);
		repoIntent = new Intent(ctx, RepoDetailActivity.class);
		orgIntent = new Intent(ctx, OrganizationDetailActivity.class);
		userIntent = new Intent(ctx, ProfileActivity.class);

		Intent intent = getIntent();
		data = intent.getData();
		assert data != null;

		// check for login
		if (tinyDB.getInt("currentActiveAccountId", -1) <= -1) {
			Intent loginIntent = new Intent(ctx, LoginActivity.class);
			loginIntent.putExtra("instanceUrl", data.getHost());
			ctx.startActivity(loginIntent);
			finish();
			return;
		}

		// check for the links(URI) to be in the db
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		assert userAccountsApi != null;
		List<UserAccount> userAccounts = userAccountsApi.loggedInUserAccounts();

		for (UserAccount userAccount : userAccounts) {

			String hostUri = userAccount.getInstanceUrl();

			String hostExternal = data.getHost();
			int portExternal = data.getPort();

			String hostUrlExternal;
			if (portExternal > 0) {
				hostUrlExternal = hostExternal + ":" + portExternal;
			} else {
				hostUrlExternal = hostExternal;
			}

			if (hostUrlExternal == null) {
				hostUrlExternal = "";
			}

			if (hostUri.toLowerCase().contains(hostUrlExternal.toLowerCase())) {

				accountFound = true;

				AppUtil.switchToAccount(ctx, userAccount, false);
				break;
			}
		}

		if (accountFound) {

			// redirect to proper fragment/activity, if no action is there, show options where user
			// to want to go like repos, profile, notifications etc
			if (data.getPathSegments().size() == 1) {
				if (Objects.equals(data.getLastPathSegment(), "notifications")) { // notifications
					mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
					ctx.startActivity(mainIntent);
					finish();
				} else if (Objects.equals(data.getLastPathSegment(), "explore")) { // explore
					mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
					ctx.startActivity(mainIntent);
					finish();
				} else if (Objects.equals(
						data.getLastPathSegment(),
						getAccount().getAccount().getUserName())) { // your user profile
					mainIntent.putExtra("launchFragmentByLinkHandler", "profile");
					ctx.startActivity(mainIntent);
					finish();
				} else if (Objects.equals(data.getLastPathSegment(), "admin")) {
					mainIntent.putExtra("launchFragmentByLinkHandler", "admin");
					ctx.startActivity(mainIntent);
					finish();
				} else {
					new Handler(Looper.getMainLooper())
							.postDelayed(() -> getUserOrOrg(data.getLastPathSegment()), 500);
				}
			} else if (data.getPathSegments().size() == 2) {
				if (data.getPathSegments().get(0).equals("explore")) { // specific explore tab
					if (data.getPathSegments().get(1).equals("organizations")) { // orgs
						mainIntent.putExtra("exploreOrgs", true);
					}
					mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
					ctx.startActivity(mainIntent);
					finish();
				} else if (data.getPathSegments().get(0).equals("user")
						&& data.getPathSegments().get(1).equals("login")) { // open login
					Intent loginIntent = new Intent(ctx, AddNewAccountActivity.class);
					loginIntent.putExtra("instanceUrl", data.getHost());
					loginIntent.putExtra("instanceProtocol", data.getScheme());
					ctx.startActivity(loginIntent);
					finish();
				} else if (data.getPathSegments().get(0).equals("admin")) {
					mainIntent.putExtra("launchFragmentByLinkHandler", "admin");
					mainIntent.putExtra("giteaAdminAction", data.getLastPathSegment());
					ctx.startActivity(mainIntent);
					finish();
				} else if (!data.getPathSegments().get(0).isEmpty()
						& !Objects.equals(data.getLastPathSegment(), "")) { // go to repo
					String repo = data.getLastPathSegment();
					assert repo != null;
					if (repo.endsWith(".git")) { // Git clone URL
						repo = repo.substring(0, repo.length() - 4);
					}
					String finalRepo = repo;
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													finalRepo,
													"repo"),
									500);
				} else { // no action, show options
					showNoActionButtons();
				}
			} else if (data.getPathSegments().size() >= 3) {
				if (data.getPathSegments().get(2).equals("issues")) { // issue

					if (!Objects.requireNonNull(data.getLastPathSegment()).contains("issues")
							& StringUtils.isNumeric(data.getLastPathSegment())) {

						issueIntent.putExtra("issueNumber", data.getLastPathSegment());
						issueIntent.putExtra("openedFromLink", "true");

						String[] urlSplitted = data.toString().split("#");
						if (urlSplitted.length == 2) {
							issueIntent.putExtra("issueComment", urlSplitted[1]);
						}

						IssueContext issue =
								new IssueContext(
										new RepositoryContext(
												data.getPathSegments().get(0),
												data.getPathSegments().get(1),
												ctx),
										Integer.parseInt(data.getLastPathSegment()),
										"Issue");

						issue.getRepository().saveToDB(ctx);

						issueIntent.putExtra(IssueContext.INTENT_EXTRA, issue);

						ctx.startActivity(issueIntent);
						finish();
					} else if (Objects.requireNonNull(data.getLastPathSegment())
							.contains("issues")) {
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() ->
												goToRepoSection(
														data.getPathSegments().get(0),
														data.getPathSegments().get(1),
														"issue"),
										500);
					} else if (data.getLastPathSegment().equals("new")) {
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() ->
												goToRepoSection(
														data.getPathSegments().get(0),
														data.getPathSegments().get(1),
														"issueNew"),
										500);
					} else {
						ctx.startActivity(mainIntent);
						finish();
					}
				} else if (data.getPathSegments().get(2).equals("pulls")) { // pr

					if (!Objects.requireNonNull(data.getLastPathSegment()).contains("pulls")
							& StringUtils.isNumeric(data.getLastPathSegment())) {

						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											String[] urlSplitted = data.toString().split("#");
											if (urlSplitted.length == 2) {
												issueIntent.putExtra(
														"issueComment", urlSplitted[1]);
											}

											getPullRequest(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													Integer.parseInt(data.getLastPathSegment()));
										},
										500);

					} else if (Objects.requireNonNull(data.getLastPathSegment())
							.contains("pulls")) {
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() ->
												goToRepoSection(
														data.getPathSegments().get(0),
														data.getPathSegments().get(1),
														"pull"),
										500);
					} else if (data.getLastPathSegment().equals("files")) { // pr diff
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											issueIntent.putExtra("openPrDiff", "true");
											getPullRequest(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													Integer.parseInt(
															data.getPathSegments().get(3)));
										},
										500);
					} else {
						ctx.startActivity(mainIntent);
						finish();
					}
				} else if (data.getPathSegments().get(2).equals("compare")) { // new pull request
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"pullNew"),
									500);
				} else if (data.getPathSegments().get(2).equals("commit")) {
					repoIntent.putExtra("sha", data.getLastPathSegment());
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"commit"),
									500);
				} else if (data.getPathSegments().get(2).equals("commits")) { // commits list
					String branch = data.getLastPathSegment();
					repoIntent.putExtra("branchName", branch);
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"commitsList"),
									500);
				} else if (data.getPathSegments().get(2).equals("milestones")
						&& Objects.equals(data.getLastPathSegment(), "new")) { // new milestone
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"milestonesNew"),
									500);
				} else if (data.getPathSegments().get(2).equals("milestones")) { // milestones
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"milestones"),
									500);
				} else if (data.getPathSegments().get(2).equals("milestone")) { // milestone
					repoIntent.putExtra("milestoneId", data.getLastPathSegment());
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"milestones"),
									500);
				} else if (data.getPathSegments().get(2).equals("releases")
						&& Objects.equals(data.getLastPathSegment(), "new")) { // new release
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"newRelease"),
									500);
				} else if (data.getPathSegments().get(2).equals("releases")) { // releases
					if (data.getPathSegments().size() == 5) {
						if (data.getPathSegments().get(2).equals("releases")
								&& data.getPathSegments().get(3).equals("tag")) {
							repoIntent.putExtra("releaseTagName", data.getLastPathSegment());
						}
					}
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"releases"),
									500);
				} else if (data.getPathSegments().get(2).equals("labels")) { // labels
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"labels"),
									500);
				} else if (data.getPathSegments().get(2).equals("settings")) { // repo settings
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"settings"),
									500);
				} else if (Objects.equals(data.getLastPathSegment(), "branches")) { // branches list
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"branchesList"),
									500);
				} else if (data.getPathSegments().size() == 5
						&& data.getPathSegments().get(2).equals("src")
						&& data.getPathSegments().get(3).equals("branch")) { // branch
					repoIntent.putExtra("selectedBranch", data.getLastPathSegment());
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											goToRepoSection(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													"branch"),
									500);
				} else if (data.getPathSegments().get(2).equals("src")
						&& data.getPathSegments().get(3).equals("branch")) { // file/dir
					StringBuilder filePath = new StringBuilder();
					ArrayList<String> segments = new ArrayList<>(data.getPathSegments());
					segments.subList(0, 5).clear();
					for (String item : segments) {
						filePath.append(item);
						filePath.append("/");
					}
					filePath.deleteCharAt(filePath.toString().length() - 1);
					new Handler(Looper.getMainLooper())
							.postDelayed(
									() ->
											getFile(
													data.getPathSegments().get(0),
													data.getPathSegments().get(1),
													filePath.toString(),
													data.getPathSegments().get(4)),
									500);
				} else if (data.getPathSegments().get(2).equals("wiki")) { // wiki

					if (data.getQueryParameter("action") != null
							&& Objects.requireNonNull(data.getQueryParameter("action"))
									.equalsIgnoreCase("_new")) {
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() ->
												goToRepoSection(
														data.getPathSegments().get(0),
														data.getPathSegments().get(1),
														"wikiNew"),
										500);
					} else {
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() ->
												goToRepoSection(
														data.getPathSegments().get(0),
														data.getPathSegments().get(1),
														"wiki"),
										500);
					}
				} else { // no action, show options
					showNoActionButtons();
				}
			} else {

				startActivity(mainIntent);
				finish();
			}
		} else {

			viewBinding.progressBar.setVisibility(View.GONE);
			viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
			viewBinding.noActionFrame.setVisibility(View.GONE);
			viewBinding.addAccountText.setText(
					String.format(
							getResources().getString(R.string.accountDoesNotExist),
							data.getHost()));

			viewBinding.addNewAccount.setOnClickListener(
					addNewAccount -> {
						Intent accountIntent = new Intent(ctx, AddNewAccountActivity.class);
						accountIntent.putExtra("instanceUrl", data.getHost());
						startActivity(accountIntent);
						finish();
					});

			viewBinding.openInBrowser.setOnClickListener(
					addNewAccount -> {
						AppUtil.openUrlInBrowser(this, String.valueOf(data));
						finish();
					});

			viewBinding.launchApp.setOnClickListener(
					launchApp -> {
						startActivity(mainIntent);
						finish();
					});
		}
	}

	private void getPullRequest(String repoOwner, String repoName, int index) {

		Call<PullRequest> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGetPullRequest(repoOwner, repoName, (long) index);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull retrofit2.Response<PullRequest> response) {

						PullRequest prInfo = response.body();

						if (response.code() == 200) {

							assert prInfo != null;

							issueIntent.putExtra("openedFromLink", "true");

							IssueContext issue =
									new IssueContext(
											prInfo,
											new RepositoryContext(repoOwner, repoName, ctx));

							issue.getRepository().saveToDB(ctx);

							issueIntent.putExtra(IssueContext.INTENT_EXTRA, issue);
							ctx.startActivity(issueIntent);
							finish();
						} else {

							ctx.startActivity(mainIntent);
							finish();
							Log.e("onFailure-links-pr", String.valueOf(response.code()));
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {

						ctx.startActivity(issueIntent);
						finish();
						Log.e("onFailure-links-pr", t.toString());
					}
				});
	}

	private void goToRepoSection(String repoOwner, String repoName, String type) {

		Call<Repository> call = RetrofitClient.getApiInterface(ctx).repoGet(repoOwner, repoName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull retrofit2.Response<Repository> response) {
						Repository repoInfo = response.body();

						if (response.code() == 200) {
							assert repoInfo != null;

							RepositoryContext repo = new RepositoryContext(repoInfo, ctx);

							repoIntent.putExtra("goToSection", "yes");
							repoIntent.putExtra("goToSectionType", type);

							repo.saveToDB(ctx);
							repoIntent.putExtra(RepositoryContext.INTENT_EXTRA, repo);

							ctx.startActivity(repoIntent);
							finish();
						} else {
							ctx.startActivity(mainIntent);
							finish();
							Log.e("error-goToRepo", response.message());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

						ctx.startActivity(mainIntent);
						finish();
						Log.e("onFailure-goToRepo", t.toString());
					}
				});
	}

	private void getUserOrOrg(String userOrgName) {
		Call<Organization> call = RetrofitClient.getApiInterface(ctx).orgGet(userOrgName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Organization> call,
							@NonNull Response<Organization> response) {
						if (response.code() == 404) { // org doesn't exist or it's a user user
							Log.d("getUserOrOrg-404", String.valueOf(response.code()));
							getUser(userOrgName);
						} else if (response.code() == 200) { // org
							assert response.body() != null;
							orgIntent.putExtra("orgName", response.body().getUsername());
							orgIntent.putExtra("organizationId", response.body().getId());
							orgIntent.putExtra("organizationAction", true);
							ctx.startActivity(orgIntent);
							finish();
						} else {
							Log.e("getUserOrOrg-code", String.valueOf(response.code()));
							ctx.startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {
						Log.e("onFailure-getUserOrOrg", t.toString());
					}
				});
	}

	private void getUser(String userName) {
		Call<User> call = RetrofitClient.getApiInterface(ctx).userGet(userName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull Response<User> response) {
						if (response.code() == 200) {
							assert response.body() != null;
							userIntent.putExtra("username", response.body().getLogin());
							ctx.startActivity(userIntent);
						} else {
							Log.e("getUser-code", String.valueOf(response.code()));
							ctx.startActivity(mainIntent);
						}
						finish();
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Log.e("onFailure-getUser", t.toString());
						ctx.startActivity(mainIntent);
						finish();
					}
				});
	}

	private void getFile(String owner, String repo, String filePath, String branch) {
		Call<ContentsResponse> call =
				RetrofitClient.getApiInterface(ctx).repoGetContents(owner, repo, filePath, branch);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<ContentsResponse> call,
							@NonNull Response<ContentsResponse> response) {
						if (response.code() == 200) {
							// check if file and open file/dir
							ContentsResponse file = response.body();
							assert file != null;
							if (file.getType().equals("file")) {
								repoIntent.putExtra("file", file);
								repoIntent.putExtra("branch", branch);
								goToRepoSection(owner, repo, "file");
							}
						} else {
							Log.e("getFile-onFailure", String.valueOf(response.code()));
							ctx.startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ContentsResponse> call, @NonNull Throwable t) {
						Log.e("getFile-onFailure", t.toString());
						// maybe it's a directory
						getDir(owner, repo, filePath, branch);
					}
				});
	}

	private void getDir(String owner, String repo, String filePath, String branch) {
		repoIntent.putExtra("branch", branch);
		repoIntent.putExtra("dir", filePath);
		goToRepoSection(owner, repo, "dir");
	}

	private void showNoActionButtons() {
		viewBinding.progressBar.setVisibility(View.GONE);

		switch (tinyDB.getInt("defaultScreenId")) {
			case 1: // repos
				mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
				ctx.startActivity(mainIntent);
				finish();
				break;
			case 2: // org
				mainIntent.putExtra("launchFragmentByLinkHandler", "org");
				ctx.startActivity(mainIntent);
				finish();
				break;
			case 3: // notifications
				mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
				ctx.startActivity(mainIntent);
				finish();
				break;
			case 4: // explore
				mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
				ctx.startActivity(mainIntent);
				finish();
				break;
			default: // show options
				viewBinding.noActionFrame.setVisibility(View.VISIBLE);
				viewBinding.addNewAccountFrame.setVisibility(View.GONE);

				viewBinding.openInBrowserNoActionFrame.setOnClickListener(
						noActionFrameOpenInBrowser -> {
							AppUtil.openUrlInBrowser(this, String.valueOf(data));
							finish();
						});

				viewBinding.repository.setOnClickListener(
						repository -> {
							tinyDB.putInt("defaultScreenId", 1);
							mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
							ctx.startActivity(mainIntent);
							finish();
						});

				viewBinding.organization.setOnClickListener(
						organization -> {
							tinyDB.putInt("defaultScreenId", 2);
							mainIntent.putExtra("launchFragmentByLinkHandler", "org");
							ctx.startActivity(mainIntent);
							finish();
						});

				viewBinding.notification.setOnClickListener(
						notification -> {
							tinyDB.putInt("defaultScreenId", 3);
							mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
							ctx.startActivity(mainIntent);
							finish();
						});

				viewBinding.explore.setOnClickListener(
						explore -> {
							tinyDB.putInt("defaultScreenId", 4);
							mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
							ctx.startActivity(mainIntent);
							finish();
						});

				viewBinding.launchApp2.setOnClickListener(
						launchApp2 -> {
							tinyDB.putInt("defaultScreenId", 0);
							ctx.startActivity(mainIntent);
							finish();
						});
		}
	}
}
