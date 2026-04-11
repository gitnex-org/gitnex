package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitsViewModel extends ViewModel {

	private final MutableLiveData<List<Commit>> commits = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<Commit>> getCommits() {
		return commits;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public void fetchCommits(
			Context ctx, RepositoryContext repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.repoGetAllCommits(
						repo.getOwner(),
						repo.getName(),
						repo.getBranchRef(),
						null,
						null,
						null,
						true,
						true,
						true,
						page,
						limit,
						"")
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Commit>> call,
									@NonNull Response<List<Commit>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Commit>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleResponse(Response<List<Commit>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) {
				totalCount = Integer.parseInt(totalHeader);
			}

			List<Commit> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(commits.getValue()));
			currentList.addAll(response.body());
			commits.setValue(currentList);

			if (response.body().size() < limit || currentList.size() >= totalCount) {
				isLastPage = true;
			}
		} else if (response.code() != 409) {
			errorMessage.setValue("Server Error: " + response.code());
		}
	}
}
