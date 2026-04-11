package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.databinding.ListDiffFilesBinding;
import org.mian.gitnex.helpers.FileDiffView;
import org.mian.gitnex.helpers.contexts.IssueContext;

/**
 * @author mmarif
 */
public class DiffFilesAdapter extends RecyclerView.Adapter<DiffFilesAdapter.FilesHolder> {

	private static final Pattern statisticsPattern = Pattern.compile("(\\d+).*?,.*?(\\d+)");

	private final Context context;
	private final IssueContext issue;
	private final String fragmentType;
	private List<FileDiffView> fileDiffViews;
	private final String repoOwner;
	private final String repoName;
	private final String sha;

	public DiffFilesAdapter(
			Context context,
			List<FileDiffView> fileDiffViews,
			IssueContext issue,
			String repoOwner,
			String repoName,
			String sha,
			String fragmentType) {
		this.context = context;
		this.fileDiffViews = fileDiffViews;
		this.issue = issue;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.sha = sha;
		this.fragmentType = fragmentType;
	}

	@NonNull @Override
	public FilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new FilesHolder(
				ListDiffFilesBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull FilesHolder holder, int position) {
		holder.bind(fileDiffViews.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return fileDiffViews != null ? fileDiffViews.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<FileDiffView> list) {
		this.fileDiffViews = list;
		notifyDataSetChanged();
	}

	public class FilesHolder extends RecyclerView.ViewHolder {

		private final ListDiffFilesBinding binding;
		private FileDiffView currentItem;

		FilesHolder(ListDiffFilesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.mainFrame.setOnClickListener(
					v -> {
						if (currentItem == null) return;

						Intent intent = new Intent(context, DiffActivity.class);
						intent.putExtra("owner", repoOwner);
						intent.putExtra("repo", repoName);
						intent.putExtra("sha", sha);

						if (issue != null) {
							intent.putExtra("pr_id", issue.getIssueIndex());
						}

						intent.putExtra("type", fragmentType);
						intent.putExtra("file_path", currentItem.getFileName());
						context.startActivity(intent);
					});
		}

		void bind(FileDiffView fileDiffView) {
			this.currentItem = fileDiffView;
			binding.fileName.setText(fileDiffView.getFileName());

			Matcher matcher = statisticsPattern.matcher(fileDiffView.getFileInfo());
			if (matcher.find() && matcher.groupCount() == 2) {
				binding.fileStatistics.setText(
						context.getString(
								R.string.diffStatistics, matcher.group(1), matcher.group(2)));
			} else {
				binding.fileStatistics.setText(fileDiffView.getFileInfo());
			}
		}
	}
}
