package org.mian.gitnex.helpers;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mmarif
 */
public class CustomEmojiMapper {

	private static final Map<String, String> CUSTOM_EMOJI_MAP = new HashMap<>();

	static {
		CUSTOM_EMOJI_MAP.put("git", "📦");
		CUSTOM_EMOJI_MAP.put("gitea", "🍵");
		CUSTOM_EMOJI_MAP.put("codeberg", "🏔️");
		CUSTOM_EMOJI_MAP.put("gitlab", "🦊");
		CUSTOM_EMOJI_MAP.put("github", "🐙");
		CUSTOM_EMOJI_MAP.put("forgejo", "🔨");
	}

	private CustomEmojiMapper() {}

	public static String getEmojiForCustom(String name) {
		if (name == null || name.isEmpty()) {
			return "🖼️";
		}
		return CUSTOM_EMOJI_MAP.getOrDefault(name, "🖼️");
	}

	public static boolean isCustomEmoji(String content, java.util.List<String> customEmojis) {
		return customEmojis != null && customEmojis.contains(content);
	}
}
