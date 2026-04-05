package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Release;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListReleasesBinding;
import org.mian.gitnex.fragments.ReleasesFragment;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;

/**
 * @author mmarif
 */
public class ReleasesAdapter extends RecyclerView.Adapter<ReleasesAdapter.ReleasesViewHolder> {

	private final Context context;
	private List<Release> releasesList;
	private final boolean canDelete;
	private final ReleasesFragment.OnReleaseItemClickListener listener;

	public ReleasesAdapter(
			Context context,
			List<Release> releases,
			boolean canDelete,
			ReleasesFragment.OnReleaseItemClickListener listener) {
		this.context = context;
		this.releasesList = releases;
		this.canDelete = canDelete;
		this.listener = listener;
	}

	@NonNull @Override
	public ReleasesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListReleasesBinding binding =
				ListReleasesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ReleasesViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ReleasesViewHolder holder, int position) {
		Release release = releasesList.get(position);
		holder.bind(release);
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return releasesList != null ? releasesList.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Release> list) {
		this.releasesList = list;
		notifyDataSetChanged();
	}

	public void removeItem(int position) {
		if (position >= 0 && position < releasesList.size()) {
			releasesList.remove(position);
			notifyItemRemoved(position);
			notifyItemRangeChanged(position, releasesList.size());
		}
	}

	public class ReleasesViewHolder extends RecyclerView.ViewHolder {
		private final ListReleasesBinding binding;

		public ReleasesViewHolder(ListReleasesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(Release release) {
			String name =
					(release.getName() == null || release.getName().isEmpty())
							? release.getTagName()
							: release.getName();
			binding.releaseName.setText(name);

			setupTypeBadge(release);

			if (release.getAuthor() != null) {
				binding.authorName.setText(
						context.getString(
								R.string.releasePublishedBy, release.getAuthor().getLogin()));
				Glide.with(context)
						.load(release.getAuthor().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.circleCrop()
						.into(binding.authorAvatar);
			}

			binding.releaseTag.setText(release.getTagName());
			if (release.getPublishedAt() != null) {
				binding.releaseDate.setText(
						TimeHelper.formatTime(release.getPublishedAt(), Locale.getDefault()));
			}

			if (release.getBody() != null && !release.getBody().isEmpty()) {
				Markdown.render(context, release.getBody(), binding.releaseBodyContent);
			} else {
				binding.releaseBodyContent.setText(R.string.noReleaseBodyContent);
			}

			if (release.getAssets() != null && !release.getAssets().isEmpty()) {
				binding.downloadList.setVisibility(View.VISIBLE);
				ReleasesDownloadsAdapter downloadsAdapter =
						new ReleasesDownloadsAdapter(release.getAssets(), listener);

				binding.downloadList.setLayoutManager(new LinearLayoutManager(context));
				binding.downloadList.setAdapter(downloadsAdapter);
				binding.downloadList.setNestedScrollingEnabled(false);
			} else {
				binding.downloadList.setVisibility(View.GONE);
			}

			binding.releasesOptionsMenu.setVisibility(canDelete ? View.VISIBLE : View.GONE);
			binding.releasesOptionsMenu.setOnClickListener(
					v -> listener.onDelete(release, getBindingAdapterPosition()));

			binding.btnAssets.setOnClickListener(
					v -> {
						boolean isVisible =
								binding.downloadsContainer.getVisibility() == View.VISIBLE;
						TransitionManager.beginDelayedTransition(
								binding.card, new AutoTransition());
						binding.downloadsContainer.setVisibility(
								isVisible ? View.GONE : View.VISIBLE);
						binding.imgExpand.setImageResource(
								isVisible
										? R.drawable.ic_chevron_right
										: R.drawable.ic_chevron_down);
					});

			binding.sourceZipLayout.downloadName.setText(R.string.zipArchiveDownloadReleasesTab);
			binding.sourceZipLayout
					.getRoot()
					.setOnClickListener(v -> listener.onDownload(release.getZipballUrl()));
		}

		private void setupTypeBadge(Release release) {
			String label;
			int color;
			if (release.isPrerelease()) {
				label = context.getString(R.string.releaseTypePre).toUpperCase();
				color = ContextCompat.getColor(context, R.color.releasePre);
			} else if (release.isDraft()) {
				label = context.getString(R.string.releaseDraftText).toUpperCase();
				color = ContextCompat.getColor(context, R.color.lightYellow);
			} else {
				label = context.getString(R.string.releaseTypeStable).toUpperCase();
				color = ContextCompat.getColor(context, R.color.darkGreen);
			}
			binding.releaseTypeBadge.setImageDrawable(
					AvatarGenerator.getLabelDrawable(context, label, color, 20));
		}
	}
}
