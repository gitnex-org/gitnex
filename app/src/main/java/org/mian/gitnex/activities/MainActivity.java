package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.button.MaterialButton;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.GeneralAPISettings;
import org.gitnex.tea4j.v2.models.GeneralAttachmentSettings;
import org.gitnex.tea4j.v2.models.NotificationCount;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityMainBinding;
import org.mian.gitnex.databinding.FragmentHomeDashboardBinding;
import org.mian.gitnex.fragments.HomeDashboardFragment;
import org.mian.gitnex.fragments.NotificationsFragment;
import org.mian.gitnex.fragments.RepositoriesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BadgeHelper;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.notifications.NotificationsBadge;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MainActivity extends BaseActivity
		implements NotificationsFragment.NotificationCountListener {

	private static final String STATE_ACTIVE_TAB = "active_tab";
	private static final String TAB_HOME = "home";
	private static final String TAB_REPOS = "repos";
	private static final String TAB_NOTIFICATIONS = "notifications";

	public ActivityMainBinding binding;
	private TinyDB tinyDB;
	public static boolean reloadRepos;
	public static boolean closeActivity;
	private final Fragment homeFrag = new HomeDashboardFragment();
	private final Fragment repoFrag = new RepositoriesFragment();
	private final Fragment notifyFrag = new NotificationsFragment();
	private Fragment activeFragment = homeFrag;
	private String currentActiveTab = TAB_HOME;
	private final FragmentManager fm = getSupportFragmentManager();
	private View detachedDivider;
	private View detachedAddBtn;
	private View detachedSearchBtn;
	private View detachedSortBtn;
	private BadgeDrawable notificationBadge;

	public interface UserInfoCallback {
		void onUserInfoLoaded(
				String username,
				boolean isAdmin,
				String serverVersion,
				long followers,
				long following,
				long uid);

		void onUserAccountsLoaded();
	}

	@Override
	public void onUpdateNotificationActionVisibility(boolean visible) {
		if (activeFragment instanceof NotificationsFragment) {
			updateContextualDockActions(R.id.btn_nav_notifications);
		}
	}

	@Override
	public void onNotificationsMarkedRead() {
		getNotificationsCount();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_ACTIVE_TAB, currentActiveTab);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		tinyDB = TinyDB.getInstance(this);

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		detachedDivider = binding.dockRepoDivider;
		detachedAddBtn = binding.btnDockNewRepo;
		detachedSearchBtn = binding.btnDockSearch;
		detachedSortBtn = binding.btnDockSort;

		if (savedInstanceState != null) {
			currentActiveTab = savedInstanceState.getString(STATE_ACTIVE_TAB, TAB_HOME);
		}

		if (handleAccountSetup()) return;

		setupFragments();
		setupDockListeners();
		loadInitialState(savedInstanceState);
		handleLaunchIntent(getIntent());

		getOnBackPressedDispatcher()
				.addCallback(
						this,
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								if (activeFragment != homeFrag) {
									switchTab(homeFrag, R.id.btn_nav_home);
								} else {
									finish();
								}
							}
						});
	}

	@Override
	protected void onGlobalRefresh() {
		RepositoriesFragment fragment =
				(RepositoriesFragment) getSupportFragmentManager().findFragmentByTag(TAB_REPOS);
		if (fragment != null) fragment.refreshFromGlobal();
	}

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.nav_host_fragment, notifyFrag, TAB_NOTIFICATIONS)
				.hide(notifyFrag)
				.add(R.id.nav_host_fragment, repoFrag, TAB_REPOS)
				.hide(repoFrag)
				.add(R.id.nav_host_fragment, homeFrag, TAB_HOME)
				.hide(homeFrag)
				.commitNow();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		handleLaunchIntent(intent);
	}

	private void handleLaunchIntent(Intent intent) {
		if (intent == null) return;

		if (intent.hasExtra("switchAccountId")) {
			int targetAccountId = intent.getIntExtra("switchAccountId", -1);
			int currentAccountId = tinyDB.getInt("currentActiveAccountId", -1);

			if (targetAccountId != -1 && targetAccountId != currentAccountId) {
				UserAccountsApi api = BaseApi.getInstance(this, UserAccountsApi.class);
				if (api != null) {
					UserAccount targetAccount = api.getAccountById(targetAccountId);
					if (targetAccount != null && AppUtil.switchToAccount(this, targetAccount)) {
						intent.removeExtra("switchAccountId");
						recreate();
						return;
					}
				}
			}
		}

		String launch = intent.getStringExtra("launchFragment");
		String link = intent.getStringExtra("launchFragmentByLink");

		if ("notifications".equals(launch) || "notification".equals(link)) {
			updateDockUI(R.id.btn_nav_notifications);

			fm.beginTransaction().hide(activeFragment).show(notifyFrag).commitNow();

			activeFragment = notifyFrag;
			currentActiveTab = TAB_NOTIFICATIONS;

			if (notifyFrag instanceof NotificationsFragment nf) {
				nf.forceUnreadFilter();
			}

			intent.removeExtra("launchFragment");
			intent.removeExtra("launchFragmentByLink");
		}
	}

	private void setupDockListeners() {
		prepareNavButton(binding.btnNavHome);
		prepareNavButton(binding.btnNavRepos);
		prepareNavButton(binding.btnNavNotifications);

		binding.btnNavHome.setOnClickListener(v -> switchTab(homeFrag, R.id.btn_nav_home));
		binding.btnNavRepos.setOnClickListener(v -> switchTab(repoFrag, R.id.btn_nav_repos));
		binding.btnNavNotifications.setOnClickListener(
				v -> switchTab(notifyFrag, R.id.btn_nav_notifications));

		binding.btnDockNewRepo.setOnClickListener(
				v -> {
					if (activeFragment instanceof RepositoriesFragment rf) {
						rf.createNewRepo();
					}
				});

		binding.btnDockSearch.setOnClickListener(
				v -> {
					if (activeFragment instanceof RepositoriesFragment rf) {
						rf.toggleSearch();
					}
				});

		binding.btnDockSort.setOnClickListener(
				v -> {
					if (activeFragment instanceof RepositoriesFragment rf) {
						rf.openSortMenu();
					} else if (activeFragment instanceof NotificationsFragment nf) {
						nf.openNotificationMenu();
					}
				});
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn) {
		btn.setSelected(true);
		if (btn.getBackground() != null) {
			btn.getBackground().setAlpha(255);
		}
	}

	private void resetPill(MaterialButton btn) {
		btn.setSelected(false);
		if (btn.getBackground() != null) {
			btn.getBackground().setAlpha(0);
		}
	}

	private void switchTab(Fragment target, int btnId) {
		if (target.isVisible() && activeFragment == target) {
			if (target instanceof NotificationsFragment nf) {
				nf.refreshData();
			}
			updateDockUI(btnId);
			return;
		}

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;

		if (target == homeFrag) {
			currentActiveTab = TAB_HOME;
		} else if (target == repoFrag) {
			currentActiveTab = TAB_REPOS;
		} else if (target == notifyFrag) {
			currentActiveTab = TAB_NOTIFICATIONS;
		}

		updateDockUI(btnId);
	}

	private void updateDockUI(int activeBtnId) {
		resetPill(binding.btnNavHome);
		resetPill(binding.btnNavRepos);
		resetPill(binding.btnNavNotifications);

		if (activeBtnId == R.id.btn_nav_home) {
			activatePill(binding.btnNavHome);
		} else if (activeBtnId == R.id.btn_nav_repos) {
			activatePill(binding.btnNavRepos);
		} else if (activeBtnId == R.id.btn_nav_notifications) {
			activatePill(binding.btnNavNotifications);
		}

		updateContextualDockActions(activeBtnId);
	}

	private void updateContextualDockActions(int activeBtnId) {
		ViewGroup parent = binding.dockContainer;

		parent.removeView(detachedDivider);
		parent.removeView(detachedAddBtn);
		parent.removeView(detachedSearchBtn);
		parent.removeView(detachedSortBtn);

		int currentCount =
				NotificationsBadge.getBadgeCount(this, tinyDB.getInt("currentActiveAccountId"));
		int badgeMargin =
				(currentCount > 0)
						? getResources().getDimensionPixelSize(R.dimen.dimen8dp)
						: getResources().getDimensionPixelSize(R.dimen.dimen12dp);

		if (activeBtnId == R.id.btn_nav_repos) {
			parent.addView(detachedDivider);
			setDividerMargin(badgeMargin);

			parent.addView(detachedAddBtn);
			parent.addView(detachedSearchBtn);
			parent.addView(detachedSortBtn);

		} else if (activeBtnId == R.id.btn_nav_notifications) {
			parent.addView(detachedDivider);
			setDividerMargin(badgeMargin);
			parent.addView(detachedSortBtn);
		}

		binding.dockedToolbar.requestLayout();
	}

	private void setDividerMargin(int margin) {
		ViewGroup.MarginLayoutParams params =
				(ViewGroup.MarginLayoutParams) detachedDivider.getLayoutParams();
		params.setMarginStart(margin);
		detachedDivider.setLayoutParams(params);
	}

	private boolean handleAccountSetup() {
		Intent intent = getIntent();
		if (intent != null && intent.hasExtra("switchAccountId")) {
			int targetId = intent.getIntExtra("switchAccountId", -1);
			int currentId = tinyDB.getInt("currentActiveAccountId", -1);

			if (targetId != -1 && targetId != currentId) {
				UserAccountsApi api = BaseApi.getInstance(this, UserAccountsApi.class);
				if (api != null && AppUtil.switchToAccount(this, api.getAccountById(targetId))) {
					intent.removeExtra("switchAccountId");
					recreate();
					return true;
				}
			}
		}

		if (tinyDB.getInt("currentActiveAccountId", -1) <= 0) {
			AppUtil.logout(this);
			return true;
		}
		return false;
	}

	private void loadInitialState(Bundle savedInstanceState) {
		loadSavedBadgeCount();
		if (Boolean.parseBoolean(
				AppDatabaseSettings.getSettingsValue(
						this, AppDatabaseSettings.APP_NOTIFICATIONS_KEY))) {
			Notifications.startBadgeWorker(this);
		}
		getNotificationsCount();

		new Handler(Looper.getMainLooper())
				.postDelayed(
						() -> {
							if (AppUtil.hasNetworkConnection(this)) {
								giteaVersion();
								serverPageLimitSettings();
								updateGeneralAttachmentSettings();
							} else {
								Toasty.show(this, getString(R.string.checkNetConnection));
							}
						},
						1500);

		int versionCode = AppUtil.getAppBuildNo(this);
		if (versionCode > tinyDB.getInt("versionCode")) {
			tinyDB.putInt("versionCode", versionCode);
			new ChangeLog(this).showDialog();
		}

		boolean isRestoring = savedInstanceState != null;

		if (isRestoring) {
			restoreFromSavedTab();
		} else {
			String linkHandlerExtra = getIntent().getStringExtra("launchFragmentByLinkHandler");
			if (linkHandlerExtra != null) {
				handleDeepLinkNavigation(linkHandlerExtra);
			} else {
				handleHomeScreenSettingNavigation();
			}
		}
	}

	private void restoreFromSavedTab() {
		switch (currentActiveTab) {
			case TAB_REPOS:
				fm.beginTransaction().hide(homeFrag).hide(notifyFrag).show(repoFrag).commitNow();
				activeFragment = repoFrag;
				updateDockUI(R.id.btn_nav_repos);
				break;
			case TAB_NOTIFICATIONS:
				fm.beginTransaction().hide(homeFrag).hide(repoFrag).show(notifyFrag).commitNow();
				activeFragment = notifyFrag;
				updateDockUI(R.id.btn_nav_notifications);
				break;
			case TAB_HOME:
			default:
				activeFragment = homeFrag;
				updateDockUI(R.id.btn_nav_home);
				break;
		}
	}

	private void handleDeepLinkNavigation(String destination) {
		switch (destination) {
			case "repos":
				switchTab(repoFrag, R.id.btn_nav_repos);
				currentActiveTab = TAB_REPOS;
				break;
			case "notification":
				switchTab(notifyFrag, R.id.btn_nav_notifications);
				currentActiveTab = TAB_NOTIFICATIONS;
				break;
			case "home":
			default:
				updateDockUI(R.id.btn_nav_home);
				currentActiveTab = TAB_HOME;
				break;
		}
	}

	private void handleHomeScreenSettingNavigation() {
		int val = 0;
		try {
			String savedValue =
					AppDatabaseSettings.getSettingsValue(
							this, AppDatabaseSettings.APP_HOME_SCREEN_KEY);
			val = Integer.parseInt(savedValue);
		} catch (Exception ignored) {
		}

		switch (val) {
			case 1:
				switchTab(repoFrag, R.id.btn_nav_repos);
				currentActiveTab = TAB_REPOS;
				break;
			case 2:
				switchTab(notifyFrag, R.id.btn_nav_notifications);
				currentActiveTab = TAB_NOTIFICATIONS;
				break;
			case 0:
			default:
				updateDockUI(R.id.btn_nav_home);
				currentActiveTab = TAB_HOME;
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (closeActivity) finishAndRemoveTask();
		if (reloadRepos && activeFragment == repoFrag) {
			((RepositoriesFragment) repoFrag).refreshData();
			reloadRepos = false;
		}
		getNotificationsCount();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void loadUserInfo(
			HomeDashboardFragment fragment,
			FragmentHomeDashboardBinding b,
			List<UserAccount> userAccountsList,
			UserInfoCallback callback) {
		Call<User> call = RetrofitClient.getApiInterface(this).userGetCurrent();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull Response<User> response) {
						if (fragment != null && !fragment.isAdded()) return;
						if (b != null && userAccountsList != null)
							loadUserAccounts(fragment, b, userAccountsList, callback);

						User userDetails = response.body();
						if (response.isSuccessful()
								&& response.code() == 200
								&& userDetails != null) {
							updateUserUI(b, userDetails);
							fetchServerVersion(
									fragment,
									userDetails.getLogin(),
									userDetails.isIsAdmin(),
									userDetails.getFollowersCount(),
									userDetails.getFollowingCount(),
									userDetails.getId(),
									callback);
						} else if (response.code() == 401) {
							AlertDialogs.authorizationTokenRevokedDialog(MainActivity.this);
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						if (callback != null) callback.onUserAccountsLoaded();
					}
				});
	}

	private void updateUserUI(FragmentHomeDashboardBinding b, User userDetails) {
		int accountId = tinyDB.getInt("currentActiveAccountId");
		UserAccountsApi userApi = BaseApi.getInstance(this, UserAccountsApi.class);
		if (userApi == null) return;

		if (!userApi.getAccountById(accountId).getUserName().equals(userDetails.getLogin())) {
			userApi.updateUsername(accountId, userDetails.getLogin());
		}

		if (b != null) {
			b.userFullname.setText(
					userDetails.getFullName() != null && !userDetails.getFullName().isEmpty()
							? HtmlCompat.fromHtml(
									userDetails.getFullName(), HtmlCompat.FROM_HTML_MODE_LEGACY)
							: userDetails.getLogin());

			boolean hideEmail =
					Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									this, AppDatabaseSettings.APP_USER_HIDE_EMAIL_IN_NAV_KEY));
			b.userEmail.setVisibility(hideEmail ? View.GONE : View.VISIBLE);
			b.userEmail.setText(userDetails.getEmail() != null ? userDetails.getEmail() : "");

			Glide.with(this)
					.load(userDetails.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(b.userAvatar);
		}
	}

	private void loadUserAccounts(
			HomeDashboardFragment fragment,
			FragmentHomeDashboardBinding b,
			List<UserAccount> list,
			UserInfoCallback cb) {
		UserAccountsApi api = BaseApi.getInstance(this, UserAccountsApi.class);
		if (api == null || fragment == null) return;
		api.getAllAccounts()
				.observe(
						fragment.getViewLifecycleOwner(),
						accounts -> {
							if (accounts != null) {
								list.clear();
								list.addAll(accounts);
							}
							if (cb != null) cb.onUserAccountsLoaded();
						});
	}

	private void fetchServerVersion(
			HomeDashboardFragment fragment,
			String username,
			boolean isAdmin,
			long followers,
			long following,
			long uid,
			UserInfoCallback callback) {
		RetrofitClient.getApiInterface(this)
				.getVersion()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ServerVersion> call,
									@NonNull Response<ServerVersion> response) {
								if (response.isSuccessful() && response.body() != null) {
									String version = response.body().getVersion();
									tinyDB.putString("serverVersion", version);
									if (callback != null)
										callback.onUserInfoLoaded(
												username, isAdmin, version, followers, following,
												uid);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<ServerVersion> call, @NonNull Throwable t) {}
						});
	}

	public void getNotificationsCount() {
		RetrofitClient.getApiInterface(this)
				.notifyNewAvailable()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<NotificationCount> call,
									@NonNull Response<NotificationCount> response) {
								if (response.isSuccessful() && response.body() != null) {
									int count = Math.toIntExact(response.body().getNew());

									NotificationsBadge.saveBadgeCount(
											MainActivity.this,
											tinyDB.getInt("currentActiveAccountId"),
											count);

									updateBadgeUI(count);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<NotificationCount> call, @NonNull Throwable t) {}
						});
	}

	private void updateBadgeUI(int count) {
		runOnUiThread(
				() -> {
					notificationBadge =
							BadgeHelper.updateBadge(
									this, binding.btnNavNotifications, notificationBadge, count);

					if (activeFragment == notifyFrag || activeFragment == repoFrag) {
						updateContextualDockActions(
								activeFragment == notifyFrag
										? R.id.btn_nav_notifications
										: R.id.btn_nav_repos);
					}
				});
	}

	private void loadSavedBadgeCount() {
		int count = NotificationsBadge.getBadgeCount(this, tinyDB.getInt("currentActiveAccountId"));
		if (count > 0) {
			updateBadgeUI(count);
		}
	}

	public void giteaVersion() {
		RetrofitClient.getApiInterface(this)
				.getVersion()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ServerVersion> call,
									@NonNull Response<ServerVersion> response) {
								if (response.isSuccessful() && response.body() != null) {
									Objects.requireNonNull(
													BaseApi.getInstance(
															MainActivity.this,
															UserAccountsApi.class))
											.updateServerVersion(
													response.body().getVersion(),
													tinyDB.getInt("currentActiveAccountId"));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<ServerVersion> call, @NonNull Throwable t) {}
						});
	}

	public void serverPageLimitSettings() {
		RetrofitClient.getApiInterface(this)
				.getGeneralAPISettings()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<GeneralAPISettings> call,
									@NonNull Response<GeneralAPISettings> response) {
								if (response.isSuccessful() && response.body() != null) {
									int max =
											response.body().getMaxResponseItems() != null
													? Math.toIntExact(
															response.body().getMaxResponseItems())
													: 50;
									int def =
											response.body().getDefaultPagingNum() != null
													? Math.toIntExact(
															response.body().getDefaultPagingNum())
													: 25;
									Objects.requireNonNull(
													BaseApi.getInstance(
															MainActivity.this,
															UserAccountsApi.class))
											.updateServerPagingLimit(
													max,
													def,
													tinyDB.getInt("currentActiveAccountId"));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<GeneralAPISettings> call, @NonNull Throwable t) {}
						});
	}

	public void updateGeneralAttachmentSettings() {
		RetrofitClient.getApiInterface(this)
				.getGeneralAttachmentSettings()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<GeneralAttachmentSettings> call,
									@NonNull Response<GeneralAttachmentSettings> response) {
								if (response.isSuccessful() && response.body() != null) {
									int size =
											response.body().getMaxSize() != null
													? Math.toIntExact(response.body().getMaxSize())
													: 2;
									int files =
											response.body().getMaxFiles() != null
													? Math.toIntExact(response.body().getMaxFiles())
													: 5;
									Objects.requireNonNull(
													BaseApi.getInstance(
															MainActivity.this,
															UserAccountsApi.class))
											.updateGeneralAttachmentSettings(
													size,
													files,
													tinyDB.getInt("currentActiveAccountId"));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<GeneralAttachmentSettings> call,
									@NonNull Throwable t) {}
						});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Notifications.stopBadgeWorker(this);
	}
}
