package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoRunner;
import org.mian.gitnex.databinding.ListForgejoActionsRunnersBinding;

/**
 * @author mmarif
 */
public class ForgejoActionsRunnersAdapter
		extends RecyclerView.Adapter<ForgejoActionsRunnersAdapter.RunnerHolder> {

	private final Context context;
	private List<ForgejoRunner> runners;

	public ForgejoActionsRunnersAdapter(Context context, List<ForgejoRunner> runners) {
		this.context = context;
		this.runners = runners;
	}

	@NonNull @Override
	public RunnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new RunnerHolder(
				ListForgejoActionsRunnersBinding.inflate(
						LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RunnerHolder holder, int position) {
		holder.bind(runners.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return runners.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ForgejoRunner> newList) {
		this.runners = newList;
		notifyDataSetChanged();
	}

	public class RunnerHolder extends RecyclerView.ViewHolder {
		private final ListForgejoActionsRunnersBinding binding;

		RunnerHolder(ListForgejoActionsRunnersBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(ForgejoRunner runner) {
			binding.runnerName.setText(
					runner.getName() != null ? runner.getName() : runner.getUuid());
			binding.runnerStatus.setText(runner.getStatus());

			int statusColor =
					"online".equalsIgnoreCase(runner.getStatus())
							? ContextCompat.getColor(context, R.color.colorLightGreen)
							: ContextCompat.getColor(context, R.color.colorRed);
			binding.runnerStatus.setTextColor(statusColor);

			if (runner.getVersion() != null) {
				binding.runnerVersion.setText(runner.getVersion());
				binding.runnerVersion.setVisibility(View.VISIBLE);
			} else {
				binding.runnerVersion.setVisibility(View.GONE);
			}

			if (runner.getLabels() != null && !runner.getLabels().isEmpty()) {
				binding.runnerLabels.setText(String.join(", ", runner.getLabels()));
				binding.runnerLabels.setVisibility(View.VISIBLE);
			} else {
				binding.runnerLabels.setVisibility(View.GONE);
			}
		}
	}
}
