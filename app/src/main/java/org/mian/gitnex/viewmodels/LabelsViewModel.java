package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateLabelOption;
import org.gitnex.tea4j.v2.models.EditLabelOption;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class LabelsViewModel extends ViewModel {

	private final MutableLiveData<List<Label>> labels = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);

	private final List<Label> fullList = new ArrayList<>();

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public LiveData<List<Label>> getLabels() {
		return labels;
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

	public void resetPagination() {
		fullList.clear();
		isLastPage = false;
		totalCount = -1;
		labels.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void fetchLabels(
			Context ctx,
			String owner,
			String repo,
			String type,
			int page,
			int limit,
			boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;

		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		Call<List<Label>> call;
		if (type.equalsIgnoreCase("repo")) {
			call = RetrofitClient.getApiInterface(ctx).issueListLabels(owner, repo, page, limit);
		} else {
			call = RetrofitClient.getApiInterface(ctx).orgListLabels(owner, page, limit);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull Response<List<Label>> response) {
						isLoading.setValue(false);
						hasLoadedOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							String totalHeader = response.headers().get("x-total-count");
							if (totalHeader != null) {
								totalCount = Integer.parseInt(totalHeader);
							}

							List<Label> body = response.body();
							if (isRefresh) {
								fullList.clear();
							}

							for (Label newLabel : body) {
								if (!fullList.contains(newLabel)) {
									fullList.add(newLabel);
								}
							}
							labels.setValue(new ArrayList<>(fullList));

							if (body.size() < limit
									|| (totalCount != -1 && fullList.size() >= totalCount)) {
								isLastPage = true;
							}
						} else {
							if (response.code() == 404 && isRefresh) {
								labels.setValue(new ArrayList<>());
							}
							isLastPage = true;
							if (response.code() != 404) {
								error.setValue("Error: " + response.code());
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						hasLoadedOnce.setValue(true);
						error.setValue(t.getMessage());
					}
				});
	}

	public void fetchLabelsMerged(Context ctx, String owner, String repo, int page, int limit) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.issueListLabels(owner, repo, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Label>> call,
									@NonNull Response<List<Label>> repoResponse) {
								List<Label> tempMergedList = new ArrayList<>();

								if (repoResponse.isSuccessful() && repoResponse.body() != null) {
									tempMergedList.addAll(repoResponse.body());
								}

								RetrofitClient.getApiInterface(ctx)
										.orgListLabels(owner, 1, 100)
										.enqueue(
												new Callback<>() {
													@Override
													public void onResponse(
															@NonNull Call<List<Label>> call,
															@NonNull Response<List<Label>>
																			orgResponse) {
														if (orgResponse.isSuccessful()
																&& orgResponse.body() != null) {
															for (Label orgLabel :
																	orgResponse.body()) {
																if (!listContainsLabel(
																		tempMergedList, orgLabel)) {
																	tempMergedList.add(orgLabel);
																}
															}
														}

														processAndPublish(tempMergedList);
													}

													@Override
													public void onFailure(
															@NonNull Call<List<Label>> call,
															@NonNull Throwable t) {
														processAndPublish(tempMergedList);
													}
												});
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Label>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void processAndPublish(List<Label> mergedList) {
		fullList.clear();
		if (mergedList != null) {
			fullList.addAll(mergedList);
		}
		labels.setValue(new ArrayList<>(fullList));
		isLoading.setValue(false);
		hasLoadedOnce.setValue(true);
	}

	private boolean listContainsLabel(List<Label> list, Label target) {
		for (Label l : list) {
			if (Objects.equals(l.getId(), target.getId())) return true;
		}
		return false;
	}

	public void saveLabel(
			Context ctx,
			String type,
			String owner,
			String repo,
			Long labelId,
			CreateLabelOption createOptions,
			EditLabelOption editOptions) {
		isActionLoading.setValue(true);
		boolean isEdit = (labelId != null);

		Call<Label> call;
		if ("org".equalsIgnoreCase(type)) {
			call =
					isEdit
							? RetrofitClient.getApiInterface(ctx)
									.orgEditLabel(owner, labelId, editOptions)
							: RetrofitClient.getApiInterface(ctx)
									.orgCreateLabel(owner, createOptions);
		} else {
			call =
					isEdit
							? RetrofitClient.getApiInterface(ctx)
									.issueEditLabel(owner, repo, labelId, editOptions)
							: RetrofitClient.getApiInterface(ctx)
									.issueCreateLabel(owner, repo, createOptions);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Label> call, @NonNull Response<Label> response) {
						isActionLoading.setValue(false);
						if (response.isSuccessful()) {
							actionResult.postValue(response.code());
						} else {
							error.setValue("Error: " + response.code());
							actionResult.postValue(response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Label> call, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						actionResult.setValue(500);
					}
				});
	}

	public void deleteLabel(Context ctx, String type, String owner, String repo, long labelId) {
		isActionLoading.setValue(true);
		Call<Void> call;

		if ("org".equalsIgnoreCase(type)) {
			call = RetrofitClient.getApiInterface(ctx).orgDeleteLabel(owner, labelId);
		} else {
			call = RetrofitClient.getApiInterface(ctx).issueDeleteLabel(owner, repo, labelId);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isActionLoading.setValue(false);
						if (response.isSuccessful() && response.code() == 204) {
							fullList.removeIf(l -> l.getId() == labelId);
							labels.setValue(new ArrayList<>(fullList));

							if (totalCount > 0) {
								totalCount--;
							}

							actionResult.postValue(204);
						} else {
							error.setValue("Error: " + response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						actionResult.setValue(500);
					}
				});
	}
}
