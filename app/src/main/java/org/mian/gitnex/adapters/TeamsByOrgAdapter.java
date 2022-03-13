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
import org.gitnex.tea4j.models.OrgPermissions;
import org.gitnex.tea4j.models.Teams;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.OrganizationTeamInfoActivity;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class TeamsByOrgAdapter extends RecyclerView.Adapter<TeamsByOrgAdapter.OrgTeamsViewHolder> implements Filterable {

    private final List<Teams> teamList;
    private final Context context;
    private final List<Teams> teamListFull;
    private final OrgPermissions permissions;
    private final String orgName;

    static class OrgTeamsViewHolder extends RecyclerView.ViewHolder {

    	private Teams team;

    	private OrgPermissions permissions;
        private final TextView teamTitle;
        private final TextView teamDescription;
        private final LinearLayout membersPreviewFrame;

	    private final List<UserInfo> userInfos;
        private final TeamMembersByOrgPreviewAdapter adapter;
        private String orgName;

        private OrgTeamsViewHolder(View itemView) {
            super(itemView);

            teamTitle = itemView.findViewById(R.id.teamTitle);
            teamDescription = itemView.findViewById(R.id.teamDescription);
            membersPreviewFrame = itemView.findViewById(R.id.membersPreviewFrame);

	        RecyclerView membersPreview = itemView.findViewById(R.id.membersPreview);

	        userInfos = new ArrayList<>();
            adapter = new TeamMembersByOrgPreviewAdapter(itemView.getContext(), userInfos);

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

    public TeamsByOrgAdapter(Context ctx, List<Teams> teamListMain, OrgPermissions permissions, String orgName) {
        this.context = ctx;
        this.teamList = teamListMain;
        this.permissions = permissions;
        teamListFull = new ArrayList<>(teamList);
        this.orgName = orgName;
    }

    @NonNull
    @Override
    public TeamsByOrgAdapter.OrgTeamsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_teams_by_org, parent, false);
        return new TeamsByOrgAdapter.OrgTeamsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamsByOrgAdapter.OrgTeamsViewHolder holder, int position) {

        Teams currentItem = teamList.get(position);

        holder.team = currentItem;
        holder.teamTitle.setText(currentItem.getName());
        holder.permissions = permissions;
        holder.orgName = orgName;

	    holder.membersPreviewFrame.setVisibility(View.GONE);
	    holder.userInfos.clear();
	    holder.adapter.notifyDataSetChanged();

	    RetrofitClient.getApiInterface(context)
		    .getTeamMembersByOrg(((BaseActivity) context).getAccount().getAuthorization(), currentItem.getId())
		    .enqueue(new Callback<List<UserInfo>>() {
			    @Override
			    public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
				    if(response.isSuccessful() &&
					    response.body() != null &&
					    response.body().size() > 0) {

					    holder.membersPreviewFrame.setVisibility(View.VISIBLE);
					    holder.userInfos.addAll(response.body().stream()
						    .limit(Math.min(response.body().size(), 6))
						    .collect(Collectors.toList()));

					    holder.adapter.notifyDataSetChanged();
				    }
			    }

			    @Override public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {}
	    });

        if (currentItem.getDescription() != null && !currentItem.getDescription().isEmpty()) {
            holder.teamDescription.setVisibility(View.VISIBLE);
            holder.teamDescription.setText(currentItem.getDescription());
        } else {
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

    private final Filter orgTeamsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Teams> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(teamListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Teams item : teamListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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
            teamList.addAll((List<Teams>) results.values);
            notifyDataSetChanged();
        }
    };

}
