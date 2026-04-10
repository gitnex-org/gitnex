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
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.databinding.BottomsheetIssuesFilterBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.IssueFilterState;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetIssuesFilter extends BottomSheetDialogFragment {

	private BottomsheetIssuesFilterBinding binding;
	private IssuesViewModel viewModel;
	private RepositoryContext repository;
	private IssueFilterState filterState;

	public static BottomSheetIssuesFilter newInstance(RepositoryContext repository) {
		BottomSheetIssuesFilter f = new BottomSheetIssuesFilter();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetIssuesFilterBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(requireActivity()).get(IssuesViewModel.class);
		repository = RepositoryContext.fromBundle(requireArguments());
		filterState = viewModel.getFilterState();

		setupUI();
		return binding.getRoot();
	}

	private void setupUI() {
		binding.searchQueryEdit.setText(filterState.query);

		if ("closed".equals(filterState.state)) {
			binding.chipClosed.setChecked(true);
		} else {
			binding.chipOpen.setChecked(true);
		}

		binding.chipMentioned.setChecked(filterState.mentionedBy != null);

		updateLabelsPreview();
		updateMilestonesPreview();

		binding.btnSelectLabels.setOnClickListener(v -> showLabelPicker());
		binding.btnSelectMilestone.setOnClickListener(v -> showMilestonePicker());

		binding.btnReset.setOnClickListener(
				v -> {
					filterState.reset();
					dismiss();
					viewModel.applyFilters(
							requireContext(), repository.getOwner(), repository.getName(), 30);
				});

		binding.btnApply.setOnClickListener(
				v -> {
					filterState.query =
							Objects.requireNonNull(binding.searchQueryEdit.getText()).toString();
					filterState.state = binding.chipOpen.isChecked() ? "open" : "closed";
					filterState.mentionedBy =
							binding.chipMentioned.isChecked() ? repository.getMentionedBy() : null;

					viewModel.applyFilters(
							requireContext(), repository.getOwner(), repository.getName(), 30);
					dismiss();
				});
	}

	private void updateLabelsPreview() {
		binding.selectedLabelsGroup.removeAllViews();
		for (String labelName : filterState.selectedLabels) {
			Chip chip = new Chip(requireContext());
			chip.setText(labelName);
			chip.setClickable(false);
			chip.setCheckable(false);
			chip.setCloseIconVisible(true);

			chip.setOnCloseIconClickListener(
					v -> {
						filterState.selectedLabels.remove(labelName);
						binding.selectedLabelsGroup.removeView(chip);

						if (filterState.selectedLabels.isEmpty()) {
							binding.selectedLabelsGroup.setVisibility(View.GONE);
						}
					});
			binding.selectedLabelsGroup.addView(chip);
		}
		binding.selectedLabelsGroup.setVisibility(
				filterState.selectedLabels.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void showLabelPicker() {
		BottomSheetLabelPicker picker =
				BottomSheetLabelPicker.newInstance(repository, filterState.selectedLabels);
		picker.setOnLabelsSelectedListener(
				selected -> {
					filterState.selectedLabels = new ArrayList<>(selected);
					updateLabelsPreview();
				});
		picker.show(getChildFragmentManager(), "label_picker");
	}

	private void updateMilestonesPreview() {
		if (filterState.milestoneTitle != null) {
			binding.chipSelectedMilestone.setText(filterState.milestoneTitle);
			binding.chipSelectedMilestone.setVisibility(View.VISIBLE);
			binding.chipSelectedMilestone.setClickable(false);
			binding.chipSelectedMilestone.setCheckable(false);

			binding.chipSelectedMilestone.setOnCloseIconClickListener(
					v -> {
						filterState.milestoneTitle = null;
						binding.chipSelectedMilestone.setVisibility(View.GONE);
					});
		} else {
			binding.chipSelectedMilestone.setVisibility(View.GONE);
		}
	}

	private void showMilestonePicker() {
		List<String> current = new ArrayList<>();
		if (filterState.milestoneTitle != null) {
			current.add(filterState.milestoneTitle);
		}

		BottomSheetMilestonePicker picker =
				BottomSheetMilestonePicker.newInstance(repository, current);

		picker.setOnMilestonesSelectedListener(
				selected -> {
					if (selected == null || selected.isEmpty()) {
						filterState.milestoneTitle = null;
					} else {
						filterState.milestoneTitle = selected.iterator().next();
					}
					updateMilestonesPreview();
				});

		picker.show(getChildFragmentManager(), "milestone_picker");
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
