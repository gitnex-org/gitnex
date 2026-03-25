package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Cron;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.settings.RepositoryGlobal;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class AdministrationViewModel extends ViewModel {

	private final MutableLiveData<RepositoryGlobal> repositorySettings = new MutableLiveData<>();
	private final MutableLiveData<List<Cron>> cronTasks = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<String> taskSuccessMessage = new MutableLiveData<>();

	private int cronTotalCount = -1;
	private boolean isCronLastPage = false;

	public LiveData<RepositoryGlobal> getRepositorySettings() {
		return repositorySettings;
	}

	public LiveData<List<Cron>> getCronTasks() {
		return cronTasks;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public LiveData<String> getTaskSuccessMessage() {
		return taskSuccessMessage;
	}

	public void fetchRepositoryGlobalSettings(Context context) {
		isLoading.setValue(true);
		ApiRetrofitClient.getInstance(context)
				.getRepositoryGlobalSettings()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<RepositoryGlobal> call,
									@NonNull Response<RepositoryGlobal> response) {
								isLoading.setValue(false);
								if (response.isSuccessful()) {
									repositorySettings.setValue(response.body());
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<RepositoryGlobal> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchCronTasks(Context context, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isCronLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(context)
				.adminCronList(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Cron>> call,
									@NonNull Response<List<Cron>> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										cronTotalCount = Integer.parseInt(totalHeader);
									}

									List<Cron> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	cronTasks.getValue()));
									currentList.addAll(response.body());
									cronTasks.setValue(currentList);

									if (response.body().size() < limit
											|| currentList.size() >= cronTotalCount) {
										isCronLastPage = true;
									}
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Cron>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void resetCronPagination() {
		this.isCronLastPage = false;
		this.cronTotalCount = -1;
		this.cronTasks.setValue(new ArrayList<>());
	}

	public void runCronTask(Context context, String taskName) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(context)
				.adminCronRun(taskName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isLoading.setValue(false);
								if (response.code() == 204) {
									taskSuccessMessage.setValue(taskName);
									taskSuccessMessage.setValue(null);
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}
}
