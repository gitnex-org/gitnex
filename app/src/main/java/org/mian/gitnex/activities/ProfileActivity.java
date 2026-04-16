package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityProfileBinding;
import org.mian.gitnex.fragments.profile.DetailFragment;
import org.mian.gitnex.fragments.profile.FollowersFragment;
import org.mian.gitnex.fragments.profile.FollowingFragment;
import org.mian.gitnex.fragments.profile.OrganizationsFragment;
import org.mian.gitnex.fragments.profile.RepositoriesFragment;
import org.mian.gitnex.fragments.profile.StarredRepositoriesFragment;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.UserProfileViewModel;

/**
 * @author mmarif
 */
public class ProfileActivity extends BaseActivity {

	private static final String STATE_ACTIVE_TAB = "active_tab";
	private static final String TAB_DETAILS = "details";
	private static final String TAB_REPOS = "repos";
	private static final String TAB_STARRED = "starred";
	private static final String TAB_ORGS = "orgs";
	private static final String TAB_FOLLOWERS = "followers";
	private static final String TAB_FOLLOWING = "following";

	private ActivityProfileBinding binding;
	private UserProfileViewModel viewModel;
	private String username;

	private final FragmentManager fm = getSupportFragmentManager();
	private Fragment detailFrag, repoFrag, starredFrag, orgFrag, followersFrag, followingFrag;
	private Fragment activeFragment;
	private String currentActiveTab = TAB_DETAILS;

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_ACTIVE_TAB, currentActiveTab);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityProfileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		username = getIntent().getStringExtra("username");
		if (username == null || username.isEmpty()) {
			Toasty.show(this, getString(R.string.userInvalidUserName));
			finish();
			return;
		}

		viewModel = new ViewModelProvider(this).get(UserProfileViewModel.class);

		if (savedInstanceState != null) {
			currentActiveTab = savedInstanceState.getString(STATE_ACTIVE_TAB, TAB_DETAILS);
		}

		setupFragments();
		setupDockListeners();
		observeViewModel();

		if (savedInstanceState != null) {
			restoreFromSavedTab();
		} else {
			initializeDefaultTab();
		}

		if (!username.equals(getAccount().getAccount().getUserName())) {
			viewModel.checkFollowStatus(this, username);
		}
	}

	private void observeViewModel() {
		viewModel
				.getErrorMessage()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								viewModel.clearMessages();
							}
						});

		viewModel.getIsFollowing().observe(this, following -> {});
	}

	private void initializeDefaultTab() {
		updateDockUI(R.id.btn_nav_details);
		binding.dockScrollView.post(() -> centerDockIcon(binding.btnNavDetails));
	}

	private void restoreFromSavedTab() {
		Fragment targetFragment;
		int activeBtnId =
				switch (currentActiveTab) {
					case TAB_REPOS -> {
						targetFragment = repoFrag;
						yield R.id.btn_nav_repos;
					}
					case TAB_STARRED -> {
						targetFragment = starredFrag;
						yield R.id.btn_nav_starred_repos;
					}
					case TAB_ORGS -> {
						targetFragment = orgFrag;
						yield R.id.btn_nav_organizations;
					}
					case TAB_FOLLOWERS -> {
						targetFragment = followersFrag;
						yield R.id.btn_nav_followers;
					}
					case TAB_FOLLOWING -> {
						targetFragment = followingFrag;
						yield R.id.btn_nav_following;
					}
					default -> {
						targetFragment = detailFrag;
						yield R.id.btn_nav_details;
					}
				};

		fm.beginTransaction()
				.hide(detailFrag)
				.hide(repoFrag)
				.hide(starredFrag)
				.hide(orgFrag)
				.hide(followersFrag)
				.hide(followingFrag)
				.show(targetFragment)
				.commitNow();

		activeFragment = targetFragment;
		updateDockUI(activeBtnId);
		centerDockIcon(findViewById(activeBtnId));
	}

	private void setupFragments() {

		detailFrag = DetailFragment.newInstance(username);
		repoFrag = RepositoriesFragment.newInstance(username);
		starredFrag = StarredRepositoriesFragment.newInstance(username);
		orgFrag = OrganizationsFragment.newInstance(username);
		followersFrag = FollowersFragment.newInstance(username);
		followingFrag = FollowingFragment.newInstance(username);

		fm.beginTransaction()
				.add(R.id.user_profile_container, followingFrag, "following")
				.hide(followingFrag)
				.add(R.id.user_profile_container, followersFrag, "followers")
				.hide(followersFrag)
				.add(R.id.user_profile_container, orgFrag, "orgs")
				.hide(orgFrag)
				.add(R.id.user_profile_container, starredFrag, "stars")
				.hide(starredFrag)
				.add(R.id.user_profile_container, repoFrag, "repos")
				.hide(repoFrag)
				.add(R.id.user_profile_container, detailFrag, "detail")
				.commitNow();

		activeFragment = detailFrag;
	}

	private void setupDockListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		MaterialButton[] navButtons = {
			binding.btnNavDetails, binding.btnNavRepos, binding.btnNavStarredRepos,
			binding.btnNavOrganizations, binding.btnNavFollowers, binding.btnNavFollowing
		};

		for (MaterialButton btn : navButtons) {
			prepareNavButton(btn);
		}

		binding.btnNavDetails.setOnClickListener(v -> switchTab(detailFrag, R.id.btn_nav_details));
		binding.btnNavRepos.setOnClickListener(v -> switchTab(repoFrag, R.id.btn_nav_repos));
		binding.btnNavStarredRepos.setOnClickListener(
				v -> switchTab(starredFrag, R.id.btn_nav_starred_repos));
		binding.btnNavOrganizations.setOnClickListener(
				v -> switchTab(orgFrag, R.id.btn_nav_organizations));
		binding.btnNavFollowers.setOnClickListener(
				v -> switchTab(followersFrag, R.id.btn_nav_followers));
		binding.btnNavFollowing.setOnClickListener(
				v -> switchTab(followingFrag, R.id.btn_nav_following));

		binding.btnDockSearch.setOnClickListener(
				v -> {
					if (activeFragment instanceof ProfileActionInterface) {
						((ProfileActionInterface) activeFragment).onSearchTriggered();
					}
				});

		LinearLayout.LayoutParams params =
				(LinearLayout.LayoutParams) binding.btnNavFollowing.getLayoutParams();
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen16dp));
		binding.btnNavFollowing.setLayoutParams(params);
	}

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;

		if (target == detailFrag) {
			currentActiveTab = TAB_DETAILS;
		} else if (target == repoFrag) {
			currentActiveTab = TAB_REPOS;
		} else if (target == starredFrag) {
			currentActiveTab = TAB_STARRED;
		} else if (target == orgFrag) {
			currentActiveTab = TAB_ORGS;
		} else if (target == followersFrag) {
			currentActiveTab = TAB_FOLLOWERS;
		} else if (target == followingFrag) {
			currentActiveTab = TAB_FOLLOWING;
		}

		updateDockUI(btnId);
		centerDockIcon(findViewById(btnId));
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

	private void updateDockUI(int activeBtnId) {
		int[] allButtons = {
			R.id.btn_nav_details,
			R.id.btn_nav_repos,
			R.id.btn_nav_starred_repos,
			R.id.btn_nav_organizations,
			R.id.btn_nav_followers,
			R.id.btn_nav_following
		};

		for (int id : allButtons) {
			MaterialButton btn = findViewById(id);
			if (btn != null) {
				if (id == activeBtnId) activatePill(btn);
				else resetPill(btn);
			}
		}

		if (activeBtnId == R.id.btn_nav_details) {
			binding.btnDockSearch.setVisibility(View.GONE);
			binding.dockDivider.setVisibility(View.GONE);
		} else {
			binding.btnDockSearch.setVisibility(View.VISIBLE);
			binding.dockDivider.setVisibility(View.VISIBLE);
		}
	}

	public void switchTabFromFragment(int position) {
		int[] btnIds = {
			R.id.btn_nav_details, R.id.btn_nav_repos, R.id.btn_nav_starred_repos,
			R.id.btn_nav_organizations, R.id.btn_nav_followers, R.id.btn_nav_following
		};

		Fragment[] fragments = {
			detailFrag, repoFrag, starredFrag, orgFrag, followersFrag, followingFrag
		};

		if (position >= 0 && position < fragments.length) {
			switchTab(fragments[position], btnIds[position]);
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

	public interface ProfileActionInterface {
		void onSearchTriggered();
	}
}
