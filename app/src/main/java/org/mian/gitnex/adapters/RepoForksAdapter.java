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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.clients.PicassoService;
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

public class RepoForksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<org.gitnex.tea4j.v2.models.Repository> forksList;
	private Runnable loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;

	public RepoForksAdapter(Context ctx, List<org.gitnex.tea4j.v2.models.Repository> forksListMain) {

		this.context = ctx;
		this.forksList = forksListMain;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new RepoForksAdapter.ForksHolder(inflater.inflate(R.layout.list_repositories, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.run();
		}
		((RepoForksAdapter.ForksHolder) holder).bindData(forksList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return forksList.size();
	}

	class ForksHolder extends RecyclerView.ViewHolder {

		private org.gitnex.tea4j.v2.models.Repository userRepositories;

		private final ImageView image;
		private final TextView repoName;
		private final TextView orgName;
		private final TextView repoDescription;
		private CheckBox isRepoAdmin;
		private final TextView repoStars;
		private final TextView repoLastUpdated;

		ForksHolder(View itemView) {

			super(itemView);
			repoName = itemView.findViewById(R.id.repoName);
			orgName = itemView.findViewById(R.id.orgName);
			repoDescription = itemView.findViewById(R.id.repoDescription);
			isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);
			image = itemView.findViewById(R.id.imageAvatar);
			repoStars = itemView.findViewById(R.id.repoStars);
			repoLastUpdated = itemView.findViewById(R.id.repoLastUpdated);
		}

		@SuppressLint("SetTextI18n")
		void bindData(org.gitnex.tea4j.v2.models.Repository forksModel) {

			TinyDB tinyDb = TinyDB.getInstance(context);
			int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

			Locale locale = context.getResources().getConfiguration().locale;
			String timeFormat = tinyDb.getString("dateFormat", "pretty");
			this.userRepositories = forksModel;
			orgName.setText(forksModel.getFullName().split("/")[0]);
			repoName.setText(forksModel.getFullName().split("/")[1]);
			repoStars.setText(AppUtil.numberFormatter(forksModel.getStarsCount()));

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(forksModel.getName());
			String firstCharacter = String.valueOf(forksModel.getFullName().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28)
				.endConfig().buildRoundRect(firstCharacter, color, 3);

			if(forksModel.getAvatarUrl() != null) {
				if(!forksModel.getAvatarUrl().equals("")) {
					PicassoService.getInstance(context).get().load(forksModel.getAvatarUrl()).placeholder(R.drawable.loader_animated)
						.transform(new RoundedTransformation(imgRadius, 0)).resize(120, 120).centerCrop().into(image);
				}
				else {
					image.setImageDrawable(drawable);
				}
			}
			else {
				image.setImageDrawable(drawable);
			}

			if(forksModel.getUpdatedAt() != null) {

				switch(timeFormat) {
					case "pretty": {
						PrettyTime prettyTime = new PrettyTime(locale);
						String createdTime = prettyTime.format(forksModel.getUpdatedAt());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						repoLastUpdated.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(forksModel.getUpdatedAt()), context));
						break;
					}
					case "normal": {
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(forksModel.getUpdatedAt());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
					case "normal1": {
						DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", locale);
						String createdTime = formatter.format(forksModel.getUpdatedAt());
						repoLastUpdated.setText(context.getString(R.string.lastUpdatedAt, createdTime));
						break;
					}
				}
			}
			else {
				repoLastUpdated.setVisibility(View.GONE);
			}

			if(!forksModel.getDescription().equals("")) {
				repoDescription.setText(forksModel.getDescription());
			}
			else {
				repoDescription.setText(context.getString(R.string.noDataDescription));
			}

			if(isRepoAdmin == null) {
				isRepoAdmin = new CheckBox(context);
			}
			isRepoAdmin.setChecked(forksModel.getPermissions().isAdmin());

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();

				RepositoryContext repo = new RepositoryContext(userRepositories, context);
				repo.saveToDB(context);
				Intent intent = repo.getIntent(context, RepoDetailActivity.class);

				context.startActivity(intent);

			});
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

	public void updateList(List<org.gitnex.tea4j.v2.models.Repository> list) {
		forksList = list;
		notifyDataChanged();
	}
}
