package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetNotificationItemMenuBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.viewmodels.NotificationsViewModel;

/**
 * @author opyale
 * @author mmarif
 */
public class BottomSheetNotificationsFragment extends BottomSheetDialogFragment {

	private NotificationThread thread;
	private NotificationsViewModel viewModel;
	private BottomsheetNotificationItemMenuBinding binding;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void onAttach(NotificationThread thread) {
		this.thread = thread;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetNotificationItemMenuBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

		setupUI();

		binding.markRead.setOnClickListener(
				v -> {
					String status = thread.isUnread() ? "read" : "unread";
					updateStatus(status);
				});

		binding.markPinned.setOnClickListener(
				v -> {
					String status = thread.isPinned() ? "unpinned" : "pinned";
					updateStatus(status);
				});

		return binding.getRoot();
	}

	private void updateStatus(String status) {
		viewModel.updateNotificationStatus(requireContext(), thread.getId(), status);
		dismiss();
	}

	private void setupUI() {
		if (thread == null) return;

		if (thread.getSubject() != null) {
			binding.sheetTitle.setText(thread.getSubject().getTitle());
		}
		if (thread.getRepository() != null) {
			binding.sheetSubtitle.setText(thread.getRepository().getFullName());
		}

		if (thread.isUnread()) {
			binding.readText.setText(R.string.markAsRead);
			binding.readIcon.setImageResource(R.drawable.ic_unwatch);
		} else {
			binding.readText.setText(R.string.markAsUnread);
			binding.readIcon.setImageResource(R.drawable.ic_watchers);
		}

		if (thread.isPinned()) {
			binding.pinText.setText(R.string.unpin);
			binding.pinIcon.setImageResource(R.drawable.ic_unpin);
		} else {
			binding.pinText.setText(R.string.pinNotification);
			binding.pinIcon.setImageResource(R.drawable.ic_pin);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}
}
