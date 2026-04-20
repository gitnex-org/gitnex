package org.mian.gitnex.fragments;

import android.app.Dialog;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MilestoneSelectionAdapter;
import org.mian.gitnex.databinding.BottomsheetLabelPickerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.MilestonesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetMilestonePicker extends BottomSheetDialogFragment {

	public interface OnMilestonesSelectedListener {
		void onSelected(Set<String> selected);
	}

	public interface OnMilestonesSelectedWithIdsListener {
		void onSelected(Set<String> selectedMilestones, Map<String, Long> milestoneIds);
	}

	private BottomsheetLabelPickerBinding binding;
	private OnMilestonesSelectedListener listener;
	private OnMilestonesSelectedWithIdsListener listenerWithIds;
	private Set<String> selectedMilestones;
	private final Map<String, Long> milestoneIdMap = new HashMap<>();
	private RepositoryContext repository;
	private MilestonesViewModel viewModel;
	private MilestoneSelectionAdapter adapter;
	private int resultLimit;

	public static BottomSheetMilestonePicker newInstance(
			RepositoryContext repo, List<String> current) {
		BottomSheetMilestonePicker f = new BottomSheetMilestonePicker();
		Bundle b = repo.getBundle();
		b.putStringArrayList("current_milestones", new ArrayList<>(current));
		f.setArguments(b);
		return f;
	}

	public void setOnMilestonesSelectedListener(OnMilestonesSelectedListener l) {
		this.listener = l;
	}

	public void setOnMilestonesSelectedWithIdsListener(OnMilestonesSelectedWithIdsListener l) {
		this.listenerWithIds = l;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetLabelPickerBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

		Bundle args = requireArguments();
		repository = RepositoryContext.fromBundle(args);
		selectedMilestones =
				new HashSet<>(
						Objects.requireNonNull(args.getStringArrayList("current_milestones")));
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		binding.title.setText(R.string.selectMilestone);

		setupRecyclerView();
		observeViewModel();

		if (Boolean.FALSE.equals(viewModel.getHasLoadedOnce().getValue())) {
			fetchPage(1, true);
		}

		binding.btnDone.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onSelected(selectedMilestones);
					}
					if (listenerWithIds != null) {
						Map<String, Long> selectedIds = new HashMap<>();
						for (String milestoneName : selectedMilestones) {
							Long id = milestoneIdMap.get(milestoneName);
							if (id != null) {
								selectedIds.put(milestoneName, id);
							}
						}
						listenerWithIds.onSelected(selectedMilestones, selectedIds);
					}
					dismiss();
				});

		return binding.getRoot();
	}

	private void fetchPage(int page, boolean isRefresh) {
		viewModel.fetchMilestones(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				"open",
				page,
				resultLimit,
				isRefresh);
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.rvLabels.setLayoutManager(layoutManager);
		adapter = new MilestoneSelectionAdapter(new ArrayList<>(), selectedMilestones);
		binding.rvLabels.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						fetchPage(page, false);
					}
				};
		binding.rvLabels.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getMilestones()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							List<Milestone> data = (list != null) ? list : new ArrayList<>();

							for (Milestone milestone : data) {
								if (milestone.getTitle() != null && milestone.getId() != null) {
									milestoneIdMap.put(milestone.getTitle(), milestone.getId());
								}
							}

							adapter.updateList(data);
							if (!data.isEmpty()) {
								binding.rvLabels.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							}
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading) {
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
									binding.layoutEmpty.getRoot().setVisibility(View.GONE);
									binding.rvLabels.setVisibility(View.GONE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								if (adapter.getItemCount() == 0) {
									binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
									binding.rvLabels.setVisibility(View.GONE);
								} else {
									binding.layoutEmpty.getRoot().setVisibility(View.GONE);
									binding.rvLabels.setVisibility(View.VISIBLE);
								}
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
