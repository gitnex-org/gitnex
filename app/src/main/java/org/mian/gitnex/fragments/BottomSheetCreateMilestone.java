package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateMilestoneBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.MilestonesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateMilestone extends BottomSheetDialogFragment {

	private BottomsheetCreateMilestoneBinding binding;
	private MilestonesViewModel viewModel;
	private RepositoryContext repoContext;
	private Milestone milestoneToEdit;

	public static BottomSheetCreateMilestone newInstance(
			RepositoryContext repo, Milestone milestone) {
		BottomSheetCreateMilestone fragment = new BottomSheetCreateMilestone();
		Bundle args = new Bundle();
		args.putSerializable("repo_context", repo);
		args.putSerializable("milestone_item", milestone);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateMilestoneBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			repoContext = (RepositoryContext) args.getSerializable("repo_context");
			milestoneToEdit = (Milestone) args.getSerializable("milestone_item");
		}

		if (repoContext == null) {
			dismiss();
			return;
		}

		viewModel = new ViewModelProvider(requireActivity()).get(MilestonesViewModel.class);

		setupUI();
		observeViewModel();
	}

	private void setupUI() {
		if (milestoneToEdit != null) {
			binding.sheetTitle.setText(R.string.edit_milestone);
			binding.milestoneTitle.setText(milestoneToEdit.getTitle());
			binding.milestoneDescription.setText(milestoneToEdit.getDescription());
			binding.btnAction.setText(R.string.update);

			if (milestoneToEdit.getDueOn() != null) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				String formattedDate = sdf.format(milestoneToEdit.getDueOn());
				binding.milestoneDueDate.setText(formattedDate);
			}
		}

		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.milestoneDueDate.setOnClickListener(
				v -> {
					MaterialDatePicker<Long> picker =
							MaterialDatePicker.Builder.datePicker().build();
					picker.addOnPositiveButtonClickListener(
							selection -> {
								SimpleDateFormat format =
										new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
								binding.milestoneDueDate.setText(
										format.format(new Date(selection)));
							});
					picker.show(getChildFragmentManager(), "DATE_PICKER");
				});

		binding.btnAction.setOnClickListener(
				v -> {
					String title =
							Objects.requireNonNull(binding.milestoneTitle.getText()).toString();
					String desc =
							Objects.requireNonNull(binding.milestoneDescription.getText())
									.toString();
					String date =
							Objects.requireNonNull(binding.milestoneDueDate.getText()).toString();

					if (title.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.milestoneNameErrorEmpty));
						return;
					}

					viewModel.createOrUpdateMilestone(
							getContext(),
							repoContext.getOwner(),
							repoContext.getName(),
							milestoneToEdit,
							title,
							desc,
							date);
				});
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							binding.btnAction.setEnabled(!loading);
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						result -> {
							if (result == 201 || result == 200) {
								dismiss();
								viewModel.resetActionResult();
							}
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
