package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.mian.gitnex.R;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.IssueContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author opyale
 */

public class DiffAdapter extends BaseAdapter {

	private final Context context;
	private final FragmentManager fragmentManager;
	private final List<String> lines;
	private final IssueContext issue;

	private final List<Integer> selectedLines;
	private final Typeface typeface;
	private final String type;

	private static int COLOR_ADDED;
	private static int COLOR_REMOVED;
	private static int COLOR_NORMAL;
	private static int COLOR_SELECTED;
	private static int COLOR_FONT;

	public DiffAdapter(Context context, FragmentManager fragmentManager, List<String> lines, IssueContext issue, String type) {

		this.context = context;
		this.fragmentManager = fragmentManager;
		this.lines = lines;
		this.issue = issue;
		this.type = type;

		selectedLines = new ArrayList<>();
		typeface = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf");

		COLOR_ADDED = AppUtil.getColorFromAttribute(context, R.attr.diffAddedColor);
		COLOR_REMOVED = AppUtil.getColorFromAttribute(context, R.attr.diffRemovedColor);
		COLOR_NORMAL = AppUtil.getColorFromAttribute(context, R.attr.primaryBackgroundColor);
		COLOR_SELECTED = AppUtil.getColorFromAttribute(context, R.attr.diffSelectedColor);
		COLOR_FONT = AppUtil.getColorFromAttribute(context, R.attr.inputTextColor);

	}

	@Override
	public int getCount() {
		return lines.size();
	}

	@Override
	public Object getItem(int position) {
		return lines.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView == null) {

			TextView textView = new TextView(context);

			textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			textView.setTextColor(COLOR_FONT);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			textView.setPadding(32, 0, 32, 0);
			textView.setTypeface(typeface);

			convertView = textView;

		}

		if(type.equals("pull")) {
			convertView.setOnClickListener(v -> {

				if(selectedLines.contains(position)) {

					selectedLines.remove((Object) position);
					v.setBackgroundColor(getLineColor(lines.get(position)));
				}
				else {

					selectedLines.add(position);
					v.setBackgroundColor(COLOR_SELECTED);
				}
			});

			convertView.setOnLongClickListener(v -> {

				if(selectedLines.contains(position)) {

					StringBuilder stringBuilder = new StringBuilder();
					stringBuilder.append("```\n");

					for(Integer selectedLine : selectedLines.stream().sorted().collect(Collectors.toList())) {
						stringBuilder.append(lines.get(selectedLine));
						stringBuilder.append("\n");
					}

					stringBuilder.append("```\n\n");
					selectedLines.clear();

					Bundle bundle = new Bundle();
					bundle.putString("commentBody", stringBuilder.toString());
					bundle.putBoolean("cursorToEnd", true);

				BottomSheetReplyFragment.newInstance(bundle, issue).show(fragmentManager, "replyBottomSheet");
			}

				return true;

			});
		}

		String line = lines.get(position);

		int backgroundColor = selectedLines.contains(position) ? COLOR_SELECTED : getLineColor(line);

		convertView.setBackgroundColor(backgroundColor);
		((TextView) convertView).setText(line);

		return convertView;

	}

	private int getLineColor(String line) {

		if(line.length() == 0) {
			return COLOR_NORMAL;
		}

		switch(line.charAt(0)) {
			case '+': return COLOR_ADDED;
			case '-': return COLOR_REMOVED;

			default: return COLOR_NORMAL;
		}
	}

}
