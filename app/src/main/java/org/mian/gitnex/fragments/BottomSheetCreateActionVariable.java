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
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateRepoActionVariableBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositoryActionsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateActionVariable extends BottomSheetDialogFragment {

	private BottomsheetCreateRepoActionVariableBinding binding;
	private RepositoryActionsViewModel viewModel;
	private String owner;
	private String repo;

	public static BottomSheetCreateActionVariable newInstance(String owner, String repo) {
		BottomSheetCreateActionVariable fragment = new BottomSheetCreateActionVariable();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			owner = getArguments().getString("owner");
			repo = getArguments().getString("repo");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateRepoActionVariableBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(RepositoryActionsViewModel.class);

		viewModel.clearVariableCreated();
		viewModel.clearCreateVariableError();

		setupListeners();
		observeViewModel();
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.createButton.setOnClickListener(v -> createVariable());
	}

	private void createVariable() {
		String name =
				binding.variableName.getText() != null
						? binding.variableName.getText().toString().trim()
						: "";
		String value =
				binding.variableValue.getText() != null
						? binding.variableValue.getText().toString().trim()
						: "";
		String description =
				binding.variableDescription.getText() != null
						? binding.variableDescription.getText().toString().trim()
						: "";

		if (name.isEmpty()) {
			Toasty.show(requireContext(), R.string.variable_name_error);
			return;
		}
		if (!name.matches("^[a-zA-Z0-9_]+$")) {
			Toasty.show(requireContext(), R.string.variable_name_invalid);
			return;
		}
		if (value.isEmpty()) {
			Toasty.show(requireContext(), R.string.variable_value_error);
			return;
		}

		viewModel.createVariable(requireContext(), owner, repo, name, value, description);
	}

	private void observeViewModel() {
		viewModel
				.getIsCreatingVariable()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							binding.createButton.setEnabled(!isCreating);
							binding.createButton.setText(
									isCreating ? "" : getString(R.string.newCreateButtonCopy));
						});

		viewModel
				.getVariableCreated()
				.observe(
						getViewLifecycleOwner(),
						created -> {
							if (created != null && created) {
								Toasty.show(requireContext(), R.string.variable_create_success);
								viewModel.resetVariablesPagination();
								dismiss();
							}
						});

		viewModel
				.getCreateVariableError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(requireContext(), error);
								viewModel.clearCreateVariableError();
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
