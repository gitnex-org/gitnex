package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Calendar;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AccountSettingsActivity;
import org.mian.gitnex.activities.ActivitiesActivity;
import org.mian.gitnex.activities.AdministrationActivity;
import org.mian.gitnex.activities.AppSettingsActivity;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.MostVisitedReposActivity;
import org.mian.gitnex.activities.MyIssuesActivity;
import org.mian.gitnex.activities.MyReposActivity;
import org.mian.gitnex.activities.NotesActivity;
import org.mian.gitnex.activities.OrganizationsActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.StarredReposActivity;
import org.mian.gitnex.activities.WatchedReposActivity;
import org.mian.gitnex.adapters.UserAccountsAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.BottomsheetUserAccountsBinding;
import org.mian.gitnex.databinding.FragmentHomeDashboardBinding;
import org.mian.gitnex.databinding.ItemDashboardCardFullBinding;
import org.mian.gitnex.databinding.ItemDashboardCardLargeBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.UrlHelper;

/**
 * @author mmarif
 */
public class HomeDashboardFragment extends Fragment {

	private FragmentHomeDashboardBinding binding;
	private String username;
	private TinyDB tinyDB;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
		tinyDB = TinyDB.getInstance(requireContext());

		setupDashboardCards();
		initClickListeners();
		loadData();
		loadActiveServerIcon();

