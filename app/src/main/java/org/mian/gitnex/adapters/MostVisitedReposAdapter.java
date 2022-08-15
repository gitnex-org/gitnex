package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;

/**
 * @author M M Arif
 */

public class MostVisitedReposAdapter extends RecyclerView.Adapter<MostVisitedReposAdapter.MostVisitedViewHolder> {

	private List<Repository> mostVisitedReposList;

	public MostVisitedReposAdapter(List<Repository> reposListMain) {
		this.mostVisitedReposList = reposListMain;
	}

	@NonNull
	@Override
	public MostVisitedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_most_visited_repos, parent, false);
		return new MostVisitedViewHolder(v);
	}

	@SuppressLint("DefaultLocale")
	@Override
	public void onBindViewHolder(@NonNull MostVisitedViewHolder holder, int position) {

		Repository currentItem = mostVisitedReposList.get(position);
		holder.repository = currentItem;

		ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
		int color = generator.getColor(currentItem.getRepositoryOwner());
		String firstCharacter = String.valueOf(currentItem.getRepositoryOwner().charAt(0));
		TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 14);

		holder.image.setImageDrawable(drawable);
		holder.orgName.setText(currentItem.getRepositoryOwner());
		holder.repoName.setText(currentItem.getRepositoryName());
		holder.mostVisited.setText(AppUtil.numberFormatter(currentItem.getMostVisited()));
	}

	@Override
	public int getItemCount() {
		return mostVisitedReposList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<Repository> list) {

		mostVisitedReposList = list;
		notifyDataChanged();
	}

	static class MostVisitedViewHolder extends RecyclerView.ViewHolder {

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView mostVisited;
		private Repository repository;

		private MostVisitedViewHolder(View itemView) {

			super(itemView);

			image = itemView.findViewById(R.id.image);
			repoName = itemView.findViewById(R.id.repo_name);
			orgName = itemView.findViewById(R.id.org_name);
			mostVisited = itemView.findViewById(R.id.most_visited);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();
				RepositoryContext repositoryContext = new RepositoryContext(repository.getRepositoryOwner(), repository.getRepositoryName(), context);
				Intent intent = repositoryContext.getIntent(context, RepoDetailActivity.class);
				context.startActivity(intent);
			});
		}

	}

}
