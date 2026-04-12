package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
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
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueTemplate;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateIssueBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.CreateIssueViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateIssue extends BottomSheetDialogFragment {

	private BottomsheetCreateIssueBinding binding;
	private CreateIssueViewModel viewModel;
	private RepositoryContext repoContext;
	private Issue issueToEdit;
	private Set<String> selectedLabels = new HashSet<>();
	private final List<Long> selectedLabelIds = new ArrayList<>();
	private String selectedMilestone = null;
	private Long selectedMilestoneId = null;
	private String selectedDueDate = null;

	public static BottomSheetCreateIssue newInstance(
			RepositoryContext repository, @Nullable Issue issue) {
		BottomSheetCreateIssue fragment = new BottomSheetCreateIssue();
		Bundle args = new Bundle();
		args.putSerializable("repo_context", repository);
		if (issue != null) {
			args.putSerializable("issue_item", issue);
		}
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoContext = (RepositoryContext) getArguments().getSerializable("repo_context");
			issueToEdit = (Issue) getArguments().getSerializable("issue_item");
		}
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(CreateIssueViewModel.class);

		setupUI();
		setupListeners();
		observeViewModel();

		if (issueToEdit == null) {
			viewModel.fetchIssueTemplates(requireContext(), repoContext);
		}

		ViewGroup.LayoutParams editTextParams = binding.issueDescription.getLayoutParams();
		editTextParams.height = (int) (224 * getResources().getDisplayMetrics().density);
		binding.issueDescription.setLayoutParams(editTextParams);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateIssueBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	private void setupUI() {
		binding.cardLabels.cardIcon.setImageResource(R.drawable.ic_label);
		binding.cardLabels.tvCardLabel.setText(R.string.newIssueLabelsTitle);

		binding.cardMilestone.cardIcon.setImageResource(R.drawable.ic_milestone);
		binding.cardMilestone.tvCardLabel.setText(R.string.milestone);

		binding.cardDueDate.cardIcon.setImageResource(R.drawable.ic_calendar);
		binding.cardDueDate.tvCardLabel.setText(R.string.newIssueDueDateTitle);

		if (issueToEdit != null) {
			binding.sheetTitle.setText(R.string.editIssue);
			binding.issueTitle.setText(issueToEdit.getTitle());
			binding.issueDescription.setText(issueToEdit.getBody());
			binding.btnCreate.setText(R.string.update);

			if (issueToEdit.getLabels() != null && !issueToEdit.getLabels().isEmpty()) {
				for (Label label : issueToEdit.getLabels()) {
					selectedLabels.add(label.getName());
					if (label.getId() != null) {
						selectedLabelIds.add(label.getId());
					}
				}
				updateLabelsDisplay();
			}

			if (issueToEdit.getMilestone() != null) {
				selectedMilestone = issueToEdit.getMilestone().getTitle();
				selectedMilestoneId = issueToEdit.getMilestone().getId();
				updateMilestoneDisplay();
			}

			if (issueToEdit.getDueDate() != null) {
				selectedDueDate = formatDateForDisplay(issueToEdit.getDueDate());
				updateDueDateDisplay();
			}
		} else {
			updateLabelsDisplay();
			updateMilestoneDisplay();
			updateDueDateDisplay();
		}

		updateClearButtonVisibility();
		updateMilestoneClearButtonVisibility();
		updateDueDateClearButtonVisibility();
	}

	private void setupListeners() {
		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnExpand.setOnClickListener(v -> openFullScreenEditor());

		binding.cardLabels.getRoot().setOnClickListener(v -> openLabelPicker());

		binding.cardLabels.btnClear.setOnClickListener(
				v -> {
					selectedLabels.clear();
					selectedLabelIds.clear();
					updateLabelsDisplay();
					updateClearButtonVisibility();
				});

		binding.cardMilestone.getRoot().setOnClickListener(v -> openMilestonePicker());
		binding.cardMilestone.btnClear.setOnClickListener(
				v -> {
					selectedMilestone = null;
					selectedMilestoneId = null;
					updateMilestoneDisplay();
					updateMilestoneClearButtonVisibility();
				});

		binding.cardDueDate.getRoot().setOnClickListener(v -> openDatePicker());
		binding.cardDueDate.btnClear.setOnClickListener(
				v -> {
					selectedDueDate = null;
					updateDueDateDisplay();
					updateDueDateClearButtonVisibility();
				});
	}

	private void openFullScreenEditor() {
		BottomSheetFullScreenEditor editorBottomSheet =
				BottomSheetFullScreenEditor.newInstance(
						Objects.requireNonNull(binding.issueDescription.getText()).toString(),
						repoContext,
						false);

		editorBottomSheet.setEditorListener(
				newContent -> {
					binding.issueDescription.setText(newContent);
					binding.issueDescription.setSelection(
							newContent != null ? newContent.length() : 0);
				});

		editorBottomSheet.show(getParentFragmentManager(), "FULLSCREEN_EDITOR");
	}

	private void observeViewModel() {
		viewModel
				.getTemplates()
				.observe(
						getViewLifecycleOwner(),
						templates -> {
							if (templates != null && !templates.isEmpty()) {
								setupTemplateSpinner(templates);
								binding.issueTemplateLayout.setVisibility(View.VISIBLE);
							} else {
								binding.issueTemplateLayout.setVisibility(View.GONE);
							}
						});

		viewModel
				.getTemplatesLoading()
				.observe(
						getViewLifecycleOwner(),
						isLoading -> {
							binding.issueTemplateLayout.setEnabled(!isLoading);
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && !error.isEmpty()) {
								viewModel.clearError();
							}
						});
	}

	private void setupTemplateSpinner(List<IssueTemplate> templates) {
		if (templates == null || templates.isEmpty()) {
			binding.issueTemplateLayout.setVisibility(View.GONE);
			return;
		}

		binding.issueTemplateLayout.setVisibility(View.VISIBLE);

		List<String> templateNames = new ArrayList<>();
		templateNames.add(getString(R.string.none));

		for (IssueTemplate template : templates) {
			templateNames.add(template.getName());
		}

		ArrayAdapter<String> adapter =
				new ArrayAdapter<>(requireContext(), R.layout.list_spinner_items, templateNames);

		binding.issueTemplateSpinner.setAdapter(adapter);

		binding.issueTemplateSpinner.setOnItemClickListener(
				(parent, view, position, id) -> {
					String selectedName = adapter.getItem(position);
					if (selectedName == null) {
						return;
					}

					if (selectedName.equals(getString(R.string.none))) {
						if (issueToEdit == null) {
							binding.issueTitle.setText("");
							binding.issueDescription.setText("");
						}
					} else {
						for (IssueTemplate template : templates) {
							if (template.getName().equals(selectedName)) {
								applyIssueTemplate(template);
								break;
							}
						}
					}
				});
	}

	private void applyIssueTemplate(IssueTemplate template) {
		if (template == null) {
			return;
		}

		if (template.getTitle() != null && !template.getTitle().isEmpty()) {
			binding.issueTitle.setText(template.getTitle().trim());
		} else {
			binding.issueTitle.setText("");
		}

		String templateContent = "";
		if (template.getContent() != null) {
			templateContent = template.getContent();
		} else if (template.getBody() != null) {
			templateContent = template.getBody().toString();
		}

		if (!templateContent.isEmpty()) {
			String trimmedContent = templateContent.trim();
			binding.issueDescription.setText(trimmedContent);

			binding.issueDescription.post(
					() -> {
						int length =
								binding.issueDescription.getText() != null
										? binding.issueDescription.getText().length()
										: 0;
						if (length > 0) {
							binding.issueDescription.setSelection(length);
						}
					});
		} else {
			binding.issueDescription.setText("");
		}
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
			String labelsText = String.join(", ", selectedLabels);
			binding.cardLabels.tvSelectedText.setText(labelsText);
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
			String displayDate = formatDateForDisplay(selectedDueDate);
			binding.cardDueDate.tvSelectedText.setText(displayDate);
		}
	}

	private void updateDueDateClearButtonVisibility() {
		binding.cardDueDate.btnClear.setVisibility(
				selectedDueDate == null || selectedDueDate.isEmpty() ? View.GONE : View.VISIBLE);
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

	// Helpers for API call
	private List<Long> getLabelIdsForApi() {
		return selectedLabelIds;
	}

	private Date getDueDateForApi() {
		if (selectedDueDate == null || selectedDueDate.isEmpty()) {
			return null;
		}
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
	}
}
