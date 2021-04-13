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
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileFollowersAdapter extends RecyclerView.Adapter<ProfileFollowersAdapter.FollowersViewHolder> {

    private final List<UserInfo> followersList;
    private final Context context;

    static class FollowersViewHolder extends RecyclerView.ViewHolder {

	    private String userLoginId;

        private final ImageView userAvatar;
        private final TextView userFullName;
        private final TextView userName;

        private FollowersViewHolder(View itemView) {

            super(itemView);

            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);

	        userAvatar.setOnClickListener(loginId -> {

		        Context context = loginId.getContext();

		        AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
	        });
        }
    }

    public ProfileFollowersAdapter(Context ctx, List<UserInfo> followersListMain) {

        this.context = ctx;
        this.followersList = followersListMain;
    }

    @NonNull
    @Override
    public ProfileFollowersAdapter.FollowersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_profile_followers, parent, false);
        return new FollowersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFollowersAdapter.FollowersViewHolder holder, int position) {

        UserInfo currentItem = followersList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

	    holder.userLoginId = currentItem.getLogin();

        if(!currentItem.getFullname().equals("")) {
            holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
            holder.userName.setText(context.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {
            holder.userFullName.setText(currentItem.getUsername());
            holder.userName.setVisibility(View.GONE);
        }

        PicassoService.getInstance(context).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
    }

    @Override
    public int getItemCount() {
        return followersList.size();
    }

}


