package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.PullRequests;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class PullRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<PullRequests> prList;
	private PullRequestsAdapter.OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public PullRequestsAdapter(Context context, List<PullRequests> prListMain) {

		this.context = context;
		this.prList = prListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new PullRequestsAdapter.PullRequestsHolder(inflater.inflate(R.layout.list_pr, parent, false));
		}
		else {
			return new PullRequestsAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((PullRequestsAdapter.PullRequestsHolder) holder).bindData(prList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {

		if(prList.get(position).getTitle() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {

		return prList.size();
	}

	class PullRequestsHolder extends RecyclerView.ViewHolder {

		private PullRequests pullRequest;

		private final ImageView assigneeAvatar;
		private final TextView prTitle;
		private final TextView prCreatedTime;
		private final TextView prCommentsCount;

		PullRequestsHolder(View itemView) {

			super(itemView);
			assigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			prTitle = itemView.findViewById(R.id.prTitle);
			prCommentsCount = itemView.findViewById(R.id.prCommentsCount);
			prCreatedTime = itemView.findViewById(R.id.prCreatedTime);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", pullRequest.getNumber());
				intent.putExtra("prMergeable", pullRequest.isMergeable());
				intent.putExtra("prHeadBranch", pullRequest.getHead().getRef());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", String.valueOf(pullRequest.getNumber()));
				tinyDb.putString("prMergeable", String.valueOf(pullRequest.isMergeable()));
				tinyDb.putString("prHeadBranch", pullRequest.getHead().getRef());

				if(pullRequest.getHead() != null && pullRequest.getHead().getRepo() != null) {
					tinyDb.putString("prIsFork", String.valueOf(pullRequest.getHead().getRepo().isFork()));
					tinyDb.putString("prForkFullName", pullRequest.getHead().getRepo().getFull_name());
				}
				else {
					// pull was done from a deleted fork
					tinyDb.putString("prIsFork", "true");
					tinyDb.putString("prForkFullName", context.getString(R.string.prDeletedFork));
				}

				tinyDb.putString("issueType", "Pull");
				context.startActivity(intent);

			});

			assigneeAvatar.setOnClickListener(v -> {
				Context context = v.getContext();
				String userLoginId = pullRequest.getUser().getLogin();

				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
			});

		}

		@SuppressLint("SetTextI18n")
		void bindData(PullRequests pullRequest) {

			TinyDB tinyDb = TinyDB.getInstance(context);
			String locale = tinyDb.getString("locale");
			String timeFormat = tinyDb.getString("dateFormat");
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			PicassoService.getInstance(context).get()
				.load(pullRequest.getUser().getAvatar_url())
				.placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(imgRadius, 0))
				.resize(120, 120)
				.centerCrop()
				.into(this.assigneeAvatar);

			this.pullRequest = pullRequest;

			String prNumber_ = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + pullRequest.getNumber() + "</font>";

			this.prTitle.setText(HtmlCompat.fromHtml(prNumber_ + " " + EmojiParser.parseToUnicode(pullRequest.getTitle()), HtmlCompat.FROM_HTML_MODE_LEGACY));
			this.prCommentsCount.setText(String.valueOf(pullRequest.getComments()));
			this.prCreatedTime.setText(TimeHelper.formatTime(pullRequest.getCreated_at(), new Locale(locale), timeFormat, context));

			if(timeFormat.equals("pretty")) {
				this.prCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(pullRequest.getCreated_at()), context));
			}
		}
	}

	static class LoadHolder extends RecyclerView.ViewHolder {

		LoadHolder(View itemView) {

			super(itemView);
		}

	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {

		isMoreDataAvailable = moreDataAvailable;

	}

	public void notifyDataChanged() {

		notifyDataSetChanged();
		isLoading = false;
	}

	public interface OnLoadMoreListener {

		void onLoadMore();
	}

	public void setLoadMoreListener(PullRequestsAdapter.OnLoadMoreListener loadMoreListener) {

		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<PullRequests> list) {

		prList = list;
		notifyDataSetChanged();
	}

}
