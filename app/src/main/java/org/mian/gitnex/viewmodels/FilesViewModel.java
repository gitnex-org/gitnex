package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreateBranchRepoOption;
import org.gitnex.tea4j.v2.models.CreateFileOptions;
import org.gitnex.tea4j.v2.models.DeleteFileOptions;
import org.gitnex.tea4j.v2.models.FileDeleteResponse;
import org.gitnex.tea4j.v2.models.FileResponse;
import org.gitnex.tea4j.v2.models.UpdateFileOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class FilesViewModel extends ViewModel {

	public enum FileAction {
		CREATE,
		EDIT,
		DELETE
	}

	private final MutableLiveData<Boolean> isProcessing = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
	private final MutableLiveData<String> operationError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isCreatingBranch = new MutableLiveData<>(false);
	private final MutableLiveData<Branch> createdBranch = new MutableLiveData<>();
	private final MutableLiveData<String> createBranchError = new MutableLiveData<>();
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

	public LiveData<Boolean> getIsProcessing() {
		return isProcessing;
	}

	public LiveData<Boolean> getOperationSuccess() {
		return operationSuccess;
	}

	public LiveData<String> getOperationError() {
		return operationError;
	}

	public LiveData<Boolean> getIsCreatingBranch() {
		return isCreatingBranch;
	}

	public LiveData<Branch> getCreatedBranch() {
		return createdBranch;
	}

	public LiveData<String> getCreateBranchError() {
		return createBranchError;
	}

	public void clearOperationSuccess() {
		operationSuccess.setValue(null);
	}

	public void clearOperationError() {
		operationError.setValue(null);
	}

	public void clearCreatedBranch() {
		createdBranch.setValue(null);
	}

	public void clearCreateBranchError() {
		createBranchError.setValue(null);
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

	public void createFile(
			Context ctx,
			String owner,
			String repo,
			String fileName,
			String content,
			String commitMessage,
			String branchName) {
		isProcessing.setValue(true);
		operationError.setValue(null);

		CreateFileOptions options = new CreateFileOptions();
		options.setContent(AppUtil.encodeBase64(content));
		options.setMessage(commitMessage);
		options.setBranch(branchName);

		Call<FileResponse> call =
				RetrofitClient.getApiInterface(ctx).repoCreateFile(options, owner, repo, fileName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileResponse> call,
							@NonNull Response<FileResponse> response) {
						isProcessing.setValue(false);
						if (response.code() == 201) {
							operationSuccess.setValue(true);
						} else {
							handleFileOperationError(response.code(), ctx);
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {
						isProcessing.setValue(false);
						operationError.setValue(t.getMessage());
					}
				});
	}

	public void createFileFromUri(
			Context ctx,
			String owner,
			String repo,
			String fileName,
			Uri fileUri,
			String commitMessage,
			String branchName) {
		isProcessing.setValue(true);
		operationError.setValue(null);

		String base64Content = AppUtil.encodeUriToBase64(ctx, fileUri);

		CreateFileOptions options = new CreateFileOptions();
		options.setContent(base64Content);
		options.setMessage(commitMessage);
		options.setBranch(branchName);

		Call<FileResponse> call =
				RetrofitClient.getApiInterface(ctx).repoCreateFile(options, owner, repo, fileName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileResponse> call,
							@NonNull Response<FileResponse> response) {
						isProcessing.setValue(false);
						if (response.code() == 201) {
							operationSuccess.setValue(true);
						} else {
							handleFileOperationError(response.code(), ctx);
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {
						isProcessing.setValue(false);
						operationError.setValue(t.getMessage());
					}
				});
	}

	public void editFile(
			Context ctx,
			String owner,
			String repo,
			String fileName,
			String content,
			String commitMessage,
			String branchName,
			String fileSha) {
		isProcessing.setValue(true);
		operationError.setValue(null);

		UpdateFileOptions options = new UpdateFileOptions();
		options.setContent(AppUtil.encodeBase64(content));
		options.setMessage(commitMessage);
		options.setSha(fileSha);
		options.setBranch(branchName);

		Call<FileResponse> call =
				RetrofitClient.getApiInterface(ctx).repoUpdateFile(options, owner, repo, fileName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileResponse> call,
							@NonNull Response<FileResponse> response) {
						isProcessing.setValue(false);
						if (response.code() == 200) {
							operationSuccess.setValue(true);
						} else {
							handleFileOperationError(response.code(), ctx);
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {
						isProcessing.setValue(false);
						operationError.setValue(t.getMessage());
					}
				});
	}

	public void deleteFile(
			Context ctx,
			String owner,
			String repo,
			String fileName,
			String commitMessage,
			String branchName,
			String fileSha) {
		isProcessing.setValue(true);
		operationError.setValue(null);

		DeleteFileOptions options = new DeleteFileOptions();
		options.setMessage(commitMessage);
		options.setSha(fileSha);
		options.setBranch(branchName);

		Call<FileDeleteResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.repoDeleteFileWithBody(owner, repo, fileName, options);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<FileDeleteResponse> call,
							@NonNull Response<FileDeleteResponse> response) {
						isProcessing.setValue(false);
						if (response.code() == 200) {
							operationSuccess.setValue(true);
						} else {
							handleFileOperationError(response.code(), ctx);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<FileDeleteResponse> call, @NonNull Throwable t) {
						isProcessing.setValue(false);
						operationError.setValue(t.getMessage());
					}
				});
	}

	public void createBranch(
			Context ctx, String owner, String repo, String branchName, String sourceRef) {
		isCreatingBranch.setValue(true);
		createBranchError.setValue(null);

		CreateBranchRepoOption options = new CreateBranchRepoOption();
		options.setNewBranchName(branchName);
		options.setOldRefName(sourceRef);

		Call<Branch> call =
				RetrofitClient.getApiInterface(ctx).repoCreateBranch(owner, repo, options);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Branch> call, @NonNull Response<Branch> response) {
						isCreatingBranch.setValue(false);
						if (response.code() == 201 && response.body() != null) {
							createdBranch.setValue(response.body());
						} else {
							handleBranchError(response.code(), ctx, branchName);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Branch> call, @NonNull Throwable t) {
						isCreatingBranch.setValue(false);
						createBranchError.setValue(t.getMessage());
					}
				});
	}

	private void handleFileOperationError(int code, Context ctx) {
		switch (code) {
			case 401:
				operationError.setValue("UNAUTHORIZED");
				break;
			case 404:
				operationError.setValue(ctx.getString(R.string.apiNotFound));
				break;
			case 409:
				operationError.setValue(ctx.getString(R.string.file_conflict_error));
				break;
			default:
				operationError.setValue(ctx.getString(R.string.genericError));
		}
	}

	private void handleBranchError(int code, Context ctx, String branchName) {
		switch (code) {
			case 401:
				createBranchError.setValue("UNAUTHORIZED");
				break;
			case 403:
				createBranchError.setValue(ctx.getString(R.string.branch_error_archive_mirror));
				break;
			case 404:
				createBranchError.setValue(ctx.getString(R.string.branch_error_ref_not_found));
				break;
			case 409:
				createBranchError.setValue(ctx.getString(R.string.branch_error_exists, branchName));
				break;
			case 423:
				createBranchError.setValue(ctx.getString(R.string.branch_error_repo_locked));
				break;
			default:
				createBranchError.setValue(ctx.getString(R.string.genericError));
		}
	}
}
