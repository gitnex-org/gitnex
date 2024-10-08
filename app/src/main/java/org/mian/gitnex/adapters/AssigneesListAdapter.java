package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;

/**
 * @author M M Arif
 */
public class AssigneesListAdapter
		extends RecyclerView.Adapter<AssigneesListAdapter.AssigneesViewHolder> {

	private final Context context;
	private final List<User> assigneesList;
	private final AssigneesListAdapterListener assigneesListener;
	private List<String> assigneesStrings = new ArrayList<>();
	private List<String> currentAssignees;

	public AssigneesListAdapter(
			Context ctx,
			List<User> dataMain,
			AssigneesListAdapterListener assigneesListener,
			List<String> currentAssignees) {

		this.context = ctx;
		this.assigneesList = dataMain;
		this.assigneesListener = assigneesListener;
		this.currentAssignees = currentAssignees;
	}

	@NonNull @Override
	public AssigneesListAdapter.AssigneesViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.custom_assignees_list, parent, false);
		return new AssigneesListAdapter.AssigneesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull AssigneesListAdapter.AssigneesViewHolder holder, int position) {

		User currentItem = assigneesList.get(position);

		if (currentItem.getFullName().isEmpty()) {

			holder.assigneesName.setText(currentItem.getLogin());
		} else {

			holder.assigneesName.setText(Html.fromHtml(currentItem.getFullName()));
		}

		Glide.with(context)
				.load(currentItem.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(holder.assigneesAvatar);

		for (int i = 0; i < assigneesList.size(); i++) {

			if (assigneesStrings.contains(currentItem.getLogin())) {

				holder.assigneesSelection.setChecked(true);
			}
		}

		currentAssignees = new ArrayList<>(new LinkedHashSet<>(currentAssignees));

		for (int i = 0; i < currentAssignees.size(); i++) {

			if (currentAssignees.contains(currentItem.getLogin())) {

				holder.assigneesSelection.setChecked(true);
				assigneesStrings.add(currentAssignees.get(i));
			}
		}

		assigneesListener.assigneesInterface(assigneesStrings);

		holder.assigneesSelection.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {

						assigneesStrings.add(currentItem.getLogin());
					} else {

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

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<String> list) {

		currentAssignees = list;
		notifyDataSetChanged();
	}

	public interface AssigneesListAdapterListener {

		void assigneesInterface(List<String> data);
	}

	public static class AssigneesViewHolder extends RecyclerView.ViewHolder {

		private final CheckBox assigneesSelection;
		private final TextView assigneesName;
		private final ImageView assigneesAvatar;

		private AssigneesViewHolder(View itemView) {

			super(itemView);
			this.setIsRecyclable(false);

			assigneesSelection = itemView.findViewById(R.id.assigneesSelection);
			assigneesName = itemView.findViewById(R.id.assigneesName);
			assigneesAvatar = itemView.findViewById(R.id.assigneesAvatar);
		}
	}
}
