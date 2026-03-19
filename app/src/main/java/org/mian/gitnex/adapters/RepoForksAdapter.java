package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class RepoForksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
		implements Filterable {

	private final List<org.gitnex.tea4j.v2.models.Repository> forksListFull;
	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> forksList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	private final Filter forksFilter =
			new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					List<org.gitnex.tea4j.v2.models.Repository> filteredList = new ArrayList<>();

					if (constraint == null || constraint.length() == 0) {
						filteredList.addAll(forksListFull);
					} else {
						String filterPattern = constraint.toString().toLowerCase().trim();

						for (org.gitnex.tea4j.v2.models.Repository item : forksListFull) {
							if (item.getFullName().toLowerCase().contains(filterPattern)
									|| item.getOwner()
											.getLogin()
											.toLowerCase()
											.contains(filterPattern)
									|| item.getOwner()
											.getEmail()
											.toLowerCase()
											.contains(filterPattern)) {
								filteredList.add(item);
							}
						}
					}

					FilterResults results = new FilterResults();
					results.values = filteredList;

					return results;
				}

				@Override
				protected void publishResults(CharSequence constraint, FilterResults results) {

					forksList.clear();
					forksList.addAll((List) results.values);
					notifyDataChanged();
				}
			};

	public RepoForksAdapter(
			Context ctx, List<org.gitnex.tea4j.v2.models.Repository> forksListMain) {

		this.context = ctx;
		this.forksList = forksListMain;
		forksListFull = new ArrayList<>(forksList);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new RepoForksAdapter.ForksHolder(
				inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}
		((RepoForksAdapter.ForksHolder) holder).bindData(forksList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return forksList.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
		forksList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return forksFilter;
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	public class ForksHolder extends RecyclerView.ViewHolder {

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private final TextView repoStars;
		private final TextView repoLastUpdated;
		private org.gitnex.tea4j.v2.models.Repository userRepositories;
		private CheckBox isRepoAdmin;

		ForksHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository forksModel) {

			this.userRepositories = forksModel;
			orgName.setText(forksModel.getFullName().split("/")[0]);
			repoName.setText(forksModel.getFullName().split("/")[1]);
			repoStars.setText(AppUtil.numberFormatter(forksModel.getStarsCount()));

			Drawable placeholder =
					AvatarGenerator.getLetterAvatar(context, forksModel.getName(), 44);

			if (forksModel.getAvatarUrl() != null && !forksModel.getAvatarUrl().isEmpty()) {
				Glide.with(context)
						.load(forksModel.getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(placeholder)
						.error(placeholder)
						.centerCrop()
						.into(image);
			} else {
				image.setImageDrawable(placeholder);
			}

			if (forksModel.getUpdatedAt() != null) {
				repoLastUpdated.setText(
						context.getString(
								R.string.lastUpdatedAt,
								TimeHelper.formatTime(
										forksModel.getUpdatedAt(), Locale.getDefault())));

				repoLastUpdated.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												forksModel.getUpdatedAt(), Locale.getDefault())));
			} else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if (!forksModel.getDescription().isEmpty()) {
				repoDescription.setText(forksModel.getDescription());
			} else {
				repoDescription.setText(context.getString(R.string.noDataDescription));
			}

			if (isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(forksModel.getPermissions().isAdmin());

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();

						RepositoryContext repo = new RepositoryContext(userRepositories, context);
						repo.saveToDB(context);
						Intent intent = repo.getIntent(context, RepoDetailActivity.class);

						context.startActivity(intent);
					});
		}
	}
}
