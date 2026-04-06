package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.EditMilestoneOption;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class MilestonesViewModel extends ViewModel {

	private final MutableLiveData<List<Milestone>> milestones =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<Milestone>> getMilestones() {
		return milestones;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void resetPagination() {
		isLastPage = false;
		totalCount = -1;
		milestones.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void fetchMilestones(
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
				.issueGetMilestonesList(owner, repo, state, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Milestone>> call,
									@NonNull Response<List<Milestone>> response) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);

								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										totalCount = Integer.parseInt(totalHeader);
									}

									List<Milestone> body = response.body();
									List<Milestone> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	milestones.getValue()));

									for (Milestone m : body) {
										if (!currentList.contains(m)) {
											currentList.add(m);
										}
									}
									milestones.setValue(currentList);

									if (body.size() < limit
											|| (totalCount != -1
													&& currentList.size() >= totalCount)) {
										isLastPage = true;
									}
								} else {
									if (response.code() == 404 && isRefresh) {
										milestones.setValue(new ArrayList<>());
									}
									isLastPage = true;
									if (response.code() != 404) {
										error.setValue("Error: " + response.code());
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);
								error.setValue(t.getMessage());
							}
						});
	}

	public void toggleMilestoneState(
			Context ctx, String owner, String repo, Milestone milestone, String newState) {
		EditMilestoneOption body = new EditMilestoneOption();
		body.setState(newState);

		RetrofitClient.getApiInterface(ctx)
				.issueEditMilestone(owner, repo, String.valueOf(milestone.getId()), body)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Milestone> call,
									@NonNull Response<Milestone> response) {
								if (response.isSuccessful()) {
									List<Milestone> current =
											new ArrayList<>(
													Objects.requireNonNull(milestones.getValue()));
									current.remove(milestone);
									milestones.setValue(current);

									actionResult.setValue(200);
								} else {
									error.setValue("Update failed: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Milestone> call, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}

	public void deleteMilestone(
			Context ctx, String owner, String repo, long milestoneId, Milestone milestone) {
		RetrofitClient.getApiInterface(ctx)
				.issueDeleteMilestone(owner, repo, String.valueOf(milestoneId))
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									List<Milestone> current =
											new ArrayList<>(
													Objects.requireNonNull(milestones.getValue()));
									current.remove(milestone);
									milestones.setValue(current);
									actionResult.setValue(204);
								} else {
									error.setValue("Delete failed: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								error.setValue(t.getMessage());
							}
						});
	}
}
