package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListTagsBinding;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.helpers.Markdown;

/**
 * @author qwerty287
 * @author mmarif
 */
public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

	private final Context context;
	private List<Tag> tags;
	private final ReleasesFragment.OnReleaseItemClickListener listener;
	private final boolean canDelete;

	public TagsAdapter(
			Context context,
			List<Tag> tags,
			boolean canDelete,
			ReleasesFragment.OnReleaseItemClickListener listener) {
		this.context = context;
		this.tags = tags;
		this.canDelete = canDelete;
		this.listener = listener;
	}

	@NonNull @Override
	public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TagsViewHolder(
				ListTagsBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
		Tag tag = tags.get(position);
		ListTagsBinding b = holder.binding;

		b.tagName.setText(tag.getName());

		if (tag.getMessage() != null && !tag.getMessage().isEmpty()) {
			b.tagBodyContent.setVisibility(View.VISIBLE);
			Markdown.render(context, tag.getMessage(), b.tagBodyContent);
		} else {
			b.tagBodyContent.setVisibility(View.GONE);
		}

		b.deleteTag.setVisibility(canDelete ? View.VISIBLE : View.GONE);
		b.deleteTag.setOnClickListener(
				v -> listener.onDelete(tag, holder.getBindingAdapterPosition()));

		b.zipDownload.downloadName.setText(R.string.zipArchiveDownloadReleasesTab);
		b.tarDownload.downloadName.setText(R.string.tarArchiveDownloadReleasesTab);

		b.downloadCopyFrame.setOnClickListener(
				v -> {
					boolean isVisible = b.downloads.getVisibility() == View.VISIBLE;
					b.downloads.setVisibility(isVisible ? View.GONE : View.VISIBLE);
					b.downloadDropdownIcon.setImageResource(
							isVisible ? R.drawable.ic_chevron_right : R.drawable.ic_chevron_down);
				});

		b.zipDownload.getRoot().setOnClickListener(v -> listener.onDownload(tag.getZipballUrl()));
		b.tarDownload.getRoot().setOnClickListener(v -> listener.onDownload(tag.getTarballUrl()));

		b.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return tags != null ? tags.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Tag> newList) {
		this.tags = newList;
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		if (position >= 0 && position < tags.size()) {
			tags.remove(position);
			notifyItemRemoved(position);
			notifyItemRangeChanged(position, tags.size());
		}
	}

	public static class TagsViewHolder extends RecyclerView.ViewHolder {
		final ListTagsBinding binding;

		TagsViewHolder(ListTagsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
