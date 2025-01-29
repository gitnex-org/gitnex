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
public class CollaboratorsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private List<User> collaboratorsList;
	private final Context context;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public CollaboratorsAdapter(Context ctx, List<User> collaboratorsListMain) {

		this.context = ctx;
		this.collaboratorsList = collaboratorsListMain;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new CollaboratorsAdapter.DataHolder(
				inflater.inflate(R.layout.list_collaborators, parent, false));
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
		((CollaboratorsAdapter.DataHolder) holder).bindData(collaboratorsList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return collaboratorsList.size();
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
		collaboratorsList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class DataHolder extends RecyclerView.ViewHolder {

		private final ImageView collaboratorAvatar;
		private final TextView collaboratorName;
		private final TextView userName;
		private String userLoginId;

		DataHolder(View v) {
			super(v);

			collaboratorAvatar = v.findViewById(R.id.collaboratorAvatar);
			collaboratorName = v.findViewById(R.id.collaboratorName);
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
					.into(collaboratorAvatar);

			userLoginId = dataModel.getLogin();

			if (!dataModel.getFullName().isEmpty()) {

				collaboratorName.setText(Html.fromHtml(dataModel.getFullName()));
				userName.setText(
						context.getResources()
								.getString(R.string.usernameWithAt, dataModel.getLogin()));
			} else {

				collaboratorName.setText(dataModel.getLogin());
				userName.setVisibility(View.GONE);
			}
		}
	}
}
