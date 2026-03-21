package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.SearchResults;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoriesViewModel extends ViewModel {

	private final MutableLiveData<List<Repository>> repos =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);

	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<Repository>> getRepos() {
		return repos;
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

	public void fetchRepos(
			Context ctx,
			String type,
			String userLogin,
			String orgName,
			int page,
			int limit,
			String sort,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		String fetchType = type != null ? type : "repos";

		Call<List<Repository>> call =
				switch (fetchType) {
					case "userRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.userListRepos(userLogin, page, limit);

					case "starredRepos" -> {
						if (userLogin != null && !userLogin.isEmpty()) {
							yield RetrofitClient.getApiInterface(ctx)
									.userListStarred(userLogin, page, limit);
						} else {
							yield RetrofitClient.getApiInterface(ctx)
									.userCurrentListStarred(page, limit);
						}
					}

					case "myRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.customUserListRepos(userLogin, page, limit, sort);

					case "org" ->
							RetrofitClient.getApiInterface(ctx).orgListRepos(orgName, page, limit);

					case "team" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListTeamRepos(Long.valueOf(userLogin), page, limit);

					case "watched" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListSubscriptions(page, limit);

					default ->
							RetrofitClient.getApiInterface(ctx)
									.customUserCurrentListRepos(page, limit, sort);
				};

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {
						handleResponse(response, isRefresh, limit);
						hasLoadedOnce.setValue(true);
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
						hasLoadedOnce.setValue(true);
					}
				});
	}

	private void handleResponse(Response<List<Repository>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) totalCount = Integer.parseInt(totalHeader);

			List<Repository> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(repos.getValue()));

			currentList.addAll(response.body());
			repos.setValue(currentList);

			if (response.body().size() < limit || currentList.size() >= totalCount) {
				isLastPage = true;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
	}

	public void searchExploreRepos(
			Context ctx,
			String query,
			boolean includeTopic,
			boolean includeDesc,
			boolean includeTemplate,
			boolean onlyArchived,
			int page,
			int limit,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		Call<SearchResults> call =
				RetrofitClient.getApiInterface(ctx)
						.repoSearch(
								query,
								includeTopic,
								includeDesc,
								null,
								null,
								null,
								null,
								true,
								null,
								includeTemplate,
								onlyArchived,
								null,
								null,
								"updated",
								"desc",
								page,
								limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<SearchResults> call,
							@NonNull Response<SearchResults> response) {
						handleSearchResponse(response, isRefresh, limit);
						hasLoadedOnce.setValue(true);
					}

					@Override
					public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
						hasLoadedOnce.setValue(true);
					}
				});
	}

	private void handleSearchResponse(
			Response<SearchResults> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		if (response.isSuccessful() && response.body() != null) {
			List<Repository> newRepos = response.body().getData();

			List<Repository> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(repos.getValue()));

			currentList.addAll(newRepos);
			repos.setValue(currentList);

			if (newRepos.size() < limit) {
				isLastPage = true;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
	}

	public void resetPagination() {
		this.isLastPage = false;
		this.totalCount = -1;
		this.hasLoadedOnce.setValue(false);
	}
}
