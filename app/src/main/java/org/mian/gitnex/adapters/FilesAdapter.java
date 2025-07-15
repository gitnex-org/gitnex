package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.TimeHelper;

/**
 * @author mmarif
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder>
		implements Filterable {

	private final List<ContentsResponse> originalFiles = new ArrayList<>();
	private final List<ContentsResponse> alteredFiles = new ArrayList<>();
	private final Context context;
	private final FilesAdapterListener filesListener;
	private final Filter filesFilter =
			new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					List<ContentsResponse> filteredList = new ArrayList<>();
					if (constraint == null || constraint.length() == 0) {
						filteredList.addAll(originalFiles);
					} else {
						String filterPattern = constraint.toString().toLowerCase().trim();
						for (ContentsResponse item : originalFiles) {
							if (item.getName().toLowerCase().contains(filterPattern)
									|| item.getPath().toLowerCase().contains(filterPattern)) {
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
					alteredFiles.addAll((List<ContentsResponse>) results.values);
					notifyDataSetChanged();
				}
			};

	public FilesAdapter(Context ctx, FilesAdapterListener filesListener) {
		this.context = ctx;
		this.filesListener = filesListener;
	}

	public List<ContentsResponse> getOriginalFiles() {
		return originalFiles;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyOriginalDataSetChanged() {
		alteredFiles.clear();
		alteredFiles.addAll(originalFiles);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public FilesAdapter.FilesViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_files, parent, false);
		return new FilesAdapter.FilesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull FilesAdapter.FilesViewHolder holder, int position) {

		Locale locale = context.getResources().getConfiguration().getLocales().get(0);
		ContentsResponse currentItem = alteredFiles.get(position);

		holder.file = currentItem;
		holder.fileName.setText(currentItem.getName());

		holder.fileTypeIs.setImageDrawable(
				AppCompatResources.getDrawable(
						context,
						FileIcon.getIconResource(currentItem.getName(), currentItem.getType())));

		switch (currentItem.getType()) {
			case "file":
				holder.fileInfo.setVisibility(View.VISIBLE);
				holder.fileInfo.setText(
						FileUtils.byteCountToDisplaySize(Math.toIntExact(currentItem.getSize())));
				break;
			case "dir":
			case "submodule":
			case "symlink":
			default:
				holder.fileInfo.setVisibility(View.GONE);
				break;
		}

		if (currentItem.getLastCommitterDate() != null) {
			holder.fileDate.setText(
					TimeHelper.formatTime(currentItem.getLastCommitterDate(), locale));
			holder.fileDate.setVisibility(View.VISIBLE);
			holder.fileDate.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(
									currentItem.getLastCommitterDate()),
							context));
		} else {
			holder.fileDate.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return alteredFiles.size();
	}

	@Override
	public Filter getFilter() {
		return filesFilter;
	}

	public interface FilesAdapterListener {
		void onClickFile(ContentsResponse file);
	}

	public class FilesViewHolder extends RecyclerView.ViewHolder {

		private final ImageView fileTypeIs;
		private final TextView fileName;
		private final TextView fileInfo;
		private final TextView fileDate;
		private ContentsResponse file;

		private FilesViewHolder(View itemView) {
			super(itemView);
			LinearLayout fileFrame = itemView.findViewById(R.id.fileFrame);
			fileName = itemView.findViewById(R.id.fileName);
			fileTypeIs = itemView.findViewById(R.id.fileTypeIs);
			fileInfo = itemView.findViewById(R.id.fileInfo);
			fileDate = itemView.findViewById(R.id.fileDate);
			fileFrame.setOnClickListener(v -> filesListener.onClickFile(file));
		}
	}
}
