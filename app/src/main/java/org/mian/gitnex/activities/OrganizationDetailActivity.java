package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.bottomsheets.GenericMenuBottomSheet;
import org.mian.gitnex.databinding.ActivityOrgDetailBinding;
import org.mian.gitnex.fragments.OrganizationInfoFragment;
import org.mian.gitnex.fragments.OrganizationLabelsFragment;
import org.mian.gitnex.fragments.OrganizationMembersFragment;
import org.mian.gitnex.fragments.OrganizationRepositoriesFragment;
import org.mian.gitnex.fragments.OrganizationTeamsFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BookmarkHelper;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.models.GenericMenuItemModel;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationDetailActivity extends BaseActivity {

	private ActivityOrgDetailBinding binding;
	private OrganizationsViewModel viewModel;
	private String orgName;

	private final FragmentManager fm = getSupportFragmentManager();
	private Fragment infoFrag, repoFrag, labelFrag, teamFrag, memberFrag;
	private Fragment activeFragment;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityOrgDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		orgName = getIntent().getStringExtra("orgName");
		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);

		setupDockListeners();
		observeViewModel();

		viewModel.loadOrganizationContext(this, orgName, getAccount().getAccount().getUserName());
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (!loading && activeFragment == null) {
								Boolean memberStatus = viewModel.getIsMember().getValue();
								boolean isMember = (memberStatus != null && memberStatus);
								setupFragments(isMember);
								initializeDefaultTab();
							}
						});

		viewModel
				.getIsMember()
				.observe(
						this,
						member -> {
							if (member == null || activeFragment == null) return;
							binding.btnNavTeams.setVisibility(member ? View.VISIBLE : View.GONE);
							if (member && teamFrag == null) {
								teamFrag = OrganizationTeamsFragment.newInstance(orgName);
								fm.beginTransaction()
										.add(R.id.user_profile_container, teamFrag, "teams")
										.hide(teamFrag)
										.commitNow();
							}
						});
	}

	private void setupFragments(boolean isMember) {
		infoFrag = OrganizationInfoFragment.newInstance(orgName);
		repoFrag = OrganizationRepositoriesFragment.newInstance(orgName);
		labelFrag = OrganizationLabelsFragment.newInstance(orgName);
		memberFrag = OrganizationMembersFragment.newInstance(orgName);

		FragmentTransaction ft = fm.beginTransaction();

		ft.add(R.id.user_profile_container, memberFrag, "members").hide(memberFrag);
		ft.add(R.id.user_profile_container, labelFrag, "labels").hide(labelFrag);
		ft.add(R.id.user_profile_container, repoFrag, "repos").hide(repoFrag);

		if (isMember) {
			teamFrag = OrganizationTeamsFragment.newInstance(orgName);
			ft.add(R.id.user_profile_container, teamFrag, "teams").hide(teamFrag);
		}

		binding.btnNavTeams.setVisibility(isMember ? View.VISIBLE : View.GONE);

		ft.add(R.id.user_profile_container, infoFrag, "info");
		ft.commitNow();

		activeFragment = infoFrag;
	}

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target || target == null) return;

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
		int[] navIds = {
			R.id.btn_nav_details,
			R.id.btn_nav_repos,
			R.id.btn_nav_labels,
			R.id.btn_nav_teams,
			R.id.btn_nav_members
		};

		for (int id : navIds) {
			MaterialButton btn = findViewById(id);
			if (btn != null) {
				if (id == activeBtnId) activatePill(btn);
				else resetPill(btn);
			}
		}

		boolean isInfo = activeBtnId == R.id.btn_nav_details;
		binding.btnDockSearch.setVisibility(isInfo ? View.GONE : View.VISIBLE);
		binding.dockDivider.setVisibility(isInfo ? View.GONE : View.VISIBLE);

		if (isInfo || activeBtnId == R.id.btn_nav_members) {
			binding.btnDockAdd.setVisibility(View.GONE);
		} else if (activeFragment instanceof OrgActionInterface) {
			binding.btnDockAdd.setVisibility(
					((OrgActionInterface) activeFragment).canAdd() ? View.VISIBLE : View.GONE);
		} else {
			binding.btnDockAdd.setVisibility(View.GONE);
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

	private void initializeDefaultTab() {
		updateDockUI(R.id.btn_nav_details);
		binding.dockScrollView.post(() -> centerDockIcon(binding.btnNavDetails));
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

	public interface OrgActionInterface {
		void onSearchTriggered();

		void onAddRequested();

		boolean canAdd();
	}

	private void setupDockListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		MaterialButton[] navButtons = {
			binding.btnNavDetails,
			binding.btnNavRepos,
			binding.btnNavLabels,
			binding.btnNavTeams,
			binding.btnNavMembers
		};

		for (MaterialButton btn : navButtons) {
			prepareNavButton(btn);
		}

		binding.btnDockSearch.setVisibility(View.GONE);
		binding.btnDockAdd.setVisibility(View.GONE);
		binding.dockDivider.setVisibility(View.GONE);

		binding.btnNavDetails.setOnClickListener(v -> switchTab(infoFrag, R.id.btn_nav_details));
		binding.btnNavRepos.setOnClickListener(v -> switchTab(repoFrag, R.id.btn_nav_repos));
		binding.btnNavLabels.setOnClickListener(v -> switchTab(labelFrag, R.id.btn_nav_labels));
		binding.btnNavTeams.setOnClickListener(v -> switchTab(teamFrag, R.id.btn_nav_teams));
		binding.btnNavMembers.setOnClickListener(v -> switchTab(memberFrag, R.id.btn_nav_members));

		binding.btnDockSearch.setOnClickListener(
				v -> {
					if (activeFragment instanceof OrgActionInterface)
						((OrgActionInterface) activeFragment).onSearchTriggered();
				});

		binding.btnDockAdd.setOnClickListener(
				v -> {
					if (activeFragment instanceof OrgActionInterface)
						((OrgActionInterface) activeFragment).onAddRequested();
				});

		binding.btnDockMenu.setOnClickListener(v -> showOrganizationBottomSheet());

		LinearLayout.LayoutParams params =
				(LinearLayout.LayoutParams) binding.btnNavMembers.getLayoutParams();
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen16dp));
		binding.btnNavMembers.setLayoutParams(params);
	}

	private void showOrganizationBottomSheet() {
		String url = UrlHelper.buildCurrentContextUrl(this, orgName, null);
		boolean isBookmarked = BookmarkHelper.isBookmarked(this, "org", orgName, null, null, null);

		List<GenericMenuItemModel> items = new ArrayList<>();

		items.add(
				new GenericMenuItemModel(
						"ORG_BOOKMARK",
						isBookmarked ? R.string.bookmark_remove : R.string.bookmark_add,
						isBookmarked ? R.drawable.ic_bookmark_remove : R.drawable.ic_bookmark_add,
						isBookmarked ? R.attr.colorErrorContainer : R.attr.colorPrimarySurface,
						isBookmarked
								? R.attr.colorOnErrorContainer
								: R.attr.colorOnPrimarySurface));

		items.add(
				new GenericMenuItemModel(
						"ORG_SHARE",
						R.string.share,
						R.drawable.ic_share,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		items.add(
				new GenericMenuItemModel(
						"ORG_COPY_URL",
						R.string.genericCopyUrl,
						R.drawable.ic_copy,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		items.add(
				new GenericMenuItemModel(
						"ORG_BROWSER",
						R.string.openInBrowser,
						R.drawable.ic_browser,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		GenericMenuBottomSheet sheet = GenericMenuBottomSheet.newInstance(orgName, null, items);

		sheet.setOnMenuItemClickListener(
				id -> {
					switch (id) {
						case "ORG_BOOKMARK":
							BookmarkHelper.toggleBookmark(
									this, "org", orgName, null, null, null, null, orgName, url);
							break;
						case "ORG_SHARE":
							AppUtil.sharingIntent(this, url);
							break;
						case "ORG_COPY_URL":
							AppUtil.copyToClipboard(this, url, getString(R.string.genericCopyUrl));
							break;
						case "ORG_BROWSER":
							AppUtil.openUrlInBrowser(this, url);
							break;
					}
				});

		sheet.show(getSupportFragmentManager(), "ORG_MENU");
	}
}
