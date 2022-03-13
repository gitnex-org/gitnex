package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.MilestoneActions;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class MilestonesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<Milestones> dataList;
	private Runnable loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;
	private final RepositoryContext repository;

	public MilestonesAdapter(Context ctx, List<Milestones> dataListMain, RepositoryContext repository) {
		this.repository = repository;
		this.context = ctx;
		this.dataList = dataListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new MilestonesAdapter.DataHolder(inflater.inflate(R.layout.list_milestones, parent, false));
		}
		else {
			return new MilestonesAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

			isLoading = true;
			loadMoreListener.run();
		}

		if(getItemViewType(position) == TYPE_LOAD) {

			((MilestonesAdapter.DataHolder) holder).bindData(dataList.get(position));
		}
	}

	class DataHolder extends RecyclerView.ViewHolder {

		private Milestones milestones;

		private final TextView msTitle;
		private final TextView msDescription;
		private final TextView msOpenIssues;
		private final TextView msClosedIssues;
		private final TextView msDueDate;
		private final ProgressBar msProgress;

		DataHolder(View itemView) {

			super(itemView);

			msTitle = itemView.findViewById(R.id.milestoneTitle);
			msDescription = itemView.findViewById(R.id.milestoneDescription);
			msOpenIssues = itemView.findViewById(R.id.milestoneIssuesOpen);
			msClosedIssues = itemView.findViewById(R.id.milestoneIssuesClosed);
			msDueDate = itemView.findViewById(R.id.milestoneDueDate);
			msProgress = itemView.findViewById(R.id.milestoneProgress);
			ImageView milestonesMenu = itemView.findViewById(R.id.milestonesMenu);

			if(!((RepoDetailActivity) itemView.getContext()).repository.getPermissions().canPush()) {
				milestonesMenu.setVisibility(View.GONE);
			}
			milestonesMenu.setOnClickListener(v -> {

				Context ctx = v.getContext();
				int milestoneId_ = Integer.parseInt(String.valueOf(milestones.getId()));

				@SuppressLint("InflateParams") View view = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_milestones_in_list, null);

				TextView closeMilestone = view.findViewById(R.id.closeMilestone);
				TextView openMilestone = view.findViewById(R.id.openMilestone);

				BottomSheetDialog dialog = new BottomSheetDialog(ctx);
				dialog.setContentView(view);
				dialog.show();

				if(milestones.getState().equals("open")) {

					closeMilestone.setVisibility(View.VISIBLE);
					openMilestone.setVisibility(View.GONE);
				}
				else {

					closeMilestone.setVisibility(View.GONE);
					openMilestone.setVisibility(View.VISIBLE);
				}

				closeMilestone.setOnClickListener(v12 -> {

					MilestoneActions.closeMilestone(ctx, milestoneId_, repository);
					dialog.dismiss();
					updateAdapter(getAdapterPosition());
				});

				openMilestone.setOnClickListener(v12 -> {

					MilestoneActions.openMilestone(ctx, milestoneId_, repository);
					dialog.dismiss();
					updateAdapter(getAdapterPosition());
				});

			});

		}

		@SuppressLint("SetTextI18n")
		void bindData(Milestones dataModel) {

			this.milestones = dataModel;
			final TinyDB tinyDb = TinyDB.getInstance(context);
			final String locale = context.getResources().getConfiguration().locale.getLanguage();
			final String timeFormat = tinyDb.getString("dateFormat", "pretty");

			Markdown.render(context, dataModel.getTitle(), msTitle);

			if(!dataModel.getDescription().equals("")) {

				Markdown.render(context, EmojiParser.parseToUnicode(dataModel.getDescription()), msDescription);
			}
			else {

				msDescription.setText(context.getString(R.string.milestoneNoDescription));
			}

			msOpenIssues.setText(context.getString(R.string.milestoneIssueStatusOpen, dataModel.getOpen_issues()));
			msClosedIssues.setText(context.getString(R.string.milestoneIssueStatusClosed, dataModel.getClosed_issues()));

			if((dataModel.getOpen_issues() + dataModel.getClosed_issues()) > 0) {

				if(dataModel.getOpen_issues() == 0) {

					msProgress.setProgress(100);
					msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, 100), context));
				}
				else {

					int msCompletion = 100 * dataModel.getClosed_issues() / (dataModel.getOpen_issues() + dataModel.getClosed_issues());
					msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, msCompletion), context));
					msProgress.setProgress(msCompletion);
				}

			}
			else {

				msProgress.setProgress(0);
				msProgress.setOnClickListener(new ClickListener(context.getResources().getString(R.string.milestoneCompletion, 0), context));
			}

			if(dataModel.getDue_on() != null) {

				String TAG = Constants.tagMilestonesAdapter;
				if(timeFormat.equals("normal") || timeFormat.equals("pretty")) {

					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", new Locale(locale));
					Date date = null;

					try {
						date = formatter.parse(dataModel.getDue_on());
					}
					catch(ParseException e) {
						Log.e(TAG, e.toString());
					}

					assert date != null;
					String dueDate = formatter.format(date);

					if(date.before(new Date())) {
						msDueDate.setTextColor(ResourcesCompat.getColor(context.getResources(), R.color.darkRed, null));
					}

					msDueDate.setText(dueDate);
					msDueDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToast(dataModel.getDue_on()), context));
				}
				else if(timeFormat.equals("normal1")) {

					SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", new Locale(locale));

					Date date1 = null;

					try {
						date1 = formatter.parse(dataModel.getDue_on());
					}
					catch(ParseException e) {
						Log.e(TAG, e.toString());
					}

					assert date1 != null;
					String dueDate = formatter.format(date1);
					msDueDate.setText(dueDate);
				}

			}
			else {

				msDueDate.setText(context.getString(R.string.milestoneNoDueDate));
			}

		}

	}

	private void updateAdapter(int position) {

		dataList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, dataList.size());
	}

	@Override
	public int getItemViewType(int position) {

		if(dataList.get(position).getTitle() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {

		return dataList.size();
	}

	static class LoadHolder extends RecyclerView.ViewHolder {

		LoadHolder(View itemView) {

			super(itemView);
		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {

		isMoreDataAvailable = moreDataAvailable;
	}

	public void notifyDataChanged() {

		notifyDataSetChanged();
		isLoading = false;
	}

	public void setLoadMoreListener(Runnable loadMoreListener) {

		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Milestones> list) {

		dataList = list;
		notifyDataSetChanged();
	}

}
