package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityOrgTeamDetailsBinding;
import org.mian.gitnex.databinding.BottomsheetOrgTeamDetailPermissionsBinding;
import org.mian.gitnex.fragments.OrganizationTeamDetailsMembersFragment;
import org.mian.gitnex.fragments.OrganizationTeamDetailsReposFragment;

/**
 * @author mmarif
 */
public class OrganizationTeamDetailsActivity extends BaseActivity {

	private ActivityOrgTeamDetailsBinding binding;
	private Team team;
	private String orgName;
	private String teamPermissions;
	private final FragmentManager fm = getSupportFragmentManager();
	private Fragment repoFrag, memberFrag;
	private Fragment activeFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityOrgTeamDetailsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		team = (Team) getIntent().getSerializableExtra("team");
		orgName = getIntent().getStringExtra("orgName");
		if (team != null && team.getPermission() != null) {
			teamPermissions = team.getPermission().getValue();
		} else {
			teamPermissions = "read";
		}

		setupDockListeners();
		setupFragments();

		activeFragment = null;
		switchTab(repoFrag, R.id.btn_nav_repos);
	}

	private void setupFragments() {
		repoFrag = OrganizationTeamDetailsReposFragment.newInstance(team, orgName);
		memberFrag = OrganizationTeamDetailsMembersFragment.newInstance(team);

		fm.beginTransaction()
				.add(R.id.org_teams_details_container, memberFrag, "members")
				.hide(memberFrag)
				.add(R.id.org_teams_details_container, repoFrag, "repos")
				.hide(repoFrag)
				.commitNow();
	}

	private void setupDockListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		prepareNavButton(binding.btnNavRepos);
		prepareNavButton(binding.btnNavMembers);
		prepareNavButton(binding.btnNavPermissions);

		binding.btnNavRepos.setOnClickListener(v -> switchTab(repoFrag, R.id.btn_nav_repos));
		binding.btnNavMembers.setOnClickListener(v -> switchTab(memberFrag, R.id.btn_nav_members));

		binding.btnNavPermissions.setOnClickListener(v -> showTeamPermissionSheet());

		binding.btnDockSearch.setOnClickListener(
				v -> {
					if (activeFragment instanceof OrgActionInterface) {
						((OrgActionInterface) activeFragment).onSearchTriggered();
					}
				});

		binding.btnDockAdd.setOnClickListener(
				v -> {
					if (activeFragment instanceof OrgActionInterface) {
						((OrgActionInterface) activeFragment).onAddRequested();
					}
				});
	}

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target || target == null) return;

		FragmentTransaction ft =
				fm.beginTransaction()
						.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

		if (activeFragment != null) {
			ft.hide(activeFragment);
		}

		ft.show(target).commit();

		activeFragment = target;
		updateDockActions(btnId);
	}

	private void updateDockActions(int activeBtnId) {
		activatePill(binding.btnNavRepos, activeBtnId == R.id.btn_nav_repos);
		activatePill(binding.btnNavMembers, activeBtnId == R.id.btn_nav_members);

		binding.dockContainer.removeView(binding.btnDockAdd);
		binding.dockContainer.removeView(binding.btnDockSearch);
		binding.dockContainer.removeView(binding.dockDivider);

		binding.dockContainer.addView(binding.dockDivider);
		binding.dockContainer.addView(binding.btnDockSearch);

		boolean canManage = false;
		if (team != null && team.getPermission() != null) {
			String perm = team.getPermission().getValue();
			canManage = "admin".equals(perm) || "owner".equals(perm) || "write".equals(perm);
		}

		if (canManage && activeFragment instanceof OrgActionInterface actionFrag) {
			if (actionFrag.canAdd()) {
				binding.dockContainer.addView(
						binding.btnDockAdd,
						binding.dockContainer.indexOfChild(binding.dockDivider) + 1);
				binding.btnDockAdd.setVisibility(View.VISIBLE);
			}
		}
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn, boolean active) {
		btn.setSelected(active);
		if (btn.getBackground() != null) {
			btn.getBackground().setAlpha(active ? 255 : 0);
		}
	}

	public interface OrgActionInterface {
		void onSearchTriggered();

		void onAddRequested();

		boolean canAdd();
	}

	private void showTeamPermissionSheet() {
		BottomsheetOrgTeamDetailPermissionsBinding sheetBinding =
				BottomsheetOrgTeamDetailPermissionsBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		dialog.setContentView(sheetBinding.getRoot());

		sheetBinding.sheetTitle.setText(team.getName());

		String perm = team.getPermission().getValue();
		int resId =
				switch (perm) {
					case "admin" -> R.string.teamPermissionAdmin;
					case "write" -> R.string.teamPermissionWrite;
					case "owner" -> R.string.teamPermissionOwner;
					case "none" -> R.string.teamPermissionNone;
					default -> R.string.teamPermissionRead;
				};

		sheetBinding.permissionText.setText(getString(resId));
		dialog.show();
	}
}
