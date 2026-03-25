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
	private final MutableLiveData<List<String>> unadoptedRepos =
			new MutableLiveData<>(new ArrayList<>());

	private final MutableLiveData<Boolean> isSettingsLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isCronLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isUnadoptedLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<String> taskSuccessMessage = new MutableLiveData<>();
	private final MutableLiveData<RepoActionResult> repoActionSuccess = new MutableLiveData<>();

	private int cronTotalCount = -1;
	private boolean isCronLastPage = false;
	private int unadoptedTotalCount = -1;
	private boolean isUnadoptedLastPage = false;

	public LiveData<RepositoryGlobal> getRepositorySettings() {
		return repositorySettings;
	}

	public LiveData<List<Cron>> getCronTasks() {
		return cronTasks;
	}

	public LiveData<List<String>> getUnadoptedRepos() {
		return unadoptedRepos;
	}

	public LiveData<Boolean> getIsSettingsLoading() {
		return isSettingsLoading;
	}

	public LiveData<Boolean> getIsCronLoading() {
		return isCronLoading;
	}

	public LiveData<Boolean> getIsUnadoptedLoading() {
		return isUnadoptedLoading;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public LiveData<String> getTaskSuccessMessage() {
		return taskSuccessMessage;
	}

	public LiveData<RepoActionResult> getRepoActionSuccess() {
		return repoActionSuccess;
	}

	public void fetchRepositoryGlobalSettings(Context context) {
		isSettingsLoading.setValue(true);
		ApiRetrofitClient.getInstance(context)
				.getRepositoryGlobalSettings()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<RepositoryGlobal> call,
									@NonNull Response<RepositoryGlobal> response) {
								isSettingsLoading.setValue(false);
								if (response.isSuccessful())
									repositorySettings.setValue(response.body());
								else errorMessage.setValue("Error: " + response.code());
							}

							@Override
							public void onFailure(
									@NonNull Call<RepositoryGlobal> call, @NonNull Throwable t) {
								isSettingsLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchCronTasks(Context context, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isCronLoading.getValue())) return;
		if (!isRefresh && isCronLastPage) return;

		isCronLoading.setValue(true);
		RetrofitClient.getApiInterface(context)
				.adminCronList(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Cron>> call,
									@NonNull Response<List<Cron>> response) {
								isCronLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									handleCronResponse(
											response.body(),
											response.headers().get("x-total-count"),
											limit,
											isRefresh);
								} else errorMessage.setValue("Error: " + response.code());
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Cron>> call, @NonNull Throwable t) {
								isCronLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleCronResponse(
			List<Cron> body, String totalHeader, int limit, boolean isRefresh) {
		if (totalHeader != null) cronTotalCount = Integer.parseInt(totalHeader);
		List<Cron> currentList =
				isRefresh
						? new ArrayList<>()
						: new ArrayList<>(Objects.requireNonNull(cronTasks.getValue()));
		currentList.addAll(body);
		cronTasks.setValue(currentList);
		if (body.size() < limit || currentList.size() >= cronTotalCount) isCronLastPage = true;
	}

	public void runCronTask(Context context, String taskName) {
		isCronLoading.setValue(true);
		RetrofitClient.getApiInterface(context)
				.adminCronRun(taskName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isCronLoading.setValue(false);
								if (response.code() == 204) {
									taskSuccessMessage.setValue(taskName);
									taskSuccessMessage.setValue(null);
								} else errorMessage.setValue("Error: " + response.code());
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isCronLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchUnadoptedRepos(Context context, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isUnadoptedLoading.getValue())) return;
		if (!isRefresh && isUnadoptedLastPage) return;

		isUnadoptedLoading.setValue(true);
		RetrofitClient.getApiInterface(context)
				.adminUnadoptedList(page, limit, null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<String>> call,
									@NonNull Response<List<String>> response) {
								isUnadoptedLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									handleUnadoptedResponse(
											response.body(),
											response.headers().get("x-total-count"),
											limit,
											isRefresh);
								} else errorMessage.setValue("Error: " + response.code());
							}

							@Override
							public void onFailure(
									@NonNull Call<List<String>> call, @NonNull Throwable t) {
								isUnadoptedLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleUnadoptedResponse(
			List<String> body, String totalHeader, int limit, boolean isRefresh) {
		if (totalHeader != null) unadoptedTotalCount = Integer.parseInt(totalHeader);
		List<String> currentList =
				isRefresh
						? new ArrayList<>()
						: new ArrayList<>(Objects.requireNonNull(unadoptedRepos.getValue()));
		currentList.addAll(body);
		unadoptedRepos.setValue(currentList);
		if (body.size() < limit || currentList.size() >= unadoptedTotalCount)
			isUnadoptedLastPage = true;
	}

	public void performRepoAction(Context context, String repoName, boolean isDelete) {
		isUnadoptedLoading.setValue(true);
		String[] parts = repoName.split("/");
		Call<Void> call =
				isDelete
						? RetrofitClient.getApiInterface(context)
								.adminDeleteUnadoptedRepository(parts[0], parts[1])
						: RetrofitClient.getApiInterface(context)
								.adminAdoptRepository(parts[0], parts[1]);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isUnadoptedLoading.setValue(false);
						if (response.code() == 204) {
							List<String> current =
									new ArrayList<>(
											Objects.requireNonNull(unadoptedRepos.getValue()));
							current.remove(repoName);
							unadoptedRepos.setValue(current);

							repoActionSuccess.setValue(new RepoActionResult(repoName, isDelete));
							repoActionSuccess.setValue(null);
						} else {
							errorMessage.setValue("Error: " + response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isUnadoptedLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
					}
				});
	}

	public void resetCronPagination() {
		this.isCronLastPage = false;
		this.cronTotalCount = -1;
		this.cronTasks.setValue(new ArrayList<>());
	}

	public void resetUnadoptedPagination() {
		this.isUnadoptedLastPage = false;
		this.unadoptedTotalCount = -1;
		this.unadoptedRepos.setValue(new ArrayList<>());
	}

	public record RepoActionResult(String repoName, boolean isDelete) {}
}
