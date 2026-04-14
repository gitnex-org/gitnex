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
import org.mian.gitnex.databinding.ActivityRepoDetailBinding;
import org.mian.gitnex.fragments.BottomSheetCreateIssue;
import org.mian.gitnex.fragments.BottomSheetCreateMilestone;
import org.mian.gitnex.fragments.BottomSheetCreateRelease;
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

	private ActivityRepoDetailBinding binding;
	public RepositoryContext repository;
	private RepositoryDetailsViewModel viewModel;
	private final FragmentManager fm = getSupportFragmentManager();
	private Fragment infoFrag;
	public Fragment filesFrag;
	public Fragment issuesFrag;
	public Fragment prFrag;
	private Fragment releaseFrag;
	private Fragment wikiFrag;
	private Fragment milestoneFrag;
	private Fragment labelFrag;
	private Fragment collabFrag;
	private Fragment activeFragment;
	private boolean isStarred = false;
	private boolean isWatched = false;
	private boolean isGiteaRepoActionsVisible = false;
	private boolean adminStatus = false;
	private int activeTabId = R.id.btn_nav_details;
	private BadgeDrawable issuesBadge;
	private BadgeDrawable prBadge;
	private BadgeDrawable releaseBadge;

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

		setupProviderFlags();
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
	}

	private void setupProviderFlags() {
		String provider = getAccount().getAccount().getProvider();
		if (provider != null) {
			String serverVersion = getAccount().getAccount().getServerVersion();
			Version minVersion = new Version("1.24");
			Version currentVersion =
					Version.valid(serverVersion) ? new Version(serverVersion) : new Version("0.0");
			isGiteaRepoActionsVisible =
					"gitea".equals(provider) && !currentVersion.less(minVersion);
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
								if (fm.findFragmentByTag("info") == null) {
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

		if (fm.findFragmentByTag("info") == null) {
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
		infoFrag = fm.findFragmentByTag("info");
		filesFrag = fm.findFragmentByTag("files");
		issuesFrag = fm.findFragmentByTag("issues");
		prFrag = fm.findFragmentByTag("prs");
		releaseFrag = fm.findFragmentByTag("releases");
		wikiFrag = fm.findFragmentByTag("wiki");
		milestoneFrag = fm.findFragmentByTag("milestones");
		labelFrag = fm.findFragmentByTag("labels");
		collabFrag = fm.findFragmentByTag("collab");

		if (infoFrag == null) {
			infoFrag = RepoInfoFragment.newInstance(repository);
			filesFrag = FilesFragment.newInstance(repository);
			issuesFrag = IssuesFragment.newInstance(repository);
			prFrag = PullRequestsFragment.newInstance(repository);
			releaseFrag = ReleasesFragment.newInstance(repository);
			wikiFrag = WikiFragment.newInstance(repository);
			milestoneFrag = MilestonesFragment.newInstance(repository);
			labelFrag = LabelsFragment.newInstance(repository);
			collabFrag = CollaboratorsFragment.newInstance(repository);

			fm.beginTransaction()
					.add(R.id.repo_details_container, collabFrag, "collab")
					.hide(collabFrag)
					.add(R.id.repo_details_container, labelFrag, "labels")
					.hide(labelFrag)
					.add(R.id.repo_details_container, milestoneFrag, "milestones")
					.hide(milestoneFrag)
					.add(R.id.repo_details_container, wikiFrag, "wiki")
					.hide(wikiFrag)
					.add(R.id.repo_details_container, releaseFrag, "releases")
					.hide(releaseFrag)
					.add(R.id.repo_details_container, prFrag, "prs")
					.hide(prFrag)
					.add(R.id.repo_details_container, issuesFrag, "issues")
					.hide(issuesFrag)
					.add(R.id.repo_details_container, filesFrag, "files")
					.hide(filesFrag)
					.add(R.id.repo_details_container, infoFrag, "info")
					.commitNow();

			activeFragment = infoFrag;
		} else {
			restoreState();
		}
	}

	private void restoreState() {
		infoFrag = fm.findFragmentByTag("info");
		filesFrag = fm.findFragmentByTag("files");
		issuesFrag = fm.findFragmentByTag("issues");
		prFrag = fm.findFragmentByTag("prs");
		releaseFrag = fm.findFragmentByTag("releases");
		wikiFrag = fm.findFragmentByTag("wiki");
		milestoneFrag = fm.findFragmentByTag("milestones");
		labelFrag = fm.findFragmentByTag("labels");
		collabFrag = fm.findFragmentByTag("collab");

		String[] tags = {
			"info", "files", "issues", "prs", "releases", "wiki", "milestones", "labels", "collab"
		};
		int[] ids = {
			R.id.btn_nav_details,
			R.id.btn_nav_files,
			R.id.btn_nav_issues,
			R.id.btn_nav_prs,
			R.id.btn_nav_releases,
			R.id.btn_nav_wiki,
			R.id.btn_nav_milestones,
			R.id.btn_nav_labels,
			R.id.btn_nav_collaborators
		};

		for (int i = 0; i < tags.length; i++) {
			Fragment f = fm.findFragmentByTag(tags[i]);
			if (f != null && !f.isHidden()) {
				activeFragment = f;
				final int activeId = ids[i];
				binding.dockScrollView.post(
						() -> {
							updateDockUI(activeId);
							centerDockIcon(findViewById(activeId));
						});
				break;
			}
		}
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

		binding.btnNavDetails.setOnClickListener(v -> switchTab("info", R.id.btn_nav_details));
		binding.btnNavFiles.setOnClickListener(v -> switchTab("files", R.id.btn_nav_files));
		binding.btnNavIssues.setOnClickListener(v -> switchTab("issues", R.id.btn_nav_issues));
		binding.btnNavPrs.setOnClickListener(v -> switchTab("prs", R.id.btn_nav_prs));
		binding.btnNavReleases.setOnClickListener(
				v -> switchTab("releases", R.id.btn_nav_releases));
		binding.btnNavWiki.setOnClickListener(v -> switchTab("wiki", R.id.btn_nav_wiki));
		binding.btnNavMilestones.setOnClickListener(
				v -> switchTab("milestones", R.id.btn_nav_milestones));
		binding.btnNavLabels.setOnClickListener(v -> switchTab("labels", R.id.btn_nav_labels));
		binding.btnNavCollaborators.setOnClickListener(
				v -> switchTab("collab", R.id.btn_nav_collaborators));

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
				startActivity(repository.getIntent(ctx, RepositoryActions.class));
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
					switchTab("files", R.id.btn_nav_files);
					String branch1 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch1);

					Intent fileIntent = repository.getIntent(this, FileViewActivity.class);
					fileIntent.putExtra("file", mainIntent.getSerializableExtra("file"));
					startActivity(fileIntent);
					break;

				case "dir":
					switchTab("files", R.id.btn_nav_files);
					String branch2 = mainIntent.getStringExtra("branch");
					repository.setBranchRef(branch2);
					break;

				case "commitsList":
					switchTab("files", R.id.btn_nav_files);
					String branch = mainIntent.getStringExtra("branchName");
					repository.setBranchRef(branch);

					Intent commitsIntent = repository.getIntent(this, CommitsActivity.class);
					startActivity(commitsIntent);
					break;

				case "commit":
					switchTab("info", R.id.btn_nav_details);
					String sha = mainIntent.getStringExtra("sha");

					Intent commitIntent = repository.getIntent(this, CommitDetailActivity.class);
					commitIntent.putExtra("sha", sha);
					startActivity(commitIntent);
					break;

				case "issue":
					switchTab("issues", R.id.btn_nav_issues);
					break;

				case "issueNew":
					switchTab("issues", R.id.btn_nav_issues);
					BottomSheetCreateIssue.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_ISSUE");
					break;

				case "pull":
					switchTab("prs", R.id.btn_nav_prs);
					break;

				case "pullNew":
					switchTab("prs", R.id.btn_nav_prs);
					startActivity(repository.getIntent(this, CreatePullRequestActivity.class));
					break;

				case "releases":
					switchTab("releases", R.id.btn_nav_releases);
					break;

				case "newRelease":
					switchTab("releases", R.id.btn_nav_releases);
					BottomSheetCreateRelease.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_RELEASE");
					break;

				case "wiki":
					switchTab("wiki", R.id.btn_nav_wiki);
					break;

				case "wikiNew":
					switchTab("wiki", R.id.btn_nav_wiki);
					Intent intentWiki = repository.getIntent(this, WikiActivity.class);
					intentWiki.putExtra("action", "add");
					intentWiki.putExtra(RepositoryContext.INTENT_EXTRA, repository);
					startActivity(intentWiki);
					break;

				case "milestones":
					switchTab("milestones", R.id.btn_nav_milestones);
					break;

				case "milestonesNew":
					switchTab("milestones", R.id.btn_nav_milestones);
					BottomSheetCreateMilestone.newInstance(repository, null)
							.show(getSupportFragmentManager(), "CREATE_MILESTONE");
					break;

				case "labels":
					switchTab("labels", R.id.btn_nav_labels);
					break;

				case "settings":
					settingsLauncher.launch(
							repository.getIntent(this, RepositorySettingsActivity.class));
					break;
			}
		}
	}
}
