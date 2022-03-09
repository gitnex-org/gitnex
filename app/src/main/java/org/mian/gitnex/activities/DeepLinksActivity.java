package org.mian.gitnex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.models.Files;
import org.gitnex.tea4j.models.Organization;
import org.gitnex.tea4j.models.PullRequests;
import org.gitnex.tea4j.models.UserInfo;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityDeeplinksBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.UrlHelper;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import io.mikael.urlbuilder.UrlBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private String currentInstance;
	private String instanceToken;
	private boolean accountFound = false;

	private Intent mainIntent;
	private Intent issueIntent;
	private Intent repoIntent;
	private Intent orgIntent;
	private Intent userIntent;

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
		Uri data = intent.getData();
		assert data != null;

		// check for login
		if(!tinyDB.getBoolean("loggedInMode")) {
			Intent loginIntent = new Intent(ctx, LoginActivity.class);
			loginIntent.putExtra("instanceUrl", data.getHost());
			ctx.startActivity(loginIntent);
			finish();
		}

		// check for the links(URI) to be in the db
		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		List<UserAccount> userAccounts = userAccountsApi.usersAccounts();

		for(UserAccount userAccount : userAccounts) {

			String hostUri = userAccount.getInstanceUrl();

			currentInstance = userAccount.getInstanceUrl();
			instanceToken = userAccount.getToken();
			String host = data.getHost();
			if (host == null) host = "";

			if(hostUri.toLowerCase().contains(host.toLowerCase())) {

				accountFound = true;

				AppUtil.switchToAccount(ctx, userAccount);
				break;

			}
		}

		if(accountFound) {

			// redirect to proper fragment/activity, if no action is there, show options where user to want to go like repos, profile, notifications etc
			if(data.getPathSegments().size() == 1) {
				if(data.getLastPathSegment().equals("notifications")) { // notifications
					mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(data.getLastPathSegment().equals("explore")) { // explore
					mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(data.getLastPathSegment().equals(tinyDB.getString("userLogin"))) { // your user profile
					mainIntent.putExtra("launchFragmentByLinkHandler", "profile");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(data.getLastPathSegment().equals("admin")) {
					mainIntent.putExtra("launchFragmentByLinkHandler", "admin");
					ctx.startActivity(mainIntent);
					finish();
				}
				else {
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						getUserOrOrg(currentInstance, instanceToken, data.getLastPathSegment()), 500);
				}
			}
			else if(data.getPathSegments().size() == 2) {
				if(data.getPathSegments().get(0).equals("explore")) { // specific explore tab
					if(data.getPathSegments().get(1).equals("organizations")) { // orgs
						mainIntent.putExtra("exploreOrgs", true);
					}
					mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(data.getPathSegments().get(0).equals("user") && data.getPathSegments().get(1).equals("login")) { // open login
					Intent loginIntent = new Intent(ctx, AddNewAccountActivity.class);
					loginIntent.putExtra("instanceUrl", data.getHost());
					loginIntent.putExtra("instanceProtocol", data.getScheme());
					ctx.startActivity(loginIntent);
					finish();
				}
				else if(data.getPathSegments().get(0).equals("admin")) {
					mainIntent.putExtra("launchFragmentByLinkHandler", "admin");
					mainIntent.putExtra("giteaAdminAction", data.getLastPathSegment());
					ctx.startActivity(mainIntent);
					finish();
				}
				else if(!data.getPathSegments().get(0).equals("") & !data.getLastPathSegment().equals("")) { // go to repo
					String repo = data.getLastPathSegment();
					if (repo.endsWith(".git")) { // Git clone URL
						repo = repo.substring(0, repo.length() - 4);
					}
					String finalRepo = repo;
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), finalRepo, "repo"), 500);
				}
				else { // no action, show options
					showNoActionButtons();
				}
			}
			else if(data.getPathSegments().size() >= 3) {
				if(data.getPathSegments().get(2).equals("issues")) { // issue

					if(!Objects.requireNonNull(data.getLastPathSegment()).contains("issues") & StringUtils.isNumeric(data.getLastPathSegment())) {

						issueIntent.putExtra("issueNumber", data.getLastPathSegment());
						issueIntent.putExtra("openedFromLink", "true");

						String[] urlSplitted = data.toString().split("#");
						if (urlSplitted.length == 2) {
							issueIntent.putExtra("issueComment", urlSplitted[1]);
						}

						tinyDB.putString("issueNumber", data.getLastPathSegment());
						tinyDB.putString("issueType", "Issue");

						tinyDB.putString("repoFullName", data.getPathSegments().get(0) + "/" + data.getPathSegments().get(1));

						final String repoOwner = data.getPathSegments().get(0);
						final String repoName = data.getPathSegments().get(1);

						int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
						RepositoriesApi repositoryData = BaseApi.getInstance(ctx, RepositoriesApi.class);

						Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

						if(count == 0) {

							long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
							tinyDB.putLong("repositoryId", id);
						}
						else {

							Repository dataRepo = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
							tinyDB.putLong("repositoryId", dataRepo.getRepositoryId());
						}

						ctx.startActivity(issueIntent);
						finish();
					}
					else if(Objects.requireNonNull(data.getLastPathSegment()).contains("issues")) {
						new Handler(Looper.getMainLooper()).postDelayed(() ->
							goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "issue"), 500);
					}
					else if(data.getLastPathSegment().equals("new")) {
						new Handler(Looper.getMainLooper()).postDelayed(() ->
							goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "issueNew"), 500);
					}
					else {
						ctx.startActivity(mainIntent);
						finish();
					}
				}
				else if(data.getPathSegments().get(2).equals("pulls")) { // pr

					if(!Objects.requireNonNull(data.getLastPathSegment()).contains("pulls") & StringUtils.isNumeric(data.getLastPathSegment())) {

						new Handler(Looper.getMainLooper()).postDelayed(() -> {

							String[] urlSplitted = data.toString().split("#");
							if (urlSplitted.length == 2) {
								issueIntent.putExtra("issueComment", urlSplitted[1]);
							}

							getPullRequest(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), Integer.parseInt(data.getLastPathSegment()));
						}, 500);

					}
					else if(Objects.requireNonNull(data.getLastPathSegment()).contains("pulls")) {
						new Handler(Looper.getMainLooper()).postDelayed(() ->
							goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "pull"), 500);
					}
					else if(data.getLastPathSegment().equals("files")) { // pr diff
						new Handler(Looper.getMainLooper()).postDelayed(() -> {
							issueIntent.putExtra("openPrDiff", "true");
							getPullRequest(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), Integer.parseInt(data.getPathSegments().get(3)));
						}, 500);
					}
					else {
						ctx.startActivity(mainIntent);
						finish();
					}
				}

				else if(data.getPathSegments().get(2).equals("compare")) { // new pull request
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "pullNew"), 500);
				}
				else if(data.getPathSegments().get(2).equals("commit")) {
					repoIntent.putExtra("sha", data.getLastPathSegment());
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "commit"), 500);
				}
				else if(data.getPathSegments().get(2).equals("commits")) { // commits list
					String branch = data.getLastPathSegment();
					repoIntent.putExtra("branchName", branch);
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "commitsList"), 500);
				}
				else if(data.getPathSegments().get(2).equals("milestones") && data.getLastPathSegment().equals("new")) { // new milestone
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "milestonesNew"), 500);
				}
				else if(data.getPathSegments().get(2).equals("milestones")) { // milestones
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "milestones"), 500);
				}
				else if(data.getPathSegments().get(2).equals("milestone")) { // milestone
					repoIntent.putExtra("milestoneId", data.getLastPathSegment());
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "milestones"), 500);
				}
				else if(data.getPathSegments().get(2).equals("releases") && data.getLastPathSegment().equals("new")) { // new release
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "newRelease"), 500);
				}
				else if(data.getPathSegments().get(2).equals("releases")) { // releases
					if(data.getPathSegments().size() == 5) {
						if(data.getPathSegments().get(2).equals("releases") && data.getPathSegments().get(3).equals("tag")) {
							repoIntent.putExtra("releaseTagName", data.getLastPathSegment());
						}
					}
					new Handler(Looper.getMainLooper()).postDelayed(
						() -> goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1),
							"releases"), 500);
				}
				else if(data.getPathSegments().get(2).equals("labels")) { // labels
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "labels"), 500);
				}
				else if(data.getPathSegments().get(2).equals("settings")) { // repo settings
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "settings"), 500);
				}
				else if(data.getLastPathSegment().equals("branches")) { // branches list
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "branchesList"), 500);
				}
				else if(data.getPathSegments().size() == 5 && data.getPathSegments().get(2).equals("src") && data.getPathSegments().get(3).equals("branch")) { // branch
					repoIntent.putExtra("selectedBranch", data.getLastPathSegment());
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						goToRepoSection(currentInstance, instanceToken, data.getPathSegments().get(0), data.getPathSegments().get(1), "branch"), 500);
				}
				else if(data.getPathSegments().get(2).equals("src") && data.getPathSegments().get(3).equals("branch")) { // file/dir
					StringBuilder filePath = new StringBuilder();
					ArrayList<String> segments = new ArrayList<>(data.getPathSegments());
					segments.subList(0, 5).clear();
					for (String item : segments) {
						filePath.append(item);
						filePath.append("/");
					}
					filePath.deleteCharAt(filePath.toString().length() - 1);
					new Handler(Looper.getMainLooper()).postDelayed(() ->
						getFile(currentInstance, instanceToken, data.getPathSegments().get(0),
							data.getPathSegments().get(1), filePath.toString(), data.getPathSegments().get(4)), 500);
				}
				else { // no action, show options
					showNoActionButtons();
				}
			}
			else {

				startActivity(mainIntent);
				finish();
			}
		}
		else {

			viewBinding.progressBar.setVisibility(View.GONE);
			viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
			viewBinding.noActionFrame.setVisibility(View.GONE);
			viewBinding.addAccountText.setText(String.format(getResources().getString(R.string.accountDoesNotExist), data.getHost()));

			viewBinding.addNewAccount.setOnClickListener(addNewAccount -> {

				Intent accountIntent = new Intent(ctx, AddNewAccountActivity.class);
				accountIntent.putExtra("instanceUrl", data.getHost());
				startActivity(accountIntent);
				finish();
			});

			viewBinding.openInBrowser.setOnClickListener(addNewAccount -> {

				Integer port = data.getPort() >= 0 ? data.getPort() : null;

				URI host = UrlBuilder.fromString(UrlHelper.fixScheme(data.getHost(), "https"))
					.withPort(port)
					.toUri();

				AppUtil.openUrlInBrowser(this, String.valueOf(host));
				finish();

			});

			viewBinding.launchApp.setOnClickListener(launchApp -> {

				startActivity(mainIntent);
				finish();
			});
		}
	}

	private void getPullRequest(String url, String token, String repoOwner, String repoName, int index) {

		Call<PullRequests> call = RetrofitClient
			.getApiInterface(ctx, url)
			.getPullRequestByIndex(token, repoOwner, repoName, index);

		call.enqueue(new Callback<PullRequests>() {

			@Override
			public void onResponse(@NonNull Call<PullRequests> call, @NonNull retrofit2.Response<PullRequests> response) {

				PullRequests prInfo = response.body();

				if (response.code() == 200) {

					assert prInfo != null;

					issueIntent.putExtra("issueNumber", index);
					issueIntent.putExtra("prMergeable", prInfo.isMergeable());
					issueIntent.putExtra("openedFromLink", "true");

					if(prInfo.getHead() != null) {

						issueIntent.putExtra("prHeadBranch", prInfo.getHead().getRef());
						tinyDB.putString("prHeadBranch", prInfo.getHead().getRef());

						if(prInfo.getHead().getRepo() != null) {

							tinyDB.putString("prIsFork", String.valueOf(prInfo.getHead().getRepo().isFork()));
							tinyDB.putString("prForkFullName", prInfo.getHead().getRepo().getFull_name());
						}
						else {

							// pull was done from a deleted fork
							tinyDB.putString("prIsFork", "true");
							tinyDB.putString("prForkFullName", ctx.getString(R.string.prDeletedFork));
						}
					}

					tinyDB.putString("issueNumber", String.valueOf(index));
					tinyDB.putString("prMergeable", String.valueOf(prInfo.isMergeable()));
					tinyDB.putString("issueType", "Pull");

					tinyDB.putString("repoFullName", repoOwner + "/" + repoName);

					int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = BaseApi.getInstance(ctx, RepositoriesApi.class);

					Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

					if(count == 0) {

						long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDB.putLong("repositoryId", id);
					}
					else {

						Repository dataRepo = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDB.putLong("repositoryId", dataRepo.getRepositoryId());
					}

					ctx.startActivity(issueIntent);
					finish();
				}

				else {

					ctx.startActivity(issueIntent);
					finish();
					Log.e("onFailure-links-pr", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<PullRequests> call, @NonNull Throwable t) {

				ctx.startActivity(issueIntent);
				finish();
				Log.e("onFailure-links-pr", t.toString());
			}
		});
	}

	private void goToRepoSection(String url, String token, String repoOwner, String repoName, String type) {

		Call<UserRepositories> call = RetrofitClient
			.getApiInterface(ctx, url)
			.getUserRepository(token, repoOwner, repoName);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if (response.code() == 200) {

					assert repoInfo != null;

					repoIntent.putExtra("repoFullName", repoInfo.getFullName());
					repoIntent.putExtra("goToSection", "yes");
					repoIntent.putExtra("goToSectionType", type);

					tinyDB.putString("repoFullName", repoInfo.getFullName());
					if(repoInfo.getPrivateFlag()) {

						tinyDB.putString("repoType", getResources().getString(R.string.strPrivate));
					}
					else {

						tinyDB.putString("repoType", getResources().getString(R.string.strPublic));
					}
					tinyDB.putBoolean("isRepoAdmin", repoInfo.getPermissions().isAdmin());
					tinyDB.putBoolean("canPush", repoInfo.getPermissions().canPush());
					tinyDB.putString("repoBranch", repoInfo.getDefault_branch());

					int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = BaseApi.getInstance(ctx, RepositoriesApi.class);

					Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

					if(count == 0) {

						long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDB.putLong("repositoryId", id);
					}
					else {

						Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
						tinyDB.putLong("repositoryId", data.getRepositoryId());
					}

					ctx.startActivity(repoIntent);
					finish();
				}

				else {

					ctx.startActivity(mainIntent);
					finish();
					Log.e("onFailure-goToRepo", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				ctx.startActivity(mainIntent);
				finish();
				Log.e("onFailure-goToRepo", t.toString());
			}
		});
	}

	private void getUserOrOrg(String url, String instanceToken, String userOrgName) {
		Call<Organization> call = RetrofitClient.getApiInterface(ctx, url).getOrganization(instanceToken, userOrgName);

		call.enqueue(new Callback<Organization>() {

			@Override
			public void onResponse(@NonNull Call<Organization> call, @NonNull Response<Organization> response) {
				if(response.code() == 404) { // org doesn't exist or it's a user user
					Log.d("getUserOrOrg-404", String.valueOf(response.code()));
					getUser(url, instanceToken, userOrgName);
				}
				else if(response.code() == 200) { // org
					assert response.body() != null;
					orgIntent.putExtra("orgName", response.body().getUsername());


					TinyDB tinyDb = TinyDB.getInstance(ctx);
					tinyDb.putString("orgName", response.body().getUsername());
					tinyDb.putString("organizationId", String.valueOf(response.body().getId()));
					tinyDb.putBoolean("organizationAction", true);
					ctx.startActivity(orgIntent);
					finish();
				}
				else {
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

	private void getUser(String url, String instanceToken, String userName) {
		Call<UserInfo> call = RetrofitClient.getApiInterface(ctx, url).getUserProfile(instanceToken, userName);

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {
				if(response.code() == 200) {
					assert response.body() != null;
					userIntent.putExtra("username", response.body().getLogin());
					ctx.startActivity(userIntent);
				}
				else {
					Log.e("getUser-code", String.valueOf(response.code()));
					ctx.startActivity(mainIntent);
				}
				finish();
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
				Log.e("onFailure-getUser", t.toString());
				ctx.startActivity(mainIntent);
				finish();
			}
		});
	}

	private void getFile(String url, String instanceToken, String owner, String repo, String filePath, String branch) {
		Call<Files> call = RetrofitClient.getApiInterface(ctx, url).getSingleFileContents(instanceToken, owner, repo, filePath, branch);

		call.enqueue(new Callback<Files>() {

			@Override
			public void onResponse(@NonNull Call<Files> call, @NonNull Response<Files> response) {
				if(response.code() == 200) {
					// check if file and open file/dir
					Files file = response.body();
					assert file != null;
					if(file.getType().equals("file")) {
						repoIntent.putExtra("file", file);
						repoIntent.putExtra("branch", branch);
						goToRepoSection(url, instanceToken, owner, repo, "file");
					}
				}
				else {
					Log.e("getFile-onFailure", String.valueOf(response.code()));
					ctx.startActivity(mainIntent);
					finish();
				}
			}

			@Override
			public void onFailure(@NonNull Call<Files> call, @NonNull Throwable t) {
				Log.e("getFile-onFailure", t.toString());
				// maybe it's a directory
				getDir(url, instanceToken, owner, repo, filePath, branch);
			}
		});
	}

	private void getDir(String url, String instanceToken, String owner, String repo, String filePath, String branch) {
		repoIntent.putExtra("branch", branch);
		repoIntent.putExtra("dir", filePath);
		goToRepoSection(url, instanceToken, owner, repo, "dir");
	}

	private void showNoActionButtons()  {
		viewBinding.progressBar.setVisibility(View.GONE);

		if(tinyDB.getInt("defaultScreenId") == 1) { // repos

			mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
			ctx.startActivity(mainIntent);
			finish();
		}
		else if(tinyDB.getInt("defaultScreenId") == 2) { // org

			mainIntent.putExtra("launchFragmentByLinkHandler", "org");
			ctx.startActivity(mainIntent);
			finish();
		}
		else if(tinyDB.getInt("defaultScreenId") == 3) { // notifications

			mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
			ctx.startActivity(mainIntent);
			finish();
		}
		else if(tinyDB.getInt("defaultScreenId") == 4) { // explore

			mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
			ctx.startActivity(mainIntent);
			finish();
		}
		else if(tinyDB.getInt("defaultScreenId") == 0) { // show options

			viewBinding.noActionFrame.setVisibility(View.VISIBLE);
			viewBinding.addNewAccountFrame.setVisibility(View.GONE);

			viewBinding.repository.setOnClickListener(repository -> {

				tinyDB.putInt("defaultScreenId", 1);
				mainIntent.putExtra("launchFragmentByLinkHandler", "repos");
				ctx.startActivity(mainIntent);
				finish();
			});

			viewBinding.organization.setOnClickListener(organization -> {

				tinyDB.putInt("defaultScreenId", 2);
				mainIntent.putExtra("launchFragmentByLinkHandler", "org");
				ctx.startActivity(mainIntent);
				finish();
			});

			viewBinding.notification.setOnClickListener(notification -> {

				tinyDB.putInt("defaultScreenId", 3);
				mainIntent.putExtra("launchFragmentByLinkHandler", "notification");
				ctx.startActivity(mainIntent);
				finish();
			});

			viewBinding.explore.setOnClickListener(explore -> {

				tinyDB.putInt("defaultScreenId", 4);
				mainIntent.putExtra("launchFragmentByLinkHandler", "explore");
				ctx.startActivity(mainIntent);
				finish();
			});

			viewBinding.launchApp2.setOnClickListener(launchApp2 -> {

				tinyDB.putInt("defaultScreenId", 0);
				ctx.startActivity(mainIntent);
				finish();
			});
		}
	}
}
