package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetMilestonesFilterBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class BottomSheetMilestonesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetMilestonesFilterBinding binding;
	private RepositoryContext repository;

	public static BottomSheetMilestonesFilterFragment newInstance(RepositoryContext repository) {
		BottomSheetMilestonesFilterFragment fragment = new BottomSheetMilestonesFilterFragment();
		Bundle args = new Bundle();
		args.putSerializable(RepositoryContext.INTENT_EXTRA, repository);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		binding = BottomSheetMilestonesFilterBinding.inflate(inflater, container, false);

		if (getArguments() != null) {
			repository =
					(RepositoryContext)
							getArguments().getSerializable(RepositoryContext.INTENT_EXTRA);
		}
		if (repository == null) {
			throw new IllegalStateException("RepositoryContext is required");
		}

		binding.openChip.setChecked(repository.getMilestoneState() == RepositoryContext.State.OPEN);
		binding.closedChip.setChecked(
				repository.getMilestoneState() == RepositoryContext.State.CLOSED);

		binding.openChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {
						Bundle result = new Bundle();
						result.putString("state", "open");
						getParentFragmentManager().setFragmentResult("filter_request", result);
						dismiss();
					}
				});

		binding.closedChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked) {
						Bundle result = new Bundle();
						result.putString("state", "closed");
						getParentFragmentManager().setFragmentResult("filter_request", result);
						dismiss();
					}
				});

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
