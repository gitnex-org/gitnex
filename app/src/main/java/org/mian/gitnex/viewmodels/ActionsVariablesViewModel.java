package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.ActionVariable;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActionsVariablesViewModel extends ViewModel {

	private final MutableLiveData<List<ActionVariable>> variablesLiveData =
			new MutableLiveData<>(new ArrayList<>());
	private int currentPage = 1;
	private boolean hasMoreData = true;
	private boolean isLoading = false;

	public LiveData<List<ActionVariable>> getVariables() {
		return variablesLiveData;
	}

	public boolean isLoading() {
		return isLoading;
	}

	public void fetchVariables(Context ctx, String owner, String repo, int limit) {
		if (!hasMoreData || isLoading) {
			return;
		}

		isLoading = true;
		Call<List<ActionVariable>> call =
				RetrofitClient.getApiInterface(ctx)
						.getRepoVariablesList(owner, repo, currentPage, limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ActionVariable>> call,
							@NonNull Response<List<ActionVariable>> response) {
						isLoading = false;
						if (response.code() == 200 && response.body() != null) {
							List<ActionVariable> newVariables = response.body();
							List<ActionVariable> currentVariables = variablesLiveData.getValue();
							if (currentVariables != null) {
								currentVariables.addAll(newVariables);
								variablesLiveData.setValue(currentVariables);
							}
							hasMoreData = newVariables.size() >= limit;
							if (hasMoreData) {
								currentPage++;
							}
						} else {
							hasMoreData = false;
							if (currentPage == 1
									&& (response.body() == null || response.body().isEmpty())) {
								variablesLiveData.setValue(new ArrayList<>());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ActionVariable>> call, @NonNull Throwable t) {
						isLoading = false;
						hasMoreData = false;
						if (currentPage == 1) {
							variablesLiveData.setValue(new ArrayList<>());
						}
					}
				});
	}

	public void resetPagination() {
		currentPage = 1;
		hasMoreData = true;
		variablesLiveData.setValue(new ArrayList<>());
	}
}
