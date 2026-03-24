package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
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
import org.mian.gitnex.fragments.profile.DetailFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ChangeLog;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
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

	public ActivityMainBinding binding;
	private TinyDB tinyDB;
	private boolean noConnection;
	public static boolean reloadRepos;
	public static boolean closeActivity;
	private final Fragment homeFrag = new HomeDashboardFragment();
	private final Fragment repoFrag = new RepositoriesFragment();
	private final Fragment notifyFrag = new NotificationsFragment();
	private Fragment activeFragment = homeFrag;
	private final FragmentManager fm = getSupportFragmentManager();
	private boolean showMarkReadAction = false;
	private View detachedDivider;
	private View detachedAddBtn;
	private View detachedSearchBtn;
	private View detachedSortBtn;
	private View detachedMarkReadBtn;

	public interface UserInfoCallback {
		void onUserInfoLoaded(
				String username,
				boolean isAdmin,
				String serverVersion,
				long followers,
				long following);

		void onUserAccountsLoaded();
	}

	@Override
	public void onUpdateNotificationActionVisibility(boolean visible) {
		this.showMarkReadAction = visible;
		if (activeFragment == notifyFrag) {
			updateContextualDockActions(R.id.btn_nav_notifications);
		}
	}

	@Override
	public void onNotificationsMarkedRead() {
		getNotificationsCount();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EdgeToEdge.enable(this);

		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		tinyDB = TinyDB.getInstance(this);

		detachedDivider = binding.dockRepoDivider;
		detachedAddBtn = binding.btnDockNewRepo;
		detachedSearchBtn = binding.btnDockSearch;
		detachedSortBtn = binding.btnDockSort;
		detachedMarkReadBtn = binding.btnDockMarkRead;

		if (handleAccountSetup()) return;

		setupFragments();
		setupDockListeners();

		loadInitialState(savedInstanceState);

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

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.nav_host_fragment, notifyFrag, "3")
				.hide(notifyFrag)
				.add(R.id.nav_host_fragment, repoFrag, "2")
				.hide(repoFrag)
				.add(R.id.nav_host_fragment, homeFrag, "1")
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
						recreate();
						return;
					}
				}
			}
		}

		String launch = intent.getStringExtra("launchFragment");
		String link = intent.getStringExtra("launchFragmentByLink");

		if ("notifications".equals(launch) || "notification".equals(link)) {
			switchTab(notifyFrag, R.id.btn_nav_notifications);
		} else if ("repos".equals(link)) {
			switchTab(repoFrag, R.id.btn_nav_repos);
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

		binding.btnDockMarkRead.setOnClickListener(
				v -> {
					if (activeFragment instanceof NotificationsFragment nf) {
						nf.markAllAsRead();
					}
				});

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
			updateDockUI(btnId);
			return;
		}

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
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
		parent.removeView(detachedMarkReadBtn);

		int currentCount =
				NotificationsBadge.getBadgeCount(this, tinyDB.getInt("currentActiveAccountId"));
		int badgeMargin =
				(currentCount > 0)
						? getResources().getDimensionPixelSize(R.dimen.dimen16dp)
						: getResources().getDimensionPixelSize(R.dimen.dimen4dp);

		if (activeBtnId == R.id.btn_nav_repos) {
			parent.addView(detachedDivider);

			ViewGroup.MarginLayoutParams params =
					(ViewGroup.MarginLayoutParams) detachedDivider.getLayoutParams();
			params.setMarginStart(badgeMargin);
			detachedDivider.setLayoutParams(params);

			parent.addView(detachedAddBtn);
			parent.addView(detachedSearchBtn);
			parent.addView(detachedSortBtn);

		} else if (activeBtnId == R.id.btn_nav_notifications) {
			if (showMarkReadAction) {
				parent.addView(detachedDivider);

				ViewGroup.MarginLayoutParams params =
						(ViewGroup.MarginLayoutParams) detachedDivider.getLayoutParams();
				params.setMarginStart(badgeMargin);
				detachedDivider.setLayoutParams(params);

				parent.addView(detachedMarkReadBtn);
			}
		}

		binding.dockedToolbar.requestLayout();
	}

	private boolean handleAccountSetup() {
		Intent intent = getIntent();
		if (intent.hasExtra("switchAccountId")) {
			UserAccountsApi api = BaseApi.getInstance(this, UserAccountsApi.class);
			if (api != null
					&& AppUtil.switchToAccount(
							this, api.getAccountById(intent.getIntExtra("switchAccountId", 0)))) {
				recreate();
				return true;
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
								noConnection = true;
							}
						},
						1500);

		int versionCode = AppUtil.getAppBuildNo(this);
		if (versionCode > tinyDB.getInt("versionCode")) {
			tinyDB.putInt("versionCode", versionCode);
			new ChangeLog(this).showDialog();
		}

		if (savedInstanceState == null) {
			String linkHandlerExtra = getIntent().getStringExtra("launchFragmentByLinkHandler");

			if (linkHandlerExtra != null) {
				handleDeepLinkNavigation(linkHandlerExtra);
			} else {
				handleHomeScreenSettingNavigation();
			}
		} else {
			restoreFragmentState();
		}
	}

	private void handleDeepLinkNavigation(String destination) {
		switch (destination) {
			case "repos":
				switchTab(repoFrag, R.id.btn_nav_repos);
				break;
			case "notification":
				switchTab(notifyFrag, R.id.btn_nav_notifications);
				break;
			case "home":
			default:
				updateDockUI(R.id.btn_nav_home);
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
				break;
			case 2:
				switchTab(notifyFrag, R.id.btn_nav_notifications);
				break;
			case 0:
			default:
				updateDockUI(R.id.btn_nav_home);
				break;
		}
	}

	private void restoreFragmentState() {
		if (repoFrag != null && repoFrag.isVisible()) {
			activeFragment = repoFrag;
			updateDockUI(R.id.btn_nav_repos);
		} else if (notifyFrag != null && notifyFrag.isVisible()) {
			activeFragment = notifyFrag;
			updateDockUI(R.id.btn_nav_notifications);
		} else {
			activeFragment = homeFrag;
			updateDockUI(R.id.btn_nav_home);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (closeActivity) finishAndRemoveTask();
		if (DetailFragment.refProfile) {
			loadUserInfo(null, null, null, null);
			DetailFragment.refProfile = false;
		}
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
												username, isAdmin, version, followers, following);
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
								if (response.code() == 200 && response.body() != null) {
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

	private BadgeDrawable notificationBadge;

	@OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
	private void updateBadgeUI(int count) {
		runOnUiThread(
				() -> {
					if (count > 0) {
						if (notificationBadge == null) {
							notificationBadge = BadgeDrawable.create(this);
							notificationBadge.setBackgroundColor(
									getThemeColor(R.attr.primaryTextColor));
							notificationBadge.setBadgeTextColor(
									getThemeColor(R.attr.materialCardBackgroundColor));

							int offset = getResources().getDimensionPixelSize(R.dimen.dimen20dp);
							notificationBadge.setHorizontalOffset(offset);
							notificationBadge.setVerticalOffset(offset);

							binding.btnNavNotifications.post(
									() ->
											BadgeUtils.attachBadgeDrawable(
													notificationBadge,
													binding.btnNavNotifications,
													null));
						}
						notificationBadge.setNumber(count);
						notificationBadge.setVisible(true);
					} else if (notificationBadge != null) {
						notificationBadge.setVisible(false);
					}

					if (activeFragment == notifyFrag || activeFragment == repoFrag) {
						updateContextualDockActions(
								activeFragment == notifyFrag
										? R.id.btn_nav_notifications
										: R.id.btn_nav_repos);
					}
				});
	}

	private int getThemeColor(int attr) {
		android.util.TypedValue typedValue = new android.util.TypedValue();
		getTheme().resolveAttribute(attr, typedValue, true);
		return typedValue.data;
	}

	private void loadSavedBadgeCount() {
		int count = NotificationsBadge.getBadgeCount(this, tinyDB.getInt("currentActiveAccountId"));
		if (count > 0) updateBadgeUI(count);
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
