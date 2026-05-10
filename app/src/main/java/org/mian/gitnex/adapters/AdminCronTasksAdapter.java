package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Cron;
import org.mian.gitnex.databinding.ListAdminCronTasksBinding;

/**
 * @author mmarif
 */
public class AdminCronTasksAdapter extends RecyclerView.Adapter<AdminCronTasksAdapter.ViewHolder> {

	private final List<Cron> tasks;
	private final OnCronTaskListener listener;

	public interface OnCronTaskListener {
		void onRunTask(String taskName);

		void onShowDetails(Cron task);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Cron> newList) {
		this.tasks.clear();
		this.tasks.addAll(newList);
		notifyDataSetChanged();
	}

	public AdminCronTasksAdapter(List<Cron> tasks, OnCronTaskListener listener) {
		this.tasks = tasks;
		this.listener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListAdminCronTasksBinding binding =
				ListAdminCronTasksBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		Cron task = tasks.get(position);
		String cleanName = task.getName().replace("_", " ");

		holder.binding.taskName.setText(
				cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1));
		holder.binding.runTask.setOnClickListener(v -> listener.onRunTask(task.getName()));
		holder.binding.getRoot().setOnClickListener(v -> listener.onShowDetails(task));

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return tasks.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		ListAdminCronTasksBinding binding;

		ViewHolder(ListAdminCronTasksBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
