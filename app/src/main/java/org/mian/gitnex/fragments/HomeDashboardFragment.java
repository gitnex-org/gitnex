package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
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
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.HomeDashboardAdapter;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentHomeDashboardBinding;

/**
 * @author mmarif
 */
public class HomeDashboardFragment extends Fragment {

	private FragmentHomeDashboardBinding binding;
	private String username;
	private List<UserAccount> userAccountsList;
	private UserAccountsNavAdapter accountsAdapter;
	private HomeDashboardAdapter dashboardAdapter;

	@SuppressLint("NotifyDataSetChanged")
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
		userAccountsList = new ArrayList<>();
		accountsAdapter = new UserAccountsNavAdapter(requireContext(), userAccountsList);
		dashboardAdapter = new HomeDashboardAdapter(requireContext());

		binding.mainScreensRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.mainScreensRecyclerView.setAdapter(dashboardAdapter);

		binding.userAccountsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.userAccountsRecyclerView.setAdapter(accountsAdapter);

		NavController navController =
				Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
		binding.repoOrgCard.setOnClickListener(
				v -> navController.navigate(R.id.action_to_organizations));
		binding.repoMyReposCard.setOnClickListener(
				v -> navController.navigate(R.id.action_to_myRepositories));
		binding.repoStarredCard.setOnClickListener(
				v -> navController.navigate(R.id.action_to_starredRepositories));
		binding.repoWatchedCard.setOnClickListener(
				v -> navController.navigate(R.id.action_to_watchedRepositories));
		binding.repoActivitiesCard.setOnClickListener(
				v -> navController.navigate(R.id.activitiesFragment));
		binding.repoMyIssuesCard.setOnClickListener(
				v -> navController.navigate(R.id.action_to_myIssues));

		// Load user info from MainActivity
		if (requireActivity() instanceof MainActivity) {
			((MainActivity) requireActivity())
					.loadUserInfo(
							this,
							binding,
							dashboardAdapter,
							userAccountsList,
							accountsAdapter,
							new MainActivity.UserInfoCallback() {
								@Override
								public void onUserInfoLoaded(
										String username, boolean isAdmin, String serverVersion) {
									HomeDashboardFragment.this.username = username;
								}

								@Override
								public void onUserAccountsLoaded() {}
							});
		}

		binding.userAvatar.setOnClickListener(
				v -> {
					if (username != null) {
						Intent intentProfile = new Intent(requireContext(), ProfileActivity.class);
						intentProfile.putExtra("username", username);
						startActivity(intentProfile);
					}
				});

		binding.refreshButton.setOnClickListener(
				v -> {
					binding.userAvatar.setImageResource(R.drawable.loader_animated);
					binding.userFullname.setText("");
					binding.userEmail.setText("");
					userAccountsList.clear();
					accountsAdapter.notifyDataSetChanged();

					// Call MainActivity methods
					if (requireActivity() instanceof MainActivity mainActivity) {
						mainActivity.getNotificationsCount();
						mainActivity.giteaVersion();
						mainActivity.serverPageLimitSettings();
						mainActivity.updateGeneralAttachmentSettings();
						mainActivity.loadUserInfo(
								this,
								binding,
								dashboardAdapter,
								userAccountsList,
								accountsAdapter,
								new MainActivity.UserInfoCallback() {
									@Override
									public void onUserInfoLoaded(
											String username,
											boolean isAdmin,
											String serverVersion) {
										HomeDashboardFragment.this.username = username;
									}

									@Override
									public void onUserAccountsLoaded() {}
								});
					}
				});

		return binding.getRoot();
	}
}
