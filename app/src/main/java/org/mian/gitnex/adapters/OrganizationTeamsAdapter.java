package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.activities.OrganizationTeamInfoActivity;
import org.mian.gitnex.databinding.ListOrganizationTeamsBinding;

/**
 * @author mmarif
 */
public class OrganizationTeamsAdapter
		extends RecyclerView.Adapter<OrganizationTeamsAdapter.TeamViewHolder>
		implements Filterable {

	private final List<Team> teamList;
	private final List<Team> teamListFull;
	private final Context context;
	private final OrganizationPermissions permissions;
	private final String orgName;
	private Map<Long, List<User>> teamMembersMap = new HashMap<>();

	public OrganizationTeamsAdapter(
			Context ctx, List<Team> teamList, OrganizationPermissions permissions, String orgName) {
		this.context = ctx;
		this.teamList = teamList;
		this.teamListFull = new ArrayList<>(teamList);
		this.permissions = permissions;
		this.orgName = orgName;
	}

	@NonNull @Override
	public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new TeamViewHolder(
				ListOrganizationTeamsBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
		holder.bind(teamList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return teamList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateTeams(List<Team> newList) {
		teamList.clear();
		teamList.addAll(newList);
		teamListFull.clear();
		teamListFull.addAll(newList);
		notifyDataSetChanged();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateMemberMap(Map<Long, List<User>> map) {
		this.teamMembersMap = map;
		notifyDataSetChanged();
	}

	public class TeamViewHolder extends RecyclerView.ViewHolder {
		private final ListOrganizationTeamsBinding binding;
		private final OrganizationTeamMembersPreviewAdapter previewAdapter;
		private final List<User> userInfos = new ArrayList<>();

		TeamViewHolder(ListOrganizationTeamsBinding b) {
			super(b.getRoot());
			this.binding = b;

			binding.membersPreview.setLayoutManager(
					new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
			previewAdapter = new OrganizationTeamMembersPreviewAdapter(context, userInfos);
			binding.membersPreview.setAdapter(previewAdapter);

			itemView.setOnClickListener(
					v -> {
						Team team = teamList.get(getAbsoluteAdapterPosition());
						Intent intent = new Intent(context, OrganizationTeamInfoActivity.class);
						intent.putExtra("team", team);
						intent.putExtra("permissions", permissions);
						intent.putExtra("orgName", orgName);
						context.startActivity(intent);
					});
		}

		@SuppressLint("NotifyDataSetChanged")
		void bind(Team team) {
			binding.teamTitle.setText(team.getName());

			if (team.getDescription() != null && !team.getDescription().isEmpty()) {
				binding.teamDescription.setVisibility(View.VISIBLE);
				binding.teamDescription.setText(team.getDescription());
			} else {
				binding.teamDescription.setVisibility(View.GONE);
			}

			List<User> members = teamMembersMap.get(team.getId());
			if (members != null && !members.isEmpty()) {
				binding.membersPreviewFrame.setVisibility(View.VISIBLE);
				userInfos.clear();
				userInfos.addAll(members);
				previewAdapter.notifyDataSetChanged();
			} else {
				binding.membersPreviewFrame.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Team> filtered = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filtered.addAll(teamListFull);
				} else {
					String pattern = constraint.toString().toLowerCase().trim();
					for (Team item : teamListFull) {
						if (item.getName().toLowerCase().contains(pattern)
								|| (item.getDescription() != null
										&& item.getDescription().toLowerCase().contains(pattern))) {
							filtered.add(item);
						}
					}
				}
				FilterResults results = new FilterResults();
				results.values = filtered;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				teamList.clear();
				teamList.addAll((List<Team>) results.values);
				notifyDataSetChanged();
			}
		};
	}
}
