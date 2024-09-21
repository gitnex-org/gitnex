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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.helpers.Markdown;

/**
 * @author M M Arif
 */
public class OrganizationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
		implements Filterable {

	private final Context context;
	private final List<Organization> orgListFull;
	private List<Organization> orgList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final Filter orgFilter =
			new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {

					List<Organization> filteredList = new ArrayList<>();

					if (constraint == null || constraint.length() == 0) {

						filteredList.addAll(orgListFull);
					} else {

						String filterPattern = constraint.toString().toLowerCase().trim();

						for (Organization item : orgListFull) {
							if (item.getUsername().toLowerCase().contains(filterPattern)
									|| item.getDescription()
											.toLowerCase()
											.contains(filterPattern)) {
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

	public OrganizationsListAdapter(Context ctx, List<Organization> orgListMain) {
		this.context = ctx;
		this.orgList = orgListMain;
		orgListFull = new ArrayList<>(orgList);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new OrganizationsListAdapter.OrgHolder(
				inflater.inflate(R.layout.list_organizations, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
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

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Organization> list) {
		orgList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return orgFilter;
	}

	public abstract static class OnLoadMoreListener {

		protected abstract void onLoadMore();

		public void onLoadFinished() {}
	}

	class OrgHolder extends RecyclerView.ViewHolder {

		private final ImageView image;
		private final TextView orgName;
		private final TextView orgDescription;
		private Organization userOrganizations;

		OrgHolder(View itemView) {

			super(itemView);
			orgName = itemView.findViewById(R.id.orgName);
			orgDescription = itemView.findViewById(R.id.orgDescription);
			image = itemView.findViewById(R.id.imageAvatar);

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();
						Intent intent = new Intent(context, OrganizationDetailActivity.class);
						intent.putExtra("orgName", userOrganizations.getUsername());
						context.startActivity(intent);
					});
		}

		void bindData(Organization org) {

			this.userOrganizations = org;
			orgName.setText(org.getUsername());

			Glide.with(context)
					.load(org.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(image);

			if (!org.getDescription().isEmpty()) {
				orgDescription.setVisibility(View.VISIBLE);
				Markdown.render(
						context,
						EmojiParser.parseToUnicode(
								Objects.requireNonNull(
										StringUtils.substring(org.getDescription(), 0, 280))),
						orgDescription);
			} else {
				orgDescription.setVisibility(View.GONE);
			}
		}
	}
}
