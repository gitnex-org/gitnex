package org.mian.gitnex.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * @author mmarif
 */
public class LabelStylingHelper {

	private static LabelStylingHelper instance;
	private final float density;

	private LabelStylingHelper(Context context) {
		this.density = context.getResources().getDisplayMetrics().density;
	}

	public static synchronized LabelStylingHelper getInstance(Context context) {
		if (instance == null) {
			instance = new LabelStylingHelper(context.getApplicationContext());
		}
		return instance;
	}

	public static boolean isScopedLabel(String labelText, boolean exclusive) {
		return !TextUtils.isEmpty(labelText) && labelText.contains("/") && exclusive;
	}

	public void styleScopedLabel(
			String labelText,
			String colorHex,
			String textColorHex,
			TextView keyView,
			TextView valueView,
			int textSizeDp,
			int height,
			int width) {

		String[] parts = labelText.split("/", 2);
		if (parts.length != 2) return;

		String key = parts[0].trim();
		String value = parts[1].trim();
		if (key.isEmpty() || value.isEmpty()) return;

		keyView.setText(key);
		valueView.setText(value);
		valueView.setVisibility(View.VISIBLE);

		try {
			int baseColor = Color.parseColor(repeatString(colorHex, 4, 1, 2));
			int textColor = Color.parseColor(repeatString(textColorHex, 4, 1, 2));

			int valueBgColor =
					Color.argb(
							255,
							Math.min(255, Color.red(baseColor) + 25),
							Math.min(255, Color.green(baseColor) + 25),
							Math.min(255, Color.blue(baseColor) + 25));

			int valueTextColor =
					(Color.red(valueBgColor) * 0.299
											+ Color.green(valueBgColor) * 0.587
											+ Color.blue(valueBgColor) * 0.114)
									> 186
							? Color.BLACK
							: Color.WHITE;

			int textSizePx = dpToPx(textSizeDp);
			keyView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);
			valueView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);

			float radius = dpToPx(18);
			GradientDrawable keyBg = createLeftRoundedBackground(baseColor, radius);
			GradientDrawable valueBg = createRightRoundedBackground(valueBgColor, radius);

			keyView.setBackground(keyBg);
			valueView.setBackground(valueBg);
			keyView.setTextColor(textColor);
			valueView.setTextColor(valueTextColor);

			int hPadding = dpToPx(width);
			int vPadding = dpToPx(height);
			keyView.setPadding(hPadding, vPadding, hPadding, vPadding);
			valueView.setPadding(hPadding, vPadding, hPadding, vPadding);

			removeMargins(keyView);
			removeMargins(valueView);
		} catch (Exception e) {
			keyView.setTextColor(Color.BLACK);
			valueView.setTextColor(Color.BLACK);
			keyView.setBackgroundColor(Color.LTGRAY);
			valueView.setBackgroundColor(Color.LTGRAY);
		}
	}

	public void styleRegularLabel(
			String labelText,
			String colorHex,
			String textColorHex,
			TextView labelView,
			int textSizeDp,
			int height,
			int width) {

		labelView.setText(labelText);

		try {
			int baseColor = Color.parseColor(repeatString(colorHex, 4, 1, 2));
			int textColor = Color.parseColor(repeatString(textColorHex, 4, 1, 2));

			int textSizePx = dpToPx(textSizeDp);
			labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizePx);

			GradientDrawable bg = new GradientDrawable();
			bg.setColor(baseColor);
			bg.setCornerRadius(dpToPx(18));

			labelView.setBackground(bg);
			labelView.setTextColor(textColor);

			int hPadding = dpToPx(width);
			int vPadding = dpToPx(height);
			labelView.setPadding(hPadding, vPadding, hPadding, vPadding);
		} catch (Exception e) {
			labelView.setTextColor(Color.BLACK);
			labelView.setBackgroundColor(Color.LTGRAY);
		}
	}

	private GradientDrawable createLeftRoundedBackground(int color, float radius) {
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(color);
		bg.setCornerRadii(new float[] {radius, radius, 0, 0, 0, 0, radius, radius});
		return bg;
	}

	private GradientDrawable createRightRoundedBackground(int color, float radius) {
		GradientDrawable bg = new GradientDrawable();
		bg.setColor(color);
		bg.setCornerRadii(new float[] {0, 0, radius, radius, radius, radius, 0, 0});
		return bg;
	}

	private void removeMargins(TextView textView) {
		ViewGroup.LayoutParams params = textView.getLayoutParams();
		if (params instanceof ViewGroup.MarginLayoutParams marginParams) {
			marginParams.setMargins(0, 0, 0, 0);
			textView.setLayoutParams(marginParams);
		}
	}

	private int dpToPx(float dp) {
		return (int) (dp * density);
	}

	private String repeatString(String str, int length, int index, int count) {

		String colorString = str;
		if (colorString.length() == length) {
			String sub = colorString.substring(index);
			colorString = "#" + sub.repeat(count);
		}
		return colorString;
	}
}
