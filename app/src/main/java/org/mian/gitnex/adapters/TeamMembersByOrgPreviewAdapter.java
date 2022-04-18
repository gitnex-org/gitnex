package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * @author opyale
 */

public class TeamMembersByOrgPreviewAdapter extends RecyclerView.Adapter<TeamMembersByOrgPreviewAdapter.ViewHolder> {

	private final Context context;
	private final List<User> userData;

	public TeamMembersByOrgPreviewAdapter(Context context, List<User> userInfo) {
		this.context = context;
		this.userData = userInfo;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(context).inflate(R.layout.list_members_by_org_preview, parent, false);
		return new ViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		User userInfo = userData.get(position);

		PicassoService.getInstance(context).get()
			.load(userInfo.getAvatarUrl())
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(AppUtil.getPixelsFromDensity(context, 3), 0))
			.resize(120, 120)
			.centerCrop().into(holder.avatar);
	}

	@Override
	public int getItemCount() {
		return userData.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);
			avatar = itemView.findViewById(R.id.avatar);
		}
	}
}
