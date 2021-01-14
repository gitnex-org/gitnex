package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.PullRequests;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class PullRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context context;
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

		private TextView prNumber;
		private TextView prMergeable;
		private TextView prHeadBranch;
		private TextView prIsFork;
		private TextView prForkFullName;
		private ImageView assigneeAvatar;
		private TextView prTitle;
		private TextView prCreatedTime;
		private TextView prCommentsCount;

		PullRequestsHolder(View itemView) {

			super(itemView);

			prNumber = itemView.findViewById(R.id.prNumber);
			prMergeable = itemView.findViewById(R.id.prMergeable);
			prHeadBranch = itemView.findViewById(R.id.prHeadBranch);
			prIsFork = itemView.findViewById(R.id.prIsFork);
			prForkFullName = itemView.findViewById(R.id.prForkFullName);
			assigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			prTitle = itemView.findViewById(R.id.prTitle);
			prCommentsCount = itemView.findViewById(R.id.prCommentsCount);
			LinearLayout frameCommentsCount = itemView.findViewById(R.id.frameCommentsCount);
			prCreatedTime = itemView.findViewById(R.id.prCreatedTime);

			prTitle.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", prNumber.getText());
				intent.putExtra("prMergeable", prMergeable.getText());
				intent.putExtra("prHeadBranch", prHeadBranch.getText());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", prNumber.getText().toString());
				tinyDb.putString("prMergeable", prMergeable.getText().toString());
				tinyDb.putString("prHeadBranch", prHeadBranch.getText().toString());
				tinyDb.putString("prIsFork", prIsFork.getText().toString());
				tinyDb.putString("prForkFullName", prForkFullName.getText().toString());
				tinyDb.putString("issueType", "Pull");
				context.startActivity(intent);

			});
			frameCommentsCount.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", prNumber.getText());
				intent.putExtra("prMergeable", prMergeable.getText());
				intent.putExtra("prHeadBranch", prHeadBranch.getText());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("issueNumber", prNumber.getText().toString());
				tinyDb.putString("prMergeable", prMergeable.getText().toString());
				tinyDb.putString("prHeadBranch", prHeadBranch.getText().toString());
				tinyDb.putString("prIsFork", prIsFork.getText().toString());
				tinyDb.putString("prForkFullName", prForkFullName.getText().toString());
				tinyDb.putString("issueType", "Pull");
				context.startActivity(intent);

			});

		}

		@SuppressLint("SetTextI18n")
		void bindData(PullRequests prModel) {

			final TinyDB tinyDb = TinyDB.getInstance(context);
			final String locale = tinyDb.getString("locale");
			final String timeFormat = tinyDb.getString("dateFormat");

			if(!prModel.getUser().getFull_name().equals("")) {
				assigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.prCreator) + prModel.getUser().getFull_name(), context));
			}
			else {
				assigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.prCreator) + prModel.getUser().getLogin(), context));
			}

			if(prModel.getUser().getAvatar_url() != null) {
				PicassoService.getInstance(context).get().load(prModel.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(assigneeAvatar);
			}
			else {
				PicassoService.getInstance(context).get().load(prModel.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(assigneeAvatar);
			}

			String prNumber_ = "<font color='" + context.getResources().getColor(R.color.lightGray) + "'>" + context.getResources().getString(R.string.hash) + prModel.getNumber() + "</font>";
			prTitle.setText(Html.fromHtml(prNumber_ + " " + prModel.getTitle()));

			prNumber.setText(String.valueOf(prModel.getNumber()));
			prMergeable.setText(String.valueOf(prModel.isMergeable()));
			if(prModel.getHead() != null) {
				prHeadBranch.setText(prModel.getHead().getRef());
				if(prModel.getHead().getRepo() != null) {
					prIsFork.setText(String.valueOf(prModel.getHead().getRepo().isFork()));
					prForkFullName.setText(prModel.getHead().getRepo().getFull_name());
				}
				else {
					// pull was done from a deleted fork
					prIsFork.setText("true");
					prForkFullName.setText(context.getString(R.string.prDeletedFrok));
				}
			}
			prCommentsCount.setText(String.valueOf(prModel.getComments()));

			prCreatedTime.setText(TimeHelper.formatTime(prModel.getCreated_at(), new Locale(locale), timeFormat, context));

			if(timeFormat.equals("pretty")) {
				prCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(prModel.getCreated_at()), context));
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
