package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.databinding.ListRepositoriesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.helpers.languagestatistics.LanguageColor;

/**
 * @author mmarif
 */
public class ExploreRepositoriesAdapter
		extends RecyclerView.Adapter<ExploreRepositoriesAdapter.RepositoriesHolder> {

	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private Runnable loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;

	public ExploreRepositoriesAdapter(
			List<org.gitnex.tea4j.v2.models.Repository> dataList, Context ctx) {
		this.context = ctx;
		this.reposList = dataList;
	}

	@NonNull @Override
	public RepositoriesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		ListRepositoriesBinding binding = ListRepositoriesBinding.inflate(inflater, parent, false);
		return new RepositoriesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull RepositoriesHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}

		holder.bindData(reposList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return reposList.size();
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

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
		this.reposList = list;
		notifyDataChanged();
	}

	public class RepositoriesHolder extends RecyclerView.ViewHolder {

		private final ListRepositoriesBinding binding;
		private org.gitnex.tea4j.v2.models.Repository userRepositories;

		RepositoriesHolder(ListRepositoriesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot()
					.setOnClickListener(
							v -> {
								Context ctx = v.getContext();
								RepositoryContext repo =
										new RepositoryContext(userRepositories, ctx);
								repo.saveToDB(ctx);
								Intent intent = repo.getIntent(ctx, RepoDetailActivity.class);
								ctx.startActivity(intent);
							});
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository repositories) {
			this.userRepositories = repositories;

			binding.repoOpenIssues.setText(String.valueOf(repositories.getOpenIssuesCount()));
			binding.repoOpenPRs.setText(String.valueOf(repositories.getOpenPrCounter()));

			String fullName = repositories.getFullName();
			if (fullName != null && fullName.contains("/")) {
				String[] parts = fullName.split("/");
				binding.orgName.setText(parts[0]);
				binding.repoName.setText(parts[1]);
			} else {
				binding.repoName.setText(repositories.getName());
			}

			binding.repoIsArchivedFrame.setVisibility(
					repositories.isArchived() ? View.VISIBLE : View.GONE);
			binding.repoIsArchivedFrame.setImageDrawable(
					AvatarGenerator.getLabelDrawable(
							context,
							context.getResources().getString(R.string.archivedRepository),
							context.getResources()
									.getColor(R.color.alert_important_border, context.getTheme()),
							18));

			binding.repoStars.setText(AppUtil.numberFormatter(repositories.getStarsCount()));

			if (repositories.getLanguage() != null
					&& !repositories.getLanguage().trim().isEmpty()) {
				binding.repoLanguageFrame.setVisibility(View.VISIBLE);
				int colorRes = LanguageColor.languageColor(repositories.getLanguage());
				int bgColor = context.getColor(colorRes);

				binding.repoLanguageFrame.setImageDrawable(
						AvatarGenerator.getLabelDrawable(
								context, repositories.getLanguage(), bgColor, 20));
			} else {
				binding.repoLanguageFrame.setVisibility(View.GONE);
			}

			binding.repoIsPrivate.setVisibility(
					repositories.isPrivate() ? View.VISIBLE : View.GONE);

			if (repositories.getUpdatedAt() != null) {
				binding.repoLastUpdated.setVisibility(View.VISIBLE);
				binding.repoLastUpdated.setText(
						TimeHelper.formatTime(repositories.getUpdatedAt(), Locale.getDefault()));
				binding.repoLastUpdated.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												repositories.getUpdatedAt(), Locale.getDefault())));
			} else {
				binding.repoLastUpdated.setVisibility(View.GONE);
			}

			if (repositories.getDescription() != null && !repositories.getDescription().isEmpty()) {
				binding.repoDescription.setVisibility(View.VISIBLE);
				binding.repoDescription.setText(repositories.getDescription());
				binding.spacerView.setVisibility(View.GONE);
			} else {
				binding.repoDescription.setVisibility(View.GONE);
				binding.spacerView.setVisibility(View.VISIBLE);
			}

			if (repositories.getPermissions() != null) {
				binding.repoIsAdmin.setChecked(repositories.getPermissions().isAdmin());
			}

			loadAvatar(repositories);
		}

		private void loadAvatar(org.gitnex.tea4j.v2.models.Repository repositories) {
			String label =
					(repositories.getFullName() != null && !repositories.getFullName().isEmpty())
							? repositories.getFullName()
							: repositories.getName();

			Drawable placeholder = AvatarGenerator.getLetterAvatar(context, label, 44);

			Glide.with(context)
					.load(repositories.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(placeholder)
					.error(placeholder)
					.centerCrop()
					.into(binding.imageAvatar);
		}
	}
}
