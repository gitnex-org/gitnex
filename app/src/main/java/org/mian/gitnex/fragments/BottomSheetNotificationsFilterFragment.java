package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetNotificationsFilterBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author opyale
 */
public class BottomSheetNotificationsFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetListener listener;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		BottomSheetNotificationsFilterBinding binding =
				BottomSheetNotificationsFilterBinding.inflate(inflater, container, false);

		binding.readNotifications.setOnClickListener(
				v1 -> {
					listener.onButtonClicked("read");
					dismiss();
				});

		binding.unreadNotifications.setOnClickListener(
				v12 -> {
					listener.onButtonClicked("unread");
					dismiss();
				});

		return binding.getRoot();
	}

	public void setOnClickListener(BottomSheetListener listener) {
		this.listener = listener;
	}
}
