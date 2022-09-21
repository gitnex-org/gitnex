package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;

/**
 * @author M M Arif
 */
public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<User> followersList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public UsersAdapter(List<User> dataList, Context ctx) {
		this.context = ctx;
		this.followersList = dataList;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new UsersAdapter.UsersHolder(inflater.inflate(R.layout.list_users, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((UsersAdapter.UsersHolder) holder).bindData(followersList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return followersList.size();
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

	public void updateList(List<User> list) {
		followersList = list;
		notifyDataChanged();
	}

	class UsersHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userName;
		private User userInfo;

		UsersHolder(View itemView) {
			super(itemView);

			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);

			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(userInfo.getLogin())) {

									itemView.setOnClickListener(
											loginId -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra("username", userInfo.getLogin());
												context.startActivity(intent);
											});

									itemView.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														userInfo.getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																userInfo.getLogin()));
												return true;
											});
								}
							},
							500);
		}

		@SuppressLint("SetTextI18n")
		void bindData(User userInfo) {
			this.userInfo = userInfo;
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			if (!userInfo.getFullName().equals("")) {
				userFullName.setText(Html.fromHtml(userInfo.getFullName()));
				userName.setText(
						context.getResources()
								.getString(R.string.usernameWithAt, userInfo.getLogin()));
			} else {
				userFullName.setText(userInfo.getLogin());
				userName.setVisibility(View.GONE);
			}

			PicassoService.getInstance(context)
					.get()
					.load(userInfo.getAvatarUrl())
					.placeholder(R.drawable.loader_animated)
					.transform(new RoundedTransformation(imgRadius, 0))
					.resize(120, 120)
					.centerCrop()
					.into(userAvatar);
		}
	}
}
