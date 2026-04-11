package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Activity;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActivitiesViewModel extends ViewModel {

	private final MutableLiveData<List<Activity>> activities =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

	private boolean isLastPage = false;
	private int resultLimit;

	public LiveData<List<Activity>> getActivities() {
		return activities;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public void setResultLimit(int limit) {
		this.resultLimit = limit;
	}

	public void fetchActivities(Context ctx, String username, int page, boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.userListActivityFeeds(username, false, null, page, resultLimit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Activity>> call,
									@NonNull Response<List<Activity>> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									List<Activity> incoming = response.body();
									List<Activity> current =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	activities.getValue()));

									current.addAll(incoming);
									activities.setValue(current);

									if (incoming.size() < resultLimit) {
										isLastPage = true;
									} else if (isRefresh) {
										isLastPage = false;
									}
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Activity>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}
}
