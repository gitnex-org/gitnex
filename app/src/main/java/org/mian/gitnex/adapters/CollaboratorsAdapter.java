package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
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
public class CollaboratorsAdapter extends BaseAdapter {

	private final List<User> collaboratorsList;
	private final Context context;

	public CollaboratorsAdapter(Context ctx, List<User> collaboratorsListMain) {

		this.context = ctx;
		this.collaboratorsList = collaboratorsListMain;
	}

	@Override
	public int getCount() {
		return collaboratorsList.size();
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

		ViewHolder viewHolder;

		if (finalView == null) {

			finalView = LayoutInflater.from(context).inflate(R.layout.list_collaborators, null);
			viewHolder = new ViewHolder(finalView);
			finalView.setTag(viewHolder);
		} else {

			viewHolder = (ViewHolder) finalView.getTag();
		}

		initData(viewHolder, position);
		return finalView;
	}

	private void initData(ViewHolder viewHolder, int position) {

		User currentItem = collaboratorsList.get(position);

		Glide.with(context)
				.load(currentItem.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(viewHolder.collaboratorAvatar);

		viewHolder.userLoginId = currentItem.getLogin();

		if (!currentItem.getFullName().isEmpty()) {

			viewHolder.collaboratorName.setText(Html.fromHtml(currentItem.getFullName()));
			viewHolder.userName.setText(
					context.getResources()
							.getString(R.string.usernameWithAt, currentItem.getLogin()));
		} else {

			viewHolder.collaboratorName.setText(currentItem.getLogin());
			viewHolder.userName.setVisibility(View.GONE);
		}
	}

	private class ViewHolder {

		private final ImageView collaboratorAvatar;
		private final TextView collaboratorName;
		private final TextView userName;
		private String userLoginId;

		ViewHolder(View v) {

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
	}
}
