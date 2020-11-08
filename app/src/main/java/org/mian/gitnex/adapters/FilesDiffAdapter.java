package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.FragmentManager;
import org.mian.gitnex.R;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.models.FileDiffView;
import org.mian.gitnex.views.DiffTextView;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Author opyale
 */

public class FilesDiffAdapter extends BaseAdapter {

	private static Map<Long, View> selectedViews;
	private static final int MAXIMUM_LINES = 5000;

	private static int COLOR_ADDED;
	private static int COLOR_REMOVED;
	private static int COLOR_NORMAL;
	private static int COLOR_SELECTED;
	private static int COLOR_FONT;

	private final Context context;
	private final FragmentManager fragmentManager;
	private final List<FileDiffView> fileDiffViews;

	public FilesDiffAdapter(Context context, FragmentManager fragmentManager, List<FileDiffView> fileDiffViews) {

		this.context = context;
		this.fragmentManager = fragmentManager;
		this.fileDiffViews = fileDiffViews;

		selectedViews = new ConcurrentSkipListMap<>();

		COLOR_ADDED = AppUtil.getColorFromAttribute(context, R.attr.diffAddedColor);
		COLOR_REMOVED = AppUtil.getColorFromAttribute(context, R.attr.diffRemovedColor);
		COLOR_NORMAL = AppUtil.getColorFromAttribute(context, R.attr.primaryBackgroundColor);
		COLOR_SELECTED = AppUtil.getColorFromAttribute(context, R.attr.diffSelectedColor);
		COLOR_FONT = AppUtil.getColorFromAttribute(context, R.attr.inputTextColor);

	}

	@Override
	public int getCount() {

		return fileDiffViews.size();
	}

	@Override
	public Object getItem(int position) {

		return fileDiffViews.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	@SuppressLint({"ViewHolder", "InflateParams"})
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		convertView = LayoutInflater.from(context).inflate(R.layout.list_files_diffs, null, false);

		TextView headerFileName = convertView.findViewById(R.id.headerFileName);
		TextView headerFileInfo = convertView.findViewById(R.id.headerFileInfo);
		ImageView footerImage = convertView.findViewById(R.id.footerImage);
		LinearLayout diffStats = convertView.findViewById(R.id.diff_stats);
		LinearLayout diffLines = convertView.findViewById(R.id.diffLines);

		FileDiffView data = (FileDiffView) getItem(position);
		headerFileName.setText(data.getFileName());

		if(data.isFileBinary()) {

			diffStats.setVisibility(View.GONE);
			diffLines.addView(getMessageView(context.getResources().getString(R.string.binaryFileError)));

		}
		else {

			diffStats.setVisibility(View.VISIBLE);
			headerFileInfo.setText(data.getFileInfo());

			String[] codeLines = getLines(data.toString());

			if(MAXIMUM_LINES > codeLines.length) {

				for(int l=0; l<codeLines.length; l++) {

					if(codeLines[l].length() > 0) {

						int uniquePosition = l + (position * MAXIMUM_LINES);

						DiffTextView diffTextView = new DiffTextView(context);

						diffTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
						diffTextView.setPadding(15, 2, 15, 2);
						diffTextView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf"));
						diffTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
						diffTextView.setPosition(uniquePosition);

						boolean isSelected = false;

						for(View view : selectedViews.values()) {

							if(((DiffTextView) view).getPosition() == uniquePosition) {

								diffTextView.setBackgroundColor(COLOR_SELECTED);
								isSelected = true;
								break;

							}

						}


						if(codeLines[l].startsWith("+")) {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(COLOR_FONT);

							if(!isSelected) {

								diffTextView.setInitialBackgroundColor(COLOR_ADDED);
							}

						}
						else if(codeLines[l].startsWith("-")) {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(COLOR_FONT);

							if(!isSelected) {

								diffTextView.setInitialBackgroundColor(COLOR_REMOVED);
							}

						}
						else {

							diffTextView.setText(codeLines[l]);
							diffTextView.setTextColor(COLOR_FONT);

							if(!isSelected) {

								diffTextView.setInitialBackgroundColor(COLOR_NORMAL);
							}

						}


						diffTextView.setOnClickListener(v -> {

							if(((DiffTextView) v).getCurrentBackgroundColor() != COLOR_SELECTED) {

								selectedViews.put(((DiffTextView) v).getPosition(), v);
								v.setBackgroundColor(COLOR_SELECTED);

							}
							else {

								selectedViews.remove(((DiffTextView) v).getPosition());
								v.setBackgroundColor(((DiffTextView) v).getInitialBackgroundColor());

							}

						});

						diffTextView.setOnLongClickListener(v -> {

							if(((DiffTextView) v).getCurrentBackgroundColor() == COLOR_SELECTED) {

								StringBuilder stringBuilder = new StringBuilder();
								stringBuilder.append("```\n");

								for(View view : selectedViews.values()) {

									stringBuilder.append(((DiffTextView) view).getText());
									stringBuilder.append("\n");

								}

								stringBuilder.append("```\n\n");

								selectedViews.clear();

								Bundle bundle = new Bundle();
								bundle.putString("commentBody", stringBuilder.toString());
								bundle.putBoolean("cursorToEnd", true);

								BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");

							}

							return true;

						});

						diffLines.addView(diffTextView);

					}

				}

			}
			else {

				diffLines.addView(getMessageView(context.getResources().getString(R.string.fileTooLarge)));

			}

		}

		return convertView;

	}

	private TextView getMessageView(String message) {

		TextView textView = new TextView(context);

		textView.setTextColor(COLOR_FONT);
		textView.setBackgroundColor(COLOR_NORMAL);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		textView.setPadding(15, 15, 15, 15);
		textView.setTypeface(Typeface.DEFAULT);
		textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setText(message);

		return textView;

	}

	private String[] getLines(String content) {

		return content.split("\\R");

	}

}
