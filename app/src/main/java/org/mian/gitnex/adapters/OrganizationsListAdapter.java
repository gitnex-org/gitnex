package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.databinding.ListOrganizationsBinding;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;

/**
 * @author mmarif
 */
public class OrganizationsListAdapter
		extends RecyclerView.Adapter<OrganizationsListAdapter.OrgHolder> implements Filterable {

	private final Context context;
	private List<Organization> orgList;
	private final List<Organization> orgListFull;

	public OrganizationsListAdapter(Context context, List<Organization> orgList) {
		this.context = context;
		this.orgList = orgList;
		this.orgListFull = new ArrayList<>(orgList);
	}

	@NonNull @Override
	public OrgHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListOrganizationsBinding binding =
				ListOrganizationsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new OrgHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull OrgHolder holder, int position) {
		holder.bind(orgList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return orgList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Organization> newList) {
		this.orgList = newList;
		this.orgListFull.clear();
		this.orgListFull.addAll(newList);
		notifyDataSetChanged();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<Organization> filteredList = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filteredList.addAll(orgListFull);
				} else {
					String pattern = constraint.toString().toLowerCase().trim();
					for (Organization item : orgListFull) {
						if (item.getUsername().toLowerCase().contains(pattern)
								|| (item.getDescription() != null
										&& item.getDescription().toLowerCase().contains(pattern))) {
							filteredList.add(item);
						}
					}
				}
				FilterResults results = new FilterResults();
				results.values = filteredList;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				Object values = results.values;
				if (values instanceof List<?> rawList) {
					orgList = new ArrayList<>();
					for (Object item : rawList) {
						if (item instanceof Organization) {
							orgList.add((Organization) item);
						}
					}
				}
				notifyDataSetChanged();
			}
		};
	}

	public class OrgHolder extends RecyclerView.ViewHolder {
		private final ListOrganizationsBinding binding;

		OrgHolder(ListOrganizationsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot()
					.setOnClickListener(
							v -> {
								int pos = getBindingAdapterPosition();
								if (pos != RecyclerView.NO_POSITION) {
									Organization org = orgList.get(pos);
									Intent intent =
											new Intent(context, OrganizationDetailActivity.class);
									intent.putExtra("orgName", org.getUsername());
									context.startActivity(intent);
								}
							});
		}

		void bind(Organization org) {
			binding.orgName.setText(org.getUsername());

			String label = (org.getName() != null) ? org.getName() : org.getFullName();
			Drawable placeholder = AvatarGenerator.getLetterAvatar(context, label, 44);

			Glide.with(context)
					.load(org.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(placeholder)
					.centerCrop()
					.into(binding.imageAvatar);

			if (org.getDescription() != null && !org.getDescription().isEmpty()) {
				binding.orgDescription.setVisibility(View.VISIBLE);
				Markdown.render(
						context,
						EmojiParser.parseToUnicode(org.getDescription()),
						binding.orgDescription);
			} else {
				binding.orgDescription.setVisibility(View.GONE);
			}
		}
	}
}
