package org.mian.gitnex.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.NotificationThread;
import java.util.List;

/**
 * Author opyale
 */

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationsViewHolder> {

	private Context context;
	private List<NotificationThread> notificationThreads;
	private OnMoreClickedListener onMoreClickedListener;
	private OnNotificationClickedListener onNotificationClickedListener;
	private TinyDB tinyDb;

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads, OnMoreClickedListener onMoreClickedListener, OnNotificationClickedListener onNotificationClickedListener) {

		this.tinyDb = new TinyDB(context);
		this.context = context;
		this.notificationThreads = notificationThreads;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;

	}

	static class NotificationsViewHolder extends RecyclerView.ViewHolder {

		private LinearLayout frame;
		private TextView subject;
		private TextView repository;
		private ImageView typePr;
		private ImageView typeIssue;
		private ImageView typeUnknown;
		private ImageView pinned;
		private ImageView more;

		public NotificationsViewHolder(@NonNull View itemView) {

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
	}

	@NonNull
	@Override
	public NotificationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

		View v = LayoutInflater.from(context).inflate(R.layout.list_notifications, parent, false);
		return new NotificationsAdapter.NotificationsViewHolder(v);

	}

	@Override
	public void onBindViewHolder(@NonNull NotificationsViewHolder holder, int position) {

		NotificationThread notificationThread = notificationThreads.get(position);

		String url = notificationThread.getSubject().getUrl();
		String subjectId = "<font color='" + context.getResources().getColor(R.color.lightGray) + "'>" + context.getResources()
			.getString(R.string.hash) + url.substring(url.lastIndexOf("/") + 1) + "</font>";

		holder.subject.setText(Html.fromHtml(subjectId + " " + notificationThread.getSubject().getTitle()));
		holder.repository.setText(notificationThread.getRepository().getFullname());

		if(notificationThread.isPinned()) {
			holder.pinned.setVisibility(View.VISIBLE);
		}
		else {
			holder.pinned.setVisibility(View.GONE);
		}

		switch(notificationThread.getSubject().getType()) {

			case "Pull":
				holder.typePr.setVisibility(View.VISIBLE);
				holder.typeIssue.setVisibility(View.GONE);
				holder.typeUnknown.setVisibility(View.GONE);
				break;

			case "Issue":
				holder.typePr.setVisibility(View.GONE);
				holder.typeIssue.setVisibility(View.VISIBLE);
				holder.typeUnknown.setVisibility(View.GONE);
				break;

			default:
				holder.typePr.setVisibility(View.GONE);
				holder.typeIssue.setVisibility(View.GONE);
				holder.typeUnknown.setVisibility(View.VISIBLE);
				break;

		}

		holder.frame.setOnClickListener(v -> {

			onNotificationClickedListener.onNotificationClicked(notificationThread);

			String[] parts = notificationThread.getRepository().getFullname().split("/");
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
		});

		holder.more.setOnClickListener(v -> onMoreClickedListener.onMoreClicked(notificationThread));

	}

	@Override
	public int getItemCount() {

		return notificationThreads.size();
	}

	public interface OnNotificationClickedListener {

		void onNotificationClicked(NotificationThread notificationThread);
	}

	public interface OnMoreClickedListener {

		void onMoreClicked(NotificationThread notificationThread);
	}

}
