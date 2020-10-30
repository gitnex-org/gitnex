package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.JsonElement;
import com.vdurmont.emoji.EmojiParser;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UserMentions;
import org.mian.gitnex.models.IssueComments;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.core.CorePlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.ext.tables.TablePlugin;
import io.noties.markwon.ext.tasklist.TaskListPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.image.DefaultMediaDecoder;
import io.noties.markwon.image.ImageItem;
import io.noties.markwon.image.ImagesPlugin;
import io.noties.markwon.image.SchemeHandler;
import io.noties.markwon.image.gif.GifMediaDecoder;
import io.noties.markwon.image.svg.SvgMediaDecoder;
import io.noties.markwon.linkify.LinkifyPlugin;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class IssueCommentsAdapter extends RecyclerView.Adapter<IssueCommentsAdapter.IssueCommentViewHolder> {

	private List<IssueComments> issuesComments;
	private FragmentManager fragmentManager;
	private Context mCtx;

	public IssueCommentsAdapter(Context mCtx, FragmentManager fragmentManager, List<IssueComments> issuesCommentsMain) {

		this.mCtx = mCtx;
		this.fragmentManager = fragmentManager;
		this.issuesComments = issuesCommentsMain;

	}

	class IssueCommentViewHolder extends RecyclerView.ViewHolder {

		private TextView issueNumber;
		private TextView commendId;
		private ImageView issueCommenterAvatar;
		private TextView issueComment;
		private TextView issueCommentDate;
		private TextView commendBodyRaw;
		private TextView commentModified;
		private TextView commenterUsername;
		private TextView htmlUrl;

		private IssueCommentViewHolder(View itemView) {

			super(itemView);

			issueNumber = itemView.findViewById(R.id.issueNumber);
			commendId = itemView.findViewById(R.id.commendId);
			issueCommenterAvatar = itemView.findViewById(R.id.issueCommenterAvatar);
			issueComment = itemView.findViewById(R.id.issueComment);
			issueCommentDate = itemView.findViewById(R.id.issueCommentDate);
			ImageView commentsOptionsMenu = itemView.findViewById(R.id.commentsOptionsMenu);
			commendBodyRaw = itemView.findViewById(R.id.commendBodyRaw);
			commentModified = itemView.findViewById(R.id.commentModified);
			commenterUsername = itemView.findViewById(R.id.commenterUsername);
			htmlUrl = itemView.findViewById(R.id.htmlUrl);

			commentsOptionsMenu.setOnClickListener(v -> {

				final Context ctx = v.getContext();
				final TinyDB tinyDb = new TinyDB(ctx);
				final String loginUid = tinyDb.getString("loginUid");

				@SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_issue_comments, null);

				TextView commentMenuEdit = view.findViewById(R.id.commentMenuEdit);
				TextView commentShare = view.findViewById(R.id.issueCommentShare);
				TextView commentMenuQuote = view.findViewById(R.id.commentMenuQuote);
				TextView commentMenuCopy = view.findViewById(R.id.commentMenuCopy);
				TextView commentMenuDelete = view.findViewById(R.id.commentMenuDelete);
				TextView issueCommentCopyUrl = view.findViewById(R.id.issueCommentCopyUrl);

				if(!loginUid.contentEquals(commenterUsername.getText())) {
					commentMenuEdit.setVisibility(View.GONE);
					commentMenuDelete.setVisibility(View.GONE);
				}

				if(issueComment.getText().toString().isEmpty()) {
					commentMenuCopy.setVisibility(View.GONE);
				}

				BottomSheetDialog dialog = new BottomSheetDialog(ctx);
				dialog.setContentView(view);
				dialog.show();

				commentMenuEdit.setOnClickListener(ediComment -> {

					Bundle bundle = new Bundle();
					bundle.putString("commentId", commendId.getText().toString());
					bundle.putString("commentAction", "edit");
					bundle.putString("commentBody", commendBodyRaw.getText().toString());

					BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");
					dialog.dismiss();

				});

				commentShare.setOnClickListener(ediComment -> {

					// get comment Url
					CharSequence commentUrl = htmlUrl.getText();

					// share issue comment
					Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
					sharingIntent.setType("text/plain");
					String intentHeader = tinyDb.getString("issueNumber") + ctx.getResources().getString(R.string.hash) + "issuecomment-" + commendId.getText() + " " + tinyDb.getString("issueTitle");
					sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, intentHeader);
					sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, commentUrl);
					ctx.startActivity(Intent.createChooser(sharingIntent, intentHeader));

					dialog.dismiss();

				});

				issueCommentCopyUrl.setOnClickListener(ediComment -> {

					// comment Url
					CharSequence commentUrl = htmlUrl.getText();

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText(commentUrl, commentUrl);
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

				});

				commentMenuQuote.setOnClickListener(v1 -> {

					StringBuilder stringBuilder = new StringBuilder();
					String commenterName = commenterUsername.getText().toString();

					if(!commenterName.equals(tinyDb.getString("userLogin"))) {

						stringBuilder.append("@").append(commenterName).append("\n\n");
					}

					String[] lines = commendBodyRaw.getText().toString().split("\\R");

					for(String line : lines) {

						stringBuilder.append(">").append(line).append("\n");
					}

					stringBuilder.append("\n");

					Bundle bundle = new Bundle();
					bundle.putString("commentBody", stringBuilder.toString());
					bundle.putBoolean("cursorToEnd", true);

					dialog.dismiss();
					BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");

				});

				commentMenuCopy.setOnClickListener(view1 -> {

					ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
					assert clipboard != null;

					ClipData clip = ClipData.newPlainText("Comment on issue #" + issueNumber.getText().toString(), issueComment.getText().toString());
					clipboard.setPrimaryClip(clip);

					dialog.dismiss();
					Toasty.success(ctx, ctx.getString(R.string.copyIssueCommentToastMsg));

				});

				commentMenuDelete.setOnClickListener(deleteComment -> {

					deleteIssueComment(ctx, Integer.parseInt(commendId.getText().toString()), getAdapterPosition());
					dialog.dismiss();

				});

			});

		}

	}

	private void updateAdapter(int position) {

		issuesComments.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, issuesComments.size());

	}

	private void deleteIssueComment(final Context ctx, final int commentId, int position) {

		final TinyDB tinyDb = new TinyDB(ctx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String[] repoFullName = tinyDb.getString("repoFullName").split("/");
		if (repoFullName.length != 2) {
			return;
		}
		final String repoOwner = repoFullName[0];
		final String repoName = repoFullName[1];

		Call<JsonElement> call;

		call = RetrofitClient
				.getInstance(instanceUrl, ctx)
				.getApiInterface()
				.deleteComment(instanceToken, repoOwner, repoName, commentId);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 204) {

					updateAdapter(position);
					Toasty.success(ctx, ctx.getResources().getString(R.string.deleteCommentSuccess));

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

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

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull IssueCommentsAdapter.IssueCommentViewHolder holder, int position) {

		final TinyDB tinyDb = new TinyDB(mCtx);
		final String locale = tinyDb.getString("locale");
		final String timeFormat = tinyDb.getString("dateFormat");

		IssueComments currentItem = issuesComments.get(position);

		holder.htmlUrl.setText(currentItem.getHtml_url());
		holder.commenterUsername.setText(currentItem.getUser().getUsername());
		holder.commendId.setText(String.valueOf(currentItem.getId()));
		holder.commendBodyRaw.setText(currentItem.getBody());

		if(!currentItem.getUser().getFull_name().equals("")) {
			holder.issueCommenterAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCommenter) + currentItem.getUser().getFull_name(), mCtx));
		}
		else {
			holder.issueCommenterAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCommenter) + currentItem.getUser().getLogin(), mCtx));
		}

		PicassoService.getInstance(mCtx).get().load(currentItem.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.issueCommenterAvatar);

		String cleanIssueComments = currentItem.getBody().trim();

		final Markwon markwon = Markwon.builder(Objects.requireNonNull(mCtx)).usePlugin(CorePlugin.create()).usePlugin(ImagesPlugin.create(new ImagesPlugin.ImagesConfigure() {

			@Override
			public void configureImages(@NonNull ImagesPlugin plugin) {

				plugin.addSchemeHandler(new SchemeHandler() {

					@NonNull
					@Override
					public ImageItem handle(@NonNull String raw, @NonNull Uri uri) {

						final int resourceId = mCtx.getResources().getIdentifier(raw.substring("drawable://".length()), "drawable", mCtx.getPackageName());

						final Drawable drawable = ContextCompat.getDrawable(mCtx, resourceId);

						assert drawable != null;
						return ImageItem.withResult(drawable);

					}

					@NonNull
					@Override
					public Collection<String> supportedSchemes() {

						return Collections.singleton("drawable");
					}
				});
				plugin.placeholderProvider(drawable -> null);
				plugin.addMediaDecoder(GifMediaDecoder.create(false));
				plugin.addMediaDecoder(SvgMediaDecoder.create(mCtx.getResources()));
				plugin.addMediaDecoder(SvgMediaDecoder.create());
				plugin.defaultMediaDecoder(DefaultMediaDecoder.create(mCtx.getResources()));
				plugin.defaultMediaDecoder(DefaultMediaDecoder.create());

			}
		})).usePlugin(new AbstractMarkwonPlugin() {

			@Override
			public void configureTheme(@NonNull MarkwonTheme.Builder builder) {

				builder.codeTextColor(tinyDb.getInt("codeBlockColor")).codeBackgroundColor(tinyDb.getInt("codeBlockBackground")).linkColor(mCtx.getResources().getColor(R.color.lightBlue));
			}

		}).usePlugin(TablePlugin.create(mCtx)).usePlugin(TaskListPlugin.create(mCtx)).usePlugin(HtmlPlugin.create()).usePlugin(StrikethroughPlugin.create()).usePlugin(LinkifyPlugin.create()).build();

		Spanned bodyWithMD = markwon.toMarkdown(EmojiParser.parseToUnicode(cleanIssueComments));
		markwon.setParsedMarkdown(holder.issueComment, UserMentions.UserMentionsFunc(mCtx, bodyWithMD, cleanIssueComments));

		String edited;

		if(!currentItem.getUpdated_at().equals(currentItem.getCreated_at())) {

			edited = mCtx.getResources().getString(R.string.colorfulBulletSpan) + mCtx.getResources().getString(R.string.modifiedText);
			holder.commentModified.setVisibility(View.VISIBLE);
			holder.commentModified.setText(edited);
			holder.commentModified.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdated_at()), mCtx));

		}
		else {

			holder.commentModified.setVisibility(View.INVISIBLE);

		}

		holder.issueCommentDate.setText(TimeHelper.formatTime(currentItem.getCreated_at(), new Locale(locale), timeFormat, mCtx));

		if(timeFormat.equals("pretty")) {
			holder.issueCommentDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getCreated_at()), mCtx));
		}
	}

	@Override
	public int getItemCount() {

		return issuesComments.size();

	}

}
