package org.mian.gitnex.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.NotificationsActions;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author opyale
 * Modified M M Arif
 */

public class NotificationsFragment extends Fragment implements NotificationsAdapter.OnNotificationClickedListener, NotificationsAdapter.OnMoreClickedListener, BottomSheetNotificationsFragment.OnOptionSelectedListener {

	private FragmentNotificationsBinding viewBinding;
	private List<NotificationThread> notificationThreads;
	private NotificationsAdapter notificationsAdapter;
	private NotificationsActions notificationsActions;

	private Activity activity;
	private Context context;
	private TinyDB tinyDB;
	private Menu menu;

	private int resultLimit;
	private int pageSize;
	private String currentFilterMode = "unread";
	private final String TAG = Constants.tagNotifications;
	private String instanceToken;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		viewBinding = FragmentNotificationsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		activity = requireActivity();
		context = getContext();
		tinyDB = TinyDB.getInstance(context);

		String loginUid = tinyDB.getString("loginUid");
		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		resultLimit = Constants.getCurrentResultLimit(context);
		tinyDB.putString("notificationsFilterState", currentFilterMode);

		notificationThreads = new ArrayList<>();
		notificationsActions = new NotificationsActions(context);
		notificationsAdapter = new NotificationsAdapter(context, notificationThreads, this, this);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.notifications.getContext(), DividerItemDecoration.VERTICAL);

		viewBinding.notifications.setHasFixedSize(true);
		viewBinding.notifications.setLayoutManager(linearLayoutManager);
		viewBinding.notifications.setAdapter(notificationsAdapter);
		viewBinding.notifications.addItemDecoration(dividerItemDecoration);

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial(resultLimit);
			notificationsAdapter.notifyDataChanged();
		}, 200));

		notificationsAdapter.setLoadMoreListener(() -> viewBinding.notifications.post(() -> {
			if(notificationThreads.size() == resultLimit || pageSize == resultLimit) {
				int page = (notificationThreads.size() + resultLimit) / resultLimit;
				loadMore(resultLimit, page);
			}
		}));

		viewBinding.notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				if(currentFilterMode.equalsIgnoreCase("unread")) {
					if(dy > 0 && viewBinding.markAllAsRead.isShown()) {
						viewBinding.markAllAsRead.setVisibility(View.GONE);
					}
					else if(dy < 0) {
						viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
					}
				}
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		viewBinding.markAllAsRead.setOnClickListener(v1 -> {

			Thread thread = new Thread(() -> {
				try {
					if(notificationsActions.setAllNotificationsRead(new Date())) {
						activity.runOnUiThread(() -> {
							Toasty.success(context, getString(R.string.markedNotificationsAsRead));
							loadInitial(resultLimit);
						});
					}
				}
				catch(IOException e) {
					activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					Log.e("onError", e.toString());
				}
			});
			thread.start();
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> {
			loadInitial(resultLimit);
		});

		loadInitial(resultLimit);
		return viewBinding.getRoot();
	}

	private void loadInitial(int resultLimit) {

		notificationThreads.clear();
		notificationsAdapter.notifyDataChanged();
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		notificationThreads.clear();
		String[] filter = tinyDB.getString("notificationsFilterState").equals("read") ?
			new String[]{"pinned", "read"} :
			new String[]{"pinned", "unread"};
		viewBinding.pullToRefresh.setRefreshing(false);

		Call<List<NotificationThread>> call = RetrofitClient
			.getApiInterface(context)
			.getNotificationThreads(instanceToken, false, filter,
				Constants.defaultOldestTimestamp, "",
				1, resultLimit);
		call.enqueue(new Callback<List<NotificationThread>>() {
			@Override
			public void onResponse(@NonNull Call<List<NotificationThread>> call, @NonNull Response<List<NotificationThread>> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().size() > 0) {
						notificationThreads.addAll(response.body());
						notificationsAdapter.notifyDataChanged();
						viewBinding.noDataNotifications.setVisibility(View.GONE);
					}
					else {
						notificationsAdapter.notifyDataChanged();
						viewBinding.noDataNotifications.setVisibility(View.VISIBLE);
					}
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					viewBinding.noDataNotifications.setVisibility(View.VISIBLE);
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					notificationsAdapter.notifyDataChanged();
					Log.e(TAG, String.valueOf(response.code()));
				}
				onCleanup();
			}

			@Override
			public void onFailure(@NonNull Call<List<NotificationThread>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
				onCleanup();
			}
		});
	}

	private void loadMore(int resultLimit, int page) {

		String[] filter = tinyDB.getString("notificationsFilterState").equals("read") ?
			new String[]{"pinned", "read"} :
			new String[]{"pinned", "unread"};

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<List<NotificationThread>> call = RetrofitClient.getApiInterface(context)
			.getNotificationThreads(instanceToken, false, filter,
				Constants.defaultOldestTimestamp, "",
				page, resultLimit);
		call.enqueue(new Callback<List<NotificationThread>>() {
			@Override
			public void onResponse(@NonNull Call<List<NotificationThread>> call, @NonNull Response<List<NotificationThread>> response) {
				if(response.code() == 200) {
					assert response.body() != null;
					List<NotificationThread> result = response.body();

					if(result.size() > 0) {
						pageSize = result.size();
						notificationThreads.addAll(result);
					}
					else {
						SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
						notificationsAdapter.setMoreDataAvailable(false);
					}
					notificationsAdapter.notifyDataChanged();
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
				onCleanup();
			}

			@Override
			public void onFailure(@NonNull Call<List<NotificationThread>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
				onCleanup();
			}
		});
	}

	private void onCleanup() {

		AppUtil.setMultiVisibility(View.GONE, viewBinding.progressBar, viewBinding.progressBar);
		viewBinding.pullToRefresh.setRefreshing(false);

		if(currentFilterMode.equalsIgnoreCase("unread")) {

			if(notificationThreads.isEmpty()) {
				viewBinding.noDataNotifications.setVisibility(View.VISIBLE);
				viewBinding.markAllAsRead.setVisibility(View.GONE);
			}
			else {
				viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
			}
		}
	}

	private void changeFilterMode() {

		int filterIcon = currentFilterMode.equalsIgnoreCase("read") ?
			R.drawable.ic_filter_closed :
			R.drawable.ic_filter;

		menu.getItem(0).setIcon(filterIcon);

		if(currentFilterMode.equalsIgnoreCase("read")) {
			viewBinding.markAllAsRead.setVisibility(View.GONE);
		}
		else {
			viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		this.menu = menu;
		inflater.inflate(R.menu.filter_menu_notifications, menu);
		currentFilterMode = tinyDB.getString("notificationsFilterState");
		changeFilterMode();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		if(item.getItemId() == R.id.filterNotifications) {

			BottomSheetNotificationsFilterFragment bottomSheetNotificationsFilterFragment = new BottomSheetNotificationsFilterFragment();
			bottomSheetNotificationsFilterFragment.show(getChildFragmentManager(), "notificationsFilterBottomSheet");
			bottomSheetNotificationsFilterFragment.setOnDismissedListener(() -> {

				currentFilterMode = tinyDB.getString("notificationsFilterState");
				changeFilterMode();
				loadInitial(resultLimit);
			});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNotificationClicked(NotificationThread notificationThread) {

		Thread thread = new Thread(() -> {
			try {
				if(notificationThread.isUnread()) {
					notificationsActions.setNotificationStatus(notificationThread, NotificationsActions.NotificationStatus.READ);
					activity.runOnUiThread(() -> loadInitial(resultLimit));
				}
			} catch(IOException ignored) {}
		});

		thread.start();

		if(StringUtils.containsAny(notificationThread.getSubject().getType().toLowerCase(), "pull", "issue")) {

			Intent intent = new Intent(context, IssueDetailActivity.class);
			String issueUrl = notificationThread.getSubject().getUrl();
			tinyDB.putString("issueNumber", issueUrl.substring(issueUrl.lastIndexOf("/") + 1));
			tinyDB.putString("issueType", notificationThread.getSubject().getType());
			tinyDB.putString("repoFullName", notificationThread.getRepository().getFullName());

			startActivity(intent);
		}
	}

	@Override
	public void onMoreClicked(NotificationThread notificationThread) {
		BottomSheetNotificationsFragment bottomSheetNotificationsFragment = new BottomSheetNotificationsFragment();
		bottomSheetNotificationsFragment.onAttach(context, notificationThread, this);
		bottomSheetNotificationsFragment.show(getChildFragmentManager(), "notificationsBottomSheet");
	}

	@Override
	public void onSelected() {
		loadInitial(resultLimit);
	}
}
