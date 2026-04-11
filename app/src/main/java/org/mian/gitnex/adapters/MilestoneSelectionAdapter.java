package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Set;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.databinding.ListItemSelectionLabelBinding;

/**
 * @author mmarif
 */
public class MilestoneSelectionAdapter
		extends RecyclerView.Adapter<MilestoneSelectionAdapter.ViewHolder> {

	private final List<Milestone> milestones;
	private final Set<String> selectedMilestones;

	public MilestoneSelectionAdapter(List<Milestone> milestones, Set<String> selectedMilestones) {
		this.milestones = milestones;
		this.selectedMilestones = selectedMilestones;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemSelectionLabelBinding binding =
				ListItemSelectionLabelBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@SuppressLint("NotifyDataSetChanged")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Milestone milestone = milestones.get(position);
		String title = milestone.getTitle();

		holder.binding.colorDot.setVisibility(View.GONE);
		holder.binding.labelName.setText(title);
		holder.binding.checkbox.setOnCheckedChangeListener(null);
		holder.binding.checkbox.setChecked(selectedMilestones.contains(title));
		holder.binding.checkbox.setClickable(false);
		holder.binding.labelName.setClickable(false);

		holder.binding.card.setOnClickListener(
				v -> {
					if (selectedMilestones.contains(title)) {
						selectedMilestones.clear();
					} else {
						selectedMilestones.clear();
						selectedMilestones.add(title);
					}

					notifyDataSetChanged();
				});

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return milestones != null ? milestones.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Milestone> newList) {
		this.milestones.clear();
		this.milestones.addAll(newList);
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ListItemSelectionLabelBinding binding;

		public ViewHolder(ListItemSelectionLabelBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
