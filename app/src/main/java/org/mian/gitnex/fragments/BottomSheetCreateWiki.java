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
import org.gitnex.tea4j.v2.models.WikiPage;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateWikiBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.WikiViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateWiki extends BottomSheetDialogFragment {

	private BottomsheetCreateWikiBinding binding;
	private WikiViewModel viewModel;
	private RepositoryContext repoContext;
	private WikiPage pageToEdit;

	public static BottomSheetCreateWiki newInstance(
			RepositoryContext repository, @Nullable WikiPage page) {
		BottomSheetCreateWiki fragment = new BottomSheetCreateWiki();
		Bundle args = new Bundle();
		args.putSerializable("repo_context", repository);
		if (page != null) {
			args.putSerializable("wiki_page", page);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable("repo_context");
			pageToEdit = (WikiPage) getArguments().getSerializable("wiki_page");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateWikiBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(WikiViewModel.class);

		viewModel.clearCreatedPage();
		viewModel.clearUpdatedPage();

		setupUI();
		setupListeners();
		observeViewModel();
	}

	private void setupUI() {
		binding.wikiContent.setOnTouchListener(
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

		if (pageToEdit != null) {
			binding.sheetTitle.setText(R.string.editWikiPage);
			binding.wikiTitle.setText(pageToEdit.getTitle());
			binding.wikiTitle.setEnabled(false);

			String decodedContent = AppUtil.decodeBase64(pageToEdit.getContentBase64());
			binding.wikiContent.setText(decodedContent);
			binding.wikiContent.setSelection(decodedContent.length());

			binding.btnSubmit.setText(R.string.update);
		} else {
			binding.sheetTitle.setText(R.string.createWikiPage);
			binding.btnSubmit.setText(R.string.newCreateButtonCopy);
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnExpand.setOnClickListener(v -> openFullScreenEditor());
		binding.btnSubmit.setOnClickListener(v -> submitAction());
	}

	private void openFullScreenEditor() {
		BottomSheetFullScreenEditor editorBottomSheet =
				BottomSheetFullScreenEditor.newInstance(
						Objects.requireNonNull(binding.wikiContent.getText()).toString(),
						repoContext,
						true,
						true);

		editorBottomSheet.setEditorListener(
				newContent -> {
					binding.wikiContent.setText(newContent);
					binding.wikiContent.setSelection(newContent != null ? newContent.length() : 0);
				});

		editorBottomSheet.show(getParentFragmentManager(), "FULLSCREEN_EDITOR");
	}

	private void submitAction() {
		String title =
				binding.wikiTitle.getText() != null
						? binding.wikiTitle.getText().toString().trim()
						: "";
		String content =
				binding.wikiContent.getText() != null
						? binding.wikiContent.getText().toString().trim()
						: "";

		if (title.isEmpty() || content.isEmpty()) {
			Toasty.show(requireContext(), R.string.wikiPageNameAndContentError);
			return;
		}

		if (pageToEdit != null) {
			viewModel.updateWikiPage(
					requireContext(),
					repoContext.getOwner(),
					repoContext.getName(),
					pageToEdit.getSubUrl(),
					title,
					content);
		} else {
			viewModel.createWikiPage(
					requireContext(),
					repoContext.getOwner(),
					repoContext.getName(),
					title,
					content);
		}
	}

	private void observeViewModel() {
		viewModel
				.getIsCreating()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							if (pageToEdit == null) {
								binding.loadingIndicator.setVisibility(
										isCreating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isCreating);
								binding.btnSubmit.setText(
										isCreating ? "" : getString(R.string.newCreateButtonCopy));
							}
						});

		viewModel
				.getCreatedPage()
				.observe(
						getViewLifecycleOwner(),
						page -> {
							if (page != null) {
								Toasty.show(requireContext(), R.string.wikiCreated);
								dismiss();
							}
						});

		viewModel
				.getCreateError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								handleError(error);
								viewModel.clearCreateError();
							}
						});

		viewModel
				.getIsUpdating()
				.observe(
						getViewLifecycleOwner(),
						isUpdating -> {
							if (pageToEdit != null) {
								binding.loadingIndicator.setVisibility(
										isUpdating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isUpdating);
								binding.btnSubmit.setText(
										isUpdating ? "" : getString(R.string.update));
							}
						});

		viewModel
				.getUpdatedPage()
				.observe(
						getViewLifecycleOwner(),
						page -> {
							if (page != null) {
								Toasty.show(requireContext(), R.string.wikiUpdated);
								dismiss();
							}
						});

		viewModel
				.getUpdateError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								handleError(error);
								viewModel.clearUpdateError();
							}
						});
	}

	private void handleError(String error) {
		if (error.equals("UNAUTHORIZED")) {
			AlertDialogs.authorizationTokenRevokedDialog(requireContext());
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
