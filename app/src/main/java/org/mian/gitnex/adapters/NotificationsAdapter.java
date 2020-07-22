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

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads, OnMoreClickedListener onMoreClickedListener, OnNotificationClickedListener onNotificationClickedListener) {

		this.context = context;
		this.notificationThreads = notificationThreads;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;

	}

	static class NotificationsViewHolder extends RecyclerView.ViewHolder {

		private LinearLayout frame;
		private TextView subject;
		private TextView repository;
		private ImageView type;
		private ImageView pinned;
		private ImageView more;

		public NotificationsViewHolder(@NonNull View itemView) {

			super(itemView);

			frame = itemView.findViewById(R.id.frame);
			subject = itemView.findViewById(R.id.subject);
			repository = itemView.findViewById(R.id.repository);
			type = itemView.findViewById(R.id.type);
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
				holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_pull_request, null));
				break;

			case "Issue":
				holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_issue, null));
				break;

			default:
				holder.type.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_question, null));
				break;

		}

		holder.frame.setOnClickListener(v -> onNotificationClickedListener.onNotificationClicked(notificationThread));
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
