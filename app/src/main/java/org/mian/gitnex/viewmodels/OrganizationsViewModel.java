package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class OrganizationsViewModel extends ViewModel {

	private final MutableLiveData<List<Organization>> orgs =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<Organization>> getOrgs() {
		return orgs;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public void fetchOrganizations(Context ctx, int page, int limit, boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.orgListCurrentUserOrgs(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchAllPublicOrgs(Context ctx, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgGetAll(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchUserOrgs(
			Context ctx, String username, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgListUserOrgs(username, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleResponse(
			Response<List<Organization>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);

		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) {
				totalCount = Integer.parseInt(totalHeader);
			}

			List<Organization> incomingList = response.body();
			List<Organization> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(orgs.getValue()));

			currentList.addAll(incomingList);
			orgs.setValue(currentList);

			if (incomingList.size() < limit
					|| (totalCount != -1 && currentList.size() >= totalCount)) {
				isLastPage = true;
			} else if (isRefresh) {
				isLastPage = false;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
	}
}
