package org.mian.gitnex.adapters;

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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class OrganizationAddUserToTeamMemberAdapter
		extends RecyclerView.Adapter<OrganizationAddUserToTeamMemberAdapter.UserSearchViewHolder> {

	private final List<User> usersSearchList;
	private final Context context;
	private final int teamId;
	private final String orgName;

	public OrganizationAddUserToTeamMemberAdapter(
			List<User> dataList, Context ctx, int teamId, String orgName) {
		this.context = ctx;
		this.usersSearchList = dataList;
		this.teamId = teamId;
		this.orgName = orgName;
	}

	@NonNull @Override
	public OrganizationAddUserToTeamMemberAdapter.UserSearchViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_collaborators_search, parent, false);
		return new OrganizationAddUserToTeamMemberAdapter.UserSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull final OrganizationAddUserToTeamMemberAdapter.UserSearchViewHolder holder,
			int position) {

		User currentItem = usersSearchList.get(position);
		holder.userInfo = currentItem;

		if (!currentItem.getFullName().isEmpty()) {

			holder.userFullName.setText(Html.fromHtml(currentItem.getFullName()));
		} else {

			holder.userFullName.setText(
					context.getResources()
							.getString(R.string.usernameWithAt, currentItem.getLogin()));
		}

		holder.userName.setText(
				context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));

		if (!currentItem.getAvatarUrl().isEmpty()) {
			Glide.with(context)
					.load(currentItem.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(holder.userAvatar);
		}

		if (getItemCount() > 0) {

			final String loginUid =
					((BaseActivity) context).getAccount().getAccount().getUserName();

			Call<User> call =
					RetrofitClient.getApiInterface(context)
							.orgListTeamMember((long) teamId, currentItem.getLogin());

			call.enqueue(
					new Callback<>() {

						@Override
						public void onResponse(
								@NonNull Call<User> call, @NonNull Response<User> response) {

							if (response.code() == 200) {

								if (!currentItem.getLogin().equals(loginUid)) {
									holder.addMemberButtonRemove.setVisibility(View.VISIBLE);
								} else {
									holder.addMemberButtonRemove.setVisibility(View.GONE);
								}

							} else if (response.code() == 404) {

								if (!currentItem.getLogin().equals(loginUid)) {
									holder.addMemberButtonAdd.setVisibility(View.VISIBLE);
								} else {
									holder.addMemberButtonAdd.setVisibility(View.GONE);
								}

							} else {
								holder.addMemberButtonRemove.setVisibility(View.GONE);
								holder.addMemberButtonAdd.setVisibility(View.GONE);
							}
						}

						@Override
						public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

							Toasty.error(
									context,
									context.getResources()
											.getString(R.string.genericServerResponseError));
						}
					});
		}
	}

	@Override
	public int getItemCount() {
		return usersSearchList.size();
	}

	public class UserSearchViewHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userName;
		private final ImageView addMemberButtonAdd;
		private final ImageView addMemberButtonRemove;
		private User userInfo;

		private UserSearchViewHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			addMemberButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			addMemberButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);

			addMemberButtonAdd.setOnClickListener(
					v ->
							AlertDialogs.addMemberDialog(
									context,
									userInfo.getLogin(),
									Integer.parseInt(String.valueOf(teamId))));
			addMemberButtonRemove.setOnClickListener(
					v ->
							AlertDialogs.removeMemberDialog(
									context,
									userInfo.getLogin(),
									Integer.parseInt(String.valueOf(teamId))));

			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(userInfo.getLogin())) {

									userAvatar.setOnClickListener(
											loginId -> {
												Intent intent =
														new Intent(context, ProfileActivity.class);
												intent.putExtra("username", userInfo.getLogin());
												context.startActivity(intent);
											});

									userAvatar.setOnLongClickListener(
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
	}
}
