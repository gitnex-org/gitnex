package org.mian.gitnex.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.GeneralAPISettings;
import org.gitnex.tea4j.v2.models.GeneralAttachmentSettings;
import org.gitnex.tea4j.v2.models.NotificationCount;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityMainBinding;
import org.mian.gitnex.fragments.AccountSettingsFragment;
import org.mian.gitnex.fragments.AdministrationFragment;
import org.mian.gitnex.fragments.BottomSheetMyIssuesFilterFragment;
import org.mian.gitnex.fragments.DashboardFragment;
import org.mian.gitnex.fragments.ExploreFragment;
import org.mian.gitnex.fragments.MostVisitedReposFragment;
import org.mian.gitnex.fragments.MyIssuesFragment;
import org.mian.gitnex.fragments.MyRepositoriesFragment;
import org.mian.gitnex.fragments.NotesFragment;
import org.mian.gitnex.fragments.NotificationsFragment;
import org.mian.gitnex.fragments.OrganizationsFragment;
import org.mian.gitnex.fragments.RepositoriesFragment;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.fragments.StarredRepositoriesFragment;
import org.mian.gitnex.fragments.WatchedRepositoriesFragment;
import org.mian.gitnex.fragments.profile.DetailFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.structs.FragmentRefreshListener;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
@SuppressWarnings("ConstantConditions")
public class MainActivity extends BaseActivity
		implements NavigationView.OnNavigationItemSelectedListener,
				BottomSheetMyIssuesFilterFragment.BottomSheetListener {

	public static boolean refActivity = false;
	public static boolean reloadRepos = false;
	private DrawerLayout drawer;
	private TextView toolbarTitle;
	private Typeface myTypeface;
	private boolean noConnection = false;
	private View hView;
	private NavigationView navigationView;
	private MenuItem navNotifications;
	private TextView notificationCounter;
	private BottomSheetListener profileInitListener;
	private FragmentRefreshListener fragmentRefreshListenerMyIssues;
	private String username;
	public static boolean closeActivity = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(activityMainBinding.getRoot());

		Intent mainIntent = getIntent();
		Handler handler = new Handler();

		// DO NOT MOVE
		if (mainIntent.hasExtra("switchAccountId")
				&& AppUtil.switchToAccount(
						ctx,
						BaseApi.getInstance(ctx, UserAccountsApi.class)
								.getAccountById(mainIntent.getIntExtra("switchAccountId", 0)))) {

			mainIntent.removeExtra("switchAccountId");
			recreate();
			return;
		}
		// DO NOT MOVE

		if (tinyDB.getInt("currentActiveAccountId", -1) <= 0) {
			AppUtil.logout(ctx);
			return;
		}

		noConnection = false;

		Toolbar toolbar = activityMainBinding.toolbar;
		toolbarTitle = activityMainBinding.toolbarTitle;

		myTypeface = AppUtil.getTypeface(this);
		toolbarTitle.setTypeface(myTypeface);
		setSupportActionBar(toolbar);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragmentById = fm.findFragmentById(R.id.fragment_container);

		if (fragmentById instanceof SettingsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navSettings));
		} else if (fragmentById instanceof MyRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
		} else if (fragmentById instanceof StarredRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
		} else if (fragmentById instanceof OrganizationsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navOrg));
		} else if (fragmentById instanceof ExploreFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
		} else if (fragmentById instanceof NotificationsFragment) {
			toolbarTitle.setText(R.string.pageTitleNotifications);
		} else if (fragmentById instanceof AccountSettingsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navAccount));
		} else if (fragmentById instanceof MostVisitedReposFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navMostVisited));
		} else if (fragmentById instanceof NotesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navNotes));
		} else if (fragmentById instanceof AdministrationFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
		} else if (fragmentById instanceof MyIssuesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
		} else if (fragmentById instanceof DashboardFragment) {
			toolbarTitle.setText(getResources().getString(R.string.dashboard));
		} else if (fragmentById instanceof WatchedRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navWatchedRepositories));
		}

		getNotificationsCount();

		drawer = activityMainBinding.drawerLayout;
		navigationView = activityMainBinding.navView;
		navigationView.setNavigationItemSelectedListener(this);
		hView = navigationView.getHeaderView(0);

		Menu menu = navigationView.getMenu();
		navNotifications = menu.findItem(R.id.nav_notifications);
		/*MenuItem navDashboard = menu.findItem(R.id.nav_dashboard);

		navDashboard.getActionView().findViewById(R.id.betaBadge).setVisibility(View.VISIBLE);
		TextView dashboardBetaView = navDashboard.getActionView().findViewById(R.id.betaBadge);
		dashboardBetaView.setText(R.string.beta);*/

		navigationView
				.getViewTreeObserver()
				.addOnGlobalLayoutListener(
						() -> {
							ArrayList<View> menuItems = new ArrayList<>(menu.size());
							for (int i = 0; i < menu.size(); i++) {
								MenuItem item = menu.getItem(i);
								navigationView.findViewsWithText(
										menuItems, item.getTitle(), View.FIND_VIEWS_WITH_TEXT);
							}

							for (final View menuItem : menuItems) {
								((TextView) menuItem).setTypeface(myTypeface);
							}
						});

		ActionBarDrawerToggle toggle =
				new ActionBarDrawerToggle(
						this,
						drawer,
						toolbar,
						R.string.navigationDrawerOpen,
						R.string.navigationDrawerClose);

		drawer.addDrawerListener(toggle);
		drawer.addDrawerListener(
				new DrawerLayout.DrawerListener() {

					@Override
					public void onDrawerOpened(@NonNull View drawerView) {

						if (noConnection) {

							Toasty.error(
									ctx, getResources().getString(R.string.checkNetConnection));
							noConnection = false;
						}

						TextView userEmail = hView.findViewById(R.id.userEmail);
						TextView userFullName = hView.findViewById(R.id.userFullname);
						ImageView userAvatar = hView.findViewById(R.id.userAvatar);
						MaterialCardView navRecyclerViewFrame =
								hView.findViewById(R.id.userAccountsFrame);

						List<UserAccount> userAccountsList = new ArrayList<>();
						UserAccountsApi userAccountsApi;
						userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

						RecyclerView navRecyclerViewUserAccounts =
								hView.findViewById(R.id.userAccounts);
						UserAccountsNavAdapter adapterUserAccounts =
								new UserAccountsNavAdapter(ctx, userAccountsList, drawer);

						userAccountsApi
								.getAllAccounts()
								.observe(
										(AppCompatActivity) ctx,
										userAccounts -> {
											if (!userAccounts.isEmpty()) {
												userAccountsList.clear();
												userAccountsList.addAll(userAccounts);
												navRecyclerViewUserAccounts.setAdapter(
														adapterUserAccounts);
												navRecyclerViewFrame.setVisibility(View.VISIBLE);
											}
										});

						userEmail.setTypeface(myTypeface);
						userFullName.setTypeface(myTypeface);

						if (getAccount().getUserInfo() != null) {
							username = getAccount().getUserInfo().getLogin();
							String userEmailNav = getAccount().getUserInfo().getEmail();
							String userFullNameNav = getAccount().getFullName();
							String userAvatarNav = getAccount().getUserInfo().getAvatarUrl();

							if (Boolean.parseBoolean(
									AppDatabaseSettings.getSettingsValue(
											ctx,
											AppDatabaseSettings.APP_USER_HIDE_EMAIL_IN_NAV_KEY))) {
								userEmail.setVisibility(View.GONE);
							} else {
								userEmail.setVisibility(View.VISIBLE);
								if (!userEmailNav.isEmpty()) {
									userEmail.setText(userEmailNav);
								}
							}

							if (!userFullNameNav.isEmpty()) {
								userFullName.setText(Html.fromHtml(userFullNameNav));
							}

							if (!userAvatarNav.isEmpty()) {

								Glide.with(ctx)
										.load(userAvatarNav)
										.diskCacheStrategy(DiskCacheStrategy.ALL)
										.placeholder(R.drawable.loader_animated)
										.centerCrop()
										.into(userAvatar);
							}
						}

						userAvatar.setOnClickListener(
								v -> {
									Intent intentProfile = new Intent(ctx, ProfileActivity.class);
									intentProfile.putExtra("username", username);
									ctx.startActivity(intentProfile);
									drawer.closeDrawers();
								});

						getNotificationsCount();
					}

					@Override
					public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

						if (getAccount().getUserInfo() != null) {
							navigationView
									.getMenu()
									.findItem(R.id.nav_administration)
									.setVisible(getAccount().getUserInfo().isIsAdmin());
						} else {
							// hide first
							navigationView
									.getMenu()
									.findItem(R.id.nav_administration)
									.setVisible(false);
						}

						if (getAccount().requiresVersion("1.20.0")) {
							navigationView.getMenu().findItem(R.id.nav_dashboard).setVisible(true);
						}
					}

					@Override
					public void onDrawerClosed(@NonNull View drawerView) {}

					@Override
					public void onDrawerStateChanged(int newState) {}
				});

		toggle.syncState();
		toolbar.setNavigationIcon(R.drawable.ic_menu);

		String launchFragment = mainIntent.getStringExtra("launchFragment");

		if (launchFragment != null) {

			mainIntent.removeExtra("launchFragment");

			if (launchFragment.equals("notifications")) {
				toolbarTitle.setText(getResources().getString(R.string.pageTitleNotifications));
				getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_container, new NotificationsFragment())
						.commit();
				navigationView.setCheckedItem(R.id.nav_notifications);
				return;
			}
		}

		String launchFragmentByHandler = mainIntent.getStringExtra("launchFragmentByLinkHandler");

		if (launchFragmentByHandler != null) {

			mainIntent.removeExtra("launchFragmentByLinkHandler");

			switch (launchFragmentByHandler) {
				case "repos":
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new RepositoriesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_repositories);
					return;

				case "org":
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new OrganizationsFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_organizations);
					return;

				case "notification":
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new NotificationsFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_notifications);
					setActionBarTitle(getResources().getString(R.string.pageTitleNotifications));
					return;

				case "explore":
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new ExploreFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_explore);
					return;

				case "profile":
					Intent intentProfile = new Intent(ctx, ProfileActivity.class);
					intentProfile.putExtra("username", username);
					ctx.startActivity(intentProfile);
					return;

				case "admin":
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new AdministrationFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_administration);
					return;
			}
		}

		if (savedInstanceState == null) {

			switch (Integer.parseInt(
					AppDatabaseSettings.getSettingsValue(
							ctx, AppDatabaseSettings.APP_HOME_SCREEN_KEY))) {
				case 1:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new StarredRepositoriesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_starred_repos);
					break;

				case 2:
					toolbarTitle.setText(getResources().getString(R.string.navOrg));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new OrganizationsFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_organizations);
					break;

				case 3:
					toolbarTitle.setText(getResources().getString(R.string.navRepos));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new RepositoriesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_repositories);
					break;

				case 4:
					toolbarTitle.setText(getResources().getString(R.string.navAccount));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new AccountSettingsFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_account_settings);
					break;

				case 5:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new ExploreFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_explore);
					break;

				case 6:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleNotifications));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new NotificationsFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_notifications);
					break;

				case 7:
					toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new MyIssuesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_my_issues);
					break;
				case 8:
					toolbarTitle.setText(getResources().getString(R.string.navMostVisited));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new MostVisitedReposFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_most_visited);
					break;
				case 9:
					toolbarTitle.setText(getResources().getString(R.string.navNotes));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new NotesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_notes);
					break;
				case 10:
					toolbarTitle.setText(getResources().getString(R.string.dashboard));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new DashboardFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_dashboard);
					break;
				case 11:
					toolbarTitle.setText(getResources().getString(R.string.navWatchedRepositories));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new WatchedRepositoriesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_watched_repositories);
					break;
				default:
					toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
					getSupportFragmentManager()
							.beginTransaction()
							.replace(R.id.fragment_container, new MyRepositoriesFragment())
							.commit();
					navigationView.setCheckedItem(R.id.nav_home);
					break;
			}
		}

		handler.postDelayed(
				() -> {
					boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
					if (!connToInternet) {

						if (!noConnection) {
							Toasty.error(
									ctx, getResources().getString(R.string.checkNetConnection));
						}
						noConnection = true;
					} else {

						loadUserInfo();
						noConnection = false;
					}
				},
				750);

		handler.postDelayed(
				() -> {
					boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
					if (!connToInternet) {

						if (!noConnection) {
							Toasty.error(
									ctx, getResources().getString(R.string.checkNetConnection));
						}
						noConnection = true;
					} else {

						giteaVersion();
						serverPageLimitSettings();
						updateGeneralAttachmentSettings();
					}
				},
				1500);

		// Changelog popup
		int versionCode = AppUtil.getAppBuildNo(appCtx);
		if (versionCode > tinyDB.getInt("versionCode")) {

			tinyDB.putInt("versionCode", versionCode);

			ChangeLog changelogDialog = new ChangeLog(this);
			changelogDialog.showDialog();
		}

		OnBackPressedCallback onBackPressedCallback =
				new OnBackPressedCallback(true) {
					@Override
					public void handleOnBackPressed() {
						if (drawer.isDrawerOpen(GravityCompat.START)) {
							drawer.closeDrawer(GravityCompat.START);
						} else {
							finish();
						}
					}
				};
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (closeActivity) {
			finishAndRemoveTask();
			closeActivity = false;
		}

		if (refActivity) {
			this.recreate();
			this.overridePendingTransition(0, 0);
			refActivity = false;
		}
		if (DetailFragment.refProfile) {
			loadUserInfo();
			DetailFragment.refProfile = false;
		}
	}

	public void setActionBarTitle(String title) {

		toolbarTitle.setText(title);
	}

	@Override
	public void onButtonClicked(String text) {

		if (getFragmentRefreshListener() != null) {
			getFragmentRefreshListener().onRefresh(text);
		} else {

			Fragment currentFragment =
					getSupportFragmentManager().findFragmentById(R.id.fragment_container);
			if (currentFragment instanceof MyIssuesFragment) {
				((MyIssuesFragment) currentFragment).updateFilterState(text);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		if (id == R.id.filter) {

			Fragment currentFragment =
					getSupportFragmentManager().findFragmentById(R.id.fragment_container);
			String currentFilter = "open_created_by_me";

			if (currentFragment instanceof MyIssuesFragment) {
				currentFilter = ((MyIssuesFragment) currentFragment).getCurrentFilter();
			}
			BottomSheetMyIssuesFilterFragment filterBottomSheet =
					BottomSheetMyIssuesFilterFragment.newInstance(currentFilter);
			filterBottomSheet.show(getSupportFragmentManager(), "myIssuesFilterMenuBottomSheet");
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		int id = menuItem.getItemId();

		if (id == R.id.nav_home) {

			toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new MyRepositoriesFragment())
					.commit();
		} else if (id == R.id.nav_organizations) {

			toolbarTitle.setText(getResources().getString(R.string.navOrg));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new OrganizationsFragment())
					.commit();
		} else if (id == R.id.nav_profile) {

			Intent intentProfile = new Intent(ctx, ProfileActivity.class);
			intentProfile.putExtra("username", username);
			ctx.startActivity(intentProfile);
			drawer.closeDrawers();
		} else if (id == R.id.nav_repositories) {

			toolbarTitle.setText(getResources().getString(R.string.navRepos));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new RepositoriesFragment())
					.commit();
		} else if (id == R.id.nav_settings) {

			toolbarTitle.setText(getResources().getString(R.string.navSettings));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new SettingsFragment())
					.commit();
		} else if (id == R.id.nav_starred_repos) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new StarredRepositoriesFragment())
					.commit();
		} else if (id == R.id.nav_explore) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new ExploreFragment())
					.commit();
		} else if (id == R.id.nav_notifications) {

			toolbarTitle.setText(R.string.pageTitleNotifications);
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new NotificationsFragment())
					.commit();
		} else if (id == R.id.nav_administration) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new AdministrationFragment())
					.commit();
		} else if (id == R.id.nav_my_issues) {

			toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new MyIssuesFragment())
					.commit();
		} else if (id == R.id.nav_most_visited) {

			toolbarTitle.setText(getResources().getString(R.string.navMostVisited));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new MostVisitedReposFragment())
					.commit();
		} else if (id == R.id.nav_notes) {

			toolbarTitle.setText(getResources().getString(R.string.navNotes));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new NotesFragment())
					.commit();
		} else if (id == R.id.nav_dashboard) {

			toolbarTitle.setText(getResources().getString(R.string.dashboard));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new DashboardFragment())
					.commit();
		} else if (id == R.id.nav_account_settings) {

			toolbarTitle.setText(getResources().getString(R.string.navAccount));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new AccountSettingsFragment())
					.commit();
		} else if (id == R.id.nav_watched_repositories) {

			toolbarTitle.setText(getResources().getString(R.string.navWatchedRepositories));
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, new WatchedRepositoriesFragment())
					.commit();
		}

		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private void updateGeneralAttachmentSettings() {

		Call<GeneralAttachmentSettings> generalAttachmentSettings =
				RetrofitClient.getApiInterface(ctx).getGeneralAttachmentSettings();

		generalAttachmentSettings.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<GeneralAttachmentSettings> generalAPISettings,
							@NonNull retrofit2.Response<GeneralAttachmentSettings> response) {

						if (response.code() == 200 && response.body() != null) {

							int max_size = 2;
							int max_files = 5;

							if (response.body().getMaxSize() != null) {
								max_size = Math.toIntExact(response.body().getMaxSize());
							}
							if (response.body().getMaxFiles() != null) {
								max_files = Math.toIntExact(response.body().getMaxFiles());
							}

							BaseApi.getInstance(ctx, UserAccountsApi.class)
									.updateGeneralAttachmentSettings(
											max_size,
											max_files,
											tinyDB.getInt("currentActiveAccountId"));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralAttachmentSettings> generalAPISettings,
							@NonNull Throwable t) {}
				});
	}

	private void serverPageLimitSettings() {

		Call<GeneralAPISettings> generalAPISettings =
				RetrofitClient.getApiInterface(ctx).getGeneralAPISettings();
		generalAPISettings.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<GeneralAPISettings> generalAPISettings,
							@NonNull retrofit2.Response<GeneralAPISettings> response) {

						if (response.code() == 200 && response.body() != null) {

							int maxResponseItems = 50;
							int defaultPagingNumber = 25;

							if (response.body().getMaxResponseItems() != null) {
								maxResponseItems =
										Math.toIntExact(response.body().getMaxResponseItems());
							}
							if (response.body().getDefaultPagingNum() != null) {
								defaultPagingNumber =
										Math.toIntExact(response.body().getDefaultPagingNum());
							}

							BaseApi.getInstance(ctx, UserAccountsApi.class)
									.updateServerPagingLimit(
											maxResponseItems,
											defaultPagingNumber,
											tinyDB.getInt("currentActiveAccountId"));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralAPISettings> generalAPISettings,
							@NonNull Throwable t) {}
				});
	}

	private void giteaVersion() {

		Call<ServerVersion> callVersion = RetrofitClient.getApiInterface(ctx).getVersion();
		callVersion.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<ServerVersion> callVersion,
							@NonNull retrofit2.Response<ServerVersion> responseVersion) {

						if (responseVersion.code() == 200 && responseVersion.body() != null) {
							String version = responseVersion.body().getVersion();

							BaseApi.getInstance(ctx, UserAccountsApi.class)
									.updateServerVersion(
											version, tinyDB.getInt("currentActiveAccountId"));
							getAccount()
									.setAccount(
											BaseApi.getInstance(ctx, UserAccountsApi.class)
													.getAccountById(
															tinyDB.getInt(
																	"currentActiveAccountId")));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> callVersion, @NonNull Throwable t) {}
				});
	}

	private void loadUserInfo() {
		Call<User> call = RetrofitClient.getApiInterface(ctx).userGetCurrent();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User userDetails = response.body();

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								assert userDetails != null;

								getAccount().setUserInfo(userDetails);
								navigationView
										.getMenu()
										.findItem(R.id.nav_administration)
										.setVisible(userDetails.isIsAdmin());
								if (!getAccount()
										.getAccount()
										.getUserName()
										.equals(userDetails.getLogin())) {
									// user changed it's name -> update database
									int accountId = getAccount().getAccount().getAccountId();
									BaseApi.getInstance(MainActivity.this, UserAccountsApi.class)
											.updateUsername(accountId, userDetails.getLogin());
									getAccount()
											.setAccount(
													BaseApi.getInstance(
																	MainActivity.this,
																	UserAccountsApi.class)
															.getAccountById(accountId));
								}
								if (profileInitListener != null) {
									profileInitListener.onButtonClicked(null);
								}
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							String toastError =
									getResources()
											.getString(R.string.genericApiError, response.code());
							// Toasty.error(ctx, toastError);
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
				});
	}

	private void getNotificationsCount() {

		Call<NotificationCount> call = RetrofitClient.getApiInterface(ctx).notifyNewAvailable();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<NotificationCount> call,
							@NonNull retrofit2.Response<NotificationCount> response) {

						NotificationCount notificationCount = response.body();

						if (response.code() == 200) {

							assert notificationCount != null;
							if (notificationCount.getNew() > 0) {

								navNotifications
										.getActionView()
										.findViewById(R.id.counterBadgeNotification)
										.setVisibility(View.VISIBLE);
								notificationCounter =
										navNotifications
												.getActionView()
												.findViewById(R.id.counterBadgeNotification);
								notificationCounter.setText(
										String.valueOf(notificationCount.getNew()));
							} else {
								navNotifications
										.getActionView()
										.findViewById(R.id.counterBadgeNotification)
										.setVisibility(View.GONE);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<NotificationCount> call, @NonNull Throwable t) {}
				});
	}

	public void setProfileInitListener(BottomSheetListener profileInitListener) {

		this.profileInitListener = profileInitListener;
	}

	public FragmentRefreshListener getFragmentRefreshListener() {
		return fragmentRefreshListenerMyIssues;
	}

	public void setFragmentRefreshListenerMyIssues(FragmentRefreshListener listener) {
		this.fragmentRefreshListenerMyIssues = listener;
	}

	public interface FragmentRefreshListener {
		void onRefresh(String myIssues);
	}
}
