package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.gitnex.tea4j.v2.models.Release;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentReleasesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.structs.FragmentRefreshListener;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder> {

	private final Context context;
	private final String repoOwner;
	private final String repoName;
	private final FragmentRefreshListener startDownload;
	private final FragmentReleasesBinding fragmentReleasesBinding;
	private List<Release> releasesList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public ReleasesAdapter(Context ctx, List<Release> releasesMain, FragmentRefreshListener startDownload, String repoOwner, String repoName, FragmentReleasesBinding fragmentReleasesBinding) {
		this.context = ctx;
		this.releasesList = releasesMain;
		this.startDownload = startDownload;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.fragmentReleasesBinding = fragmentReleasesBinding;
	}

	@NonNull
	@Override
	public ReleasesAdapter.ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_releases, parent, false);
		return new ReleasesAdapter.ReleasesViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull ReleasesAdapter.ReleasesViewHolder holder, int position) {

		final TinyDB tinyDb = TinyDB.getInstance(context);
		final Locale locale = context.getResources().getConfiguration().locale;
		final String timeFormat = tinyDb.getString("dateFormat", "pretty");
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		Release currentItem = releasesList.get(position);
		holder.releases = currentItem;

		holder.releaseName.setText(currentItem.getName());

		if(currentItem.isPrerelease()) {
			holder.releaseType.setBackgroundResource(R.drawable.shape_pre_release);
			holder.releaseType.setText(R.string.releaseTypePre);
		}
		else if(currentItem.isDraft()) {
			holder.releaseType.setBackgroundResource(R.drawable.shape_draft_release);
			holder.releaseType.setText(R.string.releaseDraftText);
		}
		else {
			holder.releaseType.setBackgroundResource(R.drawable.shape_stable_release);
			holder.releaseType.setText(R.string.releaseTypeStable);
		}

		if(currentItem.getAuthor().getAvatarUrl() != null) {
			PicassoService.getInstance(context).get().load(currentItem.getAuthor().getAvatarUrl()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop()
				.into(holder.authorAvatar);
		}

		holder.authorName.setText(context.getResources().getString(R.string.releasePublishedBy, currentItem.getAuthor().getLogin()));

		if(currentItem.getTagName() != null) {
			holder.releaseTag.setText(currentItem.getTagName());
		}

		if(currentItem.getPublishedAt() != null) {
			holder.releaseDate.setText(TimeHelper.formatTime(currentItem.getPublishedAt(), locale, timeFormat, context));
		}

		if(timeFormat.equals("pretty")) {
			holder.releaseDate.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getPublishedAt()), context));
		}

		if(!currentItem.getBody().equals("")) {
			Markdown.render(context, currentItem.getBody(), holder.releaseBodyContent);
		}
		else {
			holder.releaseBodyContent.setText(R.string.noReleaseBodyContent);
		}

		holder.downloadCopyFrame.setOnClickListener(v -> {

			if(holder.downloads.getVisibility() == View.GONE) {

				holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_down);
				holder.downloads.setVisibility(View.VISIBLE);
			}
			else {

				holder.downloadDropdownIcon.setImageResource(R.drawable.ic_chevron_right);
				holder.downloads.setVisibility(View.GONE);
			}

		});

		holder.releaseZipDownloadFrame.setOnClickListener(v -> startDownload.onRefresh(currentItem.getZipballUrl()));
		holder.releaseTarDownloadFrame.setOnClickListener(v -> startDownload.onRefresh(currentItem.getTarballUrl()));

		ReleasesDownloadsAdapter adapter = new ReleasesDownloadsAdapter(currentItem.getAssets(), startDownload);
		holder.downloadList.setAdapter(adapter);

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(!((RepoDetailActivity) context).repository.getPermissions().isPush()) {
			holder.optionsMenu.setVisibility(View.GONE);
		}
	}

	@Override
	public int getItemCount() {
		return releasesList.size();
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

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Release> list) {
		releasesList = list;
		notifyDataChanged();
	}

	private void updateAdapter(int position) {
		releasesList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, releasesList.size());
	}

	private void deleteRelease(final Context context, final String releaseName, final Long releaseId, final String owner, final String repo, int position) {

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder.setTitle(String.format(context.getString(R.string.deleteGenericTitle), releaseName)).setMessage(R.string.deleteReleaseConfirmation)
			.setPositiveButton(R.string.menuDeleteText, (dialog, whichButton) -> RetrofitClient.getApiInterface(context).repoDeleteRelease(owner, repo, releaseId).enqueue(new Callback<>() {

				@Override
				public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

					if(response.isSuccessful()) {
						updateAdapter(position);
						Toasty.success(context, context.getString(R.string.releaseDeleted));
						MainActivity.reloadRepos = true;
						if(getItemCount() == 0) {
							fragmentReleasesBinding.noDataReleases.setVisibility(View.VISIBLE);
						}
					}
					else if(response.code() == 403) {
						Toasty.error(context, context.getString(R.string.authorizeError));
					}
					else {
						Toasty.error(context, context.getString(R.string.genericError));
					}
				}

				@Override
				public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

					Toasty.error(context, context.getString(R.string.genericError));
				}
			})).setNeutralButton(R.string.cancelButton, null).show();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();

	}

	protected class ReleasesViewHolder extends RecyclerView.ViewHolder {

		private final TextView releaseType;
		private final TextView releaseName;
		private final ImageView authorAvatar;
		private final TextView authorName;
		private final TextView releaseTag;
		private final TextView releaseDate;
		private final TextView releaseBodyContent;
		private final LinearLayout downloadCopyFrame;
		private final LinearLayout downloads;
		private final LinearLayout releaseZipDownloadFrame;
		private final LinearLayout releaseTarDownloadFrame;
		private final ImageView downloadDropdownIcon;
		private final RecyclerView downloadList;
		private final ImageView optionsMenu;
		private Release releases;

		private ReleasesViewHolder(View itemView) {

			super(itemView);

			releaseType = itemView.findViewById(R.id.releaseType);
			releaseName = itemView.findViewById(R.id.releaseName);
			authorAvatar = itemView.findViewById(R.id.authorAvatar);
			authorName = itemView.findViewById(R.id.authorName);
			releaseTag = itemView.findViewById(R.id.releaseTag);
			TextView releaseCommitSha = itemView.findViewById(R.id.releaseCommitSha);
			releaseDate = itemView.findViewById(R.id.releaseDate);
			releaseBodyContent = itemView.findViewById(R.id.releaseBodyContent);
			downloadCopyFrame = itemView.findViewById(R.id.downloadCopyFrame);
			downloads = itemView.findViewById(R.id.downloads);
			releaseZipDownloadFrame = itemView.findViewById(R.id.releaseZipDownloadFrame);
			releaseTarDownloadFrame = itemView.findViewById(R.id.releaseTarDownloadFrame);
			downloadDropdownIcon = itemView.findViewById(R.id.downloadDropdownIcon);
			downloadList = itemView.findViewById(R.id.downloadList);
			optionsMenu = itemView.findViewById(R.id.releasesOptionsMenu);

			downloadList.setHasFixedSize(true);
			downloadList.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

			new Handler().postDelayed(() -> {
				if(!AppUtil.checkGhostUsers(releases.getAuthor().getLogin())) {

					authorAvatar.setOnClickListener(loginId -> {
						Context context = loginId.getContext();

						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", releases.getAuthor().getLogin());
						context.startActivity(intent);
					});
				}
			}, 500);

			optionsMenu.setOnClickListener(v -> {
				final Context context = v.getContext();

				View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_release_in_list, itemView.findViewById(android.R.id.content), false);

				TextView deleteRelease = view.findViewById(R.id.deleteRelease);

				BottomSheetDialog dialog = new BottomSheetDialog(context);
				dialog.setContentView(view);
				dialog.show();

				deleteRelease.setOnClickListener(v1 -> {
					deleteRelease(context, releases.getName(), releases.getId(), repoOwner, repoName, getBindingAdapterPosition());
					dialog.dismiss();
				});
			});
		}

	}

}
