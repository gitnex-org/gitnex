package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateReleaseOption;
import org.gitnex.tea4j.v2.models.CreateTagOption;
import org.gitnex.tea4j.v2.models.Release;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateReleaseBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.ReleasesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateRelease extends BottomSheetDialogFragment {

	private BottomsheetCreateReleaseBinding binding;
	private ReleasesViewModel viewModel;
	private RepositoryContext repoContext;
	private Release releaseToEdit;
	private String selectedBranch = null;
	private boolean isReleaseMode = true;

	public static BottomSheetCreateRelease newInstance(
			RepositoryContext repository, @Nullable Release release) {
		BottomSheetCreateRelease fragment = new BottomSheetCreateRelease();
		Bundle args = new Bundle();
		args.putSerializable("repo_context", repository);
		if (release != null) {
			args.putSerializable("release_item", release);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable("repo_context");
			releaseToEdit = (Release) getArguments().getSerializable("release_item");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateReleaseBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(ReleasesViewModel.class);

		viewModel.clearCreatedRelease();
		viewModel.clearCreatedTag();
		viewModel.clearUpdatedRelease();

		setupUI();
		setupListeners();
		observeViewModel();
	}

	private void setupUI() {
		boolean hasWriteAccess =
				repoContext.getPermissions() != null && repoContext.getPermissions().isPush();

		binding.releaseContent.setOnTouchListener(
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

		binding.cardBranch.cardIcon.setImageResource(R.drawable.ic_branch);
		binding.cardBranch.tvCardLabel.setText(R.string.branch);
		binding.cardBranch.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		if (releaseToEdit != null) {
			binding.sheetTitle.setText(R.string.editRelease);
			binding.createTypeToggle.setVisibility(View.GONE);
			binding.releaseTagNameLayout.setVisibility(View.GONE);
			binding.switchDraft.setVisibility(View.GONE);
			binding.descriptionContainer.setVisibility(View.VISIBLE);
			binding.switchPrerelease.setVisibility(View.VISIBLE);

			binding.releaseTitle.setText(releaseToEdit.getName());
			binding.releaseContent.setText(releaseToEdit.getBody());
			binding.switchPrerelease.setChecked(releaseToEdit.isPrerelease());

			binding.cardBranch.getRoot().setVisibility(View.GONE);

			binding.btnSubmit.setText(R.string.update);
		} else {
			binding.sheetTitle.setText(R.string.createRelease);
			binding.descriptionContainer.setVisibility(View.VISIBLE);
			binding.switchPrerelease.setVisibility(View.VISIBLE);
			binding.switchDraft.setVisibility(View.VISIBLE);

			if (hasWriteAccess) {
				updateBranchDisplay();
			}
		}

		updateBranchClearButtonVisibility();
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnExpand.setOnClickListener(v -> openFullScreenEditor());
		binding.btnSubmit.setOnClickListener(v -> submitAction());

		boolean hasWriteAccess =
				repoContext.getPermissions() != null && repoContext.getPermissions().isPush();

		if (hasWriteAccess) {
			binding.cardBranch.getRoot().setOnClickListener(v -> openBranchPicker());
			binding.cardBranch.btnClear.setOnClickListener(
					v -> {
						selectedBranch = null;
						updateBranchDisplay();
						updateBranchClearButtonVisibility();
					});
		}

		binding.createTypeToggle.addOnButtonCheckedListener(
				(group, checkedId, isChecked) -> {
					if (isChecked) {
						isReleaseMode = checkedId == R.id.btn_create_release;
						updateUIForMode();
					}
				});
	}

	private void updateUIForMode() {
		if (releaseToEdit == null) {
			if (isReleaseMode) {
				binding.releaseTitleLayout.setHint(R.string.releaseTitleText);
				binding.descriptionContainer.setVisibility(View.VISIBLE);
				binding.switchPrerelease.setVisibility(View.VISIBLE);
				binding.switchDraft.setVisibility(View.VISIBLE);
				binding.btnSubmit.setText(R.string.createRelease);
			} else {
				binding.releaseTitleLayout.setHint(R.string.description);
				binding.descriptionContainer.setVisibility(View.GONE);
				binding.switchPrerelease.setVisibility(View.GONE);
				binding.switchDraft.setVisibility(View.GONE);
				binding.btnSubmit.setText(R.string.createTag);
			}
		}
	}

	private void openBranchPicker() {
		BottomsheetBranchPicker branchPicker =
				BottomsheetBranchPicker.newInstance(
						repoContext.getOwner(),
						repoContext.getName(),
						selectedBranch != null ? selectedBranch : repoContext.getBranchRef());

		branchPicker.setOnBranchSelectedListener(
				branchName -> {
					selectedBranch = branchName;
					updateBranchDisplay();
					updateBranchClearButtonVisibility();
				});

		branchPicker.show(getParentFragmentManager(), "BRANCH_PICKER");
	}

	private void updateBranchDisplay() {
		if (selectedBranch == null || selectedBranch.isEmpty()) {
			binding.cardBranch.tvSelectedText.setText(R.string.add_release_branch);
		} else {
			binding.cardBranch.tvSelectedText.setText(selectedBranch);
		}
	}

	private void updateBranchClearButtonVisibility() {
		binding.cardBranch.btnClear.setVisibility(
				selectedBranch == null || selectedBranch.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void openFullScreenEditor() {
		BottomSheetFullScreenEditor editorBottomSheet =
				BottomSheetFullScreenEditor.newInstance(
						Objects.requireNonNull(binding.releaseContent.getText()).toString(),
						repoContext,
						true,
						true);

		editorBottomSheet.setEditorListener(
				newContent -> {
					binding.releaseContent.setText(newContent);
					binding.releaseContent.setSelection(
							newContent != null ? newContent.length() : 0);
				});

		editorBottomSheet.show(getParentFragmentManager(), "FULLSCREEN_EDITOR");
	}

	private void submitAction() {
		if (releaseToEdit != null) {
			submitUpdateRelease();
		} else if (isReleaseMode) {
			submitCreateRelease();
		} else {
			submitCreateTag();
		}
	}

	private void submitCreateRelease() {
		String tagName =
				binding.releaseTagName.getText() != null
						? binding.releaseTagName.getText().toString().trim()
						: "";
		String title =
				binding.releaseTitle.getText() != null
						? binding.releaseTitle.getText().toString().trim()
						: "";
		String content =
				binding.releaseContent.getText() != null
						? binding.releaseContent.getText().toString().trim()
						: "";

		if (tagName.isEmpty()) {
			Toasty.show(requireContext(), R.string.tagNameErrorEmpty);
			return;
		}

		if (title.isEmpty()) {
			Toasty.show(requireContext(), R.string.titleErrorEmpty);
			return;
		}

		if (selectedBranch == null || selectedBranch.isEmpty()) {
			Toasty.show(requireContext(), R.string.selectBranchError);
			return;
		}

		CreateReleaseOption releaseData = new CreateReleaseOption();
		releaseData.setTagName(tagName);
		releaseData.setName(title);
		releaseData.setName(title);
		releaseData.setBody(content);
		releaseData.setTargetCommitish(selectedBranch);
		releaseData.setDraft(binding.switchDraft.isChecked());
		releaseData.setPrerelease(binding.switchPrerelease.isChecked());

		viewModel.createRelease(
				requireContext(), repoContext.getOwner(), repoContext.getName(), releaseData);
	}

	private void submitCreateTag() {
		String tagName =
				binding.releaseTagName.getText() != null
						? binding.releaseTagName.getText().toString().trim()
						: "";
		String message =
				binding.releaseTitle.getText() != null
						? binding.releaseTitle.getText().toString().trim()
						: "";

		if (tagName.isEmpty()) {
			Toasty.show(requireContext(), R.string.tagNameErrorEmpty);
			return;
		}

		if (selectedBranch == null || selectedBranch.isEmpty()) {
			Toasty.show(requireContext(), R.string.selectBranchError);
			return;
		}

		CreateTagOption tagData = new CreateTagOption();
		tagData.setTagName(tagName);
		tagData.setMessage(message);
		tagData.setTarget(selectedBranch);

		viewModel.createTag(
				requireContext(), repoContext.getOwner(), repoContext.getName(), tagData);
	}

	private void submitUpdateRelease() {
		String title =
				binding.releaseTitle.getText() != null
						? binding.releaseTitle.getText().toString().trim()
						: "";
		String content =
				binding.releaseContent.getText() != null
						? binding.releaseContent.getText().toString().trim()
						: "";

		if (title.isEmpty()) {
			Toasty.show(requireContext(), R.string.titleErrorEmpty);
			return;
		}

		viewModel.updateRelease(
				requireContext(),
				repoContext.getOwner(),
				repoContext.getName(),
				releaseToEdit.getId(),
				title,
				content,
				binding.switchPrerelease.isChecked());
	}

	private void observeViewModel() {
		viewModel
				.getIsCreatingRelease()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							binding.loadingIndicator.setVisibility(
									isCreating ? View.VISIBLE : View.GONE);
							binding.btnSubmit.setEnabled(!isCreating);
							binding.btnSubmit.setText(
									isCreating
											? ""
											: getString(
													releaseToEdit != null
															? R.string.update
															: (isReleaseMode
																	? R.string.createRelease
																	: R.string.createTag)));
						});

		viewModel
				.getCreatedRelease()
				.observe(
						getViewLifecycleOwner(),
						release -> {
							if (release != null) {
								Toasty.show(requireContext(), R.string.releaseCreatedText);
								dismiss();
							}
						});

		viewModel
				.getCreateReleaseError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								handleError(error);
								viewModel.clearCreateReleaseError();
							}
						});

		viewModel
				.getIsCreatingTag()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							if (!isReleaseMode) {
								binding.loadingIndicator.setVisibility(
										isCreating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isCreating);
								binding.btnSubmit.setText(
										isCreating ? "" : getString(R.string.createTag));
							}
						});

		viewModel
				.getCreatedTag()
				.observe(
						getViewLifecycleOwner(),
						tag -> {
							if (tag != null) {
								Toasty.show(requireContext(), R.string.tagCreated);
								dismiss();
							}
						});

		viewModel
				.getCreateTagError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								handleError(error);
								viewModel.clearCreateTagError();
							}
						});

		viewModel
				.getIsUpdatingRelease()
				.observe(
						getViewLifecycleOwner(),
						isUpdating -> {
							if (releaseToEdit != null) {
								binding.loadingIndicator.setVisibility(
										isUpdating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isUpdating);
								binding.btnSubmit.setText(
										isUpdating ? "" : getString(R.string.update));
							}
						});

		viewModel
				.getUpdatedRelease()
				.observe(
						getViewLifecycleOwner(),
						release -> {
							if (release != null) {
								Toasty.show(requireContext(), R.string.editReleaseSuccessMessage);
								dismiss();
							}
						});

		viewModel
				.getUpdateReleaseError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								handleError(error);
								viewModel.clearUpdateReleaseError();
							}
						});
	}

	private void handleError(String error) {
		if (error.equals("UNAUTHORIZED")) {
			TokenAuthorizationDialog.authorizationTokenRevokedDialog(requireContext());
		} else if (error.equals(getString(R.string.tagNameConflictError))) {
			Toasty.show(requireContext(), R.string.tagNameConflictError);
		} else {
			Toasty.show(requireContext(), error);
		}
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
