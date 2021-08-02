package org.mian.gitnex.adapters.profile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class FollowingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<UserInfo> usersList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public FollowingAdapter(Context ctx, List<UserInfo> usersListMain) {
		this.context = ctx;
		this.usersList = usersListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(context);

		if(viewType == TYPE_LOAD) {
			return new UsersHolder(inflater.inflate(R.layout.list_profile_followers_following, parent, false));
		}
		else {
			return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((UsersHolder) holder).bindData(usersList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(usersList.get(position).getUsername() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {
		return usersList.size();
	}

	class UsersHolder extends RecyclerView.ViewHolder {

		private UserInfo userInfo;

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userName;

		UsersHolder(View itemView) {

			super(itemView);
			Context context = itemView.getContext();

			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);

			itemView.setOnClickListener(loginId -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", userInfo.getLogin());
				context.startActivity(intent);
			});

			itemView.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, userInfo.getLogin(), context.getString(R.string.copyLoginIdToClipBoard, userInfo.getLogin()));
				return true;
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(UserInfo userInfo) {

			this.userInfo = userInfo;
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			//Locale locale = context.getResources().getConfiguration().locale;

			if(!userInfo.getFullname().equals("")) {
				userFullName.setText(Html.fromHtml(userInfo.getFullname()));
				userName.setText(context.getResources().getString(R.string.usernameWithAt, userInfo.getUsername()));
			}
			else {
				userFullName.setText(userInfo.getUsername());
				userName.setVisibility(View.GONE);
			}

			PicassoService
				.getInstance(context)
				.get()
				.load(userInfo.getAvatar())
				.placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(imgRadius, 0))
				.resize(120, 120)
				.centerCrop()
				.into(userAvatar);
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

	public interface OnLoadMoreListener {
		void onLoadMore();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserInfo> list) {
		usersList = list;
		notifyDataSetChanged();
	}
}
