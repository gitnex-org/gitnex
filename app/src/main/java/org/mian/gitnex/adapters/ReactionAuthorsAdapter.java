package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * @author opyale
 */

public class ReactionAuthorsAdapter extends RecyclerView.Adapter<ReactionAuthorsAdapter.ViewHolder> {

	private final Context context;
	private final List<UserInfo> userInfos;

	public ReactionAuthorsAdapter(Context context, List<UserInfo> userInfos) {
		this.context = context;
		this.userInfos = userInfos;
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_reaction_authors, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		UserInfo userInfo = userInfos.get(position);

		PicassoService.getInstance(context).get()
			.load(userInfo.getAvatar())
			.placeholder(R.drawable.loader_animated)
			.resize(240, 240)
			.transform(new RoundedTransformation(AppUtil.getPixelsFromDensity(context, 6), 0))
			.centerCrop().into(holder.authorAvatar);

		if(userInfo.getFullname() == null || userInfo.getFullname().isEmpty()) {
			holder.authorFullName.setVisibility(View.GONE);
		} else {
			holder.authorFullName.setText(userInfo.getFullname());
			holder.authorFullName.setVisibility(View.VISIBLE);
		}

		holder.authorLogin.setText(userInfo.getLogin());
	}

	@Override
	public int getItemCount() {
		return userInfos.size();
	}

	static class ViewHolder extends RecyclerView.ViewHolder {

		private final ImageView authorAvatar;

		private final TextView authorFullName;
		private final TextView authorLogin;

		public ViewHolder(@NonNull View itemView) {
			super(itemView);

			authorAvatar = itemView.findViewById(R.id.authorAvatar);
			authorFullName = itemView.findViewById(R.id.authorFullName);
			authorLogin = itemView.findViewById(R.id.authorLogin);
		}
	}

}
