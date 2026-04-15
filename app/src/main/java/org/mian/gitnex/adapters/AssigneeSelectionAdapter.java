package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ItemAssigneeBinding;
import org.mian.gitnex.helpers.AvatarGenerator;

/**
 * @author mmarif
 */
public class AssigneeSelectionAdapter
		extends RecyclerView.Adapter<AssigneeSelectionAdapter.ViewHolder> {

	private final List<User> assignees;
	private final Set<String> selectedAssignees;
	private final RequestOptions avatarOptions;
	private final String excludeUser;
	private final List<User> filteredAssignees = new ArrayList<>();

	public AssigneeSelectionAdapter(
			List<User> assignees, Set<String> selectedAssignees, @Nullable String excludeUser) {
		this.assignees = assignees;
		this.selectedAssignees = selectedAssignees;
		this.excludeUser = excludeUser;
		this.avatarOptions =
				new RequestOptions()
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.loader_animated)
						.error(R.drawable.ic_person)
						.centerCrop();
		filterAssignees();
	}

	private void filterAssignees() {
		filteredAssignees.clear();
		for (User user : assignees) {
			if (excludeUser == null || !excludeUser.equals(user.getLogin())) {
				filteredAssignees.add(user);
			}
		}
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ItemAssigneeBinding binding =
				ItemAssigneeBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User user = filteredAssignees.get(position);
		Context context = holder.itemView.getContext();

		String fullName = user.getFullName();
		String username = user.getLogin();

		holder.binding.assigneeFullname.setText(
				fullName != null && !fullName.isEmpty() ? fullName : username);
		holder.binding.assigneeUsername.setText("@" + username);

		if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
			Glide.with(context)
					.load(user.getAvatarUrl())
					.apply(avatarOptions)
					.into(holder.binding.assigneeAvatar);
		} else {
			holder.binding.assigneeAvatar.setImageDrawable(
					AvatarGenerator.getLetterAvatar(context, username, 44));
		}

		holder.binding.checkbox.setOnCheckedChangeListener(null);
		holder.binding.checkbox.setChecked(selectedAssignees.contains(username));

		holder.itemView.setOnClickListener(v -> holder.binding.checkbox.toggle());

		holder.binding.checkbox.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {
						selectedAssignees.add(username);
					} else {
						selectedAssignees.remove(username);
					}
				});

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return filteredAssignees.size();
	}

	public boolean isEmpty() {
		return filteredAssignees.isEmpty();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.assignees.clear();
		this.assignees.addAll(newList);
		filterAssignees();
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ItemAssigneeBinding binding;

		public ViewHolder(ItemAssigneeBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
