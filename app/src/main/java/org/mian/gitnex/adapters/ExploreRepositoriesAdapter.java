package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.databinding.ListRepositoriesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

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

			// 1. Top Utility Stats (Issues and PRs)
			binding.repoOpenIssues.setText(String.valueOf(repositories.getOpenIssuesCount()));
			binding.repoOpenPRs.setText(String.valueOf(repositories.getOpenPrCounter()));

			// 2. Identity Row (Org and Archived)
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

			// 3. Info Footer (Lang and Stars)
			binding.repoStars.setText(AppUtil.numberFormatter(repositories.getStarsCount()));

			if (repositories.getLanguage() != null
					&& !repositories.getLanguage().trim().isEmpty()) {
				binding.repoLanguageFrame.setVisibility(View.VISIBLE);
				binding.repoStars2.setText(repositories.getLanguage());
			} else {
				binding.repoLanguageFrame.setVisibility(View.GONE);
			}

			// Private Lock Icon
			binding.repoIsPrivate.setVisibility(
					repositories.isPrivate() ? View.VISIBLE : View.GONE);

			// 4. Date/Time with Click for Detail
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

			// 5. Description
			if (repositories.getDescription() != null && !repositories.getDescription().isEmpty()) {
				binding.repoDescription.setVisibility(View.VISIBLE);
				binding.repoDescription.setText(repositories.getDescription());
				binding.spacerView.setVisibility(View.GONE);
			} else {
				binding.repoDescription.setVisibility(View.GONE);
				binding.spacerView.setVisibility(View.VISIBLE);
			}

			// 6. Backend Only
			if (repositories.getPermissions() != null) {
				binding.repoIsAdmin.setChecked(repositories.getPermissions().isAdmin());
			}

			loadAvatar(repositories);
		}

		private void loadAvatar(org.gitnex.tea4j.v2.models.Repository repositories) {
			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(repositories.getName());
			String firstCharacter =
					repositories.getFullName() != null && !repositories.getFullName().isEmpty()
							? String.valueOf(repositories.getFullName().charAt(0))
							: "?";

			TextDrawable drawable =
					TextDrawable.builder()
							.beginConfig()
							.useFont(Typeface.DEFAULT)
							.fontSize(28)
							.toUpperCase()
							.width(44)
							.height(44)
							.endConfig()
							.buildRoundRect(firstCharacter, color, 12);

			if (repositories.getAvatarUrl() != null && !repositories.getAvatarUrl().isEmpty()) {
				Glide.with(context)
						.load(repositories.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.loader_animated)
						.centerCrop()
						.into(binding.imageAvatar);
			} else {
				binding.imageAvatar.setImageDrawable(drawable);
			}
		}
	}
}
