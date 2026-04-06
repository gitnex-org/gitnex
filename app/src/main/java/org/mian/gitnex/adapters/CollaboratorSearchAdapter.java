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
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.databinding.ListItemSearchActionBinding;

/**
 * @author mmarif
 */
public class CollaboratorSearchAdapter
		extends RecyclerView.Adapter<CollaboratorSearchAdapter.SearchViewHolder> {

	private List<User> searchResults = new ArrayList<>();
	private final Context context;
	private final OnCollaboratorActionListener listener;

	public interface OnCollaboratorActionListener {
		void onAdd(User user);

		void onRemove(User user);

		boolean isAlreadyCollaborator(String login);
	}

	public CollaboratorSearchAdapter(Context context, OnCollaboratorActionListener listener) {
		this.context = context;
		this.listener = listener;
	}

	@NonNull @Override
	public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemSearchActionBinding binding =
				ListItemSearchActionBinding.inflate(LayoutInflater.from(context), parent, false);
		return new SearchViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
		holder.bind(searchResults.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return searchResults.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.searchResults = newList;
		notifyDataSetChanged();
	}

	public class SearchViewHolder extends RecyclerView.ViewHolder {
		private final ListItemSearchActionBinding binding;

		SearchViewHolder(ListItemSearchActionBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(User user) {
			binding.userFullName.setText(
					user.getFullName().isEmpty() ? user.getLogin() : user.getFullName());
			binding.userName.setText(context.getString(R.string.usernameWithAt, user.getLogin()));

			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.userAvatar);

			boolean isMember = listener.isAlreadyCollaborator(user.getLogin());

			binding.addCollaboratorButtonAdd.setVisibility(isMember ? View.GONE : View.VISIBLE);
			binding.addCollaboratorButtonRemove.setVisibility(isMember ? View.VISIBLE : View.GONE);

			binding.addCollaboratorButtonAdd.setOnClickListener(v -> listener.onAdd(user));
			binding.addCollaboratorButtonRemove.setOnClickListener(v -> listener.onRemove(user));

			binding.avatarContainer.setOnClickListener(
					v -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", user.getLogin());
						context.startActivity(intent);
					});
		}
	}
}
