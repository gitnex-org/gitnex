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
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * @author M M Arif
 */

public class CollaboratorsAdapter extends BaseAdapter {

	private final List<User> collaboratorsList;
	private final Context context;

	private class ViewHolder {

		private String userLoginId;

		private final ImageView collaboratorAvatar;
		private final TextView collaboratorName;
		private final TextView userName;

		ViewHolder(View v) {

			collaboratorAvatar = v.findViewById(R.id.collaboratorAvatar);
			collaboratorName = v.findViewById(R.id.collaboratorName);
			userName = v.findViewById(R.id.userName);

			collaboratorAvatar.setOnClickListener(loginId -> {
				Intent intent = new Intent(context, ProfileActivity.class);
				intent.putExtra("username", userLoginId);
				context.startActivity(intent);
			});

			collaboratorAvatar.setOnLongClickListener(loginId -> {
				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
				return true;
			});
		}

	}

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

		if(finalView == null) {

			finalView = LayoutInflater.from(context).inflate(R.layout.list_collaborators, null);
			viewHolder = new ViewHolder(finalView);
			finalView.setTag(viewHolder);
		}
		else {

			viewHolder = (ViewHolder) finalView.getTag();
		}

		initData(viewHolder, position);
		return finalView;
	}

	private void initData(ViewHolder viewHolder, int position) {

		int imgRadius = AppUtil.getPixelsFromDensity(context, 90);

		User currentItem = collaboratorsList.get(position);
		PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(180, 180).centerCrop()
			.into(viewHolder.collaboratorAvatar);

		viewHolder.userLoginId = currentItem.getLogin();

		if(!currentItem.getFullName().equals("")) {

			viewHolder.collaboratorName.setText(Html.fromHtml(currentItem.getFullName()));
			viewHolder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));
		}
		else {

			viewHolder.collaboratorName.setText(currentItem.getLogin());
			viewHolder.userName.setVisibility(View.GONE);
		}

	}

}
