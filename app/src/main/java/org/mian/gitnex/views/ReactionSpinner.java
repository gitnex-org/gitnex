package org.mian.gitnex.views;

import android.annotation.SuppressLint;
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
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.IssueReaction;
import org.mian.gitnex.models.UISettings;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import retrofit2.Response;

/**
 * @author opyale
 */

@SuppressLint("ViewConstructor")
public class ReactionSpinner extends HorizontalScrollView {

	private enum ReactionType { COMMENT, ISSUE }
	private enum ReactionAction { REMOVE, ADD }

	private OnInteractedListener onInteractedListener;

	public ReactionSpinner(Context context, Bundle bundle) {

		super(context);

		LinearLayout root = new LinearLayout(context);

		int dens = AppUtil.getPixelsFromDensity(context, 10);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		root.setOrientation(LinearLayout.HORIZONTAL);
		root.setPadding(dens, 0, dens, 0);
		root.setGravity(Gravity.START);
		root.setLayoutParams(layoutParams);

		TinyDB tinyDB = TinyDB.getInstance(context);

		String loginUid = tinyDB.getString("loginUid");
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

				List<IssueReaction> allReactions = getReactions(repoOwner, repoName, reactionType, id);

				for(String allowedReaction : getAllowedReactions()) {

					@SuppressLint("InflateParams") CardView reactionButton = (CardView) LayoutInflater.from(context)
						.inflate(R.layout.layout_reaction_button, root, false);

					IssueReaction myReaction = null;

					for(IssueReaction issueReaction : allReactions) {

						if(issueReaction.getContent().equals(allowedReaction) && issueReaction.getUser().getLogin().equals(loginUid)) {
							myReaction = issueReaction;
							break;
						}
					}

					ReactionAction reactionAction;

					if(myReaction != null) {

						reactionButton.setCardBackgroundColor(AppUtil.getColorFromAttribute(context, R.attr.inputSelectedColor));
						reactionAction = ReactionAction.REMOVE;
					} else {
						reactionAction = ReactionAction.ADD;
					}

					reactionButton.setOnClickListener(v -> new Thread(() -> {

						try {
							if(react(repoOwner, repoName, reactionType, reactionAction, new IssueReaction(allowedReaction), id)) {
								v.post(() -> onInteractedListener.onInteracted());
							}
						} catch(IOException ignored) {}

					}).start());

					Emoji emoji = EmojiManager.getForAlias(allowedReaction);

					((TextView) reactionButton.findViewById(R.id.symbol)).setText((emoji == null) ? allowedReaction : emoji.getUnicode());
					root.post(() -> root.addView(reactionButton));

				}

			} catch(IOException ignored) {}

		}).start();

		setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		addView(root);

	}

	private boolean react(String repoOwner, String repoName, ReactionType reactionType, ReactionAction reactionAction, IssueReaction issueReaction, int id) throws IOException {

		Response<?> response = null;

		switch(reactionType) {

			case ISSUE:
				switch(reactionAction) {

					case ADD:
						response = RetrofitClient
							.getApiInterface(getContext())
							.setIssueReaction(Authorization.get(getContext()), repoOwner, repoName, id, issueReaction)
							.execute();
						break;


					case REMOVE:
						response = RetrofitClient
							.getApiInterface(getContext())
							.removeIssueReaction(Authorization.get(getContext()), repoOwner, repoName, id, issueReaction)
							.execute();
						break;

				}
				break;

			case COMMENT:
				switch(reactionAction) {

					case ADD:
						response = RetrofitClient
							.getApiInterface(getContext())
							.setIssueCommentReaction(Authorization.get(getContext()), repoOwner, repoName, id, issueReaction)
							.execute();
						break;


					case REMOVE:
						response = RetrofitClient
							.getApiInterface(getContext())
							.removeIssueCommentReaction(Authorization.get(getContext()), repoOwner, repoName, id, issueReaction)
							.execute();
						break;

				}
				break;

		}

		return response.isSuccessful();

	}

	private List<IssueReaction> getReactions(String repoOwner, String repoName, ReactionType reactionType, int id) throws IOException {

		Response<List<IssueReaction>> response = null;

		switch(reactionType) {

			case ISSUE:
				response = RetrofitClient
					.getApiInterface(getContext())
					.getIssueReactions(Authorization.get(getContext()), repoOwner, repoName, id)
					.execute();
				break;

			case COMMENT:
				response = RetrofitClient
					.getApiInterface(getContext())
					.getIssueCommentReactions(Authorization.get(getContext()), repoOwner, repoName, id)
					.execute();
				break;

		}

		if(response.isSuccessful() && response.body() != null)
			return response.body();
		else
			return Collections.emptyList();

	}

	private List<String> getAllowedReactions() throws IOException {

		List<String> allowedReactions = new ArrayList<>();

		Response<UISettings> response = RetrofitClient
			.getApiInterface(getContext())
			.getUISettings(Authorization.get(getContext()))
			.execute();

		if(response.isSuccessful() && response.body() != null) {
			allowedReactions.addAll(Arrays.asList(response.body().getAllowed_reactions()));
		} else {
			allowedReactions.addAll(Arrays.asList("+1", "-1", "laugh", "hooray", "confused", "heart", "rocket", "eyes"));
		}

		return allowedReactions;

	}

	public void setOnInteractedListener(OnInteractedListener onInteractedListener) {
		this.onInteractedListener = onInteractedListener;
	}

	public interface OnInteractedListener { void onInteracted(); }

}
