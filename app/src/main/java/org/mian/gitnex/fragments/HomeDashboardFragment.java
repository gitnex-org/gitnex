package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.HomeDashboardAdapter;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentHomeDashboardBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class HomeDashboardFragment extends Fragment {

	private FragmentHomeDashboardBinding binding;
	private TinyDB tinyDB;
	private String username;
	private List<UserAccount> userAccountsList;
	private UserAccountsNavAdapter accountsAdapter;
	private HomeDashboardAdapter dashboardAdapter;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentHomeDashboardBinding.inflate(inflater, container, false);
		tinyDB = TinyDB.getInstance(requireContext());
		userAccountsList = new ArrayList<>();
		accountsAdapter = new UserAccountsNavAdapter(requireContext(), userAccountsList);
		dashboardAdapter = new HomeDashboardAdapter(requireContext());

		binding.mainSectionsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.mainSectionsRecyclerView.setAdapter(dashboardAdapter);

		binding.userAccountsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
		binding.userAccountsRecyclerView.setAdapter(accountsAdapter);

		loadUserInfo();

		binding.userAvatar.setOnClickListener(
				v -> {
					if (username != null) {
						Intent intentProfile = new Intent(requireContext(), ProfileActivity.class);
						intentProfile.putExtra("username", username);
						startActivity(intentProfile);
					}
				});

		return binding.getRoot();
	}

	private void loadUserInfo() {
		Call<User> call = RetrofitClient.getApiInterface(requireContext()).userGetCurrent();
		call.enqueue(
				new Callback<>() {
					@SuppressLint("NotifyDataSetChanged")
					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull Response<User> response) {
						if (!isAdded()) return;

						User userDetails = response.body();
						if (response.isSuccessful()
								&& response.code() == 200
								&& userDetails != null) {
							int accountId = tinyDB.getInt("currentActiveAccountId");
							UserAccountsApi userAccountsApi =
									BaseApi.getInstance(requireContext(), UserAccountsApi.class);
							assert userAccountsApi != null;
							UserAccount currentAccount = userAccountsApi.getAccountById(accountId);

							if (currentAccount == null) {
								AppUtil.logout(requireContext());
								return;
							}

							username = userDetails.getLogin();
							String userEmail = userDetails.getEmail();
							String userFullName = userDetails.getFullName();
							String userAvatarUrl = userDetails.getAvatarUrl();

							if (!currentAccount.getUserName().equals(userDetails.getLogin())) {
								userAccountsApi.updateUsername(accountId, userDetails.getLogin());
							}

							if (userFullName != null && !userFullName.isEmpty()) {
								binding.userFullname.setText(
										Html.fromHtml(userDetails.getFullName()));
							} else {
								binding.userFullname.setText("");
							}

							if (Boolean.parseBoolean(
									AppDatabaseSettings.getSettingsValue(
											requireContext(),
											AppDatabaseSettings.APP_USER_HIDE_EMAIL_IN_NAV_KEY))) {
								binding.userEmail.setVisibility(View.GONE);
							} else {
								binding.userEmail.setVisibility(View.VISIBLE);
								if (userEmail != null && !userEmail.isEmpty()) {
									binding.userEmail.setText(userEmail);
								} else {
									binding.userEmail.setText("");
								}
							}

							if (userAvatarUrl != null && !userAvatarUrl.isEmpty()) {
								Glide.with(requireContext())
										.load(userAvatarUrl)
										.diskCacheStrategy(DiskCacheStrategy.ALL)
										.placeholder(R.drawable.loader_animated)
										.centerCrop()
										.into(binding.userAvatar);
							}

							LiveData<List<UserAccount>> accountsLiveData =
									userAccountsApi.getAllAccounts();
							accountsLiveData.observe(
									getViewLifecycleOwner(),
									userAccounts -> {
										if (!isAdded()) return;
										if (userAccounts != null && !userAccounts.isEmpty()) {
											userAccountsList.clear();
											userAccountsList.addAll(userAccounts);
											accountsAdapter.notifyDataSetChanged();
											binding.userAccountsRecyclerView.setVisibility(
													View.VISIBLE);
										} else {
											binding.userAccountsRecyclerView.setVisibility(
													View.GONE);
										}
									});

							dashboardAdapter.updateUserInfo(
									username,
									userDetails.isIsAdmin(),
									tinyDB.getString("serverVersion"));

							fetchServerVersion();
						} else if (response.code() == 401) {
							AlertDialogs.authorizationTokenRevokedDialog(requireContext());
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
				});
	}

	private void fetchServerVersion() {
		Call<ServerVersion> call = RetrofitClient.getApiInterface(requireContext()).getVersion();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ServerVersion> call,
							@NonNull Response<ServerVersion> response) {
						if (!isAdded()) return; // Exit if fragment is detached
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							String version = response.body().getVersion();
							tinyDB.putString("serverVersion", version);
							dashboardAdapter.updateUserInfo(
									username, tinyDB.getBoolean("isAdmin"), version);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> call, @NonNull Throwable t) {}
				});
	}
}
