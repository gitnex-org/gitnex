package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.PullRequests;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import java.util.Locale;

/**
 * @author M M Arif
 */

public class PullRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<PullRequests> prList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public PullRequestsAdapter(Context context, List<PullRequests> prListMain) {
		this.context = context;
		this.prList = prListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new PullRequestsAdapter.PullRequestsHolder(inflater.inflate(R.layout.list_pr, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((PullRequestsAdapter.PullRequestsHolder) holder).bindData(prList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return prList.size();
	}

	class PullRequestsHolder extends RecyclerView.ViewHolder {

		private PullRequests pullRequestObject;

		private final ImageView assigneeAvatar;
		private final TextView prTitle;
		private final TextView prCreatedTime;
		private final TextView prCommentsCount;
		private final HorizontalScrollView labelsScrollViewWithText;
		private final LinearLayout frameLabels;
		private final HorizontalScrollView labelsScrollViewDots;
		private final LinearLayout frameLabelsDots;

		PullRequestsHolder(View itemView) {

			super(itemView);
			assigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			prTitle = itemView.findViewById(R.id.prTitle);
			prCommentsCount = itemView.findViewById(R.id.prCommentsCount);
			prCreatedTime = itemView.findViewById(R.id.prCreatedTime);
			labelsScrollViewWithText = itemView.findViewById(R.id.labelsScrollViewWithText);
			frameLabels = itemView.findViewById(R.id.frameLabels);
			labelsScrollViewDots = itemView.findViewById(R.id.labelsScrollViewDots);
			frameLabelsDots = itemView.findViewById(R.id.frameLabelsDots);

			itemView.setOnClickListener(v -> {
				Intent intent = new IssueContext(
					pullRequestObject,
					new RepositoryContext(pullRequestObject.getBase().getRepo().getFull_name().split("/")[0], pullRequestObject.getBase().getRepo().getName(), context)
				)
					.getIntent(context, IssueDetailActivity.class);

				context.startActivity(intent);

			});

			assigneeAvatar.setOnClickListener(v -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", pullRequestObject.getUser().getLogin());
				context.startActivity(intent);
			});

			assigneeAvatar.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, pullRequestObject.getUser().getLogin(), context.getString(R.string.copyLoginIdToClipBoard, pullRequestObject.getUser().getLogin()));
				return true;
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(PullRequests pullRequest) {

			TinyDB tinyDb = TinyDB.getInstance(context);
			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			PicassoService.getInstance(context).get()
				.load(pullRequest.getUser().getAvatar_url())
				.placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(imgRadius, 0))
				.resize(120, 120)
				.centerCrop()
				.into(this.assigneeAvatar);

			this.pullRequestObject = pullRequest;

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 15, 0);

			if(pullRequest.getLabels() != null) {

				if(!tinyDb.getBoolean("showLabelsInList", false)) { // default

					labelsScrollViewWithText.setVisibility(View.GONE);
					labelsScrollViewDots.setVisibility(View.VISIBLE);
					frameLabelsDots.removeAllViews();

					for(int i = 0; i < pullRequest.getLabels().size(); i++) {

						String labelColor = pullRequest.getLabels().get(i).getColor();
						int color = Color.parseColor("#" + labelColor);

						ImageView labelsView = new ImageView(context);
						frameLabelsDots.setOrientation(LinearLayout.HORIZONTAL);
						frameLabelsDots.setGravity(Gravity.START | Gravity.TOP);
						labelsView.setLayoutParams(params);

						TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).width(54).height(54).endConfig().buildRound("", color);

						labelsView.setImageDrawable(drawable);
						frameLabelsDots.addView(labelsView);
					}
				}
				else {

					labelsScrollViewDots.setVisibility(View.GONE);
					labelsScrollViewWithText.setVisibility(View.VISIBLE);
					frameLabels.removeAllViews();

					for(int i = 0; i < pullRequest.getLabels().size(); i++) {

						String labelColor = pullRequest.getLabels().get(i).getColor();
						String labelName = pullRequest.getLabels().get(i).getName();
						int color = Color.parseColor("#" + labelColor);

						ImageView labelsView = new ImageView(context);
						frameLabels.setOrientation(LinearLayout.HORIZONTAL);
						frameLabels.setGravity(Gravity.START | Gravity.TOP);
						labelsView.setLayoutParams(params);

						int height = AppUtil.getPixelsFromDensity(context, 20);
						int textSize = AppUtil.getPixelsFromScaledDensity(context, 12);

						TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).textColor(new ColorInverter().getContrastColor(color)).fontSize(textSize).width(LabelWidthCalculator
							.calculateLabelWidth(labelName, Typeface.DEFAULT, textSize, AppUtil.getPixelsFromDensity(context, 8))).height(height).endConfig().buildRoundRect(labelName, color, AppUtil.getPixelsFromDensity(context, 18));

						labelsView.setImageDrawable(drawable);
						frameLabels.addView(labelsView);
					}
				}
			}
			else {
				labelsScrollViewDots.setVisibility(View.GONE);
				labelsScrollViewWithText.setVisibility(View.GONE);
			}

			String prNumber_ = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + pullRequest.getNumber() + "</font>";

			this.prTitle.setText(HtmlCompat.fromHtml(prNumber_ + " " + EmojiParser.parseToUnicode(pullRequest.getTitle()), HtmlCompat.FROM_HTML_MODE_LEGACY));
			this.prCommentsCount.setText(String.valueOf(pullRequest.getComments()));
			this.prCreatedTime.setText(TimeHelper.formatTime(pullRequest.getCreated_at(), locale, timeFormat, context));

			if(timeFormat.equals("pretty")) {
				this.prCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(pullRequest.getCreated_at()), context));
			}
		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public void setLoadMoreListener(Runnable loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<PullRequests> list) {
		prList = list;
		notifyDataChanged();
	}
}
