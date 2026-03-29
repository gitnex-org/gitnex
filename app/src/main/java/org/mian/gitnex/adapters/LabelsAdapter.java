package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.databinding.BottomSheetLabelsInListBinding;
import org.mian.gitnex.databinding.ListLabelsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ColorInverter;

/**
 * @author mmarif
 */
public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.DataHolder>
		implements Filterable {

	private final Context context;
	private List<Label> labelsList;
	private List<Label> labelsListFull;
	private final String type;
	private final String orgName;
	private boolean canEdit;

	public LabelsAdapter(
			Context ctx, List<Label> list, String type, String orgName, boolean canEdit) {
		this.context = ctx;
		this.labelsList = list;
		this.labelsListFull = new ArrayList<>(list);
		this.type = type;
		this.orgName = orgName;
		this.canEdit = canEdit;
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
						BottomSheetLabelsInListBinding sheetB =
								BottomSheetLabelsInListBinding.inflate(
										LayoutInflater.from(context));
						BottomSheetDialog dialog = new BottomSheetDialog(context);
						dialog.setContentView(sheetB.getRoot());
						sheetB.bottomSheetHeader.setText(currentLabel.getName());

						sheetB.labelMenuEdit.setOnClickListener(
								v1 -> {
									Intent i =
											new Intent(context, CreateLabelActivity.class)
													.putExtra(
															"labelId",
															String.valueOf(currentLabel.getId()))
													.putExtra("labelTitle", currentLabel.getName())
													.putExtra("labelColor", currentLabel.getColor())
													.putExtra("labelAction", "edit")
													.putExtra("type", type)
													.putExtra("orgName", orgName);
									context.startActivity(i);
									dialog.dismiss();
								});

						sheetB.labelMenuDelete.setOnClickListener(
								v1 -> {
									AlertDialogs.labelDeleteDialog(
											context,
											currentLabel.getName(),
											String.valueOf(currentLabel.getId()),
											type,
											orgName,
											null);
									dialog.dismiss();
								});
						dialog.show();
					});
		}

		void bindData(Label label) {
			this.currentLabel = label;
			int color = Color.parseColor("#" + label.getColor());
			int contrast = new ColorInverter().getContrastColor(color);

			ImageViewCompat.setImageTintList(binding.labelIcon, ColorStateList.valueOf(contrast));
			binding.labelName.setTextColor(contrast);
			binding.labelName.setText(label.getName());
			binding.labelView.setCardBackgroundColor(color);
		}
	}
}
