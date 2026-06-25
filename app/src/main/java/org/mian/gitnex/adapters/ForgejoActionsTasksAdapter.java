package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoTask;
import org.mian.gitnex.databinding.ListForgejoActionsTasksBinding;
import org.mian.gitnex.helpers.TimeHelper;

/**
 * @author mmarif
 */
public class ForgejoActionsTasksAdapter
		extends RecyclerView.Adapter<ForgejoActionsTasksAdapter.TaskHolder> {

	private final Context context;
	private List<ForgejoTask> tasks;

	public ForgejoActionsTasksAdapter(Context context, List<ForgejoTask> tasks) {
		this.context = context;
		this.tasks = tasks;
	}

	@NonNull @Override
	public TaskHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TaskHolder(
				ListForgejoActionsTasksBinding.inflate(
						LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TaskHolder holder, int position) {
		holder.bind(tasks.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return tasks.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ForgejoTask> newList) {
		this.tasks = newList;
		notifyDataSetChanged();
	}

	public static class TaskHolder extends RecyclerView.ViewHolder {
		private final ListForgejoActionsTasksBinding binding;

		TaskHolder(ListForgejoActionsTasksBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		@SuppressLint("SetTextI18n")
		void bind(ForgejoTask task) {
			binding.taskName.setText(
					task.getDisplayTitle() != null ? task.getDisplayTitle() : task.getName());
			binding.taskStatus.setText(task.getStatus());
			binding.taskEvent.setText(task.getEvent());
			binding.taskRunNumber.setText("#" + task.getRunNumber());

			if (task.getHeadBranch() != null) {
				binding.taskBranch.setText(task.getHeadBranch());
				binding.taskBranch.setVisibility(View.VISIBLE);
			} else {
				binding.taskBranch.setVisibility(View.GONE);
			}

			if (task.getUpdatedAt() != null) {
				binding.taskTime.setText(
						TimeHelper.formatTime(task.getUpdatedAt(), Locale.getDefault()));
			}
		}
	}
}
