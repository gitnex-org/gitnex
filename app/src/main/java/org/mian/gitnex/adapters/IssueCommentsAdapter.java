package org.mian.gitnex.adapters;

import static org.mian.gitnex.helpers.AppUtil.isNightModeThemeDynamic;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.TimelineComment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomImageViewDialogBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.views.ReactionList;
import org.mian.gitnex.views.ReactionSpinner;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
 */
public class IssueCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final TinyDB tinyDB;
	private final Bundle bundle;
	private final Runnable onInteractedListener;
	private final Locale locale;
	private final IssueContext issue;
	private List<TimelineComment> issuesComments;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private Intent intent;

	public IssueCommentsAdapter(
			Context ctx,
			Bundle bundle,
			List<TimelineComment> issuesCommentsMain,
			Runnable onInteractedListener,
			IssueContext issue) {
		this.context = ctx;
		this.bundle = bundle;
		this.issuesComments = issuesCommentsMain;
		this.onInteractedListener = onInteractedListener;
		this.tinyDB = TinyDB.getInstance(ctx);
		this.locale = ctx.getResources().getConfiguration().getLocales().get(0);
		this.issue = issue;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new IssueCommentViewHolder(
				inflater.inflate(R.layout.list_issue_comments, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		checkLoadMore(position);
		((IssueCommentsAdapter.IssueCommentViewHolder) holder)
				.bindData(issuesComments.get(position));
	}

	private void checkLoadMore(int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}
	}

	public void notifyDataChanged() {
		notifyItemInserted(issuesComments.size());
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<TimelineComment> list) {
		issuesComments = list;
		notifyDataChanged();
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return issuesComments.size();
	}

	@Override
	public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
		super.onViewRecycled(holder);
		if (holder instanceof IssueCommentViewHolder) {
			((IssueCommentViewHolder) holder).clearGlideRequests();
		}
	}

	public interface OnLoadMoreListener {
		void onLoadMore();

		void onLoadFinished();
	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private ImageView avatar;
		private TextView author;
		private TextView information;
		private RecyclerView comment;
		private LinearLayout commentReactionBadges;
		private MaterialCardView commentView;
		private RelativeLayout timelineView;
		private LinearLayout timelineData;
		private ImageView timelineIcon;
		private final List<Target<?>> glideTargets = new ArrayList<>();

		private String userLoginId;
		private TimelineComment issueComment;
		private boolean isAttachedToWindow = false;

		private IssueCommentViewHolder(View view) {
			super(view);
			initializeViews(view);
			setupAvatarClickListener();
			scheduleAttachmentLoading();
			setupMenuClickListener(view);
		}

		private void initializeViews(View view) {
			avatar = view.findViewById(R.id.avatar);
			author = view.findViewById(R.id.author);
			information = view.findViewById(R.id.information);
			comment = view.findViewById(R.id.comment);
			commentReactionBadges = view.findViewById(R.id.commentReactionBadges);
			commentView = view.findViewById(R.id.comment_view);
			timelineView = view.findViewById(R.id.timeline_view);
			timelineData = view.findViewById(R.id.timeline_data);
			timelineIcon = view.findViewById(R.id.timeline_icon);
		}

		private void setupAvatarClickListener() {
			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(userLoginId)) {
									avatar.setOnClickListener(
											loginId -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra("username", userLoginId);
												context.startActivity(intent);
											});

									avatar.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														userLoginId,
														context.getString(
																R.string.copyLoginIdToClipBoard,
																userLoginId));
												return true;
											});
								}
							},
							500);
		}

		private void scheduleAttachmentLoading() {
			String token = ((BaseActivity) context).getAccount().getAccount().getToken();
			new Handler()
					.postDelayed(
							() -> {
								if (issueComment != null) {
									getAttachments(issueComment.getId(), itemView, token, this);
								}
							},
							250);
		}

		void bindData(TimelineComment timelineComment) {
			clearGlideRequests();
			isAttachedToWindow = true;

			if (timelineComment == null) {
				hideAllViews();
				return;
			}

			this.issueComment = timelineComment;
			this.userLoginId = timelineComment.getUser().getLogin();

			commentView.setVisibility(View.GONE);
			timelineView.setVisibility(View.VISIBLE);
			timelineData.removeAllViews();

			String info = buildInfoText(timelineComment);

			if (isTimelineEvent(timelineComment)) {
				handleTimelineEvent(timelineComment, info);
			} else {
				timelineView.setVisibility(View.GONE);
				handleCommentEvent(timelineComment, info);
			}
		}

		private void hideAllViews() {
			timelineView.setVisibility(View.GONE);
			commentView.setVisibility(View.GONE);
		}

		private void hideTimelineViews() {
			timelineView.setVisibility(View.GONE);
		}

		private boolean isTimelineEvent(TimelineComment timelineComment) {
			return !timelineComment.getType().equalsIgnoreCase("comment");
		}

		private String buildInfoText(TimelineComment issueComment) {
			if (issueComment.getCreatedAt() == null) return "";

			StringBuilder infoBuilder =
					new StringBuilder(TimeHelper.formatTime(issueComment.getCreatedAt(), locale));

			information.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(
									issueComment.getCreatedAt()),
							context));

			if (!issueComment.getCreatedAt().equals(issueComment.getUpdatedAt())) {
				infoBuilder
						.append(context.getString(R.string.colorfulBulletSpan))
						.append(context.getString(R.string.modifiedText));
			}

			return infoBuilder.toString();
		}

		private void handleTimelineEvent(TimelineComment timelineComment, String info) {
			timelineView.setVisibility(View.VISIBLE);

			switch (timelineComment.getType().toLowerCase()) {
				case "label":
					handleLabelEvent(timelineComment, info);
					break;
				case "pull_push":
					handlePullPushEvent(timelineComment, info);
					break;
				case "assignees":
					handleAssigneesEvent(timelineComment, info);
					break;
				case "milestone":
					handleMilestoneEvent(timelineComment, info);
					break;
				case "close":
				case "reopen":
				case "merge_pull":
				case "commit_ref":
					handleStatusEvent(timelineComment, info);
					break;
				case "review_request":
				case "review":
				case "dismiss_review":
					handleReviewEvent(timelineComment, info);
					break;
				case "change_title":
					handleChangeTitleEvent(timelineComment, info);
					break;
				case "lock":
				case "unlock":
					handleLockEvent(timelineComment, info);
					break;
				case "add_dependency":
				case "remove_dependency":
					handleDependencyEvent(timelineComment, info);
					break;
				case "project":
				case "project_board":
					handleProjectEvent(timelineComment, info);
					break;
				case "added_deadline":
				case "modified_deadline":
				case "removed_deadline":
					handleDueDateEvent(timelineComment, info);
					break;
				case "change_target_branch":
				case "delete_branch":
					handleBranchEvent(timelineComment, info);
					break;
				case "start_tracking":
				case "stop_tracking":
				case "cancel_tracking":
				case "add_time_manual":
				case "delete_time_manual":
					handleTimeTrackingEvent(timelineComment, info);
					break;
				case "change_issue_ref":
				case "issue_ref":
				case "comment_ref":
				case "pull_ref":
					handleIssueRefEvent(timelineComment, info);
					break;
				case "pin":
					handlePinEvent(timelineComment, info);
					break;
				case "code":
				case "pull_scheduled_merge":
				case "pull_cancel_scheduled_merge":
					hideTimelineViews();
					break;
				default:
					timelineView.setVisibility(View.GONE);
			}
		}

		private void handleCommentEvent(TimelineComment timelineComment, String info) {
			commentView.setVisibility(View.VISIBLE);
			author.setText(timelineComment.getUser().getLogin());
			loadAvatarSafely(timelineComment.getUser().getAvatarUrl(), avatar);
			Markdown.render(context, timelineComment.getBody(), comment, issue.getRepository());
			information.setText(info);

			setupReactions(timelineComment);
		}

		private void setupReactions(TimelineComment timelineComment) {
			commentReactionBadges.removeAllViews();

			Bundle bundle1 = new Bundle();
			bundle1.putAll(bundle);
			bundle1.putInt("commentId", Math.toIntExact(timelineComment.getId()));

			ReactionList reactionList = new ReactionList(context, bundle1);
			commentReactionBadges.addView(reactionList);

			reactionList.setOnReactionAddedListener(
					() -> {
						if (commentReactionBadges.getVisibility() != View.VISIBLE) {
							commentReactionBadges.post(
									() -> commentReactionBadges.setVisibility(View.VISIBLE));
						}
					});
		}

		private void handleLabelEvent(TimelineComment timelineComment, String info) {
			TextView textView = createBaseTextView();
			int color = Color.parseColor("#" + timelineComment.getLabel().getColor());

			String text;
			if (timelineComment.getBody().isEmpty()) {
				text =
						context.getString(
								R.string.timelineRemovedLabel,
								timelineComment.getUser().getLogin(),
								info);
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			} else {
				text =
						context.getString(
								R.string.timelineAddedLabel,
								timelineComment.getUser().getLogin(),
								info);
			}

			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_tag));

			SpannableString spannableString = createLabelSpannable(text, timelineComment, color);

			new Handler(Looper.getMainLooper())
					.postDelayed(
							() -> {
								applyThemeColor(textView);
								textView.setText(spannableString);
								timelineData.addView(textView);
							},
							250);
		}

		private SpannableString createLabelSpannable(
				String text, TimelineComment timelineComment, int color) {
			Typeface typeface = AppUtil.getTypeface(context);
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
											timelineComment.getLabel().getName(),
											typeface,
											textSize,
											AppUtil.getPixelsFromDensity(context, 10)))
							.height(height)
							.endConfig()
							.buildRoundRect(
									timelineComment.getLabel().getName(),
									color,
									AppUtil.getPixelsFromDensity(context, 6));

			SpannableString spannableString = new SpannableString(text.replace('|', ' '));
			int placeholderIndex = text.indexOf('|');

			if (placeholderIndex != -1) {
				drawable.setBounds(
						0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
				spannableString.setSpan(
						new ImageSpan(drawable),
						placeholderIndex,
						placeholderIndex + 1,
						Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}

			return spannableString;
		}

		private void handlePullPushEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			try {
				JSONObject commitsObj = new JSONObject(timelineComment.getBody());
				JSONArray commitsShaArray = commitsObj.getJSONArray("commit_ids");

				String commitText =
						context.getResources()
								.getString(
										commitsShaArray.length() == 1
												? R.string.commitText
												: R.string.commitsText);

				String commitString =
						context.getString(
								R.string.timelineAddedCommit,
								timelineComment.getUser().getLogin(),
								commitText,
								info);

				applyThemeColor(startView);
				startView.setText(
						HtmlCompat.fromHtml(commitString, HtmlCompat.FROM_HTML_MODE_LEGACY));
				startView.setTextSize(14);

				timelineData.setOrientation(LinearLayout.VERTICAL);
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_commit));
				timelineData.addView(startView);

				addCommitViews(commitsShaArray);

			} catch (JSONException ignored) {
			}
		}

		private void addCommitViews(JSONArray commitsShaArray) {
			for (int i = 0; i < commitsShaArray.length(); i++) {
				try {
					String sha = commitsShaArray.getString(i);
					String shortSha = StringUtils.substring(sha, 0, 10);

					String timelineCommits =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightBlue, null)
									+ "'>"
									+ shortSha
									+ "</font>";

					TextView dynamicCommitTv = createBaseTextView();
					dynamicCommitTv.setId(View.generateViewId());
					dynamicCommitTv.setText(
							HtmlCompat.fromHtml(timelineCommits, HtmlCompat.FROM_HTML_MODE_LEGACY));

					dynamicCommitTv.setOnClickListener(
							v -> {
								intent =
										IssueContext.fromIntent(
														((IssueDetailActivity) context).getIntent())
												.getRepository()
												.getIntent(context, CommitDetailActivity.class);
								intent.putExtra("sha", sha);
								context.startActivity(intent);
							});

					timelineData.addView(dynamicCommitTv);
				} catch (JSONException ignored) {
				}
			}
		}

		private void handleAssigneesEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.isRemovedAssignee()) {
				if (timelineComment
						.getUser()
						.getLogin()
						.equalsIgnoreCase(timelineComment.getAssignee().getLogin())) {
					startView.setText(
							context.getString(
									R.string.timelineAssigneesRemoved,
									timelineComment.getUser().getLogin(),
									info));
				} else {
					startView.setText(
							context.getString(
									R.string.timelineAssigneesUnassigned,
									timelineComment.getAssignee().getLogin(),
									timelineComment.getUser().getLogin(),
									info));
				}
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			} else {
				if (timelineComment
						.getUser()
						.getLogin()
						.equalsIgnoreCase(timelineComment.getAssignee().getLogin())) {
					startView.setText(
							context.getString(
									R.string.timelineAssigneesSelfAssigned,
									timelineComment.getUser().getLogin(),
									info));
				} else {
					startView.setText(
							context.getString(
									R.string.timelineAssigneesAssigned,
									timelineComment.getAssignee().getLogin(),
									timelineComment.getUser().getLogin(),
									info));
				}
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_person));
			timelineData.addView(startView);
		}

		private void handleMilestoneEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.getMilestone() != null) {
				startView.setText(
						context.getString(
								R.string.timelineMilestoneAdded,
								timelineComment.getUser().getLogin(),
								timelineComment.getMilestone().getTitle(),
								info));
			} else {
				if (timelineComment.getOldMilestone() != null) {
					startView.setText(
							context.getString(
									R.string.timelineMilestoneRemoved,
									timelineComment.getUser().getLogin(),
									timelineComment.getOldMilestone().getTitle(),
									info));
				} else {
					startView.setText(
							context.getString(
									R.string.timelineMilestoneDeleted,
									timelineComment.getUser().getLogin(),
									info));
				}
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(
					ContextCompat.getDrawable(context, R.drawable.ic_milestone));
			timelineData.addView(startView);
		}

		private void handleStatusEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();
			boolean isIssue = issue.getIssueType().equalsIgnoreCase("Issue");

			switch (timelineComment.getType().toLowerCase()) {
				case "close":
					handleCloseEvent(startView, timelineComment, info, isIssue);
					break;
				case "reopen":
					handleReopenEvent(startView, timelineComment, info, isIssue);
					break;
				case "merge_pull":
					handleMergeEvent(startView, timelineComment, info);
					break;
				case "commit_ref":
					handleCommitRefEvent(startView, timelineComment, info, isIssue);
					break;
			}

			startView.setTextSize(14);
			timelineData.addView(startView);
		}

		private void handleCloseEvent(
				TextView startView, TimelineComment timelineComment, String info, boolean isIssue) {
			if (isIssue) {
				startView.setText(
						context.getString(
								R.string.timelineStatusClosedIssue,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_issue));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineStatusClosedPr,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
			}
			timelineIcon.setColorFilter(
					ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
		}

		private void handleReopenEvent(
				TextView startView, TimelineComment timelineComment, String info, boolean isIssue) {
			if (isIssue) {
				startView.setText(
						context.getString(
								R.string.timelineStatusReopenedIssue,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_issue));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineStatusReopenedPr,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
			}
		}

		private void handleMergeEvent(
				TextView startView, TimelineComment timelineComment, String info) {
			startView.setText(
					context.getString(
							R.string.timelineStatusMergedPr,
							timelineComment.getUser().getLogin(),
							info));
			timelineIcon.setImageDrawable(
					ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
			timelineIcon.setColorFilter(ContextCompat.getColor(context, R.color.iconPrMergedColor));
		}

		private void handleCommitRefEvent(
				TextView startView, TimelineComment timelineComment, String info, boolean isIssue) {
			String commitString =
					context.getString(
							isIssue
									? R.string.timelineStatusRefIssue
									: R.string.timelineStatusRefPr,
							timelineComment.getUser().getLogin(),
							ResourcesCompat.getColor(
									context.getResources(), R.color.lightBlue, null),
							context.getResources().getString(R.string.commitText).toLowerCase(),
							info);

			startView.setText(HtmlCompat.fromHtml(commitString, HtmlCompat.FROM_HTML_MODE_LEGACY));
			timelineIcon.setImageDrawable(
					ContextCompat.getDrawable(context, R.drawable.ic_bookmark));

			startView.setOnClickListener(
					v -> {
						intent =
								IssueContext.fromIntent(((IssueDetailActivity) context).getIntent())
										.getRepository()
										.getIntent(context, CommitDetailActivity.class);
						intent.putExtra("sha", timelineComment.getRefCommitSha());
						context.startActivity(intent);
					});
		}

		private void handleReviewEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			switch (timelineComment.getType().toLowerCase()) {
				case "review":
					if (!timelineComment.getBody().isEmpty()) {
						startView.setText(
								context.getString(
										R.string.timelineReviewLeftComment,
										timelineComment.getUser().getLogin(),
										timelineComment.getBody(),
										info));
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_comment));
					} else {
						hideTimelineViews();
						return;
					}
					break;

				case "dismiss_review":
					hideTimelineViews();
					return;

				case "review_request":
					String reviewer = getReviewerName(timelineComment);
					startView.setText(
							context.getString(
									R.string.timelineReviewRequest,
									timelineComment.getUser().getLogin(),
									reviewer,
									info));
					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_watchers));
					break;
			}

			startView.setTextSize(14);
			timelineData.addView(startView);
		}

		private String getReviewerName(TimelineComment timelineComment) {
			if (timelineComment.getAssignee() != null) {
				return timelineComment.getAssignee().getLogin();
			} else if (timelineComment.getAssigneeTeam() != null) {
				return timelineComment.getAssigneeTeam().getName();
			}
			return "";
		}

		private void handleChangeTitleEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			startView.setText(
					context.getString(
							R.string.timelineChangeTitle,
							timelineComment.getUser().getLogin(),
							timelineComment.getOldTitle(),
							timelineComment.getNewTitle(),
							info));
			startView.setTextSize(14);

			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_edit));
			timelineData.addView(startView);
		}

		private void handleLockEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.getType().equalsIgnoreCase("lock")) {
				startView.setText(
						context.getString(
								R.string.timelineLocked,
								timelineComment.getUser().getLogin(),
								timelineComment.getBody(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_lock));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineUnlocked,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_key));
			}

			startView.setTextSize(14);
			timelineData.addView(startView);
		}

		private void handleDependencyEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.getType().equalsIgnoreCase("add_dependency")) {
				startView.setText(
						context.getString(
								R.string.timelineDependencyAdded,
								timelineComment.getUser().getLogin(),
								timelineComment.getDependentIssue().getNumber(),
								info));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineDependencyRemoved,
								timelineComment.getUser().getLogin(),
								timelineComment.getDependentIssue().getNumber(),
								info));
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(
					ContextCompat.getDrawable(context, R.drawable.ic_dependency));
			timelineData.addView(startView);
		}

		private void handleProjectEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.getProjectId() > 0) {
				startView.setText(
						context.getString(
								R.string.timelineProjectAdded,
								timelineComment.getUser().getLogin(),
								info));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineProjectRemoved,
								timelineComment.getUser().getLogin(),
								info));
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_kanban));
			timelineData.addView(startView);
		}

		private void handleDueDateEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			switch (timelineComment.getType().toLowerCase()) {
				case "added_deadline":
					startView.setText(
							context.getString(
									R.string.timelineDueDateAdded,
									timelineComment.getUser().getLogin(),
									timelineComment.getBody(),
									info));
					break;

				case "modified_deadline":
					String[] dates = timelineComment.getBody().split("\\|");
					startView.setText(
							context.getString(
									R.string.timelineDueDateModified,
									timelineComment.getUser().getLogin(),
									dates[0],
									dates[1],
									info));
					break;

				case "removed_deadline":
					startView.setText(
							context.getString(
									R.string.timelineDueDateRemoved,
									timelineComment.getUser().getLogin(),
									timelineComment.getBody(),
									info));
					timelineIcon.setColorFilter(
							ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
					break;
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clock));
			timelineData.addView(startView);
		}

		private void handleBranchEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			if (timelineComment.getType().equalsIgnoreCase("change_target_branch")) {
				startView.setText(
						context.getString(
								R.string.timelineBranchChanged,
								timelineComment.getUser().getLogin(),
								timelineComment.getOldRef(),
								timelineComment.getNewRef(),
								info));
			} else {
				startView.setText(
						context.getString(
								R.string.timelineBranchDeleted,
								timelineComment.getUser().getLogin(),
								timelineComment.getOldRef(),
								info));
				timelineIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_branch));
			timelineData.addView(startView);
		}

		private void handleTimeTrackingEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			switch (timelineComment.getType().toLowerCase()) {
				case "start_tracking":
					startView.setText(
							context.getString(
									R.string.timelineTimeTrackingStart,
									timelineComment.getUser().getLogin(),
									info));
					break;

				case "stop_tracking":
					startView.setText(
							context.getString(
									R.string.timelineTimeTrackingStop,
									timelineComment.getUser().getLogin(),
									info));
					break;

				case "cancel_tracking":
					startView.setText(
							context.getString(
									R.string.timelineTimeTrackingCancel,
									timelineComment.getUser().getLogin(),
									info));
					timelineIcon.setColorFilter(
							ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
					break;

				case "add_time_manual":
					startView.setText(
							context.getString(
									R.string.timelineTimeTrackingAddManualTime,
									timelineComment.getUser().getLogin(),
									timelineComment.getBody(),
									info));
					break;

				case "delete_time_manual":
					startView.setText(
							context.getString(
									R.string.timelineTimeTrackingDeleteManualTime,
									timelineComment.getUser().getLogin(),
									timelineComment.getBody(),
									info));
					timelineIcon.setColorFilter(
							ContextCompat.getColor(context, R.color.iconIssuePrClosedColor));
					break;
			}

			startView.setTextSize(14);
			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clock));
			timelineData.addView(startView);
		}

		private void handleIssueRefEvent(TimelineComment timelineComment, String info) {
			RecyclerView recyclerView = new RecyclerView(context);

			if (timelineComment.getType().equalsIgnoreCase("change_issue_ref")) {
				String text =
						context.getString(
								R.string.timelineChangeIssueRef,
								timelineComment.getUser().getLogin(),
								timelineComment.getNewRef(),
								info);
				Markdown.render(context, text, recyclerView, issue.getRepository());
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_branch));
			} else {
				boolean isIssue = issue.getIssueType().equalsIgnoreCase("Issue");
				String text =
						context.getString(
								isIssue ? R.string.timelineRefIssue : R.string.timelineRefPr,
								timelineComment.getUser().getLogin(),
								timelineComment.getRefIssue().getHtmlUrl(),
								info);
				Markdown.render(context, text, recyclerView, issue.getRepository());
				timelineIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_bookmark));
			}

			timelineData.addView(recyclerView);
		}

		private void handlePinEvent(TimelineComment timelineComment, String info) {
			TextView startView = createBaseTextView();

			startView.setText(
					context.getString(
							R.string.timelinePinned, timelineComment.getUser().getLogin(), info));
			startView.setTextSize(14);

			timelineIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pin));
			timelineData.addView(startView);
		}

		private TextView createBaseTextView() {
			TextView textView = new TextView(context);
			applyThemeColor(textView);
			return textView;
		}

		private void applyThemeColor(TextView textView) {
			String themeValue =
					AppDatabaseSettings.getSettingsValue(
							context, AppDatabaseSettings.APP_THEME_KEY);
			if ("8".equals(themeValue) && !isNightModeThemeDynamic(context)) {
				textView.setTextColor(AppUtil.dynamicColorResource(context));
			}
		}

		private void loadAvatarSafely(String avatarUrl, ImageView avatarView) {
			if (isActivityValid()) {
				Target<Drawable> target =
						Glide.with(context)
								.load(avatarUrl)
								.diskCacheStrategy(DiskCacheStrategy.ALL)
								.placeholder(R.drawable.loader_animated)
								.centerCrop()
								.into(avatarView);
				glideTargets.add(target);
			}
		}

		private boolean isActivityValid() {
			if (context instanceof Activity activity) {
				return !activity.isFinishing() && !activity.isDestroyed();
			}
			return true;
		}

		void clearGlideRequests() {
			if (context != null && isActivityValid()) {
				for (Target<?> target : glideTargets) {
					Glide.with(context).clear(target);
				}
			}
			glideTargets.clear();
			isAttachedToWindow = false;
		}

		@SuppressLint("InflateParams")
		private void setupMenuClickListener(View view) {
			ImageView menu = view.findViewById(R.id.menu);
			menu.setOnClickListener(v -> showBottomSheetMenu());
		}

		@SuppressLint("InflateParams")
		private void showBottomSheetMenu() {
			final String loginUid =
					((BaseActivity) context).getAccount().getAccount().getUserName();

			LinearLayout root = new LinearLayout(context);
			root.setLayoutParams(
					new ViewGroup.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT));

			View vw =
					LayoutInflater.from(context)
							.inflate(R.layout.bottom_sheet_issue_comments, root, false);

			BottomSheetDialog dialog = new BottomSheetDialog(context);
			dialog.setContentView(vw);

			configureMenuItems(vw, dialog, loginUid);
			setupReactionSpinner(vw, dialog);

			dialog.show();
		}

		private void configureMenuItems(View vw, BottomSheetDialog dialog, String loginUid) {
			TextView commentMenuEdit = vw.findViewById(R.id.commentMenuEdit);
			TextView commentShare = vw.findViewById(R.id.issueCommentShare);
			TextView commentMenuQuote = vw.findViewById(R.id.commentMenuQuote);
			TextView commentMenuCopy = vw.findViewById(R.id.commentMenuCopy);
			TextView commentMenuDelete = vw.findViewById(R.id.commentMenuDelete);
			TextView issueCommentCopyUrl = vw.findViewById(R.id.issueCommentCopyUrl);
			TextView open = vw.findViewById(R.id.open);
			LinearLayout linearLayout = vw.findViewById(R.id.commentReactionButtons);

			if (issue.getRepository().getRepository().isArchived()) {
				commentMenuEdit.setVisibility(View.GONE);
				commentMenuDelete.setVisibility(View.GONE);
				commentMenuQuote.setVisibility(View.GONE);
				linearLayout.setVisibility(View.GONE);
			}

			if (!loginUid.contentEquals(issueComment.getUser().getLogin())
					&& !issue.getRepository().getPermissions().isPush()) {
				commentMenuEdit.setVisibility(View.GONE);
				commentMenuDelete.setVisibility(View.GONE);
			}

			if (issueComment.getBody().isEmpty()) {
				commentMenuCopy.setVisibility(View.GONE);
			}

			commentMenuEdit.setOnClickListener(v1 -> handleEditComment(dialog));
			commentShare.setOnClickListener(v1 -> handleShareComment(dialog));
			issueCommentCopyUrl.setOnClickListener(v1 -> handleCopyUrl(dialog));
			open.setOnClickListener(v1 -> handleOpenInBrowser(dialog));
			commentMenuQuote.setOnClickListener(v1 -> handleQuoteComment(dialog));
			commentMenuCopy.setOnClickListener(v1 -> handleCopyComment(dialog));
			commentMenuDelete.setOnClickListener(v1 -> handleDeleteComment(dialog));
		}

		private void setupReactionSpinner(View vw, BottomSheetDialog dialog) {
			LinearLayout linearLayout = vw.findViewById(R.id.commentReactionButtons);

			TextView loadReactions = new TextView(context);
			loadReactions.setText(context.getString(R.string.genericWaitFor));
			loadReactions.setGravity(Gravity.CENTER);
			loadReactions.setLayoutParams(
					new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			linearLayout.addView(loadReactions);

			Bundle bundle1 = new Bundle();
			bundle1.putAll(bundle);
			bundle1.putInt("commentId", Math.toIntExact(issueComment.getId()));

			ReactionSpinner reactionSpinner = new ReactionSpinner(context, bundle1);
			reactionSpinner.setOnInteractedListener(
					() -> {
						onInteractedListener.run();
						dialog.dismiss();
					});

			reactionSpinner.setOnLoadingFinishedListener(
					() -> {
						linearLayout.removeView(loadReactions);
						reactionSpinner.setLayoutParams(
								new ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.MATCH_PARENT, 160));
						linearLayout.addView(reactionSpinner);
					});
		}

		private void handleEditComment(BottomSheetDialog dialog) {
			IssueDetailActivity parentActivity = (IssueDetailActivity) context;
			EditText text = parentActivity.findViewById(R.id.comment_reply);
			text.append(issueComment.getBody());

			tinyDB.putString("commentAction", "edit");
			tinyDB.putInt("commentId", Math.toIntExact(issueComment.getId()));
			dialog.dismiss();
		}

		private void handleShareComment(BottomSheetDialog dialog) {
			AppUtil.sharingIntent(context, issueComment.getHtmlUrl());
			dialog.dismiss();
		}

		private void handleCopyUrl(BottomSheetDialog dialog) {
			AppUtil.copyToClipboard(
					context,
					issueComment.getHtmlUrl(),
					context.getString(R.string.copyIssueUrlToastMsg));
			dialog.dismiss();
		}

		private void handleOpenInBrowser(BottomSheetDialog dialog) {
			AppUtil.openUrlInBrowser(context, issueComment.getHtmlUrl());
			dialog.dismiss();
		}

		private void handleQuoteComment(BottomSheetDialog dialog) {
			StringBuilder stringBuilder = new StringBuilder();
			String commenterName = issueComment.getUser().getLogin();

			if (!commenterName.equals(
					((BaseActivity) context).getAccount().getAccount().getUserName())) {
				stringBuilder.append("@").append(commenterName).append("\n\n");
			}

			String[] lines = issueComment.getBody().split("\\R");
			for (String line : lines) {
				stringBuilder.append(">").append(line).append("\n");
			}

			IssueDetailActivity parentActivity = (IssueDetailActivity) context;
			EditText text = parentActivity.findViewById(R.id.comment_reply);
			text.setText(stringBuilder.append("\n").toString());
			dialog.dismiss();
		}

		private void handleCopyComment(BottomSheetDialog dialog) {
			ClipboardManager clipboard =
					(ClipboardManager)
							Objects.requireNonNull(context)
									.getSystemService(Context.CLIPBOARD_SERVICE);

			ClipData clip =
					ClipData.newPlainText(
							"Comment on issue #" + issue.getIssueIndex(), issueComment.getBody());
			clipboard.setPrimaryClip(clip);

			dialog.dismiss();
			Toasty.success(context, context.getString(R.string.copyIssueCommentToastMsg));
		}

		private void handleDeleteComment(BottomSheetDialog dialog) {
			deleteIssueComment(
					context, Math.toIntExact(issueComment.getId()), getBindingAdapterPosition());
			dialog.dismiss();
		}
	}

	private void getAttachments(
			Long issueIndex, View view, String token, IssueCommentViewHolder holder) {
		LinearLayout attachmentFrame = view.findViewById(R.id.attachmentFrame);
		LinearLayout attachmentsView = view.findViewById(R.id.attachmentsView);

		Call<List<Attachment>> call =
				RetrofitClient.getApiInterface(context)
						.issueListIssueCommentAttachments(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								issueIndex);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Attachment>> call,
							@NonNull retrofit2.Response<List<Attachment>> response) {
						if (holder == null || !holder.isAttachedToWindow) return;

						if (response.code() == 200
								&& response.body() != null
								&& !response.body().isEmpty()) {
							attachmentFrame.setVisibility(View.VISIBLE);
							displayAttachments(response.body(), attachmentsView, token, holder);
						} else {
							attachmentFrame.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Attachment>> call, @NonNull Throwable t) {}
				});
	}

	private void displayAttachments(
			List<Attachment> attachments,
			LinearLayout attachmentsView,
			String token,
			IssueCommentViewHolder holder) {
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(96, 96);
		params.setMargins(0, 0, 48, 0);

		for (Attachment attachment : attachments) {
			MaterialCardView cardView = createAttachmentCard(params);

			if (isImageAttachment(attachment.getName())) {
				setupImageAttachment(cardView, attachment, token, holder, params);
			} else {
				setupFileAttachment(cardView, attachment, params);
			}

			attachmentsView.addView(cardView);
		}
	}

	private MaterialCardView createAttachmentCard(LinearLayout.LayoutParams params) {
		MaterialCardView cardView = new MaterialCardView(context);
		cardView.setLayoutParams(params);
		cardView.setStrokeWidth(0);
		cardView.setRadius(28);
		cardView.setCardBackgroundColor(Color.TRANSPARENT);
		return cardView;
	}

	private boolean isImageAttachment(String filename) {
		String ext = FilenameUtils.getExtension(filename).toLowerCase();
		return Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif")
				.contains(ext);
	}

	private void setupImageAttachment(
			MaterialCardView cardView,
			Attachment attachment,
			String token,
			IssueCommentViewHolder holder,
			LinearLayout.LayoutParams params) {
		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(params);

		if (holder.isActivityValid()) {
			Target<Drawable> target =
					Glide.with(context)
							.load(attachment.getBrowserDownloadUrl() + "?token=" + token)
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.loader_animated)
							.centerCrop()
							.error(R.drawable.ic_close)
							.into(imageView);
			holder.glideTargets.add(target);
		}

		cardView.addView(imageView);
		cardView.setOnClickListener(
				v -> imageViewDialog(attachment.getBrowserDownloadUrl(), token, holder));
	}

	private void setupFileAttachment(
			MaterialCardView cardView, Attachment attachment, LinearLayout.LayoutParams params) {
		ImageView imageView = new ImageView(context);
		imageView.setImageResource(R.drawable.ic_file_download);
		imageView.setPadding(4, 4, 4, 4);
		imageView.setLayoutParams(params);

		cardView.addView(imageView);
		cardView.setOnClickListener(
				v -> {
					// TODO: Implement file download
				});
	}

	private void imageViewDialog(String url, String token, IssueCommentViewHolder holder) {
		if (holder == null || !holder.isActivityValid()) return;

		MaterialAlertDialogBuilder builder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		CustomImageViewDialogBinding binding =
				CustomImageViewDialogBinding.inflate(LayoutInflater.from(context));
		builder.setView(binding.getRoot());
		builder.setNeutralButton(context.getString(R.string.close), null);

		CustomTarget<Bitmap> target = createImageTarget(binding, holder);
		holder.glideTargets.add(target);

		if (holder.isActivityValid()) {
			Glide.with(context)
					.asBitmap()
					.load(url + "?token=" + token)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_close)
					.dontAnimate()
					.into(target);
		}

		AlertDialog dialog = builder.create();
		dialog.setOnDismissListener(
				dialogInterface -> {
					if (holder.isActivityValid()) {
						Glide.with(context).clear(target);
						holder.glideTargets.remove(target);
					}
				});
		dialog.show();
	}

	private CustomTarget<Bitmap> createImageTarget(
			CustomImageViewDialogBinding binding, IssueCommentViewHolder holder) {
		return new CustomTarget<>() {
			@Override
			public void onResourceReady(
					@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
				if (holder.isAttachedToWindow) {
					binding.imageView.setImageBitmap(resource);
					binding.imageView.buildDrawingCache();
				}
			}

			@Override
			public void onLoadCleared(Drawable placeholder) {
				if (holder.isAttachedToWindow) {
					binding.imageView.setImageDrawable(placeholder);
				}
			}

			@Override
			public void onLoadFailed(@Nullable Drawable errorDrawable) {
				super.onLoadFailed(errorDrawable);
				if (holder.isAttachedToWindow) {
					binding.imageView.setImageDrawable(errorDrawable);
				}
			}
		};
	}

	private void updateAdapter(int position) {
		issuesComments.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, issuesComments.size());
	}

	private void deleteIssueComment(final Context ctx, final int commentId, int position) {
		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.issueDeleteComment(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) commentId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {
						switch (response.code()) {
							case 204:
								updateAdapter(position);
								Toasty.success(
										ctx,
										ctx.getResources()
												.getString(R.string.deleteCommentSuccess));
								IssuesFragment.resumeIssues = true;
								break;
							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
								break;
							case 403:
								Toasty.error(ctx, ctx.getString(R.string.authorizeError));
								break;
							case 404:
								Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
								break;
							default:
								Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						Toasty.error(
								ctx,
								ctx.getResources().getString(R.string.genericServerResponseError));
					}
				});
	}
}
