package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListOrganizationMembersPreviewBinding;

/**
 * @author opyale
 * @author mmarif
 */
public class OrganizationTeamMembersPreviewAdapter
		extends RecyclerView.Adapter<OrganizationTeamMembersPreviewAdapter.ViewHolder> {

	private final Context context;
	private final List<User> userData;

	public OrganizationTeamMembersPreviewAdapter(Context context, List<User> userInfo) {
		this.context = context;
		this.userData = userInfo;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListOrganizationMembersPreviewBinding binding =
				ListOrganizationMembersPreviewBinding.inflate(
						LayoutInflater.from(context), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User userInfo = userData.get(position);

		RecyclerView.LayoutParams params =
				(RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
		if (position == getItemCount() - 1) {
			params.setMarginEnd(0);
		} else {
			params.setMarginEnd((int) context.getResources().getDimension(R.dimen.negative8dp));
		}
		holder.itemView.setLayoutParams(params);

		Glide.with(context)
				.load(userInfo.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(holder.binding.avatar);
	}

	@Override
	public int getItemCount() {
		return Math.min(userData.size(), 6);
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ListOrganizationMembersPreviewBinding binding;

		public ViewHolder(ListOrganizationMembersPreviewBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
