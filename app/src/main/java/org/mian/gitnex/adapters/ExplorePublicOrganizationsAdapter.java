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
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * @author M M Arif
 */

public class ExplorePublicOrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Organization> organizationsList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public ExplorePublicOrganizationsAdapter(Context ctx, List<Organization> organizationsListMain) {
		this.context = ctx;
		this.organizationsList = organizationsListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ExplorePublicOrganizationsAdapter.OrganizationsHolder(inflater.inflate(R.layout.list_organizations, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((ExplorePublicOrganizationsAdapter.OrganizationsHolder) holder).bindData(organizationsList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
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
				context.startActivity(intent);
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(Organization organization) {
			this.organization = organization;
		    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);
			orgName.setText(organization.getUsername());
	        PicassoService.getInstance(context).get()
		        .load(organization.getAvatarUrl())
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

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public void setLoadMoreListener(Runnable loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Organization> list) {
		organizationsList = list;
		notifyDataChanged();
	}
}
