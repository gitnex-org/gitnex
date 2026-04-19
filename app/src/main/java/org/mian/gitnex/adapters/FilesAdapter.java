package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.databinding.ListFilesBinding;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder>
		implements Filterable {

	private final List<RepoGetContentsList> originalFiles = new ArrayList<>();
	private final List<RepoGetContentsList> alteredFiles = new ArrayList<>();
	private final Context context;
	private final FilesAdapterListener filesListener;

	public FilesAdapter(Context ctx, FilesAdapterListener filesListener) {
		this.context = ctx;
		this.filesListener = filesListener;
	}

	public interface FilesAdapterListener {
		void onClickFile(RepoGetContentsList file);

		void onMenuClick(RepoGetContentsList file);

		void onSearchFilterCompleted(int count);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setFiles(List<RepoGetContentsList> newList) {
		this.originalFiles.clear();
		this.originalFiles.addAll(newList);
		this.alteredFiles.clear();
		this.alteredFiles.addAll(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListFilesBinding binding =
				ListFilesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new FilesViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull FilesViewHolder holder, int position) {
		holder.bind(alteredFiles.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return alteredFiles.size();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<RepoGetContentsList> filteredList = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filteredList.addAll(originalFiles);
				} else {
					String filterPattern = constraint.toString().toLowerCase().trim();
					for (RepoGetContentsList item : originalFiles) {
						if (item.getName().toLowerCase().contains(filterPattern)
								|| (item.getPath() != null
										&& item.getPath().toLowerCase().contains(filterPattern))) {
							filteredList.add(item);
						}
					}
				}
				FilterResults results = new FilterResults();
				results.values = filteredList;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				alteredFiles.clear();
				if (results.values != null) {
					alteredFiles.addAll((List<RepoGetContentsList>) results.values);
				}
				notifyDataSetChanged();
				if (filesListener != null) {
					filesListener.onSearchFilterCompleted(alteredFiles.size());
				}
			}
		};
	}

	public class FilesViewHolder extends RecyclerView.ViewHolder {

		private final ListFilesBinding binding;

		private FilesViewHolder(ListFilesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(RepoGetContentsList item) {
			Locale locale = Locale.getDefault();
			String type = item.getType();
			boolean isFile = "file".equals(type);
			boolean isDir = "dir".equals(type);
			boolean isSymlink = "symlink".equals(type);
			boolean isClickableFile = isFile || isSymlink;
			Date committerDate = item.getCompatibleCommitDate();
			boolean hasDate = committerDate != null;

			binding.fileName.setText(item.getName());
			binding.fileTypeIs.setImageDrawable(
					AppCompatResources.getDrawable(
							context, FileIcon.getIconResource(item.getName(), type)));

			if (isFile || isSymlink) {
				binding.fileInfo.setVisibility(View.VISIBLE);
				binding.fileInfo.setText(
						FileUtils.byteCountToDisplaySize(Math.toIntExact(item.getSize())));
			} else if (isDir) {
				binding.fileInfo.setVisibility(View.VISIBLE);
				binding.fileInfo.setText(context.getString(R.string.directory));
			} else {
				binding.fileInfo.setVisibility(View.GONE);
			}

			if (isDir) {
				binding.ivChevron.setVisibility(View.VISIBLE);
				binding.ivChevron.setImageResource(R.drawable.ic_chevron_right);
				binding.ivChevron.setOnClickListener(null);
				binding.ivChevron.setBackground(null);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
			} else if (isClickableFile) {
				binding.ivChevron.setVisibility(View.VISIBLE);
				binding.ivChevron.setImageResource(R.drawable.ic_dotted_menu);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
				binding.ivChevron.setOnClickListener(v -> filesListener.onMenuClick(item));
			} else {
				binding.ivChevron.setVisibility(View.GONE);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
			}

			if (hasDate) {
				binding.fileDate.setVisibility(View.VISIBLE);
				binding.fileDate.setText(TimeHelper.formatTime(committerDate, locale));

				binding.fileDate.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												committerDate, Locale.getDefault())));
			} else {
				binding.fileDate.setVisibility(View.GONE);
			}
		}
	}
}
