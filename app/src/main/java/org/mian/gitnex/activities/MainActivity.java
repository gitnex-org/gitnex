package org.mian.gitnex.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import org.gitnex.tea4j.models.GiteaVersion;
import org.gitnex.tea4j.models.NotificationCount;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserAccountsNavAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityMainBinding;
import org.mian.gitnex.fragments.AdministrationFragment;
import org.mian.gitnex.fragments.BottomSheetDraftsFragment;
import org.mian.gitnex.fragments.DraftsFragment;
import org.mian.gitnex.fragments.ExploreFragment;
import org.mian.gitnex.fragments.MyIssuesFragment;
import org.mian.gitnex.fragments.MyProfileFragment;
import org.mian.gitnex.fragments.MyRepositoriesFragment;
import org.mian.gitnex.fragments.NotificationsFragment;
import org.mian.gitnex.fragments.OrganizationsFragment;
import org.mian.gitnex.fragments.RepositoriesFragment;
import org.mian.gitnex.fragments.SettingsFragment;
import org.mian.gitnex.fragments.StarredRepositoriesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.structs.BottomSheetListener;
import java.util.ArrayList;
import java.util.List;
import jp.wasabeef.picasso.transformations.BlurTransformation;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

@SuppressWarnings("ConstantConditions")
public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener, BottomSheetListener {

	public static boolean repoCreated = false;

	private DrawerLayout drawer;
	private TextView toolbarTitle;
	private Typeface myTypeface;

	private String instanceToken;
	private boolean noConnection = false;

	private View hView;
	private NavigationView navigationView;
	private MenuItem navNotifications;
	private TextView notificationCounter;

	private BottomSheetListener profileInitListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityMainBinding activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(activityMainBinding.getRoot());

		Intent mainIntent = getIntent();
		Handler handler = new Handler();

		// DO NOT MOVE
		if(mainIntent.hasExtra("switchAccountId") &&
			AppUtil.switchToAccount(ctx, BaseApi.getInstance(ctx, UserAccountsApi.class)
				.getAccountById(mainIntent.getIntExtra("switchAccountId", 0)))) {

			mainIntent.removeExtra("switchAccountId");
			recreate();
			return;
		}
		// DO NOT MOVE

		instanceToken = getAccount().getAuthorization();
		noConnection = false;

		if(tinyDB.getInt("currentActiveAccountId", -1) <= 0) {
			AppUtil.logout(ctx);
		}

		Toolbar toolbar = activityMainBinding.toolbar;
		toolbarTitle = activityMainBinding.toolbarTitle;

		switch(tinyDB.getInt("customFontId", -1)) {

			case 0:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
				break;

			case 2:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/sourcecodeproregular.ttf");
				break;

			default:
				myTypeface = Typeface.createFromAsset(getAssets(), "fonts/manroperegular.ttf");
				break;
		}

		toolbarTitle.setTypeface(myTypeface);
		setSupportActionBar(toolbar);

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragmentById = fm.findFragmentById(R.id.fragment_container);

		if(fragmentById instanceof SettingsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navSettings));
		}
		else if(fragmentById instanceof MyRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
		}
		else if(fragmentById instanceof StarredRepositoriesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
		}
		else if(fragmentById instanceof OrganizationsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navOrg));
		}
		else if(fragmentById instanceof ExploreFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
		}
		else if(fragmentById instanceof NotificationsFragment) {
			toolbarTitle.setText(R.string.pageTitleNotifications);
		}
		else if(fragmentById instanceof MyProfileFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navProfile));
		}
		else if(fragmentById instanceof DraftsFragment) {
			toolbarTitle.setText(getResources().getString(R.string.titleDrafts));
		}
		else if(fragmentById instanceof AdministrationFragment) {
			toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
		}
		else if(fragmentById instanceof MyIssuesFragment) {
			toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
		}

		getNotificationsCount(instanceToken);

		drawer = activityMainBinding.drawerLayout;
		navigationView = activityMainBinding.navView;
		navigationView.setNavigationItemSelectedListener(this);
		hView = navigationView.getHeaderView(0);

		Menu menu = navigationView.getMenu();
		navNotifications = menu.findItem(R.id.nav_notifications);

		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigationDrawerOpen, R.string.navigationDrawerClose);

		drawer.addDrawerListener(toggle);
		drawer.addDrawerListener(new DrawerLayout.DrawerListener() {

			@Override
			public void onDrawerOpened(@NonNull View drawerView) {

				if(noConnection) {

					Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
					noConnection = false;
				}

				TextView userEmail = hView.findViewById(R.id.userEmail);
				TextView userFullName = hView.findViewById(R.id.userFullname);
				ImageView userAvatar = hView.findViewById(R.id.userAvatar);
				ImageView userAvatarBackground = hView.findViewById(R.id.userAvatarBackground);
				CardView navRecyclerViewFrame = hView.findViewById(R.id.userAccountsFrame);

				List<UserAccount> userAccountsList = new ArrayList<>();
				UserAccountsApi userAccountsApi;
				userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

				RecyclerView navRecyclerViewUserAccounts = hView.findViewById(R.id.userAccounts);
				UserAccountsNavAdapter adapterUserAccounts = new UserAccountsNavAdapter(ctx, userAccountsList, drawer);

				userAccountsApi.getAllLoggedInAccounts().observe((AppCompatActivity) ctx, userAccounts -> {
					if(userAccounts.size() > 0) {
						userAccountsList.clear();
						userAccountsList.addAll(userAccounts);
						navRecyclerViewUserAccounts.setAdapter(adapterUserAccounts);
						navRecyclerViewFrame.setVisibility(View.VISIBLE);
					}
				});

				userEmail.setTypeface(myTypeface);
				userFullName.setTypeface(myTypeface);

				if (getAccount().getUserInfo() != null) {
					String userEmailNav = getAccount().getUserInfo().getEmail();
					String userFullNameNav = getAccount().getFullName();
					String userAvatarNav = getAccount().getUserInfo().getAvatar();

					if(!userEmailNav.equals("")) {
						userEmail.setText(userEmailNav);
					}

					if(!userFullNameNav.equals("")) {
						userFullName.setText(Html.fromHtml(userFullNameNav));
					}

					if(!userAvatarNav.equals("")) {

						int avatarRadius = AppUtil.getPixelsFromDensity(ctx, 3);

						PicassoService.getInstance(ctx).get().load(userAvatarNav).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(avatarRadius, 0)).resize(160, 160).centerCrop().into(userAvatar);

						PicassoService.getInstance(ctx).get().load(userAvatarNav).transform(new BlurTransformation(ctx))
							.into(userAvatarBackground, new com.squareup.picasso.Callback() {

								@Override
								public void onSuccess() {

									int textColor = new ColorInverter().getImageViewContrastColor(userAvatarBackground);

									userFullName.setTextColor(textColor);
									userEmail.setTextColor(textColor);
								}

								@Override
								public void onError(Exception e) {

								}
							});
					}
				}

				userAvatar.setOnClickListener(v -> {

					toolbarTitle.setText(getResources().getString(R.string.navProfile));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyProfileFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_profile);
					drawer.closeDrawers();

				});

				getNotificationsCount(instanceToken);
			}

			@Override
			public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

				if (getAccount().getUserInfo() != null) {
					navigationView.getMenu().findItem(R.id.nav_administration).setVisible(getAccount().getUserInfo().getIs_admin());
				} else {
					// hide first
					navigationView.getMenu().findItem(R.id.nav_administration).setVisible(false);
				}

				if(getAccount().requiresVersion("1.14.0")) {
					navigationView.getMenu().findItem(R.id.nav_my_issues).setVisible(true);
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

		if(launchFragment != null) {

			mainIntent.removeExtra("launchFragment");

			switch(launchFragment) {

				case "drafts":
					toolbarTitle.setText(getResources().getString(R.string.titleDrafts));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DraftsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_comments_draft);
					return;

				case "notifications":
					toolbarTitle.setText(getResources().getString(R.string.pageTitleNotifications));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_notifications);
					return;
			}
		}

		String launchFragmentByHandler = mainIntent.getStringExtra("launchFragmentByLinkHandler");

		if(launchFragmentByHandler != null) {

			mainIntent.removeExtra("launchFragmentByLinkHandler");

			switch(launchFragmentByHandler) {

				case "repos":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepositoriesFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_repositories);
					return;

				case "org":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_organizations);
					return;

				case "notification":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_notifications);
					setActionBarTitle(getResources().getString(R.string.pageTitleNotifications));
					return;

				case "explore":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_explore);
					return;

				case "profile":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyProfileFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_profile);
					return;

				case "admin":
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdministrationFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_administration);
					return;

			}
		}

		if(savedInstanceState == null) {

			if(!getAccount().requiresVersion("1.12.3")) {
				if(tinyDB.getInt("homeScreenId", 0) == 7) {
					tinyDB.putInt("homeScreenId", 0);
				}
			}

			switch(tinyDB.getInt("homeScreenId", 0)) {

				case 1:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StarredRepositoriesFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_starred_repos);
					break;

				case 2:
					toolbarTitle.setText(getResources().getString(R.string.navOrg));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_organizations);
					break;

				case 3:
					toolbarTitle.setText(getResources().getString(R.string.navRepos));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepositoriesFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_repositories);
					break;

				case 4:
					toolbarTitle.setText(getResources().getString(R.string.navProfile));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyProfileFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_profile);
					break;

				case 5:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_explore);
					break;

				case 6:
					toolbarTitle.setText(getResources().getString(R.string.titleDrafts));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DraftsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_comments_draft);
					break;

				case 7:
					toolbarTitle.setText(getResources().getString(R.string.pageTitleNotifications));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_notifications);
					break;

				case 8:
					toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyIssuesFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_my_issues);
					break;

				default:
					toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
					getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRepositoriesFragment()).commit();
					navigationView.setCheckedItem(R.id.nav_home);
					break;
			}
		}

		handler.postDelayed(() -> {

			boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
			if(!connToInternet) {

				if(!noConnection) {
					Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
				}
				noConnection = true;
			}
			else {

				loadUserInfo();
				giteaVersion();
				noConnection = false;
			}
			Log.e("Network status is: ", String.valueOf(connToInternet));
		}, 1500);

		// Changelog popup
		int versionCode = AppUtil.getAppBuildNo(appCtx);
		if(versionCode > tinyDB.getInt("versionCode")) {

			tinyDB.putInt("versionCode", versionCode);

			ChangeLog changelogDialog = new ChangeLog(this);
			changelogDialog.showDialog();
		}
	}

	public void setActionBarTitle(String title) {

		toolbarTitle.setText(title);
	}

	@Override
	public void onButtonClicked(String text) {
		int currentActiveAccountId = tinyDB.getInt("currentActiveAccountId");

		if("deleteDrafts".equals(text)) {

			if(currentActiveAccountId > 0) {

				FragmentManager fm = getSupportFragmentManager();
				DraftsFragment frag = (DraftsFragment) fm.findFragmentById(R.id.fragment_container);

				if(frag != null) {

					new AlertDialog.Builder(ctx)
						.setTitle(R.string.deleteAllDrafts)
						.setIcon(R.drawable.ic_delete)
						.setCancelable(false)
						.setMessage(R.string.deleteAllDraftsDialogMessage)
						.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

							frag.deleteAllDrafts(currentActiveAccountId);
							dialog.dismiss();

						})
						.setNeutralButton(R.string.cancelButton, null).show();
				}
				else {

					Toasty.error(ctx, getResources().getString(R.string.genericError));
				}

			}
			else {

				Toasty.error(ctx, getResources().getString(R.string.genericError));
			}

		}

	}

	@Override
	public void onBackPressed() {

		if(drawer.isDrawerOpen(GravityCompat.START)) {

			drawer.closeDrawer(GravityCompat.START);
		}
		else {

			super.onBackPressed();
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

		int id = menuItem.getItemId();

		if(id == R.id.nav_home) {

			toolbarTitle.setText(getResources().getString(R.string.navMyRepos));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRepositoriesFragment()).commit();
		}
		else if(id == R.id.nav_organizations) {

			toolbarTitle.setText(getResources().getString(R.string.navOrg));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new OrganizationsFragment()).commit();
		}
		else if(id == R.id.nav_profile) {
			toolbarTitle.setText(getResources().getString(R.string.navProfile));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyProfileFragment()).commit();
		}
		else if(id == R.id.nav_repositories) {

			toolbarTitle.setText(getResources().getString(R.string.navRepos));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new RepositoriesFragment()).commit();
		}
		else if(id == R.id.nav_settings) {

			toolbarTitle.setText(getResources().getString(R.string.navSettings));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SettingsFragment()).commit();
		}
		else if(id == R.id.nav_logout) {

			AppUtil.logout(ctx);
			overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		}
		else if(id == R.id.nav_starred_repos) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleStarredRepos));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StarredRepositoriesFragment()).commit();
		}
		else if(id == R.id.nav_explore) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleExplore));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExploreFragment()).commit();
		}
		else if(id == R.id.nav_notifications) {

			toolbarTitle.setText(R.string.pageTitleNotifications);
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new NotificationsFragment()).commit();
		}
		else if(id == R.id.nav_comments_draft) {

			toolbarTitle.setText(getResources().getString(R.string.titleDrafts));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new DraftsFragment()).commit();
		}
		else if(id == R.id.nav_administration) {

			toolbarTitle.setText(getResources().getString(R.string.pageTitleAdministration));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AdministrationFragment()).commit();
		}
		else if(id == R.id.nav_my_issues) {

			toolbarTitle.setText(getResources().getString(R.string.navMyIssues));
			getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyIssuesFragment()).commit();
		}

		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if(id == R.id.genericMenu) {

			BottomSheetDraftsFragment bottomSheet = new BottomSheetDraftsFragment();
			bottomSheet.show(getSupportFragmentManager(), "draftsBottomSheet");
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void giteaVersion() {

		Call<GiteaVersion> callVersion = RetrofitClient.getApiInterface(ctx).getGiteaVersionWithToken(getAccount().getAuthorization());
		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200 && responseVersion.body() != null) {
					String version = responseVersion.body().getVersion();

					BaseApi.getInstance(ctx, UserAccountsApi.class).updateServerVersion(version, tinyDB.getInt("currentActiveAccountId"));
					getAccount().setAccount(BaseApi.getInstance(ctx, UserAccountsApi.class).getAccountById(tinyDB.getInt("currentActiveAccountId")));
				}
			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {
				Log.e("onFailure-version", t.toString());
			}
		});
	}

	private void loadUserInfo() {
		Call<UserInfo> call = RetrofitClient.getApiInterface(ctx).getUserInfo(getAccount().getAuthorization());

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						assert userDetails != null;

						getAccount().setUserInfo(userDetails);
						navigationView.getMenu().findItem(R.id.nav_administration).setVisible(userDetails.getIs_admin());
						if(!getAccount().getAccount().getUserName().equals(userDetails.getUsername())) {
							// user changed it's name -> update database
							int accountId = getAccount().getAccount().getAccountId();
							BaseApi.getInstance(MainActivity.this, UserAccountsApi.class).updateUsername(accountId,
								userDetails.getUsername());
							getAccount().setAccount(BaseApi.getInstance(MainActivity.this, UserAccountsApi.class).getAccountById(accountId));
						}
						if(profileInitListener != null) profileInitListener.onButtonClicked(null);
					}
				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.cancelButton), getResources().getString(R.string.navLogout));
				}
				else {

					String toastError = getResources().getString(R.string.genericApiError, response.code());
					Toasty.error(ctx, toastError);
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void getNotificationsCount(String token) {

		Call<NotificationCount> call = RetrofitClient.getApiInterface(ctx).checkUnreadNotifications(token);

		call.enqueue(new Callback<NotificationCount>() {

			@Override
			public void onResponse(@NonNull Call<NotificationCount> call, @NonNull retrofit2.Response<NotificationCount> response) {

				NotificationCount notificationCount = response.body();

				if(response.code() == 200) {

					assert notificationCount != null;
					notificationCounter = navNotifications.getActionView().findViewById(R.id.counterBadgeNotification);
					notificationCounter.setText(String.valueOf(notificationCount.getCounter()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<NotificationCount> call, @NonNull Throwable t) {

				Log.e("onFailure-notification", t.toString());
			}
		});
	}

	public void setProfileInitListener(BottomSheetListener profileInitListener) {

		this.profileInitListener = profileInitListener;
	}

}
