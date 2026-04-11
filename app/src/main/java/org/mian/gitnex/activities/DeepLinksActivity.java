package org.mian.gitnex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
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
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author @mmarif
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

		initNavigationIntents();

		Intent intent = getIntent();
		if (intent == null || intent.getData() == null) {
			startActivity(mainIntent);
			finish();
			return;
		}

		data = normalizeUri(intent.getData());

		if (tinyDB.getInt("currentActiveAccountId", -1) <= -1) {
			goToLogin(data.getHost());
			return;
		}

		UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);
		if (userAccountsApi == null) {
			startActivity(mainIntent);
			finish();
			return;
		}

		matchAndSwitchAccount(userAccountsApi.loggedInUserAccounts());

		if (accountFound) {
			handleRouting(data);
		} else {
			handleAccountNotFound();
		}
	}

	private Uri normalizeUri(Uri incomingData) {
		if ("gitnex".equals(incomingData.getScheme())) {
			return Uri.parse(incomingData.toString().replaceFirst("gitnex://", "https://"));
		}
		return incomingData;
	}

	private void matchAndSwitchAccount(List<UserAccount> userAccounts) {
		if (userAccounts == null) return;

		String hostExternal = data.getHost();
		int portExternal = data.getPort();
		String hostUrlExternal =
				(portExternal > 0) ? hostExternal + ":" + portExternal : hostExternal;

		if (hostUrlExternal == null) hostUrlExternal = "";

		for (UserAccount userAccount : userAccounts) {
			String hostUri = userAccount.getInstanceUrl();
			if (hostUri != null && hostUri.toLowerCase().contains(hostUrlExternal.toLowerCase())) {
				accountFound = true;
				AppUtil.switchToAccount(ctx, userAccount, false);
				break;
			}
		}
	}

	private void handleRouting(Uri uri) {
		List<String> segments = uri.getPathSegments();
		int size = segments.size();
		String last = uri.getLastPathSegment();

		if (size == 0) {
			showNoActionButtons();
			return;
		}

		if (size == 1) {
			handlePathSizeOne(last);
		} else if (size == 2) {
			handlePathSizeTwo(segments, last);
		} else {
			handlePathSizeThreePlus(segments, last);
		}
	}

	private void handlePathSizeOne(String last) {
		if (last == null) return;
		switch (last) {
			case "notifications":
				launchMainWithFragment("notification");
				break;
			case "explore":
				launchMainWithFragment("explore");
				break;
			case "admin":
				launchMainWithFragment("admin");
				break;
			default:
				delayedTask(() -> getUserOrOrg(last));
				break;
		}
	}

	private void handlePathSizeTwo(List<String> segments, String last) {
		String first = segments.get(0);

		if ("explore".equals(first)) {
			if ("organizations".equals(last)) {
				mainIntent.putExtra("exploreOrgs", true);
			}
			launchMainWithFragment("explore");
		} else if ("user".equals(first) && "login".equals(last)) {
			goToLogin(data.getHost());
		} else if ("admin".equals(first)) {
			mainIntent.putExtra("launchFragmentByLinkHandler", "admin");
			mainIntent.putExtra("giteaAdminAction", last);
			startActivity(mainIntent);
			finish();
		} else if (!first.isEmpty() && last != null && !last.isEmpty()) {
			String repo = last;
			if (repo.endsWith(".git")) {
				repo = repo.substring(0, repo.length() - 4);
			}
			String finalRepo = repo;
			delayedTask(() -> goToRepoSection(first, finalRepo, "repo"));
		} else {
			showNoActionButtons();
		}
	}

	private void handlePathSizeThreePlus(List<String> segments, String last) {
		String owner = segments.get(0);
		String repo = segments.get(1);
		String action = segments.get(2);

		switch (action) {
			case "issues":
				handleIssues(owner, repo, last);
				break;
			case "pulls":
				handlePulls(segments);
				break;
			case "compare":
				delayedTask(() -> goToRepoSection(owner, repo, "pullNew"));
				break;
			case "commit":
				repoIntent.putExtra("sha", last);
				delayedTask(() -> goToRepoSection(owner, repo, "commit"));
				break;
			case "commits":
				repoIntent.putExtra("branchName", last);
				delayedTask(() -> goToRepoSection(owner, repo, "commitsList"));
				break;
			case "milestones":
				String mType = "new".equals(last) ? "milestonesNew" : "milestones";
				delayedTask(() -> goToRepoSection(owner, repo, mType));
				break;
			case "milestone":
				repoIntent.putExtra("milestoneId", last);
				delayedTask(() -> goToRepoSection(owner, repo, "milestones"));
				break;
			case "releases":
				handleReleases(segments, last);
				break;
			case "labels":
				delayedTask(() -> goToRepoSection(owner, repo, "labels"));
				break;
			case "settings":
				delayedTask(() -> goToRepoSection(owner, repo, "settings"));
				break;
			case "wiki":
				handleWiki(owner, repo);
				break;
			case "src":
				handleSource(segments, last);
				break;
			default:
				if ("branches".equals(last)) {
					delayedTask(() -> goToRepoSection(owner, repo, "branchesList"));
				} else {
					showNoActionButtons();
				}
				break;
		}
	}

	private void handleIssues(String owner, String repo, String last) {
		if (last != null && !last.contains("issues") && StringUtils.isNumeric(last)) {
			issueIntent.putExtra("issueNumber", last);
			issueIntent.putExtra("openedFromLink", "true");

			String[] urlSplitted = data.toString().split("#");
			if (urlSplitted.length == 2) {
				issueIntent.putExtra("issueComment", urlSplitted[1]);
			}

			IssueContext issue =
					new IssueContext(
							new RepositoryContext(owner, repo, ctx),
							Integer.parseInt(last),
							"Issue");
			issue.getRepository().saveToDB(ctx);
			issueIntent.putExtra(IssueContext.INTENT_EXTRA, issue);
			startActivity(issueIntent);
			finish();
		} else {
			String type = "new".equals(last) ? "issueNew" : "issue";
			delayedTask(() -> goToRepoSection(owner, repo, type));
		}
	}

	private void handlePulls(List<String> segments) {
		String owner = segments.get(0);
		String repo = segments.get(1);
		String last = segments.get(segments.size() - 1);

		if (last != null && !last.contains("pulls") && StringUtils.isNumeric(last)) {
			delayedTask(
					() -> {
						String[] urlSplitted = data.toString().split("#");
						if (urlSplitted.length == 2) {
							issueIntent.putExtra("issueComment", urlSplitted[1]);
						}
						getPullRequest(owner, repo, Integer.parseInt(last));
					});
		} else if ("files".equals(last)) {
			delayedTask(
					() -> {
						issueIntent.putExtra("openPrDiff", "true");
						getPullRequest(owner, repo, Integer.parseInt(segments.get(3)));
					});
		} else {
			delayedTask(() -> goToRepoSection(owner, repo, "pull"));
		}
	}

	private void handleSource(List<String> segments, String last) {
		if (segments.size() == 5 && "branch".equals(segments.get(3))) {
			repoIntent.putExtra("selectedBranch", last);
			delayedTask(() -> goToRepoSection(segments.get(0), segments.get(1), "branch"));
		} else if (segments.size() > 4 && "branch".equals(segments.get(3))) {
			StringBuilder filePath = new StringBuilder();
			ArrayList<String> fileSegments = new ArrayList<>(segments);
			fileSegments.subList(0, 5).clear();
			for (String item : fileSegments) {
				filePath.append(item).append("/");
			}
			if (filePath.length() > 0) filePath.deleteCharAt(filePath.length() - 1);
			delayedTask(
					() ->
							getFile(
									segments.get(0),
									segments.get(1),
									filePath.toString(),
									segments.get(4)));
		}
	}

	private void handleWiki(String owner, String repo) {
		String action = data.getQueryParameter("action");
		String type = (action != null && action.equalsIgnoreCase("_new")) ? "wikiNew" : "wiki";
		delayedTask(() -> goToRepoSection(owner, repo, type));
	}

	private void handleReleases(List<String> segments, String last) {
		if (segments.size() == 5 && "tag".equals(segments.get(3))) {
			repoIntent.putExtra("releaseTagName", last);
		}
		String type = "new".equals(last) ? "newRelease" : "releases";
		delayedTask(() -> goToRepoSection(segments.get(0), segments.get(1), type));
	}

	private void handleAccountNotFound() {
		viewBinding.progressBar.setVisibility(View.GONE);
		viewBinding.addNewAccountFrame.setVisibility(View.VISIBLE);
		viewBinding.noActionFrame.setVisibility(View.GONE);
		viewBinding.addAccountText.setText(
				String.format(getString(R.string.accountDoesNotExist), data.getHost()));

		viewBinding.addNewAccount.setOnClickListener(
				v -> {
					Intent loginIntent = new Intent(ctx, LoginActivity.class);
					loginIntent.putExtra("instanceUrl", data.getHost());
					loginIntent.putExtra("mode", "new_account");
					startActivity(loginIntent);
					finish();
				});

		viewBinding.openInBrowser.setOnClickListener(
				v -> {
					AppUtil.openUrlInBrowser(this, String.valueOf(data));
					finish();
				});
	}

	private void initNavigationIntents() {
		mainIntent = new Intent(ctx, MainActivity.class);
		issueIntent = new Intent(ctx, IssueDetailActivity.class);
		repoIntent = new Intent(ctx, RepoDetailActivity.class);
		orgIntent = new Intent(ctx, OrganizationDetailActivity.class);
		userIntent = new Intent(ctx, ProfileActivity.class);
	}

	private void launchMainWithFragment(String fragmentKey) {
		mainIntent.putExtra("launchFragmentByLinkHandler", fragmentKey);
		startActivity(mainIntent);
		finish();
	}

	private void goToLogin(String host) {
		Intent loginIntent = new Intent(ctx, LoginActivity.class);
		loginIntent.putExtra("instanceUrl", host);
		startActivity(loginIntent);
		finish();
	}

	private void delayedTask(Runnable task) {
		new Handler(Looper.getMainLooper()).postDelayed(task, 500);
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
							@NonNull Response<PullRequest> response) {
						if (response.isSuccessful() && response.body() != null) {
							PullRequest prInfo = response.body();
							issueIntent.putExtra("openedFromLink", "true");
							IssueContext issue =
									new IssueContext(
											prInfo,
											new RepositoryContext(repoOwner, repoName, ctx));
							issue.getRepository().saveToDB(ctx);
							issueIntent.putExtra(IssueContext.INTENT_EXTRA, issue);
							startActivity(issueIntent);
							finish();
						} else {
							startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
						startActivity(mainIntent);
						finish();
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
							@NonNull Response<Repository> response) {
						if (response.isSuccessful() && response.body() != null) {
							RepositoryContext repo = new RepositoryContext(response.body(), ctx);
							repoIntent.putExtra("goToSection", "yes");
							repoIntent.putExtra("goToSectionType", type);
							repo.saveToDB(ctx);
							repoIntent.putExtra(RepositoryContext.INTENT_EXTRA, repo);
							startActivity(repoIntent);
							finish();
						} else {
							startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						startActivity(mainIntent);
						finish();
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
						if (response.code() == 404 || response.code() == 504) {
							getUser(userOrgName);
						} else if (response.isSuccessful() && response.body() != null) {
							orgIntent.putExtra("orgName", response.body().getUsername());
							orgIntent.putExtra("organizationId", response.body().getId());
							orgIntent.putExtra("organizationAction", true);
							startActivity(orgIntent);
							finish();
						} else {
							startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {
						startActivity(mainIntent);
						finish();
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
						if (response.isSuccessful() && response.body() != null) {
							userIntent.putExtra("username", response.body().getLogin());
							startActivity(userIntent);
						} else {
							startActivity(mainIntent);
						}
						finish();
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						startActivity(mainIntent);
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
						if (response.isSuccessful() && response.body() != null) {
							ContentsResponse file = response.body();
							if ("file".equals(file.getType())) {
								repoIntent.putExtra("file", file);
								repoIntent.putExtra("branch", branch);
								goToRepoSection(owner, repo, "file");
							}
						} else {
							startActivity(mainIntent);
							finish();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ContentsResponse> call, @NonNull Throwable t) {
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

		String savedValue =
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_LINK_HANDLER_KEY);
		int handler = 0;
		try {
			if (savedValue != null) handler = Integer.parseInt(savedValue);
		} catch (NumberFormatException ignored) {
		}

		switch (handler) {
			case 1: // Home
				startActivity(mainIntent);
				finish();
				break;
			case 2: // Repos
				launchMainWithFragment("repos");
				break;
			case 3: // Notifications
				launchMainWithFragment("notification");
				break;
			default:
				viewBinding.noActionFrame.setVisibility(View.VISIBLE);
				viewBinding.addNewAccountFrame.setVisibility(View.GONE);

				setupActionCard(
						viewBinding.homeCard.getRoot(),
						R.string.home,
						R.drawable.ic_home,
						() -> {
							saveLinkPreference(1);
							startActivity(mainIntent);
							finish();
						});

				setupActionCard(
						viewBinding.repositoryCard.getRoot(),
						R.string.navRepos,
						R.drawable.ic_repo,
						() -> {
							saveLinkPreference(2);
							launchMainWithFragment("repos");
						});

				setupActionCard(
						viewBinding.notificationCard.getRoot(),
						R.string.pageTitleNotifications,
						R.drawable.ic_notifications,
						() -> {
							saveLinkPreference(3);
							launchMainWithFragment("notification");
						});

				viewBinding.openInBrowserNoActionFrame.setOnClickListener(
						v -> {
							AppUtil.openUrlInBrowser(this, String.valueOf(data));
							finish();
						});
				break;
		}
	}

	private void setupActionCard(View cardRoot, int titleRes, int iconRes, Runnable action) {
		TextView title = cardRoot.findViewById(R.id.cardTitle);
		TextView subtext = cardRoot.findViewById(R.id.cardSubtext);
		ImageView icon = cardRoot.findViewById(R.id.cardIcon);

		if (title != null) title.setText(getString(titleRes));
		if (icon != null) icon.setImageResource(iconRes);
		if (subtext != null) subtext.setVisibility(View.GONE);

		cardRoot.setOnClickListener(v -> action.run());
	}

	private void saveLinkPreference(int value) {
		AppDatabaseSettings.updateSettingsValue(
				ctx, String.valueOf(value), AppDatabaseSettings.APP_LINK_HANDLER_KEY);
	}
}
