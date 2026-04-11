package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class NotificationsViewModel extends ViewModel {

	private final MutableLiveData<List<NotificationThread>> notifications =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);

	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<NotificationThread>> getNotifications() {
		return notifications;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public LiveData<Boolean> getActionSuccess() {
		return actionSuccess;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public void fetchNotifications(
			Context context, String filterMode, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		String[] filter =
				filterMode.equals("read")
						? new String[] {"pinned", "read"}
						: new String[] {"pinned", "unread"};

		RetrofitClient.getApiInterface(context)
				.notifyGetList(false, Arrays.asList(filter), null, null, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<NotificationThread>> call,
									@NonNull Response<List<NotificationThread>> response) {
								handleResponse(response, isRefresh, limit);
								hasLoadedOnce.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<NotificationThread>> call,
									@NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
								hasLoadedOnce.setValue(true);
							}
						});
	}

	private void handleResponse(
			Response<List<NotificationThread>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) totalCount = Integer.parseInt(totalHeader);

			List<NotificationThread> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(notifications.getValue()));

			List<NotificationThread> newItems = response.body();
			currentList.addAll(newItems);
			notifications.setValue(currentList);

			if (newItems.size() < limit || currentList.size() >= totalCount) {
				isLastPage = true;
			}
		} else {
			errorMessage.setValue("Server Error: " + response.code());
		}
	}

	public void markThreadAsRead(Context context, long threadId) {
		updateNotificationStatus(context, threadId, "read");
	}

	public void updateNotificationStatus(Context context, long threadId, String status) {
		RetrofitClient.getApiInterface(context)
				.notifyReadThread(String.valueOf(threadId), status)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<NotificationThread> call,
									@NonNull Response<NotificationThread> response) {
								if (response.isSuccessful() || response.code() == 205) {
									actionSuccess.setValue(true);
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<NotificationThread> call, @NonNull Throwable t) {
								actionSuccess.setValue(true);
							}
						});
	}

	public void markAllAsRead(Context context) {
		RetrofitClient.getApiInterface(context)
				.notifyReadList(null, "false", Arrays.asList("unread", "pinned"), "read")
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<NotificationThread>> call,
									@NonNull Response<List<NotificationThread>> response) {
								if (response.isSuccessful() || response.code() == 205) {
									actionSuccess.setValue(true);
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<NotificationThread>> call,
									@NonNull Throwable t) {
								if (t.getMessage() != null && t.getMessage().contains("205")) {
									actionSuccess.setValue(true);
								} else {
									errorMessage.setValue(t.getMessage());
								}
							}
						});
	}

	public void resetActionStatus() {
		actionSuccess.setValue(false);
	}

	public void resetPagination() {
		this.totalCount = -1;
		this.isLastPage = false;
		this.hasLoadedOnce.setValue(false);
	}

	public boolean canLoadMore() {
		return !isLastPage && !Boolean.TRUE.equals(isLoading.getValue());
	}

	public void clearData() {
		notifications.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
		isLastPage = false;
		totalCount = -1;
	}
}
