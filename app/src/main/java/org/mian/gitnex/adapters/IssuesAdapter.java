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
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class IssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<Issues> issuesList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public IssuesAdapter(Context ctx, List<Issues> issuesListMain) {

		this.context = ctx;
		this.issuesList = issuesListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new IssuesHolder(inflater.inflate(R.layout.list_issues, parent, false));
		}
		else {
			return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {

			((IssuesHolder) holder).bindData(issuesList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {

		if(issuesList.get(position).getTitle() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {

		return issuesList.size();
	}

	class IssuesHolder extends RecyclerView.ViewHolder {

		private Issues issue;

		private final ImageView issueAssigneeAvatar;
		private final TextView issueTitle;
		private final TextView issueCreatedTime;
		private final TextView issueCommentsCount;

		IssuesHolder(View itemView) {

			super(itemView);
			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);

			itemView.setOnClickListener(layoutView -> {
				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", issue.getNumber());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", String.valueOf(issue.getNumber()));
				tinyDb.putString("issueType", "Issue");
				context.startActivity(intent);
			});

			issueAssigneeAvatar.setOnClickListener(v -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", issue.getUser().getLogin());
				context.startActivity(intent);
			});

			issueAssigneeAvatar.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, issue.getUser().getLogin(), context.getString(R.string.copyLoginIdToClipBoard, issue.getUser().getLogin()));
				return true;
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(Issues issue) {

			TinyDB tinyDb = TinyDB.getInstance(context);
			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat");

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			PicassoService.getInstance(context).get()
				.load(issue.getUser().getAvatar_url())
				.placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(imgRadius, 0))
				.resize(120, 120)
				.centerCrop()
				.into(issueAssigneeAvatar);

			String issueNumber_ = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + issue.getNumber() + "</font>";
			issueTitle.setText(HtmlCompat.fromHtml(issueNumber_ + " " + EmojiParser.parseToUnicode(issue.getTitle()), HtmlCompat.FROM_HTML_MODE_LEGACY));

			this.issue = issue;
			this.issueCommentsCount.setText(String.valueOf(issue.getComments()));

			switch(timeFormat) {
				case "pretty": {
					PrettyTime prettyTime = new PrettyTime(locale);
					String createdTime = prettyTime.format(issue.getCreated_at());
					this.issueCreatedTime.setText(createdTime);
					this.issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(issue.getCreated_at()), context));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
					String createdTime = formatter.format(issue.getCreated_at());
					this.issueCreatedTime.setText(createdTime);
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
					String createdTime = formatter.format(issue.getCreated_at());
					this.issueCreatedTime.setText(createdTime);
					break;
				}
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

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {

		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Issues> list) {

		issuesList = list;
		notifyDataSetChanged();
	}

}
