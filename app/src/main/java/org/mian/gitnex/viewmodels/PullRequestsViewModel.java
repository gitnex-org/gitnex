package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class PullRequestsViewModel extends ViewModel {

	private final MutableLiveData<List<PullRequest>> prList =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<PullRequest>> getPrList() {
		return prList;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public void resetPagination() {
		isLastPage = false;
		totalCount = -1;
		prList.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void fetchPullRequests(
			Context ctx,
			String owner,
			String repo,
			String state,
			int page,
			int limit,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.repoListPullRequests(owner, repo, null, state, null, null, null, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<PullRequest>> call,
									@NonNull Response<List<PullRequest>> response) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);

								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										totalCount = Integer.parseInt(totalHeader);
									}

									List<PullRequest> body = response.body();
									List<PullRequest> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	prList.getValue()));
									currentList.addAll(body);
									prList.setValue(currentList);

									if (body.size() < limit
											|| (totalCount != -1
													&& currentList.size() >= totalCount)) {
										isLastPage = true;
									}
								} else if (response.code() == 404) {
									if (isRefresh) {
										prList.setValue(new ArrayList<>());
									}
									isLastPage = true;
								} else {
									errorMessage.setValue("API error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<PullRequest>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}
}
