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
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class CollaboratorsAdapter extends BaseAdapter  {

    private List<Collaborators> collaboratorsList;
    private Context mCtx;

    private class ViewHolder {

        private ImageView collaboratorAvatar;
        private TextView collaboratorName;

        ViewHolder(View v) {
            collaboratorAvatar  = v.findViewById(R.id.collaboratorAvatar);
            collaboratorName  = v.findViewById(R.id.collaboratorName);
        }
    }

    public CollaboratorsAdapter(Context mCtx, List<Collaborators> collaboratorsListMain) {
        this.mCtx = mCtx;
        this.collaboratorsList = collaboratorsListMain;
    }

    @Override
    public int getCount() {
        return collaboratorsList.size();
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

        ViewHolder viewHolder = null;

        if (finalView == null) {
            finalView = LayoutInflater.from(mCtx).inflate(R.layout.list_collaborators, null);
            viewHolder = new ViewHolder(finalView);
            finalView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) finalView.getTag();
        }

        initData(viewHolder, position);
        return finalView;

    }

    private void initData(ViewHolder viewHolder, int position) {

        Collaborators currentItem = collaboratorsList.get(position);
        Picasso.get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(180, 180).centerCrop().into(viewHolder.collaboratorAvatar);

        if(!currentItem.getFull_name().equals("")) {
            viewHolder.collaboratorName.setText(currentItem.getFull_name());
        }
        else {
            viewHolder.collaboratorName.setText(currentItem.getLogin());
        }

    }

}
