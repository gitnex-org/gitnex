package org.mian.gitnex.adapters;

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
import org.gitnex.tea4j.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MyReposListAdapter extends RecyclerView.Adapter<MyReposListAdapter.MyReposViewHolder> implements Filterable {

	private final List<UserRepositories> reposList;
	private final Context context;
	private final List<UserRepositories> reposListFull;

	static class MyReposViewHolder extends RecyclerView.ViewHolder {

		private UserRepositories userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;

		private MyReposViewHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();
				TinyDB tinyDb = TinyDB.getInstance(context);

				Intent intent = new Intent(context, RepoDetailActivity.class);
				intent.putExtra("repoFullName", userRepositories.getFullName());

				tinyDb.putString("repoFullName", userRepositories.getFullName());
				//tinyDb.putBoolean("resumeIssues", true);
				tinyDb.putBoolean("isRepoAdmin", isRepoAdmin.isChecked());
				tinyDb.putString("repoBranch", userRepositories.getDefault_branch());

				if(userRepositories.getPrivateFlag()) {
					tinyDb.putString("repoType", context.getResources().getString(R.string.strPrivate));
				}
				else {
					tinyDb.putString("repoType", context.getResources().getString(R.string.strPublic));
				}

				String[] parts = userRepositories.getFullName().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				//RepositoriesRepository.deleteRepositoriesByAccount(currentActiveAccountId);
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

				if(count == 0) {

					long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", id);
				}
				else {

					Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", data.getRepositoryId());
				}

				//store if user is watching this repo
				{

					final String token = "token " + tinyDb.getString(tinyDb.getString("loginUid") + "-token");

					WatchInfo watch = new WatchInfo();

					Call<WatchInfo> call;

					call = RetrofitClient.getApiInterface(context).checkRepoWatchStatus(token, repoOwner, repoName);

					call.enqueue(new Callback<WatchInfo>() {

						@Override
						public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

							if(response.isSuccessful()) {

								assert response.body() != null;
								tinyDb.putBoolean("repoWatch", response.body().getSubscribed());

							}
							else {

								tinyDb.putBoolean("repoWatch", false);

								if(response.code() != 404) {

									Toasty.error(context, context.getString(R.string.genericApiStatusError));

								}

							}

						}

						@Override
						public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

							tinyDb.putBoolean("repoWatch", false);
							Toasty.error(context, context.getString(R.string.genericApiStatusError));

						}
					});

				}

				context.startActivity(intent);

			});
		}

	}

	public MyReposListAdapter(Context ctx, List<UserRepositories> reposListMain) {

		this.context = ctx;
		this.reposList = reposListMain;
		reposListFull = new ArrayList<>(reposList);
	}

	@NonNull
	@Override
	public MyReposListAdapter.MyReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
		return new MyReposListAdapter.MyReposViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull MyReposListAdapter.MyReposViewHolder holder, int position) {

		TinyDB tinyDb = TinyDB.getInstance(context);
		UserRepositories currentItem = reposList.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		String locale = tinyDb.getString("locale");
		String timeFormat = tinyDb.getString("dateFormat");
		holder.userRepositories = currentItem;
		holder.orgName.setText(currentItem.getFullName().split("/")[0]);
		holder.repoName.setText(currentItem.getFullName().split("/")[1]);
		holder.repoStars.setText(currentItem.getStars_count());

		ColorGenerator generator = ColorGenerator.MATERIAL;
		int color = generator.getColor(currentItem.getName());
		String firstCharacter = String.valueOf(currentItem.getName().charAt(0));

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
					PrettyTime prettyTime = new PrettyTime(new Locale(locale));
					String createdTime = prettyTime.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
					holder.repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getUpdated_at()), context));
					break;
				}
				case "normal": {
					DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
					String createdTime = formatter.format(currentItem.getUpdated_at());
					holder.repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
					break;
				}
				case "normal1": {
					DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
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
			holder.repoDescription.setText(currentItem.getDescription());
		}

		if(holder.isRepoAdmin == null) {
			holder.isRepoAdmin = new CheckBox(context);
		}
		holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());
	}

	@Override
	public int getItemCount() {

		return reposList.size();
	}

	@Override
	public Filter getFilter() {

		return myReposFilter;
	}

	private final Filter myReposFilter = new Filter() {

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
			notifyDataSetChanged();
		}
	};

}
