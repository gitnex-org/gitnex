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
import org.gitnex.tea4j.v2.models.EditRepoOption;
import org.gitnex.tea4j.v2.models.InternalTracker;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetRepoEditPropertiesBinding;
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.RepositorySettingsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetRepoProperties extends BottomSheetDialogFragment {

	private BottomsheetRepoEditPropertiesBinding binding;
	private RepositorySettingsViewModel viewModel;
	private String owner;
	private String repoName;

	public static BottomSheetRepoProperties newInstance(String owner, String repoName) {
		BottomSheetRepoProperties fragment = new BottomSheetRepoProperties();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repoName", repoName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			owner = getArguments().getString("owner");
			repoName = getArguments().getString("repoName");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetRepoEditPropertiesBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(RepositorySettingsViewModel.class);

		viewModel.clearUpdatedRepo();
		viewModel.clearUpdateError();

		setupListeners();
		observeViewModel();

		fetchRepositoryData();
	}

	private void fetchRepositoryData() {
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		binding.mainView.setVisibility(View.GONE);

		viewModel.fetchRepositoryProperties(requireContext(), owner, repoName);
	}

	private void setupUI(Repository repository) {
		binding.expressiveLoader.setVisibility(View.GONE);
		binding.mainView.setVisibility(View.VISIBLE);

		binding.repoName.setText(repository.getName());
		binding.repoWebsite.setText(repository.getWebsite());
		binding.repoDescription.setText(repository.getDescription());
		binding.repoPrivate.setChecked(repository.isPrivate());
		binding.repoAsTemplate.setChecked(repository.isTemplate());
		binding.repoEnableIssues.setChecked(repository.isHasIssues());

		if (repository.getInternalTracker() != null) {
			binding.repoEnableTimer.setChecked(
					repository.getInternalTracker().isEnableTimeTracker());
		} else {
			binding.repoEnableTimer.setVisibility(View.GONE);
		}

		binding.repoEnableWiki.setChecked(repository.isHasWiki());
		binding.repoEnablePr.setChecked(repository.isHasPullRequests());
		binding.repoEnableMerge.setChecked(repository.isAllowMergeCommits());
		binding.repoEnableRebase.setChecked(repository.isAllowRebase());
		binding.repoEnableSquash.setChecked(repository.isAllowSquashMerge());
		binding.repoEnableForceMerge.setChecked(repository.isAllowRebaseExplicit());

		if (!repository.isHasIssues()) {
			binding.repoEnableTimer.setVisibility(View.GONE);
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.repoEnableIssues.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					binding.repoEnableTimer.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				});

		binding.save.setOnClickListener(v -> saveProperties());
	}

	private void saveProperties() {
		String newRepoName =
				binding.repoName.getText() != null
						? binding.repoName.getText().toString().trim()
						: "";
		String website =
				binding.repoWebsite.getText() != null
						? binding.repoWebsite.getText().toString().trim()
						: "";
		String description =
				binding.repoDescription.getText() != null
						? binding.repoDescription.getText().toString().trim()
						: "";

		if (newRepoName.isEmpty()) {
			Toasty.show(requireContext(), R.string.repoNameErrorInvalid);
			return;
		}

		EditRepoOption repoProps = new EditRepoOption();
		repoProps.setName(newRepoName);
		repoProps.setWebsite(website);
		repoProps.setDescription(description);
		repoProps.setPrivate(binding.repoPrivate.isChecked());
		repoProps.setTemplate(binding.repoAsTemplate.isChecked());
		repoProps.setHasIssues(binding.repoEnableIssues.isChecked());
		repoProps.setHasWiki(binding.repoEnableWiki.isChecked());
		repoProps.setHasPullRequests(binding.repoEnablePr.isChecked());
		repoProps.setInternalTracker(
				new InternalTracker().enableTimeTracker(binding.repoEnableTimer.isChecked()));
		repoProps.setAllowMergeCommits(binding.repoEnableMerge.isChecked());
		repoProps.setAllowRebase(binding.repoEnableRebase.isChecked());
		repoProps.setAllowSquashMerge(binding.repoEnableSquash.isChecked());
		repoProps.setAllowRebaseExplicit(binding.repoEnableForceMerge.isChecked());

		viewModel.updateRepository(requireContext(), owner, repoName, repoProps);
	}

	private void observeViewModel() {
		viewModel
				.getRepository()
				.observe(
						getViewLifecycleOwner(),
						repository -> {
							if (repository != null) {
								setupUI(repository);
								viewModel.clearRepositoryProperties();
							}
						});

		viewModel
				.getRepositoryError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(requireContext(), error);
								viewModel.clearRepositoryPropertiesError();
								dismiss();
							}
						});

		viewModel
				.getIsUpdating()
				.observe(
						getViewLifecycleOwner(),
						isUpdating -> {
							binding.save.setVisibility(isUpdating ? View.GONE : View.VISIBLE);
							binding.processingRequest.setVisibility(
									isUpdating ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getUpdatedRepo()
				.observe(
						getViewLifecycleOwner(),
						updatedRepo -> {
							if (updatedRepo != null) {
								AppUIStateManager.refreshData();

								Toasty.show(requireContext(), R.string.repoPropertiesSaveSuccess);
								viewModel.clearUpdatedRepo();
								dismiss();
							}
						});

		viewModel
				.getUpdateError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(requireContext(), error);
								viewModel.clearUpdateError();
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
