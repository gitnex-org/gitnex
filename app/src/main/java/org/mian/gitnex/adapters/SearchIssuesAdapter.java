package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class SearchIssuesAdapter extends RecyclerView.Adapter<SearchIssuesAdapter.SearchViewHolder> {

	private final List<Issues> searchedList;
	private final Context context;
	private final TinyDB tinyDb;

	public SearchIssuesAdapter(List<Issues> dataList, Context ctx) {

		this.context = ctx;
		this.searchedList = dataList;
		this.tinyDb = TinyDB.getInstance(context);
	}

	class SearchViewHolder extends RecyclerView.ViewHolder {

		private Issues issue;

		private final ImageView issueAssigneeAvatar;
		private final TextView issueTitle;
		private final TextView issueCreatedTime;
		private final TextView issueCommentsCount;

		private SearchViewHolder(View itemView) {

			super(itemView);

			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);

			itemView.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", issue.getNumber());

				tinyDb.putString("issueNumber", String.valueOf(issue.getNumber()));
				tinyDb.putString("issueType", "Issue");

				tinyDb.putString("repoFullName", issue.getRepository().getFull_name());

				String[] parts = issue.getRepository().getFull_name().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				assert repositoryData != null;
				Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

				if(count == 0) {

					long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", id);

				}
				else {

					Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", data.getRepositoryId());

				}

				context.startActivity(intent);
			});

			issueAssigneeAvatar.setOnClickListener(v -> {
				Context context = v.getContext();
				String userLoginId = issue.getUser().getLogin();

				AppUtil.copyToClipboard(context, userLoginId, context.getString(R.string.copyLoginIdToClipBoard, userLoginId));
			});
		}
	}

	@NonNull
	@Override
	public SearchIssuesAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_issues, parent, false);
		return new SearchIssuesAdapter.SearchViewHolder(v);
	}

	@Override
	public void onBindViewHolder(@NonNull final SearchIssuesAdapter.SearchViewHolder holder, int position) {

		Issues currentItem = searchedList.get(position);
		int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		String locale = tinyDb.getString("locale");
		String timeFormat = tinyDb.getString("dateFormat");

		PicassoService.getInstance(context).get()
			.load(currentItem.getUser().getAvatar_url())
			.placeholder(R.drawable.loader_animated)
			.transform(new RoundedTransformation(imgRadius, 0))
			.resize(120, 120)
			.centerCrop()
			.into(holder.issueAssigneeAvatar);

		String issueNumber_ = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + currentItem.getRepository().getFull_name() + context.getResources().getString(R.string.hash) + currentItem.getNumber() + "</font>";

		holder.issue = currentItem;
		holder.issueTitle.setText(HtmlCompat.fromHtml(issueNumber_ + " " + currentItem.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
		holder.issueCommentsCount.setText(String.valueOf(currentItem.getComments()));

		switch(timeFormat) {
			case "pretty": {
				PrettyTime prettyTime = new PrettyTime(new Locale(locale));
				String createdTime = prettyTime.format(currentItem.getCreated_at());
				holder.issueCreatedTime.setText(createdTime);
				holder.issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getCreated_at()), context));
				break;
			}
			case "normal": {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
				String createdTime = formatter.format(currentItem.getCreated_at());
				holder.issueCreatedTime.setText(createdTime);
				break;
			}
			case "normal1": {
				DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + context.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
				String createdTime = formatter.format(currentItem.getCreated_at());
				holder.issueCreatedTime.setText(createdTime);
				break;
			}
		}
	}

	@Override
	public int getItemCount() {

		return searchedList.size();
	}

	public void notifyDataChanged() {

		notifyDataSetChanged();
	}
}
