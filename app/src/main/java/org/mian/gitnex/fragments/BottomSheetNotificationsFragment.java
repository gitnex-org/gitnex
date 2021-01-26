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
import org.mian.gitnex.databinding.BottomSheetNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.NotificationThread;

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

		BottomSheetNotificationsBinding bottomSheetNotificationsBinding = BottomSheetNotificationsBinding.inflate(inflater, container, false);

		TextView markRead = bottomSheetNotificationsBinding.markRead;
		TextView markUnread = bottomSheetNotificationsBinding.markUnread;
		TextView markPinned = bottomSheetNotificationsBinding.markPinned;

		NotificationsActions notificationsActions = new NotificationsActions(context);
		Activity activity = requireActivity();

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

		return bottomSheetNotificationsBinding.getRoot();

	}

	public interface OnOptionSelectedListener {

		void onSelected();
	}

}
