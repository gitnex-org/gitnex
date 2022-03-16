package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class ReposListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<UserRepositories> reposList;
	private final List<UserRepositories> reposListFull;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final TinyDB tinyDb;

	public ReposListAdapter(List<UserRepositories> reposListMain, Context ctx) {
		this.context = ctx;
		this.reposList = reposListMain;
		reposListFull = new ArrayList<>(reposList);
		this.tinyDb = TinyDB.getInstance(context);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if(viewType == TYPE_LOAD) {
			return new ReposListAdapter.ReposHolder(inflater.inflate(R.layout.list_repositories, parent, false));
		}
		else {
			return new ReposListAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((ReposListAdapter.ReposHolder) holder).bindData(reposList.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(reposList.get(position).getFullName() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	class ReposHolder extends RecyclerView.ViewHolder {

		private UserRepositories userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;
		private final View spacerView;

		ReposHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
			spacerView = itemView.findViewById(R.id.spacerView);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				RepositoryContext repo = new RepositoryContext(userRepositories, context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				int currentActiveAccountId = TinyDB.getInstance(context).getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				assert repositoryData != null;
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repo.getOwner(), repo.getName());

				if(count == 0) {
					long id = repositoryData.insertRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId((int) id);
				}
				else {
					Repository data = repositoryData.getRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId(data.getRepositoryId());
				}

				context.startActivity(intent);
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(UserRepositories repositories) {

			this.userRepositories = repositories;
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");
			orgName.setText(repositories.getFullName().split("/")[0]);
			repoName.setText(repositories.getFullName().split("/")[1]);
			repoStars.setText(repositories.getStars_count());

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(repositories.getName());
			String firstCharacter = String.valueOf(repositories.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

			if(repositories.getAvatar_url() != null) {
				if(!repositories.getAvatar_url().equals("")) {
					PicassoService.getInstance(context).get().load(repositories.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);
				}
				else {
					image.setImageDrawable(drawable);
				}
			}
			else {
				image.setImageDrawable(drawable);
			}

			if(repositories.getUpdated_at() != null) {

				switch(timeFormat) {
					case "pretty": {
						PrettyTime prettyTime = new PrettyTime(locale);
						String createdTime = prettyTime.format(repositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repositories.getUpdated_at()), context));
						break;
					}
					case "normal": {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(repositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
					case "normal1": {
						DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(repositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
				}
			}
			else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if(!repositories.getDescription().equals("")) {
				repoDescription.setVisibility(View.VISIBLE);
				repoDescription.setText(repositories.getDescription());
				spacerView.setVisibility(View.GONE);
			}
			else {
				repoDescription.setVisibility(View.GONE);
				spacerView.setVisibility(View.VISIBLE);
			}

			if(isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(repositories.getPermissions().isAdmin());

		}
	}

	static class LoadHolder extends RecyclerView.ViewHolder {
		LoadHolder(View itemView) {
			super(itemView);
		}
	}

	/*static class ReposViewHolder extends RecyclerView.ViewHolder {

		private UserRepositories userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;
		private final View spacerView;

		private ReposViewHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
			spacerView = itemView.findViewById(R.id.spacerView);

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				RepositoryContext repo = new RepositoryContext(userRepositories, context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				int currentActiveAccountId = TinyDB.getInstance(context).getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				assert repositoryData != null;
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repo.getOwner(), repo.getName());

				if(count == 0) {
					long id = repositoryData.insertRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId((int) id);
				}
				else {
					Repository data = repositoryData.getRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
					repo.setRepositoryId(data.getRepositoryId());
				}

				context.startActivity(intent);
			});
		}

	}*/

	/*public ReposListAdapter(Context ctx, List<UserRepositories> reposListMain) {

		this.context = ctx;
		this.reposList = reposListMain;
		reposListFull = new ArrayList<>(reposList);
	}

	@NonNull
	@Override
	public ReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
		return new ReposViewHolder(v);
	}*/

	/*@Override
	public void onBindViewHolder(@NonNull ReposViewHolder holder, int position) {

		TinyDB tinyDb = TinyDB.getInstance(context);
		UserRepositories currentItem = reposList.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		Locale locale = context.getResources().getConfiguration().locale;
		String timeFormat = tinyDb.getString("dateFormat", "pretty");
		holder.userRepositories = currentItem;
		holder.orgName.setText(currentItem.getFullName().split("/")[0]);
		holder.repoName.setText(currentItem.getFullName().split("/")[1]);
		holder.repoStars.setText(currentItem.getStars_count());

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(currentItem.getName());
		String firstCharacter = String.valueOf(currentItem.getFullName().charAt(0));

		TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

		if(currentItem.getAvatar_url() != null) {
			if(!currentItem.getAvatar_url().equals("")) {
				PicassoService.getInstance(context).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(holder.image);
			}
			else {
				holder.image.setImageDrawable(drawable);
			}
		}
		else {
			holder.image.setImageDrawable(drawable);
		}

		if(currentItem.getUpdated_at() != null) {

			switch(timeFormat) {
				case "pretty": {
					PrettyTime prettyTime = new PrettyTime(locale);
					String createdTime = prettyTime.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
					holder.repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdated_at()), context));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
					String createdTime = formatter.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
					String createdTime = formatter.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
					break;
				}
			}
		}
		else {
			holder.repoLastUpdated.setVisibility(View.GONE);
		}

		if(!currentItem.getDescription().equals("")) {
			holder.repoDescription.setVisibility(View.VISIBLE);
			holder.repoDescription.setText(currentItem.getDescription());
			holder.spacerView.setVisibility(View.GONE);
		}
		else {
			holder.repoDescription.setVisibility(View.GONE);
			holder.spacerView.setVisibility(View.VISIBLE);
		}

		if(holder.isRepoAdmin == null) {
			holder.isRepoAdmin = new CheckBox(context);
		}
		holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());
	}

	@Override
	public int getItemCount() {

		return reposList.size();
	}*/

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if(!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
		void onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<UserRepositories> list) {
		reposList = list;
		notifyDataChanged();
	}

	@Override
	public Filter getFilter() {
		return reposFilter;
	}

	private final Filter reposFilter = new Filter() {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			List<UserRepositories> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {
				filteredList.addAll(reposListFull);
			}
			else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for(UserRepositories item : reposListFull) {
					if(item.getFullName().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
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

			reposList.clear();
			reposList.addAll((List) results.values);
			notifyDataChanged();
		}
	};
}
