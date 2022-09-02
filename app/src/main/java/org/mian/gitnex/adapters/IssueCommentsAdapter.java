package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.TimelineComment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.views.ReactionList;
import org.mian.gitnex.views.ReactionSpinner;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class IssueCommentsAdapter extends RecyclerView.Adapter<IssueCommentsAdapter.IssueCommentViewHolder> {

	private final Context context;
	private final TinyDB tinyDB;
	private final Bundle bundle;
	private final List<TimelineComment> issuesComments;
	private final FragmentManager fragmentManager;
	private final Runnable onInteractedListener;
	private final Locale locale;
	private final IssueContext issue;

	public IssueCommentsAdapter(Context ctx, Bundle bundle, List<TimelineComment> issuesCommentsMain, FragmentManager fragmentManager, Runnable onInteractedListener, IssueContext issue) {

		this.context = ctx;
		this.bundle = bundle;
		this.issuesComments = issuesCommentsMain;
		this.fragmentManager = fragmentManager;
		this.onInteractedListener = onInteractedListener;
		tinyDB = TinyDB.getInstance(ctx);
		locale = ctx.getResources().getConfiguration().locale;
		this.issue = issue;
	}

	private void updateAdapter(int position) {

		issuesComments.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, issuesComments.size());
	}

	private void deleteIssueComment(final Context ctx, final int commentId, int position) {

		Call<Void> call = RetrofitClient.getApiInterface(ctx).issueDeleteComment(issue.getRepository().getOwner(), issue.getRepository().getName(), (long) commentId);

		call.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				switch(response.code()) {

					case 204:
						updateAdapter(position);
						Toasty.success(ctx, ctx.getResources().getString(R.string.deleteCommentSuccess));
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

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	@NonNull
	@Override
	public IssueCommentsAdapter.IssueCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_issue_comments, parent, false);
		return new IssueCommentsAdapter.IssueCommentViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull IssueCommentsAdapter.IssueCommentViewHolder holder, int position) {

		String timeFormat = tinyDB.getString("dateFormat", "pretty");
		TimelineComment issueComment = issuesComments.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		holder.userLoginId = issueComment.getUser().getLogin();

		holder.issueComment = issueComment;

		StringBuilder informationBuilder = null;
		if(issueComment.getCreatedAt() != null) {

			if(timeFormat.equals("pretty")) {
				informationBuilder = new StringBuilder(TimeHelper.formatTime(issueComment.getCreatedAt(), locale, "pretty", context));
				holder.information.setOnClickListener(v -> TimeHelper.customDateFormatForToastDateFormat(issueComment.getCreatedAt()));
			}
			else if(timeFormat.equals("normal")) {
				informationBuilder = new StringBuilder(TimeHelper.formatTime(issueComment.getCreatedAt(), locale, "normal", context));
			}

			if(!issueComment.getCreatedAt().equals(issueComment.getUpdatedAt())) {
				if(informationBuilder != null) {
					informationBuilder.append(context.getString(R.string.colorfulBulletSpan)).append(context.getString(R.string.modifiedText));
				}
			}
		}

		// label view in timeline
		if(issueComment.getType().equalsIgnoreCase("label")) {

			holder.labelView.setVisibility(View.VISIBLE);

			int color = Color.parseColor("#" + issueComment.getLabel().getColor());

			ImageView labelsView = new ImageView(context);

			int height = AppUtil.getPixelsFromDensity(context, 20);
			int textSize = AppUtil.getPixelsFromScaledDensity(context, 12);

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).textColor(new ColorInverter().getContrastColor(color)).fontSize(textSize)
				.width(LabelWidthCalculator.calculateLabelWidth(issueComment.getLabel().getName(), Typeface.DEFAULT, textSize, AppUtil.getPixelsFromDensity(context, 10))).height(height).endConfig()
				.buildRoundRect(issueComment.getLabel().getName(), color, AppUtil.getPixelsFromDensity(context, 18));

			labelsView.setImageDrawable(drawable);

			TextView start = new TextView(context);
			TextView end = new TextView(context);

			if(issueComment.getBody().equals("")){
				start.setText(issueComment.getUser().getLogin() + " removed the ");
			}
			else {
				start.setText(issueComment.getUser().getLogin() + " added the ");
			}
			end.setText(" label " + informationBuilder);
			end.setTextSize(12);
			start.setTextSize(12);

			holder.labelData.addView(start);
			holder.labelData.addView(labelsView);
			holder.labelData.addView(end);
		}
		else {
			holder.labelView.setVisibility(View.GONE);
		}

		// pull/push/commit data view in timeline
		if(issueComment.getType().equalsIgnoreCase("pull_push")) {

			holder.commitView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);

			start.setText(issueComment.getUser().getLogin() + " added commit " + informationBuilder);
			start.setTextSize(12);

			holder.commitData.addView(start);
		}
		else {
			holder.commitView.setVisibility(View.GONE);
		}

		// assignees data view in timeline
		if(issueComment.getType().equalsIgnoreCase("assignees")) {

			holder.assigneesView.setVisibility(View.VISIBLE);
			TextView start = new TextView(context);

			if(issueComment.getUser().getLogin().equalsIgnoreCase(issueComment.getAssignee().getLogin())) {
				start.setText(issueComment.getUser().getLogin() + " self-assigned this " + informationBuilder);
			}
			else if(issueComment.isRemovedAssignee()) {

				if(issueComment.getUser().getLogin().equalsIgnoreCase(issueComment.getAssignee().getLogin())) {
					start.setText(issueComment.getUser().getLogin() + " removed their assignment " + informationBuilder);
				}
				else {
					start.setText(issueComment.getAssignee().getLogin() + " was unassigned by " + issueComment.getUser().getLogin() + " " + informationBuilder);
				}
			}
			else {
				start.setText(issueComment.getAssignee().getLogin() + " was assigned by " + issueComment.getUser().getLogin() + " " + informationBuilder);
			}


			start.setTextSize(12);

			holder.assigneesData.addView(start);
		}
		else {
			holder.assigneesView.setVisibility(View.GONE);
		}

		// milestone data view in timeline
		if(issueComment.getType().equalsIgnoreCase("milestone")) {

			holder.milestoneView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);

			if(issueComment.getMilestone() != null) {
				start.setText(issueComment.getUser().getLogin() + " added this to the " + issueComment.getMilestone().getTitle() + " milestone " + informationBuilder);
			}
			else {
				start.setText(issueComment.getUser().getLogin() + " removed this from the " + issueComment.getOldMilestone().getTitle() + " milestone " + informationBuilder);
			}
			start.setTextSize(12);

			holder.milestoneData.addView(start);
		}
		else {
			holder.milestoneView.setVisibility(View.GONE);
		}

		// status view in timeline
		if(issueComment.getType().equalsIgnoreCase("close") || issueComment.getType().equalsIgnoreCase("reopen") || issueComment.getType().equalsIgnoreCase("merge_pull") || issueComment.getType().equalsIgnoreCase("commit_ref")) {

			holder.statusView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);

			if(issue.getIssueType().equalsIgnoreCase("Issue")) {
				if(issueComment.getType().equals("close")) {
					start.setText(issueComment.getUser().getLogin() + " closed this issue " + informationBuilder);
					holder.statusIcon.setColorFilter(context.getResources().getColor(R.color.iconIssuePrClosedColor, null));
				}
				else if(issueComment.getType().equalsIgnoreCase("reopen")) {
					start.setText(issueComment.getUser().getLogin() + " reopened this issue " + informationBuilder);
				}
			}
			else if(issue.getIssueType().equalsIgnoreCase("Pull")) {
				if(issueComment.getType().equalsIgnoreCase("close")) {
					start.setText(issueComment.getUser().getLogin() + " closed this pull request " + informationBuilder);
					holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
					holder.statusIcon.setColorFilter(context.getResources().getColor(R.color.iconIssuePrClosedColor, null));
				}
				else if(issueComment.getType().equalsIgnoreCase("merge_pull")) {
					start.setText(issueComment.getUser().getLogin() + " merged this pull request " + informationBuilder);
					holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
					holder.statusIcon.setColorFilter(context.getResources().getColor(R.color.iconPrMergedColor, null));
				}
				else if(issueComment.getType().equalsIgnoreCase("commit_ref")) {
					start.setText(issueComment.getUser().getLogin() + " referenced this issue from a commit " + issueComment.getRefCommitSha() + " " + informationBuilder);
					holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_bookmark));
				}
				else {
					start.setText(issue.getIssueType() + " reopened this pull request " + informationBuilder);
					holder.statusIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pull_request));
				}
			}
			start.setTextSize(12);

			holder.statusData.addView(start);
		}
		else {
			holder.statusView.setVisibility(View.GONE);
		}

		// review data view in timeline
		if(issueComment.getType().equalsIgnoreCase("review") || issueComment.getType().equalsIgnoreCase("review_request")) {

			holder.reviewView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);

			/*if(issueComment.getType().equalsIgnoreCase("review")) {
				start.setText(issueComment.getUser().getLogin() + " approved these changes " + informationBuilder);
			}
			else*/ if(issueComment.getType().equalsIgnoreCase("review_request")) {
				start.setText(issueComment.getUser().getLogin() + " requested review from " + issueComment.getAssignee().getLogin() + " " + informationBuilder);
				holder.reviewIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_unwatch));
			}
			start.setTextSize(12);

			holder.reviewData.addView(start);
		}
		else {
			holder.reviewView.setVisibility(View.GONE);
		}

		// change title data view in timeline
		if(issueComment.getType().equalsIgnoreCase("change_title")) {

			holder.changeTitleView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);
			start.setText(issueComment.getUser().getLogin() + " changed title from " + issueComment.getOldTitle() + " to " + issueComment.getNewTitle() + " " + informationBuilder);
			start.setTextSize(12);

			holder.changeTitleData.addView(start);
		}
		else {
			holder.changeTitleView.setVisibility(View.GONE);
		}

		// lock/unlock data view in timeline
		if(issueComment.getType().equalsIgnoreCase("lock") || issueComment.getType().equalsIgnoreCase("unlock")) {

			holder.lockView.setVisibility(View.VISIBLE);

			TextView start = new TextView(context);

			if(issueComment.getType().equalsIgnoreCase("lock")) {
				start.setText(issueComment.getUser().getLogin() + " locked as " + issueComment.getBody() + " and limited conversation to collaborators " + informationBuilder);
			}
			else if(issueComment.getType().equalsIgnoreCase("unlock")) {
				start.setText(issueComment.getUser().getLogin() + " unlocked this conversation " + informationBuilder);
				holder.lockIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_key));
			}
			start.setTextSize(12);

			holder.lockData.addView(start);
		}
		else {
			holder.lockView.setVisibility(View.GONE);
		}

		// comment data view in timeline
		if(issueComment.getType().equalsIgnoreCase("comment")) {

			holder.author.setText(issueComment.getUser().getLogin());

			PicassoService.getInstance(context).get().load(issueComment.getUser().getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0))
				.resize(AppUtil.getPixelsFromDensity(context, 35), AppUtil.getPixelsFromDensity(context, 35)).centerCrop().into(holder.avatar);

			Markdown.render(context, EmojiParser.parseToUnicode(issueComment.getBody()), holder.comment, issue.getRepository());

			holder.information.setText(informationBuilder);

			Bundle bundle1 = new Bundle();
			bundle1.putAll(bundle);
			bundle1.putInt("commentId", Math.toIntExact(issueComment.getId()));

			ReactionList reactionList = new ReactionList(context, bundle1);

			holder.commentReactionBadges.addView(reactionList);
			reactionList.setOnReactionAddedListener(() -> {

				if(holder.commentReactionBadges.getVisibility() != View.VISIBLE) {
					holder.commentReactionBadges.post(() -> holder.commentReactionBadges.setVisibility(View.VISIBLE));
				}
			});
		}
		else {
			holder.commentView.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		if(issuesComments != null) {
			return issuesComments.size();
		}
		else {
			return 0;
		}
	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView author;
		private final TextView information;
		private final RecyclerView comment;
		private final LinearLayout commentReactionBadges;
		private String userLoginId;
		private TimelineComment issueComment;

		private final MaterialCardView commentView;

		private final LinearLayout labelView;
		private final LinearLayout labelData;

		private final LinearLayout commitView;
		private final LinearLayout commitData;

		private final LinearLayout assigneesView;
		private final LinearLayout assigneesData;

		private final LinearLayout milestoneView;
		private final LinearLayout milestoneData;

		private final LinearLayout statusView;
		private final LinearLayout statusData;
		private final ImageView statusIcon;

		private final LinearLayout reviewView;
		private final LinearLayout reviewData;
		private final ImageView reviewIcon;

		private final LinearLayout changeTitleView;
		private final LinearLayout changeTitleData;

		private final LinearLayout lockView;
		private final LinearLayout lockData;
		private final ImageView lockIcon;

		private IssueCommentViewHolder(View view) {

			super(view);

			avatar = view.findViewById(R.id.avatar);
			author = view.findViewById(R.id.author);
			information = view.findViewById(R.id.information);
			ImageView menu = view.findViewById(R.id.menu);
			comment = view.findViewById(R.id.comment);
			commentReactionBadges = view.findViewById(R.id.commentReactionBadges);

			commentView = view.findViewById(R.id.comment_view);

			labelView = view.findViewById(R.id.label_view);
			labelData = view.findViewById(R.id.label_data);

			commitView = view.findViewById(R.id.commit_view);
			commitData = view.findViewById(R.id.commit_data);

			assigneesView = view.findViewById(R.id.assignees_view);
			assigneesData = view.findViewById(R.id.assignees_data);

			milestoneView = view.findViewById(R.id.milestone_view);
			milestoneData = view.findViewById(R.id.milestone_data);

			statusView = view.findViewById(R.id.status_view);
			statusData = view.findViewById(R.id.status_data);
			statusIcon = view.findViewById(R.id.status_icon);

			reviewView = view.findViewById(R.id.review_view);
			reviewData = view.findViewById(R.id.review_data);
			reviewIcon = view.findViewById(R.id.review_icon);

			changeTitleView = view.findViewById(R.id.change_title_view);
			changeTitleData = view.findViewById(R.id.change_title_data);

			lockView = view.findViewById(R.id.lock_unlock_view);
			lockData = view.findViewById(R.id.lock_unlock_data);
			lockIcon = view.findViewById(R.id.lock_unlock_icon);

			menu.setOnClickListener(v -> {

				final String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();

				@SuppressLint("InflateParams") View vw = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_issue_comments, null);

				TextView commentMenuEdit = vw.findViewById(R.id.commentMenuEdit);
				TextView commentShare = vw.findViewById(R.id.issueCommentShare);
				TextView commentMenuQuote = vw.findViewById(R.id.commentMenuQuote);
				TextView commentMenuCopy = vw.findViewById(R.id.commentMenuCopy);
				TextView commentMenuDelete = vw.findViewById(R.id.commentMenuDelete);
				TextView issueCommentCopyUrl = vw.findViewById(R.id.issueCommentCopyUrl);
				TextView open = vw.findViewById(R.id.open);
				LinearLayout linearLayout = vw.findViewById(R.id.commentReactionButtons);

				if(issue.getRepository().getRepository().isArchived()) {
					commentMenuEdit.setVisibility(View.GONE);
					commentMenuDelete.setVisibility(View.GONE);
					commentMenuQuote.setVisibility(View.GONE);
					linearLayout.setVisibility(View.GONE);
				}

				if(!loginUid.contentEquals(issueComment.getUser().getLogin()) && !issue.getRepository().getPermissions().isPush()) {
					commentMenuEdit.setVisibility(View.GONE);
					commentMenuDelete.setVisibility(View.GONE);
				}

				if(issueComment.getBody().isEmpty()) {
					commentMenuCopy.setVisibility(View.GONE);
				}

				BottomSheetDialog dialog = new BottomSheetDialog(context);
				dialog.setContentView(vw);
				dialog.show();

				TextView loadReactions = new TextView(context);
				loadReactions.setText(context.getString(R.string.genericWaitFor));
				loadReactions.setGravity(Gravity.CENTER);
				loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
				linearLayout.addView(loadReactions);

				Bundle bundle1 = new Bundle();
				bundle1.putAll(bundle);
				bundle1.putInt("commentId", Math.toIntExact(issueComment.getId()));

				ReactionSpinner reactionSpinner = new ReactionSpinner(context, bundle1);
				reactionSpinner.setOnInteractedListener(() -> {
					onInteractedListener.run();
					dialog.dismiss();
				});

				reactionSpinner.setOnLoadingFinishedListener(() -> {
					linearLayout.removeView(loadReactions);
					reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
					linearLayout.addView(reactionSpinner);
				});

				commentMenuEdit.setOnClickListener(v1 -> {
					Bundle bundle = new Bundle();
					bundle.putInt("commentId", Math.toIntExact(issueComment.getId()));
					bundle.putString("commentAction", "edit");
					bundle.putString("commentBody", issueComment.getBody());

					BottomSheetReplyFragment bottomSheetReplyFragment = BottomSheetReplyFragment.newInstance(bundle, issue);
					bottomSheetReplyFragment.setOnInteractedListener(onInteractedListener);
					bottomSheetReplyFragment.show(fragmentManager, "replyBottomSheet");

					dialog.dismiss();
				});

				commentShare.setOnClickListener(v1 -> {

					AppUtil.sharingIntent(context, issueComment.getHtmlUrl());
					dialog.dismiss();
				});

				issueCommentCopyUrl.setOnClickListener(v1 -> {

					AppUtil.copyToClipboard(context, issueComment.getHtmlUrl(), context.getString(R.string.copyIssueUrlToastMsg));
					dialog.dismiss();
				});

				open.setOnClickListener(v1 -> {

					AppUtil.openUrlInBrowser(context, issueComment.getHtmlUrl());
					dialog.dismiss();
				});

				commentMenuQuote.setOnClickListener(v1 -> {
					StringBuilder stringBuilder = new StringBuilder();
					String commenterName = issueComment.getUser().getLogin();

					if(!commenterName.equals(((BaseActivity) context).getAccount().getAccount().getUserName())) {
						stringBuilder.append("@").append(commenterName).append("\n\n");
					}

					String[] lines = issueComment.getBody().split("\\R");

					for(String line : lines) {
						stringBuilder.append(">").append(line).append("\n");
					}

					stringBuilder.append("\n");

					Bundle bundle = new Bundle();
					bundle.putString("commentBody", stringBuilder.toString());
					bundle.putBoolean("cursorToEnd", true);

					dialog.dismiss();
					BottomSheetReplyFragment.newInstance(bundle, issue).show(fragmentManager, "replyBottomSheet");
				});

				commentMenuCopy.setOnClickListener(v1 -> {
					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(context).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText("Comment on issue #" + issue.getIssueIndex(), issueComment.getBody());
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(context, context.getString(R.string.copyIssueCommentToastMsg));
				});

				commentMenuDelete.setOnClickListener(v1 -> {
					deleteIssueComment(context, Math.toIntExact(issueComment.getId()), getAdapterPosition());
					dialog.dismiss();
				});

			});

			new Handler().postDelayed(() -> {
				if(!AppUtil.checkGhostUsers(userLoginId)) {
					avatar.setOnClickListener(loginId -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", userLoginId);
						context.startActivity(intent);
					});

					avatar.setOnLongClickListener(loginId -> {
						AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
						return true;
					});
				}
			}, 500);
		}

	}

}
