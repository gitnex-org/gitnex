package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.databinding.BottomsheetPrMenuBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BottomSheetPrMenu extends BottomSheetDialogFragment {

	private BottomsheetPrMenuBinding binding;
	private PullRequest pullRequest;
	private PrMenuListener listener;

	public interface PrMenuListener {
		void onFiles();

		void onPrActions();

		void onDependencies();

		void onTrackedTime();

		void onCopyUrl();

		void onShare();

		void onOpenInBrowser();
	}

	public static BottomSheetPrMenu newInstance(PullRequest pullRequest) {
		BottomSheetPrMenu sheet = new BottomSheetPrMenu();
		Bundle args = new Bundle();
		args.putSerializable("pull_request", pullRequest);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetPrMenuBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			pullRequest = (PullRequest) args.getSerializable("pull_request");
		}

		if (pullRequest != null && pullRequest.getTitle() != null) {
			binding.prInfo.setText(pullRequest.getTitle() + " #" + pullRequest.getNumber());
		}

		binding.files.setOnClickListener(
				v -> {
					if (listener != null) listener.onFiles();
					dismiss();
				});

		binding.prActions.setOnClickListener(
				v -> {
					if (listener != null) listener.onPrActions();
					dismiss();
				});

		binding.dependencies.setOnClickListener(
				v -> {
					if (listener != null) listener.onDependencies();
					dismiss();
				});

		binding.trackedTime.setOnClickListener(
				v -> {
					if (listener != null) listener.onTrackedTime();
					dismiss();
				});

		binding.copyUrl.setOnClickListener(
				v -> {
					if (listener != null) listener.onCopyUrl();
					dismiss();
				});

		binding.share.setOnClickListener(
				v -> {
					if (listener != null) listener.onShare();
					dismiss();
				});

		binding.openBrowser.setOnClickListener(
				v -> {
					if (listener != null) listener.onOpenInBrowser();
					dismiss();
				});
	}

	public void setPrMenuListener(PrMenuListener listener) {
		this.listener = listener;
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applySheetStyle(dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
