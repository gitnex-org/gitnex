package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Activity;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;

/**
 * @author M M Arif
 */
public class DashboardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	TinyDB tinyDb;
	private List<Activity> activityList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public DashboardAdapter(List<Activity> dataList, Context ctx) {
		this.context = ctx;
		this.activityList = dataList;
		this.tinyDb = TinyDB.getInstance(ctx);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new DashboardAdapter.DashboardHolder(
				inflater.inflate(R.layout.list_dashboard_activity, parent, false));
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

		((DashboardAdapter.DashboardHolder) holder).bindData(activityList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return activityList.size();
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

	public void updateList(List<Activity> list) {
		activityList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class DashboardHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView typeDetails;
		private final TextView createdTime;
		private final ImageView typeIcon;
		private final TextView dashText;
		private final LinearLayout dashTextFrame;

		private Activity activityObject;

		DashboardHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.user_avatar);
			typeDetails = itemView.findViewById(R.id.type_details);
			typeIcon = itemView.findViewById(R.id.type_icon);
			createdTime = itemView.findViewById(R.id.created_time);
			dashText = itemView.findViewById(R.id.text);
			dashTextFrame = itemView.findViewById(R.id.dash_text_frame);

			/*new Handler()
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
				500);*/
		}

		@SuppressLint("SetTextI18n")
		void bindData(Activity activity) {

			this.activityObject = activity;
			Locale locale = context.getResources().getConfiguration().locale;

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			PicassoService.getInstance(context)
					.get()
					.load(activity.getActUser().getAvatarUrl())
					.placeholder(R.drawable.loader_animated)
					.transform(new RoundedTransformation(imgRadius, 0))
					.resize(120, 120)
					.centerCrop()
					.into(userAvatar);

			String username =
					"<font color='"
							+ ResourcesCompat.getColor(
									context.getResources(), R.color.lightGray, null)
							+ "'>"
							+ activity.getActUser().getLogin()
							+ "</font>";

			String headerString = "";
			String typeString = "";

			if (activity.getOpType().contains("repo")) {

				if (activity.getOpType().equalsIgnoreCase("create_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = "created repository";
					typeIcon.setImageResource(R.drawable.ic_repo);
				} else if (activity.getOpType().equalsIgnoreCase("rename_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = "renamed repository from " + activity.getContent() + " to";
					typeIcon.setImageResource(R.drawable.ic_repo);
				} else if (activity.getOpType().equalsIgnoreCase("star_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = "starred";
					typeIcon.setImageResource(R.drawable.ic_star);
				} else if (activity.getOpType().equalsIgnoreCase("transfer_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = "transferred repository " + activity.getContent() + " to";
					typeIcon.setImageResource(R.drawable.ic_arrow_up);
				} else if (activity.getOpType().equalsIgnoreCase("commit_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					if (activity.getContent().isEmpty()) {
						String branch =
								"<font color='"
										+ ResourcesCompat.getColor(
												context.getResources(), R.color.lightGray, null)
										+ "'>"
										+ activity.getRefName()
												.substring(
														activity.getRefName().lastIndexOf("/") + 1)
												.trim()
										+ "</font>";
						typeString = "created branch " + branch + " in";
					} else {
						String branch =
								"<font color='"
										+ ResourcesCompat.getColor(
												context.getResources(), R.color.lightGray, null)
										+ "'>"
										+ activity.getRefName()
												.substring(
														activity.getRefName().lastIndexOf("/") + 1)
												.trim()
										+ "</font>";
						typeString = "pushed to " + branch + " at";
					}
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().contains("issue")) {

				String id = "";
				String content = "";
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					id = contentParts[0];
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				} else {
					id = contentParts[0];
				}

				if (activity.getOpType().equalsIgnoreCase("create_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "opened issue";
					typeIcon.setImageResource(R.drawable.ic_issue);
				} else if (activity.getOpType().equalsIgnoreCase("comment_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "commented on issue";
					typeIcon.setImageResource(R.drawable.ic_comment);
				} else if (activity.getOpType().equalsIgnoreCase("close_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "closed issue";
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				} else if (activity.getOpType().equalsIgnoreCase("reopen_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "reopened issue";
					typeIcon.setImageResource(R.drawable.ic_reopen);
				}
			} else if (activity.getOpType().contains("pull")) {

				String id = "";
				String content = "";
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					id = contentParts[0];
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				} else {
					id = contentParts[0];
				}

				if (activity.getOpType().equalsIgnoreCase("create_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "created pull request";
					typeIcon.setImageResource(R.drawable.ic_pull_request);
				} else if (activity.getOpType().equalsIgnoreCase("close_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "closed pull request";
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				} else if (activity.getOpType().equalsIgnoreCase("reopen_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "reopened pull request";
					typeIcon.setImageResource(R.drawable.ic_reopen);
				} else if (activity.getOpType().equalsIgnoreCase("merge_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "merged pull request";
					typeIcon.setImageResource(R.drawable.ic_pull_request);
				} else if (activity.getOpType().equalsIgnoreCase("approve_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "approved";
					typeIcon.setImageResource(R.drawable.ic_done);
				} else if (activity.getOpType().equalsIgnoreCase("reject_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "suggested changes for";
					typeIcon.setImageResource(R.drawable.ic_diff);
				} else if (activity.getOpType().equalsIgnoreCase("comment_pull")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "commented on pull request";
					typeIcon.setImageResource(R.drawable.ic_comment);
				} else if (activity.getOpType().equalsIgnoreCase("auto_merge_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = "automatically merged pull request";
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				}
			} else if (activity.getOpType().contains("branch")) {

				String id = "";
				String content = "";
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					id = contentParts[0];
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				} else {
					id = contentParts[0];
				}

				if (activity.getOpType().equalsIgnoreCase("delete_branch")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = "deleted branch " + branch + " at";
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().contains("tag")) {

				if (activity.getOpType().equalsIgnoreCase("push_tag")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = "pushed tag " + branch + " to";
					typeIcon.setImageResource(R.drawable.ic_commit);
				} else if (activity.getOpType().equalsIgnoreCase("delete_tag")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = "deleted tag " + branch + " from";
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().contains("release")) {

				if (activity.getOpType().equalsIgnoreCase("publish_release")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = "released " + branch + " at";
					typeIcon.setImageResource(R.drawable.ic_tag);
				}
			} else if (activity.getOpType().contains("mirror")) {

				if (activity.getOpType().equalsIgnoreCase("mirror_sync_push")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString = "synced commits to " + headerString + " at";
					typeIcon.setImageResource(R.drawable.ic_tag);
				} else if (activity.getOpType().equalsIgnoreCase("mirror_sync_create")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString = "synced new reference " + headerString + " to";
					typeIcon.setImageResource(R.drawable.ic_tag);
				} else if (activity.getOpType().equalsIgnoreCase("mirror_sync_delete")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString = "synced and deleted reference " + headerString + " at";
					typeIcon.setImageResource(R.drawable.ic_tag);
				}
			} else {
				dashTextFrame.setVisibility(View.GONE);
				dashText.setVisibility(View.GONE);
			}

			typeDetails.setText(
					HtmlCompat.fromHtml(
							username + " " + typeString + " " + headerString,
							HtmlCompat.FROM_HTML_MODE_LEGACY));

			/*String issueNumber_ =
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
					.getIntent(context, IssueDetailActivity.class);*/

			/*itemView.setOnClickListener(layoutView -> context.startActivity(intentIssueDetail));
			frameLabels.setOnClickListener(v -> context.startActivity(intentIssueDetail));
			frameLabelsDots.setOnClickListener(v -> context.startActivity(intentIssueDetail));*/

			/*LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, 0, 15, 0);

			Typeface typeface = AppUtil.getTypeface(context);*/

			this.createdTime.setText(TimeHelper.formatTime(activity.getCreated(), locale));
			this.createdTime.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(activity.getCreated()),
							context));

			/*if (issue.getLabels() != null) {

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
			}*/

			/*if (issue.getComments() > 15) {
				commentIcon.setImageDrawable(
					ContextCompat.getDrawable(context, R.drawable.ic_flame));
				commentIcon.setColorFilter(
					context.getResources().getColor(R.color.releasePre, null));
			}*/

			/*this.issueCreatedTime.setText(TimeHelper.formatTime(issue.getCreatedAt(), locale));
			this.issueCreatedTime.setOnClickListener(
				new ClickListener(
					TimeHelper.customDateFormatForToastDateFormat(issue.getCreatedAt()),
					context));*/
		}
	}
}
