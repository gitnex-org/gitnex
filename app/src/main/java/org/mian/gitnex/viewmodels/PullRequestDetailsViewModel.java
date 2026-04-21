package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class PullRequestDetailsViewModel extends ViewModel {

	private final MutableLiveData<PullRequest> prData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> actionError = new MutableLiveData<>();

	public LiveData<PullRequest> getPrData() {
		return prData;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsRefreshing() {
		return isRefreshing;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<String> getActionError() {
		return actionError;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearActionError() {
		actionError.setValue(null);
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void fetchPullRequest(Context ctx, String owner, String repo, long prNumber) {
		fetchPullRequest(ctx, owner, repo, prNumber, false);
	}

	public void refreshPullRequest(Context ctx, String owner, String repo, long prNumber) {
		fetchPullRequest(ctx, owner, repo, prNumber, true);
	}

	private void fetchPullRequest(
			Context ctx, String owner, String repo, long prNumber, boolean isRefresh) {
		if (isRefresh) {
			isRefreshing.setValue(true);
		} else {
			isLoading.setValue(true);
		}
		error.setValue(null);

		Call<PullRequest> call =
				RetrofitClient.getApiInterface(ctx).repoGetPullRequest(owner, repo, prNumber);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull Response<PullRequest> response) {
						isLoading.setValue(false);
						isRefreshing.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							prData.setValue(response.body());
						} else {
							error.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isRefreshing.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}
}
