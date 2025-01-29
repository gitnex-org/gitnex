package org.mian.gitnex.adapters;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author M M Arif
 */
public class UserGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<User> membersList;
	private final Context context;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public UserGridAdapter(Context ctx, List<User> membersListMain) {

		this.context = ctx;
		this.membersList = membersListMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new UserGridAdapter.DataHolder(
				inflater.inflate(R.layout.list_users_grid, parent, false));
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
		((UserGridAdapter.DataHolder) holder).bindData(membersList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return membersList.size();
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

	public void updateList(List<User> list) {
		membersList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class DataHolder extends RecyclerView.ViewHolder {

		private String userLoginId;
		private final ImageView memberAvatar;
		private final TextView memberName;
		private final TextView userName;

		DataHolder(View v) {

			super(v);

			memberAvatar = v.findViewById(R.id.userAvatarImageView);
			memberName = v.findViewById(R.id.userNameTv);
			userName = v.findViewById(R.id.userName);

			v.setOnClickListener(
					loginId -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", userLoginId);
						context.startActivity(intent);
					});

			v.setOnLongClickListener(
					loginId -> {
						AppUtil.copyToClipboard(
								context,
								userLoginId,
								context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
						return true;
					});
		}

		void bindData(User dataModel) {

			Glide.with(context)
					.load(dataModel.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(memberAvatar);

			userLoginId = dataModel.getLogin();

			if (!dataModel.getFullName().isEmpty()) {

				memberName.setText(Html.fromHtml(dataModel.getFullName()));
				userName.setText(
						context.getResources()
								.getString(R.string.usernameWithAt, dataModel.getLogin()));
			} else {

				memberName.setText(dataModel.getLogin());
				userName.setVisibility(View.GONE);
			}
		}
	}
}
