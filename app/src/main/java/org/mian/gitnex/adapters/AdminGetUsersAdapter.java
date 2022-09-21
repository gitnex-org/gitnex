package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import java.util.ArrayList;
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
public class AdminGetUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
		implements Filterable {

	private final List<User> usersListFull;
	private final Context context;
	private List<User> usersList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final Filter usersFilter =
			new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					List<User> filteredList = new ArrayList<>();

					if (constraint == null || constraint.length() == 0) {
						filteredList.addAll(usersListFull);
					} else {
						String filterPattern = constraint.toString().toLowerCase().trim();

						for (User item : usersListFull) {
							if (item.getEmail().toLowerCase().contains(filterPattern)
									|| item.getFullName().toLowerCase().contains(filterPattern)
									|| item.getLogin().toLowerCase().contains(filterPattern)) {
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

					usersList.clear();
					usersList.addAll((List) results.values);
					notifyDataChanged();
				}
			};

	public AdminGetUsersAdapter(List<User> usersListMain, Context ctx) {
		this.context = ctx;
		this.usersList = usersListMain;
		usersListFull = new ArrayList<>(usersList);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new AdminGetUsersAdapter.ReposHolder(
				inflater.inflate(R.layout.list_admin_users, parent, false));
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

		((AdminGetUsersAdapter.ReposHolder) holder).bindData(usersList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return usersList.size();
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
		usersList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return usersFilter;
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class ReposHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userEmail;
		private final ImageView userRole;
		private final TextView userName;
		private String userLoginId;

		ReposHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			userEmail = itemView.findViewById(R.id.userEmail);
			userRole = itemView.findViewById(R.id.userRole);

			itemView.setOnClickListener(
					loginId -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", userLoginId);
						context.startActivity(intent);
					});

			userAvatar.setOnLongClickListener(
					loginId -> {
						AppUtil.copyToClipboard(
								context,
								userLoginId,
								context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
						return true;
					});
		}

		void bindData(User users) {

			int imgRadius = AppUtil.getPixelsFromDensity(context, 60);

			userLoginId = users.getLogin();

			if (!users.getFullName().equals("")) {

				userFullName.setText(Html.fromHtml(users.getFullName()));
				userName.setText(
						context.getResources()
								.getString(R.string.usernameWithAt, users.getLogin()));
			} else {

				userFullName.setText(
						context.getResources()
								.getString(R.string.usernameWithAt, users.getLogin()));
				userName.setVisibility(View.GONE);
			}

			if (!users.getEmail().equals("")) {
				userEmail.setText(users.getEmail());
			} else {
				userEmail.setVisibility(View.GONE);
			}

			if (users.isIsAdmin()) {

				userRole.setVisibility(View.VISIBLE);
				TextDrawable drawable =
						TextDrawable.builder()
								.beginConfig()
								.textColor(
										ResourcesCompat.getColor(
												context.getResources(), R.color.colorWhite, null))
								.fontSize(60)
								.width(200)
								.height(80)
								.endConfig()
								.buildRoundRect(
										context.getResources()
												.getString(R.string.userRoleAdmin)
												.toLowerCase(),
										ResourcesCompat.getColor(
												context.getResources(), R.color.releasePre, null),
										8);
				userRole.setImageDrawable(drawable);
			} else {

				userRole.setVisibility(View.GONE);
			}

			PicassoService.getInstance(context)
					.get()
					.load(users.getAvatarUrl())
					.placeholder(R.drawable.loader_animated)
					.transform(new RoundedTransformation(imgRadius, 0))
					.resize(120, 120)
					.centerCrop()
					.into(userAvatar);
		}
	}
}
