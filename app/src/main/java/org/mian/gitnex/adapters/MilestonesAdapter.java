package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListMilestonesBinding;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class MilestonesAdapter extends RecyclerView.Adapter<MilestonesAdapter.DataHolder> {

	private final Context context;
	private List<Milestone> milestonesList;
	private final boolean canEdit;
	private final OnMilestoneAction onMenuClick;

	public interface OnMilestoneAction {
		void run(Milestone milestone);
	}

	public MilestonesAdapter(
			Context ctx, List<Milestone> list, boolean canEdit, OnMilestoneAction onMenuClick) {
		this.context = ctx;
		this.milestonesList = list;
		this.canEdit = canEdit;
		this.onMenuClick = onMenuClick;
	}

	@NonNull @Override
	public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new DataHolder(
				ListMilestonesBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull DataHolder holder, int position) {
		holder.bindData(milestonesList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return milestonesList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Milestone> newList) {
		this.milestonesList = newList;
		notifyDataSetChanged();
	}

	public class DataHolder extends RecyclerView.ViewHolder {
		private final ListMilestonesBinding binding;
		private Milestone currentMilestone;

		DataHolder(ListMilestonesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.itemMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);
			binding.itemMenu.setOnClickListener(
					v -> {
						if (onMenuClick != null) {
							onMenuClick.run(currentMilestone);
						}
					});
		}

		@SuppressLint("SetTextI18n")
		void bindData(Milestone milestone) {
			this.currentMilestone = milestone;
			Locale locale = Locale.getDefault();

			Markdown.render(context, milestone.getTitle(), binding.milestoneTitle);

			if (milestone.getDescription() != null && !milestone.getDescription().isEmpty()) {
				binding.milestoneDescription.setVisibility(View.VISIBLE);
				Markdown.render(
						context,
						EmojiParser.parseToUnicode(milestone.getDescription()),
						binding.milestoneDescription);
			} else {
				binding.milestoneDescription.setVisibility(View.GONE);
			}

			binding.milestoneIssuesOpen.setText(String.valueOf(milestone.getOpenIssues()));
			binding.milestoneIssuesClosed.setText(String.valueOf(milestone.getClosedIssues()));

			long total = milestone.getOpenIssues() + milestone.getClosedIssues();
			int progress = (total > 0) ? (int) (100 * milestone.getClosedIssues() / total) : 0;

			binding.milestoneProgress.setProgress(progress, true);
			binding.progressPercent.setText(progress + "%");

			if (milestone.getDueOn() != null) {
				binding.milestoneDueDate.setText(
						TimeHelper.getAbsoluteDate(milestone.getDueOn(), locale));

				binding.dueDateFrame.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(milestone.getDueOn(), locale)));

				if (milestone.getDueOn().before(new Date())) {
					binding.milestoneDueDate.setTextColor(context.getColor(R.color.darkRed));
					binding.dateIcon.setImageTintList(
							ColorStateList.valueOf(context.getColor(R.color.darkRed)));
				}
			} else {
				binding.milestoneDueDate.setText(context.getString(R.string.milestoneNoDueDate));
			}
		}
	}
}
