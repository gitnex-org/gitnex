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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import org.gitnex.tea4j.v2.models.Reaction;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ReactionAuthorsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import retrofit2.Response;

/**
 * @author opyale
 */

@SuppressLint("ViewConstructor")
public class ReactionList extends HorizontalScrollView {

	private Runnable onReactionAddedListener;

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

		String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();
		String repoOwner = bundle.getString("repoOwner");
		String repoName = bundle.getString("repoName");

		int id;
		ReactionType reactionType;

		if(bundle.containsKey("commentId")) {
			id = bundle.getInt("commentId");
			reactionType = ReactionType.COMMENT;
		}
		else {
			id = bundle.getInt("issueId");
			reactionType = ReactionType.ISSUE;
		}

		new Thread(() -> {

			try {

				Response<List<Reaction>> response = null;

				switch(reactionType) {

					case ISSUE:
						response = RetrofitClient.getApiInterface(context).issueGetIssueReactions(repoOwner, repoName, (long) id, null, null).execute();
						break;

					case COMMENT:
						response = RetrofitClient.getApiInterface(context).issueGetCommentReactions(repoOwner, repoName, (long) id).execute();
						break;
				}

				if(response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {

					Map<String, List<Reaction>> sortedReactions = new HashMap<>();

					for(Reaction issueReaction : response.body()) {

						if(sortedReactions.containsKey(issueReaction.getContent())) {
							sortedReactions.get(issueReaction.getContent()).add(issueReaction);
						}
						else {
							List<Reaction> issueReactions = new ArrayList<>();
							issueReactions.add(issueReaction);

							sortedReactions.put(issueReaction.getContent(), issueReactions);
						}
					}

					for(String content : sortedReactions.keySet()) {

						List<Reaction> issueReactions = sortedReactions.get(content);

						@SuppressLint("InflateParams") MaterialCardView reactionBadge = (MaterialCardView) LayoutInflater.from(context).inflate(R.layout.layout_reaction_badge, this, false);

						for(Reaction issueReaction : issueReactions) {
							if(issueReaction.getUser().getLogin().equals(loginUid)) {
								reactionBadge.setCardBackgroundColor(AppUtil.getColorFromAttribute(context, R.attr.inputSelectedColor));
								break;
							}
						}

						Emoji emoji = EmojiManager.getForAlias(content);

						((TextView) reactionBadge.findViewById(R.id.symbol)).setText(((emoji == null) ? content : emoji.getUnicode()) + " " + issueReactions.size());

						reactionBadge.setOnClickListener(v -> {

							List<User> userData = issueReactions.stream().map(Reaction::getUser).collect(Collectors.toList());

							ReactionAuthorsAdapter adapter = new ReactionAuthorsAdapter(context, userData);

							int paddingTop = AppUtil.getPixelsFromDensity(context, 10);

							RecyclerView recyclerView = new RecyclerView(context);
							recyclerView.setPadding(0, paddingTop, 0, 0);
							recyclerView.setLayoutManager(new LinearLayoutManager(context));
							recyclerView.setAdapter(adapter);

							assert emoji != null;
							MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context).setTitle(emoji.getUnicode()).setView(recyclerView).setNeutralButton(R.string.close, null);

							materialAlertDialogBuilder.create().show();

						});

						root.post(() -> root.addView(reactionBadge));
						onReactionAddedListener.run();

					}
				}

			}
			catch(IOException ignored) {
			}

		}).start();

	}

	public void setOnReactionAddedListener(Runnable onReactionAddedListener) {
		this.onReactionAddedListener = onReactionAddedListener;
	}

	private enum ReactionType {COMMENT, ISSUE}

}
