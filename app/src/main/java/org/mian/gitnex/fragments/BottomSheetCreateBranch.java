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
import org.mian.gitnex.databinding.BottomsheetCreateBranchBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.viewmodels.FilesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateBranch extends BottomSheetDialogFragment {

	private BottomsheetCreateBranchBinding binding;
	private FilesViewModel viewModel;
	private String owner;
	private String repo;
	private String initialSourceRef;
	private OnBranchCreatedListener listener;

	public interface OnBranchCreatedListener {
		void onBranchCreated(String branchName);
	}

	public static BottomSheetCreateBranch newInstance(String owner, String repo, String sourceRef) {
		BottomSheetCreateBranch fragment = new BottomSheetCreateBranch();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		args.putString("sourceRef", sourceRef);
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnBranchCreatedListener(OnBranchCreatedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			owner = getArguments().getString("owner");
			repo = getArguments().getString("repo");
			initialSourceRef = getArguments().getString("sourceRef");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateBranchBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(FilesViewModel.class);

		viewModel.clearCreatedBranch();
		viewModel.clearCreateBranchError();

		setupUI();
		setupListeners();
		observeViewModel();
	}

	private void setupUI() {
		if (initialSourceRef != null && !initialSourceRef.isEmpty()) {
			binding.sourceRef.setText(initialSourceRef);
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnCreate.setOnClickListener(v -> createBranch());
	}

	private void createBranch() {
		String branchName =
				binding.branchName.getText() != null
						? binding.branchName.getText().toString().trim()
						: "";
		String sourceRef =
				binding.sourceRef.getText() != null
						? binding.sourceRef.getText().toString().trim()
						: "";

		if (branchName.isEmpty()) {
			Toasty.show(requireContext(), R.string.create_branch_empty_fields);
			return;
		}

		if (!AppUtil.isValidGitBranchName(branchName)) {
			Toasty.show(requireContext(), R.string.newFileInvalidBranchName);
			return;
		}

		if (sourceRef.isEmpty()) {
			Toasty.show(requireContext(), R.string.create_branch_empty_fields);
			return;
		}

		viewModel.createBranch(requireContext(), owner, repo, branchName, sourceRef);
	}

	private void observeViewModel() {
		viewModel
				.getIsCreatingBranch()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							binding.btnCreate.setEnabled(!isCreating);
							binding.btnCreate.setText(
									isCreating ? "" : getString(R.string.create_branch));
							binding.loadingIndicator.setVisibility(
									isCreating ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getCreatedBranch()
				.observe(
						getViewLifecycleOwner(),
						branch -> {
							if (branch != null) {
								Toasty.show(requireContext(), R.string.branch_created);
								if (listener != null) {
									listener.onBranchCreated(branch.getName());
								}
								viewModel.clearCreatedBranch();
								dismiss();
							}
						});

		viewModel
				.getCreateBranchError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								if (error.equals("UNAUTHORIZED")) {
									TokenAuthorizationDialog.authorizationTokenRevokedDialog(
											requireContext());
								} else {
									Toasty.show(requireContext(), error);
								}
								viewModel.clearCreateBranchError();
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
