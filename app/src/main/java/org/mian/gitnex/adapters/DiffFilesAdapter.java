package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.FileDiffView;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author opyale
 */

public class DiffFilesAdapter extends BaseAdapter {

	private static final Pattern statisticsPattern = Pattern.compile("(\\d+).*?,.*?(\\d+)");

	private final Context context;
	private final List<FileDiffView> fileDiffViews;

	public DiffFilesAdapter(Context context, List<FileDiffView> fileDiffViews) {
		this.context = context;
		this.fileDiffViews = fileDiffViews;
	}

	private static class ViewHolder {
		private final TextView fileName;
		private final TextView fileStatistics;

		public ViewHolder(TextView fileName, TextView fileStatistics) {
			this.fileName = fileName;
			this.fileStatistics = fileStatistics;
		}
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder viewHolder;

		if(convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.list_diff_files, parent, false);

			viewHolder = new ViewHolder(
				convertView.findViewById(R.id.fileName),
				convertView.findViewById(R.id.fileStatistics)
			);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		FileDiffView fileDiffView = fileDiffViews.get(position);

		viewHolder.fileName.setText(fileDiffView.getFileName());

		Matcher matcher = statisticsPattern.matcher(fileDiffView.getFileInfo());

		if(matcher.find() && matcher.groupCount() == 2) {
			viewHolder.fileStatistics.setText(context.getString(R.string.diffStatistics, matcher.group(1), matcher.group(2)));
		} else {
			viewHolder.fileStatistics.setText(fileDiffView.getFileInfo());
		}

		return convertView;

	}

}
