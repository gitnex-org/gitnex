package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author opyale
 * @author mmarif
 */
public class NotificationsAdapter
		extends RecyclerView.Adapter<NotificationsAdapter.NotificationsHolder> {

	private final Context context;
	private final OnMoreClickedListener onMoreClickedListener;
	private final OnNotificationClickedListener onNotificationClickedListener;
	private List<NotificationThread> notificationThreads = new ArrayList<>();

	public NotificationsAdapter(
			Context context,
			OnMoreClickedListener onMoreClickedListener,
			OnNotificationClickedListener onNotificationClickedListener) {
		this.context = context;
		this.onMoreClickedListener = onMoreClickedListener;
		this.onNotificationClickedListener = onNotificationClickedListener;
	}

	@NonNull @Override
	public NotificationsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListNotificationsBinding binding =
				ListNotificationsBinding.inflate(LayoutInflater.from(context), parent, false);
		return new NotificationsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull NotificationsHolder holder, int position) {
		holder.bind(notificationThreads.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return notificationThreads != null ? notificationThreads.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<NotificationThread> list) {
		this.notificationThreads = list;
		notifyDataSetChanged();
	}

	public interface OnNotificationClickedListener {
		void onNotificationClicked(NotificationThread notificationThread);
	}

	public interface OnMoreClickedListener {
		void onMoreClicked(NotificationThread notificationThread);
	}

	public class NotificationsHolder extends RecyclerView.ViewHolder {
		private final ListNotificationsBinding binding;

		NotificationsHolder(ListNotificationsBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(NotificationThread thread) {
			setupSubject(thread);
			setupRepository(thread);
			setupIcon(thread);

			binding.pinned.setVisibility(thread.isPinned() ? View.VISIBLE : View.GONE);

			binding.container.setOnClickListener(
					v -> onNotificationClickedListener.onNotificationClicked(thread));

			binding.more.setOnClickListener(v -> onMoreClickedListener.onMoreClicked(thread));
		}

		private void setupSubject(NotificationThread thread) {
			String url = thread.getSubject().getUrl();
			String subjectText = thread.getSubject().getTitle();
			String type = thread.getSubject().getType().toLowerCase();

			if (Arrays.asList("pull", "issue").contains(type)) {
				String id = url.substring(url.lastIndexOf("/") + 1);
				String idPrefix = context.getString(R.string.hash) + id + " ";

				SpannableStringBuilder builder = new SpannableStringBuilder(idPrefix + subjectText);

				int currentTextColor = binding.subject.getCurrentTextColor();
				int alphaColor = ColorUtils.setAlphaComponent(currentTextColor, 179);

				builder.setSpan(
						new ForegroundColorSpan(alphaColor),
						0,
						idPrefix.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

				binding.subject.setText(builder);
			} else {
				binding.subject.setText(subjectText);
			}
		}

		private void setupRepository(NotificationThread thread) {
			if (thread.getSubject().getType().equalsIgnoreCase("repository")) {
				binding.repository.setVisibility(View.GONE);
			} else {
				binding.repository.setVisibility(View.VISIBLE);
				binding.repository.setText(thread.getRepository().getFullName());
			}
		}

		private void setupIcon(NotificationThread thread) {
			int iconRes =
					switch (thread.getSubject().getType().toLowerCase()) {
						case "pull" -> R.drawable.ic_pull_request;
						case "issue" -> R.drawable.ic_issue;
						case "commit" -> R.drawable.ic_commit;
						case "repository" -> R.drawable.ic_repo;
						default -> R.drawable.ic_question;
					};
			binding.type.setImageResource(iconRes);

			int tintColor =
					switch (thread.getSubject().getState().toLowerCase()) {
						case "closed" -> context.getColor(R.color.iconIssuePrClosedColor);
						case "merged" -> context.getColor(R.color.iconPrMergedColor);
						default -> AppUtil.getColorFromAttribute(context, R.attr.iconsColor);
					};
			binding.type.setImageTintList(ColorStateList.valueOf(tintColor));
		}
	}
}
