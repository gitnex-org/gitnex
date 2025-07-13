package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.contexts.IssueContext;

/**
 * @author M M Arif
 */
public class PullRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<PullRequest> prList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public PullRequestsAdapter(Context context, List<PullRequest> prListMain) {
		this.context = context;
		this.prList = prListMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new PullRequestsAdapter.PullRequestsHolder(
				inflater.inflate(R.layout.list_pr, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
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

	public void updateList(List<PullRequest> list) {
		prList = list;
		notifyDataChanged();
	}

	class PullRequestsHolder extends RecyclerView.ViewHolder {

		private final ImageView assigneeAvatar;
		private final TextView prTitle;
		private final ImageView issuePrState;
		private final TextView prCreatedTime;
		private final TextView prCommentsCount;
		private final HorizontalScrollView labelsScrollViewWithText;
		private final LinearLayout frameLabels;
		private final HorizontalScrollView labelsScrollViewDots;
		private final LinearLayout frameLabelsDots;
		private final ImageView commentIcon;
		private PullRequest pullRequestObject;

		PullRequestsHolder(View itemView) {

			super(itemView);
			assigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			prTitle = itemView.findViewById(R.id.prTitle);
			issuePrState = itemView.findViewById(R.id.issuePrState);
			prCommentsCount = itemView.findViewById(R.id.prCommentsCount);
			prCreatedTime = itemView.findViewById(R.id.prCreatedTime);
			labelsScrollViewWithText = itemView.findViewById(R.id.labelsScrollViewWithText);
			frameLabels = itemView.findViewById(R.id.frameLabels);
			labelsScrollViewDots = itemView.findViewById(R.id.labelsScrollViewDots);
			frameLabelsDots = itemView.findViewById(R.id.frameLabelsDots);
			commentIcon = itemView.findViewById(R.id.comment_icon);

			View.OnClickListener openPr =
					v -> {
						Intent intentPrDetail =
								new IssueContext(
												pullRequestObject,
												((RepoDetailActivity) context).repository)
										.getIntent(context, IssueDetailActivity.class);
						context.startActivity(intentPrDetail);
					};

			itemView.setOnClickListener(openPr);
			frameLabels.setOnClickListener(openPr);
			frameLabelsDots.setOnClickListener(openPr);

			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(
										pullRequestObject.getUser().getLogin())) {

									assigneeAvatar.setOnClickListener(
											v -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra(
														"username",
														pullRequestObject.getUser().getLogin());
												context.startActivity(intent);
											});

									assigneeAvatar.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														pullRequestObject.getUser().getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																pullRequestObject
																		.getUser()
																		.getLogin()));
												return true;
											});
								}
							},
							500);
		}

		void bindData(PullRequest pullRequest) {

			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			Glide.with(context)
					.load(pullRequest.getUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(this.assigneeAvatar);

			this.pullRequestObject = pullRequest;

			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 15, 0);

			Typeface typeface = AppUtil.getTypeface(context);

			if (pullRequest.getLabels() != null) {

				if (!Boolean.parseBoolean(
						AppDatabaseSettings.getSettingsValue(
								context, AppDatabaseSettings.APP_LABELS_IN_LIST_KEY))) {

					labelsScrollViewWithText.setVisibility(View.GONE);
					labelsScrollViewDots.setVisibility(View.VISIBLE);
					frameLabelsDots.removeAllViews();

					for (int i = 0; i < pullRequest.getLabels().size(); i++) {

						String labelColor = pullRequest.getLabels().get(i).getColor();
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

					for (int i = 0; i < pullRequest.getLabels().size(); i++) {

						String labelColor = pullRequest.getLabels().get(i).getColor();
						String labelName = pullRequest.getLabels().get(i).getName();
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
												AppUtil.getPixelsFromDensity(context, 6));

						labelsView.setImageDrawable(drawable);
						frameLabels.addView(labelsView);
					}
				}
			}

			String prNumber_ =
					"<font color='"
							+ ResourcesCompat.getColor(
									context.getResources(), R.color.lightGray, null)
							+ "'>"
							+ context.getResources().getString(R.string.hash)
							+ pullRequest.getNumber()
							+ "</font>";

			if (pullRequest.getTitle().contains("[WIP]")
					|| pullRequest.getTitle().contains("[wip]")) {
				this.issuePrState.setVisibility(View.VISIBLE);
				this.issuePrState.setImageResource(R.drawable.ic_draft);
				ImageViewCompat.setImageTintList(
						this.issuePrState,
						ColorStateList.valueOf(
								context.getResources().getColor(R.color.colorWhite, null)));
				this.issuePrState.setBackgroundResource(R.drawable.shape_draft_release);
				this.issuePrState.setPadding(
						(int) context.getResources().getDimension(R.dimen.dimen4dp),
						(int) context.getResources().getDimension(R.dimen.dimen0dp),
						(int) context.getResources().getDimension(R.dimen.dimen4dp),
						(int) context.getResources().getDimension(R.dimen.dimen0dp));
				this.prTitle.setPadding(
						(int) context.getResources().getDimension(R.dimen.dimen12dp),
						(int) context.getResources().getDimension(R.dimen.dimen0dp),
						(int) context.getResources().getDimension(R.dimen.dimen0dp),
						(int) context.getResources().getDimension(R.dimen.dimen0dp));
			} else {
				this.issuePrState.setVisibility(View.GONE);
			}

			Markdown.render(
					context,
					HtmlCompat.fromHtml(
									prNumber_
											+ " "
											+ EmojiParser.parseToUnicode(pullRequest.getTitle()),
									HtmlCompat.FROM_HTML_MODE_LEGACY)
							.toString(),
					this.prTitle);

			this.prCommentsCount.setText(String.valueOf(pullRequest.getComments()));
			this.prCreatedTime.setText(TimeHelper.formatTime(pullRequest.getCreatedAt(), locale));

			if (pullRequest.getComments() > 15) {
				commentIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_flame));
				commentIcon.setColorFilter(
						context.getResources().getColor(R.color.releasePre, null));
			}

			this.prCreatedTime.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(
									pullRequest.getCreatedAt()),
							context));
		}
	}
}
