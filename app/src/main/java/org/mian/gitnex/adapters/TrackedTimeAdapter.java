package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.TrackedTime;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListTrackedTimeBinding;

/**
 * @author mmarif
 */
public class TrackedTimeAdapter
		extends RecyclerView.Adapter<TrackedTimeAdapter.TrackedTimeViewHolder> {

	private final List<TrackedTime> trackedTimeList;
	private OnDeleteClickListener deleteClickListener;

	public TrackedTimeAdapter(List<TrackedTime> trackedTimeList) {
		this.trackedTimeList = trackedTimeList;
	}

	public void setOnDeleteClickListener(OnDeleteClickListener listener) {
		this.deleteClickListener = listener;
	}

	@NonNull @Override
	public TrackedTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListTrackedTimeBinding binding =
				ListTrackedTimeBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new TrackedTimeViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull TrackedTimeViewHolder holder, int position) {
		TrackedTime time = trackedTimeList.get(position);

		Glide.with(holder.itemView.getContext())
				.load(time.getIssue().getUser().getAvatarUrl())
				.placeholder(R.drawable.ic_person)
				.into(holder.binding.userAvatar);

		String userName =
				time.getIssue().getUser().getFullName().isEmpty()
						? time.getIssue().getUser().getLogin()
						: time.getIssue().getUser().getFullName();
		holder.binding.userName.setText(userName);

		long totalSeconds = time.getTime();
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;
		holder.binding.trackedTimeEntry.setText(
				String.format(Locale.US, "%dh %dm %ds", hours, minutes, seconds));

		holder.binding.deleteTrackedTime.setEnabled(true);
		holder.binding.deleteTrackedTime.setOnClickListener(
				v -> {
					if (deleteClickListener != null) {
						deleteClickListener.onDeleteClick(time, position);
					}
				});
	}

	@Override
	public int getItemCount() {
		return trackedTimeList.size();
	}

	public static class TrackedTimeViewHolder extends RecyclerView.ViewHolder {
		private final ListTrackedTimeBinding binding;

		TrackedTimeViewHolder(ListTrackedTimeBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	public interface OnDeleteClickListener {
		void onDeleteClick(TrackedTime time, int position);
	}
}
