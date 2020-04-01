package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.PullRequests;
import org.mian.gitnex.util.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class PullRequestsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    private Context context;
    private final int TYPE_LOAD = 0;
    private List<PullRequests> prList;
    private List<PullRequests> prListFull;
    private PullRequestsAdapter.OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false, isMoreDataAvailable = true;

    public PullRequestsAdapter(Context context, List<PullRequests> prListMain) {

        this.context = context;
        this.prList = prListMain;
        prListFull = new ArrayList<>(prList);

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);

        if(viewType == TYPE_LOAD){
            return new PullRequestsAdapter.PullRequestsHolder(inflater.inflate(R.layout.list_pr, parent,false));
        }
        else {
            return new PullRequestsAdapter.LoadHolder(inflater.inflate(R.layout.row_load,parent,false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount()-1 && isMoreDataAvailable && !isLoading && loadMoreListener!=null) {

            isLoading = true;
            loadMoreListener.onLoadMore();

        }

        if(getItemViewType(position) == TYPE_LOAD) {

            ((PullRequestsAdapter.PullRequestsHolder)holder).bindData(prList.get(position));

        }

    }

    @Override
    public int getItemViewType(int position) {

        if(prList.get(position).getTitle() != null) {
            return TYPE_LOAD;
        }
        else {
            return 1;
        }

    }

    @Override
    public int getItemCount() {

        return prList.size();

    }

    class PullRequestsHolder extends RecyclerView.ViewHolder {

        private TextView prNumber;
        private ImageView assigneeAvatar;
        private TextView prTitle;
        private TextView prCreatedTime;
        private TextView prCommentsCount;

        PullRequestsHolder(View itemView) {

            super(itemView);

            prNumber = itemView.findViewById(R.id.prNumber);
            assigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
            prTitle = itemView.findViewById(R.id.prTitle);
            prCommentsCount = itemView.findViewById(R.id.prCommentsCount);
            LinearLayout frameCommentsCount = itemView.findViewById(R.id.frameCommentsCount);
            prCreatedTime = itemView.findViewById(R.id.prCreatedTime);

            prTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra("issueNumber", prNumber.getText());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("issueNumber", prNumber.getText().toString());
                    tinyDb.putString("issueType", "pr");
                    context.startActivity(intent);

                }
            });
            frameCommentsCount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    Intent intent = new Intent(context, IssueDetailActivity.class);
                    intent.putExtra("issueNumber", prNumber.getText());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("issueNumber", prNumber.getText().toString());
                    tinyDb.putString("issueType", "pr");
                    context.startActivity(intent);

                }
            });

        }

        @SuppressLint("SetTextI18n")
        void bindData(PullRequests prModel){

            final TinyDB tinyDb = new TinyDB(context);
            final String locale = tinyDb.getString("locale");
            final String timeFormat = tinyDb.getString("dateFormat");

            if (!prModel.getUser().getFull_name().equals("")) {
                assigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.prCreator) + prModel.getUser().getFull_name(), context));
            } else {
                assigneeAvatar.setOnClickListener(new ClickListener(context.getResources().getString(R.string.prCreator) + prModel.getUser().getLogin(), context));
            }

            if (prModel.getUser().getAvatar_url() != null) {
                PicassoService.getInstance(context).get().load(prModel.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(assigneeAvatar);
            } else {
                PicassoService.getInstance(context).get().load(prModel.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(assigneeAvatar);
            }

            String prNumber_ = "<font color='" + context.getResources().getColor(R.color.lightGray) + "'>" + context.getResources().getString(R.string.hash) + prModel.getNumber() + "</font>";
            prTitle.setText(Html.fromHtml(prNumber_ + " " + prModel.getTitle()));

            prNumber.setText(String.valueOf(prModel.getNumber()));
            prCommentsCount.setText(String.valueOf(prModel.getComments()));

            switch (timeFormat) {
                case "pretty": {
                    PrettyTime prettyTime = new PrettyTime(new Locale(locale));
                    String createdTime = prettyTime.format(prModel.getCreated_at());
                    prCreatedTime.setText(createdTime);
                    prCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(prModel.getCreated_at()), context));
                    break;
                }
                case "normal": {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(prModel.getCreated_at());
                    prCreatedTime.setText(createdTime);
                    break;
                }
                case "normal1": {
                    DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
                    String createdTime = formatter.format(prModel.getCreated_at());
                    prCreatedTime.setText(createdTime);
                    break;
                }
            }

        }

    }

    static class LoadHolder extends RecyclerView.ViewHolder {

        LoadHolder(View itemView) {
            super(itemView);
        }

    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {

        isMoreDataAvailable = moreDataAvailable;

    }

    public void notifyDataChanged() {

        notifyDataSetChanged();
        isLoading = false;

    }

    public interface OnLoadMoreListener {

        void onLoadMore();

    }

    public void setLoadMoreListener(PullRequestsAdapter.OnLoadMoreListener loadMoreListener) {

        this.loadMoreListener = loadMoreListener;

    }

    @Override
    public Filter getFilter() {
        return prFilter;
    }

    private Filter prFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<PullRequests> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(prList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (PullRequests item : prList) {
                    if (item.getTitle().toLowerCase().contains(filterPattern) || item.getBody().toLowerCase().contains(filterPattern)) {
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
            prList.clear();
            prList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}
