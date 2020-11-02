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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.UserOrganizations;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class OrganizationsListAdapter extends RecyclerView.Adapter<OrganizationsListAdapter.OrganizationsViewHolder> implements Filterable {

    private List<UserOrganizations> orgList;
    private Context mCtx;
    private List<UserOrganizations> orgListFull;

    static class OrganizationsViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView mTextView1;
        private TextView mTextView2;
        private TextView organizationId;

        private OrganizationsViewHolder(View itemView) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.orgUsername);
            mTextView2 = itemView.findViewById(R.id.orgDescription);
            image = itemView.findViewById(R.id.imageAvatar);
            organizationId = itemView.findViewById(R.id.organizationId);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    Intent intent = new Intent(context, OrganizationDetailActivity.class);
                    intent.putExtra("orgName", mTextView1.getText().toString());

                    TinyDB tinyDb = TinyDB.getInstance(context);
                    tinyDb.putString("orgName", mTextView1.getText().toString());
                    tinyDb.putString("organizationId", organizationId.getText().toString());
                    tinyDb.putBoolean("organizationAction", true);
                    context.startActivity(intent);

                }
            });

        }
    }

    public OrganizationsListAdapter(Context mCtx, List<UserOrganizations> orgsListMain) {
        this.mCtx = mCtx;
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
        holder.mTextView2.setVisibility(View.GONE);
        holder.organizationId.setText(Integer.toString(currentItem.getId()));

        PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.image);
        holder.mTextView1.setText(currentItem.getUsername());
        if (!currentItem.getDescription().equals("")) {
            holder.mTextView2.setVisibility(View.VISIBLE);
            holder.mTextView2.setText(currentItem.getDescription());
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

    private Filter orgFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserOrganizations> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(orgListFull);
            } else {
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
