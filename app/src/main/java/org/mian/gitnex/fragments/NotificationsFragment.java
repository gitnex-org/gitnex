package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.databinding.FragmentNotificationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.NotificationsViewModel;

/**
 * @author opyale
 * @author mmarif
 */
public class NotificationsFragment extends Fragment
		implements NotificationsAdapter.OnNotificationClickedListener,
				NotificationsAdapter.OnMoreClickedListener {

	private FragmentNotificationsBinding binding;
	private NotificationsAdapter adapter;
	private NotificationsViewModel viewModel;
	private EndlessRecyclerViewScrollListener scrollListener;
	private Context context;
	private int pageResultLimit;
	private String currentFilterMode = "unread";
	private NotificationCountListener notificationCountListener;
	private boolean isViewCreated = false;

	public void forceUnreadFilter() {
		this.currentFilterMode = "unread";

		if (viewModel != null) {
			viewModel.resetPagination();
		}

		if (adapter != null) {
			adapter.updateList(new ArrayList<>());
		}

		new Handler(Looper.getMainLooper())
				.post(
						() -> {
							if (isAdded()) {
								refreshData();
							}
						});
	}

	public interface NotificationCountListener {
		void onNotificationsMarkedRead();

		void onUpdateNotificationActionVisibility(boolean visible);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentNotificationsBinding.inflate(inflater, container, false);
		context = requireContext();
		pageResultLimit = Constants.getCurrentResultLimit(context);

		if (getActivity() instanceof AppCompatActivity activity
				&& activity.getSupportActionBar() != null) {
			activity.getSupportActionBar().hide();
		}

		viewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

		setupRecyclerView();
		setupRefreshLayout();
		observeViewModel();

		isViewCreated = true;
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.notifications, binding.pullToRefresh, null);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden()) {
			refreshDataOnFocus();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isViewCreated) {
			refreshDataOnFocus();
		}
	}

	private void refreshDataOnFocus() {
		refreshData();
		if (notificationCountListener != null) {
			notificationCountListener.onNotificationsMarkedRead();
		}
	}

	private void setupRecyclerView() {
		adapter = new NotificationsAdapter(context, this, this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(context);
		binding.notifications.setLayoutManager(layoutManager);
		binding.notifications.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (viewModel.canLoadMore()) {
							viewModel.fetchNotifications(
									context, currentFilterMode, page, pageResultLimit, false);
						}
					}
				};
		binding.notifications.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getNotifications()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiState(list);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getHasLoadedOnce()
				.observe(
						getViewLifecycleOwner(),
						hasLoaded -> {
							updateUiState(viewModel.getNotifications().getValue());
						});

		viewModel
				.getActionSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (success != null && success) {
								refreshData();
								if (notificationCountListener != null) {
									notificationCountListener.onNotificationsMarkedRead();
								}
								viewModel.resetActionStatus();
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(context, error);
						});
	}

	private void updateUiState(List<NotificationThread> list) {
		boolean isEmpty = (list == null || list.isEmpty());
		Boolean hasLoaded = viewModel.getHasLoadedOnce().getValue();
		boolean loaded = (hasLoaded != null && hasLoaded);

		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
		binding.notifications.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

		boolean canMarkRead = !isEmpty && "unread".equals(currentFilterMode);
		if (notificationCountListener != null) {
			notificationCountListener.onUpdateNotificationActionVisibility(canMarkRead);
		}
	}

	private void setupRefreshLayout() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	public void refreshData() {
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		if (scrollListener != null) {
			scrollListener.resetState();
		}
		viewModel.resetPagination();
		viewModel.fetchNotifications(context, currentFilterMode, 1, pageResultLimit, true);
	}

	public void openNotificationMenu() {
		List<NotificationThread> list = viewModel.getNotifications().getValue();
		boolean hasData = (list != null && !list.isEmpty());

		BottomSheetNotificationsFilter filterSheet =
				BottomSheetNotificationsFilter.newInstance(currentFilterMode, hasData);

		filterSheet.setListener(
				new BottomSheetNotificationsFilter.OnFilterChangedListener() {
					@Override
					public void onFilterChanged(String mode) {
						currentFilterMode = mode;
						refreshData();
					}

					@Override
					public void onMarkAllReadTriggered() {
						markAllAsRead();
					}
				});

		filterSheet.show(getChildFragmentManager(), "notificationFilter");
	}

	public void markAllAsRead() {
		viewModel.markAllAsRead(context);
	}

	@Override
	public void onNotificationClicked(NotificationThread thread) {
		if (thread.isUnread() && !thread.isPinned()) {
			viewModel.markThreadAsRead(context, thread.getId());
		}

		String type = thread.getSubject().getType().toLowerCase();
		if (Arrays.asList("pull", "issue").contains(type)) {
			handleIssuePrNavigation(thread);
		} else if (type.equalsIgnoreCase("repository")) {
			RepositoryContext repoContext = new RepositoryContext(thread.getRepository(), context);
			startActivity(repoContext.getIntent(context, RepoDetailActivity.class));
		}
	}

	private void handleIssuePrNavigation(NotificationThread thread) {
		RepositoryContext repo =
				new RepositoryContext(
						thread.getRepository().getOwner().getLogin(),
						thread.getRepository().getName(),
						context);
		repo.saveToDB(context);
		String url = thread.getSubject().getUrl();
		int id = Integer.parseInt(url.substring(url.lastIndexOf("/") + 1));
		Intent intent =
				new IssueContext(repo, id, thread.getSubject().getType())
						.getIntent(context, IssueDetailActivity.class);
		intent.putExtra("openedFromLink", "true");
		startActivity(intent);
	}

	@Override
	public void onMoreClicked(NotificationThread thread) {
		BottomSheetNotificationsFragment bottomSheet = new BottomSheetNotificationsFragment();
		bottomSheet.onAttach(thread);
		bottomSheet.show(getChildFragmentManager(), "notificationsBottomSheet");
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof NotificationCountListener) {
			notificationCountListener = (NotificationCountListener) context;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		isViewCreated = false;
		binding = null;
	}
}
