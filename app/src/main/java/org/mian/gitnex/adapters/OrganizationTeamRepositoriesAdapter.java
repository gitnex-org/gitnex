package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.databinding.ListItemSearchActionBinding;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.viewmodels.OrgTeamsViewModel;

/**
 * @author mmarif
 */
public class OrganizationTeamRepositoriesAdapter
		extends RecyclerView.Adapter<OrganizationTeamRepositoriesAdapter.ViewHolder> {
	private List<Repository> list;
	private final Context context;
	private final int teamId;
	private final String orgName;
	private final OrgTeamsViewModel viewModel;

	public OrganizationTeamRepositoriesAdapter(
			List<Repository> list,
			Context context,
			int teamId,
			String orgName,
			OrgTeamsViewModel viewModel) {
		this.list = list;
		this.context = context;
		this.teamId = teamId;
		this.orgName = orgName;
		this.viewModel = viewModel;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Repository> newList) {
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
		Repository repo = list.get(position);
		ListItemSearchActionBinding b = holder.binding;

		b.userFullName.setText(repo.getName());
		b.userName.setVisibility(View.GONE);

		if (repo.getAvatarUrl() != null && !repo.getAvatarUrl().isEmpty()) {
			Glide.with(context).load(repo.getAvatarUrl()).centerCrop().into(b.userAvatar);
		} else {
			b.userAvatar.setImageDrawable(
					AvatarGenerator.getLetterAvatar(context, repo.getName(), 40));
		}

		boolean isAdded = viewModel.isRepoInTeam(repo.getName());

		b.addCollaboratorButtonAdd.setVisibility(isAdded ? View.GONE : View.VISIBLE);
		b.addCollaboratorButtonRemove.setVisibility(isAdded ? View.VISIBLE : View.GONE);

		b.addCollaboratorButtonAdd.setOnClickListener(
				v -> viewModel.addRepoToTeam(context, orgName, repo.getName(), teamId));

		b.addCollaboratorButtonRemove.setOnClickListener(
				v -> viewModel.removeRepoFromTeam(context, orgName, repo.getName(), teamId));

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return list.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		ListItemSearchActionBinding binding;

		ViewHolder(ListItemSearchActionBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
