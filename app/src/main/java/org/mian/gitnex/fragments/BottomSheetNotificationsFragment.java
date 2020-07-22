package org.mian.gitnex.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.NotificationsActions;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.NotificationThread;
import java.util.Objects;

/**
 * Author opyale
 */

public class BottomSheetNotificationsFragment extends BottomSheetDialogFragment {

	private Context context;
	private NotificationThread notificationThread;
	private OnOptionSelectedListener onOptionSelectedListener;

	public void onAttach(Context context, NotificationThread notificationThread, OnOptionSelectedListener onOptionSelectedListener) {

		this.context = context;
		this.notificationThread = notificationThread;
		this.onOptionSelectedListener = onOptionSelectedListener;

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.bottom_sheet_notifications, container, false);

		TextView markRead = v.findViewById(R.id.markRead);
		TextView markUnread = v.findViewById(R.id.markUnread);
		TextView markPinned = v.findViewById(R.id.markPinned);

		NotificationsActions notificationsActions = new NotificationsActions(context);
		Activity activity = Objects.requireNonNull(getActivity());

		if(notificationThread.isPinned()) {

			AppUtil.setMultiVisibility(View.GONE, markUnread, markPinned);
		} else if(notificationThread.isUnread()) {

			markUnread.setVisibility(View.GONE);
		} else {

			markRead.setVisibility(View.GONE);
		}

		markPinned.setOnClickListener(v12 -> {

			Thread thread = new Thread(() -> {

				try {

					notificationsActions.setNotificationStatus(notificationThread, NotificationsActions.NotificationStatus.PINNED);
					activity.runOnUiThread(() -> onOptionSelectedListener.onSelected());

				}
				catch(Exception e) {

					activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					Log.e("onError", e.toString());

				} finally {

					dismiss();
				}
			});

			thread.start();

		});

		markRead.setOnClickListener(v1 -> {

			Thread thread = new Thread(() -> {

				try {

					notificationsActions.setNotificationStatus(notificationThread, NotificationsActions.NotificationStatus.READ);
					activity.runOnUiThread(() -> onOptionSelectedListener.onSelected());

				}
				catch(Exception e) {

					activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					Log.e("onError", e.toString());

				} finally {

					dismiss();
				}
			});

			thread.start();

		});

		markUnread.setOnClickListener(v13 -> {

			Thread thread = new Thread(() -> {

				try {

					notificationsActions.setNotificationStatus(notificationThread, NotificationsActions.NotificationStatus.UNREAD);
					activity.runOnUiThread(() -> onOptionSelectedListener.onSelected());

				}
				catch(Exception e) {

					activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					Log.e("onError", e.toString());

				} finally {

					dismiss();
				}
			});

			thread.start();

		});

		return v;

	}

	public interface OnOptionSelectedListener {

		void onSelected();
	}

}
