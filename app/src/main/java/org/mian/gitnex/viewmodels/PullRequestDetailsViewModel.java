package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.MergePullRequestOption;
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
	private final MutableLiveData<Boolean> isMerging = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isDeletingBranch = new MutableLiveData<>(false);
	private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> branchExists = new MutableLiveData<>(false);

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

	public LiveData<Boolean> getIsMerging() {
		return isMerging;
	}

	public LiveData<Boolean> getIsUpdating() {
		return isUpdating;
	}

	public LiveData<Boolean> getIsDeletingBranch() {
		return isDeletingBranch;
	}

	public LiveData<String> getActionMessage() {
		return actionMessage;
	}

	public LiveData<Boolean> getBranchExists() {
		return branchExists;
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

	public void clearActionMessage() {
		actionMessage.setValue(null);
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

	public void mergePr(
			Context ctx,
			String owner,
			String repo,
			long prNumber,
			String doStrategy,
			String title,
			String message,
			boolean deleteBranch,
			boolean mergeWhenChecksSucceed) {
		isMerging.setValue(true);
		actionError.setValue(null);

		MergePullRequestOption option = new MergePullRequestOption();
		option.setDo(MergePullRequestOption.DoEnum.fromValue(doStrategy));
		option.setMergeTitleField(title);
		option.setMergeMessageField(message);
		option.setDeleteBranchAfterMerge(deleteBranch);
		option.setMergeWhenChecksSucceed(mergeWhenChecksSucceed);

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.repoMergePullRequest(owner, repo, prNumber, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isMerging.setValue(false);
						if (response.code() == 200) {
							actionMessage.setValue(ctx.getString(R.string.mergePRSuccessMsg));
							refreshPullRequest(ctx, owner, repo, prNumber);
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else if (response.code() == 404) {
							actionError.setValue(ctx.getString(R.string.mergePR404ErrorMsg));
						} else if (response.code() == 405) {
							actionError.setValue(ctx.getString(R.string.mergeNotAllowed));
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isMerging.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void updatePr(Context ctx, String owner, String repo, long prNumber, Boolean rebase) {
		isUpdating.setValue(true);
		actionError.setValue(null);

		String strategy;
		if (rebase == null) strategy = null;
		else if (!rebase) strategy = "merge";
		else strategy = "rebase";

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.repoUpdatePullRequest(owner, repo, prNumber, strategy);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isUpdating.setValue(false);
						if (response.isSuccessful()) {
							actionMessage.setValue(ctx.getString(R.string.updatePrSuccess));
							refreshPullRequest(ctx, owner, repo, prNumber);
						} else if (response.code() == 403) {
							actionError.setValue(ctx.getString(R.string.authorizeError));
						} else if (response.code() == 409) {
							actionError.setValue(ctx.getString(R.string.updatePrConflict));
						} else if (response.code() == 500) {
							actionError.setValue(ctx.getString(R.string.updatePrNothingToUpdate));
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isUpdating.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void checkBranchExists(Context ctx, String owner, String repo, String branchName) {
		Call<Branch> call =
				RetrofitClient.getApiInterface(ctx).repoGetBranch(owner, repo, branchName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Branch> call, @NonNull Response<Branch> response) {
						branchExists.setValue(response.isSuccessful() && response.body() != null);
					}

					@Override
					public void onFailure(@NonNull Call<Branch> call, @NonNull Throwable t) {
						branchExists.setValue(false);
					}
				});
	}

	public void deleteHeadBranch(
			Context ctx, String owner, String repo, String branchName, long prNumber) {
		isDeletingBranch.setValue(true);
		actionError.setValue(null);

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx).repoDeleteBranch(owner, repo, branchName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isDeletingBranch.setValue(false);
						if (response.code() == 204) {
							actionMessage.setValue(ctx.getString(R.string.deleteBranchSuccess));
							refreshPullRequest(ctx, owner, repo, prNumber);
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else if (response.code() == 404) {
							actionError.setValue(ctx.getString(R.string.deleteBranchErrorNotFound));
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isDeletingBranch.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}
}
