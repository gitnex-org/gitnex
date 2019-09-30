package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.UserInfo;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class MembersByOrgAdapter extends BaseAdapter implements Filterable {

    private List<UserInfo> membersList;
    private Context mCtx;
    private List<UserInfo> membersListFull;

    private class ViewHolder {

        private ImageView memberAvatar;
        private TextView memberName;

        ViewHolder(View v) {
            memberAvatar  = v.findViewById(R.id.memberAvatar);
            memberName  = v.findViewById(R.id.memberName);
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
            finalView = LayoutInflater.from(mCtx).inflate(R.layout.members_by_org_list, null);
            viewHolder = new MembersByOrgAdapter.ViewHolder(finalView);
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
        Picasso.get().load(currentItem.getAvatar()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(viewHolder.memberAvatar);

        if(!currentItem.getFullname().equals("")) {
            viewHolder.memberName.setText(currentItem.getFullname());
        }
        else {
            viewHolder.memberName.setText(currentItem.getLogin());
        }

    }

    @Override
    public Filter getFilter() {
        return membersFilter;
    }

    private Filter membersFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserInfo> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(membersListFull);
            } else {
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
