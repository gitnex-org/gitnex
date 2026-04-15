package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreatePullRequestOption;
import org.gitnex.tea4j.v2.models.EditPullRequestOption;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
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
	private final MutableLiveData<Integer> repoTotalPrCountLiveData = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
	private final MutableLiveData<PullRequest> createdPr = new MutableLiveData<>();
	private final MutableLiveData<PullRequest> updatedPr = new MutableLiveData<>();
	private final MutableLiveData<String> createError = new MutableLiveData<>();
	private final MutableLiveData<String> updateError = new MutableLiveData<>();
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);

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

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<Boolean> getIsUpdating() {
		return isUpdating;
	}

	public LiveData<PullRequest> getCreatedPr() {
		return createdPr;
	}

	public LiveData<PullRequest> getUpdatedPr() {
		return updatedPr;
	}

	public LiveData<String> getCreateError() {
		return createError;
	}

	public LiveData<String> getUpdateError() {
		return updateError;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public LiveData<Integer> getRepoPrTotalCount() {
		return repoTotalPrCountLiveData;
	}

	public void clearCreatedPr() {
		createdPr.setValue(null);
	}

	public void clearUpdatedPr() {
		updatedPr.setValue(null);
	}

	public void clearCreateError() {
		createError.setValue(null);
	}

	public void clearUpdateError() {
		updateError.setValue(null);
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
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
									if (isRefresh) {
										isLastPage = false;
										totalCount = -1;
									}
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										int count = Integer.parseInt(totalHeader);
										totalCount = count;
										repoTotalPrCountLiveData.setValue(count);
									} else if (isRefresh) {
										repoTotalPrCountLiveData.setValue(0);
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

	public void prefetchPrCounts(Context ctx, String owner, String repo) {
		fetchPullRequests(ctx, owner, repo, "open", 1, 1, true);
	}

	public void createPullRequest(
			Context ctx, String owner, String repo, CreatePullRequestOption prData) {
		isCreating.setValue(true);
		createError.setValue(null);

		Call<PullRequest> call =
				RetrofitClient.getApiInterface(ctx).repoCreatePullRequest(owner, repo, prData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull Response<PullRequest> response) {
						isCreating.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							createdPr.setValue(response.body());
							actionResult.setValue(201);
						} else {
							handleCreateUpdateError(response.code(), ctx, true);
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
						isCreating.setValue(false);
						createError.setValue(t.getMessage());
					}
				});
	}

	public void updatePullRequest(
			Context ctx, String owner, String repo, long prIndex, EditPullRequestOption prData) {
		isUpdating.setValue(true);
		updateError.setValue(null);

		Call<PullRequest> call =
				RetrofitClient.getApiInterface(ctx)
						.repoEditPullRequest(owner, repo, prIndex, prData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull Response<PullRequest> response) {
						isUpdating.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							updatedPr.setValue(response.body());
							actionResult.setValue(200);
						} else {
							handleCreateUpdateError(response.code(), ctx, false);
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {
						isUpdating.setValue(false);
						updateError.setValue(t.getMessage());
					}
				});
	}

	private void handleCreateUpdateError(int code, Context ctx, boolean isCreate) {
		String errorMsg =
				switch (code) {
					case 401 -> "UNAUTHORIZED";
					case 403 -> ctx.getString(R.string.authorizeError);
					case 404 -> ctx.getString(R.string.apiNotFound);
					case 409 -> ctx.getString(R.string.prAlreadyExists);
					default -> ctx.getString(R.string.genericError);
				};

		if (isCreate) {
			createError.setValue(errorMsg);
		} else {
			updateError.setValue(errorMsg);
		}
	}
}
