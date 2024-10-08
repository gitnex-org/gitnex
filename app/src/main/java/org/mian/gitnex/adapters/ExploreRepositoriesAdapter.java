package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
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
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
 */
public class ExploreRepositoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final TinyDB tinyDb;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public ExploreRepositoriesAdapter(
			List<org.gitnex.tea4j.v2.models.Repository> dataList, Context ctx) {
		this.context = ctx;
		this.reposList = dataList;
		this.tinyDb = TinyDB.getInstance(context);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ExploreRepositoriesAdapter.RepositoriesHolder(
				inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((ExploreRepositoriesAdapter.RepositoriesHolder) holder).bindData(reposList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
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
		reposList = list;
		notifyDataChanged();
	}

	class RepositoriesHolder extends RecyclerView.ViewHolder {

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private final TextView repoStars;
		private final TextView repoLastUpdated;
		private final View spacerView;
		private org.gitnex.tea4j.v2.models.Repository userRepositories;
		private CheckBox isRepoAdmin;

		RepositoriesHolder(View itemView) {
			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
			spacerView = itemView.findViewById(R.id.spacerView);

			itemView.setOnClickListener(
					v -> {
						Context context = v.getContext();
						RepositoryContext repo = new RepositoryContext(userRepositories, context);
						repo.saveToDB(context);
						Intent intent = repo.getIntent(context, RepoDetailActivity.class);

						context.startActivity(intent);
					});
		}

		void bindData(org.gitnex.tea4j.v2.models.Repository userRepositories) {
			this.userRepositories = userRepositories;

			Locale locale = context.getResources().getConfiguration().locale;

			orgName.setText(userRepositories.getFullName().split("/")[0]);
			repoName.setText(userRepositories.getFullName().split("/")[1]);
			repoStars.setText(AppUtil.numberFormatter(userRepositories.getStarsCount()));

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(userRepositories.getName());
			String firstCharacter = String.valueOf(userRepositories.getFullName().charAt(0));

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

			if (userRepositories.getAvatarUrl() != null) {
				if (!userRepositories.getAvatarUrl().isEmpty()) {
					Glide.with(context)
							.load(userRepositories.getAvatarUrl())
							.diskCacheStrategy(DiskCacheStrategy.ALL)
							.placeholder(R.drawable.loader_animated)
							.centerCrop()
							.into(image);
				} else {
					image.setImageDrawable(drawable);
				}
			} else {
				image.setImageDrawable(drawable);
			}

			if (userRepositories.getUpdatedAt() != null) {
				repoLastUpdated.setText(
						context.getString(
								R.string.lastUpdatedAt,
								TimeHelper.formatTime(userRepositories.getUpdatedAt(), locale)));
				repoLastUpdated.setOnClickListener(
						new ClickListener(
								TimeHelper.customDateFormatForToastDateFormat(
										userRepositories.getUpdatedAt()),
								context));
			} else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if (!userRepositories.getDescription().isEmpty()) {
				repoDescription.setVisibility(View.VISIBLE);
				repoDescription.setText(userRepositories.getDescription());
				spacerView.setVisibility(View.GONE);
			} else {
				repoDescription.setVisibility(View.GONE);
				spacerView.setVisibility(View.VISIBLE);
			}

			if (isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(userRepositories.getPermissions().isAdmin());
		}
	}
}
