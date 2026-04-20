package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.MaterialButton;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityExploreBinding;
import org.mian.gitnex.fragments.ExploreIssuesFragment;
import org.mian.gitnex.fragments.ExplorePublicOrganizationsFragment;
import org.mian.gitnex.fragments.ExploreRepositoriesFragment;
import org.mian.gitnex.fragments.ExploreUsersFragment;
import org.mian.gitnex.helpers.UIHelper;

/**
 * @author mmarif
 */
public class ExploreActivity extends BaseActivity {

	private static final String STATE_ACTIVE_TAB = "active_tab";
	private static final String TAB_REPOS = "repos";
	private static final String TAB_ISSUES = "issues";
	private static final String TAB_ORGS = "orgs";
	private static final String TAB_USERS = "users";

	private ActivityExploreBinding binding;
	private final FragmentManager fm = getSupportFragmentManager();
	private final Fragment repoFrag = new ExploreRepositoriesFragment();
	private final Fragment issueFrag = new ExploreIssuesFragment();
	private final Fragment orgFrag = new ExplorePublicOrganizationsFragment();
	private final Fragment userFrag = new ExploreUsersFragment();
	private Fragment activeFragment = repoFrag;
	private String currentActiveTab = TAB_REPOS;
	private View detachedDivider;
	private MaterialButton detachedSearchBtn;

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_ACTIVE_TAB, currentActiveTab);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityExploreBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		detachedDivider = binding.dockDivider;
		detachedSearchBtn = binding.btnDockSearch;

		if (savedInstanceState != null) {
			currentActiveTab = savedInstanceState.getString(STATE_ACTIVE_TAB, TAB_REPOS);
		}

		setupFragments();
		setupDockListeners();

		if (savedInstanceState != null) {
			restoreFromSavedTab();
		} else {
			updateDockUI(R.id.btn_nav_repos);
		}
	}

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.explore_container, userFrag, TAB_USERS)
				.hide(userFrag)
				.add(R.id.explore_container, orgFrag, TAB_ORGS)
				.hide(orgFrag)
				.add(R.id.explore_container, issueFrag, TAB_ISSUES)
				.hide(issueFrag)
				.add(R.id.explore_container, repoFrag, TAB_REPOS)
				.show(repoFrag)
				.commitNow();
	}

	private void setupDockListeners() {
		prepareNavButton(binding.btnNavRepos);
		prepareNavButton(binding.btnNavIssues);
		prepareNavButton(binding.btnNavOrganizations);
		prepareNavButton(binding.btnNavUsers);
		prepareNavButton(binding.btnBack);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnNavRepos.setOnClickListener(v -> switchTab(repoFrag, R.id.btn_nav_repos));
		binding.btnNavIssues.setOnClickListener(v -> switchTab(issueFrag, R.id.btn_nav_issues));
		binding.btnNavOrganizations.setOnClickListener(
				v -> switchTab(orgFrag, R.id.btn_nav_organizations));
		binding.btnNavUsers.setOnClickListener(v -> switchTab(userFrag, R.id.btn_nav_users));

		detachedSearchBtn.setOnClickListener(
				v -> {
					if (activeFragment instanceof ExploreActionInterface) {
						((ExploreActionInterface) activeFragment).onSearchTriggered();
					}
				});
	}

	private void restoreFromSavedTab() {
		switch (currentActiveTab) {
			case TAB_ISSUES:
				fm.beginTransaction()
						.hide(repoFrag)
						.hide(orgFrag)
						.hide(userFrag)
						.show(issueFrag)
						.commitNow();
				activeFragment = issueFrag;
				updateDockUI(R.id.btn_nav_issues);
				break;
			case TAB_ORGS:
				fm.beginTransaction()
						.hide(repoFrag)
						.hide(issueFrag)
						.hide(userFrag)
						.show(orgFrag)
						.commitNow();
				activeFragment = orgFrag;
				updateDockUI(R.id.btn_nav_organizations);
				break;
			case TAB_USERS:
				fm.beginTransaction()
						.hide(repoFrag)
						.hide(issueFrag)
						.hide(orgFrag)
						.show(userFrag)
						.commitNow();
				activeFragment = userFrag;
				updateDockUI(R.id.btn_nav_users);
				break;
			case TAB_REPOS:
			default:
				activeFragment = repoFrag;
				updateDockUI(R.id.btn_nav_repos);
				break;
		}
	}

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;

		if (target == repoFrag) {
			currentActiveTab = TAB_REPOS;
		} else if (target == issueFrag) {
			currentActiveTab = TAB_ISSUES;
		} else if (target == orgFrag) {
			currentActiveTab = TAB_ORGS;
		} else if (target == userFrag) {
			currentActiveTab = TAB_USERS;
		}

		updateDockUI(btnId);
	}

	private void updateDockUI(int activeBtnId) {
		resetPill(binding.btnNavRepos);
		resetPill(binding.btnNavIssues);
		resetPill(binding.btnNavOrganizations);
		resetPill(binding.btnNavUsers);

		if (activeBtnId == R.id.btn_nav_repos) activatePill(binding.btnNavRepos);
		else if (activeBtnId == R.id.btn_nav_issues) activatePill(binding.btnNavIssues);
		else if (activeBtnId == R.id.btn_nav_organizations)
			activatePill(binding.btnNavOrganizations);
		else if (activeBtnId == R.id.btn_nav_users) activatePill(binding.btnNavUsers);

		updateContextualDockActions();
	}

	private void updateContextualDockActions() {
		ViewGroup parent = binding.dockedToolbarChild;

		parent.removeView(detachedDivider);
		parent.removeView(detachedSearchBtn);

		LinearLayout.LayoutParams dividerParams =
				(LinearLayout.LayoutParams) detachedDivider.getLayoutParams();

		if (activeFragment instanceof ExploreUsersFragment) {
			dividerParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dimen16dp));
		} else {
			dividerParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dimen4dp));
		}
		detachedDivider.setLayoutParams(dividerParams);

		boolean hasSearch = !(activeFragment instanceof ExplorePublicOrganizationsFragment);

		if (hasSearch) {
			parent.addView(detachedDivider);
			detachedDivider.setVisibility(View.VISIBLE);
		}

		if (hasSearch) {
			parent.addView(detachedSearchBtn);
			detachedSearchBtn.setVisibility(View.VISIBLE);
		}

		binding.dockedToolbar.requestLayout();
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

	public interface ExploreActionInterface {
		void onSearchTriggered();
	}
}
