package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.UserInfo;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoWatchersAdapter extends BaseAdapter {

    private List<UserInfo> watchersList;
    private Context mCtx;

    private class ViewHolder {

        private ImageView memberAvatar;
        private TextView memberName;

        ViewHolder(View v) {
            memberAvatar  = v.findViewById(R.id.memberAvatar);
            memberName  = v.findViewById(R.id.memberName);
        }
    }

    public RepoWatchersAdapter(Context mCtx, List<UserInfo> membersListMain) {
        this.mCtx = mCtx;
        this.watchersList = membersListMain;
    }

    @Override
    public int getCount() {
        return watchersList.size();
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

        RepoWatchersAdapter.ViewHolder viewHolder;

        if (finalView == null) {
            finalView = LayoutInflater.from(mCtx).inflate(R.layout.repo_watchers_list, null);
            viewHolder = new RepoWatchersAdapter.ViewHolder(finalView);
            finalView.setTag(viewHolder);
        }
        else {
            viewHolder = (RepoWatchersAdapter.ViewHolder) finalView.getTag();
        }

        initData(viewHolder, position);
        return finalView;

    }

    private void initData(RepoWatchersAdapter.ViewHolder viewHolder, int position) {

        UserInfo currentItem = watchersList.get(position);
        Picasso.get().load(currentItem.getAvatar()).transform(new RoundedTransformation(100, 0)).resize(200, 200).centerCrop().into(viewHolder.memberAvatar);

        if(!currentItem.getFullname().equals("")) {
            viewHolder.memberName.setText(currentItem.getFullname());
        }
        else {
            viewHolder.memberName.setText(currentItem.getLogin());
        }

    }

}
