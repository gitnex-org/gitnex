package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomSheetIssuesFilterBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 */
public class BottomSheetIssuesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;
	private BottomSheetIssuesFilterBinding binding;
	private RepositoryContext repository;

	public static BottomSheetIssuesFilterFragment newInstance(RepositoryContext repository) {
		BottomSheetIssuesFilterFragment fragment = new BottomSheetIssuesFilterFragment();
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

		binding = BottomSheetIssuesFilterBinding.inflate(inflater, container, false);

		if (getArguments() != null) {
			repository =
					(RepositoryContext)
							getArguments().getSerializable(RepositoryContext.INTENT_EXTRA);
		}
		if (repository == null) {
			throw new IllegalStateException("RepositoryContext is required");
		}

		if (((BaseActivity) requireActivity()).getAccount().requiresVersion("1.14.0")) {
			binding.milestoneChip.setVisibility(View.VISIBLE);
		}

		binding.openChip.setChecked(repository.getIssueState() == RepositoryContext.State.OPEN);
		binding.closedChip.setChecked(repository.getIssueState() == RepositoryContext.State.CLOSED);
		binding.mentionsChip.setChecked(repository.getMentionedBy() != null);

		binding.openChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked && repository.getIssueState() != RepositoryContext.State.OPEN) {
						repository.setIssueState(RepositoryContext.State.OPEN);
						bmListener.onButtonClicked("openIssues");
						dismiss();
					} else if (!isChecked) {
						buttonView.setChecked(true);
					}
				});

		binding.closedChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked && repository.getIssueState() != RepositoryContext.State.CLOSED) {
						repository.setIssueState(RepositoryContext.State.CLOSED);
						bmListener.onButtonClicked("closedIssues");
						dismiss();
					} else if (!isChecked) {
						buttonView.setChecked(true);
					}
				});

		binding.mentionsChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					String username =
							isChecked
									? ((BaseActivity) requireActivity())
											.getAccount()
											.getAccount()
											.getUserName()
									: null;
					repository.setMentionedBy(username);
					bmListener.onButtonClicked(
							"mentionedByMe:" + (username != null ? username : "null"));
					dismiss();
				});

		binding.labelsChip.setOnClickListener(
				v -> {
					bmListener.onButtonClicked("filterByLabels");
					dismiss();
				});

		binding.milestoneChip.setOnClickListener(
				v -> {
					bmListener.onButtonClicked("filterByMilestone");
					dismiss();
				});

		return binding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		try {
			bmListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
