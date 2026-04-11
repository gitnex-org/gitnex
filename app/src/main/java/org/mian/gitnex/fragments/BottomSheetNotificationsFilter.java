package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetNotificationsFilterBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BottomSheetNotificationsFilter extends BottomSheetDialogFragment {

	private BottomsheetNotificationsFilterBinding binding;
	private String currentMode;
	private boolean canMarkRead;
	private OnFilterChangedListener listener;

	public interface OnFilterChangedListener {
		void onFilterChanged(String mode);

		void onMarkAllReadTriggered();
	}

	public static BottomSheetNotificationsFilter newInstance(
			String currentMode, boolean canMarkRead) {
		BottomSheetNotificationsFilter fragment = new BottomSheetNotificationsFilter();
		fragment.currentMode = currentMode;
		fragment.canMarkRead = canMarkRead;
		return fragment;
	}

	public void setListener(OnFilterChangedListener listener) {
		this.listener = listener;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetNotificationsFilterBinding.inflate(inflater, container, false);
		setupUI();
		return binding.getRoot();
	}

	private void setupUI() {
		boolean isUnread = "unread".equals(currentMode);

		if (isUnread) {
			binding.toggleIcon.setImageResource(R.drawable.ic_watchers);
			binding.toggleText.setText(R.string.isRead);
			binding.markAllReadCard.setVisibility(canMarkRead ? View.VISIBLE : View.GONE);
		} else {
			binding.toggleIcon.setImageResource(R.drawable.ic_notifications);
			binding.toggleText.setText(R.string.isUnread);
			binding.markAllReadCard.setVisibility(View.GONE);
		}

		binding.toggleActionLayout.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onFilterChanged(isUnread ? "read" : "unread");
					}
					dismiss();
				});

		binding.markAllReadLayout.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onMarkAllReadTriggered();
					}
					dismiss();
				});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applySheetStyle(dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
