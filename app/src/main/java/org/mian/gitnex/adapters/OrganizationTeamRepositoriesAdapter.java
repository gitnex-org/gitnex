package org.mian.gitnex.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class OrganizationTeamRepositoriesAdapter
		extends RecyclerView.Adapter<OrganizationTeamRepositoriesAdapter.TeamReposViewHolder> {

	private final List<Repository> reposList;
	private final Context context;
	private final int teamId;
	private final String orgName;
	private final String teamName;
	private final List<Repository> reposArr;

	public OrganizationTeamRepositoriesAdapter(
			List<Repository> dataList, Context ctx, int teamId, String orgName, String teamName) {
		this.context = ctx;
		this.reposList = dataList;
		this.teamId = teamId;
		this.orgName = orgName;
		this.teamName = teamName;
		reposArr = new ArrayList<>();
	}

	public class TeamReposViewHolder extends RecyclerView.ViewHolder {

		private Repository repoInfo;

		private final ImageView repoAvatar;
		private final TextView name;
		private final ImageView addRepoButtonAdd;

		private TeamReposViewHolder(View itemView) {

			super(itemView);
			repoAvatar = itemView.findViewById(R.id.userAvatar);
			name = itemView.findViewById(R.id.userFullName);
			itemView.findViewById(R.id.userName).setVisibility(View.GONE);
			addRepoButtonAdd = itemView.findViewById(R.id.addCollaboratorButtonAdd);
			ImageView addRepoButtonRemove = itemView.findViewById(R.id.addCollaboratorButtonRemove);
			// addRepoButtonAdd.setVisibility(View.VISIBLE);
			// addRepoButtonRemove.setVisibility(View.GONE);

			new Handler(Looper.getMainLooper())
					.postDelayed(OrganizationTeamRepositoriesAdapter.this::getTeamRepos, 200);

			new Handler(Looper.getMainLooper())
					.postDelayed(
							() -> {
								if (!reposArr.isEmpty()) {
									for (int i = 0; i < reposArr.size(); i++) {
										if (!reposArr.get(i).getName().equals(repoInfo.getName())) {
											addRepoButtonAdd.setVisibility(View.VISIBLE);
										} else {
											addRepoButtonAdd.setVisibility(View.GONE);
										}
									}
								} else {
									addRepoButtonAdd.setVisibility(View.VISIBLE);
								}
							},
							500);

			addRepoButtonAdd.setOnClickListener(
					v ->
							AlertDialogs.addRepoDialog(
									context,
									orgName,
									repoInfo.getName(),
									Integer.parseInt(String.valueOf(teamId)),
									teamName));

			addRepoButtonRemove.setOnClickListener(
					v ->
							AlertDialogs.removeRepoDialog(
									context,
									orgName,
									repoInfo.getName(),
									Integer.parseInt(String.valueOf(teamId)),
									teamName));
		}
	}

	@NonNull @Override
	public OrganizationTeamRepositoriesAdapter.TeamReposViewHolder onCreateViewHolder(
			@NonNull ViewGroup parent, int viewType) {
		View v =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_collaborators_search, parent, false);
		return new OrganizationTeamRepositoriesAdapter.TeamReposViewHolder(v);
	}

	@Override
	public void onBindViewHolder(
			@NonNull final OrganizationTeamRepositoriesAdapter.TeamReposViewHolder holder,
			int position) {

		Repository currentItem = reposList.get(position);
		holder.repoInfo = currentItem;

		holder.name.setText(currentItem.getName());

		TextDrawable drawable =
				TextDrawable.builder()
						.beginConfig()
						.useFont(Typeface.DEFAULT)
						.fontSize(28)
						.toUpperCase()
						.width(44)
						.height(44)
						.endConfig()
						.buildRoundRect(
								String.valueOf(currentItem.getFullName().charAt(0)),
								ColorGenerator.Companion.getMATERIAL()
										.getColor(currentItem.getName()),
								12);

		if (currentItem.getAvatarUrl() != null && !currentItem.getAvatarUrl().isEmpty()) {
			Glide.with(context)
					.load(currentItem.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(holder.repoAvatar);
		} else {
			holder.repoAvatar.setImageDrawable(drawable);
		}
	}

	@Override
	public int getItemCount() {
		return reposList.size();
	}

	private void getTeamRepos() {

		if (getItemCount() > 0) {
			Call<List<Repository>> call =
					RetrofitClient.getApiInterface(context).orgListTeamRepos((long) teamId, 1, 100);

			call.enqueue(
					new Callback<>() {
						@Override
						public void onResponse(
								@NonNull Call<List<Repository>> call,
								@NonNull Response<List<Repository>> response) {

							if (response.code() == 200) {

								for (int i = 0;
										i < Objects.requireNonNull(response.body()).size();
										i++) {
									reposArr.addAll(response.body());
								}
							}
						}

						@Override
						public void onFailure(
								@NonNull Call<List<Repository>> call, @NonNull Throwable t) {}
					});
		}
	}
}
