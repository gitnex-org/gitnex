package org.mian.gitnex.adapters;

import android.content.Context;
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
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class UserSearchForTeamMemberAdapter extends RecyclerView.Adapter<UserSearchForTeamMemberAdapter.UserSearchViewHolder> {

	private List<UserInfo> usersSearchList;
	private Context mCtx;
	private int teamId;

	public UserSearchForTeamMemberAdapter(List<UserInfo> dataList, Context mCtx, int teamId) {
		this.mCtx = mCtx;
		this.usersSearchList = dataList;
		this.teamId = teamId;
	}

	static class UserSearchViewHolder extends RecyclerView.ViewHolder {

		private ImageView userAvatar;
		private TextView userFullName;
		private TextView userName;
		private TextView userNameMain;
		private ImageView addMemberButtonAdd;
		private ImageView addMemberButtonRemove;
		private TextView teamId_;

		private UserSearchViewHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			userNameMain = itemView.findViewById(R.id.userNameMain);
			addMemberButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			addMemberButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);
			teamId_ = itemView.findViewById(R.id.teamId);

			addMemberButtonAdd.setOnClickListener(v -> {

				Context context = v.getContext();

				AlertDialogs.addMemberDialog(context, userNameMain.getText().toString(),
						context.getResources().getString(R.string.addTeamMemberTitle),
						context.getResources().getString(R.string.addTeamMemberMessage),
						context.getResources().getString(R.string.addButton),
						context.getResources().getString(R.string.cancelButton), Integer.parseInt(teamId_.getText().toString()));

			});

			addMemberButtonRemove.setOnClickListener(v -> {

				Context context = v.getContext();

				AlertDialogs.removeMemberDialog(context, userNameMain.getText().toString(),
						context.getResources().getString(R.string.removeTeamMemberTitle),
						context.getResources().getString(R.string.removeTeamMemberMessage),
						context.getResources().getString(R.string.removeButton),
						context.getResources().getString(R.string.cancelButton), Integer.parseInt(teamId_.getText().toString()));

			});

		}

	}

	@NonNull
	@Override
	public UserSearchForTeamMemberAdapter.UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
		return new UserSearchForTeamMemberAdapter.UserSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final UserSearchForTeamMemberAdapter.UserSearchViewHolder holder, int position) {

		final UserInfo currentItem = usersSearchList.get(position);

		holder.userNameMain.setText(currentItem.getLogin());
		holder.teamId_.setText(String.valueOf(teamId));

		if (!currentItem.getFullname().equals("")) {


			holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
		}
		else {

			holder.userFullName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));
		}

		holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));

		if (!currentItem.getAvatar().equals("")) {
			PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
		}

		if(getItemCount() > 0) {

			TinyDB tinyDb = TinyDB.getInstance(mCtx);
			final String loginUid = tinyDb.getString("loginUid");
			String repoFullName = tinyDb.getString("repoFullName");
			String[] parts = repoFullName.split("/");
			final String repoOwner = parts[0];
			final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

			Call<UserInfo> call = RetrofitClient
					.getApiInterface(mCtx)
					.checkTeamMember(Authorization.get(mCtx), teamId, currentItem.getLogin());

			call.enqueue(new Callback<UserInfo>() {

				@Override
				public void onResponse(@NonNull Call<UserInfo> call, @NonNull Response<UserInfo> response) {

					if(response.code() == 200) {

						if(!currentItem.getLogin().equals(loginUid) && !currentItem.getLogin().equals(repoOwner)) {
							holder.addMemberButtonRemove.setVisibility(View.VISIBLE);
						}
						else {
							holder.addMemberButtonRemove.setVisibility(View.GONE);
						}

					}
					else if(response.code() == 404) {

						if(!currentItem.getLogin().equals(loginUid) && !currentItem.getLogin().equals(repoOwner)) {
							holder.addMemberButtonAdd.setVisibility(View.VISIBLE);
						}
						else {
							holder.addMemberButtonAdd.setVisibility(View.GONE);
						}

					}
					else {
						holder.addMemberButtonRemove.setVisibility(View.GONE);
						holder.addMemberButtonAdd.setVisibility(View.GONE);
					}

				}

				@Override
				public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

					Toasty.error(mCtx, mCtx.getResources().getString(R.string.genericServerResponseError));

				}

			});

		}

	}

	@Override
	public int getItemCount() {
		return usersSearchList.size();
	}

}
