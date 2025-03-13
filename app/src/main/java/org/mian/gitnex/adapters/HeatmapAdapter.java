package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import androidx.core.content.ContextCompat;
import java.util.Arrays;
import org.mian.gitnex.R;

/**
 * @author mmarif
 */
public class HeatmapAdapter extends BaseAdapter {

	private final Context context;
	private final int[] contributions;
	private final int maxContributions;

	public HeatmapAdapter(Context context, int[] contributions) {
		this.context = context;
		this.contributions = contributions;
		this.maxContributions = Arrays.stream(contributions).max().orElse(1);
	}

	@Override
	public int getCount() {
		return contributions.length;
	}

	@Override
	public Object getItem(int position) {
		return contributions[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = new View(context);
			int size =
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									24,
									context.getResources().getDisplayMetrics());
			convertView.setLayoutParams(new ViewGroup.LayoutParams(size, size));
			convertView.setPadding(1, 2, 1, 2);
		}

		int count = contributions[position];
		int color;
		if (count == 0) {
			color = ContextCompat.getColor(context, R.color.heatmap_grey);
		} else {
			float intensity = (float) count / maxContributions;
			if (intensity < 0.25) {
				color = ContextCompat.getColor(context, R.color.heatmap_green_1);
			} else if (intensity < 0.5) {
				color = ContextCompat.getColor(context, R.color.heatmap_green_2);
			} else if (intensity < 0.75) {
				color = ContextCompat.getColor(context, R.color.heatmap_green_3);
			} else {
				color = ContextCompat.getColor(context, R.color.heatmap_green_4);
			}
		}

		GradientDrawable background = new GradientDrawable();
		background.setShape(GradientDrawable.RECTANGLE);
		background.setColor(color);
		float radius =
				TypedValue.applyDimension(
						TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
		background.setCornerRadius(radius);
		convertView.setBackground(background);

		return convertView;
	}
}
