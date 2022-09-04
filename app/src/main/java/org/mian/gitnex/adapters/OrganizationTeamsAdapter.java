package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationTeamInfoActivity;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class OrganizationTeamsAdapter extends RecyclerView.Adapter<OrganizationTeamsAdapter.OrgTeamsViewHolder> implements Filterable {

	private final List<Team> teamList;
	private final Context context;
	private final List<Team> teamListFull;
	private final OrganizationPermissions permissions;
	private final String orgName;
	private final Filter orgTeamsFilter = new Filter() {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			List<Team> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {
				filteredList.addAll(teamListFull);
			}
			else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for(Team item : teamListFull) {
					if(item.getName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
						filteredList.add(item);
					}
				}
			}

			FilterResults results = new FilterResults();
			results.values = filteredList;

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			teamList.clear();
			teamList.addAll((List<Team>) results.values);
			notifyDataSetChanged();
		}
	};

	public OrganizationTeamsAdapter(Context ctx, List<Team> teamListMain, OrganizationPermissions permissions, String orgName) {
		this.context = ctx;
		this.teamList = teamListMain;
		this.permissions = permissions;
		teamListFull = new ArrayList<>(teamList);
		this.orgName = orgName;
	}

	@NonNull
	@Override
	public OrganizationTeamsAdapter.OrgTeamsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_organization_teams, parent, false);
		return new OrganizationTeamsAdapter.OrgTeamsViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull OrganizationTeamsAdapter.OrgTeamsViewHolder holder, int position) {

		Team currentItem = teamList.get(position);

		holder.team = currentItem;
		holder.teamTitle.setText(currentItem.getName());
		holder.permissions = permissions;
		holder.orgName = orgName;

		holder.membersPreviewFrame.setVisibility(View.GONE);
		holder.userInfos.clear();
		holder.adapter.notifyDataSetChanged();

		RetrofitClient.getApiInterface(context).orgListTeamMembers(currentItem.getId(), null, null).enqueue(new Callback<List<User>>() {
			@Override
			public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
				if(response.isSuccessful() && response.body() != null && response.body().size() > 0) {

					holder.membersPreviewFrame.setVisibility(View.VISIBLE);
					holder.userInfos.addAll(response.body().stream().limit(Math.min(response.body().size(), 6)).collect(Collectors.toList()));

					holder.adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
			}
		});

		if(currentItem.getDescription() != null && !currentItem.getDescription().isEmpty()) {
			holder.teamDescription.setVisibility(View.VISIBLE);
			holder.teamDescription.setText(currentItem.getDescription());
		}
		else {
			holder.teamDescription.setVisibility(View.GONE);
			holder.teamDescription.setText("");
		}
	}

	@Override
	public int getItemCount() {
		return teamList.size();
	}

	@Override
	public Filter getFilter() {
		return orgTeamsFilter;
	}

	static class OrgTeamsViewHolder extends RecyclerView.ViewHolder {

		private final TextView teamTitle;
		private final TextView teamDescription;
		private final LinearLayout membersPreviewFrame;
		private final List<User> userInfos;
		private final OrganizationTeamMembersPreviewAdapter adapter;
		private Team team;
		private OrganizationPermissions permissions;
		private String orgName;

		private OrgTeamsViewHolder(View itemView) {
			super(itemView);

			teamTitle = itemView.findViewById(R.id.teamTitle);
			teamDescription = itemView.findViewById(R.id.teamDescription);
			membersPreviewFrame = itemView.findViewById(R.id.membersPreviewFrame);

			RecyclerView membersPreview = itemView.findViewById(R.id.membersPreview);

			userInfos = new ArrayList<>();
			adapter = new OrganizationTeamMembersPreviewAdapter(itemView.getContext(), userInfos);

			membersPreview.setLayoutManager(new LinearLayoutManager(itemView.getContext(), RecyclerView.HORIZONTAL, false));
			membersPreview.setAdapter(adapter);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();

				Intent intent = new Intent(context, OrganizationTeamInfoActivity.class);
				intent.putExtra("team", team);
				intent.putExtra("permissions", permissions);
				intent.putExtra("orgName", orgName);
				context.startActivity(intent);
			});
		}

	}

}
