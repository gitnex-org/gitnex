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
import java.util.Locale;
import org.gitnex.tea4j.v2.models.ActionWorkflow;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListActionWorkflowsBinding;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class RepoActionsWorkflowsAdapter
		extends RecyclerView.Adapter<RepoActionsWorkflowsAdapter.ViewHolder> {

	private List<ActionWorkflow> workflows;
	private final Context context;

	public RepoActionsWorkflowsAdapter(Context context, List<ActionWorkflow> workflows) {
		this.context = context;
		this.workflows = workflows;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListActionWorkflowsBinding binding =
				ListActionWorkflowsBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		ActionWorkflow workflow = workflows.get(position);
		holder.bind(workflow);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return workflows != null ? workflows.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<ActionWorkflow> newList) {
		this.workflows = newList;
		notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		final ListActionWorkflowsBinding binding;

		ViewHolder(ListActionWorkflowsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(ActionWorkflow workflow) {
			binding.workflowName.setText(
					workflow.getName() != null
							? workflow.getName()
							: context.getString(R.string.na));
			binding.workflowPath.setText(
					workflow.getPath() != null
							? workflow.getPath()
							: context.getString(R.string.na));

			String state =
					workflow.getState() != null ? workflow.getState().toLowerCase() : "unknown";
			int statusColor =
					switch (state) {
						case "active" -> ContextCompat.getColor(context, R.color.darkGreen);
						case "disabled", "inactive" ->
								ContextCompat.getColor(context, R.color.lightGray);
						default -> ContextCompat.getColor(context, R.color.lightGray);
					};
			binding.statusIndicator.setBackground(
					AvatarGenerator.getCircleColorDrawable(context, statusColor, 14));
			binding.statusIndicator.setOnClickListener(
					v -> Toasty.show(context, state.toUpperCase()));

			if (workflow.getUpdatedAt() != null) {
				String timeAgo =
						TimeHelper.formatTime(workflow.getUpdatedAt(), Locale.getDefault());
				binding.workflowLastRun.setText(context.getString(R.string.lastUpdatedAt, timeAgo));
				binding.workflowLastRun.setVisibility(View.VISIBLE);
				binding.workflowLastRun.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												workflow.getUpdatedAt(), Locale.getDefault())));
			} else if (workflow.getCreatedAt() != null) {
				String timeAgo =
						TimeHelper.formatTime(workflow.getCreatedAt(), Locale.getDefault());
				binding.workflowLastRun.setText(context.getString(R.string.noteDateTime, timeAgo));
				binding.workflowLastRun.setVisibility(View.VISIBLE);
				binding.workflowLastRun.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												workflow.getCreatedAt(), Locale.getDefault())));
			} else {
				binding.workflowLastRun.setVisibility(View.GONE);
			}
		}
	}
}
