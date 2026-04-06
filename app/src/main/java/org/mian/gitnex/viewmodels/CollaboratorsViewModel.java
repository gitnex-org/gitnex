package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.AddCollaboratorOption;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CollaboratorsViewModel extends ViewModel {

	private final MutableLiveData<List<User>> collaborators = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<List<User>> searchedUsers = new MutableLiveData<>();

	private final List<User> fullList = new ArrayList<>();
	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<User>> getCollaborators() {
		return collaborators;
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

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<List<User>> getSearchedUsers() {
		return searchedUsers;
	}

	public void resetPagination() {
		fullList.clear();
		isLastPage = false;
		totalCount = -1;
		collaborators.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void clearSearch() {
		searchedUsers.setValue(new ArrayList<>());
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void fetchCollaborators(
			Context ctx, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.repoListCollaborators(owner, repo, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<User>> call,
									@NonNull Response<List<User>> response) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);
								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										totalCount = Integer.parseInt(totalHeader);
									}
									List<User> body = response.body();
									if (isRefresh) {
										fullList.clear();
									}
									for (User user : body) {
										if (!fullList.contains(user)) {
											fullList.add(user);
										}
									}
									collaborators.setValue(new ArrayList<>(fullList));
									if (body.size() < limit
											|| (totalCount != -1
													&& fullList.size() >= totalCount)) {
										isLastPage = true;
									}
								} else {
									if (response.code() == 404 && isRefresh) {
										collaborators.setValue(new ArrayList<>());
									}
									isLastPage = true;
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<User>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);
								error.setValue(t.getMessage());
							}
						});
	}

	public void searchGlobalUsers(Context ctx, String query, int page, int limit) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.userSearch(query, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<InlineResponse2001> call,
									@NonNull Response<InlineResponse2001> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									searchedUsers.setValue(response.body().getData());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void addCollaborator(
			Context ctx, String owner, String repo, String userName, String permission) {
		isActionLoading.setValue(true);

		AddCollaboratorOption option = new AddCollaboratorOption();
		option.setPermission(
				AddCollaboratorOption.PermissionEnum.valueOf(permission.toUpperCase()));

		RetrofitClient.getApiInterface(ctx)
				.repoAddCollaborator(owner, repo, userName, option)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isActionLoading.setValue(false);
								actionResult.setValue(response.code());
								if (!response.isSuccessful()) {
									handleError(response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void deleteCollaborator(Context ctx, String owner, String repo, String userName) {
		isActionLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.repoDeleteCollaborator(owner, repo, userName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isActionLoading.setValue(false);
								actionResult.setValue(response.code());
								if (!response.isSuccessful()) {
									handleError(response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	private void handleError(int code) {
		if (code == 401) error.setValue("Unauthorized");
		else if (code == 403) error.setValue("Forbidden");
		else if (code == 404) error.setValue("Not Found");
		else error.setValue("Error: " + code);
	}
}
