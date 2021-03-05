package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class MembersByOrgAdapter extends BaseAdapter implements Filterable {

    private final List<UserInfo> membersList;
    private final Context mCtx;
    private final List<UserInfo> membersListFull;

    private static class ViewHolder {

	    private String userLoginId;

        private final ImageView memberAvatar;
        private final TextView memberName;

        ViewHolder(View v) {

            memberAvatar  = v.findViewById(R.id.memberAvatar);
            memberName  = v.findViewById(R.id.memberName);

	        memberAvatar.setOnClickListener(loginId -> {

		        Context context = loginId.getContext();

		        AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
	        });
        }
    }

    public MembersByOrgAdapter(Context mCtx, List<UserInfo> membersListMain) {

        this.mCtx = mCtx;
        this.membersList = membersListMain;
        membersListFull = new ArrayList<>(membersList);
    }

    @Override
    public int getCount() {
        return membersList.size();
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

        MembersByOrgAdapter.ViewHolder viewHolder = null;

        if (finalView == null) {

            finalView = LayoutInflater.from(mCtx).inflate(R.layout.list_members_by_org, null);
            viewHolder = new ViewHolder(finalView);
            finalView.setTag(viewHolder);
        }
        else {

            viewHolder = (MembersByOrgAdapter.ViewHolder) finalView.getTag();
        }

        initData(viewHolder, position);
        return finalView;
    }

    private void initData(MembersByOrgAdapter.ViewHolder viewHolder, int position) {

        UserInfo currentItem = membersList.get(position);
        PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(viewHolder.memberAvatar);

	    viewHolder.userLoginId = currentItem.getLogin();

        if(!currentItem.getFullname().equals("")) {

            viewHolder.memberName.setText(Html.fromHtml(currentItem.getFullname()));
        }
        else {

            viewHolder.memberName.setText(currentItem.getLogin());
        }

    }

    @Override
    public Filter getFilter() {
        return membersFilter;
    }

    private final Filter membersFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserInfo> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {

                filteredList.addAll(membersListFull);
            }
            else {

                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserInfo item : membersListFull) {
                    if (item.getFullname().toLowerCase().contains(filterPattern) || item.getLogin().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            membersList.clear();
            membersList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
