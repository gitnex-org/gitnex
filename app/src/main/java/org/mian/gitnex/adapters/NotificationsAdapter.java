package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import java.util.List;

/**
 * @author opyale
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private final OnMoreClickedListener onMoreClickedListener;
	private final OnNotificationClickedListener onNotificationClickedListener;
	private List<NotificationThread> notificationThreads;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public NotificationsAdapter(Context context, List<NotificationThread> notificationThreads, OnMoreClickedListener onMoreClickedListener, OnNotificationClickedListener onNotificationClickedListener) {

		this.context = context;
		this.notificationThreads = notificationThreads;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new NotificationsAdapter.NotificationsHolder(inflater.inflate(R.layout.list_notifications, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading) {
			isLoading = true;
		}
		((NotificationsAdapter.NotificationsHolder) holder).bindData(notificationThreads.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return notificationThreads.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
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

	class NotificationsHolder extends RecyclerView.ViewHolder {

		private final LinearLayout frame;
		private final TextView subject;
		private final TextView repository;
		private final ImageView type;
		private final ImageView more;
		private ImageView pinned;

		NotificationsHolder(View itemView) {

			super(itemView);
			frame = itemView.findViewById(R.id.frame);
			subject = itemView.findViewById(R.id.subject);
			repository = itemView.findViewById(R.id.repository);
			type = itemView.findViewById(R.id.type);
			pinned = itemView.findViewById(R.id.pinned);
			more = itemView.findViewById(R.id.more);
		}

		@SuppressLint("SetTextI18n")
		void bindData(NotificationThread notificationThread) {

			String url = notificationThread.getSubject().getUrl();
			String subjectId = "";

			if(StringUtils.containsAny(notificationThread.getSubject().getType().toLowerCase(), "pull", "issue")) {
				subjectId = "<font color='" + ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null) + "'>" + context.getResources().getString(R.string.hash) + url.substring(
					url.lastIndexOf("/") + 1) + "</font>";
			}

			subject.setText(HtmlCompat.fromHtml(subjectId + " " + notificationThread.getSubject().getTitle(), HtmlCompat.FROM_HTML_MODE_LEGACY));
			if(!notificationThread.getSubject().getType().equalsIgnoreCase("repository")) {
				repository.setText(notificationThread.getRepository().getFullName());
			}
			else {
				repository.setVisibility(View.GONE);
				pinned.setVisibility(View.GONE);
				pinned = itemView.findViewById(R.id.pinnedVertical);
			}

			if(notificationThread.isPinned()) {
				pinned.setVisibility(View.VISIBLE);
			}
			else {
				pinned.setVisibility(View.GONE);
			}

			switch(notificationThread.getSubject().getType().toLowerCase()) {

				case "pull":
					type.setImageResource(R.drawable.ic_pull_request);
					break;
				case "issue":
					type.setImageResource(R.drawable.ic_issue);
					break;
				case "commit":
					type.setImageResource(R.drawable.ic_commit);
					break;
				case "repository":
					type.setImageResource(R.drawable.ic_repo);
					break;

				default:
					type.setImageResource(R.drawable.ic_question);
					break;

			}

			switch(notificationThread.getSubject().getState().toLowerCase()) {

				case "closed":
					ImageViewCompat.setImageTintList(type, ColorStateList.valueOf(context.getResources().getColor(R.color.iconIssuePrClosedColor)));
					break;
				case "merged":
					ImageViewCompat.setImageTintList(type, ColorStateList.valueOf(context.getResources().getColor(R.color.iconPrMergedColor)));
					break;

				default:
				case "open":
					ImageViewCompat.setImageTintList(type, ColorStateList.valueOf(AppUtil.getColorFromAttribute(context, R.attr.iconsColor)));
					break;
			}

			frame.setOnClickListener(v -> onNotificationClickedListener.onNotificationClicked(notificationThread));

			more.setOnClickListener(v -> onMoreClickedListener.onMoreClicked(notificationThread));
		}

	}

}
