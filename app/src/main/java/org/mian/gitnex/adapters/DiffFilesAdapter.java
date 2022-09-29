package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.fragments.DiffFragment;
import org.mian.gitnex.helpers.FileDiffView;
import org.mian.gitnex.helpers.contexts.IssueContext;

/**
 * @author M M Arif
 */
public class DiffFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final Pattern statisticsPattern = Pattern.compile("(\\d+).*?,.*?(\\d+)");

	private final Context context;
	private final IssueContext issue;
	private final String fragmentType;
	private List<FileDiffView> fileDiffViews;

	public DiffFilesAdapter(
			Context context,
			List<FileDiffView> fileDiffViews,
			IssueContext issue,
			String fragmentType) {

		this.context = context;
		this.fileDiffViews = fileDiffViews;
		this.issue = issue;
		this.fragmentType = fragmentType;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new DiffFilesAdapter.FilesHolder(
				inflater.inflate(R.layout.list_diff_files, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		((DiffFilesAdapter.FilesHolder) holder).bindData(fileDiffViews.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return fileDiffViews.size();
	}

	public void updateList(List<FileDiffView> list) {
		fileDiffViews = list;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	class FilesHolder extends RecyclerView.ViewHolder {

		FileDiffView diffFilesObject;
		TextView fileName;
		TextView fileStatistics;
		LinearLayout main_frame;

		FilesHolder(View itemView) {

			super(itemView);

			fileName = itemView.findViewById(R.id.fileName);
			fileStatistics = itemView.findViewById(R.id.fileStatistics);
			main_frame = itemView.findViewById(R.id.main_frame);

			main_frame.setOnClickListener(
					v -> {
						if (fragmentType.equalsIgnoreCase("commit")) {
							((CommitDetailActivity) context)
									.getSupportFragmentManager()
									.beginTransaction()
									.replace(
											R.id.fragment_container,
											DiffFragment.newInstance(diffFilesObject, fragmentType))
									.commit();
						} else {
							((DiffActivity) context)
									.getSupportFragmentManager()
									.beginTransaction()
									.replace(
											R.id.fragment_container,
											DiffFragment.newInstance(diffFilesObject, issue))
									.commit();
						}
					});
		}

		void bindData(FileDiffView fileDiffView) {

			this.diffFilesObject = fileDiffView;
			fileName.setText(fileDiffView.getFileName());

			Matcher matcher = statisticsPattern.matcher(fileDiffView.getFileInfo());

			if (matcher.find() && matcher.groupCount() == 2) {
				fileStatistics.setText(
						context.getString(
								R.string.diffStatistics, matcher.group(1), matcher.group(2)));
			} else {
				fileStatistics.setText(fileDiffView.getFileInfo());
			}
		}
	}
}
