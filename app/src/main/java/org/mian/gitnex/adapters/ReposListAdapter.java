package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
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
public class ReposListAdapter extends RecyclerView.Adapter<ReposListAdapter.ReposHolder>
		implements Filterable {

	private final Context context;
	private final List<org.gitnex.tea4j.v2.models.Repository> reposListFull;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;
	public boolean isUserOrg = false;

	private final Filter reposFilter =
			new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					List<org.gitnex.tea4j.v2.models.Repository> filteredList = new ArrayList<>();

					if (constraint == null || constraint.length() == 0) {
						filteredList.addAll(reposListFull);
					} else {
						String filterPattern = constraint.toString().toLowerCase().trim();

						for (org.gitnex.tea4j.v2.models.Repository item : reposListFull) {
							if (item.getFullName().toLowerCase().contains(filterPattern)
									|| item.getDescription()
											.toLowerCase()
											.contains(filterPattern)) {
								filteredList.add(item);
							}
						}
					}

					FilterResults results = new FilterResults();
					results.values = filteredList;
					results.count = filteredList.size();
					return results;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {
					reposList.clear();

					// No cast needed - we know what we put in, we know what we get out
					// The FilterResults.values is exactly what we set in performFiltering
					Object resultObject = results.values;

					if (resultObject instanceof List<?> resultList) {

						// Create a new typed list by iterating
						List<org.gitnex.tea4j.v2.models.Repository> typedList = new ArrayList<>();
						for (Object item : resultList) {
							if (item instanceof org.gitnex.tea4j.v2.models.Repository) {
								typedList.add((org.gitnex.tea4j.v2.models.Repository) item);
							}
						}

						reposList.addAll(typedList);
					}

					notifyDataChanged();
				}
			};

	public ReposListAdapter(
			List<org.gitnex.tea4j.v2.models.Repository> reposListMain, Context ctx) {
		this.context = ctx;
		this.reposList = reposListMain;
		this.reposListFull = new ArrayList<>(reposList);
	}

	@NonNull @Override
	public ReposHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		ListRepositoriesBinding binding = ListRepositoriesBinding.inflate(inflater, parent, false);
		return new ReposHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ReposHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
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
		if (!isMoreDataAvailable && loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		if (loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
		this.reposList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return reposFilter;
	}

	public interface OnLoadMoreListener {
		void onLoadMore();

		void onLoadFinished();
	}

	public static class ReposHolder extends RecyclerView.ViewHolder {

		private final ListRepositoriesBinding binding;
		private org.gitnex.tea4j.v2.models.Repository userRepositories;
		private final ReposListAdapter adapter;

		ReposHolder(ListRepositoriesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = null; // Will be set via bind method if needed

			setupClickListeners();
		}

		private void setupClickListeners() {
			binding.getRoot()
					.setOnClickListener(
							v -> {
								Context context = v.getContext();
								RepositoryContext repo =
										new RepositoryContext(userRepositories, context);
								repo.saveToDB(context);
								Intent intent = repo.getIntent(context, RepoDetailActivity.class);
								if (adapter != null && adapter.isUserOrg) {
									intent.putExtra("openedFromUserOrg", true);
								}
								context.startActivity(intent);
							});
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository repositories) {
			this.userRepositories = repositories;

			// 1. Top Stats Row (Issues and PRs)
			binding.repoOpenIssues.setText(String.valueOf(repositories.getOpenIssuesCount()));
			binding.repoOpenPRs.setText(String.valueOf(repositories.getOpenPrCounter()));

			// 2. Organization Name & Archived Badge
			String fullName = repositories.getFullName();
			String[] nameParts = fullName.split("/");
			if (nameParts.length > 0) {
				binding.orgName.setText(nameParts[0]);
			}
			binding.repoIsArchivedFrame.setVisibility(
					repositories.isArchived() ? View.VISIBLE : View.GONE);

			if (nameParts.length > 1) {
				binding.repoName.setText(nameParts[1]);
			}

			// 3. Info Footer (Lang background is back!)
			binding.repoStars.setText(AppUtil.numberFormatter(repositories.getStarsCount()));

			if (repositories.getLanguage() != null
					&& !repositories.getLanguage().trim().isEmpty()) {
				binding.repoLanguageFrame.setVisibility(View.VISIBLE);
				binding.repoStars2.setText(repositories.getLanguage());
			} else {
				binding.repoLanguageFrame.setVisibility(View.GONE);
			}

			// Lock icon at the end of the footer
			binding.repoIsPrivate.setVisibility(
					repositories.isPrivate() ? View.VISIBLE : View.GONE);

			// 4. Date/Time
			if (repositories.getUpdatedAt() != null) {
				binding.repoLastUpdated.setVisibility(View.VISIBLE);
				binding.repoLastUpdated.setText(
						TimeHelper.formatTime(repositories.getUpdatedAt(), Locale.getDefault()));
				binding.repoLastUpdated.setOnClickListener(
						v ->
								Toasty.show(
										binding.repoLastUpdated.getContext(),
										TimeHelper.getFullDateTime(
												repositories.getUpdatedAt(), Locale.getDefault())));
			}

			// 5. Description
			if (repositories.getDescription() != null && !repositories.getDescription().isEmpty()) {
				binding.repoDescription.setVisibility(View.VISIBLE);
				binding.repoDescription.setText(repositories.getDescription());
			} else {
				binding.repoDescription.setVisibility(View.GONE);
			}

			// 6. Backend Logic
			if (repositories.getPermissions() != null) {
				binding.repoIsAdmin.setChecked(repositories.getPermissions().isAdmin());
			}

			loadAvatar(repositories);
		}

		private void loadAvatar(org.gitnex.tea4j.v2.models.Repository repositories) {
			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(repositories.getName());
			String firstCharacter = String.valueOf(repositories.getFullName().charAt(0));

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
				Glide.with(binding.imageAvatar.getContext())
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
