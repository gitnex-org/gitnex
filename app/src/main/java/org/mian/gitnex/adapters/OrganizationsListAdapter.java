package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.UserOrganizations;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class OrganizationsListAdapter extends RecyclerView.Adapter<OrganizationsListAdapter.OrganizationsViewHolder> implements Filterable {

    private final List<UserOrganizations> orgList;
    private final Context context;
    private final List<UserOrganizations> orgListFull;

    static class OrganizationsViewHolder extends RecyclerView.ViewHolder {

    	private UserOrganizations userOrganizations;

        private final ImageView image;
        private final TextView orgName;
        private final TextView orgDescription;

        private OrganizationsViewHolder(View itemView) {
            super(itemView);
	        orgName = itemView.findViewById(R.id.orgName);
	        orgDescription = itemView.findViewById(R.id.orgDescription);
            image = itemView.findViewById(R.id.imageAvatar);

            itemView.setOnClickListener(v -> {

                Context context = v.getContext();
                Intent intent = new Intent(context, OrganizationDetailActivity.class);
                intent.putExtra("orgName", userOrganizations.getUsername());

                TinyDB tinyDb = TinyDB.getInstance(context);
                tinyDb.putString("orgName", userOrganizations.getUsername());
                tinyDb.putString("organizationId", String.valueOf(userOrganizations.getId()));
                tinyDb.putBoolean("organizationAction", true);
                context.startActivity(intent);
            });
        }
    }

    public OrganizationsListAdapter(Context ctx, List<UserOrganizations> orgsListMain) {

        this.context = ctx;
        this.orgList = orgsListMain;
        orgListFull = new ArrayList<>(orgList);
    }

    @NonNull
    @Override
    public OrganizationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_organizations, parent, false);
        return new OrganizationsViewHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull OrganizationsViewHolder holder, int position) {

        UserOrganizations currentItem = orgList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

	    holder.userOrganizations = currentItem;
	    holder.orgName.setText(currentItem.getUsername());

        PicassoService.getInstance(context).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.image);

	    if(!currentItem.getDescription().equals("")) {
		    holder.orgDescription.setVisibility(View.VISIBLE);
		    holder.orgDescription.setText(currentItem.getDescription());
	    }
	    else {
		    holder.orgDescription.setVisibility(View.GONE);
	    }
    }

    @Override
    public int getItemCount() {
        return orgList.size();
    }

    @Override
    public Filter getFilter() {
        return orgFilter;
    }

    private final Filter orgFilter = new Filter() {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<UserOrganizations> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {

                filteredList.addAll(orgListFull);
            }
            else {

                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserOrganizations item : orgListFull) {
                    if (item.getUsername().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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

            orgList.clear();
            orgList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
