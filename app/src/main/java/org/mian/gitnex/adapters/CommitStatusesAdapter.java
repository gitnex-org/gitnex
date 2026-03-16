package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListCommitStatusBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author qwerty287
 * @author mmarif
 */
public class CommitStatusesAdapter extends RecyclerView.Adapter<CommitStatusesAdapter.ViewHolder> {

	private final List<CommitStatus> statuses;

	public CommitStatusesAdapter(List<CommitStatus> statuses) {
		this.statuses = statuses;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListCommitStatusBinding binding =
				ListCommitStatusBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		CommitStatus status = statuses.get(position);
		holder.bind(status);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return statuses.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		private final ListCommitStatusBinding binding;
		private CommitStatus currentStatus;

		public ViewHolder(ListCommitStatusBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			binding.getRoot().setOnClickListener(v -> openUrl());
		}

		public void bind(CommitStatus status) {
			this.currentStatus = status;
			Context ctx = itemView.getContext();

			binding.name.setText(status.getContext());
			binding.description.setText(status.getDescription());

			int colorRes;
			int iconRes;

			switch (status.getStatus()) {
				case PENDING -> {
					iconRes = R.drawable.ic_dot_fill;
					colorRes = R.color.lightYellow;
				}
				case SUCCESS -> {
					iconRes = R.drawable.ic_check;
					colorRes = R.color.colorLightGreen;
				}
				case ERROR, FAILURE -> {
					iconRes = R.drawable.ic_close;
					colorRes = R.color.iconIssuePrClosedColor;
				}
				case WARNING -> {
					iconRes = R.drawable.ic_warning;
					colorRes = R.color.lightYellow;
				}
				default -> {
					iconRes = R.drawable.ic_dot_fill;
					colorRes = R.color.colorPrimary;
				}
			}

			int color = ctx.getColor(colorRes);
			binding.statusIcon.setImageResource(iconRes);
			ImageViewCompat.setImageTintList(binding.statusIcon, ColorStateList.valueOf(color));
		}

		private void openUrl() {
			if (currentStatus.getTargetUrl() != null && !currentStatus.getTargetUrl().isEmpty()) {
				AppUtil.openUrlInBrowser(itemView.getContext(), currentStatus.getTargetUrl());
			} else {
				Toasty.info(
						itemView.getContext(),
						itemView.getContext().getString(R.string.statusNoUrl));
			}
		}
	}
}
