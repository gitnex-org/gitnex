package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonElement;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.IssueComments;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
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
 * Author M M Arif
 */

public class IssueCommentsAdapter extends RecyclerView.Adapter<IssueCommentsAdapter.IssueCommentViewHolder> {

	private final Context context;
	private final TinyDB tinyDB;
	private final Bundle bundle;
	private final List<IssueComments> issuesComments;
	private final FragmentManager fragmentManager;
	private final Runnable onInteractedListener;
	private final Locale locale;
	private final IssueContext issue;

	public IssueCommentsAdapter(Context ctx, Bundle bundle, List<IssueComments> issuesCommentsMain, FragmentManager fragmentManager, Runnable onInteractedListener, IssueContext issue) {

		this.context = ctx;
		this.bundle = bundle;
		this.issuesComments = issuesCommentsMain;
		this.fragmentManager = fragmentManager;
		this.onInteractedListener = onInteractedListener;
		tinyDB = TinyDB.getInstance(ctx);
		locale = ctx.getResources().getConfiguration().locale;
		this.issue = issue;
	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private String userLoginId;
		private IssueComments issueComment;

		private final ImageView avatar;
		private final TextView author;
		private final TextView information;
		private final RecyclerView comment;
		private final LinearLayout commentReactionBadges;

		private IssueCommentViewHolder(View view) {

			super(view);

			avatar = view.findViewById(R.id.avatar);
			author = view.findViewById(R.id.author);
			information = view.findViewById(R.id.information);
			ImageView menu = view.findViewById(R.id.menu);
			comment = view.findViewById(R.id.comment);
			commentReactionBadges = view.findViewById(R.id.commentReactionBadges);

			menu.setOnClickListener(v -> {

				final String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();

				@SuppressLint("InflateParams") View vw = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_issue_comments, null);

				TextView commentMenuEdit = vw.findViewById(R.id.commentMenuEdit);
				TextView commentShare = vw.findViewById(R.id.issueCommentShare);
				TextView commentMenuQuote = vw.findViewById(R.id.commentMenuQuote);
				TextView commentMenuCopy = vw.findViewById(R.id.commentMenuCopy);
				TextView commentMenuDelete = vw.findViewById(R.id.commentMenuDelete);
				TextView issueCommentCopyUrl = vw.findViewById(R.id.issueCommentCopyUrl);
				LinearLayout linearLayout = vw.findViewById(R.id.commentReactionButtons);

				if(issue.getRepository().getRepository().isArchived()) {
					commentMenuEdit.setVisibility(View.GONE);
					commentMenuDelete.setVisibility(View.GONE);
					commentMenuQuote.setVisibility(View.GONE);
					linearLayout.setVisibility(View.GONE);
				}

				if(!loginUid.contentEquals(issueComment.getUser().getUsername()) && !issue.getRepository().getPermissions().canPush()) {
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
				bundle1.putInt("commentId", issueComment.getId());

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
					bundle.putInt("commentId", issueComment.getId());
					bundle.putString("commentAction", "edit");
					bundle.putString("commentBody", issueComment.getBody());

					BottomSheetReplyFragment bottomSheetReplyFragment = BottomSheetReplyFragment.newInstance(bundle, issue);
					bottomSheetReplyFragment.setOnInteractedListener(onInteractedListener);
					bottomSheetReplyFragment.show(fragmentManager, "replyBottomSheet");

					dialog.dismiss();
				});

				commentShare.setOnClickListener(v1 -> {
					// get comment Url
					CharSequence commentUrl = issueComment.getHtml_url();

					// share issue comment
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					String intentHeader = issue.getIssueIndex() + context.getResources().getString(R.string.hash) + "issuecomment-" + issueComment.getId() + " " + issue.getIssue().getTitle();
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, intentHeader);
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, commentUrl);
					context.startActivity(Intent.createChooser(sharingIntent, intentHeader));

					dialog.dismiss();
				});

				issueCommentCopyUrl.setOnClickListener(v1 -> {
					// comment Url
					CharSequence commentUrl = issueComment.getHtml_url();

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(context).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText(commentUrl, commentUrl);
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(context, context.getString(R.string.copyIssueUrlToastMsg));
				});

				commentMenuQuote.setOnClickListener(v1 -> {
					StringBuilder stringBuilder = new StringBuilder();
					String commenterName = issueComment.getUser().getUsername();

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
					deleteIssueComment(context, issueComment.getId(), getAdapterPosition());
					dialog.dismiss();
				});

			});

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
	}

	private void updateAdapter(int position) {

		issuesComments.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, issuesComments.size());
	}

	private void deleteIssueComment(final Context ctx, final int commentId, int position) {

		Call<JsonElement> call = RetrofitClient
				.getApiInterface(ctx)
				.deleteComment(((BaseActivity) context).getAccount().getAuthorization(), issue.getRepository().getOwner(), issue.getRepository().getName(), commentId);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				switch(response.code()) {

					case 204:
						updateAdapter(position);
						Toasty.success(ctx, ctx.getResources().getString(R.string.deleteCommentSuccess));
						break;

					case 401:
						AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.cancelButton),
							ctx.getResources().getString(R.string.navLogout));
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
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

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
		IssueComments issueComment = issuesComments.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		holder.userLoginId = issueComment.getUser().getLogin();

		holder.issueComment = issueComment;
		holder.author.setText(issueComment.getUser().getUsername());

		PicassoService.getInstance(context).get()
			.load(issueComment.getUser().getAvatar_url())
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(imgRadius, 0))
			.resize(AppUtil.getPixelsFromDensity(context, 35), AppUtil.getPixelsFromDensity(context, 35))
			.centerCrop()
			.into(holder.avatar);

		Markdown.render(context, EmojiParser.parseToUnicode(issueComment.getBody()), holder.comment, issue.getRepository());

		StringBuilder informationBuilder = null;
		if(issueComment.getCreated_at() != null) {

			if(timeFormat.equals("pretty")) {
				informationBuilder = new StringBuilder(TimeHelper.formatTime(issueComment.getCreated_at(), locale, "pretty", context));
				holder.information.setOnClickListener(v -> TimeHelper.customDateFormatForToastDateFormat(issueComment.getCreated_at()));
			}
			else if(timeFormat.equals("normal")) {
				informationBuilder = new StringBuilder(TimeHelper.formatTime(issueComment.getCreated_at(), locale, "normal", context));
			}

			if(!issueComment.getCreated_at().equals(issueComment.getUpdated_at())) {
				if(informationBuilder != null) {
					informationBuilder.append(context.getString(R.string.colorfulBulletSpan)).append(context.getString(R.string.modifiedText));
				}
			}
		}

		holder.information.setText(informationBuilder);

		Bundle bundle1 = new Bundle();
		bundle1.putAll(bundle);
		bundle1.putInt("commentId", issueComment.getId());

		ReactionList reactionList = new ReactionList(context, bundle1);

		holder.commentReactionBadges.addView(reactionList);
		reactionList.setOnReactionAddedListener(() -> {

			if(holder.commentReactionBadges.getVisibility() != View.VISIBLE) {
				holder.commentReactionBadges.post(() -> holder.commentReactionBadges.setVisibility(View.VISIBLE));
			}
		});

	}

	@Override
	public int getItemCount() {
		return issuesComments.size();
	}

}
