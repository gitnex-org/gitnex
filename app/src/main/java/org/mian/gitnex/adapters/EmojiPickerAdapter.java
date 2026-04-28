package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import java.util.List;
import org.mian.gitnex.databinding.ItemEmojiPickerBinding;
import org.mian.gitnex.helpers.CustomEmojiMapper;

/**
 * @author mmarif
 */
public class EmojiPickerAdapter extends RecyclerView.Adapter<EmojiPickerAdapter.ViewHolder> {

	private final Context context;
	private final List<String> reactions;
	private final List<String> customEmojis;
	private OnEmojiClickListener clickListener;

	public interface OnEmojiClickListener {
		void onEmojiClick(String content);
	}

	public EmojiPickerAdapter(Context context, List<String> reactions, List<String> customEmojis) {
		this.context = context;
		this.reactions = reactions;
		this.customEmojis = customEmojis;
	}

	public void setOnEmojiClickListener(OnEmojiClickListener listener) {
		this.clickListener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemEmojiPickerBinding binding =
				ItemEmojiPickerBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String content = reactions.get(position);

		if (customEmojis.contains(content)) {
			String emoji = CustomEmojiMapper.getEmojiForCustom(content);
			holder.binding.emojiText.setText(emoji);
			holder.binding.emojiLabel.setText(":" + content + ":");
			holder.binding.emojiLabel.setVisibility(View.VISIBLE);
		} else {
			Emoji emoji = EmojiManager.getForAlias(content);
			holder.binding.emojiText.setText(emoji != null ? emoji.getUnicode() : content);
			holder.binding.emojiLabel.setVisibility(View.GONE);
		}

		holder.binding
				.getRoot()
				.setOnClickListener(
						v -> {
							if (clickListener != null) {
								clickListener.onEmojiClick(content);
							}
						});
	}

	@Override
	public int getItemCount() {
		return reactions.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ItemEmojiPickerBinding binding;

		ViewHolder(@NonNull ItemEmojiPickerBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
