package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
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
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.ListItemSearchActionBinding;
import org.mian.gitnex.viewmodels.OrgTeamsViewModel;

/**
 * @author mmarif
 */
public class OrganizationAddUserToTeamMemberAdapter
		extends RecyclerView.Adapter<OrganizationAddUserToTeamMemberAdapter.ViewHolder> {

	private List<User> list;
	private final Context context;
	private final int teamId;
	private final OrgTeamsViewModel viewModel;

	public OrganizationAddUserToTeamMemberAdapter(
			List<User> list, Context context, int teamId, OrgTeamsViewModel viewModel) {
		this.list = list;
		this.context = context;
		this.teamId = teamId;
		this.viewModel = viewModel;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<User> newList) {
		this.list = newList;
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListItemSearchActionBinding binding =
				ListItemSearchActionBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User user = list.get(position);
		ListItemSearchActionBinding binding = holder.binding;

		String fullName =
				(user.getFullName() == null || user.getFullName().isEmpty())
						? user.getLogin()
						: user.getFullName();
		binding.userFullName.setText(fullName);

		binding.userName.setText(context.getString(R.string.usernameWithAt, user.getLogin()));

		Glide.with(context)
				.load(user.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.error(R.drawable.ic_person)
				.centerCrop()
				.into(binding.userAvatar);

		boolean isMember = viewModel.isUserInTeam(user.getLogin());
		String currentAccount = ((BaseActivity) context).getAccount().getAccount().getUserName();

		if (isMember) {
			binding.addCollaboratorButtonAdd.setVisibility(View.GONE);
			binding.addCollaboratorButtonRemove.setVisibility(
					user.getLogin().equals(currentAccount) ? View.GONE : View.VISIBLE);
		} else {
			binding.addCollaboratorButtonAdd.setVisibility(View.VISIBLE);
			binding.addCollaboratorButtonRemove.setVisibility(View.GONE);
		}

		binding.addCollaboratorButtonAdd.setOnClickListener(
				v -> {
					viewModel.addTeamMember(context, user.getLogin(), teamId);
				});

		binding.addCollaboratorButtonRemove.setOnClickListener(
				v -> {
					viewModel.removeTeamMember(context, user.getLogin(), teamId);
				});

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list != null ? list.size() : 0;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ListItemSearchActionBinding binding;

		ViewHolder(ListItemSearchActionBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
