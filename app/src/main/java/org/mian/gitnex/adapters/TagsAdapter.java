package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.gitnex.tea4j.models.GitTag;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;

/**
 * Author qwerty287
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private List<GitTag> tags;
    private final Context context;
    private final String repo;
    private final String owner;
	private Context ctx;

	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	static class TagsViewHolder extends RecyclerView.ViewHolder {

        private final TextView tagName;
        private final TextView tagBody;
        private final LinearLayout downloadFrame;
        private final LinearLayout downloads;
        private final TextView releaseZipDownload;
	    private final TextView releaseTarDownload;
	    private final ImageView downloadDropdownIcon;
	    private final ImageView options;

        private TagsViewHolder(View itemView) {

            super(itemView);

	        tagName = itemView.findViewById(R.id.tagName);
	        tagBody = itemView.findViewById(R.id.tagBodyContent);
	        downloadFrame = itemView.findViewById(R.id.downloadFrame);
	        downloads = itemView.findViewById(R.id.downloads);
	        releaseZipDownload = itemView.findViewById(R.id.releaseZipDownload);
	        releaseTarDownload = itemView.findViewById(R.id.releaseTarDownload);
	        downloadDropdownIcon = itemView.findViewById(R.id.downloadDropdownIcon);
	        options = itemView.findViewById(R.id.tagsOptionsMenu);
        }
    }

    public TagsAdapter(Context ctx, List<GitTag> releasesMain, String repoOwner, String repoName) {
        this.context = ctx;
        this.tags = releasesMain;
        owner = repoOwner;
        repo = repoName;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_tags, parent, false);
        return new TagsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {

        GitTag currentItem = tags.get(position);

	    holder.tagName.setText(currentItem.getName());

        if(!currentItem.getMessage().equals("")) {
	        Markdown.render(context, currentItem.getMessage(), holder.tagBody);
        }
        else {
	        holder.tagBody.setVisibility(View.GONE);
        }

	    holder.downloadFrame.setOnClickListener(v -> {

		    if(holder.downloads.getVisibility() == View.GONE) {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_down);
			    holder.downloads.setVisibility(View.VISIBLE);
		    }
		    else {

			    holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_right);
			    holder.downloads.setVisibility(View.GONE);
		    }
	    });

	    if(!TinyDB.getInstance(ctx).getBoolean("isRepoAdmin")) {
            holder.options.setVisibility(View.GONE);
        }

        holder.options.setOnClickListener(v -> {
	        final Context context = v.getContext();

	        @SuppressLint("InflateParams")
	        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_tag_in_list, null);

	        TextView delete = view.findViewById(R.id.tagMenuDelete);

	        BottomSheetDialog dialog = new BottomSheetDialog(context);
	        dialog.setContentView(view);
	        dialog.show();

	        delete.setOnClickListener(v1 -> {
		        AlertDialogs.tagDeleteDialog(context, currentItem.getName(), owner, repo);
	            dialog.dismiss();
	        });
        });

        holder.releaseZipDownload.setText(
                HtmlCompat.fromHtml("<a href='" + currentItem.getZipballUrl() + "'>" + context.getResources().getString(R.string.zipArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseZipDownload.setMovementMethod(LinkMovementMethod.getInstance());

        holder.releaseTarDownload.setText(
                HtmlCompat.fromHtml("<a href='" + currentItem.getTarballUrl() + "'>" + context.getResources().getString(R.string.tarArchiveDownloadReleasesTab) + "</a> ", HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.releaseTarDownload.setMovementMethod(LinkMovementMethod.getInstance());

	    if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
		    isLoading = true;
		    loadMoreListener.onLoadMore();
	    }
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if(!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
		void onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<GitTag> list) {
		tags = list;
		notifyDataChanged();
	}

}
