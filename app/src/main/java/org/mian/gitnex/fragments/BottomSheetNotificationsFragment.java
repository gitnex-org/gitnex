package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.gitnex.tea4j.v2.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomSheetNotificationsBinding;
import org.mian.gitnex.viewmodels.NotificationsViewModel;

/**
 * @author opyale
 * @author mmarif
 */
public class BottomSheetNotificationsFragment extends BottomSheetDialogFragment {

	private NotificationThread thread;
	private NotificationsViewModel viewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL, R.style.Custom_BottomSheet);
	}

	public void onAttach(NotificationThread thread) {
		this.thread = thread;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		BottomSheetNotificationsBinding binding =
				BottomSheetNotificationsBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(requireActivity()).get(NotificationsViewModel.class);

		setupUI(binding);

		binding.markRead.setOnClickListener(v -> updateStatus("read"));
		binding.markUnread.setOnClickListener(v -> updateStatus("unread"));
		binding.markPinned.setOnClickListener(v -> updateStatus("pinned"));

		return binding.getRoot();
	}

	private void updateStatus(String status) {
		viewModel.updateNotificationStatus(requireContext(), thread.getId(), status);
		dismiss();
	}

	private void setupUI(BottomSheetNotificationsBinding binding) {
		if (thread == null) return;

		if (thread.getSubject() != null && thread.getSubject().getTitle() != null) {
			binding.sheetTitle.setText(thread.getSubject().getTitle());
		}
		if (thread.getRepository() != null) {
			binding.sheetSubtitle.setText(thread.getRepository().getFullName());
			binding.sheetSubtitle.setVisibility(View.VISIBLE);
		}

		if (thread.isPinned()) {
			binding.markPinned.setVisibility(View.GONE);
		}

		if (thread.isUnread()) {
			binding.markUnread.setVisibility(View.GONE);
		} else {
			binding.markRead.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			View bottomSheet =
					((BottomSheetDialog) dialog)
							.findViewById(com.google.android.material.R.id.design_bottom_sheet);
			if (bottomSheet != null) {
				BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
				behavior.setFitToContents(true);
				behavior.setSkipCollapsed(true);
				behavior.setExpandedOffset(0);
				behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			}
		}
	}
}
