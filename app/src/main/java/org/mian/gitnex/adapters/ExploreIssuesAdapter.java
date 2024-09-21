package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
 */
public class ExploreIssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Issue> searchedList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public ExploreIssuesAdapter(List<Issue> dataList, Context ctx) {
		this.context = ctx;
		this.searchedList = dataList;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ExploreIssuesAdapter.IssuesHolder(
				inflater.inflate(R.layout.list_issues, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((ExploreIssuesAdapter.IssuesHolder) holder).bindData(searchedList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return searchedList.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Issue> list) {
		searchedList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class IssuesHolder extends RecyclerView.ViewHolder {

		private final ImageView issueAssigneeAvatar;
		private final TextView issueTitle;
		private final TextView issueCreatedTime;
		private final TextView issueCommentsCount;
		private final HorizontalScrollView labelsScrollViewWithText;
		private final LinearLayout frameLabels;
		private final HorizontalScrollView labelsScrollViewDots;
		private final LinearLayout frameLabelsDots;
		private final ImageView commentIcon;
		private Issue issue;

		IssuesHolder(View itemView) {

			super(itemView);
			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);
			labelsScrollViewWithText = itemView.findViewById(R.id.labelsScrollViewWithText);
			frameLabels = itemView.findViewById(R.id.frameLabels);
			labelsScrollViewDots = itemView.findViewById(R.id.labelsScrollViewDots);
			frameLabelsDots = itemView.findViewById(R.id.frameLabelsDots);
			commentIcon = itemView.findViewById(R.id.comment_icon);

			new Handler()
					.postDelayed(
							() -> {
								String[] parts = issue.getRepository().getFullName().split("/");
								final String repoOwner = parts[0];
								final String repoName = parts[1];

								RepositoryContext repo =
										new RepositoryContext(repoOwner, repoName, context);

								Intent intentIssueDetail =
										new IssueContext(issue, repo)
												.getIntent(context, IssueDetailActivity.class);
								intentIssueDetail.putExtra("openedFromLink", "true");

								itemView.setOnClickListener(
										v -> {
											repo.saveToDB(context);
											context.startActivity(intentIssueDetail);
										});
								frameLabels.setOnClickListener(
										v -> {
											repo.saveToDB(context);
											context.startActivity(intentIssueDetail);
										});
								frameLabelsDots.setOnClickListener(
										v -> {
											repo.saveToDB(context);
											context.startActivity(intentIssueDetail);
										});

								if (!AppUtil.checkGhostUsers(issue.getUser().getLogin())) {

									issueAssigneeAvatar.setOnClickListener(
											v -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra(
														"username", issue.getUser().getLogin());
												context.startActivity(intent);
											});

									issueAssigneeAvatar.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														issue.getUser().getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																issue.getUser().getLogin()));
												return true;
											});
								}
							},
							200);
		}

		void bindData(Issue issue) {

			this.issue = issue;

			Locale locale = context.getResources().getConfiguration().locale;

			Glide.with(context)
					.load(issue.getUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(issueAssigneeAvatar);

			String issueNumber_ =
					"<font color='"
							+ ResourcesCompat.getColor(
									context.getResources(), R.color.lightGray, null)
							+ "'>"
							+ issue.getRepository().getFullName()
							+ context.getResources().getString(R.string.hash)
							+ issue.getNumber()
							+ "</font>";

			issueTitle.setText(
					HtmlCompat.fromHtml(
							issueNumber_ + " " + issue.getTitle(),
							HtmlCompat.FROM_HTML_MODE_LEGACY));
			issueCommentsCount.setText(String.valueOf(issue.getComments()));

			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 15, 0);

			Typeface typeface = AppUtil.getTypeface(context);

			if (issue.getLabels() != null) {

				if (!Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_LABELS_IN_LIST_KEY))) {

					labelsScrollViewWithText.setVisibility(View.GONE);
					labelsScrollViewDots.setVisibility(View.VISIBLE);
					frameLabelsDots.removeAllViews();

					for (int i = 0; i < issue.getLabels().size(); i++) {

						String labelColor = issue.getLabels().get(i).getColor();
						int color = Color.parseColor("#" + labelColor);

						ImageView labelsView = new ImageView(context);
						frameLabelsDots.setOrientation(LinearLayout.HORIZONTAL);
						frameLabelsDots.setGravity(Gravity.START | Gravity.TOP);
						labelsView.setLayoutParams(params);

						TextDrawable drawable =
								TextDrawable.builder()
										.beginConfig()
										.useFont(typeface)
										.width(54)
										.height(54)
										.endConfig()
										.buildRound("", color);

						labelsView.setImageDrawable(drawable);
						frameLabelsDots.addView(labelsView);
					}
				} else {

					labelsScrollViewDots.setVisibility(View.GONE);
					labelsScrollViewWithText.setVisibility(View.VISIBLE);
					frameLabels.removeAllViews();

					for (int i = 0; i < issue.getLabels().size(); i++) {

						String labelColor = issue.getLabels().get(i).getColor();
						String labelName = issue.getLabels().get(i).getName();
						int color = Color.parseColor("#" + labelColor);

						ImageView labelsView = new ImageView(context);
						frameLabels.setOrientation(LinearLayout.HORIZONTAL);
						frameLabels.setGravity(Gravity.START | Gravity.TOP);
						labelsView.setLayoutParams(params);

						int height = AppUtil.getPixelsFromDensity(context, 20);
						int textSize = AppUtil.getPixelsFromScaledDensity(context, 12);

						TextDrawable drawable =
								TextDrawable.builder()
										.beginConfig()
										.useFont(typeface)
										.textColor(new ColorInverter().getContrastColor(color))
										.fontSize(textSize)
										.width(
												LabelWidthCalculator.calculateLabelWidth(
														labelName,
														typeface,
														textSize,
														AppUtil.getPixelsFromDensity(context, 8)))
										.height(height)
										.endConfig()
										.buildRoundRect(
												labelName,
												color,
												AppUtil.getPixelsFromDensity(context, 18));

						labelsView.setImageDrawable(drawable);
						frameLabels.addView(labelsView);
					}
				}
			} else {
				labelsScrollViewDots.setVisibility(View.GONE);
				labelsScrollViewWithText.setVisibility(View.GONE);
			}

			if (issue.getComments() > 15) {
				commentIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_flame));
				commentIcon.setColorFilter(
						context.getResources().getColor(R.color.releasePre, null));
			}

			issueCreatedTime.setText(TimeHelper.formatTime(issue.getCreatedAt(), locale));
			issueCreatedTime.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(issue.getCreatedAt()),
							context));
		}
	}
}
