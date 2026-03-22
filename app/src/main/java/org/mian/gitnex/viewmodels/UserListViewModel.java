package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class UserListViewModel extends ViewModel {

	private final MutableLiveData<List<User>> users = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);

	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<User>> getUsers() {
		return users;
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

	public void fetchUsers(
			Context ctx,
			String type,
			String owner,
			String idOrRepo,
			String query,
			int page,
			int limit,
			boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		if ("explore".equals(type)) {
			RetrofitClient.getApiInterface(ctx)
					.userSearch(query, null, page, limit)
					.enqueue(
							new Callback<>() {
								@Override
								public void onResponse(
										@NonNull Call<InlineResponse2001> call,
										@NonNull Response<InlineResponse2001> response) {
									handleResponse(
											response.isSuccessful() && response.body() != null
													? response.body().getData()
													: null,
											response,
											isRefresh,
											limit);
								}

								@Override
								public void onFailure(
										@NonNull Call<InlineResponse2001> call,
										@NonNull Throwable t) {
									handleError(t);
								}
							});
		} else {
			Call<List<User>> call =
					switch (type) {
						case "watchers" ->
								RetrofitClient.getApiInterface(ctx)
										.repoListSubscribers(owner, idOrRepo, page, limit);
						case "stargazers" ->
								RetrofitClient.getApiInterface(ctx)
										.repoListStargazers(owner, idOrRepo, page, limit);
						case "followers" ->
								RetrofitClient.getApiInterface(ctx)
										.userListFollowers(owner, page, limit);
						case "following" ->
								RetrofitClient.getApiInterface(ctx)
										.userListFollowing(owner, page, limit);
						case "org_members" ->
								RetrofitClient.getApiInterface(ctx)
										.orgListMembers(owner, page, limit);
						case "team_members" ->
								RetrofitClient.getApiInterface(ctx)
										.orgListTeamMembers(Long.parseLong(idOrRepo), page, limit);
						default -> throw new IllegalArgumentException("Unknown type: " + type);
					};

			call.enqueue(
					new Callback<>() {
						@Override
						public void onResponse(
								@NonNull Call<List<User>> call,
								@NonNull Response<List<User>> response) {
							handleResponse(response.body(), response, isRefresh, limit);
						}

						@Override
						public void onFailure(
								@NonNull Call<List<User>> call, @NonNull Throwable t) {
							handleError(t);
						}
					});
		}
	}

	private void handleResponse(
			List<User> body, Response<?> response, boolean isRefresh, int limit) {
		if (response.isSuccessful() && body != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) totalCount = Integer.parseInt(totalHeader);

			List<User> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(users.getValue()));
			currentList.addAll(body);
			users.setValue(currentList);

			if (body.size() < limit || (totalCount != -1 && currentList.size() >= totalCount)) {
				isLastPage = true;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
		hasLoadedOnce.setValue(true);
		isLoading.setValue(false);
	}

	private void handleError(Throwable t) {
		errorMessage.setValue(t.getMessage());
		hasLoadedOnce.setValue(true);
		isLoading.setValue(false);
	}

	public void resetPagination() {
		this.isLastPage = false;
		this.totalCount = -1;
		this.hasLoadedOnce.setValue(false);
		this.users.setValue(new ArrayList<>());
	}
}
