package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Commits;

/**
 * Author M M Arif
 */

public class CommitsAdapter extends PagedListAdapter<Commits, CommitsAdapter.CommitsViewHolder> {

    private Context mCtx;

    public CommitsAdapter(Context mCtx) {

        super(DIFF_CALLBACK);
        this.mCtx = mCtx;

    }

    @NonNull
    @Override
    public CommitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mCtx).inflate(R.layout.list_commits, parent, false);
        return new CommitsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull CommitsViewHolder holder, int position) {

        Commits commit_ = getItem(position);

        if (commit_ != null) {
            holder.commitTitle.setText(commit_.getCommit().getMessage());
        }

    }

    private static DiffUtil.ItemCallback<Commits> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Commits>() {
                @Override
                public boolean areItemsTheSame(Commits oldCommit, Commits newCommit) {
                    return oldCommit.getSha().equals(newCommit.getSha());
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(Commits oldCommit, @NonNull Commits newCommit) {
                    return oldCommit.equals(newCommit);
                }
            };

    static class CommitsViewHolder extends RecyclerView.ViewHolder {

        TextView commitTitle;

        CommitsViewHolder(View itemView) {

            super(itemView);
            commitTitle = itemView.findViewById(R.id.commitTitle);

        }

    }

}
