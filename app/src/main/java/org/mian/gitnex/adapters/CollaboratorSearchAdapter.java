package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class CollaboratorSearchAdapter extends RecyclerView.Adapter<CollaboratorSearchAdapter.CollaboratorSearchViewHolder> {

	private final List<User> usersSearchList;
	private final Context context;
	private final RepositoryContext repository;

	public CollaboratorSearchAdapter(List<User> dataList, Context ctx, RepositoryContext repository) {
		this.context = ctx;
		this.usersSearchList = dataList;
		this.repository = repository;
	}

	@NonNull
	@Override
	public CollaboratorSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
		return new CollaboratorSearchAdapter.CollaboratorSearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final CollaboratorSearchViewHolder holder, int position) {

		User currentItem = usersSearchList.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 60);
		holder.userInfo = currentItem;

		if(!currentItem.getFullName().equals("")) {

			holder.userFullName.setText(Html.fromHtml(currentItem.getFullName()));
		}
		else {

			holder.userFullName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));
		}

		holder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getLogin()));

		if(!currentItem.getAvatarUrl().equals("")) {
			PicassoService.getInstance(context).get().load(currentItem.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop()
				.into(holder.userAvatar);
		}

		if(getItemCount() > 0) {

			final String loginUid = ((BaseActivity) context).getAccount().getAccount().getUserName();

			Call<Void> call = RetrofitClient.getApiInterface(context).repoCheckCollaborator(repository.getOwner(), repository.getName(), currentItem.getLogin());

			call.enqueue(new Callback<Void>() {

				@Override
				public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

					if(response.code() == 204) {
						if(!currentItem.getLogin().equals(loginUid) && !currentItem.getLogin().equals(repository.getOwner())) {
							holder.addCollaboratorButtonRemove.setVisibility(View.VISIBLE);
						}
						else {
							holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
						}
					}
					else if(response.code() == 404) {
						if(!currentItem.getLogin().equals(loginUid) && !currentItem.getLogin().equals(repository.getOwner())) {
							holder.addCollaboratorButtonAdd.setVisibility(View.VISIBLE);
						}
						else {
							holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
						}
					}
					else {
						holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
						holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
					}
				}

				@Override
				public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
					Log.i("onFailure", t.toString());
				}
			});
		}
	}

	@Override
	public int getItemCount() {
		return usersSearchList.size();
	}

	class CollaboratorSearchViewHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView userFullName;
		private final TextView userName;
		private final ImageView addCollaboratorButtonAdd;
		private final ImageView addCollaboratorButtonRemove;
		private final String[] permissionList = {"Read", "Write", "Admin"};
		final private int permissionSelectedChoice = 0;
		private User userInfo;

		private CollaboratorSearchViewHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.userAvatar);
			userFullName = itemView.findViewById(R.id.userFullName);
			userName = itemView.findViewById(R.id.userName);
			addCollaboratorButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			addCollaboratorButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);

			addCollaboratorButtonAdd.setOnClickListener(v -> {

				MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context).setTitle(R.string.newTeamPermission).setSingleChoiceItems(permissionList, permissionSelectedChoice, null)
					.setNeutralButton(R.string.cancelButton, null).setPositiveButton(R.string.addButton, (dialog, which) -> {

						ListView lw = ((AlertDialog) dialog).getListView();
						Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

						CollaboratorActions.addCollaborator(context, String.valueOf(checkedItem).toLowerCase(), userInfo.getLogin(), repository);
					});

				materialAlertDialogBuilder.create().show();
			});

			addCollaboratorButtonRemove.setOnClickListener(v -> AlertDialogs.collaboratorRemoveDialog(context, userInfo.getLogin(), repository));

			new Handler().postDelayed(() -> {
				if(!AppUtil.checkGhostUsers(userInfo.getLogin())) {

					userAvatar.setOnClickListener(loginId -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", userInfo.getLogin());
						context.startActivity(intent);
					});

					userAvatar.setOnLongClickListener(loginId -> {
						AppUtil.copyToClipboard(context, userInfo.getLogin(), context.getString(R.string.copyLoginIdToClipBoard, userInfo.getLogin()));
						return true;
					});
				}
			}, 500);
		}

	}

}
