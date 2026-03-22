package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.databinding.ListUsersBinding;

/**
 * @author mmarif
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

	private List<User> userList;
	private final Context context;

	public UsersAdapter(Context ctx, List<User> userList) {
		this.context = ctx;
		this.userList = userList;
	}

	@NonNull @Override
	public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListUsersBinding binding =
				ListUsersBinding.inflate(LayoutInflater.from(context), parent, false);
		return new UserViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
		User user = userList.get(position);
		holder.bind(user);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return userList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.userList = newList;
		notifyDataSetChanged();
	}

	public class UserViewHolder extends RecyclerView.ViewHolder {
		private final ListUsersBinding binding;

		UserViewHolder(ListUsersBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot()
					.setOnClickListener(
							v -> {
								User user = userList.get(getBindingAdapterPosition());
								Intent intent = new Intent(context, ProfileActivity.class);
								intent.putExtra("username", user.getLogin());
								context.startActivity(intent);
							});
		}

		void bind(User user) {
			binding.userNameTv.setText(
					user.getFullName().isEmpty() ? user.getLogin() : user.getFullName());
			binding.userName.setText(context.getString(R.string.usernameWithAt, user.getLogin()));
			binding.userName.setVisibility(user.getFullName().isEmpty() ? View.GONE : View.VISIBLE);

			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_person)
					.centerCrop()
					.into(binding.userAvatarImageView);
		}
	}
}
