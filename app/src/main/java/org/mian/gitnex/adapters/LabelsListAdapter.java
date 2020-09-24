package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Labels;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class LabelsListAdapter extends RecyclerView.Adapter<LabelsListAdapter.LabelsViewHolder> {

	private List<Labels> labels;
	private ArrayList<String> labelsStrings = new ArrayList<>();
	private ArrayList<Integer> labelsIds = new ArrayList<>();

	private LabelsListAdapterListener labelsListener;

	public interface LabelsListAdapterListener {

		void labelsStringData(ArrayList<String> data);
		void labelsIdsData(ArrayList<Integer> data);
	}

	public LabelsListAdapter(List<Labels> labelsMain, LabelsListAdapterListener labelsListener) {

		this.labels = labelsMain;
		this.labelsListener = labelsListener;
	}

	static class LabelsViewHolder extends RecyclerView.ViewHolder {

		private CheckBox labelSelection;

		private LabelsViewHolder(View itemView) {
			super(itemView);

			labelSelection = itemView.findViewById(R.id.labelSelection);

		}
	}

	@NonNull
	@Override
	public LabelsListAdapter.LabelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_labels_list, parent, false);
		return new LabelsListAdapter.LabelsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull LabelsListAdapter.LabelsViewHolder holder, int position) {

		Labels currentItem = labels.get(position);

		holder.labelSelection.setText(currentItem.getName());

		for(int i = 0; i < labelsIds.size(); i++) {

			if(labelsStrings.contains(currentItem.getName())) {

				holder.labelSelection.setChecked(true);
			}
		}

		holder.labelSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {

				labelsStrings.add(currentItem.getName());
				labelsIds.add(currentItem.getId());
			}
			else {

				labelsStrings.remove(currentItem.getName());
				labelsIds.remove(Integer.valueOf(currentItem.getId()));
			}

			labelsListener.labelsStringData(labelsStrings);
			labelsListener.labelsIdsData(labelsIds);
		});
	}

	@Override
	public int getItemCount() {
		return labels.size();
	}
}
