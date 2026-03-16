package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author opyale
 * @author mmarif
 */
public class DiffAdapter extends RecyclerView.Adapter<DiffAdapter.ViewHolder> {

	private final List<String> lines;
	private final int colorAdded, colorRemoved, colorNormal;
	private final Typeface typeface;

	public DiffAdapter(Context context, List<String> lines) {
		this.lines = lines;
		this.colorAdded = AppUtil.getColorFromAttribute(context, R.attr.diffAddedColor);
		this.colorRemoved = AppUtil.getColorFromAttribute(context, R.attr.diffRemovedColor);
		this.colorNormal = AppUtil.getColorFromAttribute(context, R.attr.primaryBackgroundColor);
		this.typeface =
				Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf");
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		TextView view =
				(TextView)
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.list_item_diff_line, parent, false);
		view.setTypeface(typeface);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String line = lines.get(position);
		holder.textView.setText(line);

		int bgColor = colorNormal;
		if (line != null && !line.isEmpty()) {
			char firstChar = line.charAt(0);
			if (firstChar == '+') {
				bgColor = colorAdded;
			} else if (firstChar == '-') {
				bgColor = colorRemoved;
			}
		}
		holder.textView.setBackgroundColor(bgColor);
	}

	@Override
	public int getItemCount() {
		return lines.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		public ViewHolder(TextView itemView) {
			super(itemView);
			this.textView = itemView;
		}
	}
}
