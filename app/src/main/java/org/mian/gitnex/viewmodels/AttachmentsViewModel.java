package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.gitnex.tea4j.v2.models.Attachment;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.attachments.AttachmentUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class AttachmentsViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>(false);
	private final MutableLiveData<String> uploadError = new MutableLiveData<>();
	private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>(0);
	private final MutableLiveData<Boolean> uploadComplete = new MutableLiveData<>(false);
	private final MutableLiveData<List<Attachment>> attachments = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoadingAttachments = new MutableLiveData<>(false);
	private final MutableLiveData<String> fetchError = new MutableLiveData<>();

	private final List<Uri> pendingUploads = new ArrayList<>();
	private int totalUploads = 0;
	private int completedUploads = 0;

	public LiveData<Boolean> getIsUploading() {
		return isUploading;
	}

	public LiveData<String> getUploadError() {
		return uploadError;
	}

	public LiveData<Boolean> getUploadComplete() {
		return uploadComplete;
	}

	public LiveData<List<Attachment>> getAttachments() {
		return attachments;
	}

	public LiveData<Boolean> getIsLoadingAttachments() {
		return isLoadingAttachments;
	}

	public LiveData<String> getFetchError() {
		return fetchError;
	}

	public void clearFetchError() {
		fetchError.setValue(null);
	}

	public enum TargetType {
		ISSUE,
		COMMENT
	}

	public void addPendingUpload(Uri uri) {
		pendingUploads.add(uri);
	}

	public void reset() {
		isUploading.setValue(false);
		uploadError.setValue(null);
		uploadProgress.setValue(0);
		uploadComplete.setValue(false);
		pendingUploads.clear();
		totalUploads = 0;
		completedUploads = 0;
	}

	public void clearPendingUploads() {
		pendingUploads.clear();
		totalUploads = 0;
		completedUploads = 0;
	}

	public void uploadAttachments(
			Context context, String repoOwner, String repoName, long issueIndex) {
		if (pendingUploads.isEmpty()) {
			uploadComplete.setValue(true);
			return;
		}

		totalUploads = pendingUploads.size();
		completedUploads = 0;
		isUploading.setValue(true);
		uploadProgress.setValue(0);
		uploadComplete.setValue(false);

		uploadNext(context, repoOwner, repoName, issueIndex, 0);
	}

	private void uploadNext(
			Context context, String repoOwner, String repoName, long issueIndex, int index) {
		if (index >= pendingUploads.size()) {
			isUploading.setValue(false);
			uploadComplete.setValue(true);
			return;
		}

		Uri uri = pendingUploads.get(index);
		if (uri == null) {
			uploadNext(context, repoOwner, repoName, issueIndex, index + 1);
			return;
		}

		File file = AttachmentUtils.getFile(context, uri);
		if (file == null || !file.exists()) {
			isUploading.postValue(false);
			uploadError.postValue("File not found: " + uri);
			return;
		}

		String mimeType = context.getContentResolver().getType(uri);
		if (mimeType == null || mimeType.isEmpty()) {
			mimeType = "application/octet-stream";
		}

		MediaType mediaType = MediaType.parse(mimeType);
		if (mediaType == null) {
			mediaType = MediaType.parse("application/octet-stream");
		}

		RequestBody requestFile = RequestBody.create(file, mediaType);
		String fileName = AttachmentUtils.queryName(context, uri);
		if (fileName == null || fileName.isEmpty()) {
			fileName = "attachment";
		}

		Call<Attachment> call =
				RetrofitClient.getApiInterface(context)
						.issueCreateIssueAttachment(
								requestFile, repoOwner, repoName, issueIndex, fileName);

		String finalFileName = fileName;
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Attachment> call,
							@NonNull Response<Attachment> response) {
						if (response.isSuccessful() && response.code() == 201) {
							completedUploads++;
							int progress =
									totalUploads > 0
											? (completedUploads * 100) / totalUploads
											: 100;
							uploadProgress.postValue(progress);
							uploadNext(context, repoOwner, repoName, issueIndex, index + 1);
						} else {
							isUploading.postValue(false);
							String errorMsg = "Failed to upload: " + finalFileName;
							if (response.errorBody() != null) {
								try {
									errorMsg += " - " + response.errorBody().string();
								} catch (Exception ignored) {
								}
							}
							uploadError.postValue(errorMsg);
						}
					}

					@Override
					public void onFailure(@NonNull Call<Attachment> call, @NonNull Throwable t) {
						isUploading.postValue(false);
						uploadError.postValue("Upload failed: " + t.getMessage());
					}
				});
	}

	public void clearError() {
		uploadError.setValue(null);
	}

	public void fetchIssueAttachments(Context ctx, String owner, String repo, long issueIndex) {
		isLoadingAttachments.setValue(true);
		fetchError.setValue(null);

		Call<List<Attachment>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssueAttachments(owner, repo, issueIndex);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Attachment>> call,
							@NonNull Response<List<Attachment>> response) {
						isLoadingAttachments.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							attachments.setValue(response.body());
						} else {
							attachments.setValue(null);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Attachment>> call, @NonNull Throwable t) {
						isLoadingAttachments.setValue(false);
						fetchError.setValue(t.getMessage());
						attachments.setValue(null);
					}
				});
	}

	public void fetchCommentAttachments(Context ctx, String owner, String repo, long commentId) {
		isLoadingAttachments.setValue(true);
		fetchError.setValue(null);

		Call<List<Attachment>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssueCommentAttachments(owner, repo, commentId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Attachment>> call,
							@NonNull Response<List<Attachment>> response) {
						isLoadingAttachments.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							attachments.setValue(response.body());
						} else {
							attachments.setValue(null);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Attachment>> call, @NonNull Throwable t) {
						isLoadingAttachments.setValue(false);
						fetchError.setValue(t.getMessage());
						attachments.setValue(null);
					}
				});
	}

	public void clearAttachments() {
		attachments.setValue(null);
	}
}
