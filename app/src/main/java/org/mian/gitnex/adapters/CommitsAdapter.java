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
import com.google.android.material.card.MaterialCardView;
import com.vdurmont.emoji.EmojiParser;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.CommitsActivity;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.contexts.IssueContext;
import java.util.List;
import java.util.Objects;

/**
 * @author M M Arif
 */

public class CommitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Commit> commitsList;
	private Runnable loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;

	public CommitsAdapter(Context ctx, List<Commit> commitsListMain) {

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

	public void updateList(List<Commit> list) {
		commitsList = list;
		notifyDataChanged();
	}

	class CommitsHolder extends RecyclerView.ViewHolder {

		View rootView;

		TextView commitSubject;
		TextView commitAuthorAndCommitter;
		ImageView commitAuthorAvatar;
		ImageView commitCommitterAvatar;
		TextView commitSha;
		MaterialCardView commitCommitterAvatarFrame;
		MaterialCardView commitAuthorAvatarFrame;

		CommitsHolder(View itemView) {

			super(itemView);

			rootView = itemView;

			commitSubject = itemView.findViewById(R.id.commitSubject);
			commitAuthorAndCommitter = itemView.findViewById(R.id.commitAuthorAndCommitter);
			commitAuthorAvatar = itemView.findViewById(R.id.commitAuthorAvatar);
			commitCommitterAvatar = itemView.findViewById(R.id.commitCommitterAvatar);
			commitSha = itemView.findViewById(R.id.commitSha);
			commitCommitterAvatarFrame = itemView.findViewById(R.id.commitCommitterAvatarFrame);
			commitAuthorAvatarFrame = itemView.findViewById(R.id.commitAuthorAvatarFrame);
		}

		void bindData(Commit commitsModel) {

			String[] commitMessageParts = commitsModel.getCommit().getMessage().split("(\r\n|\n)", 2);

			commitSubject.setText(EmojiParser.parseToUnicode(commitMessageParts[0].trim()));

			if(!Objects.equals(commitsModel.getCommit().getCommitter().getEmail(), commitsModel.getCommit().getAuthor().getEmail())) {
				commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(
					context.getString(R.string.commitAuthoredByAndCommittedByWhen, commitsModel.getCommit().getAuthor().getName(), commitsModel.getCommit().getCommitter().getName(),
						TimeHelper.formatTime(TimeHelper.parseIso8601(commitsModel.getCommit().getCommitter().getDate()), context.getResources().getConfiguration().locale)),
					HtmlCompat.FROM_HTML_MODE_COMPACT));
			}
			else {
				commitAuthorAndCommitter.setText(HtmlCompat.fromHtml(context.getString(R.string.commitCommittedByWhen, commitsModel.getCommit().getCommitter().getName(),
						TimeHelper.formatTime(TimeHelper.parseIso8601(commitsModel.getCommit().getCommitter().getDate()), context.getResources().getConfiguration().locale)),
					HtmlCompat.FROM_HTML_MODE_COMPACT));
			}

			if(commitsModel.getAuthor() != null && commitsModel.getAuthor().getAvatarUrl() != null && !commitsModel.getAuthor().getAvatarUrl().isEmpty()) {

				commitAuthorAvatarFrame.setVisibility(View.VISIBLE);

				int imgRadius = AppUtil.getPixelsFromDensity(context, 60);

				PicassoService.getInstance(context).get().load(commitsModel.getAuthor().getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120)
					.centerCrop().into(commitAuthorAvatar);
			}
			else {
				commitAuthorAvatar.setImageDrawable(null);
				commitAuthorAvatarFrame.setVisibility(View.GONE);
			}

			if(commitsModel.getCommitter() != null && (commitsModel.getAuthor() == null || !commitsModel.getAuthor().getLogin().equals(commitsModel.getCommitter().getLogin())) && commitsModel.getCommitter()
				.getAvatarUrl() != null && !commitsModel.getCommitter().getAvatarUrl().isEmpty()) {

				commitCommitterAvatarFrame.setVisibility(View.VISIBLE);

				int imgRadius = AppUtil.getPixelsFromDensity(context, 60);

				PicassoService.getInstance(context).get().load(commitsModel.getCommitter().getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120)
					.centerCrop().into(commitCommitterAvatar);
			}
			else {
				commitCommitterAvatar.setImageDrawable(null);
				commitCommitterAvatarFrame.setVisibility(View.GONE);
			}

			commitSha.setText(commitsModel.getSha().substring(0, Math.min(commitsModel.getSha().length(), 10)));
			rootView.setOnClickListener(v -> {
				Intent intent;
				if(context instanceof CommitsActivity) {
					intent = ((CommitsActivity) context).repository.getIntent(context, CommitDetailActivity.class);
				}
				else {
					intent = IssueContext.fromIntent(((DiffActivity) context).getIntent()).getRepository().getIntent(context, CommitDetailActivity.class);
				}
				intent.putExtra("sha", commitsModel.getSha());
				context.startActivity(intent);
			});

		}

	}

}
