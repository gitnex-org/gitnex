package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateReleaseOption;
import org.gitnex.tea4j.v2.models.CreateTagOption;
import org.gitnex.tea4j.v2.models.EditReleaseOption;
import org.gitnex.tea4j.v2.models.Release;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ReleasesViewModel extends ViewModel {

	private final MutableLiveData<List<Release>> releases = new MutableLiveData<>();
	private final MutableLiveData<List<Tag>> tags = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isTagsLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> repoReleasesCountLiveData = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isCreatingRelease = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isCreatingTag = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isUpdatingRelease = new MutableLiveData<>(false);
	private final MutableLiveData<Release> createdRelease = new MutableLiveData<>();
	private final MutableLiveData<Tag> createdTag = new MutableLiveData<>();
	private final MutableLiveData<Release> updatedRelease = new MutableLiveData<>();
	private final MutableLiveData<String> createReleaseError = new MutableLiveData<>();
	private final MutableLiveData<String> createTagError = new MutableLiveData<>();
	private final MutableLiveData<String> updateReleaseError = new MutableLiveData<>();

	private int totalCount = -1;
	private boolean isLastPage = false;
	private int tagsTotalCount = -1;
	private boolean isTagsLastPage = false;

	public LiveData<List<Release>> getReleases() {
		return releases;
	}

	public LiveData<List<Tag>> getTags() {
		return tags;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsTagsLoading() {
		return isTagsLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public LiveData<Integer> getReleasesTotalCount() {
		return repoReleasesCountLiveData;
	}

	public LiveData<Boolean> getIsCreatingRelease() {
		return isCreatingRelease;
	}

	public LiveData<Boolean> getIsCreatingTag() {
		return isCreatingTag;
	}

	public LiveData<Boolean> getIsUpdatingRelease() {
		return isUpdatingRelease;
	}

	public LiveData<Release> getCreatedRelease() {
		return createdRelease;
	}

	public LiveData<Tag> getCreatedTag() {
		return createdTag;
	}

	public LiveData<Release> getUpdatedRelease() {
		return updatedRelease;
	}

	public LiveData<String> getCreateReleaseError() {
		return createReleaseError;
	}

	public LiveData<String> getCreateTagError() {
		return createTagError;
	}

	public LiveData<String> getUpdateReleaseError() {
		return updateReleaseError;
	}

	public void resetPagination() {
		this.isLastPage = false;
		this.totalCount = -1;
		this.hasLoadedOnce.setValue(false);
		this.releases.setValue(null);
	}

	public void resetTagsPagination() {
		this.isTagsLastPage = false;
		this.tagsTotalCount = -1;
		this.tags.setValue(null);
	}

	public void clearCreatedRelease() {
		createdRelease.setValue(null);
	}

	public void clearCreatedTag() {
		createdTag.setValue(null);
	}

	public void clearUpdatedRelease() {
		updatedRelease.setValue(null);
	}

	public void clearCreateReleaseError() {
		createReleaseError.setValue(null);
	}

	public void clearCreateTagError() {
		createTagError.setValue(null);
	}

	public void clearUpdateReleaseError() {
		updateReleaseError.setValue(null);
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void prefetchCounts(Context ctx, String owner, String repo) {
		fetchReleases(ctx, owner, repo, 1, 1, true);
	}

	public void fetchReleases(
			Context ctx, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;
		if (!isRefresh && (isLastPage || (totalCount != -1 && getListSize(releases) >= totalCount)))
			return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.repoListReleases(owner, repo, null, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Release>> call,
									@NonNull Response<List<Release>> response) {
								handleResponse(
										response,
										releases,
										isLoading,
										isRefresh,
										limit,
										total -> totalCount = total,
										last -> isLastPage = last);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Release>> call, @NonNull Throwable t) {
								handleError(isLoading, t);
							}
						});
	}

	public void fetchTags(
			Context ctx, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isTagsLoading.getValue()) && !isRefresh) return;
		if (!isRefresh
				&& (isTagsLastPage
						|| (tagsTotalCount != -1 && getListSize(tags) >= tagsTotalCount))) return;

		isTagsLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.repoListTags(owner, repo, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Tag>> call,
									@NonNull Response<List<Tag>> response) {
								handleResponse(
										response,
										tags,
										isTagsLoading,
										isRefresh,
										limit,
										total -> tagsTotalCount = total,
										last -> isTagsLastPage = last);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Tag>> call, @NonNull Throwable t) {
								handleError(isTagsLoading, t);
							}
						});
	}

	private <T> int getListSize(MutableLiveData<List<T>> data) {
		return data.getValue() != null ? data.getValue().size() : 0;
	}

	private <T> void handleResponse(
			Response<List<T>> response,
			MutableLiveData<List<T>> liveData,
			MutableLiveData<Boolean> loading,
			boolean isRefresh,
			int limit,
			java.util.function.Consumer<Integer> totalSetter,
			java.util.function.Consumer<Boolean> lastPageSetter) {

		loading.setValue(false);
		hasLoadedOnce.setValue(true);

		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			int total = totalHeader != null ? Integer.parseInt(totalHeader) : -1;
			totalSetter.accept(total);

			repoReleasesCountLiveData.setValue(total);

			List<T> body = response.body();
			List<T> currentList =
					(isRefresh || liveData.getValue() == null)
							? new ArrayList<>()
							: new ArrayList<>(liveData.getValue());

			for (T item : body) {
				if (!currentList.contains(item)) {
					currentList.add(item);
				}
			}
			liveData.setValue(currentList);

			lastPageSetter.accept(
					body.size() < limit || (total != -1 && currentList.size() >= total));

		} else if (response.code() == 404 && isRefresh) {
			liveData.setValue(new ArrayList<>());
			lastPageSetter.accept(true);
		} else {
			errorMessage.setValue("API error: " + response.code());
		}
	}

	private void handleError(MutableLiveData<Boolean> loading, Throwable t) {
		loading.setValue(false);
		errorMessage.setValue(t.getMessage());
	}

	public void deleteTag(Context ctx, String owner, String repo, String tagName, int position) {
		RetrofitClient.getApiInterface(ctx)
				.repoDeleteTag(owner, repo, tagName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									List<Tag> current =
											new ArrayList<>(
													Objects.requireNonNull(tags.getValue()));
									if (position >= 0 && position < current.size()) {
										current.remove(position);
										tags.setValue(current);
										if (tagsTotalCount > 0) {
											tagsTotalCount--;
										}
									}
									actionResult.setValue(204);
								} else {
									errorMessage.setValue("Delete failed: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void deleteRelease(
			Context ctx, String owner, String repo, long releaseId, int position) {
		RetrofitClient.getApiInterface(ctx)
				.repoDeleteRelease(owner, repo, releaseId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									List<Release> current =
											new ArrayList<>(
													Objects.requireNonNull(releases.getValue()));
									if (position >= 0 && position < current.size()) {
										current.remove(position);
										releases.setValue(current);
										if (totalCount > 0) {
											totalCount--;
										}
									}
									actionResult.setValue(204);
								} else {
									errorMessage.setValue("Delete failed: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void createRelease(
			Context ctx, String owner, String repo, CreateReleaseOption releaseData) {
		isCreatingRelease.setValue(true);

		Call<Release> call =
				RetrofitClient.getApiInterface(ctx).repoCreateRelease(owner, repo, releaseData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Release> call, @NonNull Response<Release> response) {
						isCreatingRelease.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							createdRelease.setValue(response.body());
							actionResult.setValue(201);
						} else if (response.code() == 401) {
							createReleaseError.setValue("UNAUTHORIZED");
						} else if (response.code() == 403) {
							createReleaseError.setValue(ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							createReleaseError.setValue(ctx.getString(R.string.apiNotFound));
						} else if (response.code() == 409) {
							createReleaseError.setValue(
									ctx.getString(R.string.tagNameConflictError));
						} else {
							createReleaseError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
						isCreatingRelease.setValue(false);
						createReleaseError.setValue(t.getMessage());
					}
				});
	}

	public void createTag(Context ctx, String owner, String repo, CreateTagOption tagData) {
		isCreatingTag.setValue(true);

		Call<Tag> call = RetrofitClient.getApiInterface(ctx).repoCreateTag(owner, repo, tagData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Tag> call, @NonNull Response<Tag> response) {
						isCreatingTag.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							createdTag.setValue(response.body());
							actionResult.setValue(201);
						} else if (response.code() == 401) {
							createTagError.setValue("UNAUTHORIZED");
						} else if (response.code() == 403) {
							createTagError.setValue(ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							createTagError.setValue(ctx.getString(R.string.apiNotFound));
						} else if (response.code() == 409) {
							createTagError.setValue(ctx.getString(R.string.tagNameConflictError));
						} else {
							createTagError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Tag> call, @NonNull Throwable t) {
						isCreatingTag.setValue(false);
						createTagError.setValue(t.getMessage());
					}
				});
	}

	public void updateRelease(
			Context ctx,
			String owner,
			String repo,
			long releaseId,
			String name,
			String body,
			boolean prerelease) {
		isUpdatingRelease.setValue(true);

		EditReleaseOption editData = new EditReleaseOption();
		editData.setName(name);
		editData.setBody(body);
		editData.setPrerelease(prerelease);

		Call<Release> call =
				RetrofitClient.getApiInterface(ctx)
						.repoEditRelease(owner, repo, releaseId, editData);

		call.enqueue(
				new Callback<Release>() {
					@Override
					public void onResponse(
							@NonNull Call<Release> call, @NonNull Response<Release> response) {
						isUpdatingRelease.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							updatedRelease.setValue(response.body());
							actionResult.setValue(200);
						} else if (response.code() == 401) {
							updateReleaseError.setValue("UNAUTHORIZED");
						} else if (response.code() == 403) {
							updateReleaseError.setValue(ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							updateReleaseError.setValue(ctx.getString(R.string.apiNotFound));
						} else {
							updateReleaseError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Release> call, @NonNull Throwable t) {
						isUpdatingRelease.setValue(false);
						updateReleaseError.setValue(t.getMessage());
					}
				});
	}
}
