package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftsViewHolder> {

    private List<DraftWithRepository> draftsList;
    private final FragmentManager fragmentManager;
    private final Context context;

    class DraftsViewHolder extends RecyclerView.ViewHolder {

    	private DraftWithRepository draftWithRepository;

        private final TextView draftText;
        private final TextView repoInfo;
	    private final ImageView editCommentStatus;

        private DraftsViewHolder(View itemView) {

            super(itemView);

            draftText = itemView.findViewById(R.id.draftText);
            repoInfo = itemView.findViewById(R.id.repoInfo);
            ImageView deleteDraft = itemView.findViewById(R.id.deleteDraft);
	        editCommentStatus = itemView.findViewById(R.id.editCommentStatus);

            deleteDraft.setOnClickListener(itemDelete -> {

                int getDraftId = draftWithRepository.getDraftId();
                deleteDraft(getBindingAdapterPosition());

	            DraftsApi draftsApi = BaseApi.getInstance(context, DraftsApi.class);
	            assert draftsApi != null;
	            draftsApi.deleteSingleDraft(getDraftId);

            });

	        itemView.setOnClickListener(itemEdit -> {

	        	RepositoryContext repository = new RepositoryContext(draftWithRepository.getRepositoryOwner(), draftWithRepository.getRepositoryName(), context);
	        	repository.setRepositoryId(draftWithRepository.getRepositoryId());
	        	IssueContext issue = new IssueContext(repository, draftWithRepository.getIssueId(), draftWithRepository.getIssueType());
		        Bundle bundle = issue.getBundle();

                bundle.putString("commentBody", draftWithRepository.getDraftText());
                bundle.putString("issueNumber", String.valueOf(draftWithRepository.getIssueId()));
                bundle.putString("draftTitle", repoInfo.getText().toString());
		        bundle.putString("commentId", draftWithRepository.getCommentId());
		        bundle.putString("draftId", String.valueOf(draftWithRepository.getDraftId()));

                if(!draftWithRepository.getCommentId().isEmpty()) {
	                bundle.putString("commentAction", "edit");
                }

		        BottomSheetReplyFragment bottomSheetReplyFragment = BottomSheetReplyFragment.newInstance(bundle, issue);
		        bottomSheetReplyFragment.setOnInteractedListener(() -> {
					Intent i = new IssueContext(
						new RepositoryContext(
							draftWithRepository.getRepositoryOwner(),
							draftWithRepository.getRepositoryName(),
							context
						),
						draftWithRepository.getIssueId(),
				        draftWithRepository.getIssueType()
					).getIntent(context, IssueDetailActivity.class);
					i.putExtra("openedFromLink", "true");
					context.startActivity(i);
		        });
		        bottomSheetReplyFragment.show(fragmentManager, "replyBottomSheet");
            });

        }

    }

    public DraftsAdapter(Context ctx, FragmentManager fragmentManager, List<DraftWithRepository> draftsListMain) {
        this.context = ctx;
        this.fragmentManager = fragmentManager;
        this.draftsList = draftsListMain;
    }

    private void deleteDraft(int position) {

        draftsList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, draftsList.size());
        Toasty.success(context, context.getResources().getString(R.string.draftsSingleDeleteSuccess));
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

	    String issueNumber = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + currentItem.getIssueId() + "</font>";
	    Spanned headTitle = HtmlCompat
		    .fromHtml(issueNumber + " " + currentItem.getRepositoryOwner() + " / " + currentItem.getRepositoryName(), HtmlCompat.FROM_HTML_MODE_LEGACY);

	    holder.repoInfo.setText(headTitle);
	    holder.draftWithRepository = currentItem;

	    Markdown.render(context, currentItem.getDraftText(), holder.draftText);

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
