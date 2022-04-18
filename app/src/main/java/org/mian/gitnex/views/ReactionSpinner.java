package org.mian.gitnex.views;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.gitnex.tea4j.v2.models.EditReactionOption;
import org.gitnex.tea4j.v2.models.GeneralUISettings;
import org.gitnex.tea4j.v2.models.Reaction;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

/**
 * @author opyale
 */
@SuppressLint("ViewConstructor")
public class ReactionSpinner extends HorizontalScrollView {

	private static final List<String> allowedReactionsCache = new ArrayList<>();

	private enum ReactionType { COMMENT, ISSUE }
	private enum ReactionAction { REMOVE, ADD }

	private Runnable onInteractedListener;
	private Runnable onLoadingFinishedListener;

	public ReactionSpinner(Context context, Bundle bundle) {

		super(context);

		LinearLayout root = new LinearLayout(context);

		int sidesPadding = AppUtil.getPixelsFromDensity(context, 10);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		root.setOrientation(LinearLayout.HORIZONTAL);
		root.setPadding(sidesPadding, 0, sidesPadding, 0);
		root.setGravity(Gravity.START);
		root.setLayoutParams(layoutParams);

		String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();
		String repoOwner = bundle.getString("repoOwner");
		String repoName = bundle.getString("repoName");

		int id;
		ReactionType reactionType;

		if(bundle.containsKey("commentId")) {
			id = bundle.getInt("commentId");
			reactionType = ReactionType.COMMENT;
		} else {
			id = bundle.getInt("issueId");
			reactionType = ReactionType.ISSUE;
		}

		new Thread(() -> {

			try {

				List<Reaction> allReactions = getReactions(repoOwner, repoName, reactionType, id);
				List<String> allowedReactions = getAllowedReactions();

				if(!allowedReactions.isEmpty()) {
					// Show all allowed reactions
					for(String allowedReaction : allowedReactions) {

						@SuppressLint("InflateParams") CardView reactionButton = (CardView) LayoutInflater.from(context)
							.inflate(R.layout.layout_reaction_button, root, false);

						// Checks if current user reacted with 'allowedReaction'
						boolean myReaction = allReactions.stream().anyMatch(issueReaction ->
							issueReaction.getContent().equals(allowedReaction) &&
								issueReaction.getUser().getLogin().equals(loginUid));

						ReactionAction reactionAction;

						if(myReaction) {
							reactionButton.setCardBackgroundColor(AppUtil.getColorFromAttribute(context, R.attr.inputSelectedColor));
							reactionAction = ReactionAction.REMOVE;
						} else {
							reactionAction = ReactionAction.ADD;
						}

						reactionButton.setOnClickListener(v -> new Thread(() -> {

							try {
								if(react(repoOwner, repoName, reactionType, reactionAction, new EditReactionOption().content(allowedReaction), id)) {
									v.post(() -> {
										IssueDetailActivity.singleIssueUpdate = reactionType == ReactionType.ISSUE;
										((IssueDetailActivity) context).commentEdited = reactionType == ReactionType.COMMENT;
										onInteractedListener.run();
									});
								}
							} catch(IOException ignored) {}

						}).start());

						Emoji emoji = EmojiManager.getForAlias(allowedReaction);

						((TextView) reactionButton.findViewById(R.id.symbol)).setText((emoji == null) ? allowedReaction : emoji.getUnicode());
						root.post(() -> root.addView(reactionButton));

					}

					this.post(() -> {
						setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
						addView(root);
						setVisibility(VISIBLE);
					});

				} else {
					this.post(() -> setVisibility(GONE));
				}
			} catch(IOException ignored) {}
			if(onLoadingFinishedListener != null) {
				((Activity) context).runOnUiThread(() -> onLoadingFinishedListener.run());
			}
		}).start();

	}

	private boolean react(String repoOwner, String repoName, ReactionType reactionType, ReactionAction reactionAction, EditReactionOption issueReaction, int id) throws IOException {

		Response<?> response = null;

		switch(reactionType) {

			case ISSUE:
				switch(reactionAction) {

					case ADD:
						response = RetrofitClient
							.getApiInterface(getContext())
							.issuePostIssueReaction(repoOwner, repoName, (long) id, issueReaction)
							.execute();
						break;


					case REMOVE:
						response = RetrofitClient
							.getApiInterface(getContext())
							.issueDeleteIssueReactionWithBody(repoOwner, repoName, (long) id, issueReaction)
							.execute();
						break;

				}
				break;

			case COMMENT:
				switch(reactionAction) {

					case ADD:
						response = RetrofitClient
							.getApiInterface(getContext())
							.issuePostCommentReaction(repoOwner, repoName, (long) id, issueReaction)
							.execute();
						break;


					case REMOVE:
						response = RetrofitClient
							.getApiInterface(getContext())
							.issueDeleteCommentReactionWithBody(repoOwner, repoName, (long) id, issueReaction)
							.execute();
						break;

				}
				break;

		}

		return response.isSuccessful();

	}

	private List<Reaction> getReactions(String repoOwner, String repoName, ReactionType reactionType, int id) throws IOException {

		Response<List<Reaction>> response = null;

		switch(reactionType) {

			case ISSUE:
				response = RetrofitClient
					.getApiInterface(getContext())
					.issueGetIssueReactions(repoOwner, repoName, (long) id, null, null)
					.execute();
				break;

			case COMMENT:
				response = RetrofitClient
					.getApiInterface(getContext())
					.issueGetCommentReactions(repoOwner, repoName, (long) id)
					.execute();
				break;

		}

		if(response.isSuccessful() && response.body() != null) {
			return response.body();
		} else {
			return Collections.emptyList();
		}
	}

	// Assumes that there's something wrong when no allowed reactions are returned by the server
	private List<String> getAllowedReactions() throws IOException {

		if(allowedReactionsCache.isEmpty()) {

			Response<GeneralUISettings> response = RetrofitClient
				.getApiInterface(getContext())
				.getGeneralUISettings()
				.execute();

			if(response.isSuccessful() && response.body() != null) {
				allowedReactionsCache.addAll(response.body().getAllowedReactions());
			}
		}

		return allowedReactionsCache;

	}

	public void setOnInteractedListener(Runnable onInteractedListener) {
		this.onInteractedListener = onInteractedListener;
	}

	public void setOnLoadingFinishedListener(Runnable onLoadingFinishedListener) {
		this.onLoadingFinishedListener = onLoadingFinishedListener;
	}

}
