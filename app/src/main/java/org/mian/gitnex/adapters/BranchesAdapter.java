package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitsActivity;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author M M Arif
 */

public class BranchesAdapter extends RecyclerView.Adapter<BranchesAdapter.BranchesViewHolder> {

    private List<Branches> branchesList;
    private Context mCtx;

    static class BranchesViewHolder extends RecyclerView.ViewHolder {

        private TextView branchNameTv;
        private TextView branchCommitAuthor;

        private BranchesViewHolder(View itemView) {
            super(itemView);

            branchNameTv = itemView.findViewById(R.id.branchName);
            branchCommitAuthor = itemView.findViewById(R.id.branchCommitAuthor);
            TextView branchCommitHash = itemView.findViewById(R.id.branchCommitHash);

            branchCommitHash.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    Intent intent = new Intent(v.getContext(), CommitsActivity.class);
                    intent.putExtra("branchName", String.valueOf(branchNameTv.getText()));
                    Objects.requireNonNull(v.getContext()).startActivity(intent);

                }
            });

        }
    }

    public BranchesAdapter(Context mCtx, List<Branches> branchesMain) {
        this.mCtx = mCtx;
        this.branchesList = branchesMain;
    }

    @NonNull
    @Override
    public BranchesAdapter.BranchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_branches, parent, false);
        return new BranchesAdapter.BranchesViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BranchesAdapter.BranchesViewHolder holder, int position) {

        final TinyDB tinyDb = new TinyDB(mCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");

        Branches currentItem = branchesList.get(position);
        holder.branchNameTv.setText(currentItem.getName());

        if(currentItem.getCommit().getAuthor().getName() != null || !currentItem.getCommit().getAuthor().getName().equals("")) {
            holder.branchCommitAuthor.setText(mCtx.getResources().getString(R.string.commitAuthor, currentItem.getCommit().getAuthor().getName()));
        }
        else {
            holder.branchCommitAuthor.setText(mCtx.getResources().getString(R.string.commitAuthor, currentItem.getCommit().getAuthor().getUsername()));
        }

    }

    @Override
    public int getItemCount() {
        return branchesList.size();
    }

}


