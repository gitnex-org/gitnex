package org.mian.gitnex.views.reactions;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.core.content.ContextCompat;
import com.google.android.material.chip.Chip;
import org.mian.gitnex.R;

/**
 * @author mmarif
 */
public class ReactionChip extends Chip {

	private int count;
	private boolean isUserReaction;

	public ReactionChip(Context context) {
		super(context);
		init();
	}

	public ReactionChip(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ReactionChip(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		setCheckable(false);
		setChipIconVisible(false);
		setTextStartPadding(8);
		setTextEndPadding(8);

		TypedValue typedValue = new TypedValue();
		getContext()
				.getTheme()
				.resolveAttribute(
						com.google.android.material.R.attr.textAppearanceLabelMedium,
						typedValue,
						true);
		setTextAppearance(typedValue.resourceId);
	}

	public int getCount() {
		return count;
	}

	public boolean isUserReaction() {
		return isUserReaction;
	}

	public void setReaction(String content, int count, boolean userReacted) {
		this.count = count;
		this.isUserReaction = userReacted;

		updateBackground(userReacted);
	}

	private void updateBackground(boolean userReacted) {
		int color;
		if (userReacted) {
			color = ContextCompat.getColor(getContext(), R.color.reaction_selected_overlay);
		} else {
			TypedValue typedValue = new TypedValue();
			getContext()
					.getTheme()
					.resolveAttribute(
							com.google.android.material.R.attr.colorSurfaceContainerHigh,
							typedValue,
							true);
			color = typedValue.data;
		}
		setChipBackgroundColor(ColorStateList.valueOf(color));
	}
}
