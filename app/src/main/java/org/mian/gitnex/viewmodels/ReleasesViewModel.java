package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Release;
import org.gitnex.tea4j.v2.models.Tag;
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
	private final MutableLiveData<Integer> deleteActionSuccess = new MutableLiveData<>();

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

	public LiveData<Integer> getDeleteActionSuccess() {
		return deleteActionSuccess;
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

			List<T> body = response.body();
			List<T> currentList =
					(isRefresh || liveData.getValue() == null)
							? new ArrayList<>()
							: new ArrayList<>(liveData.getValue());

			currentList.addAll(body);
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
									deleteActionSuccess.setValue(position);
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
									deleteActionSuccess.setValue(position);
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
}
