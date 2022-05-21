package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import java.util.List;

/**
 * @author M M Arif
 */

public class WikiListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context ctx;
	private List<WikiPageMetaData> wikiList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public WikiListAdapter(List<WikiPageMetaData> wikiListMain, Context ctx) {
		this.ctx = ctx;
		this.wikiList = wikiListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		return new WikiListAdapter.WikisHolder(inflater.inflate(R.layout.list_wiki, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((WikiListAdapter.WikisHolder) holder).bindData(wikiList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return wikiList.size();
	}

	class WikisHolder extends RecyclerView.ViewHolder {

		private WikiPageMetaData wikiPageMeta;

		private final ImageView avatar;
		private final TextView pageName;
		private final TextView wikiLastUpdatedBy;

		WikisHolder(View itemView) {

			super(itemView);
			pageName = itemView.findViewById(R.id.page_name);
			avatar = itemView.findViewById(R.id.image_avatar);
			wikiLastUpdatedBy = itemView.findViewById(R.id.wiki_last_updated_by);

			itemView.setOnClickListener(v -> {
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(WikiPageMetaData wikiPageMetaData) {

			this.wikiPageMeta = wikiPageMetaData;

			pageName.setText(wikiPageMetaData.getTitle());
			wikiLastUpdatedBy.setText(
				HtmlCompat.fromHtml(ctx.getResources().getString(R.string.wikiAuthor, wikiPageMetaData.getLastCommit().getAuthor().getName(),
					TimeHelper.formatTime(TimeHelper.parseIso8601(wikiPageMetaData.getLastCommit().getAuthor().getDate()), ctx.getResources().getConfiguration().locale, "pretty",
						ctx)), HtmlCompat.FROM_HTML_MODE_COMPACT));
			this.wikiLastUpdatedBy.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(TimeHelper.parseIso8601(wikiPageMetaData.getLastCommit().getAuthor().getDate())), ctx));

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(wikiPageMetaData.getTitle());
			String firstCharacter = String.valueOf(wikiPageMetaData.getTitle().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);
			avatar.setImageDrawable(drawable);
		}
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

	public void updateList(List<WikiPageMetaData> list) {
		wikiList = list;
		notifyDataChanged();
	}
}
