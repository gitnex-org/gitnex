package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.gitnex.tea4j.models.Collaborators;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.List;

/**
 * Author M M Arif
 */

public class CollaboratorsAdapter extends BaseAdapter  {

    private final List<Collaborators> collaboratorsList;
    private final Context context;

    private class ViewHolder {

	    private String userLoginId;

        private final ImageView collaboratorAvatar;
        private final TextView collaboratorName;

        ViewHolder(View v) {

            collaboratorAvatar  = v.findViewById(R.id.collaboratorAvatar);
            collaboratorName  = v.findViewById(R.id.collaboratorName);

	        collaboratorAvatar.setOnClickListener(loginId -> {
		        Intent intent = new Intent(context, ProfileActivity.class);
		        intent.putExtra("username", userLoginId);
		        context.startActivity(intent);
	        });

	        collaboratorAvatar.setOnLongClickListener(loginId -> {
		        AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
		        return true;
	        });
        }
    }

    public CollaboratorsAdapter(Context ctx, List<Collaborators> collaboratorsListMain) {

        this.context = ctx;
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

            finalView = LayoutInflater.from(context).inflate(R.layout.list_collaborators, null);
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

	    int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

        Collaborators currentItem = collaboratorsList.get(position);
        PicassoService.getInstance(context).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(180, 180).centerCrop().into(viewHolder.collaboratorAvatar);

	    viewHolder.userLoginId = currentItem.getLogin();

        if(!currentItem.getFull_name().equals("")) {

            viewHolder.collaboratorName.setText(Html.fromHtml(currentItem.getFull_name()));
        }
        else {

            viewHolder.collaboratorName.setText(currentItem.getLogin());
        }

    }

}
