package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserSearchViewHolder> {

    private List<UserInfo> usersSearchList;
    private Context mCtx;

    public UserSearchAdapter(List<UserInfo> dataList, Context mCtx) {
        this.mCtx = mCtx;
        this.usersSearchList = dataList;
    }

    static class UserSearchViewHolder extends RecyclerView.ViewHolder {

        private ImageView userAvatar;
        private TextView userFullName;
        private TextView userName;
        private TextView userNameMain;
        private ImageView addCollaboratorButtonAdd;
        private ImageView addCollaboratorButtonRemove;

        private String[] permissionList = {"Read", "Write", "Admin"};
        final private int permissionSelectedChoice = 0;

        private UserSearchViewHolder(View itemView) {
            super(itemView);
            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);
            userNameMain = itemView.findViewById(R.id.userNameMain);
            addCollaboratorButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
            addCollaboratorButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);

            addCollaboratorButtonAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Context context = v.getContext();

                    AlertDialog.Builder pBuilder = new AlertDialog.Builder(context);

                    pBuilder.setTitle(R.string.newTeamPermission);
                    pBuilder.setSingleChoiceItems(permissionList, permissionSelectedChoice, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancelButton, null)
                            .setPositiveButton(R.string.addButton, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    ListView lw = ((AlertDialog)dialog).getListView();
                                    Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());

                                    CollaboratorActions.addCollaborator(context,  String.valueOf(checkedItem).toLowerCase(), userNameMain.getText().toString());

                                }
                            });

                    AlertDialog pDialog = pBuilder.create();
                    pDialog.show();

                }
            });

            addCollaboratorButtonRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    AlertDialogs.collaboratorRemoveDialog(context, userNameMain.getText().toString(),
                            context.getResources().getString(R.string.removeCollaboratorTitle),
                            context.getResources().getString(R.string.removeCollaboratorMessage),
                            context.getResources().getString(R.string.removeButton),
                            context.getResources().getString(R.string.cancelButton), "fa");

                }
            });

        }

    }

    @NonNull
    @Override
    public UserSearchAdapter.UserSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_collaborators_search, parent, false);
        return new UserSearchAdapter.UserSearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserSearchAdapter.UserSearchViewHolder holder, int position) {

        final UserInfo currentItem = usersSearchList.get(position);

        holder.userNameMain.setText(currentItem.getUsername());

        if (!currentItem.getFullname().equals("")) {
            holder.userFullName.setText(currentItem.getFullname());
            holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {
            holder.userFullName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
            holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }

        if (!currentItem.getAvatar().equals("")) {
            Picasso.get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
        }

        if(getItemCount() > 0) {

            TinyDB tinyDb = new TinyDB(mCtx);
            final String instanceUrl = tinyDb.getString("instanceUrl");
            final String loginUid = tinyDb.getString("loginUid");
            String repoFullName = tinyDb.getString("repoFullName");
            String[] parts = repoFullName.split("/");
            final String repoOwner = parts[0];
            final String repoName = parts[1];
            final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

            Call<Collaborators> call = RetrofitClient
                    .getInstance(instanceUrl, mCtx)
                    .getApiInterface()
                    .checkRepoCollaborator(Authorization.returnAuthentication(mCtx, loginUid, instanceToken), repoOwner, repoName, currentItem.getUsername());

            call.enqueue(new Callback<Collaborators>() {

                @Override
                public void onResponse(@NonNull Call<Collaborators> call, @NonNull Response<Collaborators> response) {

                    if(response.code() == 204) {
                        if(!currentItem.getUsername().equals(loginUid) && !currentItem.getUsername().equals(repoOwner)) {
                            holder.addCollaboratorButtonRemove.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
                        }
                    }
                    else if(response.code() == 404) {
                        if(!currentItem.getUsername().equals(loginUid) && !currentItem.getUsername().equals(repoOwner)) {
                            holder.addCollaboratorButtonAdd.setVisibility(View.VISIBLE);
                        }
                        else {
                            holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
                        }
                    }
                    else {
                        holder.addCollaboratorButtonRemove.setVisibility(View.GONE);
                        holder.addCollaboratorButtonAdd.setVisibility(View.GONE);
                        Log.i("onResponse", String.valueOf(response.code()));
                    }

                }

                @Override
                public void onFailure(@NonNull Call<Collaborators> call, @NonNull Throwable t) {
                    Log.i("onFailure", t.toString());
                }

            });

        }

    }

    @Override
    public int getItemCount() {
        return usersSearchList.size();
    }
}
