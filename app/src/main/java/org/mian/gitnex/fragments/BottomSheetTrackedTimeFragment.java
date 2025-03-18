package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gitnex.tea4j.v2.models.AddTimeOption;
import org.gitnex.tea4j.v2.models.TrackedTime;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.TrackedTimeAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetTrackedTimeBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class BottomSheetTrackedTimeFragment extends BottomSheetDialogFragment {

	private BottomSheetTrackedTimeBinding binding;
	private IssueContext issue;
	private TrackedTimeAdapter trackedTimeAdapter;
	private List<TrackedTime> trackedTimeList;
	private int currentPage = 1;
	private boolean isLoading = false;
	private boolean hasMore = true;
	private int resultLimit;

	public static BottomSheetTrackedTimeFragment newInstance(IssueContext issue) {

		BottomSheetTrackedTimeFragment fragment = new BottomSheetTrackedTimeFragment();
		Bundle args = new Bundle();
		args.putSerializable(IssueContext.INTENT_EXTRA, issue);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		binding = BottomSheetTrackedTimeBinding.inflate(inflater, container, false);

		if (getArguments() != null) {
			issue = (IssueContext) getArguments().getSerializable(IssueContext.INTENT_EXTRA);
		}
		if (issue == null) {
			throw new IllegalStateException("IssueContext is required");
		}

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		trackedTimeList = new ArrayList<>();
		trackedTimeAdapter = new TrackedTimeAdapter(trackedTimeList);
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.trackedTimeRecyclerView.setLayoutManager(layoutManager);
		binding.trackedTimeRecyclerView.setAdapter(trackedTimeAdapter);

		trackedTimeAdapter.setOnDeleteClickListener(this::deleteTrackedTime);

		binding.trackedTimeRecyclerView.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
						super.onScrolled(recyclerView, dx, dy);
						int totalItems = layoutManager.getItemCount();
						int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
						if (!isLoading
								&& hasMore
								&& totalItems > 0
								&& lastVisibleItem >= totalItems - 5) {
							currentPage++;
							loadTrackedTimes();
						}
					}
				});

		binding.addTimeButton.setEnabled(true);
		binding.addTimeButton.setOnClickListener(v -> addTrackedTime());

		loadTrackedTimes();

		return binding.getRoot();
	}

	private void loadTrackedTimes() {

		if (isLoading) return;
		isLoading = true;

		Call<List<TrackedTime>> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueTrackedTimes(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								null,
								null,
								null,
								currentPage,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<TrackedTime>> call,
							@NonNull Response<List<TrackedTime>> response) {
						isLoading = false;
						if (response.isSuccessful() && response.body() != null) {
							List<TrackedTime> newTimes = response.body();
							int startPosition = trackedTimeList.size();
							trackedTimeList.addAll(newTimes);
							trackedTimeAdapter.notifyItemRangeInserted(
									startPosition, newTimes.size());
							hasMore = newTimes.size() >= resultLimit;
							updateTotalTime();
							updateUI();
						} else {
							hasMore = false;
							updateUI();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<TrackedTime>> call, @NonNull Throwable t) {
						isLoading = false;
						hasMore = false;
						updateUI();
					}
				});
	}

	private void addTrackedTime() {

		String hoursStr =
				binding.hoursInput.getText() != null ? binding.hoursInput.getText().toString() : "";
		String minutesStr =
				binding.minutesInput.getText() != null
						? binding.minutesInput.getText().toString()
						: "";

		int hours = hoursStr.isEmpty() ? 0 : Integer.parseInt(hoursStr);
		int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);

		if (hours == 0 && minutes == 0) {
			Toasty.warning(requireContext(), getString(R.string.enter_time));
			return;
		}

		long totalSeconds = (hours * 3600L) + (minutes * 60L);

		AddTimeOption timeOption = new AddTimeOption();
		timeOption.setCreated(new Date());
		timeOption.setTime(totalSeconds);

		Call<TrackedTime> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueAddTime(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								timeOption);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<TrackedTime> call,
							@NonNull Response<TrackedTime> response) {
						if (response.isSuccessful() && response.body() != null) {
							TrackedTime newTime = response.body();
							trackedTimeList.add(0, newTime);
							trackedTimeAdapter.notifyItemInserted(0);
							binding.trackedTimeRecyclerView.scrollToPosition(0);
							binding.hoursInput.setText("");
							binding.minutesInput.setText("");
							updateTotalTime();
							updateUI();
							Toasty.success(requireContext(), getString(R.string.time_added));
						} else {
							Toasty.error(requireContext(), getString(R.string.time_add_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<TrackedTime> call, @NonNull Throwable t) {
						Toasty.error(
								requireContext(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void deleteTrackedTime(TrackedTime time, int position) {

		Call<Void> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueDeleteTime(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								time.getId());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {

						if (response.isSuccessful()) {

							trackedTimeList.remove(position);
							trackedTimeAdapter.notifyItemRemoved(position);
							trackedTimeAdapter.notifyItemRangeChanged(
									position, trackedTimeList.size());
							updateTotalTime();
							updateUI();
							Toasty.success(requireContext(), getString(R.string.time_removed));
						} else {
							Toasty.error(requireContext(), getString(R.string.time_delete_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						Toasty.error(
								requireContext(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void updateTotalTime() {

		long totalSeconds = 0;
		for (TrackedTime time : trackedTimeList) {
			totalSeconds += time.getTime();
		}
		long hours = totalSeconds / 3600;
		long minutes = (totalSeconds % 3600) / 60;
		long seconds = totalSeconds % 60;
		binding.totalTrackedTime.setText(getString(R.string.total_time, hours, minutes, seconds));
	}

	private void updateUI() {

		if (trackedTimeList.isEmpty()) {
			binding.trackedTimeRecyclerView.setVisibility(View.GONE);
			binding.noTrackedTimeText.setVisibility(View.VISIBLE);
		} else {
			binding.trackedTimeRecyclerView.setVisibility(View.VISIBLE);
			binding.noTrackedTimeText.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
