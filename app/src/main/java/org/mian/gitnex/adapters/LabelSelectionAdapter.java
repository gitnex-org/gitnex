package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Set;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.databinding.ListItemSelectionLabelBinding;
import org.mian.gitnex.helpers.AvatarGenerator;

/**
 * @author mmarif
 */
public class LabelSelectionAdapter extends RecyclerView.Adapter<LabelSelectionAdapter.ViewHolder> {

	private final List<Label> labels;
	private final Set<String> selectedLabels;

	public LabelSelectionAdapter(List<Label> labels, Set<String> selectedLabels) {
		this.labels = labels;
		this.selectedLabels = selectedLabels;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemSelectionLabelBinding binding =
				ListItemSelectionLabelBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Label label = labels.get(position);
		Context context = holder.itemView.getContext();
		holder.binding.labelName.setText(label.getName());

		int color = Color.parseColor("#" + label.getColor());

		holder.binding.colorDot.setBackground(
				AvatarGenerator.getCircleColorDrawable(context, color, 16));

		holder.binding.checkbox.setOnCheckedChangeListener(null);
		holder.binding.checkbox.setChecked(selectedLabels.contains(label.getName()));

		holder.binding.card.setOnClickListener(v -> holder.binding.checkbox.toggle());

		holder.binding.checkbox.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {
						selectedLabels.add(label.getName());
					} else {
						selectedLabels.remove(label.getName());
					}
				});

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return labels != null ? labels.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Label> newList) {
		this.labels.clear();
		this.labels.addAll(newList);
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
