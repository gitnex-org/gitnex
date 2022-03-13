package org.mian.gitnex.adapters.profile;

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
import org.gitnex.tea4j.models.UserOrganizations;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class OrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<UserOrganizations> organizationsList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public OrganizationsAdapter(Context ctx, List<UserOrganizations> organizationsListMain) {
		this.context = ctx;
		this.organizationsList = organizationsListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new OrganizationsHolder(inflater.inflate(R.layout.list_organizations, parent, false));
		}
		else {
			return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((OrganizationsHolder) holder).bindData(organizationsList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(organizationsList.get(position).getUsername() != null) {
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

		private UserOrganizations userOrganizations;

		private final ImageView image;
		private final TextView orgName;
		private final TextView orgDescription;

		OrganizationsHolder(View itemView) {

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

		@SuppressLint("SetTextI18n")
		void bindData(UserOrganizations userOrganizations) {

			this.userOrganizations = userOrganizations;
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			orgName.setText(userOrganizations.getUsername());

			PicassoService.getInstance(context).get().load(userOrganizations.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);

			if (!userOrganizations.getDescription().equals("")) {
				orgDescription.setText(userOrganizations.getDescription());
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

	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public void setLoadMoreListener(Runnable loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserOrganizations> list) {
		organizationsList = list;
		notifyDataSetChanged();
	}
}
