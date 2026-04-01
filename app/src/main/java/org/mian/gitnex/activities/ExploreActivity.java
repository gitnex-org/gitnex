package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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

	private ActivityExploreBinding binding;
	private final FragmentManager fm = getSupportFragmentManager();
	private final Fragment repoFrag = new ExploreRepositoriesFragment();
	private final Fragment issueFrag = new ExploreIssuesFragment();
	private final Fragment orgFrag = new ExplorePublicOrganizationsFragment();
	private final Fragment userFrag = new ExploreUsersFragment();
	private Fragment activeFragment = repoFrag;
	private View detachedDivider;
	private MaterialButton detachedSearchBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityExploreBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		detachedDivider = binding.dockDivider;
		detachedSearchBtn = binding.btnDockSearch;

		setupFragments();
		setupDockListeners();

		updateDockUI(R.id.btn_nav_repos);
	}

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.explore_container, userFrag, "users")
				.hide(userFrag)
				.add(R.id.explore_container, orgFrag, "orgs")
				.hide(orgFrag)
				.add(R.id.explore_container, issueFrag, "issues")
				.hide(issueFrag)
				.add(R.id.explore_container, repoFrag, "repos")
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

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
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
