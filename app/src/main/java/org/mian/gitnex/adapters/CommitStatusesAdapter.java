package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author qwerty287
 */
public class CommitStatusesAdapter
		extends RecyclerView.Adapter<CommitStatusesAdapter.CommitStatusesViewHolder> {

	private final List<CommitStatus> statuses;

	public static class CommitStatusesViewHolder extends RecyclerView.ViewHolder {

		private CommitStatus status;

		private final TextView name;
		private final TextView description;
		private final ImageView icon;

		private CommitStatusesViewHolder(View itemView) {

			super(itemView);

			icon = itemView.findViewById(R.id.statusIcon);
			name = itemView.findViewById(R.id.name);
			description = itemView.findViewById(R.id.description);

			itemView.setOnClickListener(taskInfo -> openUrl());
		}

		private void openUrl() {
			if (status.getTargetUrl() != null && !status.getTargetUrl().isEmpty()) {
				AppUtil.openUrlInBrowser(itemView.getContext(), status.getTargetUrl());
			} else {
				Toasty.info(
						itemView.getContext(),
						itemView.getContext().getString(R.string.statusNoUrl));
			}
		}
	}

	public CommitStatusesAdapter(List<CommitStatus> statuses) {
		this.statuses = statuses;
	}

	@NonNull @Override
	public CommitStatusesAdapter.CommitStatusesViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {

		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_commit_status, parent, false);
		return new CommitStatusesAdapter.CommitStatusesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull CommitStatusesAdapter.CommitStatusesViewHolder holder, int position) {

		CommitStatus currentItem = statuses.get(position);
		Context ctx = holder.itemView.getContext();

		holder.status = currentItem;
		holder.name.setText(currentItem.getContext());
		holder.description.setText(currentItem.getDescription());
		switch (currentItem.getStatus().toLowerCase()) {
			case "pending":
				holder.icon.setImageResource(R.drawable.ic_dot_fill);
				ImageViewCompat.setImageTintList(
						holder.icon,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.lightYellow, null)));
				break;
			case "success":
				holder.icon.setImageResource(R.drawable.ic_check);
				ImageViewCompat.setImageTintList(
						holder.icon,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.colorLightGreen, null)));
				break;
			case "error":
			case "failure":
				holder.icon.setImageResource(R.drawable.ic_close);
				ImageViewCompat.setImageTintList(
						holder.icon,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.iconIssuePrClosedColor, null)));
				break;
			case "warning":
				holder.icon.setImageResource(R.drawable.ic_warning);
				ImageViewCompat.setImageTintList(
						holder.icon,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.lightYellow, null)));
				break;
			default:
				holder.icon.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return statuses.size();
	}
}
