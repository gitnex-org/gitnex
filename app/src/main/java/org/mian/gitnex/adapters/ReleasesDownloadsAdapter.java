package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.Attachment;
import org.mian.gitnex.R;
import org.mian.gitnex.structs.FragmentRefreshListener;
import java.util.List;

/**
 * Author M M Arif
 **/

public class ReleasesDownloadsAdapter extends RecyclerView.Adapter<ReleasesDownloadsAdapter.ReleasesDownloadsViewHolder> {

	private final List<Attachment> releasesDownloadsList;
	private final FragmentRefreshListener startDownload;

	static class ReleasesDownloadsViewHolder extends RecyclerView.ViewHolder {

		private final TextView downloadName;

		private ReleasesDownloadsViewHolder(View itemView) {

			super(itemView);
			downloadName = itemView.findViewById(R.id.downloadName);
		}
	}

	ReleasesDownloadsAdapter(List<Attachment> releasesDownloadsMain, FragmentRefreshListener startDownload) {

		this.releasesDownloadsList = releasesDownloadsMain;
		this.startDownload = startDownload;
	}

	@NonNull
	@Override
	public ReleasesDownloadsAdapter.ReleasesDownloadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_releases_downloads, parent, false);
		return new ReleasesDownloadsAdapter.ReleasesDownloadsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ReleasesDownloadsAdapter.ReleasesDownloadsViewHolder holder, int position) {

		Attachment currentItem = releasesDownloadsList.get(position);

		if(currentItem.getName() != null) {

			holder.downloadName.setText(currentItem.getName());
			holder.downloadName.setOnClickListener(v -> startDownload.onRefresh(currentItem.getBrowserDownloadUrl()));

		}
	}

	@Override
	public int getItemCount() {
		return releasesDownloadsList.size();
	}

}
