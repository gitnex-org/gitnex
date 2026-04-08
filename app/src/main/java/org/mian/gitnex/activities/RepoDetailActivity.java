package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityRepoDetailBinding;
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
import org.mian.gitnex.helpers.RepositoryMenuItemModel;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
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
		} else {
			viewModel.fetchRepository(this, repository.getOwner(), repository.getName());
		}

		viewModel.checkRepoStatus(this, repository.getOwner(), repository.getName());

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
			initializeDefaultTab();
		}

		viewModel.loadRepositoryDetails(
				this, repository.getOwner(), repository.getName(), repository.getBranchRef());
	}

	private void initializeDefaultTab() {
		updateDockUI(R.id.btn_nav_details);
		binding.dockScrollView.post(() -> centerDockIcon(binding.btnNavDetails));
	}

	private void setupFragments() {
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

		binding.btnNavDetails.setOnClickListener(v -> switchTab(infoFrag, R.id.btn_nav_details));
		binding.btnNavFiles.setOnClickListener(v -> switchTab(filesFrag, R.id.btn_nav_files));
		binding.btnNavIssues.setOnClickListener(v -> switchTab(issuesFrag, R.id.btn_nav_issues));
		binding.btnNavPrs.setOnClickListener(v -> switchTab(prFrag, R.id.btn_nav_prs));
		binding.btnNavReleases.setOnClickListener(
				v -> switchTab(releaseFrag, R.id.btn_nav_releases));
		binding.btnNavWiki.setOnClickListener(v -> switchTab(wikiFrag, R.id.btn_nav_wiki));
		binding.btnNavMilestones.setOnClickListener(
				v -> switchTab(milestoneFrag, R.id.btn_nav_milestones));
		binding.btnNavLabels.setOnClickListener(v -> switchTab(labelFrag, R.id.btn_nav_labels));
		binding.btnNavCollaborators.setOnClickListener(
				v -> switchTab(collabFrag, R.id.btn_nav_collaborators));

		binding.btnDockMenu.setOnClickListener(
				v -> {
					List<RepositoryMenuItemModel> items = new ArrayList<>();
					if (activeFragment instanceof RepoHubProvider provider) {
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
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen16dp));
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

		if (activeFragment instanceof RepoHubProvider) {
			((RepoHubProvider) activeFragment).onHubActionSelected(actionId);
		}
	}

	public void switchTab(Fragment target, int btnId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
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
}
