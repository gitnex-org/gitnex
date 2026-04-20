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
import org.gitnex.tea4j.v2.models.ActionRunner;
import org.gitnex.tea4j.v2.models.ActionRunnerLabel;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListActionRunnersBinding;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class RepoActionsRunnersAdapter
		extends RecyclerView.Adapter<RepoActionsRunnersAdapter.ViewHolder> {

	private List<ActionRunner> runners;
	private final Context context;

	public RepoActionsRunnersAdapter(Context context, List<ActionRunner> runners) {
		this.context = context;
		this.runners = runners;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListActionRunnersBinding binding =
				ListActionRunnersBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		ActionRunner runner = runners.get(position);
		holder.bind(runner);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return runners != null ? runners.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ActionRunner> newList) {
		this.runners = newList;
		notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		final ListActionRunnersBinding binding;

		ViewHolder(ListActionRunnersBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(ActionRunner runner) {
			binding.runnerName.setText(
					runner.getName() != null ? runner.getName() : context.getString(R.string.na));

			String status =
					runner.getStatus() != null ? runner.getStatus().toLowerCase() : "offline";
			int statusColor =
					switch (status) {
						case "online", "active" ->
								ContextCompat.getColor(context, R.color.darkGreen);
						case "offline", "inactive" ->
								ContextCompat.getColor(context, R.color.lightGray);
						default -> ContextCompat.getColor(context, R.color.lightGray);
					};
			binding.runnerStatusIndicator.setBackground(
					AvatarGenerator.getCircleColorDrawable(context, statusColor, 14));
			binding.runnerStatusIndicator.setOnClickListener(
					v -> Toasty.show(context, status.toUpperCase()));

			if (runner.isBusy() != null && runner.isBusy()) {
				binding.runnerBusyBadge.setVisibility(View.VISIBLE);
				int busyColor = ContextCompat.getColor(context, R.color.releasePre);
				binding.runnerBusyBadge.setBackground(
						AvatarGenerator.getLabelDrawable(
								context,
								context.getString(R.string.busy).toUpperCase(),
								busyColor,
								16));
			} else {
				binding.runnerBusyBadge.setVisibility(View.GONE);
			}

			List<ActionRunnerLabel> runnerLabels = runner.getLabels();
			if (runnerLabels != null && !runnerLabels.isEmpty()) {
				StringBuilder labelsText = new StringBuilder();
				for (ActionRunnerLabel label : runnerLabels) {
					if (label.getName() != null) {
						labelsText.append(label.getName()).append(", ");
					}
				}
				if (labelsText.length() > 0) {
					labelsText.setLength(labelsText.length() - 2);
				}
				binding.runnerLabels.setText(labelsText.toString());
				binding.labelsContainer.setVisibility(View.VISIBLE);
			} else {
				binding.labelsContainer.setVisibility(View.GONE);
			}
		}
	}
}
