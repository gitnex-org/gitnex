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
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class AdminGetUsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

	private List<UserInfo> usersList;
	private final List<UserInfo> usersListFull;
	private final Context context;
	private final int TYPE_LOAD = 0;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public AdminGetUsersAdapter(List<UserInfo> usersListMain, Context ctx) {
		this.context = ctx;
		this.usersList = usersListMain;
		usersListFull = new ArrayList<>(usersList);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if(viewType == TYPE_LOAD) {
			return new AdminGetUsersAdapter.ReposHolder(inflater.inflate(R.layout.list_admin_users, parent, false));
		}
		else {
			return new AdminGetUsersAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((AdminGetUsersAdapter.ReposHolder) holder).bindData(usersList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(usersList.get(position).getFullname() != null) {
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

	class ReposHolder extends RecyclerView.ViewHolder {

		private String userLoginId;
		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userEmail;
		private final ImageView userRole;
		private final TextView userName;

		ReposHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			userEmail = itemView.findViewById(R.id.userEmail);
			userRole = itemView.findViewById(R.id.userRole);

			itemView.setOnClickListener(loginId -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", userLoginId);
				context.startActivity(intent);
			});

			userAvatar.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
				return true;
			});
		}

		void bindData(UserInfo users) {

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			userLoginId = users.getLogin();

			if(!users.getFullname().equals("")) {

				userFullName.setText(Html.fromHtml(users.getFullname()));
				userName.setText(context.getResources().getString(R.string.usernameWithAt, users.getUsername()));
			}
			else {

				userFullName.setText(context.getResources().getString(R.string.usernameWithAt, users.getUsername()));
				userName.setVisibility(View.GONE);
			}

			if(!users.getEmail().equals("")) {
				userEmail.setText(users.getEmail());
			}
			else {
				userEmail.setVisibility(View.GONE);
			}

			if(users.getIs_admin()) {

				userRole.setVisibility(View.VISIBLE);
				TextDrawable drawable = TextDrawable.builder().beginConfig()
					.textColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null)).fontSize(44).width(180).height(60)
					.endConfig().buildRoundRect(context.getResources().getString(R.string.userRoleAdmin).toLowerCase(), ResourcesCompat.getColor(context.getResources(), R.color.releasePre, null), 8);
				userRole.setImageDrawable(drawable);
			}
			else {

				userRole.setVisibility(View.GONE);
			}

			PicassoService.getInstance(context).get().load(users.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(userAvatar);
		}
	}

	static class LoadHolder extends RecyclerView.ViewHolder {
		LoadHolder(View itemView) {
			super(itemView);
		}
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if(!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
		void onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserInfo> list) {
		usersList = list;
		notifyDataChanged();
	}

    @Override
    public Filter getFilter() {
        return usersFilter;
    }

    private final Filter usersFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserInfo> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(usersListFull);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserInfo item : usersListFull) {
                    if (item.getEmail().toLowerCase().contains(filterPattern) || item.getFullname().toLowerCase().contains(filterPattern) || item.getUsername().toLowerCase().contains(filterPattern)) {
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
}
