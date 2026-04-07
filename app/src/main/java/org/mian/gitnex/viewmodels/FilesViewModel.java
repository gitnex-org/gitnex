package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Branch;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class FilesViewModel extends ViewModel {

	private final MutableLiveData<List<RepoGetContentsList>> files = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isFilesLoading = new MutableLiveData<>();
	private final MutableLiveData<List<Branch>> branches = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isBranchesLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isLastPageBranches = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

	private int currentBranchPage = 1;

	public LiveData<List<RepoGetContentsList>> getFiles() {
		return files;
	}

	public LiveData<Boolean> getIsFilesLoading() {
		return isFilesLoading;
	}

	public LiveData<List<Branch>> getBranches() {
		return branches;
	}

	public LiveData<Boolean> getIsBranchesLoading() {
		return isBranchesLoading;
	}

	public LiveData<Boolean> getIsLastPageBranches() {
		return isLastPageBranches;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public void loadFiles(Context ctx, String owner, String repo, String ref, String path) {
		isFilesLoading.setValue(true);

		Call<List<RepoGetContentsList>> call =
				(path == null || path.isEmpty())
						? ApiRetrofitClient.getInstance(ctx).getRepoContents(owner, repo, ref)
						: ApiRetrofitClient.getInstance(ctx)
								.getRepoContents(owner, repo, path, ref);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<RepoGetContentsList>> call,
							@NonNull Response<List<RepoGetContentsList>> response) {
						isFilesLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							List<RepoGetContentsList> list = response.body();

							list.sort(
									(o1, o2) -> {
										if (o1.getType().equals(o2.getType())) {
											return o1.getName().compareToIgnoreCase(o2.getName());
										}
										return o1.getType().equals("dir") ? -1 : 1;
									});

							files.setValue(list);
						} else {
							files.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<RepoGetContentsList>> call, @NonNull Throwable t) {
						isFilesLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
					}
				});
	}

	public void loadBranches(Context ctx, String owner, String repo) {
		if (Boolean.TRUE.equals(isBranchesLoading.getValue())
				|| Boolean.TRUE.equals(isLastPageBranches.getValue())) return;

		isBranchesLoading.setValue(true);
		int limit = Constants.getCurrentResultLimit(ctx);

		RetrofitClient.getApiInterface(ctx)
				.repoListBranches(owner, repo, currentBranchPage, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Branch>> call,
									@NonNull Response<List<Branch>> response) {
								isBranchesLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									List<Branch> newBranches = response.body();

									List<Branch> currentList =
											new ArrayList<>(
													Objects.requireNonNull(branches.getValue()));
									currentList.addAll(newBranches);
									branches.setValue(currentList);

									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										int totalItems = Integer.parseInt(totalHeader);
										isLastPageBranches.setValue(
												currentBranchPage
														>= Math.ceil((double) totalItems / limit));
									} else {
										isLastPageBranches.setValue(newBranches.size() < limit);
									}
									currentBranchPage++;
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Branch>> call, @NonNull Throwable t) {
								isBranchesLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void resetBranches() {
		branches.setValue(new ArrayList<>());
		currentBranchPage = 1;
		isLastPageBranches.setValue(false);
	}
}
