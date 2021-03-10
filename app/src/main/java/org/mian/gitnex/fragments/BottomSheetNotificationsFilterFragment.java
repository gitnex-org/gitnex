package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetNotificationsFilterBinding;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author opyale
 */

public class BottomSheetNotificationsFilterFragment extends BottomSheetDialogFragment {

	private TinyDB tinyDB;
	private OnDismissedListener onDismissedListener;

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		this.tinyDB = TinyDB.getInstance(context);

	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetNotificationsFilterBinding bottomSheetNotificationsFilterBinding = BottomSheetNotificationsFilterBinding.inflate(inflater, container, false);

		bottomSheetNotificationsFilterBinding.readNotifications.setOnClickListener(v1 -> {

			tinyDB.putString("notificationsFilterState", "read");
			dismiss();

		});

		bottomSheetNotificationsFilterBinding.unreadNotifications.setOnClickListener(v12 -> {

			tinyDB.putString("notificationsFilterState", "unread");
			dismiss();

		});

		return bottomSheetNotificationsFilterBinding.getRoot();

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
