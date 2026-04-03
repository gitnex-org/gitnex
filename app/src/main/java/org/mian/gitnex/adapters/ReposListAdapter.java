package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class ReposListAdapter extends RecyclerView.Adapter<ReposListAdapter.ReposHolder>
		implements Filterable {

	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private List<org.gitnex.tea4j.v2.models.Repository> reposListFull;
	public boolean isUserOrg = false;

	public ReposListAdapter(List<org.gitnex.tea4j.v2.models.Repository> list, Context ctx) {
		this.context = ctx;
		this.reposList = list;
		this.reposListFull = new ArrayList<>(list);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> newList) {
		this.reposList = newList;
		this.reposListFull = new ArrayList<>(newList);
		notifyDataSetChanged();
	}

	@NonNull @Override
	public ReposHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListRepositoriesBinding binding =
				ListRepositoriesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ReposHolder(binding, this);
	}

	@Override
	public void onBindViewHolder(@NonNull ReposHolder holder, int position) {
		holder.bindData(reposList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<org.gitnex.tea4j.v2.models.Repository> filtered = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filtered.addAll(reposListFull);
				} else {
					String pattern = constraint.toString().toLowerCase().trim();
					for (org.gitnex.tea4j.v2.models.Repository item : reposListFull) {
						if (item.getFullName().toLowerCase().contains(pattern)
								|| (item.getDescription() != null
										&& item.getDescription().toLowerCase().contains(pattern))) {
							filtered.add(item);
						}
					}
				}
				FilterResults results = new FilterResults();
				results.values = filtered;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				if (results.values instanceof List<?>) {
					reposList = new ArrayList<>();
					for (Object item : (List<?>) results.values) {
						if (item instanceof org.gitnex.tea4j.v2.models.Repository) {
							reposList.add((org.gitnex.tea4j.v2.models.Repository) item);
						}
					}
				}
				notifyDataSetChanged();
			}
		};
	}

	public static class ReposHolder extends RecyclerView.ViewHolder {
		private final ListRepositoriesBinding binding;
		private final ReposListAdapter adapter;
		private final Context context;
		private org.gitnex.tea4j.v2.models.Repository repo;

		ReposHolder(ListRepositoriesBinding binding, ReposListAdapter adapter) {
			super(binding.getRoot());
			this.binding = binding;
			this.adapter = adapter;
			this.context = binding.getRoot().getContext();
			setupClick();
		}

		private void setupClick() {
			binding.getRoot()
					.setOnClickListener(
							v -> {
								RepositoryContext repoCtx = new RepositoryContext(repo, context);
								repoCtx.saveToDB(context);
								Intent intent =
										repoCtx.getIntent(context, RepoDetailActivity.class);
								if (adapter.isUserOrg) intent.putExtra("openedFromUserOrg", true);
								context.startActivity(intent);
							});
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository repository) {
			this.repo = repository;

			binding.repoOpenIssues.setText(String.valueOf(repo.getOpenIssuesCount()));
			binding.repoOpenPRs.setText(String.valueOf(repo.getOpenPrCounter()));

			String fullName = repo.getFullName();
			String[] nameParts = fullName.split("/");
			if (nameParts.length > 0) binding.orgName.setText(nameParts[0]);
			if (nameParts.length > 1) binding.repoName.setText(nameParts[1]);

			binding.repoIsArchivedFrame.setVisibility(repo.isArchived() ? View.VISIBLE : View.GONE);
			binding.repoIsPrivate.setVisibility(repo.isPrivate() ? View.VISIBLE : View.GONE);
			binding.repoStars.setText(AppUtil.numberFormatter(repo.getStarsCount()));

			if (repo.getLanguage() != null && !repo.getLanguage().trim().isEmpty()) {
				binding.repoLanguageFrame.setVisibility(View.VISIBLE);
				binding.repoStars2.setText(repo.getLanguage());

				int colorRes = LanguageColor.languageColor(repo.getLanguage());
				int bgColor = context.getColor(colorRes);

				binding.repoLanguageFrame.setCardBackgroundColor(ColorStateList.valueOf(bgColor));

				if (AppUtil.isLightColor(bgColor)) {
					binding.repoStars2.setTextColor(Color.BLACK);
				} else {
					binding.repoStars2.setTextColor(Color.WHITE);
				}
			} else {
				binding.repoLanguageFrame.setVisibility(View.GONE);
			}

			if (repo.getUpdatedAt() != null) {
				binding.repoLastUpdated.setText(
						TimeHelper.formatTime(repo.getUpdatedAt(), Locale.getDefault()));
				binding.repoLastUpdated.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												repo.getUpdatedAt(), Locale.getDefault())));
			}

			if (repo.getDescription() != null && !repo.getDescription().isEmpty()) {
				binding.repoDescription.setVisibility(View.VISIBLE);
				binding.repoDescription.setText(repo.getDescription());
			} else {
				binding.repoDescription.setVisibility(View.GONE);
			}

			loadAvatar(repo);
		}

		private void loadAvatar(org.gitnex.tea4j.v2.models.Repository repository) {
			String label =
					(repository.getFullName() != null)
							? repository.getFullName()
							: repository.getName();
			Drawable placeholder = AvatarGenerator.getLetterAvatar(context, label, 44);
			Glide.with(context)
					.load(repository.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(placeholder)
					.centerCrop()
					.into(binding.imageAvatar);
		}
	}
}
