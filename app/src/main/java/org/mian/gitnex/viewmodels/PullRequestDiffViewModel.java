package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.FileDiffView;
import org.mian.gitnex.helpers.ParseDiff;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class PullRequestDiffViewModel extends ViewModel {

	private final MutableLiveData<List<Commit>> commits = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<FileDiffView>> files =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<String> filesHeader = new MutableLiveData<>("");
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isFilesLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isCommitsLoading = new MutableLiveData<>(false);

	private int commitPage = 1;
	private boolean isLastCommitPage = false;

	public LiveData<List<Commit>> getCommits() {
		return commits;
	}

	public LiveData<List<FileDiffView>> getFiles() {
		return files;
	}

	public LiveData<String> getFilesHeader() {
		return filesHeader;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<Boolean> getIsFilesLoading() {
		return isFilesLoading;
	}

	public LiveData<Boolean> getIsCommitsLoading() {
		return isCommitsLoading;
	}

	public void fetchPRCommits(
			Context ctx, String owner, String name, long prId, int limit, boolean isRefresh) {
		if (!isRefresh) {
			if (Boolean.TRUE.equals(isCommitsLoading.getValue()) || isLastCommitPage) return;
		}

		isCommitsLoading.setValue(true);
		if (isRefresh) {
			commitPage = 1;
			isLastCommitPage = false;
		}

		RetrofitClient.getApiInterface(ctx)
				.repoGetPullRequestCommits(owner, name, prId, commitPage, limit, null, null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Commit>> call,
									@NonNull Response<List<Commit>> response) {
								isCommitsLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									List<Commit> current =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	commits.getValue()));
									current.addAll(response.body());
									commits.setValue(current);

									if (response.body().size() < limit) {
										isLastCommitPage = true;
									} else {
										commitPage++;
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Commit>> call, @NonNull Throwable t) {
								isCommitsLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchPRFiles(Context ctx, String owner, String name, long prId, Resources res) {
		if (Boolean.TRUE.equals(isFilesLoading.getValue())) return;

		isFilesLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.repoDownloadPullDiffOrPatch(owner, name, prId, "diff", null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<String> call,
									@NonNull Response<String> response) {
								isFilesLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									List<FileDiffView> parsedFiles =
											ParseDiff.getFileDiffViewArray(response.body());
									files.setValue(parsedFiles);

									int count = parsedFiles.size();
									String header =
											(count > 1)
													? res.getString(
															R.string.fileDiffViewHeader,
															String.valueOf(count))
													: res.getString(
															R.string.fileDiffViewHeaderSingle,
															String.valueOf(count));
									filesHeader.setValue(header);
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<String> call, @NonNull Throwable t) {
								isFilesLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void reset() {
		commits.setValue(new ArrayList<>());
		files.setValue(new ArrayList<>());
		filesHeader.setValue("");
		isCommitsLoading.setValue(false);
		isFilesLoading.setValue(false);
		errorMessage.setValue(null);
		commitPage = 1;
		isLastCommitPage = false;
	}
}
