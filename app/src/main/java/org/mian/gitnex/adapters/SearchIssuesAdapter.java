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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.Issues;
import org.ocpsoft.prettytime.PrettyTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Author M M Arif
 */

public class SearchIssuesAdapter extends RecyclerView.Adapter<SearchIssuesAdapter.SearchViewHolder> {

	private List<Issues> searchedList;
	private Context mCtx;
	private TinyDB tinyDb;

	public SearchIssuesAdapter(List<Issues> dataList, Context mCtx) {

		this.mCtx = mCtx;
		this.searchedList = dataList;
		this.tinyDb = TinyDB.getInstance(mCtx);
	}

	class SearchViewHolder extends RecyclerView.ViewHolder {

		private TextView issueNumber;
		private ImageView issueAssigneeAvatar;
		private TextView issueTitle;
		private TextView issueCreatedTime;
		private TextView issueCommentsCount;
		private TextView repoFullName;

		private SearchViewHolder(View itemView) {

			super(itemView);

			issueNumber = itemView.findViewById(R.id.issueNumber);
			issueAssigneeAvatar = itemView.findViewById(R.id.assigneeAvatar);
			issueTitle = itemView.findViewById(R.id.issueTitle);
			issueCommentsCount = itemView.findViewById(R.id.issueCommentsCount);
			issueCreatedTime = itemView.findViewById(R.id.issueCreatedTime);
			repoFullName = itemView.findViewById(R.id.repoFullName);

			issueTitle.setOnClickListener(v -> {

				Context context = v.getContext();

				Intent intent = new Intent(context, IssueDetailActivity.class);
				intent.putExtra("issueNumber", issueNumber.getText());

				tinyDb.putString("issueNumber", issueNumber.getText().toString());
				tinyDb.putString("issueType", "Issue");

				tinyDb.putString("repoFullName", repoFullName.getText().toString());

				String[] parts = repoFullName.getText().toString().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = new RepositoriesApi(context);

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

		String locale = tinyDb.getString("locale");
		String timeFormat = tinyDb.getString("dateFormat");

		if(!currentItem.getUser().getFull_name().equals("")) {
			holder.issueAssigneeAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCreator) + currentItem.getUser().getFull_name(), mCtx));
		}
		else {
			holder.issueAssigneeAvatar.setOnClickListener(new ClickListener(mCtx.getResources().getString(R.string.issueCreator) + currentItem.getUser().getLogin(), mCtx));
		}

		PicassoService
			.getInstance(mCtx).get().load(currentItem.getUser().getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.issueAssigneeAvatar);

		String issueNumber_ = "<font color='" + ResourcesCompat.getColor(mCtx.getResources(), R.color.lightGray, null) + "'>" + currentItem.getRepository().getFull_name() + mCtx.getResources().getString(R.string.hash) + currentItem.getNumber() + "</font>";
		holder.issueTitle.setText(HtmlCompat.fromHtml(issueNumber_ + " " + currentItem.getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));

		holder.issueNumber.setText(String.valueOf(currentItem.getNumber()));
		holder.issueCommentsCount.setText(String.valueOf(currentItem.getComments()));
		holder.repoFullName.setText(currentItem.getRepository().getFull_name());

		switch(timeFormat) {
			case "pretty": {
				PrettyTime prettyTime = new PrettyTime(new Locale(locale));
				String createdTime = prettyTime.format(currentItem.getCreated_at());
				holder.issueCreatedTime.setText(createdTime);
				holder.issueCreatedTime.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(currentItem.getCreated_at()), mCtx));
				break;
			}
			case "normal": {
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
				String createdTime = formatter.format(currentItem.getCreated_at());
				holder.issueCreatedTime.setText(createdTime);
				break;
			}
			case "normal1": {
				DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy '" + mCtx.getResources().getString(R.string.timeAtText) + "' HH:mm", new Locale(locale));
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
