package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;

/**
 * @author M M Arif
 */
public class LabelsListAdapter extends RecyclerView.Adapter<LabelsListAdapter.LabelsViewHolder> {

	private final List<Label> labels;
	private final List<String> labelsStrings = new ArrayList<>();
	private final LabelsListAdapterListener labelsListener;
	private List<Integer> currentLabelsIds;
	private List<Integer> labelsIds = new ArrayList<>();

	public LabelsListAdapter(
			List<Label> labelsMain,
			LabelsListAdapterListener labelsListener,
			List<Integer> currentLabelsIds) {

		this.labels = labelsMain;
		this.labelsListener = labelsListener;
		this.currentLabelsIds = currentLabelsIds;
	}

	@NonNull @Override
	public LabelsListAdapter.LabelsViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.custom_labels_list, parent, false);
		return new LabelsListAdapter.LabelsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull LabelsListAdapter.LabelsViewHolder holder, int position) {

		Label currentItem = labels.get(position);

		String labelColor = currentItem.getColor();
		int color = Color.parseColor("#" + labelColor);

		holder.labelText.setText(currentItem.getName());
		holder.labelColor.setBackgroundColor(color);

		for (int i = 0; i < labelsIds.size(); i++) {

			if (labelsStrings.contains(currentItem.getName())) {

				holder.labelSelection.setChecked(true);
			}
		}

		currentLabelsIds = new ArrayList<>(new LinkedHashSet<>(currentLabelsIds));

		for (int i = 0; i < currentLabelsIds.size(); i++) {

			if (currentLabelsIds.contains(currentItem.getId().intValue())) {

				holder.labelSelection.setChecked(true);
				labelsIds.add(currentLabelsIds.get(i));
			}
		}

		labelsListener.labelsIdsInterface(labelsIds);

		holder.labelSelection.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {

						labelsStrings.add(currentItem.getName());
						labelsIds.add(currentItem.getId().intValue());
					} else {

						labelsStrings.remove(currentItem.getName());
						labelsIds.remove(Integer.valueOf(currentItem.getId().intValue()));
					}

					labelsListener.labelsInterface(labelsStrings);
					labelsListener.labelsIdsInterface(labelsIds);
				});

		labelsIds = new ArrayList<>(new LinkedHashSet<>(labelsIds));
	}

	@Override
	public int getItemCount() {

		return labels.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Integer> list) {

		currentLabelsIds = list;
		notifyDataSetChanged();
	}

	public interface LabelsListAdapterListener {

		void labelsInterface(List<String> data);

		void labelsIdsInterface(List<Integer> data);
	}

	public static class LabelsViewHolder extends RecyclerView.ViewHolder {

		private final CheckBox labelSelection;
		private final TextView labelText;
		private final ImageView labelColor;

		private LabelsViewHolder(View itemView) {

			super(itemView);
			this.setIsRecyclable(false);

			labelSelection = itemView.findViewById(R.id.labelSelection);
			labelText = itemView.findViewById(R.id.labelText);
			labelColor = itemView.findViewById(R.id.labelColor);
		}
	}
}
