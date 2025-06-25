package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.badge.BadgeDrawable;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.GeneralAPISettings;
import org.gitnex.tea4j.v2.models.GeneralAttachmentSettings;
import org.gitnex.tea4j.v2.models.NotificationCount;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.HomeDashboardAdapter;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityMainBinding;
import org.mian.gitnex.databinding.FragmentHomeDashboardBinding;
import org.mian.gitnex.fragments.HomeDashboardFragment;
import org.mian.gitnex.fragments.NotificationsFragment;
import org.mian.gitnex.fragments.profile.DetailFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.structs.BottomSheetListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MainActivity extends BaseActivity
		implements NotificationsFragment.NotificationCountListener {

	private ActivityMainBinding binding;
	private TinyDB tinyDB;
	private NavController navController;
	private boolean noConnection;
	private BottomSheetListener profileInitListener;
	public static boolean refActivity;
	public static boolean reloadRepos;
	public static boolean closeActivity;

	public interface UserInfoCallback {
		void onUserInfoLoaded(String username, boolean isAdmin, String serverVersion);

		void onUserAccountsLoaded();
	}

	@Override
	public void onNotificationsMarkedRead() {
		getNotificationsCount();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
		getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary, getTheme()));

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		tinyDB = TinyDB.getInstance(this);

		Intent mainIntent = getIntent();
		Handler handler = new Handler();

		if (mainIntent.hasExtra("switchAccountId")
				&& AppUtil.switchToAccount(
						this,
						Objects.requireNonNull(BaseApi.getInstance(this, UserAccountsApi.class))
								.getAccountById(mainIntent.getIntExtra("switchAccountId", 0)))) {
			mainIntent.removeExtra("switchAccountId");
			recreate();
			return;
		}

		if (tinyDB.getInt("currentActiveAccountId", -1) <= 0) {
			AppUtil.logout(this);
			return;
		}

		setSupportActionBar(binding.toolbar);
		binding.toolbar.setVisibility(View.GONE);
		binding.toolbar.invalidate();

		NavHostFragment navHostFragment =
				(NavHostFragment)
						getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
		if (navHostFragment != null) {
			navController = navHostFragment.getNavController();
			NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

			binding.bottomNavigation.setOnItemSelectedListener(
					item -> {
						int itemId = item.getItemId();
						NavOptions navOptions =
								new NavOptions.Builder()
										.setPopUpTo(R.id.nav_graph, true)
										.setLaunchSingleTop(true)
										.build();

						try {
							if (itemId == R.id.homeDashboardFragment) {
								navController.navigate(
										R.id.homeDashboardFragment, null, navOptions);
								binding.bottomNavigation.setSelectedItemId(0);
								return true;
							} else if (itemId == R.id.repositoriesFragment) {
								navController.navigate(R.id.repositoriesFragment, null, navOptions);
								return true;
							} else if (itemId == R.id.notificationsFragment) {
								navController.navigate(
										R.id.notificationsFragment, null, navOptions);
								return true;
							} else if (itemId == R.id.exploreFragment) {
								navController.navigate(R.id.exploreFragment, null, navOptions);
								return true;
							}
						} catch (IllegalArgumentException ignored) {
						}
						return false;
					});

			navController.addOnDestinationChangedListener(
					(controller, destination, arguments) -> {
						boolean isHomeDashboard = destination.getId() == R.id.homeDashboardFragment;
						boolean isBottomNav =
								destination.getId() == R.id.homeDashboardFragment
										|| destination.getId() == R.id.repositoriesFragment
										|| destination.getId() == R.id.notificationsFragment
										|| destination.getId() == R.id.exploreFragment;

						if (!isBottomNav) {
							binding.bottomNavigation.setSelectedItemId(0);
						}

						binding.toolbar.setVisibility(isHomeDashboard ? View.GONE : View.VISIBLE);
						binding.toolbar.invalidate();

						if (!isHomeDashboard) {
							binding.toolbarTitle.setText(destination.getLabel());
							Objects.requireNonNull(getSupportActionBar())
									.setDisplayHomeAsUpEnabled(!isBottomNav);
						}
					});
		}

		loadUserInfo(
				null,
				null,
				null,
				null,
				null,
				new UserInfoCallback() {
					@Override
					public void onUserInfoLoaded(
							String username, boolean isAdmin, String serverVersion) {
						tinyDB.putString("username", username);
						if (profileInitListener != null) {
							profileInitListener.onButtonClicked(null);
						}
					}

					@Override
					public void onUserAccountsLoaded() {}
				});

		getNotificationsCount();

		handler.postDelayed(
				() -> {
					boolean connToInternet = AppUtil.hasNetworkConnection(this);
					if (!connToInternet) {
						if (!noConnection) {
							Toasty.error(
									this, getResources().getString(R.string.checkNetConnection));
						}
						noConnection = true;
					} else {
						giteaVersion();
						serverPageLimitSettings();
						updateGeneralAttachmentSettings();
					}
				},
				1500);

		int versionCode = AppUtil.getAppBuildNo(this);
		if (versionCode > tinyDB.getInt("versionCode")) {
			tinyDB.putInt("versionCode", versionCode);
			new ChangeLog(this).showDialog();
		}

		handleLaunchFragments(mainIntent);

		getOnBackPressedDispatcher()
				.addCallback(
						this,
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								if (navController.getCurrentDestination() != null
										&& navController.getCurrentDestination().getId()
												!= R.id.homeDashboardFragment) {
									NavOptions navOptions =
											new NavOptions.Builder()
													.setPopUpTo(R.id.nav_graph, true)
													.setLaunchSingleTop(true)
													.build();
									navController.navigate(
											R.id.homeDashboardFragment, null, navOptions);
									binding.bottomNavigation.setSelectedItemId(0);
								} else {
									finish();
								}
							}
						});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (closeActivity) {
			finishAndRemoveTask();
			closeActivity = false;
		}
		if (refActivity) {
			recreate();
			overridePendingTransition(0, 0);
			refActivity = false;
		}
		if (DetailFragment.refProfile) {
			loadUserInfo(
					null,
					null,
					null,
					null,
					null,
					new UserInfoCallback() {
						@Override
						public void onUserInfoLoaded(
								String username, boolean isAdmin, String serverVersion) {
							tinyDB.putString("username", username);
							if (profileInitListener != null) {
								profileInitListener.onButtonClicked(null);
							}
						}

						@Override
						public void onUserAccountsLoaded() {}
					});
			DetailFragment.refProfile = false;
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadUserInfo(
			HomeDashboardFragment fragment,
			FragmentHomeDashboardBinding binding,
			HomeDashboardAdapter dashboardAdapter,
			List<UserAccount> userAccountsList,
			UserAccountsNavAdapter accountsAdapter,
			UserInfoCallback callback) {
		Call<User> call = RetrofitClient.getApiInterface(this).userGetCurrent();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull Response<User> response) {
						if (fragment != null && !fragment.isAdded()) {
							return;
						}

						if (binding != null
								&& userAccountsList != null
								&& accountsAdapter != null) {
							assert fragment != null;
							loadUserAccounts(
									fragment, binding, userAccountsList, accountsAdapter, callback);
						}

						User userDetails = response.body();
						if (response.isSuccessful()
								&& response.code() == 200
								&& userDetails != null) {
							int accountId = tinyDB.getInt("currentActiveAccountId");
							UserAccountsApi userApi =
									BaseApi.getInstance(MainActivity.this, UserAccountsApi.class);
							assert userApi != null;
							UserAccount account = userApi.getAccountById(accountId);

							if (account == null) {
								return;
							}

							String username = userDetails.getLogin();
							String userEmail = userDetails.getEmail();
							String name = userDetails.getFullName();
							String avatarUrl = userDetails.getAvatarUrl();

							if (!account.getUserName().equals(username)) {
								userApi.updateUsername(accountId, username);
							}

							if (binding != null) {
								TextView userFullname = binding.userFullname;
								TextView userEmailView = binding.userEmail;
								ImageView userAvatar = binding.userAvatar;

								if (name != null && !name.isEmpty()) {
									userFullname.setText(Html.fromHtml(name));
								} else {
									userFullname.setText(username);
								}

								if (Boolean.parseBoolean(
										AppDatabaseSettings.getSettingsValue(
												MainActivity.this,
												AppDatabaseSettings
														.APP_USER_HIDE_EMAIL_IN_NAV_KEY))) {
									userEmailView.setVisibility(View.GONE);
								} else {
									userEmailView.setVisibility(View.VISIBLE);
									if (userEmail != null && !userEmail.isEmpty()) {
										userEmailView.setText(userEmail);
									} else {
										userEmailView.setText("");
									}
								}

								if (avatarUrl != null && !avatarUrl.isEmpty()) {
									Glide.with(MainActivity.this)
											.load(avatarUrl)
											.diskCacheStrategy(DiskCacheStrategy.ALL)
											.placeholder(R.drawable.loader_animated)
											.centerCrop()
											.into(userAvatar);
								}
							}

							fetchServerVersion(
									fragment,
									dashboardAdapter,
									username,
									userDetails.isIsAdmin(),
									callback);
						} else if (response.code() == 401) {
							AlertDialogs.authorizationTokenRevokedDialog(MainActivity.this);
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						if (fragment == null || !fragment.isAdded() || fragment.getView() == null) {
							if (callback != null) {
								callback.onUserAccountsLoaded();
							}
							return;
						}
						if (binding != null
								&& userAccountsList != null
								&& accountsAdapter != null) {
							loadUserAccounts(
									fragment, binding, userAccountsList, accountsAdapter, callback);
						}
					}
				});
	}

	@SuppressLint("NotifyDataSetChanged")
	private void loadUserAccounts(
			HomeDashboardFragment fragment,
			FragmentHomeDashboardBinding binding,
			List<UserAccount> userAccountsList,
			UserAccountsNavAdapter accountsAdapter,
			UserInfoCallback callback) {
		if (!fragment.isAdded() || fragment.getView() == null) {
			if (callback != null) {
				callback.onUserAccountsLoaded();
			}
			return;
		}

		UserAccountsApi userAccountsApi = BaseApi.getInstance(this, UserAccountsApi.class);
		assert userAccountsApi != null;
		userAccountsApi
				.getAllAccounts()
				.observe(
						fragment.getViewLifecycleOwner(),
						userAccounts -> {
							if (!fragment.isAdded() || fragment.getView() == null) {
								if (callback != null) {
									callback.onUserAccountsLoaded();
								}
								return;
							}
							if (userAccounts != null && !userAccounts.isEmpty()) {
								userAccountsList.clear();
								userAccountsList.addAll(userAccounts);
								accountsAdapter.notifyDataSetChanged();
								binding.userAccountsRecyclerView.setVisibility(View.VISIBLE);
							} else {
								binding.userAccountsRecyclerView.setVisibility(View.GONE);
							}
							if (callback != null) {
								callback.onUserAccountsLoaded();
							}
						});
	}

	private void fetchServerVersion(
			HomeDashboardFragment fragment,
			HomeDashboardAdapter dashboardAdapter,
			String username,
			boolean isAdmin,
			UserInfoCallback callback) {
		Call<ServerVersion> call = RetrofitClient.getApiInterface(this).getVersion();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ServerVersion> call,
							@NonNull Response<ServerVersion> response) {
						if (fragment != null && !fragment.isAdded()) {
							return;
						}
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							String version = response.body().getVersion();
							tinyDB.putString("serverVersion", version);
							if (dashboardAdapter != null) {
								dashboardAdapter.updateUserInfo(username, isAdmin, version);
							}
							if (callback != null) {
								callback.onUserInfoLoaded(username, isAdmin, version);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> call, @NonNull Throwable t) {}
				});
	}

	private void handleLaunchFragments(Intent mainIntent) {
		String launchFragment = mainIntent.getStringExtra("launchFragment");

		if (launchFragment != null) {
			mainIntent.removeExtra("launchFragment");
			if (launchFragment.equals("notifications")) {
				binding.toolbarTitle.setText(
						getResources().getString(R.string.pageTitleNotifications));
				navController.navigate(R.id.notificationsFragment);
				return;
			}
		}

		String launchFragmentByHandler = mainIntent.getStringExtra("launchFragmentByLink");
		if (launchFragmentByHandler != null) {
			mainIntent.removeExtra("launchFragmentByLink");
			NavOptions navOptions =
					new NavOptions.Builder()
							.setPopUpTo(R.id.nav_graph, true)
							.setLaunchSingleTop(true)
							.build();
			switch (launchFragmentByHandler) {
				case "repos":
					navController.navigate(R.id.repositoriesFragment, null, navOptions);
					break;
				case "org":
					navController.navigate(R.id.action_to_organizations, null, navOptions);
					break;
				case "notification":
					binding.toolbarTitle.setText(
							getResources().getString(R.string.pageTitleNotifications));
					navController.navigate(R.id.notificationsFragment, null, navOptions);
					break;
				case "explore":
					navController.navigate(R.id.exploreFragment, null, navOptions);
					break;
				case "profile":
					Intent intentProfile = new Intent(this, ProfileActivity.class);
					intentProfile.putExtra("username", tinyDB.getString("username"));
					startActivity(intentProfile);
					break;
				case "admin":
					navController.navigate(R.id.action_to_administration, null, navOptions);
					break;
			}
			return;
		}

		if (navController.getCurrentDestination() != null && mainIntent.getExtras() == null) {
			int currentDestinationId = navController.getCurrentDestination().getId();

			if (currentDestinationId == R.id.homeDashboardFragment
					|| currentDestinationId == navController.getGraph().getStartDestinationId()) {
				navigateToDefaultFragment();
			} else {
				binding.toolbarTitle.setText(navController.getCurrentDestination().getLabel());
			}
			return;
		}

		if (mainIntent.getExtras() == null) {
			navigateToDefaultFragment();
		}
	}

	private void navigateToDefaultFragment() {
		NavOptions navOptions =
				new NavOptions.Builder()
						.setPopUpTo(R.id.nav_graph, true)
						.setLaunchSingleTop(true)
						.build();

		switch (Integer.parseInt(
				AppDatabaseSettings.getSettingsValue(
						this, AppDatabaseSettings.APP_HOME_SCREEN_KEY))) {
			case 0:
				navController.navigate(R.id.homeDashboardFragment, null, navOptions);
				break;
			case 1:
				binding.toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
				navController.navigate(R.id.nav_graph, null, navOptions);
				break;
			case 2:
				binding.toolbarTitle.setText(
						getResources().getString(R.string.pageTitleStarredRepos));
				navController.navigate(R.id.action_to_starredRepositories, null, navOptions);
				break;
			case 3:
				binding.toolbarTitle.setText(getResources().getString(R.string.navOrg));
				navController.navigate(R.id.action_to_organizations, null, navOptions);
				break;
			case 4:
				binding.toolbarTitle.setText(getResources().getString(R.string.navRepos));
				navController.navigate(R.id.repositoriesFragment, null, navOptions);
				break;
			case 5:
				binding.toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
				navController.navigate(R.id.exploreFragment, null, navOptions);
				break;
			case 6:
				binding.toolbarTitle.setText(
						getResources().getString(R.string.pageTitleNotifications));
				navController.navigate(R.id.notificationsFragment, null, navOptions);
				break;
			case 7:
				binding.toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
				navController.navigate(R.id.action_to_myIssues, null, navOptions);
				break;
			case 8:
				binding.toolbarTitle.setText(getResources().getString(R.string.navMostVisited));
				navController.navigate(R.id.action_to_mostVisitedRepos, null, navOptions);
				break;
			case 9:
				binding.toolbarTitle.setText(getResources().getString(R.string.navNotes));
				navController.navigate(R.id.action_to_notes, null, navOptions);
				break;
			case 10:
				binding.toolbarTitle.setText(getResources().getString(R.string.activities));
				navController.navigate(R.id.activitiesFragment, null, navOptions);
				break;
			case 11:
				binding.toolbarTitle.setText(
						getResources().getString(R.string.navWatchedRepositories));
				navController.navigate(R.id.action_to_watchedRepositories, null, navOptions);
				break;
			default:
				navController.navigate(R.id.homeDashboardFragment, null, navOptions);
				break;
		}
	}

	public void getNotificationsCount() {
		Call<NotificationCount> call = RetrofitClient.getApiInterface(this).notifyNewAvailable();
		call.enqueue(
				new Callback<NotificationCount>() {
					@Override
					public void onResponse(
							@NonNull Call<NotificationCount> call,
							@NonNull Response<NotificationCount> response) {
						NotificationCount notificationCount = response.body();
						if (response.code() == 200
								&& notificationCount != null
								&& notificationCount.getNew() > 0) {
							BadgeDrawable badge =
									binding.bottomNavigation.getOrCreateBadge(
											R.id.notificationsFragment);
							badge.setNumber(Math.toIntExact(notificationCount.getNew()));
							badge.setBackgroundColor(getThemeColor(R.attr.primaryTextColor));
							badge.setBadgeTextColor(
									getThemeColor(R.attr.materialCardBackgroundColor));
						} else {
							binding.bottomNavigation.removeBadge(R.id.notificationsFragment);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<NotificationCount> call, @NonNull Throwable t) {}
				});
	}

	private int getThemeColor(int attr) {
		TypedValue typedValue = new TypedValue();
		getTheme().resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	public void giteaVersion() {
		Call<ServerVersion> call = RetrofitClient.getApiInterface(this).getVersion();
		call.enqueue(
				new Callback<ServerVersion>() {
					@Override
					public void onResponse(
							@NonNull Call<ServerVersion> call,
							@NonNull Response<ServerVersion> response) {
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							String version = response.body().getVersion();
							Objects.requireNonNull(
											BaseApi.getInstance(
													MainActivity.this, UserAccountsApi.class))
									.updateServerVersion(
											version, tinyDB.getInt("currentActiveAccountId"));
							getAccount()
									.setAccount(
											Objects.requireNonNull(
															BaseApi.getInstance(
																	MainActivity.this,
																	UserAccountsApi.class))
													.getAccountById(
															tinyDB.getInt(
																	"currentActiveAccountId")));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> call, @NonNull Throwable t) {}
				});
	}

	public void serverPageLimitSettings() {
		Call<GeneralAPISettings> call =
				RetrofitClient.getApiInterface(this).getGeneralAPISettings();
		call.enqueue(
				new Callback<GeneralAPISettings>() {
					@Override
					public void onResponse(
							@NonNull Call<GeneralAPISettings> call,
							@NonNull Response<GeneralAPISettings> response) {
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							int maxResponseItems =
									response.body().getMaxResponseItems() != null
											? Math.toIntExact(response.body().getMaxResponseItems())
											: 50;
							int defaultPagingNumber =
									response.body().getDefaultPagingNum() != null
											? Math.toIntExact(response.body().getDefaultPagingNum())
											: 25;
							Objects.requireNonNull(
											BaseApi.getInstance(
													MainActivity.this, UserAccountsApi.class))
									.updateServerPagingLimit(
											maxResponseItems,
											defaultPagingNumber,
											tinyDB.getInt("currentActiveAccountId"));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralAPISettings> call, @NonNull Throwable t) {}
				});
	}

	public void updateGeneralAttachmentSettings() {
		Call<GeneralAttachmentSettings> call =
				RetrofitClient.getApiInterface(this).getGeneralAttachmentSettings();
		call.enqueue(
				new Callback<GeneralAttachmentSettings>() {
					@Override
					public void onResponse(
							@NonNull Call<GeneralAttachmentSettings> call,
							@NonNull Response<GeneralAttachmentSettings> response) {
						if (response.isSuccessful()
								&& response.code() == 200
								&& response.body() != null) {
							int maxSize =
									response.body().getMaxSize() != null
											? Math.toIntExact(response.body().getMaxSize())
											: 2;
							int maxFiles =
									response.body().getMaxFiles() != null
											? Math.toIntExact(response.body().getMaxFiles())
											: 5;
							Objects.requireNonNull(
											BaseApi.getInstance(
													MainActivity.this, UserAccountsApi.class))
									.updateGeneralAttachmentSettings(
											maxSize,
											maxFiles,
											tinyDB.getInt("currentActiveAccountId"));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralAttachmentSettings> call, @NonNull Throwable t) {}
				});
	}

	public void setProfileInitListener(BottomSheetListener profileInitListener) {
		this.profileInitListener = profileInitListener;
	}

	@Override
	public boolean onSupportNavigateUp() {
		return navController.navigateUp() || super.onSupportNavigateUp();
	}
}
