<<<<<<< HEAD
=======
package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.UserInfo;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author M M Arif
 */

public class ProfileFollowersAdapter extends RecyclerView.Adapter<ProfileFollowersAdapter.FollowersViewHolder> {

    private List<UserInfo> followersList;
    private Context mCtx;

    static class FollowersViewHolder extends RecyclerView.ViewHolder {

        private ImageView userAvatar;
        private TextView userFullName;
        private TextView userName;

        private FollowersViewHolder(View itemView) {
            super(itemView);

            userAvatar = itemView.findViewById(R.id.userAvatar);
            userFullName = itemView.findViewById(R.id.userFullName);
            userName = itemView.findViewById(R.id.userName);

        }
    }

    public ProfileFollowersAdapter(Context mCtx, List<UserInfo> followersListMain) {
        this.mCtx = mCtx;
        this.followersList = followersListMain;
    }

    @NonNull
    @Override
    public ProfileFollowersAdapter.FollowersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_followers_list, parent, false);
        return new ProfileFollowersAdapter.FollowersViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFollowersAdapter.FollowersViewHolder holder, int position) {

        UserInfo currentItem = followersList.get(position);

        if(!currentItem.getFullname().equals("")) {
            holder.userFullName.setText(currentItem.getFullname());
            holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {
            holder.userFullName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
            holder.userName.setVisibility(View.GONE);
        }

        Picasso.get().load(currentItem.getAvatar()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
    }

    @Override
    public int getItemCount() {
        return followersList.size();
    }

}


>>>>>>> 85140fbee0ff8626da3430c14233d3ecd7f34871
