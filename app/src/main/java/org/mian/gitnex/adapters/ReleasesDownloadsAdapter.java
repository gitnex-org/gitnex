package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Attachment;
import org.mian.gitnex.databinding.ListReleasesDownloadsBinding;
import org.mian.gitnex.structs.FragmentRefreshListener;

/**
 * @author mmarif
 */
public class ReleasesDownloadsAdapter
		extends RecyclerView.Adapter<ReleasesDownloadsAdapter.ViewHolder> {

	private final List<Attachment> list;
	private final FragmentRefreshListener listener;

	public ReleasesDownloadsAdapter(List<Attachment> list, FragmentRefreshListener listener) {
		this.list = list;
		this.listener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(
				ListReleasesDownloadsBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Attachment item = list.get(position);
		holder.binding.downloadName.setText(item.getName());
		holder.itemView.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onRefresh(item.getBrowserDownloadUrl());
					}
				});
	}

	@Override
	public int getItemCount() {
		return list != null ? list.size() : 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ListReleasesDownloadsBinding binding;

		ViewHolder(ListReleasesDownloadsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
