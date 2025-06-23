package org.mian.gitnex.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SimpleCallback;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author opyale
 * @author M M Arif
 */
public class NotificationsFragment extends Fragment
		implements NotificationsAdapter.OnNotificationClickedListener,
				NotificationsAdapter.OnMoreClickedListener {

	private final List<NotificationThread> notificationThreads = new ArrayList<>();
	private FragmentNotificationsBinding viewBinding;
	private NotificationsAdapter notificationsAdapter;
	private Activity activity;
	private Context context;
	private int pageCurrentIndex = 1;
	private int pageResultLimit;
	private String currentFilterMode = "unread";
	public static String emptyErrorResponse;
	private NotificationCountListener notificationCountListener;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	public interface NotificationCountListener {
		void onNotificationsMarkedRead();
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		viewBinding = FragmentNotificationsBinding.inflate(inflater, container, false);

		activity = requireActivity();
		context = getContext();

		pageResultLimit = Constants.getCurrentResultLimit(context);

		notificationsAdapter = new NotificationsAdapter(context, notificationThreads, this, this);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

		viewBinding.notifications.setHasFixedSize(true);
		viewBinding.notifications.setLayoutManager(linearLayoutManager);
		viewBinding.notifications.setAdapter(notificationsAdapter);

		viewBinding.notifications.addOnScrollListener(
				new RecyclerView.OnScrollListener() {

					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

						if (!recyclerView.canScrollVertically(1) && dy != 0) {
							pageCurrentIndex++;
							loadNotifications(true);
						}

						if (currentFilterMode.equalsIgnoreCase("unread")) {
							if (dy > 0 && viewBinding.markAllAsRead.isShown()) {
								viewBinding.markAllAsRead.setVisibility(View.GONE);
							} else if (dy < 0) {
								viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
							}
						}
					}

					@Override
					public void onScrollStateChanged(
							@NonNull RecyclerView recyclerView, int newState) {
						super.onScrollStateChanged(recyclerView, newState);
					}
				});

		viewBinding.markAllAsRead.setOnClickListener(
				v1 ->
						RetrofitClient.getApiInterface(context)
								.notifyReadList(
										null, "false", Arrays.asList("unread", "pinned"), "read")
								.enqueue(
										(SimpleCallback<List<NotificationThread>>)
												(call, voidResponse) -> {
													if (voidResponse.isPresent()
															&& voidResponse.get().isSuccessful()) {
														SnackBar.success(
																context,
																requireActivity()
																		.findViewById(
																				android.R.id
																						.content),
																getString(
																		R.string
																				.markedNotificationsAsRead));
														pageCurrentIndex = 1;
														loadNotifications(false);
														if (notificationCountListener != null) {
															notificationCountListener
																	.onNotificationsMarkedRead();
														}
													} else {
														if (emptyErrorResponse != null) {
															if (!emptyErrorResponse.isEmpty()) {
																if (emptyErrorResponse.contains(
																		"205")) {
																	SnackBar.success(
																			context,
																			requireActivity()
																					.findViewById(
																							android
																									.R
																									.id
																									.content),
																			getString(
																					R.string
																							.markedNotificationsAsRead));
																	pageCurrentIndex = 1;
																	loadNotifications(false);
																	if (notificationCountListener
																			!= null) {
																		notificationCountListener
																				.onNotificationsMarkedRead();
																	}
																}
															} else {
																activity.runOnUiThread(
																		() ->
																				SnackBar.error(
																						context,
																						requireActivity()
																								.findViewById(
																										android
																												.R
																												.id
																												.content),
																						getString(
																								R
																										.string
																										.genericError)));
															}
														}
													}
												}));

		viewBinding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewBinding.pullToRefresh.setRefreshing(false);
					pageCurrentIndex = 1;
					loadNotifications(false);
				});

		loadNotifications(true);

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {

							Menu menu;

							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

								this.menu = menu;
								menuInflater.inflate(R.menu.filter_menu_notifications, menu);

								int filterIcon =
										currentFilterMode.equalsIgnoreCase("read")
												? R.drawable.ic_filter_closed
												: R.drawable.ic_filter;

								menu.getItem(0).setIcon(filterIcon);

								if (currentFilterMode.equalsIgnoreCase("read")) {
									viewBinding.markAllAsRead.setVisibility(View.GONE);
								} else {
									viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

								if (menu.getItem(0).getItemId() == R.id.filterNotifications) {

									BottomSheetNotificationsFilterFragment
											bottomSheetNotificationsFilterFragment =
													new BottomSheetNotificationsFilterFragment();
									bottomSheetNotificationsFilterFragment.show(
											getChildFragmentManager(),
											"notificationsFilterBottomSheet");
									bottomSheetNotificationsFilterFragment.setOnClickListener(
											(text) -> {
												currentFilterMode = text;
												pageCurrentIndex = 1;
												loadNotifications(false);

												int filterIcon =
														currentFilterMode.equalsIgnoreCase("read")
																? R.drawable.ic_filter_closed
																: R.drawable.ic_filter;

												menu.getItem(0).setIcon(filterIcon);

												if (currentFilterMode.equalsIgnoreCase("read")) {
													viewBinding.markAllAsRead.setVisibility(
															View.GONE);
												} else {
													viewBinding.markAllAsRead.setVisibility(
															View.VISIBLE);
												}
											});
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		return viewBinding.getRoot();
	}

	private void loadNotifications(boolean append) {

		viewBinding.noDataNotifications.setVisibility(View.GONE);
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		String[] filter =
				currentFilterMode.equals("read")
						? new String[] {"pinned", "read"}
						: new String[] {"pinned", "unread"};

		RetrofitClient.getApiInterface(context)
				.notifyGetList(
						false,
						Arrays.asList(filter),
						null,
						null,
						null,
						pageCurrentIndex,
						pageResultLimit)
				.enqueue(
						(SimpleCallback<List<NotificationThread>>)
								(call1, listResponse) -> {
									if (listResponse.isPresent()
											&& listResponse.get().isSuccessful()
											&& listResponse.get().body() != null) {
										if (!append) {
											notificationThreads.clear();
										}

										if (listResponse.get().body() != null) {
											notificationThreads.addAll(
													Objects.requireNonNull(
															listResponse.get().body()));
										} else {
											notificationsAdapter.setMoreDataAvailable(false);
										}

										if (!append
												|| !Objects.requireNonNull(
																listResponse.get().body())
														.isEmpty()) {
											notificationsAdapter.notifyDataChanged();
										}
									}

									AppUtil.setMultiVisibility(View.GONE, viewBinding.progressBar);

									if (notificationThreads.isEmpty()) {
										viewBinding.noDataNotifications.setVisibility(View.VISIBLE);
									} else {
										viewBinding.noDataNotifications.setVisibility(View.GONE);
									}

									if (currentFilterMode.equalsIgnoreCase("unread")) {
										if (notificationThreads.isEmpty()) {
											viewBinding.markAllAsRead.setVisibility(View.GONE);
										} else {
											viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
										}
									}
								});
	}

	@Override
	public void onNotificationClicked(NotificationThread notificationThread) {

		if (notificationThread.isUnread() && !notificationThread.isPinned()) {
			RetrofitClient.getApiInterface(context)
					.notifyReadThread(String.valueOf(notificationThread.getId()), "read")
					.enqueue(
							(SimpleCallback<NotificationThread>)
									(call, voidResponse) -> {
										pageCurrentIndex = 1;
										loadNotifications(false);
										if (notificationCountListener != null) {
											notificationCountListener.onNotificationsMarkedRead();
										}
									});
		}

		if (StringUtils.containsAny(
				notificationThread.getSubject().getType().toLowerCase(), "pull", "issue")) {

			RepositoryContext repo =
					new RepositoryContext(
							notificationThread.getRepository().getOwner().getLogin(),
							notificationThread.getRepository().getName(),
							context); // we can't use the repository object here directly because
			// the permissions are missing
			String issueUrl = notificationThread.getSubject().getUrl();

			repo.saveToDB(context);

			Intent intent =
					new IssueContext(
									repo,
									Integer.parseInt(
											issueUrl.substring(issueUrl.lastIndexOf("/") + 1)),
									notificationThread.getSubject().getType())
							.getIntent(context, IssueDetailActivity.class);
			intent.putExtra("openedFromLink", "true");
			startActivity(intent);
		} else if (notificationThread.getSubject().getType().equalsIgnoreCase("repository")) {
			startActivity(
					new RepositoryContext(notificationThread.getRepository(), context)
							.getIntent(context, RepoDetailActivity.class));
		}
	}

	@Override
	public void onMoreClicked(NotificationThread notificationThread) {
		BottomSheetNotificationsFragment bottomSheetNotificationsFragment =
				new BottomSheetNotificationsFragment();
		bottomSheetNotificationsFragment.onAttach(
				context,
				notificationThread,
				() -> {
					pageCurrentIndex = 1;
					loadNotifications(false);
					if (notificationCountListener != null) {
						notificationCountListener.onNotificationsMarkedRead();
					}
				});
		bottomSheetNotificationsFragment.show(
				getChildFragmentManager(), "notificationsBottomSheet");
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof NotificationCountListener) {
			notificationCountListener = (NotificationCountListener) context;
		}
	}
}
