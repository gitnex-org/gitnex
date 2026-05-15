package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomsheetCreateRepoBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateRepo extends BottomSheetDialogFragment {

	private BottomsheetCreateRepoBinding binding;
	private RepositoriesViewModel viewModel;
	private String preSelectedOwner;
	private String loginUid;

	public static BottomSheetCreateRepo newInstance(String owner, boolean isOrg) {
		BottomSheetCreateRepo fragment = new BottomSheetCreateRepo();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putBoolean("isOrg", isOrg);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateRepoBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		if (getActivity() instanceof BaseActivity activity) {
			loginUid = activity.getAccount().getAccount().getUserName();
		}

		if (getArguments() != null) {
			preSelectedOwner = getArguments().getString("owner");
		}

		setupListeners();
		observeViewModel();

		if (loginUid != null) {
			viewModel.loadSetupData(requireContext(), loginUid);
		} else {
			Toasty.show(requireContext(), getString(R.string.genericError));
			dismiss();
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.newRepoDescription.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		binding.btnCreate.setOnClickListener(
				v -> {
					String name =
							Objects.requireNonNull(binding.newRepoName.getText()).toString().trim();
					String desc =
							Objects.requireNonNull(binding.newRepoDescription.getText())
									.toString()
									.trim();
					String owner = binding.ownerSpinner.getText().toString();
					String branch =
							Objects.requireNonNull(binding.defaultBranch.getText())
									.toString()
									.trim();
					String gitignore = binding.gitignoreTemplates.getText().toString();
					String selectedLabels = binding.issueLabels.getText().toString();

					int licensePos = getLicensePosition();

					viewModel.validateAndCreate(
							requireContext(),
							name,
							desc,
							owner,
							loginUid,
							branch,
							licensePos,
							gitignore,
							selectedLabels,
							binding.switchPrivate.isChecked(),
							binding.switchTemplate.isChecked());
				});
	}

	private int getLicensePosition() {
		List<String> list = viewModel.getLicensesDisplay().getValue();
		if (list != null) {
			return list.indexOf(binding.licenses.getText().toString());
		}
		return 0;
	}

	private void observeViewModel() {
		viewModel
				.getIsInitialLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.setupLoader.setVisibility(loading ? View.VISIBLE : View.GONE);
							binding.btnCreate.setEnabled(!loading);
						});

		viewModel
				.getOrganizations()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.list_spinner_items, list);
							binding.ownerSpinner.setAdapter(adapter);

							if (preSelectedOwner != null && list.contains(preSelectedOwner)) {
								binding.ownerSpinner.setText(preSelectedOwner, false);
							} else {
								binding.ownerSpinner.setText(loginUid, false);
							}
						});

		viewModel
				.getLicensesDisplay()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.list_spinner_items, list);
							binding.licenses.setAdapter(adapter);
							if (!list.isEmpty()) binding.licenses.setText(list.get(0), false);
						});

		viewModel
				.getGitignores()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.list_spinner_items, list);
							binding.gitignoreTemplates.setAdapter(adapter);
							if (!list.isEmpty())
								binding.gitignoreTemplates.setText(list.get(0), false);
						});

		viewModel
				.getIssueLabels()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											requireContext(), R.layout.list_spinner_items, list);
							binding.issueLabels.setAdapter(adapter);
							if (!list.isEmpty()) {
								binding.issueLabels.setText(list.get(0), false);
							}
						});

		viewModel
				.getIsCreating()
				.observe(
						getViewLifecycleOwner(),
						creating -> {
							binding.btnCreate.setEnabled(!creating);
							binding.btnCreate.setText(
									creating ? "" : getString(R.string.newCreateButtonCopy));
							binding.creationIndicator.setVisibility(
									creating ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getCreateSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (success) {
								Toasty.show(requireContext(), getString(R.string.repoCreated));
								Bundle result = new Bundle();
								result.putBoolean("refresh", true);
								getParentFragmentManager()
										.setFragmentResult("repo_created", result);

								dismiss();
							}
						});

		viewModel
				.getErrorAction()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								Toasty.show(requireContext(), error);
								viewModel.resetStatus();
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
