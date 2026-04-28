package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import org.gitnex.tea4j.v2.models.CreatePullRequestOption;
import org.gitnex.tea4j.v2.models.EditPullRequestOption;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.CreateAttachmentsAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.BottomsheetCreatePullRequestBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.helpers.attachments.AttachmentManager;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.AttachmentsViewModel;
import org.mian.gitnex.viewmodels.PullRequestsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreatePullRequest extends BottomSheetDialogFragment {

	private static final String ARG_PREFILL_HEAD = "prefill_head";
	private static final String ARG_PREFILL_BASE = "prefill_base";
	private static final String ARG_PREFILL_TITLE = "prefill_title";
	private static final String ARG_PREFILL_BODY = "prefill_body";

	private BottomsheetCreatePullRequestBinding binding;
	private PullRequestsViewModel viewModel;
	private RepositoryContext repoContext;
	private PullRequest prToEdit;
	private String selectedMergeInto = null;
	private String selectedPullFrom = null;
	private Set<String> selectedLabels = new HashSet<>();
	private final List<Long> selectedLabelIds = new ArrayList<>();
	private String selectedMilestone = null;
	private Long selectedMilestoneId = null;
	private Set<String> selectedAssignees = new HashSet<>();
	private Set<String> selectedReviewers = new HashSet<>();
	private String selectedDueDate = null;
	private int maxAttachmentSize = -1;
	private int maxNumberOfAttachments = -1;
	private AttachmentManager attachmentManager;
	private AttachmentsViewModel attachmentsViewModel;
	protected TinyDB tinyDB;
	private String prefillTitle;
	private String prefillBody;

	public static BottomSheetCreatePullRequest newInstance(
			RepositoryContext repository,
			@Nullable PullRequest pr,
			@Nullable String prefillHead,
			@Nullable String prefillBase,
			@Nullable String prefillTitle,
			@Nullable String prefillBody) {
		BottomSheetCreatePullRequest fragment = new BottomSheetCreatePullRequest();
		Bundle args = new Bundle();
		args.putSerializable("repo_context", repository);
		if (pr != null) {
			args.putSerializable("pr_item", pr);
		}
		if (prefillHead != null) args.putString(ARG_PREFILL_HEAD, prefillHead);
		if (prefillBase != null) args.putString(ARG_PREFILL_BASE, prefillBase);
		if (prefillTitle != null) args.putString(ARG_PREFILL_TITLE, prefillTitle);
		if (prefillBody != null) args.putString(ARG_PREFILL_BODY, prefillBody);
		fragment.setArguments(args);
		return fragment;
	}

	public static BottomSheetCreatePullRequest newInstance(
			RepositoryContext repository, @Nullable PullRequest pr) {
		return newInstance(repository, pr, null, null, null, null);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable("repo_context");
			prToEdit = (PullRequest) getArguments().getSerializable("pr_item");

			selectedPullFrom = getArguments().getString(ARG_PREFILL_HEAD);
			selectedMergeInto = getArguments().getString(ARG_PREFILL_BASE);
			prefillTitle = getArguments().getString(ARG_PREFILL_TITLE);
			prefillBody = getArguments().getString(ARG_PREFILL_BODY);
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreatePullRequestBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(PullRequestsViewModel.class);
		attachmentsViewModel =
				new ViewModelProvider(requireActivity()).get(AttachmentsViewModel.class);

		this.tinyDB = TinyDB.getInstance(requireContext());

		viewModel.clearCreatedPr();
		viewModel.clearUpdatedPr();
		attachmentsViewModel.reset();

		setupUI();
		setupListeners();
		setupAttachments();
		observeViewModel();
		observeAttachmentsViewModel();
	}

	private void setupUI() {
		boolean hasWriteAccess =
				repoContext.getPermissions() != null
						&& repoContext.getPermissions().isPush() != null
						&& repoContext.getPermissions().isPush();

		binding.prBody.setOnTouchListener(
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

		binding.cardMergeInto.cardIcon.setImageResource(R.drawable.ic_branch);
		binding.cardMergeInto.tvCardLabel.setText(R.string.mergeIntoBranch);
		binding.cardMergeInto.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardPullFrom.cardIcon.setImageResource(R.drawable.ic_branch);
		binding.cardPullFrom.tvCardLabel.setText(R.string.pullFromBranch);
		binding.cardPullFrom.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardMilestone.cardIcon.setImageResource(R.drawable.ic_milestone);
		binding.cardMilestone.tvCardLabel.setText(R.string.milestone);
		binding.cardMilestone.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardLabels.cardIcon.setImageResource(R.drawable.ic_label);
		binding.cardLabels.tvCardLabel.setText(R.string.newIssueLabelsTitle);
		binding.cardLabels.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardAssignees.cardIcon.setImageResource(R.drawable.ic_person);
		binding.cardAssignees.tvCardLabel.setText(R.string.newIssueAssigneesListTitle);
		binding.cardAssignees.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardReviewers.cardIcon.setImageResource(R.drawable.ic_followers);
		binding.cardReviewers.tvCardLabel.setText(R.string.reviewers);
		binding.cardReviewers.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		binding.cardDueDate.cardIcon.setImageResource(R.drawable.ic_calendar);
		binding.cardDueDate.tvCardLabel.setText(R.string.newIssueDueDateTitle);
		binding.cardDueDate.getRoot().setVisibility(hasWriteAccess ? View.VISIBLE : View.GONE);

		if (prToEdit != null) {
			binding.sheetTitle.setText(R.string.edit_pr);
			binding.prTitle.setText(prToEdit.getTitle());
			binding.prBody.setText(prToEdit.getBody());
			binding.btnSubmit.setText(R.string.update);

			binding.cardMergeInto.getRoot().setVisibility(View.GONE);
			binding.cardPullFrom.getRoot().setVisibility(View.GONE);
			binding.cardReviewers.getRoot().setVisibility(View.GONE);

			binding.switchAllowMaintainerEdit.setVisibility(View.VISIBLE);

			if (hasWriteAccess) {
				if (prToEdit.getLabels() != null && !prToEdit.getLabels().isEmpty()) {
					for (Label label : prToEdit.getLabels()) {
						selectedLabels.add(label.getName());
						if (label.getId() != null) {
							selectedLabelIds.add(label.getId());
						}
					}
					updateLabelsDisplay();
				}

				if (prToEdit.getMilestone() != null) {
					selectedMilestone = prToEdit.getMilestone().getTitle();
					selectedMilestoneId = prToEdit.getMilestone().getId();
					updateMilestoneDisplay();
				}

				if (prToEdit.getAssignees() != null && !prToEdit.getAssignees().isEmpty()) {
					for (User assignee : prToEdit.getAssignees()) {
						selectedAssignees.add(assignee.getLogin());
					}
					updateAssigneesDisplay();
				}

				if (prToEdit.getDueDate() != null) {
					selectedDueDate = formatDateForDisplay(prToEdit.getDueDate());
					updateDueDateDisplay();
				}

				if (prToEdit.getBase() != null && prToEdit.getBase().getRef() != null) {
					selectedMergeInto = prToEdit.getBase().getRef();
					updateMergeIntoDisplay();
				}
				if (prToEdit.getHead() != null && prToEdit.getHead().getRef() != null) {
					selectedPullFrom = prToEdit.getHead().getRef();
					updatePullFromDisplay();
				}
			}
		} else {
			binding.sheetTitle.setText(R.string.create_pr);
			binding.btnSubmit.setText(R.string.create_pr);
			binding.switchAllowMaintainerEdit.setVisibility(View.GONE);

			if (prefillTitle != null && !prefillTitle.isEmpty()) {
				binding.prTitle.setText(prefillTitle);
			}
			if (prefillBody != null && !prefillBody.isEmpty()) {
				binding.prBody.setText(prefillBody);
			}

			if (hasWriteAccess) {
				updateLabelsDisplay();
				updateMilestoneDisplay();
				updateAssigneesDisplay();
				updateReviewersDisplay();
				updateDueDateDisplay();
				updateMergeIntoDisplay();
				updatePullFromDisplay();
			}
		}

		if (hasWriteAccess) {
			updateClearButtonVisibility();
			updateMilestoneClearButtonVisibility();
			updateAssigneesClearButtonVisibility();
			updateReviewersClearButtonVisibility();
			updateDueDateClearButtonVisibility();
			updateMergeIntoClearButtonVisibility();
			updatePullFromClearButtonVisibility();
		}
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnExpand.setOnClickListener(v -> openFullScreenEditor());
		binding.btnSubmit.setOnClickListener(v -> submitAction());

		boolean hasWriteAccess =
				repoContext.getPermissions() != null
						&& repoContext.getPermissions().isPush() != null
						&& repoContext.getPermissions().isPush();

		binding.cardMergeInto.getRoot().setOnClickListener(v -> openBranchPicker("merge"));
		binding.cardMergeInto.btnClear.setOnClickListener(
				v -> {
					selectedMergeInto = null;
					updateMergeIntoDisplay();
					updateMergeIntoClearButtonVisibility();
				});

		binding.cardPullFrom.getRoot().setOnClickListener(v -> openBranchPicker("pull"));
		binding.cardPullFrom.btnClear.setOnClickListener(
				v -> {
					selectedPullFrom = null;
					updatePullFromDisplay();
					updatePullFromClearButtonVisibility();
				});

		if (hasWriteAccess) {
			binding.cardMilestone.getRoot().setOnClickListener(v -> openMilestonePicker());
			binding.cardMilestone.btnClear.setOnClickListener(
					v -> {
						selectedMilestone = null;
						selectedMilestoneId = null;
						updateMilestoneDisplay();
						updateMilestoneClearButtonVisibility();
					});

			binding.cardLabels.getRoot().setOnClickListener(v -> openLabelPicker());
			binding.cardLabels.btnClear.setOnClickListener(
					v -> {
						selectedLabels.clear();
						selectedLabelIds.clear();
						updateLabelsDisplay();
						updateClearButtonVisibility();
					});

			binding.cardAssignees.getRoot().setOnClickListener(v -> openAssigneesPicker());
			binding.cardAssignees.btnClear.setOnClickListener(
					v -> {
						selectedAssignees.clear();
						updateAssigneesDisplay();
						updateAssigneesClearButtonVisibility();
					});

			binding.cardReviewers.getRoot().setOnClickListener(v -> openReviewersPicker());
			binding.cardReviewers.btnClear.setOnClickListener(
					v -> {
						selectedReviewers.clear();
						updateReviewersDisplay();
						updateReviewersClearButtonVisibility();
					});

			binding.cardDueDate.getRoot().setOnClickListener(v -> openDatePicker());
			binding.cardDueDate.btnClear.setOnClickListener(
					v -> {
						selectedDueDate = null;
						updateDueDateDisplay();
						updateDueDateClearButtonVisibility();
					});
		}

		binding.cardAttachments.btnAddAttachment.setOnClickListener(
				v -> {
					if (attachmentManager != null) {
						attachmentManager.openFilePicker();
					}
				});
	}

	private void openFullScreenEditor() {
		BottomSheetFullScreenEditor editorBottomSheet =
				BottomSheetFullScreenEditor.newInstance(
						Objects.requireNonNull(binding.prBody.getText()).toString(),
						repoContext,
						true,
						true);

		editorBottomSheet.setEditorListener(
				newContent -> {
					binding.prBody.setText(newContent);
					binding.prBody.setSelection(newContent != null ? newContent.length() : 0);
				});

		editorBottomSheet.show(getParentFragmentManager(), "FULLSCREEN_EDITOR");
	}

	private void submitAction() {
		String title =
				binding.prTitle.getText() != null
						? binding.prTitle.getText().toString().trim()
						: "";
		String body =
				binding.prBody.getText() != null ? binding.prBody.getText().toString().trim() : "";

		if (title.isEmpty()) {
			Toasty.show(requireContext(), R.string.titleError);
			return;
		}

		if (selectedMergeInto == null || selectedMergeInto.isEmpty()) {
			Toasty.show(requireContext(), R.string.mergeIntoError);
			return;
		}

		if (selectedPullFrom == null || selectedPullFrom.isEmpty()) {
			Toasty.show(requireContext(), R.string.pullFromError);
			return;
		}

		if (selectedMergeInto.equals(selectedPullFrom)) {
			Toasty.show(requireContext(), R.string.sameBranchesError);
			return;
		}

		if (isCurrentUserInReviewers()) {
			Toasty.show(requireContext(), R.string.cannotAddSelfAsReviewer);
			return;
		}

		if (prToEdit != null) {
			submitUpdatePr(title, body);
		} else {
			submitCreatePr(title, body);
		}
	}

	private boolean isCurrentUserInReviewers() {
		if (selectedReviewers.isEmpty()) {
			return false;
		}

		UserAccountsApi userAccountsApi =
				BaseApi.getInstance(requireContext(), UserAccountsApi.class);
		if (userAccountsApi != null) {
			int currentAccountId = tinyDB.getInt("currentActiveAccountId", -1);
			UserAccount currentAccount = userAccountsApi.getAccountById(currentAccountId);
			if (currentAccount != null && currentAccount.getUserName() != null) {
				return selectedReviewers.contains(currentAccount.getUserName());
			}
		}
		return false;
	}

	private void submitCreatePr(String title, String body) {
		CreatePullRequestOption prData = new CreatePullRequestOption();
		prData.setTitle(title);
		prData.setBody(body);
		prData.setBase(selectedMergeInto);
		prData.setHead(selectedPullFrom);

		if (selectedMilestoneId != null) {
			prData.setMilestone(selectedMilestoneId);
		}

		if (!selectedLabelIds.isEmpty()) {
			prData.setLabels(new ArrayList<>(selectedLabelIds));
		}

		if (!selectedAssignees.isEmpty()) {
			prData.setAssignees(new ArrayList<>(selectedAssignees));
		}

		if (!selectedReviewers.isEmpty()) {
			prData.setReviewers(new ArrayList<>(selectedReviewers));
		}

		Date dueDate = getDueDateForApi();
		if (dueDate != null) {
			prData.setDueDate(dueDate);
		}

		viewModel.createPullRequest(
				requireContext(), repoContext.getOwner(), repoContext.getName(), prData);
	}

	private void submitUpdatePr(String title, String body) {
		EditPullRequestOption prData = new EditPullRequestOption();
		prData.setTitle(title);
		prData.setBody(body);
		prData.setAllowMaintainerEdit(binding.switchAllowMaintainerEdit.isChecked());

		if (selectedMilestoneId != null) {
			prData.setMilestone(selectedMilestoneId);
		}

		if (!selectedLabelIds.isEmpty()) {
			prData.setLabels(new ArrayList<>(selectedLabelIds));
		}

		if (!selectedAssignees.isEmpty()) {
			prData.setAssignees(new ArrayList<>(selectedAssignees));
		}

		Date dueDate = getDueDateForApi();
		if (dueDate != null) {
			prData.setDueDate(dueDate);
		}

		viewModel.updatePullRequest(
				requireContext(),
				repoContext.getOwner(),
				repoContext.getName(),
				prToEdit.getNumber(),
				prData);
	}

	private void observeViewModel() {
		viewModel
				.getIsCreating()
				.observe(
						getViewLifecycleOwner(),
						isCreating -> {
							if (prToEdit == null) {
								binding.loadingIndicator.setVisibility(
										isCreating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isCreating);
								binding.btnSubmit.setText(
										isCreating ? "" : getString(R.string.create_pr));
							}
						});

		viewModel
				.getCreatedPr()
				.observe(
						getViewLifecycleOwner(),
						pr -> {
							if (pr != null) {
								handlePrSuccess(pr.getNumber());
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
							if (prToEdit != null) {
								binding.loadingIndicator.setVisibility(
										isUpdating ? View.VISIBLE : View.GONE);
								binding.btnSubmit.setEnabled(!isUpdating);
								binding.btnSubmit.setText(
										isUpdating ? "" : getString(R.string.update));
							}
						});

		viewModel
				.getUpdatedPr()
				.observe(
						getViewLifecycleOwner(),
						pr -> {
							if (pr != null) {
								handlePrSuccess(pr.getNumber());
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
			TokenAuthorizationDialog.authorizationTokenRevokedDialog(requireContext());
		} else {
			Toasty.show(requireContext(), error);
		}
	}

	private void openBranchPicker(String type) {
		BottomsheetBranchPicker branchPicker =
				BottomsheetBranchPicker.newInstance(
						repoContext.getOwner(),
						repoContext.getName(),
						type.equals("merge") ? selectedMergeInto : selectedPullFrom);

		branchPicker.setOnBranchSelectedListener(
				branchName -> {
					if (type.equals("merge")) {
						selectedMergeInto = branchName;
						updateMergeIntoDisplay();
						updateMergeIntoClearButtonVisibility();
					} else {
						selectedPullFrom = branchName;
						updatePullFromDisplay();
						updatePullFromClearButtonVisibility();
					}
				});

		branchPicker.show(getParentFragmentManager(), "BRANCH_PICKER_" + type.toUpperCase());
	}

	private void updateMergeIntoDisplay() {
		if (selectedMergeInto == null || selectedMergeInto.isEmpty()) {
			binding.cardMergeInto.tvSelectedText.setText(R.string.add_release_branch);
		} else {
			binding.cardMergeInto.tvSelectedText.setText(selectedMergeInto);
		}
	}

	private void updateMergeIntoClearButtonVisibility() {
		binding.cardMergeInto.btnClear.setVisibility(
				selectedMergeInto == null || selectedMergeInto.isEmpty()
						? View.GONE
						: View.VISIBLE);
	}

	private void updatePullFromDisplay() {
		if (selectedPullFrom == null || selectedPullFrom.isEmpty()) {
			binding.cardPullFrom.tvSelectedText.setText(R.string.add_release_branch);
		} else {
			binding.cardPullFrom.tvSelectedText.setText(selectedPullFrom);
		}
	}

	private void updatePullFromClearButtonVisibility() {
		binding.cardPullFrom.btnClear.setVisibility(
				selectedPullFrom == null || selectedPullFrom.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void openLabelPicker() {
		BottomSheetLabelPicker labelPicker =
				BottomSheetLabelPicker.newInstance(repoContext, new ArrayList<>(selectedLabels));
		labelPicker.setOnLabelsSelectedWithIdsListener(
				(selected, labelIds) -> {
					selectedLabels = selected;
					selectedLabelIds.clear();
					selectedLabelIds.addAll(labelIds.values());
					updateLabelsDisplay();
					updateClearButtonVisibility();
				});
		labelPicker.show(getParentFragmentManager(), "LABEL_PICKER");
	}

	private void updateLabelsDisplay() {
		if (selectedLabels.isEmpty()) {
			binding.cardLabels.tvSelectedText.setText(R.string.add_labels);
		} else {
			binding.cardLabels.tvSelectedText.setText(String.join(", ", selectedLabels));
		}
	}

	private void updateClearButtonVisibility() {
		binding.cardLabels.btnClear.setVisibility(
				selectedLabels.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void openMilestonePicker() {
		List<String> current =
				selectedMilestone != null
						? Collections.singletonList(selectedMilestone)
						: new ArrayList<>();
		BottomSheetMilestonePicker milestonePicker =
				BottomSheetMilestonePicker.newInstance(repoContext, current);
		milestonePicker.setOnMilestonesSelectedWithIdsListener(
				(selected, milestoneIds) -> {
					if (selected.isEmpty()) {
						selectedMilestone = null;
						selectedMilestoneId = null;
					} else {
						selectedMilestone = selected.iterator().next();
						selectedMilestoneId = milestoneIds.get(selectedMilestone);
					}
					updateMilestoneDisplay();
					updateMilestoneClearButtonVisibility();
				});
		milestonePicker.show(getParentFragmentManager(), "MILESTONE_PICKER");
	}

	private void updateMilestoneDisplay() {
		if (selectedMilestone == null || selectedMilestone.isEmpty()) {
			binding.cardMilestone.tvSelectedText.setText(R.string.add_milestone);
		} else {
			binding.cardMilestone.tvSelectedText.setText(selectedMilestone);
		}
	}

	private void updateMilestoneClearButtonVisibility() {
		binding.cardMilestone.btnClear.setVisibility(
				selectedMilestone == null || selectedMilestone.isEmpty()
						? View.GONE
						: View.VISIBLE);
	}

	private void openAssigneesPicker() {
		BottomSheetAssigneesPicker assigneesPicker =
				BottomSheetAssigneesPicker.newInstance(
						repoContext, new ArrayList<>(selectedAssignees));
		assigneesPicker.setOnAssigneesSelectedListener(
				selected -> {
					selectedAssignees = selected;
					updateAssigneesDisplay();
					updateAssigneesClearButtonVisibility();
				});
		assigneesPicker.show(getParentFragmentManager(), "ASSIGNEES_PICKER");
	}

	private void updateAssigneesDisplay() {
		if (selectedAssignees.isEmpty()) {
			binding.cardAssignees.tvSelectedText.setText(R.string.add_assignees);
		} else {
			binding.cardAssignees.tvSelectedText.setText(String.join(", ", selectedAssignees));
		}
	}

	private void updateAssigneesClearButtonVisibility() {
		binding.cardAssignees.btnClear.setVisibility(
				selectedAssignees.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void openReviewersPicker() {
		String currentUser = getCurrentUserName();

		BottomSheetAssigneesPicker reviewersPicker =
				BottomSheetAssigneesPicker.newInstance(
						repoContext, new ArrayList<>(selectedReviewers), currentUser);
		reviewersPicker.setOnAssigneesSelectedListener(
				selected -> {
					selectedReviewers = selected;
					updateReviewersDisplay();
					updateReviewersClearButtonVisibility();
				});
		reviewersPicker.show(getParentFragmentManager(), "REVIEWERS_PICKER");
	}

	private String getCurrentUserName() {
		UserAccountsApi userAccountsApi =
				BaseApi.getInstance(requireContext(), UserAccountsApi.class);
		if (userAccountsApi != null) {
			int currentAccountId = tinyDB.getInt("currentActiveAccountId", -1);
			UserAccount currentAccount = userAccountsApi.getAccountById(currentAccountId);
			if (currentAccount != null) {
				return currentAccount.getUserName();
			}
		}
		return null;
	}

	private void updateReviewersDisplay() {
		if (selectedReviewers.isEmpty()) {
			binding.cardReviewers.tvSelectedText.setText(R.string.add_reviewers);
		} else {
			binding.cardReviewers.tvSelectedText.setText(String.join(", ", selectedReviewers));
		}
	}

	private void updateReviewersClearButtonVisibility() {
		binding.cardReviewers.btnClear.setVisibility(
				selectedReviewers.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void openDatePicker() {
		MaterialDatePicker<Long> datePicker = getLongMaterialDatePicker();
		datePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					String[] locale_ =
							AppDatabaseSettings.getSettingsValue(
											requireContext(), AppDatabaseSettings.APP_LOCALE_KEY)
									.split("\\|");
					SimpleDateFormat format =
							new SimpleDateFormat("yyyy-MM-dd", new Locale(locale_[1]));
					selectedDueDate = format.format(calendar.getTime());
					updateDueDateDisplay();
					updateDueDateClearButtonVisibility();
				});
		datePicker.show(getParentFragmentManager(), "DATE_PICKER");
	}

	@NonNull private MaterialDatePicker<Long> getLongMaterialDatePicker() {
		MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
		builder.setTitleText(R.string.newIssueDueDateTitle);
		if (selectedDueDate != null) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
				Date date = sdf.parse(selectedDueDate);
				if (date != null) {
					builder.setSelection(date.getTime());
				}
			} catch (ParseException e) {
				builder.setSelection(Calendar.getInstance().getTimeInMillis());
			}
		} else {
			builder.setSelection(Calendar.getInstance().getTimeInMillis());
		}
		return builder.build();
	}

	private void updateDueDateDisplay() {
		if (selectedDueDate == null || selectedDueDate.isEmpty()) {
			binding.cardDueDate.tvSelectedText.setText(R.string.add_due_date);
		} else {
			binding.cardDueDate.tvSelectedText.setText(formatDateForDisplay(selectedDueDate));
		}
	}

	private void updateDueDateClearButtonVisibility() {
		binding.cardDueDate.btnClear.setVisibility(
				selectedDueDate == null || selectedDueDate.isEmpty() ? View.GONE : View.VISIBLE);
	}

	private void loadAttachmentLimits() {
		UserAccountsApi userAccountsApi =
				BaseApi.getInstance(requireContext(), UserAccountsApi.class);
		if (userAccountsApi != null) {
			UserAccount userAccount =
					userAccountsApi.getAccountById(tinyDB.getInt("currentActiveAccountId", -1));
			if (userAccount != null) {
				maxAttachmentSize = userAccount.getMaxAttachmentsSize();
				maxNumberOfAttachments = userAccount.getMaxNumberOfAttachments();
			}
		}
	}

	private void setupAttachments() {
		loadAttachmentLimits();

		if (maxNumberOfAttachments == 0) {
			binding.cardAttachments.getRoot().setVisibility(View.GONE);
			return;
		}

		binding.cardAttachments.getRoot().setVisibility(View.VISIBLE);
		attachmentManager = new AttachmentManager(requireContext());

		if (maxAttachmentSize > 0) {
			attachmentManager.setMaxFileSize((long) maxAttachmentSize * 1024 * 1024);
		}
		if (maxNumberOfAttachments > 0) {
			attachmentManager.setMaxFileCount(maxNumberOfAttachments);
		}

		attachmentManager.setListener(
				new AttachmentManager.AttachmentListener() {
					@SuppressLint("SetTextI18n")
					@Override
					public void onAttachmentsChanged(int count) {
						binding.cardAttachments.attachmentCount.setText("(" + count + ")");
						updateAttachmentsEmptyState();
					}

					@Override
					public void onAttachmentAdded(Uri uri) {
						attachmentsViewModel.addPendingUpload(uri);
					}

					@Override
					public void onAttachmentRemoved(int position) {
						attachmentsViewModel.clearPendingUploads();
						for (Uri uri : attachmentManager.getPendingUris()) {
							attachmentsViewModel.addPendingUpload(uri);
						}
					}

					@Override
					public void onAttachmentRejected(String reason) {
						Toasty.show(requireContext(), reason);
					}
				});

		ActivityResultLauncher<Intent> filePickerLauncher =
				registerForActivityResult(
						new ActivityResultContracts.StartActivityForResult(),
						result -> {
							if (result.getResultCode() == Activity.RESULT_OK
									&& result.getData() != null) {
								Uri uri = result.getData().getData();
								if (uri != null) {
									attachmentManager.handleFilePickerResult(uri);
								}
								ClipData clipData = result.getData().getClipData();
								if (clipData != null) {
									for (int i = 0; i < clipData.getItemCount(); i++) {
										Uri clipUri = clipData.getItemAt(i).getUri();
										if (clipUri != null) {
											attachmentManager.handleFilePickerResult(clipUri);
										}
									}
								}
							}
						});

		attachmentManager.registerFilePicker(filePickerLauncher);

		CreateAttachmentsAdapter attachmentsAdapter = attachmentManager.createAdapter();
		binding.cardAttachments.attachmentsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.cardAttachments.attachmentsRecyclerView.setAdapter(attachmentsAdapter);

		updateAttachmentsEmptyState();
	}

	private void updateAttachmentsEmptyState() {
		boolean hasAttachments =
				attachmentManager != null && attachmentManager.getAttachmentCount() > 0;
		binding.cardAttachments.attachmentEmptyState.setVisibility(
				hasAttachments ? View.GONE : View.VISIBLE);
		binding.cardAttachments.attachmentsRecyclerView.setVisibility(
				hasAttachments ? View.VISIBLE : View.GONE);
	}

	private void observeAttachmentsViewModel() {
		attachmentsViewModel
				.getIsUploading()
				.observe(
						getViewLifecycleOwner(),
						isUploading -> {
							if (!isUploading) {
								binding.btnSubmit.setEnabled(true);
							}
						});

		attachmentsViewModel
				.getUploadError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(requireContext(), R.string.attachmentsSaveError);
								attachmentsViewModel.clearError();
							}
						});

		attachmentsViewModel
				.getUploadComplete()
				.observe(
						getViewLifecycleOwner(),
						complete -> {
							if (complete != null && complete) {
								String successMsg =
										getString(
												prToEdit != null
														? R.string.updatePrSuccess
														: R.string.prCreateSuccess);
								Toasty.show(requireContext(), successMsg);
								dismiss();
							}
						});
	}

	private void handlePrSuccess(long prNumber) {
		if (attachmentManager != null && attachmentManager.getAttachmentCount() > 0) {
			attachmentsViewModel.uploadAttachments(
					requireContext(), repoContext.getOwner(), repoContext.getName(), prNumber);
		} else {
			String successMsg =
					getString(
							prToEdit != null ? R.string.updatePrSuccess : R.string.prCreateSuccess);
			Toasty.show(requireContext(), successMsg);
			AppUIStateManager.refreshData();
			if (getActivity() instanceof BaseActivity) {
				((BaseActivity) getActivity()).triggerGlobalRefresh();
			}
			dismiss();
		}
	}

	private String formatDateForDisplay(Date date) {
		if (date == null) return "";
		SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
		return displayFormat.format(date);
	}

	private String formatDateForDisplay(String dateString) {
		if (dateString == null || dateString.isEmpty()) return "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			Date date = sdf.parse(dateString);
			return formatDateForDisplay(date);
		} catch (ParseException e) {
			return dateString;
		}
	}

	private Date getDueDateForApi() {
		if (selectedDueDate == null || selectedDueDate.isEmpty()) return null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
			return sdf.parse(selectedDueDate);
		} catch (ParseException e) {
			return null;
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
		if (attachmentManager != null) {
			attachmentManager.clear();
		}
	}
}
