package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import org.gitnex.tea4j.v2.models.User;
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

public class UserGridAdapter extends BaseAdapter implements Filterable {

	private final List<User> membersList;
	private final Context context;
	private final List<User> membersListFull;

	private class ViewHolder {

		private String userLoginId;

		private final ImageView memberAvatar;
		private final TextView memberName;
		private final TextView userName;

		ViewHolder(View v) {

			memberAvatar = v.findViewById(R.id.userAvatarImageView);
			memberName = v.findViewById(R.id.userNameTv);
			userName = v.findViewById(R.id.userName);

			v.setOnClickListener(loginId -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", userLoginId);
				context.startActivity(intent);
			});

			v.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
				return true;
			});
		}

	}

	public UserGridAdapter(Context ctx, List<User> membersListMain) {

		this.context = ctx;
		this.membersList = membersListMain;
		membersListFull = new ArrayList<>(membersList);
	}

	@Override
	public int getCount() {
		return membersList.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View finalView, ViewGroup parent) {

		UserGridAdapter.ViewHolder viewHolder;

		if(finalView == null) {

			finalView = LayoutInflater.from(context).inflate(R.layout.list_users_grid, null);
			viewHolder = new ViewHolder(finalView);
			finalView.setTag(viewHolder);
		}
		else {

			viewHolder = (UserGridAdapter.ViewHolder) finalView.getTag();
		}

		initData(viewHolder, position);
		return finalView;
	}

	private void initData(UserGridAdapter.ViewHolder viewHolder, int position) {

		User currentItem = membersList.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 0);

		PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop()
			.into(viewHolder.memberAvatar);

		viewHolder.userLoginId = currentItem.getLogin();

		if(!currentItem.getFullName().equals("")) {

			viewHolder.memberName.setText(Html.fromHtml(currentItem.getFullName()));
			viewHolder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));
		}
		else {

			viewHolder.memberName.setText(currentItem.getLogin());
			viewHolder.userName.setVisibility(View.GONE);
		}
	}

	@Override
	public Filter getFilter() {
		return membersFilter;
	}

	private final Filter membersFilter = new Filter() {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			List<User> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {

				filteredList.addAll(membersListFull);
			}
			else {

				String filterPattern = constraint.toString().toLowerCase().trim();

				for(User item : membersListFull) {
					if(item.getFullName().toLowerCase().contains(filterPattern) || item.getLogin().toLowerCase().contains(filterPattern)) {
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

			membersList.clear();
			membersList.addAll((List) results.values);
			notifyDataSetChanged();
		}
	};

}
