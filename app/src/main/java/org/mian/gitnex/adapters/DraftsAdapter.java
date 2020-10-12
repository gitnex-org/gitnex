package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftsViewHolder> {

    private List<DraftWithRepository> draftsList;
    private FragmentManager fragmentManager;
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
	    private TextView commentId;
	    private ImageView editCommentStatus;

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
	        commentId = itemView.findViewById(R.id.commentId);
            ImageView deleteDraft = itemView.findViewById(R.id.deleteDraft);
	        editCommentStatus = itemView.findViewById(R.id.editCommentStatus);

            deleteDraft.setOnClickListener(itemDelete -> {

                int getDraftId = Integer.parseInt(draftId.getText().toString());
                deleteDraft(getAdapterPosition());
	            DraftsApi draftsApi = new DraftsApi(mCtx);
	            draftsApi.deleteSingleDraft(getDraftId);

            });

	        itemView.setOnClickListener(itemEdit -> {

		        Bundle bundle = new Bundle();

                bundle.putString("commentBody", draftText.getText().toString());
                bundle.putString("issueNumber", issueNumber.getText().toString());
                bundle.putString("repositoryId", repoId.getText().toString());
                bundle.putString("draftTitle", repoInfo.getText().toString());
		        bundle.putString("commentId", commentId.getText().toString());
		        bundle.putString("draftId", draftId.getText().toString());

                if(!commentId.getText().toString().isEmpty()) {
	                bundle.putString("commentAction", "edit");
                }

                TinyDB tinyDb = new TinyDB(mCtx);
                tinyDb.putString("issueNumber", issueNumber.getText().toString());
                tinyDb.putLong("repositoryId", Long.parseLong(repoId.getText().toString()));
                //tinyDb.putString("issueType", issueType.getText().toString());

		        BottomSheetReplyFragment.newInstance(bundle).show(fragmentManager, "replyBottomSheet");

            });

        }

    }

    public DraftsAdapter(Context mCtx, FragmentManager fragmentManager, List<DraftWithRepository> draftsListMain) {
        this.mCtx = mCtx;
        this.fragmentManager = fragmentManager;
        this.draftsList = draftsListMain;
    }

    private void deleteDraft(int position) {

        draftsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, draftsList.size());
        Toasty.success(mCtx, mCtx.getResources().getString(R.string.draftsSingleDeleteSuccess));

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
	    holder.commentId.setText(currentItem.getCommentId());

	    String issueNumber = "<font color='" + mCtx.getResources().getColor(R.color.lightGray) + "'>" + mCtx.getResources().getString(R.string.hash) + currentItem.getIssueId() + "</font>";
	    Spanned headTitle = Html.fromHtml(issueNumber + " " + currentItem.getRepositoryOwner() + " / " + currentItem.getRepositoryName());
	    holder.repoInfo.setText(headTitle);

	    if(!currentItem.getCommentId().equalsIgnoreCase("new")) {
		    holder.editCommentStatus.setVisibility(View.VISIBLE);
	    }
	    else {
	    	holder.editCommentStatus.setVisibility(View.GONE);
	    }

    }

    @Override
    public int getItemCount() {
        return draftsList.size();
    }

	public void updateList(List<DraftWithRepository> list) {

		draftsList = list;
		notifyDataSetChanged();
	}


}
