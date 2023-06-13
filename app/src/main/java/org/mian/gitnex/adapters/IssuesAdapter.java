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
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.IssueContext;

/**
 * @author M M Arif
 */
public class IssuesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	TinyDB tinyDb;
	private List<Issue> issuesList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public IssuesAdapter(Context ctx, List<Issue> issuesListMain) {

		this.context = ctx;
		this.issuesList = issuesListMain;
		tinyDb = TinyDB.getInstance(context);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new IssuesHolder(inflater.inflate(R.layout.list_issues, parent, false));
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

		((IssuesHolder) holder).bindData(issuesList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {

		return issuesList.size();
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

	public void updateList(List<Issue> list) {
		issuesList = list;
		notifyDataChanged();
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
		private Issue issueObject;

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
								if (!AppUtil.checkGhostUsers(issueObject.getUser().getLogin())) {

									issueAssigneeAvatar.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														issueObject.getUser().getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																issueObject.getUser().getLogin()));
												return true;
											});

									issueAssigneeAvatar.setOnClickListener(
											v -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra(
														"username",
														issueObject.getUser().getLogin());
												context.startActivity(intent);
											});
								}
							},
							500);
		}

		@SuppressLint("SetTextI18n")
		void bindData(Issue issue) {

			Locale locale = context.getResources().getConfiguration().locale;

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			PicassoService.getInstance(context)
					.get()
					.load(issue.getUser().getAvatarUrl())
					.placeholder(R.drawable.loader_animated)
					.transform(new RoundedTransformation(imgRadius, 0))
					.resize(120, 120)
					.centerCrop()
					.into(issueAssigneeAvatar);

			String issueNumber_ =
					"<font color='"
							+ ResourcesCompat.getColor(
									context.getResources(), R.color.lightGray, null)
							+ "'>"
							+ context.getResources().getString(R.string.hash)
							+ issue.getNumber()
							+ "</font>";
			issueTitle.setText(
					HtmlCompat.fromHtml(
							issueNumber_ + " " + EmojiParser.parseToUnicode(issue.getTitle()),
							HtmlCompat.FROM_HTML_MODE_LEGACY));

			this.issueObject = issue;
			this.issueCommentsCount.setText(String.valueOf(issue.getComments()));

			Intent intentIssueDetail =
					new IssueContext(issueObject, ((RepoDetailActivity) context).repository)
							.getIntent(context, IssueDetailActivity.class);

			itemView.setOnClickListener(layoutView -> context.startActivity(intentIssueDetail));
			frameLabels.setOnClickListener(v -> context.startActivity(intentIssueDetail));
			frameLabelsDots.setOnClickListener(v -> context.startActivity(intentIssueDetail));

			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 15, 0);

			Typeface typeface = AppUtil.getTypeface(context);

			if (issue.getLabels() != null) {

				if (!tinyDb.getBoolean("showLabelsInList", false)) { // default

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
			}

			if (issue.getComments() > 15) {
				commentIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_flame));
				commentIcon.setColorFilter(
						context.getResources().getColor(R.color.releasePre, null));
			}

			this.issueCreatedTime.setText(TimeHelper.formatTime(issue.getCreatedAt(), locale));
			this.issueCreatedTime.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(issue.getCreatedAt()),
							context));
		}
	}
}
