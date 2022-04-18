package org.mian.gitnex.adapters;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.Attachment;
import org.mian.gitnex.R;
import java.util.List;

/**
 * Author M M Arif
 **/

public class ReleasesDownloadsAdapter extends RecyclerView.Adapter<ReleasesDownloadsAdapter.ReleasesDownloadsViewHolder> {

	private final List<Attachment> releasesDownloadsList;

	static class ReleasesDownloadsViewHolder extends RecyclerView.ViewHolder {

		private final TextView downloadName;

		private ReleasesDownloadsViewHolder(View itemView) {

			super(itemView);
			downloadName = itemView.findViewById(R.id.downloadName);
		}
	}

	ReleasesDownloadsAdapter(List<Attachment> releasesDownloadsMain) {

		this.releasesDownloadsList = releasesDownloadsMain;
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

			holder.downloadName.setText(
				HtmlCompat.fromHtml("<a href='" + currentItem.getBrowserDownloadUrl() + "'>" + currentItem.getName() + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
			holder.downloadName.setMovementMethod(LinkMovementMethod.getInstance());

		}
	}

	@Override
	public int getItemCount() {
		return releasesDownloadsList.size();
	}

}
