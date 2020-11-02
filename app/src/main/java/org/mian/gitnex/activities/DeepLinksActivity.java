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
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityDeeplinksBinding;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.PullRequests;
import org.mian.gitnex.models.UserRepositories;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import io.mikael.urlbuilder.UrlBuilder;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class DeepLinksActivity extends BaseActivity {

	private ActivityDeeplinksBinding viewBinding;
	private String currentInstance;
	private String instanceToken;
	private boolean noAccountFound = false;

	private Intent mainIntent;
	private Intent issueIntent;
	private Intent repoIntent;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_deeplinks;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityDeeplinksBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		mainIntent = new Intent(ctx, MainActivity.class);
		issueIntent = new Intent(ctx, IssueDetailActivity.class);
		repoIntent = new Intent(ctx, RepoDetailActivity.class);

		Intent intent = getIntent();
		Uri data = intent.getData();
		assert data != null;

		// check for login
		if(!tinyDB.getBoolean("loggedInMode")) {

			finish();
			ctx.startActivity(new Intent(ctx, LoginActivity.class));
		}

		// check for the links(URI) to be in the db
		UserAccountsApi userAccountsApi = new UserAccountsApi(ctx);
		List<UserAccount> userAccounts = userAccountsApi.usersAccounts();

		if(userAccounts.size() > 0) {

			String hostUri;
			for(int i = 0; i < userAccounts.size(); i++) {

				hostUri = userAccounts.get(i).getInstanceUrl();

				currentInstance = userAccounts.get(i).getInstanceUrl();
				instanceToken = userAccounts.get(i).getToken();

				if(hostUri.toLowerCase().contains(Objects.requireNonNull(data.getHost().toLowerCase()))) {

					noAccountFound = false;
					break;
				}

				noAccountFound = true;
			}
		}

		if(noAccountFound) {

			checkInstance(data);
			return;
		}

		// redirect to proper fragment/activity, If no action is there, show options where user to want to go like repos, profile, notifications etc
		if(data.getPathSegments().size() > 0) {

			viewBinding.progressBar.setVisibility(View.GONE);
			String[] restOfUrl = Objects.requireNonNull(data.getPath()).split("/");

			if(data.getPathSegments().contains("issues")) { // issue

				if(!Objects.requireNonNull(data.getLastPathSegment()).contains("issues") & StringUtils.isNumeric(data.getLastPathSegment())) {

					issueIntent.putExtra("issueNumber", data.getLastPathSegment());

					tinyDB.putString("issueNumber", data.getLastPathSegment());
					tinyDB.putString("issueType", "Issue");

					tinyDB.putString("repoFullName", restOfUrl[restOfUrl.length - 4] + "/" + restOfUrl[restOfUrl.length - 3]);

					final String repoOwner = restOfUrl[restOfUrl.length - 4];
					final String repoName = restOfUrl[restOfUrl.length - 3];

					int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = new RepositoriesApi(ctx);

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

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						goToRepoSection(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 3], restOfUrl[restOfUrl.length - 2], "issue");
					}, 500);
				}
				else {

					ctx.startActivity(mainIntent);
					finish();
				}
			}
			else if(data.getPathSegments().contains("pulls")) { // pr

				if(!Objects.requireNonNull(data.getLastPathSegment()).contains("pulls") & StringUtils.isNumeric(data.getLastPathSegment())) {

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						getPullRequest(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 4], restOfUrl[restOfUrl.length - 3],
							Integer.parseInt(data.getLastPathSegment()));
					}, 500);

				}
				else if(Objects.requireNonNull(data.getLastPathSegment()).contains("pulls")) {

					new Handler(Looper.getMainLooper()).postDelayed(() -> {

						goToRepoSection(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 3], restOfUrl[restOfUrl.length - 2], "pull");
					}, 500);
				}
				else {

					ctx.startActivity(mainIntent);
					finish();
				}

			}
			else if(!restOfUrl[restOfUrl.length - 2].equals("") & !restOfUrl[restOfUrl.length - 1].equals("")) { // go to repo

				new Handler(Looper.getMainLooper()).postDelayed(() -> {

					goToRepoSection(currentInstance, instanceToken, restOfUrl[restOfUrl.length - 2], restOfUrl[restOfUrl.length - 1], "repo");
				}, 500);
			}
			else { // no action, show options

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
		else {

			ctx.startActivity(mainIntent);
			finish();
		}
	}

	private void checkInstance(Uri data) {

		URI host;
		if(data.getPort() > 0) {

			host = UrlBuilder.fromString(UrlHelper.fixScheme(data.getHost(), "https")).withPort(data.getPort()).toUri();
		}
		else {

			host = UrlBuilder.fromString(UrlHelper.fixScheme(data.getHost(), "https")).toUri();
		}

		URI instanceUrl = UrlBuilder.fromUri(host).withScheme(data.getScheme().toLowerCase()).withPath(PathsHelper.join(host.getPath(), "/api/v1/"))
			.toUri();

		Call<GiteaVersion> callVersion;
		callVersion = RetrofitClient
			.getApiInterface(ctx, instanceUrl.toString())
			.getGiteaVersion();

		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.isSuccessful() || responseVersion.code() == 403) {

					viewBinding.progressBar.setVisibility(View.GONE);
					viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
					viewBinding.noActionFrame.setVisibility(View.GONE);
					viewBinding.addAccountText.setText(String.format(getResources().getString(R.string.accountDoesNotExist), data.getHost()));

					viewBinding.addNewAccount.setOnClickListener(addNewAccount -> {

						Intent accountIntent = new Intent(ctx, AddNewAccountActivity.class);
						startActivity(accountIntent);
						finish();
					});

					viewBinding.openInBrowser.setOnClickListener(addNewAccount -> {

						Intent intentBrowser = new Intent();
						intentBrowser.setAction(Intent.ACTION_VIEW);
						intentBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
						intentBrowser.setData(Uri.parse(String.valueOf(host)));
						startActivity(intentBrowser);
						finish();
					});

					viewBinding.launchApp.setOnClickListener(launchApp -> {

						startActivity(mainIntent);
						finish();
					});
				}
				else {

					Toasty.error(ctx, getResources().getString(R.string.versionUnknown));
					finish();
				}
			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {

				Toasty.error(ctx, getResources().getString(R.string.versionUnknown));
				finish();
			}
		});
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
							tinyDB.putString("prForkFullName", ctx.getString(R.string.prDeletedFrok));
						}
					}

					tinyDB.putString("issueNumber", String.valueOf(index));
					tinyDB.putString("prMergeable", String.valueOf(prInfo.isMergeable()));
					tinyDB.putString("issueType", "Pull");

					tinyDB.putString("repoFullName", repoOwner + "/" + repoName);

					int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = new RepositoriesApi(ctx);

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

					Log.e("onFailure-links-pr", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<PullRequests> call, @NonNull Throwable t) {

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
					tinyDB.putString("repoBranch", repoInfo.getDefault_branch());

					int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");
					RepositoriesApi repositoryData = new RepositoriesApi(ctx);

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

					Log.e("onFailure-links", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				Log.e("onFailure-links", t.toString());
			}
		});
	}
}
