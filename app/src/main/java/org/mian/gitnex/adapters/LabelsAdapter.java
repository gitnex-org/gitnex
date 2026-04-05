package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.databinding.BottomsheetLabelsItemMenuBinding;
import org.mian.gitnex.databinding.ListLabelsBinding;
import org.mian.gitnex.helpers.ColorInverter;

/**
 * @author mmarif
 */
public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.DataHolder>
		implements Filterable {

	private final Context context;
	private List<Label> labelsList;
	private List<Label> labelsListFull;
	private boolean canEdit;
	private final OnLabelAction onEdit;
	private final OnLabelAction onDelete;

	public interface OnLabelAction {
		void run(Label label);
	}

	public LabelsAdapter(
			Context ctx,
			List<Label> list,
			boolean canEdit,
			OnLabelAction onEdit,
			OnLabelAction onDelete) {
		this.context = ctx;
		this.labelsList = list;
		this.labelsListFull = new ArrayList<>(list);
		this.canEdit = canEdit;
		this.onEdit = onEdit;
		this.onDelete = onDelete;
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
			binding.labelsOptionsMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);
			binding.labelsOptionsMenu.setOnClickListener(
					v -> {
						BottomsheetLabelsItemMenuBinding sheetB =
								BottomsheetLabelsItemMenuBinding.inflate(
										LayoutInflater.from(context));
						BottomSheetDialog dialog = new BottomSheetDialog(context);
						dialog.setContentView(sheetB.getRoot());
						sheetB.sheetTitle.setText(currentLabel.getName());

						sheetB.labelMenuEdit.setOnClickListener(
								v1 -> {
									onEdit.run(currentLabel);
									dialog.dismiss();
								});

						sheetB.labelMenuDelete.setOnClickListener(
								v1 -> {
									onDelete.run(currentLabel);
									dialog.dismiss();
								});
						dialog.show();
					});
		}

		void bindData(Label label) {
			this.currentLabel = label;

			int color = Color.parseColor("#" + label.getColor());
			int contrast = new ColorInverter().getContrastColor(color);

			binding.labelView.setCardBackgroundColor(color);
			binding.labelName.setText(label.getName());
			binding.labelName.setTextColor(contrast);
			ImageViewCompat.setImageTintList(binding.labelIcon, ColorStateList.valueOf(contrast));

			if (label.getDescription() != null && !label.getDescription().isEmpty()) {
				binding.labelDescription.setVisibility(View.VISIBLE);
				binding.labelDescription.setText(label.getDescription());
			} else {
				binding.labelDescription.setVisibility(View.GONE);
			}
		}
	}
}
