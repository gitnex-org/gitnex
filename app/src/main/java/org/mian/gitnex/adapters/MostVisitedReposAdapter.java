package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.ListMostVisitedReposBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class MostVisitedReposAdapter
		extends RecyclerView.Adapter<MostVisitedReposAdapter.MostVisitedViewHolder> {

	private List<Repository> mostVisitedReposList;
	private final Context ctx;

	public MostVisitedReposAdapter(Context ctx, List<Repository> reposList) {
		this.ctx = ctx;
		this.mostVisitedReposList = reposList;
	}

	@NonNull @Override
	public MostVisitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListMostVisitedReposBinding binding =
				ListMostVisitedReposBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new MostVisitedViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull MostVisitedViewHolder holder, int position) {
		holder.bind(mostVisitedReposList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return mostVisitedReposList.size();
	}

	public class MostVisitedViewHolder extends RecyclerView.ViewHolder {
		private final ListMostVisitedReposBinding binding;
		private Repository repository;

		private MostVisitedViewHolder(ListMostVisitedReposBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
			setupListeners();
		}

		private void setupListeners() {
			binding.getRoot()
					.setOnClickListener(
							v -> {
								RepositoryContext repoCtx =
										new RepositoryContext(
												repository.getRepositoryOwner(),
												repository.getRepositoryName(),
												ctx);
								Intent intent = repoCtx.getIntent(ctx, RepoDetailActivity.class);
								ctx.startActivity(intent);
							});

			binding.repoInfoEndFrame.setOnClickListener(v -> showResetDialog());
		}

		void bind(Repository repo) {
			this.repository = repo;
			binding.orgName.setText(repo.getRepositoryOwner());
			binding.repoName.setText(repo.getRepositoryName());
			binding.mostVisited.setText(AppUtil.numberFormatter(repo.getMostVisited()));

			binding.image.setImageDrawable(
					AvatarGenerator.getLetterAvatar(ctx, repo.getRepositoryOwner(), 32));
		}

		private void showResetDialog() {
			new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert)
					.setTitle(R.string.reset)
					.setMessage(
							ctx.getString(
									R.string.resetCounterDialogMessage,
									repository.getRepositoryName()))
					.setPositiveButton(
							R.string.reset,
							(dialog, which) -> {
								int pos = getBindingAdapterPosition();
								int repoId = repository.getRepositoryId();

								mostVisitedReposList.remove(pos);
								notifyItemRemoved(pos);
								notifyItemRangeChanged(pos, mostVisitedReposList.size());
								Toasty.show(ctx, ctx.getString(R.string.resetMostReposCounter));

								RepositoriesApi api =
										BaseApi.getInstance(ctx, RepositoriesApi.class);
								if (api != null) {
									api.updateRepositoryMostVisited(0, repoId);
								}
							})
					.setNeutralButton(R.string.cancelButton, null)
					.show();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Repository> newList) {
		this.mostVisitedReposList = newList;
		notifyDataSetChanged();
	}
}
