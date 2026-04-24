package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gitnex.tea4j.v2.models.AddTimeOption;
import org.gitnex.tea4j.v2.models.TrackedTime;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TrackedTimeViewModel extends ViewModel {

	private final MutableLiveData<List<TrackedTime>> trackedTimes =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Long> totalSeconds = new MutableLiveData<>(0L);
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isAdding = new MutableLiveData<>(false);
	private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
	private final MutableLiveData<String> error = new MutableLiveData<>();

	private int currentPage = 1;
	private boolean hasMore = true;
	private int resultLimit;

	public LiveData<List<TrackedTime>> getTrackedTimes() {
		return trackedTimes;
	}

	public LiveData<Long> getTotalSeconds() {
		return totalSeconds;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsAdding() {
		return isAdding;
	}

	public LiveData<String> getActionMessage() {
		return actionMessage;
	}

	public LiveData<String> getError() {
		return error;
	}

	public void clearActionMessage() {
		actionMessage.setValue(null);
	}

	public void resetPagination() {
		currentPage = 1;
		hasMore = true;
		trackedTimes.setValue(new ArrayList<>());
	}

	public void loadTrackedTimes(Context ctx, String owner, String repo, long issueIndex) {
		if (Boolean.TRUE.equals(isLoading.getValue()) || !hasMore) return;

		isLoading.setValue(true);
		resultLimit = Constants.getCurrentResultLimit(ctx);

		Call<List<TrackedTime>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueTrackedTimes(
								owner,
								repo,
								issueIndex,
								null,
								null,
								null,
								currentPage,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<TrackedTime>> call,
							@NonNull Response<List<TrackedTime>> response) {
						isLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							List<TrackedTime> newTimes = response.body();
							List<TrackedTime> current =
									new ArrayList<>(
											trackedTimes.getValue() != null
													? trackedTimes.getValue()
													: new ArrayList<>());
							current.addAll(newTimes);
							trackedTimes.setValue(current);
							hasMore = newTimes.size() >= resultLimit;
							if (hasMore) currentPage++;
							calculateTotal(current);
						} else {
							hasMore = false;
							if (trackedTimes.getValue() == null
									|| trackedTimes.getValue().isEmpty()) {
								trackedTimes.setValue(new ArrayList<>());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<TrackedTime>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						hasMore = false;
						error.setValue(t.getMessage());
					}
				});
	}

	public void addTrackedTime(
			Context ctx, String owner, String repo, long issueIndex, int hours, int minutes) {
		long totalSecs = (hours * 3600L) + (minutes * 60L);

		AddTimeOption timeOption = new AddTimeOption();
		timeOption.setCreated(new Date());
		timeOption.setTime(totalSecs);

		isAdding.setValue(true);

		Call<TrackedTime> call =
				RetrofitClient.getApiInterface(ctx)
						.issueAddTime(owner, repo, issueIndex, timeOption);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<TrackedTime> call,
							@NonNull Response<TrackedTime> response) {
						isAdding.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							List<TrackedTime> current =
									new ArrayList<>(
											trackedTimes.getValue() != null
													? trackedTimes.getValue()
													: new ArrayList<>());
							current.add(0, response.body());
							trackedTimes.setValue(current);
							calculateTotal(current);
							actionMessage.setValue(ctx.getString(R.string.time_added));
						} else {
							actionMessage.setValue(ctx.getString(R.string.time_add_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<TrackedTime> call, @NonNull Throwable t) {
						isAdding.setValue(false);
						actionMessage.setValue(ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void deleteTrackedTime(
			Context ctx, String owner, String repo, long issueIndex, TrackedTime time) {
		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.issueDeleteTime(owner, repo, issueIndex, time.getId());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							List<TrackedTime> current =
									new ArrayList<>(
											trackedTimes.getValue() != null
													? trackedTimes.getValue()
													: new ArrayList<>());
							current.remove(time);
							trackedTimes.setValue(current);
							calculateTotal(current);
							actionMessage.setValue(ctx.getString(R.string.time_removed));
						} else {
							actionMessage.setValue(ctx.getString(R.string.time_delete_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						actionMessage.setValue(ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	private void calculateTotal(List<TrackedTime> times) {
		long total = 0;
		for (TrackedTime time : times) {
			total += time.getTime();
		}
		totalSeconds.setValue(total);
	}
}
