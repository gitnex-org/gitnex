package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
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

	public void resetPagination() {
		fullList.clear();
		isLastPage = false;
		totalCount = -1;
		collaborators.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
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
									if (response.code() != 404) {
										error.setValue("Error: " + response.code());
									}
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
}
