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
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OpenRepoInBrowserActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoForksActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.WatchInfo;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepoForksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context ctx;
	private final int TYPE_LOAD = 0;
	private List<UserRepositories> forksList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;

	public RepoForksAdapter(Context ctx, List<UserRepositories> forksListMain) {

		this.ctx = ctx;
		this.forksList = forksListMain;

	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		LayoutInflater inflater = LayoutInflater.from(ctx);

		if(viewType == TYPE_LOAD) {
			return new RepoForksAdapter.ForksHolder(inflater.inflate(R.layout.list_repositories, parent, false));
		}
		else {
			return new LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}

	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {

			isLoading = true;
			loadMoreListener.onLoadMore();

		}

		if(getItemViewType(position) == TYPE_LOAD) {

			((RepoForksAdapter.ForksHolder) holder).bindData(forksList.get(position));

		}

	}

	@Override
	public int getItemViewType(int position) {

		if(forksList.get(position).getName() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}

	}

	@Override
	public int getItemCount() {

		return forksList.size();

	}

	class ForksHolder extends RecyclerView.ViewHolder {

		private ImageView image;
		private TextView repoName;
		private TextView repoDescription;
		private TextView fullName;
		private CheckBox isRepoAdmin;
		private ImageView repoPrivatePublic;
		private TextView repoStars;
		private TextView repoForks;
		private TextView repoOpenIssuesCount;
		private TextView repoType;
		private LinearLayout archiveRepo;
		private TextView repoBranch;
		private ImageView reposDropdownMenu;

		ForksHolder(View itemView) {

			super(itemView);

			repoName = itemView.findViewById(R.id.repoName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			fullName = itemView.findViewById(R.id.repoFullName);
			repoPrivatePublic = itemView.findViewById(R.id.imageRepoType);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoForks = itemView.findViewById(R.id.repoForks);
			repoOpenIssuesCount = itemView.findViewById(R.id.repoOpenIssuesCount);
			reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);
			repoType = itemView.findViewById(R.id.repoType);
			archiveRepo = itemView.findViewById(R.id.archiveRepoFrame);
			repoBranch = itemView.findViewById(R.id.repoBranch);

		}

		@SuppressLint("SetTextI18n")
		void bindData(UserRepositories forksModel) {

			repoDescription.setVisibility(View.GONE);
			repoBranch.setText(forksModel.getDefault_branch());

			ColorGenerator generator = ColorGenerator.MATERIAL;
			int color = generator.getColor(forksModel.getName());
			String firstCharacter = String.valueOf(forksModel.getName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28)
				.endConfig().buildRoundRect(firstCharacter, color, 3);

			if(forksModel.getAvatar_url() != null) {
				if(!forksModel.getAvatar_url().equals("")) {
					PicassoService.getInstance(ctx).get().load(forksModel.getAvatar_url()).placeholder(R.drawable.loader_animated)
						.transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(image);
				}
				else {
					image.setImageDrawable(drawable);
				}
			}
			else {
				image.setImageDrawable(drawable);
			}

			repoName.setText(forksModel.getName());

			if(!forksModel.getDescription().equals("")) {
				repoDescription.setVisibility(View.VISIBLE);
				repoDescription.setText(forksModel.getDescription());
			}
			fullName.setText(forksModel.getFullName());

			if(forksModel.getPrivateFlag()) {
				repoPrivatePublic.setImageResource(R.drawable.ic_lock);
				repoType.setText(R.string.strPrivate);
			}
			else {
				repoPrivatePublic.setVisibility(View.GONE);
				repoType.setText(R.string.strPublic);
			}

			repoStars.setText(forksModel.getStars_count());
			repoForks.setText(forksModel.getForks_count());
			repoOpenIssuesCount.setText(forksModel.getOpen_issues_count());

			if(isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(ctx);
			}
			isRepoAdmin.setChecked(forksModel.getPermissions().isAdmin());

			if(forksModel.isArchived()) {
				archiveRepo.setVisibility(View.VISIBLE);
			}
			else {
				archiveRepo.setVisibility(View.GONE);
			}

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();
				TextView repoFullName = v.findViewById(R.id.repoFullName);
				TextView repoType_ = v.findViewById(R.id.repoType);

				Intent intent = new Intent(context, RepoDetailActivity.class);
				intent.putExtra("repoFullName", repoFullName.getText().toString());

				TinyDB tinyDb = new TinyDB(context);
				tinyDb.putString("repoFullName", repoFullName.getText().toString());
				tinyDb.putString("repoType", repoType_.getText().toString());
				//tinyDb.putBoolean("resumeIssues", true);
				tinyDb.putBoolean("isRepoAdmin", isRepoAdmin.isChecked());
				tinyDb.putString("repoBranch", repoBranch.getText().toString());

				String[] parts = repoFullName.getText().toString().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = new RepositoriesApi(context);

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

					final String instanceUrl = tinyDb.getString("instanceUrl");
					final String token = "token " + tinyDb.getString(tinyDb.getString("loginUid") + "-token");

					WatchInfo watch = new WatchInfo();

					Call<WatchInfo> call;

					call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface().checkRepoWatchStatus(token, repoOwner, repoName);

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

			reposDropdownMenu.setOnClickListener(v -> {

				final Context context = v.getContext();

				@SuppressLint("InflateParams") View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_repository_in_list, null);

				TextView repoOpenInBrowser = view.findViewById(R.id.repoOpenInBrowser);
				TextView repoStargazers = view.findViewById(R.id.repoStargazers);
				TextView repoWatchers = view.findViewById(R.id.repoWatchers);
				TextView repoForksList = view.findViewById(R.id.repoForksList);
				TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

				bottomSheetHeader
					.setText(String.format("%s / %s", fullName.getText().toString().split("/")[0], fullName.getText().toString().split("/")[1]));
				BottomSheetDialog dialog = new BottomSheetDialog(context);
				dialog.setContentView(view);
				dialog.show();

				repoOpenInBrowser.setOnClickListener(openInBrowser -> {

					Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
					intentOpenInBrowser.putExtra("repoFullNameBrowser", fullName.getText());
					context.startActivity(intentOpenInBrowser);
					dialog.dismiss();

				});

				repoStargazers.setOnClickListener(stargazers -> {

					Intent intent = new Intent(context, RepoStargazersActivity.class);
					intent.putExtra("repoFullNameForStars", fullName.getText());
					context.startActivity(intent);
					dialog.dismiss();

				});

				repoWatchers.setOnClickListener(watchers -> {

					Intent intentW = new Intent(context, RepoWatchersActivity.class);
					intentW.putExtra("repoFullNameForWatchers", fullName.getText());
					context.startActivity(intentW);
					dialog.dismiss();

				});

				repoForksList.setOnClickListener(watchers -> {

					Intent intentW = new Intent(context, RepoForksActivity.class);
					intentW.putExtra("repoFullNameForForks", fullName.getText());
					context.startActivity(intentW);
					dialog.dismiss();

				});

			});

		}

	}

	static class LoadHolder extends RecyclerView.ViewHolder {

		LoadHolder(View itemView) {

			super(itemView);
		}

	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {

		isMoreDataAvailable = moreDataAvailable;

	}

	public void notifyDataChanged() {

		notifyDataSetChanged();
		isLoading = false;

	}

	public interface OnLoadMoreListener {

		void onLoadMore();

	}

	public void setLoadMoreListener(RepoForksAdapter.OnLoadMoreListener loadMoreListener) {

		this.loadMoreListener = loadMoreListener;

	}

	public void updateList(List<UserRepositories> list) {

		forksList = list;
		notifyDataSetChanged();
	}

}
