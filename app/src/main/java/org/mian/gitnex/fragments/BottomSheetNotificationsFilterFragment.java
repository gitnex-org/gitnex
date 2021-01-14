package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author opyale
 */

public class BottomSheetNotificationsFilterFragment extends BottomSheetDialogFragment {

	private TinyDB tinyDB;
	private OnDismissedListener onDismissedListener;

	@Override
	public void onAttach(@NonNull Context context) {

		this.tinyDB = TinyDB.getInstance(context);
		super.onAttach(context);

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.bottom_sheet_notifications_filter, container, false);

		TextView readNotifications = view.findViewById(R.id.readNotifications);
		TextView unreadNotifications = view.findViewById(R.id.unreadNotifications);

		readNotifications.setOnClickListener(v1 -> {

			tinyDB.putString("notificationsFilterState", "read");
			dismiss();

		});

		unreadNotifications.setOnClickListener(v12 -> {

			tinyDB.putString("notificationsFilterState", "unread");
			dismiss();

		});

		return view;

	}

	@Override
	public void dismiss() {

		if(onDismissedListener != null) {

			onDismissedListener.onDismissed();
		}

		super.dismiss();

	}

	public void setOnDismissedListener(OnDismissedListener onDismissedListener) {

		this.onDismissedListener = onDismissedListener;
	}

	public interface OnDismissedListener {

		void onDismissed();
	}

}
