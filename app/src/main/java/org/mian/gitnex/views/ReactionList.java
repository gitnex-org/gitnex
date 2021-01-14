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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Response;

/**
 * @author opyale
 */

@SuppressLint("ViewConstructor")
public class ReactionList extends HorizontalScrollView {

	private enum ReactionType { COMMENT, ISSUE }
	private OnReactionAddedListener onReactionAddedListener;

	@SuppressLint("SetTextI18n")
	public ReactionList(Context context, Bundle bundle) {

		super(context);

		LinearLayout root = new LinearLayout(context);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		root.setOrientation(LinearLayout.HORIZONTAL);
		root.setGravity(Gravity.START);
		root.setLayoutParams(layoutParams);

		addView(root);
		setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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

				Response<List<IssueReaction>> response = null;

				switch(reactionType) {

					case ISSUE:
						response = RetrofitClient
							.getApiInterface(context)
							.getIssueReactions(Authorization.get(context), repoOwner, repoName, id)
							.execute();
						break;

					case COMMENT:
						response = RetrofitClient
							.getApiInterface(context)
							.getIssueCommentReactions(Authorization.get(context), repoOwner, repoName, id)
							.execute();
						break;

				}

				Map<String, List<IssueReaction>> sortedReactions = new HashMap<>();

				if(response.isSuccessful() && response.body() != null) {

					for(IssueReaction issueReaction : response.body()) {

						if(sortedReactions.containsKey(issueReaction.getContent())) {

							sortedReactions.get(issueReaction.getContent()).add(issueReaction);
						} else {
							List<IssueReaction> issueReactions = new ArrayList<>();
							issueReactions.add(issueReaction);

							sortedReactions.put(issueReaction.getContent(), issueReactions);
						}
					}
				}

				for(String content : sortedReactions.keySet()) {

					List<IssueReaction> issueReactions = sortedReactions.get(content);

					@SuppressLint("InflateParams") CardView reactionBadge = (CardView) LayoutInflater.from(context)
						.inflate(R.layout.layout_reaction_badge, this, false);

					for(IssueReaction issueReaction : issueReactions) {

						if(issueReaction.getUser().getLogin().equals(loginUid)) {
							reactionBadge.setCardBackgroundColor(AppUtil.getColorFromAttribute(context, R.attr.inputSelectedColor));
							break;
						}
					}

					Emoji emoji = EmojiManager.getForAlias(content);

					((TextView) reactionBadge.findViewById(R.id.symbol)).setText(((emoji == null) ? content : emoji.getUnicode()) + " " + issueReactions.size());

					root.post(() -> root.addView(reactionBadge));
					onReactionAddedListener.reactionAdded();

				}

			} catch (IOException ignored) {}

		}).start();

	}

	public void setOnReactionAddedListener(OnReactionAddedListener onReactionAddedListener) {
		this.onReactionAddedListener = onReactionAddedListener;
	}

	public interface OnReactionAddedListener { void reactionAdded(); }

}
