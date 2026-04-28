package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueDetailViewModel extends ViewModel {

	private final MutableLiveData<Issue> issueData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Repository> repositoryData = new MutableLiveData<>();

	public LiveData<Issue> getIssueData() {
		return issueData;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Repository> getRepositoryData() {
		return repositoryData;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void fetchRepository(Context ctx, String owner, String repo) {
		Call<Repository> call = RetrofitClient.getApiInterface(ctx).repoGet(owner, repo);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull Response<Repository> response) {
						if (response.isSuccessful() && response.body() != null) {
							repositoryData.setValue(response.body());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {}
				});
	}

	public void fetchIssue(Context ctx, String owner, String repo, long issueNumber) {
		isLoading.setValue(true);
		error.setValue(null);

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx).issueGetIssue(owner, repo, issueNumber);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						isLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							issueData.setValue(response.body());
						} else {
							error.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}
}
