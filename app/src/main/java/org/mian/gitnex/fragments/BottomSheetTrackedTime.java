package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.TrackedTimeAdapter;
import org.mian.gitnex.databinding.BottomsheetTrackedTimeBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.TrackedTimeViewModel;

/**
 * @author mmarif
 */
public class BottomSheetTrackedTime extends BottomSheetDialogFragment {

	private BottomsheetTrackedTimeBinding binding;
	private TrackedTimeViewModel viewModel;
	private TrackedTimeAdapter adapter;
	private String owner;
	private String repo;
	private long issueIndex;

	public static BottomSheetTrackedTime newInstance(String owner, String repo, long issueIndex) {
		BottomSheetTrackedTime sheet = new BottomSheetTrackedTime();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		args.putLong("issue_index", issueIndex);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetTrackedTimeBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			owner = args.getString("owner");
			repo = args.getString("repo");
			issueIndex = args.getLong("issue_index");
		}

		viewModel = new ViewModelProvider(requireActivity()).get(TrackedTimeViewModel.class);
		viewModel.resetPagination();

		binding.btnClose.setOnClickListener(v -> dismiss());

		adapter = new TrackedTimeAdapter(new ArrayList<>());
		binding.trackedTimeRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.trackedTimeRecyclerView.setAdapter(adapter);

		adapter.setOnDeleteClickListener(
				(time, position) -> {
					viewModel.deleteTrackedTime(requireContext(), owner, repo, issueIndex, time);
				});

		binding.trackedTimeRecyclerView.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
						super.onScrolled(recyclerView, dx, dy);
						LinearLayoutManager layoutManager =
								(LinearLayoutManager) recyclerView.getLayoutManager();
						if (layoutManager != null) {
							int totalItems = layoutManager.getItemCount();
							int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
							if (totalItems > 0 && lastVisibleItem >= totalItems - 5) {
								viewModel.loadTrackedTimes(
										requireContext(), owner, repo, issueIndex);
							}
						}
					}
				});

		binding.addTimeButton.setOnClickListener(
				v -> {
					String hoursStr =
							binding.hoursInput.getText() != null
									? binding.hoursInput.getText().toString().trim()
									: "";
					String minutesStr =
							binding.minutesInput.getText() != null
									? binding.minutesInput.getText().toString().trim()
									: "";

					int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
					int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);

					if (hours == 0 && minutes == 0) {
						Toasty.show(requireContext(), getString(R.string.enter_time));
						return;
					}

					viewModel.addTrackedTime(
							requireContext(), owner, repo, issueIndex, hours, minutes);
					binding.hoursInput.setText("");
					binding.minutesInput.setText("");
				});

		observeViewModel();
		viewModel.loadTrackedTimes(requireContext(), owner, repo, issueIndex);
	}

	private void observeViewModel() {
		viewModel
				.getTrackedTimes()
				.observe(
						getViewLifecycleOwner(),
						times -> {
							if (times != null) {
								adapter.updateList(times);
								binding.layoutEmpty
										.getRoot()
										.setVisibility(times.isEmpty() ? View.VISIBLE : View.GONE);
								binding.trackedTimeRecyclerView.setVisibility(
										times.isEmpty() ? View.GONE : View.VISIBLE);
							}
						});

		viewModel
				.getTotalSeconds()
				.observe(
						getViewLifecycleOwner(),
						total -> {
							long hours = total / 3600;
							long minutes = (total % 3600) / 60;
							long seconds = total % 60;
							binding.totalTrackedTime.setText(
									getString(R.string.total_time, hours, minutes, seconds));
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getActionMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								viewModel.clearActionMessage();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								Toasty.show(requireContext(), error);
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applyFullScreenSheetStyle(dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
