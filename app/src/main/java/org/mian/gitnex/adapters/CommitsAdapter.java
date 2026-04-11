package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Commit;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListCommitsBinding;
import org.mian.gitnex.helpers.TimeHelper;

/**
 * @author mmarif
 */
public class CommitsAdapter extends RecyclerView.Adapter<CommitsAdapter.CommitsHolder> {

	private final Context context;
	private List<Commit> commitsList;
	private final OnCommitClickListener listener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;
	private Runnable loadMoreListener;

	public interface OnCommitClickListener {
		void onCommitClick(Commit commit);
	}

	public CommitsAdapter(
			Context context, List<Commit> commitsList, OnCommitClickListener listener) {
		this.context = context;
		this.commitsList = commitsList != null ? commitsList : new ArrayList<>();
		this.listener = listener;
	}

	@NonNull @Override
	public CommitsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListCommitsBinding binding =
				ListCommitsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new CommitsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull CommitsHolder holder, int position) {
		if (commitsList.isEmpty()) return;

		Commit commit = commitsList.get(position);

		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}

		holder.bindData(commit, context, listener);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return commitsList.size();
	}

	public void setMoreDataAvailable(boolean available) {
		this.isMoreDataAvailable = available;
	}

	public void setLoadMoreListener(Runnable listener) {
		this.loadMoreListener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Commit> newList) {
		this.commitsList = newList != null ? newList : new ArrayList<>();
		this.isLoading = false;
		notifyDataSetChanged();
	}

	public static class CommitsHolder extends RecyclerView.ViewHolder {
		private final ListCommitsBinding binding;

		CommitsHolder(ListCommitsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		public void bindData(Commit commit, Context context, OnCommitClickListener listener) {
			if (commit == null || commit.getCommit() == null) return;

			String message = commit.getCommit().getMessage();
			String subject = (message != null) ? message.split("(\r\n|\n)")[0].trim() : "";
			binding.commitSubject.setText(EmojiParser.parseToUnicode(subject));

			String authorName =
					(commit.getCommit().getAuthor() != null)
							? commit.getCommit().getAuthor().getName()
							: "";
			String committerName =
					(commit.getCommit().getCommitter() != null)
							? commit.getCommit().getCommitter().getName()
							: "";

			String authorEmail =
					(commit.getCommit().getAuthor() != null)
							? commit.getCommit().getAuthor().getEmail()
							: null;
			String committerEmail =
					(commit.getCommit().getCommitter() != null)
							? commit.getCommit().getCommitter().getEmail()
							: null;

			String time = "";
			if (commit.getCommit().getCommitter() != null
					&& commit.getCommit().getCommitter().getDate() != null) {
				Date date = TimeHelper.parseIso8601(commit.getCommit().getCommitter().getDate());
				time = TimeHelper.getFullDateTime(date, Locale.getDefault());
			}

			String metaText;
			boolean isDifferentUser =
					(authorEmail != null
							&& committerEmail != null
							&& !Objects.equals(authorEmail, committerEmail));

			if (isDifferentUser) {
				metaText =
						context.getString(
								R.string.commitAuthoredByAndCommittedByWhen,
								authorName,
								committerName,
								time);
			} else if (authorEmail != null) {
				metaText = context.getString(R.string.commitAuthoredAndCommitted, authorName, time);
			} else {
				metaText = context.getString(R.string.commitCommittedByWhen, committerName, time);
			}

			binding.commitAuthorAndCommitter.setText(metaText);

			loadAvatar(
					context,
					commit.getAuthor(),
					binding.commitAuthorAvatar,
					binding.commitAuthorAvatarFrame);

			boolean showSecondAvatar =
					commit.getAuthor() != null
							&& commit.getCommitter() != null
							&& !Objects.equals(
									commit.getAuthor().getLogin(),
									commit.getCommitter().getLogin());

			if (showSecondAvatar) {
				binding.commitCommitterAvatarFrame.setVisibility(View.VISIBLE);
				loadAvatar(
						context,
						commit.getCommitter(),
						binding.commitCommitterAvatar,
						binding.commitCommitterAvatarFrame);
			} else {
				binding.commitCommitterAvatarFrame.setVisibility(View.GONE);
			}

			String sha = commit.getSha();
			if (sha != null) {
				binding.commitSha.setText(sha.substring(0, Math.min(sha.length(), 7)));
			}

			itemView.setOnClickListener(
					v -> {
						if (listener != null) listener.onCommitClick(commit);
					});
		}

		private void loadAvatar(Context context, User user, ImageView imageView, View frame) {
			if (context == null || imageView == null) return;
			if (frame != null) {
				frame.setVisibility(View.VISIBLE);
			}

			if (user != null && user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
				Glide.with(context)
						.load(user.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.loader_animated)
						.error(R.drawable.ic_person)
						.centerCrop()
						.into(imageView);
			} else {
				Glide.with(context)
						.load(R.drawable.ic_person)
						.placeholder(R.drawable.loader_animated)
						.into(imageView);
			}
		}
	}
}
