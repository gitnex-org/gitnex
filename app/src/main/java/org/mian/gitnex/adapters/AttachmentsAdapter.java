package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.attachments.AttachmentsModel;

/**
 * @author M M Arif
 */
public class AttachmentsAdapter extends RecyclerView.Adapter<AttachmentsAdapter.ViewHolder> {

	private static AttachmentsReceiverListener AttachmentsReceiveListener;
	private List<AttachmentsModel> attachmentsList;
	private final Context ctx;

	public AttachmentsAdapter(List<AttachmentsModel> attachmentsList, Context ctx) {
		this.attachmentsList = attachmentsList;
		this.ctx = ctx;
	}

	@Override
	public int getItemCount() {
		return attachmentsList == null ? 0 : attachmentsList.size();
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_attachments, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

		AttachmentsModel currentItem = attachmentsList.get(position);
		holder.attachmentsModel = currentItem;

		holder.filename.setText(currentItem.getFileName());

		if (Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif")
				.contains(FilenameUtils.getExtension(currentItem.getFileName()).toLowerCase())) {

			holder.attachmentViewFrame.setVisibility(View.VISIBLE);

			Glide.with(ctx)
					.load(currentItem.getUri())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.error(R.drawable.ic_close)
					.into(holder.attachment);
		} else {
			holder.attachmentViewFrame.setVisibility(View.GONE);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		public TextView filename;
		public ImageView delete;
		public MaterialCardView attachmentViewFrame;
		public ImageView attachment;
		private AttachmentsModel attachmentsModel;

		public ViewHolder(View itemView) {
			super(itemView);

			filename = itemView.findViewById(R.id.filename);
			delete = itemView.findViewById(R.id.delete_attachment);
			attachmentViewFrame = itemView.findViewById(R.id.attachmentViewFrame);
			attachment = itemView.findViewById(R.id.attachment);

			delete.setOnClickListener(
					itemDelete -> {
						AttachmentsReceiveListener.setAttachmentsData(attachmentsModel.getUri());
						deleteAttachment(getBindingAdapterPosition());
						notifyDataChanged();
					});
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<AttachmentsModel> list) {

		attachmentsList = list;
		notifyDataChanged();
	}

	private void deleteAttachment(int position) {

		attachmentsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, attachmentsList.size());
	}

	public interface AttachmentsReceiverListener {
		void setAttachmentsData(Uri myData);
	}

	public static void setAttachmentsReceiveListener(
			AttachmentsReceiverListener attachmentsListener) {
		AttachmentsReceiveListener = attachmentsListener;
	}
}
