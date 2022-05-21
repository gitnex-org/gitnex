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
 * @author M M Arif
 */

public class ReposListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> reposList;
	private final List<org.gitnex.tea4j.v2.models.Repository> reposListFull;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final TinyDB tinyDb;

	public ReposListAdapter(List<org.gitnex.tea4j.v2.models.Repository> reposListMain, Context ctx) {
		this.context = ctx;
		this.reposList = reposListMain;
		reposListFull = new ArrayList<>(reposList);
		this.tinyDb = TinyDB.getInstance(context);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ReposListAdapter.ReposHolder(inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((ReposListAdapter.ReposHolder) holder).bindData(reposList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	class ReposHolder extends RecyclerView.ViewHolder {

		private org.gitnex.tea4j.v2.models.Repository userRepositories;

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
				repo.saveToDB(context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				context.startActivity(intent);
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository repositories) {

			this.userRepositories = repositories;
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");
			orgName.setText(repositories.getFullName().split("/")[0]);
			repoName.setText(repositories.getFullName().split("/")[1]);
			repoStars.setText(String.valueOf(repositories.getStarsCount()));

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(repositories.getName());
			String firstCharacter = String.valueOf(repositories.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

			if(repositories.getAvatarUrl() != null) {
				if(!repositories.getAvatarUrl().equals("")) {
					PicassoService.getInstance(context).get().load(repositories.getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);
				}
				else {
					image.setImageDrawable(drawable);
				}
			}
			else {
				image.setImageDrawable(drawable);
			}

			if(repositories.getUpdatedAt() != null) {

				switch(timeFormat) {
					case "pretty": {
						PrettyTime prettyTime = new PrettyTime(locale);
						String createdTime = prettyTime.format(repositories.getUpdatedAt());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repositories.getUpdatedAt()), context));
						break;
					}
					case "normal": {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(repositories.getUpdatedAt());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
					case "normal1": {
						DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(repositories.getUpdatedAt());
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

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
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

			List<org.gitnex.tea4j.v2.models.Repository> filteredList = new ArrayList<>();

			if(constraint == null || constraint.length() == 0) {
				filteredList.addAll(reposListFull);
			}
			else {
				String filterPattern = constraint.toString().toLowerCase().trim();

				for(org.gitnex.tea4j.v2.models.Repository item : reposListFull) {
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
