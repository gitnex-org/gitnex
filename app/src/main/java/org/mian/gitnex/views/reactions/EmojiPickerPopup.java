package org.mian.gitnex.views.reactions;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.adapters.EmojiPickerAdapter;
import org.mian.gitnex.databinding.PopupEmojiPickerBinding;

/**
 * @author mmarif
 */
public class EmojiPickerPopup extends PopupWindow {

	private final Context context;
	private final List<String> allowedReactions;
	private OnEmojiSelectedListener listener;

	public interface OnEmojiSelectedListener {
		void onEmojiSelected(String content);
	}

	public EmojiPickerPopup(Context context, List<String> allowedReactions) {
		super(context);
		this.context = context;
		this.allowedReactions = allowedReactions != null ? allowedReactions : new ArrayList<>();
		init();
	}

	private void init() {
		PopupEmojiPickerBinding binding =
				PopupEmojiPickerBinding.inflate(LayoutInflater.from(context));
		setContentView(binding.getRoot());

		int columns = 4;
		int itemCount = allowedReactions.size();
		int rows = (int) Math.ceil((double) itemCount / columns);

		int itemSize = (int) (48 * context.getResources().getDisplayMetrics().density);
		int gridPadding = (int) (8 * context.getResources().getDisplayMetrics().density);

		int width = columns * itemSize + (gridPadding * 2);
		int height = rows * itemSize + (gridPadding * 2);

		setWidth(width);
		setHeight(height);

		setBackgroundDrawable(
				new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
		setOutsideTouchable(true);
		setFocusable(true);

		binding.emojiGrid.setLayoutManager(new GridLayoutManager(context, columns));
		binding.emojiGrid.setPadding(gridPadding, gridPadding, gridPadding, gridPadding);

		EmojiPickerAdapter adapter =
				new EmojiPickerAdapter(context, allowedReactions, new ArrayList<>());
		adapter.setOnEmojiClickListener(
				content -> {
					if (listener != null) {
						listener.onEmojiSelected(content);
					}
					dismiss();
				});
		binding.emojiGrid.setAdapter(adapter);
	}

	public void setOnEmojiSelectedListener(OnEmojiSelectedListener listener) {
		this.listener = listener;
	}

	public void show(View anchor) {
		int[] location = new int[2];
		anchor.getLocationOnScreen(location);

		int margin = (int) (8 * context.getResources().getDisplayMetrics().density);

		int offsetX = -getWidth() + anchor.getWidth() + margin;
		int offsetY = -getHeight() - anchor.getHeight() - margin;

		showAsDropDown(anchor, offsetX, offsetY);
	}
}
