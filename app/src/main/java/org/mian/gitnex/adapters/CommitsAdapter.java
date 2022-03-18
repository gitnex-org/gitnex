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
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.models.Commits;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.CommitsActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import java.util.List;
import java.util.Objects;

/**
 * @author M M Arif
 */

public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<Commits> commitsList;
    private Runnable loadMoreListener;
    private boolean isLoading = false;
    private boolean isMoreDataAvailable = true;

    public CommitsAdapter(Context ctx, List<Commits> commitsListMain) {

        this.context = ctx;
        this.commitsList = commitsListMain;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new CommitsHolder(inflater.inflate(R.layout.list_commits, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
            isLoading = true;
            loadMoreListener.run();
        }

        ((CommitsHolder) holder).bindData(commitsList.get(position));
    }

    @Override
    public int getItemViewType(int position) {
		return position;
    }

    @Override
    public int getItemCount() {
        return commitsList.size();
    }

    class CommitsHolder extends RecyclerView.ViewHolder {

    	View rootView;

        TextView commitSubject;
	    TextView commitAuthorAndCommitter;
	    ImageView commitAuthorAvatar;
	    ImageView commitCommitterAvatar;
        TextView commitSha;

        CommitsHolder(View itemView) {

            super(itemView);

            rootView = itemView;

            commitSubject = itemView.findViewById(R.id.commitSubject);
	        commitAuthorAndCommitter = itemView.findViewById(R.id.commitAuthorAndCommitter);
	        commitAuthorAvatar = itemView.findViewById(R.id.commitAuthorAvatar);
	        commitCommitterAvatar = itemView.findViewById(R.id.commitCommitterAvatar);
            commitSha = itemView.findViewById(R.id.commitSha);

        }

        void bindData(Commits commitsModel) {

            String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

            commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));

            if(!Objects.equals(commitsModel.getCommit().getCommitter().getEmail(), commitsModel.getCommit().getAuthor().getEmail())) {
	            commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(context
		            .getString(R.string.commitAuthoredByAndCommittedByWhen, commitsModel.getCommit().getAuthor().getName(), commitsModel.getCommit().getCommitter().getName(),
			            TimeHelper
				            .formatTime(commitsModel.getCommit().getCommitter().getDate(), context.getResources().getConfiguration().locale, "pretty",
					            context)), HtmlCompat.FROM_HTML_MODE_COMPACT));
            } else {
            	commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(context
		            .getString(R.string.commitCommittedByWhen, commitsModel.getCommit().getCommitter().getName(),
			            TimeHelper
				            .formatTime(commitsModel.getCommit().getCommitter().getDate(), context.getResources().getConfiguration().locale, "pretty",
					            context)), HtmlCompat.FROM_HTML_MODE_COMPACT));

            }

	        if(commitsModel.getAuthor() != null && commitsModel.getAuthor().getAvatar_url() != null &&
		        !commitsModel.getAuthor().getAvatar_url().isEmpty()) {

		        commitAuthorAvatar.setVisibility(View.VISIBLE);

		        int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		        PicassoService.getInstance(context).get()
			        .load(commitsModel.getAuthor().getAvatar_url())
			        .placeholder(R.drawable.loader_animated)
			        .transform(new RoundedTransformation(imgRadius, 0))
			        .resize(120, 120)
			        .centerCrop().into(commitAuthorAvatar);

	        } else {
		        commitAuthorAvatar.setImageDrawable(null);
		        commitAuthorAvatar.setVisibility(View.GONE);
	        }

            if(commitsModel.getCommitter() != null &&
				!commitsModel.getAuthor().getLogin().equals(commitsModel.getCommitter().getLogin()) &&
	            commitsModel.getCommitter().getAvatar_url() != null &&
	            !commitsModel.getCommitter().getAvatar_url().isEmpty()) {

	            commitCommitterAvatar.setVisibility(View.VISIBLE);

	            int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

	            PicassoService.getInstance(context).get()
		            .load(commitsModel.getCommitter().getAvatar_url())
		            .placeholder(R.drawable.loader_animated)
		            .transform(new RoundedTransformation(imgRadius, 0))
		            .resize(120, 120)
		            .centerCrop().into(commitCommitterAvatar);

            } else {
	            commitCommitterAvatar.setImageDrawable(null);
	            commitCommitterAvatar.setVisibility(View.GONE);
            }

	        commitSha.setText(commitsModel.getSha().substring(0, Math.min(commitsModel.getSha().length(), 10)));
            rootView.setOnClickListener(v -> {
	            Intent intent = ((CommitsActivity) context).repository.getIntent(context, CommitDetailActivity.class);
				intent.putExtra("sha", commitsModel.getSha());
				context.startActivity(intent);
            });

        }
    }

	public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void notifyDataChanged() {
        notifyDataSetChanged();
        isLoading = false;
    }

    public void setLoadMoreListener(Runnable loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void updateList(List<Commits> list) {
        commitsList = list;
        notifyDataChanged();
    }
}
