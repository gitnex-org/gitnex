package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.Collaborators;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Author M M Arif
 */

public class AssigneesListAdapter extends RecyclerView.Adapter<AssigneesListAdapter.AssigneesViewHolder> {

	private Context mCtx;
	private List<Collaborators> assigneesList;
	private List<String> assigneesStrings = new ArrayList<>();
	private List<String> currentAssignees;

	private AssigneesListAdapterListener assigneesListener;

	public interface AssigneesListAdapterListener {

		void assigneesInterface(List<String> data);
	}

	public AssigneesListAdapter(Context mCtx, List<Collaborators> dataMain, AssigneesListAdapterListener assigneesListener, List<String> currentAssignees) {

		this.mCtx = mCtx;
		this.assigneesList = dataMain;
		this.assigneesListener = assigneesListener;
		this.currentAssignees = currentAssignees;
	}

	static class AssigneesViewHolder extends RecyclerView.ViewHolder {

		private CheckBox assigneesSelection;
		private TextView assigneesName;
		private ImageView assigneesAvatar;

		private AssigneesViewHolder(View itemView) {

			super(itemView);
			this.setIsRecyclable(false);

			assigneesSelection = itemView.findViewById(R.id.assigneesSelection);
			assigneesName = itemView.findViewById(R.id.assigneesName);
			assigneesAvatar = itemView.findViewById(R.id.assigneesAvatar);
		}
	}

	@NonNull
	@Override
	public AssigneesListAdapter.AssigneesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_assignees_list, parent, false);
		return new AssigneesListAdapter.AssigneesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull AssigneesListAdapter.AssigneesViewHolder holder, int position) {

		Collaborators currentItem = assigneesList.get(position);

		if(currentItem.getFull_name().equals("")) {

			holder.assigneesName.setText(currentItem.getLogin());
		}
		else {

			holder.assigneesName.setText(currentItem.getFull_name());
		}
		PicassoService
			.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(180, 180).centerCrop().into(holder.assigneesAvatar);

		for(int i = 0; i < assigneesList.size(); i++) {

			if(assigneesStrings.contains(currentItem.getLogin())) {

				holder.assigneesSelection.setChecked(true);
			}
		}

		currentAssignees = new ArrayList<>(new LinkedHashSet<>(currentAssignees));

		for(int i = 0; i < currentAssignees.size(); i++) {

			if(currentAssignees.contains(currentItem.getLogin())) {

				holder.assigneesSelection.setChecked(true);
				assigneesStrings.add(currentAssignees.get(i));
			}
		}

		assigneesListener.assigneesInterface(assigneesStrings);

		holder.assigneesSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if(isChecked) {

				assigneesStrings.add(currentItem.getLogin());
			}
			else {

				assigneesStrings.remove(currentItem.getLogin());
			}

			assigneesListener.assigneesInterface(assigneesStrings);
		});

		assigneesStrings = new ArrayList<>(new LinkedHashSet<>(assigneesStrings));
	}

	@Override
	public int getItemCount() {

		return assigneesList.size();
	}

	public void updateList(List<String> list) {

		currentAssignees = list;
		notifyDataSetChanged();
	}
}
