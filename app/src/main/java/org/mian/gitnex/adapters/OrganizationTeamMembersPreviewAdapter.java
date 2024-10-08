package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;

/**
 * @author opyale
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
		View v =
				LayoutInflater.from(context)
						.inflate(R.layout.list_organization_members_preview, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User userInfo = userData.get(position);

		Glide.with(context)
				.load(userInfo.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(holder.avatar);
	}

	@Override
	public int getItemCount() {
		return userData.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			avatar = itemView.findViewById(R.id.avatar);
		}
	}
}
