package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.databinding.ListUsersBinding;
import org.mian.gitnex.helpers.AvatarGenerator;

/**
 * @author mmarif
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>
		implements Filterable {

	private List<User> userList;
	private List<User> userListFull;
	private final Context context;

	public UsersAdapter(Context ctx, List<User> userList) {
		this.context = ctx;
		this.userList = userList;
		this.userListFull = new ArrayList<>(userList);
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
		this.userListFull = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<User> filteredList = new ArrayList<>();

				if (constraint == null || constraint.length() == 0) {
					filteredList.addAll(userListFull);
				} else {
					String filterPattern = constraint.toString().toLowerCase().trim();
					for (User item : userListFull) {
						String login = item.getLogin() != null ? item.getLogin().toLowerCase() : "";
						String fullName =
								item.getFullName() != null ? item.getFullName().toLowerCase() : "";
						String bio =
								item.getDescription() != null
										? item.getDescription().toLowerCase()
										: "";

						if (login.contains(filterPattern)
								|| fullName.contains(filterPattern)
								|| bio.contains(filterPattern)) {
							filteredList.add(item);
						}
					}
				}

				FilterResults results = new FilterResults();
				results.values = filteredList;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results != null && results.values instanceof List) {
					@SuppressWarnings("unchecked")
					List<User> filtered = (List<User>) results.values;
					userList = filtered;
					notifyDataSetChanged();
				}
			}
		};
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

			String label = (user.getFullName() != null) ? user.getFullName() : user.getLogin();
			Drawable placeholder = AvatarGenerator.getLetterAvatar(context, label, 44);

			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(placeholder)
					.centerCrop()
					.into(binding.userAvatarImageView);
		}
	}
}
