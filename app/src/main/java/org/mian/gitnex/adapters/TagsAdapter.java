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
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author qwerty287
 */

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.TagsViewHolder> {

    private List<Tag> tags;
    private final Context context;
    private static String repo;
    private static String owner;

	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	class TagsViewHolder extends RecyclerView.ViewHolder {

		private Tag tagsHolder;
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

	        options.setOnClickListener(v -> {
		        final Context context = v.getContext();

		        @SuppressLint("InflateParams")
		        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_tag_in_list, null);

		        TextView delete = view.findViewById(R.id.tagMenuDelete);

		        BottomSheetDialog dialog = new BottomSheetDialog(context);
		        dialog.setContentView(view);
		        dialog.show();

		        delete.setOnClickListener(v1 -> {
			        tagDeleteDialog(context, tagsHolder.getName(), owner, repo, getBindingAdapterPosition());
			        dialog.dismiss();
		        });
	        });
        }
    }

    public TagsAdapter(Context ctx, List<Tag> releasesMain, String repoOwner, String repoName) {
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

	    Tag currentItem = tags.get(position);
	    holder.tagsHolder = currentItem;

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

	    if(!((RepoDetailActivity) context).repository.getPermissions().isPush()) {
            holder.options.setVisibility(View.GONE);
        }

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

	@SuppressLint("NotifyDataSetChanged")
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

	public void updateList(List<Tag> list) {
		tags = list;
		notifyDataChanged();
	}

	private void updateAdapter(int position) {
		tags.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, tags.size());
	}

	public void tagDeleteDialog(final Context context, final String tagName, final String owner, final String repo, int position) {

		new AlertDialog.Builder(context)
			.setTitle(String.format(context.getString(R.string.deleteTagTitle), tagName))
			.setMessage(R.string.deleteTagConfirmation)
			.setIcon(R.drawable.ic_delete)
			.setPositiveButton(R.string.menuDeleteText, (dialog, whichButton) -> RetrofitClient
				.getApiInterface(context).repoDeleteTag(owner, repo, tagName).enqueue(new Callback<>() {

					@Override
					public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

						if(response.isSuccessful()) {
							updateAdapter(position);
							Toasty.success(context, context.getString(R.string.tagDeleted));
						}
						else if(response.code() == 403) {
							Toasty.error(context, context.getString(R.string.authorizeError));
						}
						else if(response.code() == 409) {
							Toasty.error(context, context.getString(R.string.tagDeleteError));
						}
						else {
							Toasty.error(context, context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(context, context.getString(R.string.genericError));
					}
				}))
			.setNeutralButton(R.string.cancelButton, null).show();
	}
}
