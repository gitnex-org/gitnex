package org.mian.gitnex.views.reactions;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import com.google.android.material.chip.ChipGroup;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.gitnex.tea4j.v2.models.Reaction;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.helpers.CustomEmojiMapper;

/**
 * @author mmarif
 */
public class ReactionsManager {

	private final Context context;
	private final ChipGroup reactionsContainer;
	private final View addReactionButton;
	private final ReactionListener listener;
	private final String currentUser;
	private final Map<String, ReactionChip> reactionChips = new HashMap<>();

	private List<String> allowedReactions = new ArrayList<>();
	private List<String> customEmojis = new ArrayList<>();

	public interface ReactionListener {
		void onAddReaction(String content);

		void onRemoveReaction(String content);

		void onShowUsers(String emoji, String content, List<User> users);

		void onReactionsLoaded();
	}

	public ReactionsManager(
			Context context,
			ChipGroup container,
			View addButton,
			ReactionListener listener,
			String currentUser) {
		this.context = context;
		this.reactionsContainer = container;
		this.addReactionButton = addButton;
		this.listener = listener;
		this.currentUser = currentUser;
	}

	public void setReactionSettings(List<String> allowedReactions, List<String> customEmojis) {
		this.allowedReactions = allowedReactions != null ? allowedReactions : new ArrayList<>();
		this.customEmojis = customEmojis != null ? customEmojis : new ArrayList<>();
		setupAddButton();
	}

	public void setReactions(List<Reaction> reactions) {
		reactionsContainer.removeAllViews();
		reactionChips.clear();

		if (reactions == null || reactions.isEmpty()) {
			addReactionButton.setVisibility(View.VISIBLE);
			return;
		}

		Map<String, List<Reaction>> grouped = groupReactionsByContent(reactions);

		for (Map.Entry<String, List<Reaction>> entry : grouped.entrySet()) {
			String content = entry.getKey();
			List<Reaction> contentReactions = entry.getValue();

			boolean userReacted =
					contentReactions.stream()
							.anyMatch(r -> r.getUser().getLogin().equals(currentUser));

			ReactionChip chip = createReactionChip(content, contentReactions.size(), userReacted);
			reactionChips.put(content, chip);
			reactionsContainer.addView(chip);

			chip.setOnClickListener(
					v -> {
						boolean currentlyUserReacted = chip.isUserReaction();
						if (currentlyUserReacted) {
							listener.onRemoveReaction(content);
						} else {
							listener.onAddReaction(content);
						}
					});

			chip.setOnLongClickListener(
					v -> {
						List<User> users =
								contentReactions.stream()
										.map(Reaction::getUser)
										.collect(Collectors.toList());
						String emoji = getDisplayEmoji(content);
						listener.onShowUsers(emoji, content, users);
						return true;
					});
		}

		addReactionButton.setVisibility(View.VISIBLE);
	}

	public String getDisplayEmoji(String content) {
		if (CustomEmojiMapper.isCustomEmoji(content, customEmojis)) {
			return CustomEmojiMapper.getEmojiForCustom(content);
		} else {
			Emoji emoji = EmojiManager.getForAlias(content);
			return emoji != null ? emoji.getUnicode() : content;
		}
	}

	private Map<String, List<Reaction>> groupReactionsByContent(List<Reaction> reactions) {
		Map<String, List<Reaction>> grouped = new HashMap<>();
		for (Reaction reaction : reactions) {
			String content = reaction.getContent();
			grouped.computeIfAbsent(content, k -> new ArrayList<>()).add(reaction);
		}
		return grouped;
	}

	@SuppressLint("SetTextI18n")
	private ReactionChip createReactionChip(String content, int count, boolean userReacted) {
		ReactionChip chip = new ReactionChip(context);
		String displayEmoji = getDisplayEmoji(content);
		chip.setText(displayEmoji + " " + count);
		chip.setTextSize(16);
		chip.setReaction(content, count, userReacted);
		return chip;
	}

	private void setupAddButton() {
		addReactionButton.setOnClickListener(
				v -> {
					EmojiPickerPopup popup = new EmojiPickerPopup(context, allowedReactions);
					popup.setOnEmojiSelectedListener(
							content -> {
								ReactionChip existingChip = reactionChips.get(content);
								if (existingChip != null) {
									if (existingChip.isUserReaction()) {
										listener.onRemoveReaction(content);
									} else {
										listener.onAddReaction(content);
									}
								} else {
									listener.onAddReaction(content);
								}
							});
					popup.show(v);
				});
	}
}
