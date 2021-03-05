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

public class ProfileFollowingAdapter extends RecyclerView.Adapter<ProfileFollowingAdapter.FollowingViewHolder> {

    private final List<UserInfo> followingList;
    private final Context mCtx;

    static class FollowingViewHolder extends RecyclerView.ViewHolder {

	    private String userLoginId;

        private final ImageView userAvatar;
        private final TextView userFullName;
        private final TextView userName;

        private FollowingViewHolder(View itemView) {

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

    public ProfileFollowingAdapter(Context mCtx, List<UserInfo> followingListMain) {

        this.mCtx = mCtx;
        this.followingList = followingListMain;
    }

    @NonNull
    @Override
    public ProfileFollowingAdapter.FollowingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_profile_following, parent, false);
        return new FollowingViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileFollowingAdapter.FollowingViewHolder holder, int position) {

        UserInfo currentItem = followingList.get(position);

	    holder.userLoginId = currentItem.getLogin();

        if(!currentItem.getFullname().equals("")) {

            holder.userFullName.setText(Html.fromHtml(currentItem.getFullname()));
            holder.userName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
        }
        else {

            holder.userFullName.setText(mCtx.getResources().getString(R.string.usernameWithAt, currentItem.getUsername()));
            holder.userName.setVisibility(View.GONE);
        }

        PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.userAvatar);
    }

    @Override
    public int getItemCount() {
        return followingList.size();
    }


}
