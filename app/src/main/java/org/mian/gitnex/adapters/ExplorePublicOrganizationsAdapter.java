package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class ExplorePublicOrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<Organization> organizationsList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public ExplorePublicOrganizationsAdapter(Context ctx, List<Organization> organizationsListMain) {
		this.context = ctx;
		this.organizationsList = organizationsListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if(viewType == TYPE_LOAD) {
			return new ExplorePublicOrganizationsAdapter.OrganizationsHolder(inflater.inflate(R.layout.list_organizations, parent, false));
		}
		else {
			return new ExplorePublicOrganizationsAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((ExplorePublicOrganizationsAdapter.OrganizationsHolder) holder).bindData(organizationsList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(organizationsList.get(position).getFull_name() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {
		return organizationsList.size();
	}

	class OrganizationsHolder extends RecyclerView.ViewHolder {
		private Organization organization;
		private final ImageView image;
		private final TextView orgName;
		private final TextView orgDescription;

		OrganizationsHolder(View itemView) {
			super(itemView);
			image = itemView.findViewById(R.id.imageAvatar);
			orgName = itemView.findViewById(R.id.orgName);
			orgDescription = itemView.findViewById(R.id.orgDescription);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				Intent intent = new Intent(context, OrganizationDetailActivity.class);
				intent.putExtra("orgName", organization.getUsername());

				TinyDB tinyDb = TinyDB.getInstance(context);
				tinyDb.putString("orgName", organization.getUsername());
				tinyDb.putString("organizationId", String.valueOf(organization.getId()));
				tinyDb.putBoolean("organizationAction", true);
				context.startActivity(intent);
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(Organization organization) {
			this.organization = organization;
		    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);
			orgName.setText(organization.getUsername());
	        PicassoService.getInstance(context).get()
		        .load(organization.getAvatar_url())
		        .placeholder(R.drawable.loader_animated)
		        .transform(new RoundedTransformation(imgRadius, 0))
		        .resize(120, 120)
		        .centerCrop()
		        .into(image);

			if(!organization.getDescription().equals("")) {
				orgDescription.setVisibility(View.VISIBLE);
				orgDescription.setText(organization.getDescription());
			}
			else {
				orgDescription.setVisibility(View.GONE);
			}
		}
	}

	static class LoadHolder extends RecyclerView.ViewHolder {
		LoadHolder(View itemView) {
			super(itemView);
		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Organization> list) {
		organizationsList = list;
		notifyDataChanged();
	}
}
