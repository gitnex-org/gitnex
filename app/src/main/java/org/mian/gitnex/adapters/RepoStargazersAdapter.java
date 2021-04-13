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
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepoStargazersAdapter extends BaseAdapter {

    private final List<UserInfo> stargazersList;
    private final Context context;

    private static class ViewHolder {

        private final ImageView memberAvatar;
        private final TextView memberName;

        ViewHolder(View v) {
            memberAvatar  = v.findViewById(R.id.memberAvatar);
            memberName  = v.findViewById(R.id.memberName);
        }
    }

    public RepoStargazersAdapter(Context ctx, List<UserInfo> membersListMain) {
        this.context = ctx;
        this.stargazersList = membersListMain;
    }

    @Override
    public int getCount() {
        return stargazersList.size();
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

        RepoStargazersAdapter.ViewHolder viewHolder;

        if (finalView == null) {
            finalView = LayoutInflater.from(context).inflate(R.layout.list_repo_stargazers, null);
            viewHolder = new ViewHolder(finalView);
            finalView.setTag(viewHolder);
        }
        else {
            viewHolder = (RepoStargazersAdapter.ViewHolder) finalView.getTag();
        }

        initData(viewHolder, position);
        return finalView;

    }

    private void initData(RepoStargazersAdapter.ViewHolder viewHolder, int position) {

        UserInfo currentItem = stargazersList.get(position);
	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

        PicassoService.getInstance(context).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(180, 180).centerCrop().into(viewHolder.memberAvatar);

        final TinyDB tinyDb = TinyDB.getInstance(context);
        Typeface myTypeface;

        switch(tinyDb.getInt("customFontId", -1)) {

            case 0:
                myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/roboto.ttf");
                break;

            case 2:
                myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/sourcecodeproregular.ttf");
                break;

            default:
                myTypeface = Typeface.createFromAsset(context.getAssets(), "fonts/manroperegular.ttf");
                break;

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
