package org.mian.gitnex.helpers.attachments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CreateAttachmentsAdapter;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class AttachmentManager {

	private final Context context;
	private final List<AttachmentsModel> attachments = new ArrayList<>();
	private final List<Uri> pendingUris = new ArrayList<>();
	private CreateAttachmentsAdapter adapter;
	private AttachmentListener listener;
	private ActivityResultLauncher<Intent> filePickerLauncher;
	private int maxFileCount = -1;
	private long maxFileSizeBytes = -1;

	private static final Set<String> COMMON_ALLOWED_EXTENSIONS =
			new HashSet<>(
					Arrays.asList(
							"txt", "md", "json", "jsonc", "log", "csv", "patch", "jpg", "jpeg",
							"png", "gif", "webp", "svg", "avif", "pdf", "docx", "pptx", "xls",
							"xlsx", "odt", "ods", "odp", "odf", "odg", "zip", "gz", "tgz", "mp4",
							"mov", "webm"));

	public interface AttachmentListener {
		void onAttachmentsChanged(int count);

		void onAttachmentAdded(Uri uri);

		void onAttachmentRemoved(int position);

		void onAttachmentRejected(String reason);
	}

	public AttachmentManager(Context context) {
		this.context = context;
	}

	public void setMaxFileCount(int maxFileCount) {
		this.maxFileCount = maxFileCount;
	}

	public void setMaxFileSize(long maxFileSizeBytes) {
		this.maxFileSizeBytes = maxFileSizeBytes;
	}

	public void setListener(AttachmentListener listener) {
		this.listener = listener;
	}

	public void registerFilePicker(ActivityResultLauncher<Intent> launcher) {
		this.filePickerLauncher = launcher;
	}

	public CreateAttachmentsAdapter createAdapter() {
		adapter = new CreateAttachmentsAdapter(attachments, this::removeAttachment);
		return adapter;
	}

	public void openFilePicker() {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.setType("*/*");
		intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

		intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		filePickerLauncher.launch(intent);
	}

	public void handleFilePickerResult(Uri uri) {
		if (uri != null) {
			try {
				context.getContentResolver()
						.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
			} catch (Exception ignored) {
			}
			addAttachment(uri);
		}
	}

	private void addAttachment(Uri uri) {
		String fileName = AttachmentUtils.queryName(context, uri);
		String extension = FilenameUtils.getExtension(fileName).toLowerCase();

		if (maxFileCount > 0 && attachments.size() >= maxFileCount) {
			if (listener != null) {
				listener.onAttachmentRejected(
						context.getString(R.string.attachment_limit_count, maxFileCount));
			}
			return;
		}

		long fileSize = AttachmentUtils.getFileSize(context, uri);

		if (maxFileSizeBytes > 0 && fileSize > maxFileSizeBytes) {
			if (listener != null) {
				String sizeStr = AttachmentUtils.formatFileSize(maxFileSizeBytes);
				listener.onAttachmentRejected(
						context.getString(R.string.attachment_limit_size, sizeStr));
			}
			return;
		}

		if (!COMMON_ALLOWED_EXTENSIONS.contains(extension)) {
			Toasty.show(context, context.getString(R.string.attachmentTypeNotSupported, extension));
		}

		AttachmentsModel attachment = new AttachmentsModel(fileName, uri);
		attachment.setFileSize(fileSize);

		attachments.add(attachment);
		pendingUris.add(uri);

		if (adapter != null) {
			adapter.notifyItemInserted(attachments.size() - 1);
		}

		if (listener != null) {
			listener.onAttachmentsChanged(attachments.size());
			listener.onAttachmentAdded(uri);
		}
	}

	private void removeAttachment(int position) {
		if (position >= 0 && position < attachments.size()) {
			pendingUris.remove(position);
			attachments.remove(position);

			if (adapter != null) {
				adapter.notifyItemRemoved(position);
			}

			if (listener != null) {
				listener.onAttachmentsChanged(attachments.size());
				listener.onAttachmentRemoved(position);
			}
		}
	}

	public List<Uri> getPendingUris() {
		return new ArrayList<>(pendingUris);
	}

	public int getAttachmentCount() {
		return attachments.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void clear() {
		attachments.clear();
		pendingUris.clear();
		if (adapter != null) adapter.notifyDataSetChanged();
	}
}
