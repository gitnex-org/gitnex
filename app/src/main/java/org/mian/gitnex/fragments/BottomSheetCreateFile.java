package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.io.IOException;
import java.io.InputStream;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomsheetCreateFileBinding;
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.FilesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateFile extends BottomSheetDialogFragment {

	private static final String ARG_REPO_CONTEXT = "repo_context";
	private static final String ARG_FILE_ACTION = "file_action";
	private static final String ARG_FILE_PATH = "file_path";
	private static final String ARG_FILE_SHA = "file_sha";
	private static final String ARG_FILE_CONTENT = "file_content";

	private BottomsheetCreateFileBinding binding;
	private FilesViewModel viewModel;
	private RepositoryContext repoContext;
	private FilesViewModel.FileAction fileAction;
	private String filePath;
	private String fileSha;
	private String initialContent;
	private String selectedBranch;
	private Uri selectedFileUri;
	private boolean isBinaryFile = false;
	private String defaultBranch;
	private boolean pendingCreatePr = false;
	private String pendingPrBranch = null;
	private String pendingPrTitle = null;

	private final ActivityResultLauncher<String> filePickerLauncher =
			registerForActivityResult(
					new ActivityResultContracts.GetContent(), this::handleFileSelected);

	private final ActivityResultLauncher<Intent> codeEditorLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Dialog.BUTTON_POSITIVE
								&& result.getData() != null) {
							String editedContent =
									result.getData().getStringExtra("fileContentFromActivity");
							if (editedContent != null) {
								binding.fileContent.setText(editedContent);
							}
						}
					});

	public static BottomSheetCreateFile newInstance(
			RepositoryContext repoContext,
			FilesViewModel.FileAction action,
			@Nullable String filePath,
			@Nullable String fileSha,
			@Nullable String content) {
		BottomSheetCreateFile fragment = new BottomSheetCreateFile();
		Bundle args = new Bundle();
		args.putSerializable(ARG_REPO_CONTEXT, repoContext);
		args.putString(ARG_FILE_ACTION, action.name());
		if (filePath != null) args.putString(ARG_FILE_PATH, filePath);
		if (fileSha != null) args.putString(ARG_FILE_SHA, fileSha);
		if (content != null) args.putString(ARG_FILE_CONTENT, content);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable(ARG_REPO_CONTEXT);
			fileAction =
					FilesViewModel.FileAction.valueOf(
							getArguments().getString(ARG_FILE_ACTION, "CREATE"));
			filePath = getArguments().getString(ARG_FILE_PATH);
			fileSha = getArguments().getString(ARG_FILE_SHA);
			initialContent = getArguments().getString(ARG_FILE_CONTENT);
		}
		selectedBranch = repoContext.getBranchRef();
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateFileBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(FilesViewModel.class);

		viewModel.clearOperationSuccess();
		viewModel.clearOperationError();

		setupUI();
		setupListeners();
		observeViewModel();
	}

	private void setupUI() {
		defaultBranch = repoContext.getRepository().getDefaultBranch();

		binding.cardBranch.cardIcon.setImageResource(R.drawable.ic_branch);
		binding.cardBranch.tvCardLabel.setText(R.string.branch);
		binding.cardBranch.btnClear.setVisibility(View.VISIBLE);
		binding.cardBranch.btnClear.setIconResource(R.drawable.ic_add);

		updateBranchDisplay();

		binding.fileContent.setOnTouchListener(
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

		switch (fileAction) {
			case CREATE:
				binding.sheetTitle.setText(R.string.newFileButtonCopy);
				binding.btnSubmit.setText(R.string.commit_new_file);
				break;
			case EDIT:
				binding.sheetTitle.setText(R.string.editFile);
				binding.fileName.setText(filePath);
				binding.fileName.setEnabled(false);
				if (initialContent != null) {
					binding.fileContent.setText(initialContent);
				}
				binding.btnSubmit.setText(R.string.commit_changes);
				binding.switchCreatePr.setVisibility(View.GONE);
				binding.cardUpload.getRoot().setVisibility(View.GONE);
				break;
			case DELETE:
				binding.sheetTitle.setText(R.string.deleteFile);
				binding.fileName.setText(filePath);
				binding.fileName.setEnabled(false);
				binding.contentContainer.setVisibility(View.GONE);
				binding.cardUpload.getRoot().setVisibility(View.GONE);
				binding.cardDeleteInfo.setVisibility(View.VISIBLE);
				binding.btnSubmit.setText(R.string.commit_deletion);
				binding.switchCreatePr.setVisibility(View.GONE);
				break;
		}

		String fileName =
				binding.fileName.getText() != null
						? binding.fileName.getText().toString().trim()
						: "";
		if (!fileName.isEmpty()) {
			String defaultMessage = getDefaultCommitMessage(fileName);
			binding.commitMessage.setText(defaultMessage);
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());

		binding.cardBranch.getRoot().setOnClickListener(v -> openBranchPicker());
		binding.cardBranch.btnClear.setOnClickListener(v -> openCreateBranch());

		binding.cardUpload
				.getRoot()
				.setOnClickListener(
						v -> {
							if (selectedFileUri == null) {
								filePickerLauncher.launch("*/*");
							}
						});
		binding.cardUpload.btnClear.setOnClickListener(v -> clearUploadedFile());

		binding.btnExpand.setOnClickListener(v -> openFullScreenEditor());
		binding.btnSubmit.setOnClickListener(v -> submitAction());

		binding.fileName.addTextChangedListener(
				new android.text.TextWatcher() {
					@Override
					public void beforeTextChanged(
							CharSequence s, int start, int count, int after) {}

					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {}

					@Override
					public void afterTextChanged(android.text.Editable s) {
						String fileName = s.toString().trim();
						if (fileAction == FilesViewModel.FileAction.CREATE && !fileName.isEmpty()) {
							String currentCommit =
									binding.commitMessage.getText() != null
											? binding.commitMessage.getText().toString()
											: "";
							if (currentCommit.isEmpty()
									|| currentMatchDefaultPattern(currentCommit)) {
								binding.commitMessage.setText(getDefaultCommitMessage(fileName));
							}
						}
					}
				});
	}

	private void clearUploadedFile() {
		selectedFileUri = null;
		isBinaryFile = false;
		binding.fileName.setText("");
		binding.fileContent.setText("");
		binding.contentContainer.setVisibility(View.VISIBLE);
		updateUploadCardVisibility();
	}

	private void updateUploadCardVisibility() {
		boolean hasFile = selectedFileUri != null;
		binding.cardUpload.uploadEmptyText.setVisibility(hasFile ? View.GONE : View.VISIBLE);
		binding.cardUpload.uploadPreviewContainer.setVisibility(hasFile ? View.VISIBLE : View.GONE);
		binding.cardUpload.btnClear.setVisibility(hasFile ? View.VISIBLE : View.GONE);
	}

	private void openBranchPicker() {
		BottomsheetBranchPicker branchPicker =
				BottomsheetBranchPicker.newInstance(
						repoContext.getOwner(), repoContext.getName(), selectedBranch);

		branchPicker.setOnBranchSelectedListener(
				branchName -> {
					selectedBranch = branchName;
					updateBranchDisplay();
				});

		branchPicker.show(getParentFragmentManager(), "BRANCH_PICKER");
	}

	private void openCreateBranch() {
		BottomSheetCreateBranch createBranch =
				BottomSheetCreateBranch.newInstance(
						repoContext.getOwner(), repoContext.getName(), selectedBranch);
		createBranch.setOnBranchCreatedListener(
				newBranchName -> {
					selectedBranch = newBranchName;
					updateBranchDisplay();
				});
		createBranch.show(getParentFragmentManager(), "CREATE_BRANCH");
	}

	private void openFullScreenEditor() {
		String content =
				binding.fileContent.getText() != null
						? binding.fileContent.getText().toString()
						: "";
		String fileName =
				binding.fileName.getText() != null ? binding.fileName.getText().toString() : "";
		String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);

		BottomSheetFullScreenEditor.EditorMode mode =
				fileName.endsWith(".md")
						? BottomSheetFullScreenEditor.EditorMode.MARKDOWN
						: BottomSheetFullScreenEditor.EditorMode.CODE;

		BottomSheetFullScreenEditor editor =
				BottomSheetFullScreenEditor.newInstance(content, repoContext, mode, extension);

		editor.setEditorListener(
				newContent -> {
					binding.fileContent.setText(newContent);
					binding.fileContent.setSelection(newContent != null ? newContent.length() : 0);
				});

		editor.show(getParentFragmentManager(), "FULLSCREEN_EDITOR");
	}

	private void updateBranchDisplay() {
		if (selectedBranch == null || selectedBranch.isEmpty()) {
			binding.cardBranch.tvSelectedText.setText(R.string.pageTitleChooseBranch);
			binding.switchCreatePr.setVisibility(View.GONE);
		} else {
			binding.cardBranch.tvSelectedText.setText(selectedBranch);

			boolean isDefaultBranch = selectedBranch.equals(defaultBranch);
			boolean shouldShow = fileAction == FilesViewModel.FileAction.CREATE && !isDefaultBranch;
			binding.switchCreatePr.setVisibility(shouldShow ? View.VISIBLE : View.GONE);

			if (!shouldShow) {
				binding.switchCreatePr.setChecked(false);
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private void handleFileSelected(Uri uri) {
		if (uri == null) return;
		selectedFileUri = uri;

		String fileName = AppUtil.getFileNameFromUri(requireContext(), uri);
		if (fileName != null && !fileName.isEmpty()) {
			binding.fileName.setText(fileName);
		}

		binding.cardUpload.uploadFileName.setText(fileName);

		String mimeType = AppUtil.getMimeTypeFromUri(requireContext(), uri);
		long fileSize = AppUtil.getFileSizeFromUri(requireContext(), uri);
		String fileSizeStr =
				android.text.format.Formatter.formatShortFileSize(requireContext(), fileSize);
		binding.cardUpload.uploadFileInfo.setText(
				fileSizeStr + (mimeType != null ? " • " + mimeType : ""));

		AppUtil.FileType fileType = AppUtil.getFileTypeFromFileName(fileName);
		if (fileType == AppUtil.FileType.IMAGE) {
			Glide.with(requireContext())
					.load(uri)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.cardUpload.uploadPreviewImage);
		} else {
			binding.cardUpload.uploadPreviewImage.setImageDrawable(
					AppCompatResources.getDrawable(
							requireContext(), FileIcon.getIconResource(fileName, "file")));
		}

		long tenMB = 10 * 1024 * 1024;
		if (fileSize > tenMB) {
			binding.cardUpload.uploadFileWarning.setText(R.string.large_file_warning);
			binding.cardUpload.uploadFileWarning.setVisibility(View.VISIBLE);
		} else {
			binding.cardUpload.uploadFileWarning.setVisibility(View.GONE);
		}

		updateUploadCardVisibility();

		isBinaryFile = fileType != AppUtil.FileType.TEXT;

		if (isBinaryFile) {
			binding.contentContainer.setVisibility(View.GONE);
		} else {
			binding.contentContainer.setVisibility(View.VISIBLE);
			try {
				InputStream is = requireContext().getContentResolver().openInputStream(uri);
				if (is != null) {
					String content = AppUtil.readInputStream(is);
					binding.fileContent.setText(content);
				}
			} catch (IOException e) {
				Toasty.show(requireContext(), R.string.file_read_error);
			}
		}

		if (fileAction == FilesViewModel.FileAction.CREATE && fileName != null) {
			binding.commitMessage.setText(getDefaultCommitMessage(fileName));
		}
	}

	private String getDefaultCommitMessage(String fileName) {
		if (fileName == null || fileName.isEmpty()) return "";
		return switch (fileAction) {
			case CREATE -> getString(R.string.addTeamMember, fileName);
			case EDIT -> getString(R.string.update_filename, fileName);
			case DELETE -> getString(R.string.delete_filename, fileName);
		};
	}

	private boolean currentMatchDefaultPattern(String current) {
		String fileName =
				binding.fileName.getText() != null
						? binding.fileName.getText().toString().trim()
						: "";
		if (fileName.isEmpty()) return false;
		String defaultMsg = getDefaultCommitMessage(fileName);
		return current.equals(defaultMsg)
				|| current.startsWith("Add ")
				|| current.startsWith("Update ")
				|| current.startsWith("Delete ");
	}

	private void submitAction() {
		String fileName =
				binding.fileName.getText() != null
						? binding.fileName.getText().toString().trim()
						: "";
		String commitMessage =
				binding.commitMessage.getText() != null
						? binding.commitMessage.getText().toString().trim()
						: "";

		if (fileName.isEmpty()) {
			Toasty.show(requireContext(), R.string.file_name_empty);
			return;
		}

		if (selectedBranch == null || selectedBranch.isEmpty()) {
			Toasty.show(requireContext(), R.string.select_branch_error);
			return;
		}

		if (commitMessage.isEmpty()) {
			Toasty.show(requireContext(), R.string.commit_message_empty);
			return;
		}

		if (commitMessage.length() > 255) {
			Toasty.show(requireContext(), R.string.newFileCommitMessageError);
			return;
		}

		pendingCreatePr =
				binding.switchCreatePr.isChecked()
						&& binding.switchCreatePr.getVisibility() == View.VISIBLE;
		pendingPrBranch = selectedBranch;
		pendingPrTitle = commitMessage;

		switch (fileAction) {
			case CREATE:
				if (isBinaryFile && selectedFileUri != null) {
					viewModel.createFileFromUri(
							requireContext(),
							repoContext.getOwner(),
							repoContext.getName(),
							fileName,
							selectedFileUri,
							commitMessage,
							selectedBranch);
				} else {
					String content =
							binding.fileContent.getText() != null
									? binding.fileContent.getText().toString()
									: "";
					if (content.isEmpty()) {
						Toasty.show(requireContext(), R.string.content_empty);
						return;
					}
					viewModel.createFile(
							requireContext(),
							repoContext.getOwner(),
							repoContext.getName(),
							fileName,
							content,
							commitMessage,
							selectedBranch);
				}
				break;
			case EDIT:
				String content =
						binding.fileContent.getText() != null
								? binding.fileContent.getText().toString()
								: "";
				if (content.isEmpty()) {
					Toasty.show(requireContext(), R.string.content_empty);
					return;
				}
				viewModel.editFile(
						requireContext(),
						repoContext.getOwner(),
						repoContext.getName(),
						filePath,
						content,
						commitMessage,
						selectedBranch,
						fileSha);
				break;
			case DELETE:
				viewModel.deleteFile(
						requireContext(),
						repoContext.getOwner(),
						repoContext.getName(),
						filePath,
						commitMessage,
						selectedBranch,
						fileSha);
				break;
		}
	}

	private String getSubmitButtonText() {
		return switch (fileAction) {
			case CREATE -> getString(R.string.commit_new_file);
			case EDIT -> getString(R.string.commit_changes);
			case DELETE -> getString(R.string.commit_deletion);
		};
	}

	private void observeViewModel() {
		viewModel
				.getIsProcessing()
				.observe(
						getViewLifecycleOwner(),
						isProcessing -> {
							binding.btnSubmit.setEnabled(!isProcessing);
							binding.btnSubmit.setText(isProcessing ? "" : getSubmitButtonText());
							binding.loadingIndicator.setVisibility(
									isProcessing ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getOperationSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (success != null && success) {
								String successMsg = getSuccessMessage();
								Toasty.show(requireContext(), successMsg);
								AppUIStateManager.refreshData();
								if (getActivity() instanceof BaseActivity) {
									((BaseActivity) getActivity()).triggerGlobalRefresh();
								}

								if (pendingCreatePr && pendingPrBranch != null) {
									openPullRequestSheet(pendingPrBranch, pendingPrTitle);
								}

								viewModel.clearOperationSuccess();
								dismiss();
							}
						});

		viewModel
				.getOperationError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								pendingCreatePr = false;
								pendingPrBranch = null;
								pendingPrTitle = null;

								if (error.equals("UNAUTHORIZED")) {
									TokenAuthorizationDialog.authorizationTokenRevokedDialog(
											requireContext());
								} else {
									Toasty.show(requireContext(), error);
								}
								viewModel.clearOperationError();
							}
						});
	}

	private void openPullRequestSheet(String pullFromBranch, String title) {
		pendingCreatePr = false;
		pendingPrBranch = null;
		pendingPrTitle = null;

		BottomSheetCreatePullRequest prSheet =
				BottomSheetCreatePullRequest.newInstance(
						repoContext, null, pullFromBranch, defaultBranch, title, title);

		prSheet.show(getParentFragmentManager(), "CREATE_PR");
	}

	private String getSuccessMessage() {
		return switch (fileAction) {
			case CREATE -> getString(R.string.newFileSuccessMessage);
			case EDIT -> getString(R.string.editFileMessage, selectedBranch);
			case DELETE -> getString(R.string.deleteFileMessage, selectedBranch);
		};
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
