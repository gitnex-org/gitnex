package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.databinding.ListLabelsBinding;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelStylingHelper;

/**
 * @author mmarif
 */
public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.DataHolder>
		implements Filterable {

	private final Context context;
	private List<Label> labelsList;
	private List<Label> labelsListFull;
	private boolean canEdit;
	private final OnLabelMenuClickListener onMenuClick;

	public interface OnLabelMenuClickListener {
		void onMenuClick(Label label);
	}

	public LabelsAdapter(
			Context ctx, List<Label> list, boolean canEdit, OnLabelMenuClickListener onMenuClick) {
		this.context = ctx;
		this.labelsList = list;
		this.labelsListFull = new ArrayList<>(list);
		this.canEdit = canEdit;
		this.onMenuClick = onMenuClick;
	}

	@NonNull @Override
	public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new DataHolder(
				ListLabelsBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull DataHolder holder, int position) {
		holder.bindData(labelsList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return labelsList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Label> newList) {
		this.labelsList = newList;
		this.labelsListFull = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Label> filtered = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filtered.addAll(labelsListFull);
				} else {
					String pattern = constraint.toString().toLowerCase().trim();
					for (Label label : labelsListFull) {
						if (label.getName().toLowerCase().contains(pattern)) filtered.add(label);
					}
				}
				FilterResults results = new FilterResults();
				results.values = filtered;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				labelsList = (List<Label>) results.values;
				notifyDataSetChanged();
			}
		};
	}

	public class DataHolder extends RecyclerView.ViewHolder {
		private final ListLabelsBinding binding;
		private Label currentLabel;

		DataHolder(ListLabelsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			binding.itemMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);
			binding.itemMenu.setOnClickListener(
					v -> {
						if (onMenuClick != null) {
							onMenuClick.onMenuClick(currentLabel);
						}
					});
		}

		void bindData(Label label) {
			this.currentLabel = label;
			String labelText = label.getName();
			String labelColor = "#" + label.getColor();
			boolean exclusive = label.isExclusive();
			int color = Color.parseColor("#" + label.getColor());
			int contrast = ColorInverter.getContrastColor(color);

			if (LabelStylingHelper.isScopedLabel(labelText, exclusive)) {
				binding.labelValue.setVisibility(View.VISIBLE);
				LabelStylingHelper.getInstance(context)
						.styleScopedLabel(
								labelText,
								labelColor,
								String.format("#%06X", contrast),
								binding.labelName,
								binding.labelValue,
								13,
								6,
								12);
			} else {
				binding.labelValue.setVisibility(View.GONE);
				LabelStylingHelper.getInstance(context)
						.styleRegularLabel(
								labelText,
								labelColor,
								String.format("#%06X", contrast),
								binding.labelName,
								13,
								6,
								12);
			}

			if (label.getDescription() != null && !label.getDescription().isEmpty()) {
				binding.labelDescription.setVisibility(View.VISIBLE);
				binding.labelDescription.setText(label.getDescription());
			} else {
				binding.labelDescription.setVisibility(View.GONE);
			}
		}
	}
}
