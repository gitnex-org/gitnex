package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.models.IssueFilterState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssuesViewModel extends ViewModel {

	private final MutableLiveData<List<Issue>> issues = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<List<Issue>> repoIssues =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isRepoLoading = new MutableLiveData<>(false);
	private final MutableLiveData<List<Issue>> pinnedIssues =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> hasRepoLoadedOnce = new MutableLiveData<>(false);
	private final IssueFilterState currentFilterState = new IssueFilterState();

	private int repoTotalCount = -1;
	private boolean isRepoLastPage = false;
	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<Issue>> getIssues() {
		return issues;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<List<Issue>> getRepoIssues() {
		return repoIssues;
	}

	public LiveData<List<Issue>> getPinnedIssues() {
		return pinnedIssues;
	}

	public LiveData<Boolean> getIsRepoLoading() {
		return isRepoLoading;
	}

	public LiveData<Boolean> getHasRepoLoadedOnce() {
		return hasRepoLoadedOnce;
	}

	public IssueFilterState getFilterState() {
		return currentFilterState;
	}

	public void resetRepoPagination() {
		isRepoLastPage = false;
		repoTotalCount = -1;
		repoIssues.setValue(new ArrayList<>());
		hasRepoLoadedOnce.setValue(false);
	}

	public void applyFilters(Context ctx, String owner, String repo, int limit) {
		resetRepoPagination();

		String labelsParam =
				currentFilterState.selectedLabels.isEmpty()
						? null
						: String.join(",", currentFilterState.selectedLabels);

		fetchRepoIssues(
				ctx,
				owner,
				repo,
				currentFilterState.state,
				labelsParam,
				currentFilterState.query.isEmpty() ? null : currentFilterState.query,
				Constants.issuesRequestType,
				currentFilterState.milestoneTitle,
				currentFilterState.mentionedBy,
				1,
				limit,
				true);
	}

	public void fetchRepoIssues(
			Context ctx,
			String owner,
			String repo,
			String state,
			String labels,
			String query,
			String type,
			String milestone,
			String mentionedBy,
			int page,
			int limit,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isRepoLoading.getValue()) && !isRefresh) return;
		if (!isRefresh && isRepoLastPage) return;

		isRepoLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.issueListIssues(
						owner,
						repo,
						state,
						labels,
						query,
						type,
						milestone,
						null,
						null,
						null,
						null,
						mentionedBy,
						page,
						limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Issue>> call,
									@NonNull Response<List<Issue>> response) {
								isRepoLoading.setValue(false);
								hasRepoLoadedOnce.setValue(true);

								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										repoTotalCount = Integer.parseInt(totalHeader);
									}

									List<Issue> body = response.body();
									List<Issue> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	repoIssues.getValue()));
									currentList.addAll(body);
									repoIssues.setValue(currentList);

									if (body.size() < limit
											|| (repoTotalCount != -1
													&& currentList.size() >= repoTotalCount)) {
										isRepoLastPage = true;
									}
								} else if (response.code() == 404) {
									if (isRefresh) {
										repoIssues.setValue(new ArrayList<>());
									}
									isRepoLastPage = true;
								} else {
									errorMessage.setValue("API error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
								isRepoLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchPinnedIssues(Context ctx, String owner, String repo) {
		RetrofitClient.getApiInterface(ctx)
				.repoListPinnedIssues(owner, repo)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Issue>> call,
									@NonNull Response<List<Issue>> response) {
								if (response.isSuccessful() && response.body() != null) {
									pinnedIssues.setValue(response.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Issue>> call, @NonNull Throwable t) {}
						});
	}

	public void resetPagination() {
		isLastPage = false;
		totalCount = -1;
		issues.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void fetchIssues(
			Context ctx,
			String query,
			String state,
			String labels,
			String milestones,
			Boolean assigned,
			Boolean created,
			int page,
			int limit,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.issueSearchIssues(
						state,
						labels,
						milestones,
						query,
						"issues",
						null,
						null,
						assigned,
						created,
						null,
						null,
						null,
						null,
						null,
						page,
						limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Issue>> call,
									@NonNull Response<List<Issue>> response) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);

								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										totalCount = Integer.parseInt(totalHeader);
									}

									List<Issue> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	issues.getValue()));
									currentList.addAll(response.body());
									issues.setValue(currentList);

									if (response.body().size() < limit
											|| currentList.size() >= totalCount) {
										isLastPage = true;
									}
								} else {
									errorMessage.setValue("API error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}
}
