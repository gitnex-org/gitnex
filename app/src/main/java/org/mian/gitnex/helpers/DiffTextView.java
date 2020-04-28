package org.mian.gitnex.helpers;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Author opyale
 */

public class DiffTextView extends androidx.appcompat.widget.AppCompatTextView {

	private int initialBackgroundColor;
	private int currentBackgroundColor;
	private long position;

	public DiffTextView(Context context) {

		super(context);
	}

	public DiffTextView(Context context, AttributeSet attrs) {

		super(context, attrs);
	}

	public DiffTextView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setBackgroundColor(int color) {

		currentBackgroundColor = color;
		super.setBackgroundColor(color);
	}

	public void setInitialBackgroundColor(int initialBackgroundColor) {

		setBackgroundColor(initialBackgroundColor);
		this.initialBackgroundColor = initialBackgroundColor;
	}

	public int getInitialBackgroundColor() {

		return initialBackgroundColor;
	}

	public int getCurrentBackgroundColor() {

		return currentBackgroundColor;
	}

	public long getPosition() {

		return position;
	}

	public void setPosition(int position) {

		this.position = position;
	}

}
