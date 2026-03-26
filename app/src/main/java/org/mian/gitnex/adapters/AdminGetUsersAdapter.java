package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.databinding.ListAdminUsersBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;

/**
 * @author mmarif
 */
public class AdminGetUsersAdapter extends RecyclerView.Adapter<AdminGetUsersAdapter.ViewHolder> {

	private final List<User> usersList;
	private final Context context;

	public AdminGetUsersAdapter(List<User> usersList, Context context) {
		this.context = context;
		this.usersList = usersList;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListAdminUsersBinding binding =
				ListAdminUsersBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User user = usersList.get(position);
		holder.bind(user, position);
	}

	@Override
	public int getItemCount() {
		return usersList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.usersList.clear();
		this.usersList.addAll(newList);
		notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private final ListAdminUsersBinding binding;

		ViewHolder(ListAdminUsersBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(User user, int position) {
			String loginId = user.getLogin();

			binding.card.setOnClickListener(
					v -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", loginId);
						context.startActivity(intent);
					});

			binding.userAvatar.setOnLongClickListener(
					v -> {
						AppUtil.copyToClipboard(
								context,
								loginId,
								context.getString(R.string.copyLoginIdToClipBoard, loginId));
						return true;
					});

			if (user.getFullName() != null && !user.getFullName().isEmpty()) {
				binding.userFullName.setText(user.getFullName());
				binding.userName.setText(context.getString(R.string.usernameWithAt, loginId));
				binding.userName.setVisibility(View.VISIBLE);
			} else {
				binding.userFullName.setText(context.getString(R.string.usernameWithAt, loginId));
				binding.userName.setVisibility(View.GONE);
			}

			if (user.getEmail() != null && !user.getEmail().isEmpty()) {
				binding.userEmail.setText(user.getEmail());
				binding.userEmail.setVisibility(View.VISIBLE);
			} else {
				binding.userEmail.setVisibility(View.GONE);
			}

			if (user.isIsAdmin()) {
				binding.userRole.setVisibility(View.VISIBLE);
				int badgeColor =
						ResourcesCompat.getColor(context.getResources(), R.color.darkGreen, null);
				String label = context.getString(R.string.userRoleAdmin).toUpperCase();

				binding.userRole.setImageDrawable(
						AvatarGenerator.getLabelDrawable(context, label, badgeColor, 20));
			} else {
				binding.userRole.setVisibility(View.GONE);
			}

			Drawable placeholder = AvatarGenerator.getLetterAvatar(context, loginId, 56);
			Glide.with(context)
					.load(user.getAvatarUrl())
					.placeholder(R.drawable.loader_animated)
					.error(placeholder)
					.centerCrop()
					.into(binding.userAvatar);

			binding.getRoot().updateAppearance(position, getItemCount());
		}
	}
}
