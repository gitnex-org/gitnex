package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.models.Commits;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context ctx;
    private final int TYPE_LOAD = 0;
    private List<Commits> commitsList;
    private CommitsAdapter.OnLoadMoreListener loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;

    public CommitsAdapter(Context ctx, List<Commits> commitsListMain) {

        this.ctx = ctx;
        this.commitsList = commitsListMain;

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(ctx);

        if(viewType == TYPE_LOAD) {
            return new CommitsHolder(inflater.inflate(R.layout.list_commits, parent, false));
        }
        else {
            return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

            isLoading = true;
            loadMoreListener.onLoadMore();

        }

        if(getItemViewType(position) == TYPE_LOAD) {

            ((CommitsHolder) holder).bindData(commitsList.get(position));

        }

    }

    @Override
    public int getItemViewType(int position) {

        if(commitsList.get(position).getSha() != null) {
            return TYPE_LOAD;
        }
        else {
            return 1;
        }

    }

    @Override
    public int getItemCount() {

        return commitsList.size();

    }

    class CommitsHolder extends RecyclerView.ViewHolder {

        TextView commitTitle;
        TextView commitCommitter;
        TextView commitDate;
        Button commitHtmlUrl;

        CommitsHolder(View itemView) {

            super(itemView);

            commitTitle = itemView.findViewById(R.id.commitTitleVw);
            commitCommitter = itemView.findViewById(R.id.commitCommitterVw);
            commitDate = itemView.findViewById(R.id.commitDateVw);
            commitHtmlUrl = itemView.findViewById(R.id.commitHtmlUrlVw);

        }

        @SuppressLint("SetTextI18n")
        void bindData(Commits commitsModel) {

            final TinyDB tinyDb = new TinyDB(ctx);
            final String locale = tinyDb.getString("locale");
            final String timeFormat = tinyDb.getString("dateFormat");

            commitTitle.setText(commitsModel.getCommit().getMessage());
            commitCommitter.setText(ctx.getString(R.string.commitCommittedBy, commitsModel.getCommit().getCommitter().getName()));

            commitDate.setText(TimeHelper.formatTime(commitsModel.getCommit().getCommitter().getDate(), new Locale(locale), timeFormat, ctx));

            if(timeFormat.equals("pretty")) {
                commitDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(commitsModel.getCommit().getCommitter().getDate()), ctx));
            }

            commitHtmlUrl.setOnClickListener(v -> ctx.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(commitsModel.getHtml_url()))));

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

    public void setLoadMoreListener(CommitsAdapter.OnLoadMoreListener loadMoreListener) {

        this.loadMoreListener = loadMoreListener;

    }

    public void updateList(List<Commits> list) {

        commitsList = list;
        notifyDataSetChanged();
    }

}
