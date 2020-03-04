package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
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
import org.mian.gitnex.util.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoWatchersAdapter extends BaseAdapter {

    private List<UserInfo> watchersList;
    private Context mCtx;

    private static class ViewHolder {

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
            viewHolder = new ViewHolder(finalView);
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
        Picasso.get().load(currentItem.getAvatar()).transform(new RoundedTransformation(8, 0)).resize(180, 180).centerCrop().into(viewHolder.memberAvatar);

        final TinyDB tinyDb = new TinyDB(mCtx);
        Typeface myTypeface;
        if(tinyDb.getInt("customFontId") == 0) {

            myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/roboto.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 1) {

            myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/manroperegular.ttf");

        }
        else if (tinyDb.getInt("customFontId") == 2) {

            myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/sourcecodeproregular.ttf");

        }
        else {

            myTypeface = Typeface.createFromAsset(mCtx.getAssets(), "fonts/roboto.ttf");

        }

        if(!currentItem.getFullname().equals("")) {
            viewHolder.memberName.setText(currentItem.getFullname());
            viewHolder.memberName.setTypeface(myTypeface);
        }
        else {
            viewHolder.memberName.setText(currentItem.getLogin());
            viewHolder.memberName.setTypeface(myTypeface);
        }

    }

}
