package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
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
import org.mian.gitnex.databinding.ListCollaboratorsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class CollaboratorsAdapter
		extends RecyclerView.Adapter<CollaboratorsAdapter.CollaboratorViewHolder> {

	private List<User> collaboratorsList;
	private final Context context;

	public CollaboratorsAdapter(Context ctx, List<User> collaboratorsList) {
		this.context = ctx;
		this.collaboratorsList = collaboratorsList;
	}

	@NonNull @Override
	public CollaboratorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListCollaboratorsBinding binding =
				ListCollaboratorsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new CollaboratorViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull CollaboratorViewHolder holder, int position) {
		User collaborator = collaboratorsList.get(position);
		holder.bind(collaborator);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return collaboratorsList != null ? collaboratorsList.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.collaboratorsList = newList;
		notifyDataSetChanged();
	}

	public class CollaboratorViewHolder extends RecyclerView.ViewHolder {
		private final ListCollaboratorsBinding binding;

		CollaboratorViewHolder(ListCollaboratorsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot()
					.setOnClickListener(
							v -> {
								int position = getBindingAdapterPosition();
								if (position != RecyclerView.NO_POSITION) {
									User user = collaboratorsList.get(position);
									Intent intent = new Intent(context, ProfileActivity.class);
									intent.putExtra("username", user.getLogin());
									context.startActivity(intent);
								}
							});

			binding.getRoot()
					.setOnLongClickListener(
							v -> {
								int position = getBindingAdapterPosition();
								if (position != RecyclerView.NO_POSITION) {
									User user = collaboratorsList.get(position);
									AppUtil.copyToClipboard(
											context,
											user.getLogin(),
											context.getString(
													R.string.copyLoginIdToClipBoard,
													user.getLogin()));
								}
								return true;
							});
		}

		void bind(User user) {
			if (user.getFullName() != null && !user.getFullName().isEmpty()) {
				binding.userFullname.setText(
						Html.fromHtml(user.getFullName(), Html.FROM_HTML_MODE_COMPACT));
				binding.username.setText(
						context.getString(R.string.usernameWithAt, user.getLogin()));
				binding.username.setVisibility(View.VISIBLE);
			} else {
				binding.userFullname.setText(user.getLogin());
				binding.username.setVisibility(View.GONE);
			}

			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_person)
					.centerCrop()
					.into(binding.userAvatar);
		}
	}
}
