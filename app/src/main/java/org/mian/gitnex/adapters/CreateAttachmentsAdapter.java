package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ItemAttachmentBinding;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.attachments.AttachmentUtils;
import org.mian.gitnex.helpers.attachments.AttachmentsModel;

/**
 * @author mmarif
 */
public class CreateAttachmentsAdapter
		extends RecyclerView.Adapter<CreateAttachmentsAdapter.ViewHolder> {

	private final List<AttachmentsModel> attachments;
	private final OnRemoveListener removeListener;
	private static final List<String> IMAGE_EXTENSIONS =
			Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif", "avif");

	public interface OnRemoveListener {
		void onRemove(int position);
	}

	public CreateAttachmentsAdapter(List<AttachmentsModel> attachments, OnRemoveListener listener) {
		this.attachments = attachments;
		this.removeListener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemAttachmentBinding binding =
				ItemAttachmentBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		AttachmentsModel attachment = attachments.get(position);
		holder.bind(attachment, position);
	}

	@Override
	public int getItemCount() {
		return attachments.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemAttachmentBinding binding;

		ViewHolder(ItemAttachmentBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(AttachmentsModel attachment, int position) {
			binding.attachmentFileName.setText(attachment.getFileName());

			String mimeType =
					itemView.getContext().getContentResolver().getType(attachment.getUri());
			String info = AttachmentUtils.formatFileSize(attachment.getFileSize());
			if (mimeType != null && !mimeType.isEmpty()) {
				info += " • " + mimeType;
			}
			binding.attachmentFileInfo.setText(info);

			String extension = FilenameUtils.getExtension(attachment.getFileName()).toLowerCase();
			if (IMAGE_EXTENSIONS.contains(extension)
					|| (mimeType != null && mimeType.startsWith("image/"))) {
				loadImageThumbnail(attachment.getUri());
			} else {
				showFileIcon(attachment.getFileName());
			}

			binding.attachmentRemoveBtn.setOnClickListener(
					v -> {
						if (removeListener != null) {
							removeListener.onRemove(position);
						}
					});
		}

		private void loadImageThumbnail(android.net.Uri uri) {
			Glide.with(itemView.getContext())
					.load(uri)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.error(R.drawable.ic_file)
					.into(binding.attachmentPreview);
		}

		private void showFileIcon(String fileName) {
			Glide.with(itemView.getContext()).clear(binding.attachmentPreview);

			binding.attachmentPreview.setImageDrawable(
					AppCompatResources.getDrawable(
							itemView.getContext(), FileIcon.getIconResource(fileName, "file")));
		}
	}
}
