package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueMeta;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class DependenciesViewModel extends ViewModel {

	private final MutableLiveData<List<Issue>> dependencies =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<Issue>> searchResults =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isSearching = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<String> actionMessage = new MutableLiveData<>();

	public LiveData<List<Issue>> getDependencies() {
		return dependencies;
	}

	public LiveData<List<Issue>> getSearchResults() {
		return searchResults;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsSearching() {
		return isSearching;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<String> getActionMessage() {
		return actionMessage;
	}

	public void clearActionMessage() {
		actionMessage.setValue(null);
	}

	public void clearSearchResults() {
		searchResults.setValue(new ArrayList<>());
	}

	public void loadDependencies(Context ctx, String owner, String repo, long issueIndex) {
		isLoading.setValue(true);

		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssueDependencies(owner, repo, String.valueOf(issueIndex), 1, 10);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						isLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							dependencies.setValue(response.body());
						} else {
							dependencies.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						dependencies.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	public void searchIssues(
			Context ctx,
			String owner,
			String repo,
			String query,
			long currentIssueId,
			List<Issue> existingDeps) {
		isSearching.setValue(true);

		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssues(
								owner, repo, "open", null, query, null, null, null, null, null,
								null, null, 1, 3);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						isSearching.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							List<Long> dependencyIds = new ArrayList<>();
							for (Issue dep : existingDeps) {
								dependencyIds.add(dep.getId());
							}

							List<Issue> filtered = new ArrayList<>();
							for (Issue result : response.body()) {
								if (result.getId() != currentIssueId
										&& !dependencyIds.contains(result.getId())) {
									filtered.add(result);
								}
							}
							searchResults.setValue(filtered);
						} else {
							searchResults.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						isSearching.setValue(false);
						searchResults.setValue(new ArrayList<>());
						error.setValue(t.getMessage());
					}
				});
	}

	public void addDependency(
			Context ctx, String owner, String repo, long issueId, Issue dependency) {
		IssueMeta meta = new IssueMeta();
		meta.setOwner(owner);
		meta.setRepo(repo);
		meta.setIndex(dependency.getNumber());

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateIssueDependencies(owner, repo, String.valueOf(issueId), meta);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						if (response.isSuccessful()) {
							searchResults.setValue(new ArrayList<>());
							loadDependencies(ctx, owner, repo, issueId);
							actionMessage.setValue(ctx.getString(R.string.dependency_added));
						} else {
							actionMessage.setValue(ctx.getString(R.string.dependency_add_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						actionMessage.setValue(ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void removeDependency(
			Context ctx, String owner, String repo, long issueId, Issue dependency) {
		IssueMeta meta = new IssueMeta();
		meta.setOwner(owner);
		meta.setRepo(repo);
		meta.setIndex(dependency.getNumber());

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.issueRemoveIssueDependencies2(owner, repo, String.valueOf(issueId), meta);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							loadDependencies(ctx, owner, repo, issueId);
							actionMessage.setValue(ctx.getString(R.string.dependency_removed));
						} else {
							actionMessage.setValue(
									ctx.getString(R.string.dependency_removal_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						actionMessage.setValue(ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
