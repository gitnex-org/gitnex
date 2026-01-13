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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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
		tinyDB = TinyDB.getInstance(ctx);
		locale = ctx.getResources().getConfiguration().getLocales().get(0);
		this.issue = issue;
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

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new IssueCommentViewHolder(
				inflater.inflate(R.layout.list_issue_comments, parent, false));
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

		((IssueCommentsAdapter.IssueCommentViewHolder) holder)
				.bindData(issuesComments.get(position));
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

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView author;
		private final TextView information;
		private final RecyclerView comment;
		private final LinearLayout commentReactionBadges;
		private final MaterialCardView commentView;
		private final RelativeLayout timelineView;
		private final LinearLayout timelineData;
		private final ImageView timelineIcon;
		private String userLoginId;
		private TimelineComment issueComment;
		private final LinearLayout timelineDividerView;
		private final FrameLayout timelineLine2;
		private final List<Target<?>> glideTargets = new ArrayList<>();
		private boolean isAttachedToWindow = false;

		private IssueCommentViewHolder(View view) {

			super(view);

			avatar = view.findViewById(R.id.avatar);
			author = view.findViewById(R.id.author);
			information = view.findViewById(R.id.information);
			ImageView menu = view.findViewById(R.id.menu);
			comment = view.findViewById(R.id.comment);
			commentReactionBadges = view.findViewById(R.id.commentReactionBadges);

			commentView = view.findViewById(R.id.comment_view);

			timelineView = view.findViewById(R.id.timeline_view);
			timelineData = view.findViewById(R.id.timeline_data);
			timelineIcon = view.findViewById(R.id.timeline_icon);
			timelineDividerView = view.findViewById(R.id.timeline_divider_view);
			timelineLine2 = view.findViewById(R.id.timeline_line_2);

			String token = ((BaseActivity) context).getAccount().getAccount().getToken();

			Handler handler = new Handler();

			handler.postDelayed(
					() -> {
						if (issueComment != null) {
							getAttachments(issueComment.getId(), view, token, this);
						}
					},
					250);

			menu.setOnClickListener(
					v -> {
						final String loginUid =
								((BaseActivity) context).getAccount().getAccount().getUserName();

						@SuppressLint("InflateParams")
						View vw =
								LayoutInflater.from(context)
										.inflate(R.layout.bottom_sheet_issue_comments, null);

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

						BottomSheetDialog dialog = new BottomSheetDialog(context);
						dialog.setContentView(vw);
						dialog.show();

						TextView loadReactions = new TextView(context);
						loadReactions.setText(context.getString(R.string.genericWaitFor));
						loadReactions.setGravity(Gravity.CENTER);
						loadReactions.setLayoutParams(
								new ViewGroup.LayoutParams(
										ViewGroup.LayoutParams.MATCH_PARENT, 160));
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

						commentMenuEdit.setOnClickListener(
								v1 -> {
									IssueDetailActivity parentActivity =
											(IssueDetailActivity) context;
									EditText text = parentActivity.findViewById(R.id.comment_reply);
									text.append(issueComment.getBody());

									tinyDB.putString("commentAction", "edit");
									tinyDB.putInt(
											"commentId", Math.toIntExact(issueComment.getId()));

									dialog.dismiss();
								});

						commentShare.setOnClickListener(
								v1 -> {
									AppUtil.sharingIntent(context, issueComment.getHtmlUrl());
									dialog.dismiss();
								});

						issueCommentCopyUrl.setOnClickListener(
								v1 -> {
									AppUtil.copyToClipboard(
											context,
											issueComment.getHtmlUrl(),
											context.getString(R.string.copyIssueUrlToastMsg));
									dialog.dismiss();
								});

						open.setOnClickListener(
								v1 -> {
									AppUtil.openUrlInBrowser(context, issueComment.getHtmlUrl());
									dialog.dismiss();
								});

						commentMenuQuote.setOnClickListener(
								v1 -> {
									StringBuilder stringBuilder = new StringBuilder();
									String commenterName = issueComment.getUser().getLogin();

									if (!commenterName.equals(
											((BaseActivity) context)
													.getAccount()
													.getAccount()
													.getUserName())) {
										stringBuilder
												.append("@")
												.append(commenterName)
												.append("\n\n");
									}

									String[] lines = issueComment.getBody().split("\\R");

									for (String line : lines) {
										stringBuilder.append(">").append(line).append("\n");
									}

									String comment = String.valueOf(stringBuilder.append("\n"));

									IssueDetailActivity parentActivity =
											(IssueDetailActivity) context;
									EditText text = parentActivity.findViewById(R.id.comment_reply);
									text.setText(comment);

									dialog.dismiss();
								});

						commentMenuCopy.setOnClickListener(
								v1 -> {
									ClipboardManager clipboard =
											(ClipboardManager)
													Objects.requireNonNull(context)
															.getSystemService(
																	Context.CLIPBOARD_SERVICE);
									assert clipboard != null;

									ClipData clip =
											ClipData.newPlainText(
													"Comment on issue #" + issue.getIssueIndex(),
													issueComment.getBody());
									clipboard.setPrimaryClip(clip);

									dialog.dismiss();
									Toasty.success(
											context,
											context.getString(R.string.copyIssueCommentToastMsg));
								});

						commentMenuDelete.setOnClickListener(
								v1 -> {
									deleteIssueComment(
											context,
											Math.toIntExact(issueComment.getId()),
											getBindingAdapterPosition());
									dialog.dismiss();
								});
					});

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

		void bindData(TimelineComment timelineComment) {

			clearGlideRequests();
			isAttachedToWindow = true;

			if (timelineComment != null) {

				Typeface typeface = AppUtil.getTypeface(context);
				int fontSize = 14;

				userLoginId = timelineComment.getUser().getLogin();

				this.issueComment = timelineComment;

				/*if (getBindingAdapterPosition() == 0) {
					timelineDividerView.setVisibility(View.GONE);
				} else {
					timelineDividerView.setVisibility(View.VISIBLE);
				}

				if (getBindingAdapterPosition() == getItemCount() - 1) {
					timelineLine2.setVisibility(View.GONE);
				} else {
					timelineLine2.setVisibility(View.VISIBLE);
				}*/

				StringBuilder infoBuilder = null;
				if (issueComment.getCreatedAt() != null) {

					infoBuilder =
							new StringBuilder(
									TimeHelper.formatTime(issueComment.getCreatedAt(), locale));

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
				}
				assert infoBuilder != null;
				String info = infoBuilder.toString();

				// label view in timeline
				if (issueComment.getType().equalsIgnoreCase("label")) {

					int color = Color.parseColor("#" + issueComment.getLabel().getColor());
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
													issueComment.getLabel().getName(),
													typeface,
													textSize,
													AppUtil.getPixelsFromDensity(context, 10)))
									.height(height)
									.endConfig()
									.buildRoundRect(
											issueComment.getLabel().getName(),
											color,
											AppUtil.getPixelsFromDensity(context, 6));

					TextView textView = new TextView(context);
					String text;

					if (issueComment.getBody().isEmpty()) {
						text =
								context.getString(
										R.string.timelineRemovedLabel,
										issueComment.getUser().getLogin(),
										info);
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					} else {
						text =
								context.getString(
										R.string.timelineAddedLabel,
										issueComment.getUser().getLogin(),
										info);
					}

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_tag));

					SpannableString spannableString = new SpannableString(text.replace('|', ' '));

					drawable.setBounds(
							0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
					ImageSpan image = new ImageSpan(drawable);

					new Handler()
							.postDelayed(
									() -> {
										spannableString.setSpan(
												image,
												text.indexOf('|'),
												text.indexOf('|') + 1,
												Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

										if (Integer.parseInt(
														AppDatabaseSettings.getSettingsValue(
																context,
																AppDatabaseSettings.APP_THEME_KEY))
												== 8) {
											if (!isNightModeThemeDynamic(context)) {
												textView.setTextColor(
														AppUtil.dynamicColorResource(context));
											}
										}
										textView.setText(spannableString);
										timelineData.addView(textView);
									},
									250);
				}
				// pull/push/commit data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("pull_push")) {

					TextView start = new TextView(context);

					JSONObject commitsObj = null;
					try {
						commitsObj = new JSONObject(issueComment.getBody());
					} catch (JSONException ignored) {
					}

					JSONArray commitsShaArray = null;
					try {
						commitsShaArray =
								Objects.requireNonNull(commitsObj).getJSONArray("commit_ids");
					} catch (JSONException ignored) {
					}

					String commitText = context.getResources().getString(R.string.commitsText);
					if (Objects.requireNonNull(commitsShaArray).length() == 1) {
						commitText = context.getResources().getString(R.string.commitText);
					}

					String commitString =
							context.getString(
									R.string.timelineAddedCommit,
									issueComment.getUser().getLogin(),
									commitText,
									info);
					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}
					start.setText(
							HtmlCompat.fromHtml(commitString, HtmlCompat.FROM_HTML_MODE_LEGACY));
					start.setTextSize(fontSize);

					timelineData.setOrientation(LinearLayout.VERTICAL);
					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_commit));
					timelineData.addView(start);

					for (int i = 0; i < Objects.requireNonNull(commitsShaArray).length(); i++) {

						try {

							String timelineCommits =
									"<font color='"
											+ ResourcesCompat.getColor(
													context.getResources(), R.color.lightBlue, null)
											+ "'>"
											+ StringUtils.substring(
													String.valueOf(commitsShaArray.get(i)), 0, 10)
											+ "</font>";

							TextView dynamicCommitTv = new TextView(context);
							dynamicCommitTv.setId(View.generateViewId());

							dynamicCommitTv.setText(
									HtmlCompat.fromHtml(
											timelineCommits, HtmlCompat.FROM_HTML_MODE_LEGACY));

							JSONArray finalCommitsArray = commitsShaArray;
							int finalI = i;

							dynamicCommitTv.setOnClickListener(
									v14 -> {
										intent =
												IssueContext.fromIntent(
																((IssueDetailActivity) context)
																		.getIntent())
														.getRepository()
														.getIntent(
																context,
																CommitDetailActivity.class);
										try {
											intent.putExtra(
													"sha", (String) finalCommitsArray.get(finalI));
										} catch (JSONException ignored) {
										}
										context.startActivity(intent);
									});

							timelineData.setOrientation(LinearLayout.VERTICAL);
							timelineData.addView(dynamicCommitTv);
						} catch (JSONException ignored) {
						}
					}
				}
				// assignees data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("assignees")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.isRemovedAssignee()) {

						if (issueComment
								.getUser()
								.getLogin()
								.equalsIgnoreCase(issueComment.getAssignee().getLogin())) {
							start.setText(
									context.getString(
											R.string.timelineAssigneesRemoved,
											issueComment.getUser().getLogin(),
											info));
						} else {
							start.setText(
									context.getString(
											R.string.timelineAssigneesUnassigned,
											issueComment.getAssignee().getLogin(),
											issueComment.getUser().getLogin(),
											info));
						}
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					} else {
						if (issueComment
								.getUser()
								.getLogin()
								.equalsIgnoreCase(issueComment.getAssignee().getLogin())) {
							start.setText(
									context.getString(
											R.string.timelineAssigneesSelfAssigned,
											issueComment.getUser().getLogin(),
											info));
						} else {
							start.setText(
									context.getString(
											R.string.timelineAssigneesAssigned,
											issueComment.getAssignee().getLogin(),
											issueComment.getUser().getLogin(),
											info));
						}
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_person));
					timelineData.addView(start);
				}
				// milestone data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("milestone")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getMilestone() != null) {
						start.setText(
								context.getString(
										R.string.timelineMilestoneAdded,
										issueComment.getUser().getLogin(),
										issueComment.getMilestone().getTitle(),
										info));
					} else {
						if (issueComment.getOldMilestone() != null) {
							start.setText(
									context.getString(
											R.string.timelineMilestoneRemoved,
											issueComment.getUser().getLogin(),
											issueComment.getOldMilestone().getTitle(),
											info));
						} else {
							start.setText(
									context.getString(
											R.string.timelineMilestoneDeleted,
											issueComment.getUser().getLogin(),
											info));
						}
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);
					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_milestone));
					timelineData.addView(start);
				}
				// status view in timeline
				else if (issueComment.getType().equalsIgnoreCase("close")
						|| issueComment.getType().equalsIgnoreCase("reopen")
						|| issueComment.getType().equalsIgnoreCase("merge_pull")
						|| issueComment.getType().equalsIgnoreCase("commit_ref")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issue.getIssueType().equalsIgnoreCase("Issue")) {
						if (issueComment.getType().equals("close")) {
							start.setText(
									context.getString(
											R.string.timelineStatusClosedIssue,
											issueComment.getUser().getLogin(),
											info));
							timelineIcon.setColorFilter(
									context.getResources()
											.getColor(R.color.iconIssuePrClosedColor, null));
						} else if (issueComment.getType().equalsIgnoreCase("reopen")) {
							start.setText(
									context.getString(
											R.string.timelineStatusReopenedIssue,
											issueComment.getUser().getLogin(),
											info));
						} else if (issueComment.getType().equalsIgnoreCase("commit_ref")) {
							String commitString =
									context.getString(
											R.string.timelineStatusRefIssue,
											issueComment.getUser().getLogin(),
											ResourcesCompat.getColor(
													context.getResources(),
													R.color.lightBlue,
													null),
											context.getResources()
													.getString(R.string.commitText)
													.toLowerCase(),
											info);
							start.setText(
									HtmlCompat.fromHtml(
											commitString, HtmlCompat.FROM_HTML_MODE_LEGACY));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_bookmark));

							start.setOnClickListener(
									v14 -> {
										intent =
												IssueContext.fromIntent(
																((IssueDetailActivity) context)
																		.getIntent())
														.getRepository()
														.getIntent(
																context,
																CommitDetailActivity.class);
										intent.putExtra("sha", issueComment.getRefCommitSha());
										context.startActivity(intent);
									});
						}
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_issue));
					} else if (issue.getIssueType().equalsIgnoreCase("Pull")) {
						if (issueComment.getType().equalsIgnoreCase("close")) {
							start.setText(
									context.getString(
											R.string.timelineStatusClosedPr,
											issueComment.getUser().getLogin(),
											info));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
							timelineIcon.setColorFilter(
									context.getResources()
											.getColor(R.color.iconIssuePrClosedColor, null));
						} else if (issueComment.getType().equalsIgnoreCase("merge_pull")) {
							start.setText(
									context.getString(
											R.string.timelineStatusMergedPr,
											issueComment.getUser().getLogin(),
											info));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
							timelineIcon.setColorFilter(
									context.getResources()
											.getColor(R.color.iconPrMergedColor, null));
						} else if (issueComment.getType().equalsIgnoreCase("commit_ref")) {
							String commitString =
									context.getString(
											R.string.timelineStatusRefPr,
											issueComment.getUser().getLogin(),
											ResourcesCompat.getColor(
													context.getResources(),
													R.color.lightBlue,
													null),
											context.getResources()
													.getString(R.string.commitText)
													.toLowerCase(),
											info);
							start.setText(
									HtmlCompat.fromHtml(
											commitString, HtmlCompat.FROM_HTML_MODE_LEGACY));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_bookmark));

							start.setOnClickListener(
									v14 -> {
										intent =
												IssueContext.fromIntent(
																((IssueDetailActivity) context)
																		.getIntent())
														.getRepository()
														.getIntent(
																context,
																CommitDetailActivity.class);
										intent.putExtra("sha", issueComment.getRefCommitSha());
										context.startActivity(intent);
									});
						} else {
							start.setText(
									context.getString(
											R.string.timelineStatusReopenedPr,
											issueComment.getUser().getLogin(),
											info));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
						}
					}
					start.setTextSize(fontSize);

					timelineData.addView(start);
				}
				// review data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("review_request")
						|| issueComment.getType().equalsIgnoreCase("review")
						|| issueComment.getType().equalsIgnoreCase("dismiss_review")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("review")) {
						if (!issueComment.getBody().equalsIgnoreCase("")) {

							start.setText(
									context.getString(
											R.string.timelineReviewLeftComment,
											issueComment.getUser().getLogin(),
											issueComment.getBody(),
											info));
							timelineIcon.setImageDrawable(
									ContextCompat.getDrawable(context, R.drawable.ic_comment));
						} else {
							timelineView.setVisibility(View.GONE);
							timelineDividerView.setVisibility(View.GONE);
						}
					} else if (issueComment.getType().equalsIgnoreCase("dismiss_review")) {
						timelineView.setVisibility(View.GONE);
						timelineDividerView.setVisibility(View.GONE);
					} else if (issueComment.getType().equalsIgnoreCase("review_request")) {
						String reviewer;
						if (issueComment.getAssignee() != null) {
							reviewer = issueComment.getAssignee().getLogin();
						} else {
							if (issueComment.getAssigneeTeam() != null) {
								reviewer = issueComment.getAssigneeTeam().getName();
							} else {
								reviewer = "";
							}
						}
						start.setText(
								context.getString(
										R.string.timelineReviewRequest,
										issueComment.getUser().getLogin(),
										reviewer,
										info));
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_watchers));
					}
					start.setTextSize(fontSize);

					timelineData.addView(start);
				}
				// change title data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("change_title")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					start.setText(
							context.getString(
									R.string.timelineChangeTitle,
									issueComment.getUser().getLogin(),
									issueComment.getOldTitle(),
									issueComment.getNewTitle(),
									info));
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_edit));
					timelineData.addView(start);
				}
				// lock/unlock data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("lock")
						|| issueComment.getType().equalsIgnoreCase("unlock")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("lock")) {
						start.setText(
								context.getString(
										R.string.timelineLocked,
										issueComment.getUser().getLogin(),
										issueComment.getBody(),
										info));
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_lock));
					} else if (issueComment.getType().equalsIgnoreCase("unlock")) {
						start.setText(
								context.getString(
										R.string.timelineUnlocked,
										issueComment.getUser().getLogin(),
										info));
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_key));
					}
					start.setTextSize(fontSize);

					timelineData.addView(start);
				}
				// dependency data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("add_dependency")
						|| issueComment.getType().equalsIgnoreCase("remove_dependency")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("add_dependency")) {
						start.setText(
								context.getString(
										R.string.timelineDependencyAdded,
										issueComment.getUser().getLogin(),
										issueComment.getDependentIssue().getNumber(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("remove_dependency")) {
						start.setText(
								context.getString(
										R.string.timelineDependencyRemoved,
										issueComment.getUser().getLogin(),
										issueComment.getDependentIssue().getNumber(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_dependency));
					timelineData.addView(start);
				}
				// project data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("project")
						|| issueComment.getType().equalsIgnoreCase("project_board")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getProjectId() > 0) {
						start.setText(
								context.getString(
										R.string.timelineProjectAdded,
										issueComment.getUser().getLogin(),
										info));
					} else {
						start.setText(
								context.getString(
										R.string.timelineProjectRemoved,
										issueComment.getUser().getLogin(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_kanban));
					timelineData.addView(start);
				}
				// due date/deadline data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("added_deadline")
						|| issueComment.getType().equalsIgnoreCase("modified_deadline")
						|| issueComment.getType().equalsIgnoreCase("removed_deadline")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("added_deadline")) {
						start.setText(
								context.getString(
										R.string.timelineDueDateAdded,
										issueComment.getUser().getLogin(),
										issueComment.getBody(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("modified_deadline")) {
						start.setText(
								context.getString(
										R.string.timelineDueDateModified,
										issueComment.getUser().getLogin(),
										issueComment.getBody().split("\\|")[0],
										issueComment.getBody().split("\\|")[1],
										info));
					} else if (issueComment.getType().equalsIgnoreCase("removed_deadline")) {
						start.setText(
								context.getString(
										R.string.timelineDueDateRemoved,
										issueComment.getUser().getLogin(),
										issueComment.getBody(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_clock));
					timelineData.addView(start);
				}
				// branch data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("change_target_branch")
						|| issueComment.getType().equalsIgnoreCase("delete_branch")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("change_target_branch")) {
						start.setText(
								context.getString(
										R.string.timelineBranchChanged,
										issueComment.getUser().getLogin(),
										issueComment.getOldRef(),
										issueComment.getNewRef(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("delete_branch")) {
						start.setText(
								context.getString(
										R.string.timelineBranchDeleted,
										issueComment.getUser().getLogin(),
										issueComment.getOldRef(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_branch));
					timelineData.addView(start);
				}
				// time tracking data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("start_tracking")
						|| issueComment.getType().equalsIgnoreCase("stop_tracking")
						|| issueComment.getType().equalsIgnoreCase("cancel_tracking")
						|| issueComment.getType().equalsIgnoreCase("add_time_manual")
						|| issueComment.getType().equalsIgnoreCase("delete_time_manual")) {

					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					if (issueComment.getType().equalsIgnoreCase("start_tracking")) {
						start.setText(
								context.getString(
										R.string.timelineTimeTrackingStart,
										issueComment.getUser().getLogin(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("stop_tracking")) {
						start.setText(
								context.getString(
										R.string.timelineTimeTrackingStop,
										issueComment.getUser().getLogin(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("cancel_tracking")) {
						start.setText(
								context.getString(
										R.string.timelineTimeTrackingCancel,
										issueComment.getUser().getLogin(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					} else if (issueComment.getType().equalsIgnoreCase("add_time_manual")) {
						start.setText(
								context.getString(
										R.string.timelineTimeTrackingAddManualTime,
										issueComment.getUser().getLogin(),
										issueComment.getBody(),
										info));
					} else if (issueComment.getType().equalsIgnoreCase("delete_time_manual")) {
						start.setText(
								context.getString(
										R.string.timelineTimeTrackingDeleteManualTime,
										issueComment.getUser().getLogin(),
										issueComment.getBody(),
										info));
						timelineIcon.setColorFilter(
								context.getResources()
										.getColor(R.color.iconIssuePrClosedColor, null));
					}
					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_clock));
					timelineData.addView(start);
				}
				// issue/pr refs data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("change_issue_ref")
						|| issueComment.getType().equalsIgnoreCase("issue_ref")
						|| issueComment.getType().equalsIgnoreCase("comment_ref")
						|| issueComment.getType().equalsIgnoreCase("pull_ref")) {

					RecyclerView recyclerView = new RecyclerView(context);

					if (issueComment.getType().equalsIgnoreCase("change_issue_ref")) {
						String text =
								context.getString(
										R.string.timelineChangeIssueRef,
										issueComment.getUser().getLogin(),
										issueComment.getNewRef(),
										info);
						Markdown.render(context, text, recyclerView, issue.getRepository());
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_branch));
					} else if (issueComment.getType().equalsIgnoreCase("comment_ref")
							|| issueComment.getType().equalsIgnoreCase("issue_ref")
							|| issueComment.getType().equalsIgnoreCase("pull_ref")) {

						if (issue.getIssueType().equalsIgnoreCase("Issue")) {
							String text =
									context.getString(
											R.string.timelineRefIssue,
											issueComment.getUser().getLogin(),
											timelineComment.getRefIssue().getHtmlUrl(),
											info);
							Markdown.render(context, text, recyclerView, issue.getRepository());
						} else if (issue.getIssueType().equalsIgnoreCase("Pull")) {
							String text =
									context.getString(
											R.string.timelineRefPr,
											issueComment.getUser().getLogin(),
											timelineComment.getRefIssue().getHtmlUrl(),
											info);
							Markdown.render(context, text, recyclerView, issue.getRepository());
						}
						timelineIcon.setImageDrawable(
								ContextCompat.getDrawable(context, R.drawable.ic_bookmark));
					}

					timelineData.addView(recyclerView);
				}
				// code data view in timeline
				else if (issueComment.getType().equalsIgnoreCase("code")) {
					timelineView.setVisibility(View.GONE);
					timelineDividerView.setVisibility(View.GONE);
				}
				// schedule pr view in timeline
				else if (issueComment.getType().equalsIgnoreCase("pull_scheduled_merge")
						|| issueComment.getType().equalsIgnoreCase("pull_cancel_scheduled_merge")) {
					timelineView.setVisibility(View.GONE);
					timelineDividerView.setVisibility(View.GONE);
				}
				// issue/pr pinned
				else if (issueComment.getType().equalsIgnoreCase("pin")) {
					TextView start = new TextView(context);

					if (Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											context, AppDatabaseSettings.APP_THEME_KEY))
							== 8) {
						if (!isNightModeThemeDynamic(context)) {
							start.setTextColor(AppUtil.dynamicColorResource(context));
						}
					}

					start.setText(
							context.getString(
									R.string.timelinePinned,
									issueComment.getUser().getLogin(),
									info));

					start.setTextSize(fontSize);

					timelineIcon.setImageDrawable(
							ContextCompat.getDrawable(context, R.drawable.ic_pin));
					timelineData.addView(start);
				} else {
					timelineView.setVisibility(View.GONE);
				}

				// comment data view in timeline
				if (issueComment.getType().equalsIgnoreCase("comment")) {

					author.setText(issueComment.getUser().getLogin());

					loadAvatarSafely(issueComment.getUser().getAvatarUrl(), avatar);

					Markdown.render(
							context, issueComment.getBody(), comment, issue.getRepository());

					information.setText(info);

					Bundle bundle1 = new Bundle();
					bundle1.putAll(bundle);
					bundle1.putInt("commentId", Math.toIntExact(issueComment.getId()));

					ReactionList reactionList = new ReactionList(context, bundle1);

					commentReactionBadges.addView(reactionList);
					reactionList.setOnReactionAddedListener(
							() -> {
								if (commentReactionBadges.getVisibility() != View.VISIBLE) {
									commentReactionBadges.post(
											() ->
													commentReactionBadges.setVisibility(
															View.VISIBLE));
								}
							});
				} else {
					commentView.setVisibility(View.GONE);
				}
			} else {
				timelineDividerView.setVisibility(View.GONE);
				timelineView.setVisibility(View.GONE);
				commentView.setVisibility(View.GONE);
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

						if (holder == null || !holder.isAttachedToWindow) {
							return;
						}

						List<Attachment> attachment = response.body();

						if (response.code() == 200) {
							assert attachment != null;

							if (!attachment.isEmpty()) {

								attachmentFrame.setVisibility(View.VISIBLE);
								LinearLayout.LayoutParams paramsAttachment =
										new LinearLayout.LayoutParams(96, 96);
								paramsAttachment.setMargins(0, 0, 48, 0);

								for (int i = 0; i < attachment.size(); i++) {

									ImageView attachmentView = new ImageView(context);
									MaterialCardView materialCardView =
											new MaterialCardView(context);
									materialCardView.setLayoutParams(paramsAttachment);
									materialCardView.setStrokeWidth(0);
									materialCardView.setRadius(28);
									materialCardView.setCardBackgroundColor(Color.TRANSPARENT);

									if (Arrays.asList(
													"bmp", "gif", "jpg", "jpeg", "png", "webp",
													"heic", "heif")
											.contains(
													FilenameUtils.getExtension(
																	attachment.get(i).getName())
															.toLowerCase())) {

										if (holder.isActivityValid()) {
											Target<Drawable> target =
													Glide.with(context)
															.load(
																	attachment
																					.get(i)
																					.getBrowserDownloadUrl()
																			+ "?token="
																			+ token)
															.diskCacheStrategy(
																	DiskCacheStrategy.ALL)
															.placeholder(R.drawable.loader_animated)
															.centerCrop()
															.error(R.drawable.ic_close)
															.into(attachmentView);

											holder.glideTargets.add(target);
										}

										attachmentsView.addView(materialCardView);
										attachmentView.setLayoutParams(paramsAttachment);
										materialCardView.addView(attachmentView);

										int finalI1 = i;
										materialCardView.setOnClickListener(
												v1 ->
														imageViewDialog(
																attachment
																		.get(finalI1)
																		.getBrowserDownloadUrl(),
																token,
																holder));

									} else {

										attachmentView.setImageResource(
												R.drawable.ic_file_download);
										attachmentView.setPadding(4, 4, 4, 4);
										attachmentsView.addView(materialCardView);
										attachmentView.setLayoutParams(paramsAttachment);
										materialCardView.addView(attachmentView);

										// int finalI = i;
										materialCardView.setOnClickListener(
												v1 -> {
													// filesize = attachment.get(finalI).getSize();
													// filename = attachment.get(finalI).getName();
													// filehash = attachment.get(finalI).getUuid();
													// requestFileDownload();
												});
									}
								}
							} else {
								attachmentFrame.setVisibility(View.GONE);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Attachment>> call, @NonNull Throwable t) {}
				});
	}

	private void imageViewDialog(String url, String token, IssueCommentViewHolder holder) {

		if (holder == null || !holder.isActivityValid()) {
			return;
		}

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		CustomImageViewDialogBinding imageViewDialogBinding =
				CustomImageViewDialogBinding.inflate(LayoutInflater.from(context));
		View view = imageViewDialogBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setNeutralButton(context.getString(R.string.close), null);

		CustomTarget<Bitmap> target =
				new CustomTarget<Bitmap>() {
					@Override
					public void onResourceReady(
							@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
						if (holder.isAttachedToWindow) {
							imageViewDialogBinding.imageView.setImageBitmap(resource);
							imageViewDialogBinding.imageView.buildDrawingCache();
						}
					}

					@Override
					public void onLoadCleared(Drawable placeholder) {
						if (holder.isAttachedToWindow) {
							imageViewDialogBinding.imageView.setImageDrawable(placeholder);
						}
					}

					@Override
					public void onLoadFailed(@Nullable Drawable errorDrawable) {
						super.onLoadFailed(errorDrawable);
						if (holder.isAttachedToWindow) {
							imageViewDialogBinding.imageView.setImageDrawable(errorDrawable);
						}
					}
				};

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

		AlertDialog dialog = materialAlertDialogBuilder.create();

		dialog.setOnDismissListener(
				dialogInterface -> {
					if (holder.isActivityValid()) {
						Glide.with(context).clear(target);
						holder.glideTargets.remove(target);
					}
				});

		dialog.show();
	}

	@Override
	public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
		super.onViewRecycled(holder);

		if (holder instanceof IssueCommentViewHolder) {
			((IssueCommentViewHolder) holder).clearGlideRequests();
		}
	}
}