		return binding.getRoot();
	}

	private void loadData() {
		if (requireActivity() instanceof MainActivity mainActivity) {
			mainActivity.loadUserInfo(
					this,
					binding,
					null,
					new MainActivity.UserInfoCallback() {
						@Override
						public void onUserInfoLoaded(
								String username,
								boolean isAdmin,
								String serverVersion,
								long followers,
								long following) {
							if (!isAdded()) return;
							HomeDashboardFragment.this.username = username;
							binding.userFollowers.setText(String.valueOf(followers));
							binding.userFollowing.setText(String.valueOf(following));
							updateAdminVisibility(isAdmin);
						}

						@Override
						public void onUserAccountsLoaded() {}
					});
		}
	}

	private void loadActiveServerIcon() {
		int activeAccountId = tinyDB.getInt("currentActiveAccountId", -1);
		if (activeAccountId != -1) {
			UserAccountsApi accountsApi =
					BaseApi.getInstance(requireContext(), UserAccountsApi.class);
			if (accountsApi != null) {
				UserAccount activeAccount = accountsApi.getAccountById(activeAccountId);
				if (activeAccount != null) {
					Glide.with(this)
							.load(
									UrlHelper.appendPath(
											activeAccount.getInstanceUrl(),
											"assets/img/favicon.png"))
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.loader_animated)
							.error(R.drawable.ic_server)
							.centerCrop()
							.into(binding.serverIcon);
				}
			}
		}
	}

	private void showAccountsBottomSheet() {

		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		BottomsheetUserAccountsBinding sheetBinding =
				BottomsheetUserAccountsBinding.inflate(getLayoutInflater());
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		View bottomSheet =
				bottomSheetDialog.findViewById(
						com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheet != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
			behavior.setFitToContents(true);
			behavior.setSkipCollapsed(true);
			behavior.setExpandedOffset(0);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
		}

		UserAccountsAdapter adapter = new UserAccountsAdapter(requireContext(), bottomSheetDialog);
		sheetBinding.accountsList.setLayoutManager(new LinearLayoutManager(requireContext()));
		sheetBinding.accountsList.setAdapter(adapter);

		sheetBinding.newAccount.setOnClickListener(
				v -> {
					Intent intent = new Intent(requireContext(), LoginActivity.class);
					intent.putExtra("mode", "new_account");
					startActivity(intent);
					bottomSheetDialog.dismiss();
				});

		bottomSheetDialog.show();
	}

	private void initClickListeners() {

		binding.serverIconCard.setOnClickListener(v -> showAccountsBottomSheet());

		binding.refreshButtonCard.setOnClickListener(v -> performRefresh());

		binding.settingsCard.setOnClickListener(
				v -> {
					Intent intent = new Intent(requireContext(), AppSettingsActivity.class);
					startActivity(intent);
				});
		binding.repoStarredCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), StarredReposActivity.class);
							startActivity(intent);
						});
		binding.repoWatchedCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), WatchedReposActivity.class);
							startActivity(intent);
						});
		binding.myReposCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent = new Intent(requireContext(), MyReposActivity.class);
							startActivity(intent);
						});
		binding.myIssuesCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent = new Intent(requireContext(), MyIssuesActivity.class);
							startActivity(intent);
						});
		binding.organizationsCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), OrganizationsActivity.class);
							startActivity(intent);
						});
		binding.activitiesCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent = new Intent(requireContext(), ActivitiesActivity.class);
							startActivity(intent);
						});
		binding.mostVisitedReposCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), MostVisitedReposActivity.class);
							startActivity(intent);
						});
		binding.notesCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent = new Intent(requireContext(), NotesActivity.class);
							startActivity(intent);
						});
		binding.accountSettingsCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), AccountSettingsActivity.class);
							startActivity(intent);
						});
		binding.instanceAdministrationCard
				.getRoot()
				.setOnClickListener(
						v -> {
							Intent intent =
									new Intent(requireContext(), AdministrationActivity.class);
							startActivity(intent);
						});

		binding.userAvatar.setOnClickListener(
				v -> {
					if (username != null) {
						Intent intentProfile = new Intent(requireContext(), ProfileActivity.class);
						intentProfile.putExtra("username", username);
						startActivity(intentProfile);
					}
				});
	}

	private void navigateTo(int destinationId) {
		try {
			NavController navController =
					Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
			navController.navigate(destinationId);
		} catch (Exception ignored) {
		}
	}

	private void setupDashboardCards() {

		Calendar c = Calendar.getInstance();
		int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
		int greetingRes =
				(timeOfDay < 12)
						? R.string.good_morning
						: (timeOfDay < 16) ? R.string.good_afternoon : R.string.good_evening;
		binding.greetingText.setText(getString(greetingRes));

		binding.userFollowers.setText("-");
		binding.userFollowing.setText("-");

		updateLargeCard(
				binding.repoStarredCard, R.string.navStarredRepos, null, R.drawable.ic_star);
		updateLargeCard(
				binding.repoWatchedCard,
				R.string.navWatchedRepositories,
				null,
				R.drawable.ic_watchers);
		updateLargeCard(binding.myReposCard, R.string.navMyRepos, null, R.drawable.ic_repo);
		updateLargeCard(binding.myIssuesCard, R.string.navMyIssues, null, R.drawable.ic_issue);
		updateLargeCard(
				binding.organizationsCard, R.string.navOrg, null, R.drawable.ic_organization);
		updateLargeCard(
				binding.activitiesCard, R.string.activities, null, R.drawable.ic_activities);
		updateLargeCard(
				binding.accountSettingsCard,
				R.string.navAccount,
				null,
				R.drawable.ic_account_settings);
		updateLargeCard(
				binding.instanceAdministrationCard,
				R.string.navAdministration,
				null,
				R.drawable.ic_tool);

		updateFullCard(
				binding.mostVisitedReposCard,
				R.string.navMostVisited,
				getString(R.string.dashboard_most_visited_repos_sub_title),
				R.drawable.ic_trending);
		updateFullCard(
				binding.notesCard,
				R.string.navNotes,
				getString(R.string.dashboard_notes_sub_title),
				R.drawable.ic_notes);
	}

	private void updateAdminVisibility(boolean isAdmin) {
		binding.instanceAdministrationCard
				.getRoot()
				.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
	}

	private void performRefresh() {
		binding.userAvatar.setImageResource(R.drawable.loader_animated);
		binding.serverIcon.setImageResource(R.drawable.loader_animated);
		binding.userFullname.setText("");
		binding.userEmail.setText("");
		binding.userFollowers.setText("-");
		binding.userFollowing.setText("-");

		loadData();
		loadActiveServerIcon();
	}

	private void updateLargeCard(
			ItemDashboardCardLargeBinding cardBinding, int titleRes, String subtext, int iconRes) {
		cardBinding.tileTitle.setText(titleRes);
		cardBinding.tileIcon.setImageResource(iconRes);
		cardBinding.tileSubtitle.setVisibility(
				(subtext != null && !subtext.isEmpty()) ? View.VISIBLE : View.GONE);
		if (subtext != null) cardBinding.tileSubtitle.setText(subtext);
	}

	private void updateFullCard(
			ItemDashboardCardFullBinding cardBinding, int titleRes, String subtext, int iconRes) {
		cardBinding.cardTitle.setText(titleRes);
		cardBinding.cardIcon.setImageResource(iconRes);
		cardBinding.cardSubtext.setVisibility(
				(subtext != null && !subtext.isEmpty()) ? View.VISIBLE : View.GONE);
		if (subtext != null) cardBinding.cardSubtext.setText(subtext);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
