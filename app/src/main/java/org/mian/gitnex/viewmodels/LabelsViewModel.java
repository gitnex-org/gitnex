package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
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

	private final List<Label> fullList = new ArrayList<>();

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
		labels.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void fetchLabels(
			Context ctx,
			String owner,
			String repo,
			String type,
			int page,
			int limit,
			boolean isRefresh) {

		if (isLoading.getValue() != null && isLoading.getValue() && !isRefresh) return;

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
							if (isRefresh) {
								fullList.clear();
							}

							for (Label newLabel : response.body()) {
								if (!fullList.contains(newLabel)) {
									fullList.add(newLabel);
								}
							}

							labels.setValue(new ArrayList<>(fullList));
						} else {
							error.setValue("Error: " + response.code());
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
}
