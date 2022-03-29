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
import java.util.List;
import java.util.Locale;

/**
 * @author M M Arif
 */

public class ExploreRepositoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<UserRepositories> reposList;
	private Runnable loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final TinyDB tinyDb;

	public ExploreRepositoriesAdapter(List<UserRepositories> dataList, Context ctx) {
		this.context = ctx;
		this.reposList = dataList;
		this.tinyDb = TinyDB.getInstance(context);
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ExploreRepositoriesAdapter.RepositoriesHolder(inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
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

	class RepositoriesHolder extends RecyclerView.ViewHolder {
		private UserRepositories userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;
		private final View spacerView;

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

			itemView.setOnClickListener(v -> {
				Context context = v.getContext();
				RepositoryContext repo = new RepositoryContext(userRepositories, context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
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
		void bindData(UserRepositories userRepositories) {
			this.userRepositories = userRepositories;

			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);
			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");

			orgName.setText(userRepositories.getFullName().split("/")[0]);
			repoName.setText(userRepositories.getFullName().split("/")[1]);
			repoStars.setText(userRepositories.getStars_count());

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(userRepositories.getName());
			String firstCharacter = String.valueOf(userRepositories.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 3);

			if(userRepositories.getAvatar_url() != null) {
				if(!userRepositories.getAvatar_url().equals("")) {
					PicassoService.getInstance(context).get().load(userRepositories.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);
				}
				else {
					image.setImageDrawable(drawable);
				}
			}
			else {
				image.setImageDrawable(drawable);
			}

			if(userRepositories.getUpdated_at() != null) {

				switch(timeFormat) {
					case "pretty": {
						PrettyTime prettyTime = new PrettyTime(locale);
						String createdTime = prettyTime.format(userRepositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(userRepositories.getUpdated_at()), context));
						break;
					}
					case "normal": {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(userRepositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
					case "normal1": {
						DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(userRepositories.getUpdated_at());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
				}
			}
			else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if(!userRepositories.getDescription().equals("")) {
				repoDescription.setVisibility(View.VISIBLE);
				repoDescription.setText(userRepositories.getDescription());
				spacerView.setVisibility(View.GONE);
			}
			else {
				repoDescription.setVisibility(View.GONE);
				spacerView.setVisibility(View.VISIBLE);
			}

			if(isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(userRepositories.getPermissions().isAdmin());
		}
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

	public void updateList(List<UserRepositories> list) {
		reposList = list;
		notifyDataChanged();
	}
}
