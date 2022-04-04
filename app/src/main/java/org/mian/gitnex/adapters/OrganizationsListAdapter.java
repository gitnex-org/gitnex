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
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class OrganizationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

	private final Context context;
	private List<UserOrganizations> orgList;
	private final List<UserOrganizations> orgListFull;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public OrganizationsListAdapter(List<UserOrganizations> orgListMain, Context ctx) {
		this.context = ctx;
		this.orgList = orgListMain;
		orgListFull = new ArrayList<>(orgList);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new OrganizationsListAdapter.OrgHolder(inflater.inflate(R.layout.list_organizations, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((OrganizationsListAdapter.OrgHolder) holder).bindData(orgList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return orgList.size();
	}

	class OrgHolder extends RecyclerView.ViewHolder {

		private UserOrganizations userOrganizations;

		private final ImageView image;
		private final TextView orgName;
		private final TextView orgDescription;

		OrgHolder(View itemView) {

			super(itemView);
			orgName = itemView.findViewById(R.id.orgName);
			orgDescription = itemView.findViewById(R.id.orgDescription);
			image = itemView.findViewById(R.id.imageAvatar);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				Intent intent = new Intent(context, OrganizationDetailActivity.class);
				intent.putExtra("orgName", userOrganizations.getUsername());
				context.startActivity(intent);
			});
		}

		void bindData(UserOrganizations org) {

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			this.userOrganizations = org;
			orgName.setText(org.getUsername());

			PicassoService.getInstance(context).get().load(org.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);

			if(!org.getDescription().equals("")) {
				orgDescription.setVisibility(View.VISIBLE);
				orgDescription.setText(org.getDescription());
			}
			else {
				orgDescription.setVisibility(View.GONE);
			}
		}

	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if(!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
		void onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserOrganizations> list) {
		orgList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return orgFilter;
	}

	private final Filter orgFilter = new Filter() {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			List<UserOrganizations> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {

				filteredList.addAll(orgListFull);
			}
			else {

				String filterPattern = constraint.toString().toLowerCase().trim();

				for(UserOrganizations item : orgListFull) {
					if(item.getUsername().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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
			notifyDataChanged();
		}
	};
}
