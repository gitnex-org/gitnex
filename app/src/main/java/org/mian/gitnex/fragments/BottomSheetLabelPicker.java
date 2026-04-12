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
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.adapters.LabelSelectionAdapter;
import org.mian.gitnex.databinding.BottomsheetIssuesLabelPickerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetLabelPicker extends BottomSheetDialogFragment {

	public interface OnLabelsSelectedListener {
		void onSelected(Set<String> selected);
	}

	public interface OnLabelsSelectedWithIdsListener {
		void onSelected(Set<String> selectedLabels, Map<String, Long> labelIds);
	}

	private BottomsheetIssuesLabelPickerBinding binding;
	private OnLabelsSelectedListener listener;
	private OnLabelsSelectedWithIdsListener listenerWithIds;
	private Set<String> selectedLabels;
	private final Map<String, Long> labelIdMap = new HashMap<>();
	private RepositoryContext repository;
	private LabelsViewModel labelsViewModel;
	private LabelSelectionAdapter adapter;
	private int resultLimit;

	public static BottomSheetLabelPicker newInstance(RepositoryContext repo, List<String> current) {
		BottomSheetLabelPicker f = new BottomSheetLabelPicker();
		Bundle b = repo.getBundle();
		b.putStringArrayList("current_labels", new ArrayList<>(current));
		f.setArguments(b);
		return f;
	}

	public void setOnLabelsSelectedListener(OnLabelsSelectedListener l) {
		this.listener = l;
	}

	public void setOnLabelsSelectedWithIdsListener(OnLabelsSelectedWithIdsListener l) {
		this.listenerWithIds = l;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetIssuesLabelPickerBinding.inflate(inflater, container, false);
		labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);

		Bundle args = requireArguments();
		repository = RepositoryContext.fromBundle(args);
		selectedLabels =
				new HashSet<>(Objects.requireNonNull(args.getStringArrayList("current_labels")));
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		observeViewModel();

		if (Boolean.FALSE.equals(labelsViewModel.getHasLoadedOnce().getValue())) {
			fetchPage(1, true);
		}

		binding.btnDone.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onSelected(selectedLabels);
					}
					if (listenerWithIds != null) {
						Map<String, Long> selectedIds = new HashMap<>();
						for (String labelName : selectedLabels) {
							Long id = labelIdMap.get(labelName);
							if (id != null) {
								selectedIds.put(labelName, id);
							}
						}
						listenerWithIds.onSelected(selectedLabels, selectedIds);
					}
					dismiss();
				});

		return binding.getRoot();
	}

	private void fetchPage(int page, boolean isRefresh) {
		if (page == 1) {
			labelsViewModel.fetchLabelsMerged(
					requireContext(), repository.getOwner(), repository.getName(), 1, resultLimit);
		} else {
			labelsViewModel.fetchLabels(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					"repo",
					page,
					resultLimit,
					false);
		}
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.rvLabels.setLayoutManager(layoutManager);
		adapter = new LabelSelectionAdapter(new ArrayList<>(), selectedLabels);
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
		labelsViewModel
				.getLabels()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							List<Label> data = (list != null) ? list : new ArrayList<>();

							for (Label label : data) {
								if (label.getName() != null && label.getId() != null) {
									labelIdMap.put(label.getName(), label.getId());
								}
							}

							adapter.updateList(data);
							if (!data.isEmpty()) {
								binding.rvLabels.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							}
						});

		labelsViewModel
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
