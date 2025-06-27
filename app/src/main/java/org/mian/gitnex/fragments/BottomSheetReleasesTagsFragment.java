package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetReleasesTagsBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author opyale
 */
public class BottomSheetReleasesTagsFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;
	private BottomSheetReleasesTagsBinding binding;
	private RepositoryContext repository;

	public static BottomSheetReleasesTagsFragment newInstance(RepositoryContext repository) {
		BottomSheetReleasesTagsFragment fragment = new BottomSheetReleasesTagsFragment();
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

		binding = BottomSheetReleasesTagsBinding.inflate(inflater, container, false);

		if (getArguments() != null) {
			repository =
					(RepositoryContext)
							getArguments().getSerializable(RepositoryContext.INTENT_EXTRA);
		}
		if (repository == null) {
			throw new IllegalStateException("RepositoryContext is required");
		}

		binding.releasesChip.setChecked(!repository.isReleasesViewTypeIsTag());
		binding.tagsChip.setChecked(repository.isReleasesViewTypeIsTag());

		binding.releasesChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked && repository.isReleasesViewTypeIsTag()) {
						repository.setReleasesViewTypeIsTag(false);
						bmListener.onButtonClicked("releases");
						dismiss();
					} else if (!isChecked) {
						buttonView.setChecked(true);
					}
				});

		binding.tagsChip.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					if (isChecked && !repository.isReleasesViewTypeIsTag()) {
						repository.setReleasesViewTypeIsTag(true);
						bmListener.onButtonClicked("tags");
						dismiss();
					} else if (!isChecked) {
						buttonView.setChecked(true);
					}
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
