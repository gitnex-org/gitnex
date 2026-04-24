package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.databinding.ActivityRepoDetailBinding;
import org.mian.gitnex.fragments.BottomSheetCreateIssue;
import org.mian.gitnex.fragments.BottomSheetCreateMilestone;
import org.mian.gitnex.fragments.BottomSheetCreatePullRequest;
import org.mian.gitnex.fragments.BottomSheetCreateRelease;
import org.mian.gitnex.fragments.BottomSheetCreateWiki;
import org.mian.gitnex.fragments.BottomsheetRepoMenu;
import org.mian.gitnex.fragments.CollaboratorsFragment;
import org.mian.gitnex.fragments.FilesFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.LabelsFragment;
import org.mian.gitnex.fragments.MilestonesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.fragments.RepoInfoFragment;
import org.mian.gitnex.fragments.WikiFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BadgeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.viewmodels.IssuesViewModel;
import org.mian.gitnex.viewmodels.PullRequestsViewModel;
import org.mian.gitnex.viewmodels.ReleasesViewModel;
import org.mian.gitnex.viewmodels.RepositoryDetailsViewModel;

/**
 * @author mmarif
 */
public class RepoDetailActivity extends BaseActivity
		implements BottomsheetRepoMenu.OnRepoMenuItemListener {

	private static final String TAG_INFO = "info";
	private static final String TAG_FILES = "files";
	private static final String TAG_ISSUES = "issues";
	private static final String TAG_PRS = "prs";
	private static final String TAG_RELEASES = "releases";
	private static final String TAG_WIKI = "wiki";
	private static final String TAG_MILESTONES = "milestones";
	private static final String TAG_LABELS = "labels";
	private static final String TAG_COLLAB = "collab";

	private ActivityRepoDetailBinding binding;
	public RepositoryContext repository;
	private RepositoryDetailsViewModel viewModel;
	private final FragmentManager fm = getSupportFragmentManager();
	private boolean isStarred = false;
	private boolean isWatched = false;
	private boolean isGiteaRepoActionsVisible = false;
	private boolean hasActions = false;
	private boolean adminStatus = false;
	private int activeTabId = R.id.btn_nav_details;
	private BadgeDrawable issuesBadge;
	private BadgeDrawable prBadge;
	private BadgeDrawable releaseBadge;

	private final String[] fragmentTags = {
		TAG_INFO,
		TAG_FILES,
		TAG_ISSUES,
		TAG_PRS,
		TAG_RELEASES,
		TAG_WIKI,
		TAG_MILESTONES,
		TAG_LABELS,
		TAG_COLLAB
	};

	private final int[] buttonIds = {
		R.id.btn_nav_details, R.id.btn_nav_files, R.id.btn_nav_issues,
		R.id.btn_nav_prs, R.id.btn_nav_releases, R.id.btn_nav_wiki,
		R.id.btn_nav_milestones, R.id.btn_nav_labels, R.id.btn_nav_collaborators
	};

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("active_tab_id", activeTabId);
	}

	private final ActivityResultLauncher<Intent> settingsLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == 200) {
							assert result.getData() != null;
							if (result.getData().getBooleanExtra("nameChanged", false)) {
								recreate();
							}
						}
					});

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			activeTabId = savedInstanceState.getInt("active_tab_id");
		}
		super.onCreate(savedInstanceState);
		binding = ActivityRepoDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		viewModel = new ViewModelProvider(this).get(RepositoryDetailsViewModel.class);
		repository = RepositoryContext.fromIntent(getIntent());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		observeViewModel();
		setupDockListeners();

		if (repository.hasRepository()) {
			completeInitialization(repository.getRepository());
			viewModel.checkRepoStatus(this, repository.getOwner(), repository.getName());
		} else {
			viewModel.fetchRepository(this, repository.getOwner(), repository.getName());
			viewModel.checkRepoStatus(this, repository.getOwner(), repository.getName());
		}

		getSupportFragmentManager()
				.setFragmentResultListener(
						"repo_hub_request",
						this,
						(requestKey, bundle) -> {
							String actionId = bundle.getString("repo_hub_item_id");
							if (actionId != null) handleHubAction(actionId);
						});

		setupProviderFlags();
	}

	@Override
	protected void onGlobalRefresh() {
		RepoInfoFragment repoInfoFragment =
				(RepoInfoFragment) getSupportFragmentManager().findFragmentByTag(TAG_INFO);
		if (repoInfoFragment != null) repoInfoFragment.refreshFromGlobal();

		IssuesFragment issuesFragment =
				(IssuesFragment) getSupportFragmentManager().findFragmentByTag(TAG_ISSUES);
		if (issuesFragment != null) issuesFragment.refreshFromGlobal();

		FilesFragment filesFragment =
				(FilesFragment) getSupportFragmentManager().findFragmentByTag(TAG_FILES);
		if (filesFragment != null) filesFragment.refreshFromGlobal();

		PullRequestsFragment pullRequestsFragment =
				(PullRequestsFragment) getSupportFragmentManager().findFragmentByTag(TAG_PRS);
		if (pullRequestsFragment != null) pullRequestsFragment.refreshFromGlobal();
	}

	private void setupProviderFlags() {
		String provider = getAccount().getAccount().getProvider();
		if (provider != null) {
			String serverVersion = getAccount().getAccount().getServerVersion();
			Version minVersion = new Version("1.24");
			Version currentVersion =
					Version.valid(serverVersion) ? new Version(serverVersion) : new Version("0.0");
			isGiteaRepoActionsVisible =
					"gitea".equals(provider) && !currentVersion.less(minVersion) && hasActions;
		}
	}

	private void observeViewModel() {
		IssuesViewModel issuesVM = new ViewModelProvider(this).get(IssuesViewModel.class);
		PullRequestsViewModel prVM = new ViewModelProvider(this).get(PullRequestsViewModel.class);
		ReleasesViewModel releasesViewModel =
				new ViewModelProvider(this).get(ReleasesViewModel.class);

		issuesVM.getRepoTotalCount()
				.observe(
						this,
						count -> {
							runOnUiThread(
									() -> {
										issuesBadge =
												BadgeHelper.updateBadge(
														this,
														binding.btnNavIssues,
														issuesBadge,
														count);
									});
						});

		prVM.getRepoPrTotalCount()
				.observe(
						this,
						prCount -> {
							runOnUiThread(
									() -> {
										prBadge =
												BadgeHelper.updateBadge(
														this, binding.btnNavPrs, prBadge, prCount);
									});
						});

		releasesViewModel
				.getReleasesTotalCount()
				.observe(
						this,
						releasesCount -> {
							runOnUiThread(
									() -> {
										releaseBadge =
												BadgeHelper.updateBadge(
														this,
														binding.btnNavReleases,
														releaseBadge,
														releasesCount);
									});
						});

		viewModel
				.getRepoData()
				.observe(
						this,
						repo -> {
							if (repo != null) {
								if (fm.findFragmentByTag(TAG_INFO) == null) {
									completeInitialization(repo);
								}
							}
						});

		viewModel
				.getIsStarred()
				.observe(
						this,
						starred -> {
							this.isStarred = starred;
							repository.setStarred(starred);
						});

		viewModel
				.getIsWatched()
				.observe(
						this,
						watched -> {
							this.isWatched = watched;
							repository.setWatched(watched);
						});

		viewModel
				.getIsActionLoading()
				.observe(
						this,
						loading -> {
							// binding.loadingIndicator.setVisibility(loading ? View.VISIBLE :
							// View.GONE);
						});

		viewModel
				.getActionSuccessEvent()
				.observe(
						this,
						resId -> {
							if (resId != null) {
								Toasty.show(this, getString(resId));
								viewModel.consumeActionEvents();
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								viewModel.consumeActionEvents();
							}
						});
	}

	private void completeInitialization(Repository repo) {
		repository.setRepository(repo);
		if (repo.getDefaultBranch() != null) {
			repository.setBranchRef(repo.getDefaultBranch());
		}

		if (repo.getPermissions() != null) {
			this.adminStatus = Boolean.TRUE.equals(repo.getPermissions().isAdmin());
		}

		applyRepositoryFeatures(repo);

		if (fm.findFragmentByTag(TAG_INFO) == null) {
			setupFragments();
			Intent intent = getIntent();
			if (intent.hasExtra("goToSection")) {
				handleLinkIntent();
			} else {
				updateDockUI(R.id.btn_nav_details);
			}
		} else {
			restoreState();
		}

		IssuesViewModel issuesVM = new ViewModelProvider(this).get(IssuesViewModel.class);
		issuesVM.prefetchCounts(this, repository.getOwner(), repository.getName());

		PullRequestsViewModel prVM = new ViewModelProvider(this).get(PullRequestsViewModel.class);
		prVM.prefetchPrCounts(this, repository.getOwner(), repository.getName());

		ReleasesViewModel releasesViewModel =
				new ViewModelProvider(this).get(ReleasesViewModel.class);
		releasesViewModel.prefetchCounts(this, repository.getOwner(), repository.getName());

		viewModel.loadRepositoryDetails(
				this, repository.getOwner(), repository.getName(), repository.getBranchRef());
	}

	private void setupFragments() {
		Fragment infoFrag = fm.findFragmentByTag(TAG_INFO);

		if (infoFrag == null) {
			infoFrag = RepoInfoFragment.newInstance(repository);
			Fragment filesFrag = FilesFragment.newInstance(repository);
			Fragment issuesFrag = IssuesFragment.newInstance(repository);
			Fragment prFrag = PullRequestsFragment.newInstance(repository);
			Fragment releaseFrag = ReleasesFragment.newInstance(repository);
			Fragment wikiFrag = WikiFragment.newInstance(repository);
			Fragment milestoneFrag = MilestonesFragment.newInstance(repository);
			Fragment labelFrag = LabelsFragment.newInstance(repository);
			Fragment collabFrag = CollaboratorsFragment.newInstance(repository);

			fm.beginTransaction()
					.add(R.id.repo_details_container, collabFrag, TAG_COLLAB)
					.hide(collabFrag)
					.add(R.id.repo_details_container, labelFrag, TAG_LABELS)
					.hide(labelFrag)
					.add(R.id.repo_details_container, milestoneFrag, TAG_MILESTONES)
					.hide(milestoneFrag)
					.add(R.id.repo_details_container, wikiFrag, TAG_WIKI)
					.hide(wikiFrag)
					.add(R.id.repo_details_container, releaseFrag, TAG_RELEASES)
					.hide(releaseFrag)
					.add(R.id.repo_details_container, prFrag, TAG_PRS)
					.hide(prFrag)
					.add(R.id.repo_details_container, issuesFrag, TAG_ISSUES)
					.hide(issuesFrag)
					.add(R.id.repo_details_container, filesFrag, TAG_FILES)
					.hide(filesFrag)
					.add(R.id.repo_details_container, infoFrag, TAG_INFO)
					.commitNow();
		} else {
			restoreState();
		}
	}

	private void restoreState() {
		for (int i = 0; i < fragmentTags.length; i++) {
			Fragment f = fm.findFragmentByTag(fragmentTags[i]);
			if (f != null && !f.isHidden()) {
				final int activeId = buttonIds[i];
				binding.dockScrollView.post(
						() -> {
							updateDockUI(activeId);
							centerDockIcon(findViewById(activeId));
						});
				break;
			}
		}
	}

	private void applyRepositoryFeatures(Repository repo) {
		binding.btnNavFiles.setVisibility(repo.isHasCode() ? View.VISIBLE : View.GONE);
		binding.btnNavIssues.setVisibility(repo.isHasIssues() ? View.VISIBLE : View.GONE);
		binding.btnNavPrs.setVisibility(repo.isHasPullRequests() ? View.VISIBLE : View.GONE);
		binding.btnNavReleases.setVisibility(repo.isHasReleases() ? View.VISIBLE : View.GONE);
		binding.btnNavWiki.setVisibility(repo.isHasWiki() ? View.VISIBLE : View.GONE);
		binding.btnNavMilestones.setVisibility(
				repo.isHasIssues() || repo.isHasPullRequests() ? View.VISIBLE : View.GONE);
		binding.btnNavLabels.setVisibility(
				repo.isHasIssues() || repo.isHasPullRequests() ? View.VISIBLE : View.GONE);
		binding.btnNavCollaborators.setVisibility(!repo.isInternal() ? View.VISIBLE : View.GONE);
		hasActions = repo.isHasActions();
	}

	private void setupDockListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		MaterialButton[] navButtons = {
			binding.btnNavDetails, binding.btnNavFiles, binding.btnNavIssues,
			binding.btnNavPrs, binding.btnNavReleases, binding.btnNavWiki,
			binding.btnNavMilestones, binding.btnNavLabels, binding.btnNavCollaborators
		};

		for (MaterialButton btn : navButtons) {
			prepareNavButton(btn);
		}

		binding.btnNavDetails.setOnClickListener(v -> switchTab(TAG_INFO, R.id.btn_nav_details));
		binding.btnNavFiles.setOnClickListener(v -> switchTab(TAG_FILES, R.id.btn_nav_files));
		binding.btnNavIssues.setOnClickListener(v -> switchTab(TAG_ISSUES, R.id.btn_nav_issues));
		binding.btnNavPrs.setOnClickListener(v -> switchTab(TAG_PRS, R.id.btn_nav_prs));
		binding.btnNavReleases.setOnClickListener(
				v -> switchTab(TAG_RELEASES, R.id.btn_nav_releases));
		binding.btnNavWiki.setOnClickListener(v -> switchTab(TAG_WIKI, R.id.btn_nav_wiki));
		binding.btnNavMilestones.setOnClickListener(
				v -> switchTab(TAG_MILESTONES, R.id.btn_nav_milestones));
		binding.btnNavLabels.setOnClickListener(v -> switchTab(TAG_LABELS, R.id.btn_nav_labels));
		binding.btnNavCollaborators.setOnClickListener(
				v -> switchTab(TAG_COLLAB, R.id.btn_nav_collaborators));

		binding.btnDockMenu.setOnClickListener(
				v -> {
					List<RepositoryMenuItemModel> items = new ArrayList<>();
					Fragment currentVisible = null;
					for (Fragment f : fm.getFragments()) {
						if (f != null && f.isAdded() && !f.isHidden()) {
							currentVisible = f;
							break;
						}
					}

					if (currentVisible instanceof RepoHubProvider provider) {
						items = provider.getRepoHubItems();
					}

					BottomsheetRepoMenu sheet =
							BottomsheetRepoMenu.newInstance(
									items,
									repository,
									isStarred,
									isWatched,
									isGiteaRepoActionsVisible,
									adminStatus);
					sheet.show(getSupportFragmentManager(), "repo_universal_hub");
				});

		LinearLayout.LayoutParams params =
				(LinearLayout.LayoutParams) binding.btnNavCollaborators.getLayoutParams();
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen12dp));
		binding.btnNavCollaborators.setLayoutParams(params);
	}

	public interface RepoHubProvider {
		List<RepositoryMenuItemModel> getRepoHubItems();

		default void onLocalSearchTriggered() {}

		void onHubActionSelected(String actionId);
	}

	@Override
	public void onMenuItemSelected(String actionId) {
		handleHubAction(actionId);
	}

	private void handleHubAction(String actionId) {
		switch (actionId) {
			case "CORE_STAR":
				viewModel.toggleStar(this, repository.getOwner(), repository.getName());
				break;
			case "CORE_WATCH":
				viewModel.toggleWatch(this, repository.getOwner(), repository.getName());
				break;
			case "CORE_COPY":
				AppUtil.copyToClipboard(
						this,
						repository.getRepository().getHtmlUrl(),
						getString(R.string.copied_to_clipboard));
				break;
			case "CORE_SHARE":
				AppUtil.sharingIntent(this, repository.getRepository().getHtmlUrl());
				break;
			case "CORE_BROWSER":
				AppUtil.openUrlInBrowser(this, repository.getRepository().getHtmlUrl());
				break;
			case "CORE_SETTINGS":
				settingsLauncher.launch(
						repository.getIntent(ctx, RepositorySettingsActivity.class));
				break;
			case "CORE_ACTIONS":
				startActivity(repository.getIntent(ctx, RepositoryActionsActivity.class));
				break;
		}

		for (Fragment f : fm.getFragments()) {
			if (f != null && f.isAdded() && !f.isHidden() && f instanceof RepoHubProvider) {
				((RepoHubProvider) f).onHubActionSelected(actionId);
				break;
			}
		}
	}

	public void switchTab(String targetTag, int btnId) {
		Fragment target = fm.findFragmentByTag(targetTag);

		if (target == null) return;

		Fragment currentVisible = null;
		List<Fragment> fragments = fm.getFragments();
		for (Fragment f : fragments) {
			if (f != null && f.isAdded() && !f.isHidden()) {
				currentVisible = f;
				break;
			}
		}

		if (currentVisible == target) return;

		FragmentTransaction ft =
				fm.beginTransaction()
						.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

		if (currentVisible != null) {
			ft.hide(currentVisible);
		}

		ft.show(target).commit();

		this.activeTabId = btnId;
		updateDockUI(btnId);
		centerDockIcon(findViewById(btnId));
	}

	private void updateDockUI(int activeBtnId) {
		int[] allButtons = {
			R.id.btn_nav_details, R.id.btn_nav_files, R.id.btn_nav_issues,
			R.id.btn_nav_prs, R.id.btn_nav_releases, R.id.btn_nav_wiki,
			R.id.btn_nav_milestones, R.id.btn_nav_labels, R.id.btn_nav_collaborators
		};

		for (int id : allButtons) {
			MaterialButton btn = findViewById(id);
			if (btn != null) {
				if (id == activeBtnId) activatePill(btn);
				else resetPill(btn);
			}
		}
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn) {
		btn.setSelected(true);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(255);
	}

	private void resetPill(MaterialButton btn) {
		btn.setSelected(false);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void centerDockIcon(View btn) {
		if (btn == null) return;
		binding.dockScrollView.post(
				() -> {
					int scrollX =
							(btn.getLeft() - (binding.dockScrollView.getWidth() / 2))
									+ (btn.getWidth() / 2);
					binding.dockScrollView.smoothScrollTo(scrollX, 0);
				});
	}

	private void handleLinkIntent() {
		Intent mainIntent = getIntent();
		String goToSection = mainIntent.getStringExtra("goToSection");
		String goToSectionType = mainIntent.getStringExtra("goToSectionType");

		if (goToSection != null) {
			mainIntent.removeExtra("goToSection");
			mainIntent.removeExtra("goToSectionType");

			switch (Objects.requireNonNull(goToSectionType)) {
				case "file":
					switchTab(TAG_FILES, R.id.btn_nav_files);
					String branch1 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch1);

					RepoGetContentsList file =
							(RepoGetContentsList) mainIntent.getSerializableExtra("file");
					if (file != null) {
						FilesFragment filesFragment =
								(FilesFragment)
										getSupportFragmentManager().findFragmentByTag(TAG_FILES);
						if (filesFragment != null) {
							filesFragment.openViewerLinkIntent(file);
						}
					}
					break;

				case "dir":
					switchTab(TAG_FILES, R.id.btn_nav_files);
					String branch2 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch2);
					break;

				case "commitsList":
					switchTab(TAG_FILES, R.id.btn_nav_files);
					String branch = mainIntent.getStringExtra("branchName");
					repository.setBranchRef(branch);

					Intent commitsIntent = repository.getIntent(this, CommitsActivity.class);
					startActivity(commitsIntent);
					break;

				case "commit":
					switchTab(TAG_INFO, R.id.btn_nav_details);
					String sha = mainIntent.getStringExtra("sha");

					Intent commitIntent = repository.getIntent(this, CommitDetailActivity.class);
					commitIntent.putExtra("sha", sha);
					commitIntent.putExtra("owner", repository.getOwner());
					commitIntent.putExtra("repo", repository.getName());
					startActivity(commitIntent);
					break;

				case "issue":
					switchTab(TAG_ISSUES, R.id.btn_nav_issues);
					break;

				case "issueNew":
					switchTab(TAG_ISSUES, R.id.btn_nav_issues);
					BottomSheetCreateIssue.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_ISSUE");
					break;

				case "pull":
					switchTab(TAG_PRS, R.id.btn_nav_prs);
					break;

				case "pullNew":
					switchTab(TAG_PRS, R.id.btn_nav_prs);
					BottomSheetCreatePullRequest.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_PULL_REQUEST");
					break;

				case "releases":
					switchTab(TAG_RELEASES, R.id.btn_nav_releases);
					break;

				case "newRelease":
					switchTab(TAG_RELEASES, R.id.btn_nav_releases);
					BottomSheetCreateRelease.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_RELEASE");
					break;

				case "wiki":
					switchTab(TAG_WIKI, R.id.btn_nav_wiki);
					break;

				case "wikiNew":
					switchTab(TAG_WIKI, R.id.btn_nav_wiki);
					BottomSheetCreateWiki.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_WIKI");
					break;

				case "milestones":
					switchTab(TAG_MILESTONES, R.id.btn_nav_milestones);
					break;

				case "milestonesNew":
					switchTab(TAG_MILESTONES, R.id.btn_nav_milestones);
					BottomSheetCreateMilestone.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_MILESTONE");
					break;

				case "labels":
					switchTab(TAG_LABELS, R.id.btn_nav_labels);
					break;

				case "settings":
					settingsLauncher.launch(
							repository.getIntent(this, RepositorySettingsActivity.class));
					break;
			}
		}
	}
}
