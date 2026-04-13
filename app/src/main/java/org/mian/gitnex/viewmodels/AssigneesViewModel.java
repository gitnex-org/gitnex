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
public class AssigneesViewModel extends ViewModel {

	private final MutableLiveData<List<User>> assigneesLiveData =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>(null);

	private int currentPage = 1;
	private boolean hasMorePages = true;

	public LiveData<List<User>> getAssignees() {
		return assigneesLiveData;
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

	public void fetchAssignees(
			Context context, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (isRefresh) {
			currentPage = 1;
			hasMorePages = true;
			assigneesLiveData.setValue(new ArrayList<>());
		}

		if (!hasMorePages && !isRefresh) {
			return;
		}

		isLoading.setValue(true);

		Call<List<User>> call =
				RetrofitClient.getApiInterface(context).repoGetAssignees(owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {
						isLoading.setValue(false);
						hasLoadedOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							List<User> newAssignees = response.body();

							if (isRefresh) {
								assigneesLiveData.setValue(newAssignees);
							} else {
								List<User> currentList = assigneesLiveData.getValue();
								if (currentList == null) {
									currentList = new ArrayList<>();
								}
								currentList.addAll(newAssignees);
								assigneesLiveData.setValue(currentList);
							}

							hasMorePages = newAssignees.size() >= limit;
							currentPage = page;
						} else {
							error.setValue("Failed to load assignees");
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	public void clearError() {
		error.setValue(null);
	}

	public void reset() {
		currentPage = 1;
		hasMorePages = true;
		assigneesLiveData.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
		isLoading.setValue(false);
	}
}
