package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ReplyToIssueActivity;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftsViewHolder> {

    private List<DraftWithRepository> draftsList;
    private Context mCtx;

    class DraftsViewHolder extends RecyclerView.ViewHolder {

        private TextView draftText;
        private TextView repoInfo;
        private TextView repoId;
        private TextView draftId;
        private TextView issueNumber;
        private TextView issueType;
        private TextView repoOwner;
        private TextView repoName;

        private DraftsViewHolder(View itemView) {

            super(itemView);

            draftText = itemView.findViewById(R.id.draftText);
            repoInfo = itemView.findViewById(R.id.repoInfo);
            repoId = itemView.findViewById(R.id.repoId);
            draftId = itemView.findViewById(R.id.draftId);
            issueNumber = itemView.findViewById(R.id.issueNumber);
            issueType = itemView.findViewById(R.id.issueType);
            repoOwner = itemView.findViewById(R.id.repoOwner);
            repoName = itemView.findViewById(R.id.repoName);
            ImageView deleteDraft = itemView.findViewById(R.id.deleteDraft);

            deleteDraft.setOnClickListener(itemDelete -> {

                int getDraftId = Integer.parseInt(draftId.getText().toString());
                deleteDraft(getAdapterPosition());
	            DraftsApi draftsApi = new DraftsApi(mCtx);
	            draftsApi.deleteSingleDraft(getDraftId);

            });

	        itemView.setOnClickListener(itemEdit -> {

                Intent intent = new Intent(mCtx, ReplyToIssueActivity.class);
                intent.putExtra("commentBody", draftText.getText());
                intent.putExtra("issueNumber", issueNumber.getText());
                intent.putExtra("repositoryId", repoId.getText());
                intent.putExtra("draftTitle", repoInfo.getText());

                TinyDB tinyDb = new TinyDB(mCtx);
                tinyDb.putString("issueNumber", issueNumber.getText().toString());
                tinyDb.putLong("repositoryId", Long.parseLong(repoId.getText().toString()));
                //tinyDb.putString("issueType", issueType.getText().toString());

                mCtx.startActivity(intent);

            });

        }

    }

    public DraftsAdapter(Context mCtx, List<DraftWithRepository> draftsListMain) {
        this.mCtx = mCtx;
        this.draftsList = draftsListMain;
    }

    private void deleteDraft(int position) {

        draftsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, draftsList.size());
        Toasty.info(mCtx, mCtx.getResources().getString(R.string.draftsSingleDeleteSuccess));

    }

    @NonNull
    @Override
    public DraftsAdapter.DraftsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_drafts, parent, false);
        return new DraftsViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull DraftsAdapter.DraftsViewHolder holder, int position) {

        DraftWithRepository currentItem = draftsList.get(position);

        holder.repoId.setText(String.valueOf(currentItem.getRepositoryId()));
        holder.draftId.setText(String.valueOf(currentItem.getDraftId()));
        holder.issueNumber.setText(String.valueOf(currentItem.getIssueId()));
        holder.issueType.setText(currentItem.getDraftType());
        holder.repoOwner.setText(currentItem.getRepositoryOwner());
        holder.repoName.setText(currentItem.getRepositoryName());
        holder.draftText.setText(currentItem.getDraftText());
	    holder.repoInfo.setText(String.format("%s%d %s / %s", mCtx.getResources().getString(R.string.hash), currentItem.getIssueId(), currentItem.getRepositoryOwner(), currentItem.getRepositoryName()));

    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

}
