package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.TinyDB;
import java.util.List;

/**
 * Author opyale
 * Modified M M Arif
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final int TYPE_LOAD = 0;
	private List<NotificationThread> notificationThreads;
	private final OnMoreClickedListener onMoreClickedListener;
	private final OnNotificationClickedListener onNotificationClickedListener;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private final TinyDB tinyDb;

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads, OnMoreClickedListener onMoreClickedListener, OnNotificationClickedListener onNotificationClickedListener) {
		this.tinyDb = TinyDB.getInstance(context);
		this.context = context;
		this.notificationThreads = notificationThreads;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		if(viewType == TYPE_LOAD) {
			return new NotificationsAdapter.NotificationsHolder(inflater.inflate(R.layout.list_notifications, parent, false));
		}
		else {
			return new NotificationsAdapter.LoadHolder(inflater.inflate(R.layout.row_load, parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		if(getItemViewType(position) == TYPE_LOAD) {
			((NotificationsAdapter.NotificationsHolder) holder).bindData(notificationThreads.get(position));
		}
	}

	@Override
	public int getItemViewType(int position) {
		if(notificationThreads.get(position).getSubject() != null) {
			return TYPE_LOAD;
		}
		else {
			return 1;
		}
	}

	@Override
	public int getItemCount() {
		return notificationThreads.size();
	}

	class NotificationsHolder extends RecyclerView.ViewHolder {

		private final LinearLayout frame;
		private final TextView subject;
		private final TextView repository;
		private final ImageView typePr;
		private final ImageView typeIssue;
		private final ImageView typeUnknown;
		private final ImageView pinned;
		private final ImageView more;

		NotificationsHolder(View itemView) {

			super(itemView);
			frame = itemView.findViewById(R.id.frame);
			subject = itemView.findViewById(R.id.subject);
			repository = itemView.findViewById(R.id.repository);
			typePr = itemView.findViewById(R.id.typePr);
			typeIssue = itemView.findViewById(R.id.typeIssue);
			typeUnknown = itemView.findViewById(R.id.typeUnknown);
			pinned = itemView.findViewById(R.id.pinned);
			more = itemView.findViewById(R.id.more);
		}

		@SuppressLint("SetTextI18n")
		void bindData(NotificationThread notificationThread) {

			String url = notificationThread.getSubject().getUrl();
			String subjectId = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + url.substring(url.lastIndexOf("/") + 1) + "</font>";

			subject.setText(HtmlCompat.fromHtml(subjectId + " " + notificationThread.getSubject().getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
			repository.setText(notificationThread.getRepository().getFullName());

			if(notificationThread.isPinned()) {
				pinned.setVisibility(View.VISIBLE);
			}
			else {
				pinned.setVisibility(View.GONE);
			}

			switch(notificationThread.getSubject().getType()) {
				case "Pull":
					typePr.setVisibility(View.VISIBLE);
					typeIssue.setVisibility(View.GONE);
					typeUnknown.setVisibility(View.GONE);
					break;
				case "Issue":
					typePr.setVisibility(View.GONE);
					typeIssue.setVisibility(View.VISIBLE);
					typeUnknown.setVisibility(View.GONE);
					break;
				default:
					typePr.setVisibility(View.GONE);
					typeIssue.setVisibility(View.GONE);
					typeUnknown.setVisibility(View.VISIBLE);
					break;
			}

			frame.setOnClickListener(v -> {

				onNotificationClickedListener.onNotificationClicked(notificationThread);

				String[] parts = notificationThread.getRepository().getFullName().split("/");
				final String repoOwner = parts[0];
				final String repoName = parts[1];

				int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
				RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

				Integer count = repositoryData.checkRepository(currentActiveAccountId, repoOwner, repoName);

				if(count == 0) {
					long id = repositoryData.insertRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", id);
				}
				else {
					Repository data = repositoryData.getRepository(currentActiveAccountId, repoOwner, repoName);
					tinyDb.putLong("repositoryId", data.getRepositoryId());
				}
			});

			more.setOnClickListener(v -> onMoreClickedListener.onMoreClicked(notificationThread));
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

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<NotificationThread> list) {
		notificationThreads = list;
		notifyDataChanged();
	}

	public interface OnNotificationClickedListener {
		void onNotificationClicked(NotificationThread notificationThread);
	}

	public interface OnMoreClickedListener {
		void onMoreClicked(NotificationThread notificationThread);
	}
}
