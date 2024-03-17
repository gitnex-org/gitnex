package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.CreateIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.Milestone;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.AssigneesActions;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.adapters.AttachmentsAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateIssueBinding;
import org.mian.gitnex.databinding.BottomSheetAttachmentsBinding;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.attachments.AttachmentUtils;
import org.mian.gitnex.helpers.attachments.AttachmentsModel;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateIssueActivity extends BaseActivity
		implements LabelsListAdapter.LabelsListAdapterListener,
				AssigneesListAdapter.AssigneesListAdapterListener,
				AttachmentsAdapter.AttachmentsReceiverListener {

	private final List<Label> labelsList = new ArrayList<>();
	private final LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	private final List<User> assigneesList = new ArrayList<>();
	private ActivityCreateIssueBinding viewBinding;
	private int milestoneId;
	private RepositoryContext repository;
	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private List<Integer> labelsIds = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();
	private boolean renderMd = false;
	private RepositoryContext repositoryContext;
	private static List<AttachmentsModel> attachmentsList;
	private AttachmentsAdapter attachmentsAdapter;
	private static final List<Uri> contentUri = new ArrayList<>();

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityCreateIssueBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repositoryContext = RepositoryContext.fromIntent(getIntent());

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		repository = RepositoryContext.fromIntent(getIntent());

		int resultLimit = Constants.getCurrentResultLimit(ctx);

		attachmentsList = new ArrayList<>();
		attachmentsAdapter = new AttachmentsAdapter(attachmentsList, ctx);

		AttachmentsAdapter.setAttachmentsReceiveListener(this);

		viewBinding.newIssueDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		labelsAdapter = new LabelsListAdapter(labelsList, CreateIssueActivity.this, labelsIds);
		assigneesAdapter =
				new AssigneesListAdapter(
						ctx, assigneesList, CreateIssueActivity.this, assigneesListData);

		showDatePickerDialog();

		viewBinding.newIssueDueDateLayout.setEndIconOnClickListener(
				view -> viewBinding.newIssueDueDate.setText(""));

		viewBinding.topAppBar.setNavigationOnClickListener(
				v -> {
					finish();
					contentUri.clear();
				});

		viewBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.markdown) {

						if (!renderMd) {
							Markdown.render(
									ctx,
									EmojiParser.parseToUnicode(
											Objects.requireNonNull(
															viewBinding.newIssueDescription
																	.getText())
													.toString()),
									viewBinding.markdownPreview,
									repositoryContext);

							viewBinding.markdownPreview.setVisibility(View.VISIBLE);
							viewBinding.newIssueDescriptionLayout.setVisibility(View.GONE);
							renderMd = true;
						} else {
							viewBinding.markdownPreview.setVisibility(View.GONE);
							viewBinding.newIssueDescriptionLayout.setVisibility(View.VISIBLE);
							renderMd = false;
						}

						return true;
					} else if (id == R.id.create) {
						processNewIssue();
						return true;
					} else if (id == R.id.attachment) {
						checkForAttachments();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});

		getMilestones(repository.getOwner(), repository.getName(), resultLimit);

		viewBinding.newIssueLabels.setOnClickListener(newIssueLabels -> showLabels());

		viewBinding.newIssueAssigneesList.setOnClickListener(
				newIssueAssigneesList -> showAssignees());

		if (!repository.getPermissions().isPush()) {
			viewBinding.newIssueAssigneesListLayout.setVisibility(View.GONE);
			viewBinding.newIssueMilestoneSpinnerLayout.setVisibility(View.GONE);
			viewBinding.newIssueLabelsLayout.setVisibility(View.GONE);
			viewBinding.newIssueDueDateLayout.setVisibility(View.GONE);
		}
	}

	ActivityResultLauncher<Intent> startActivityForResult =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							Intent data = result.getData();
							assert data != null;
							contentUri.add(data.getData());
							attachmentsList.add(
									new AttachmentsModel(
											AttachmentUtils.queryName(ctx, data.getData()),
											data.getData()));
							attachmentsAdapter.updateList(attachmentsList);
						}
					});

	public void onDestroy() {
		AttachmentsAdapter.setAttachmentsReceiveListener(null);
		super.onDestroy();
	}

	@Override
	public void setAttachmentsData(Uri filename) {
		contentUri.remove(filename);
	}

	private void checkForAttachments() {

		if (contentUri.size() > 0) {

			BottomSheetAttachmentsBinding bottomSheetAttachmentsBinding =
					BottomSheetAttachmentsBinding.inflate(getLayoutInflater());

			BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(ctx);

			bottomSheetAttachmentsBinding.addAttachment.setOnClickListener(
					v1 -> openFileAttachmentActivity());

			bottomSheetAttachmentsBinding.recyclerViewAttachments.setHasFixedSize(true);
			bottomSheetAttachmentsBinding.recyclerViewAttachments.setLayoutManager(
					new LinearLayoutManager(ctx));
			bottomSheetAttachmentsBinding.recyclerViewAttachments.setAdapter(attachmentsAdapter);

			bottomSheetDialog.setContentView(bottomSheetAttachmentsBinding.getRoot());
			bottomSheetDialog.show();
		} else {
			openFileAttachmentActivity();
		}
	}

	private void openFileAttachmentActivity() {

		Intent data = new Intent(Intent.ACTION_GET_CONTENT);
		data.addCategory(Intent.CATEGORY_OPENABLE);
		data.setType("*/*");
		Intent intent = Intent.createChooser(data, "Choose a file");
		startActivityForResult.launch(intent);
	}

	private void processAttachments(long issueIndex) {

		for (int i = 0; i < contentUri.size(); i++) {

			File file = AttachmentUtils.getFile(ctx, contentUri.get(i));

			RequestBody requestFile =
					RequestBody.create(
							file,
							MediaType.parse(
									Objects.requireNonNull(
											getContentResolver().getType(contentUri.get(i)))));

			uploadAttachments(requestFile, issueIndex, file.getName());
		}
	}

	private void uploadAttachments(RequestBody requestFile, long issueIndex, String filename1) {

		Call<Attachment> call3 =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateIssueAttachment(
								requestFile,
								repository.getOwner(),
								repository.getName(),
								issueIndex,
								filename1);

		call3.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Attachment> call,
							@NonNull retrofit2.Response<Attachment> response2) {

						if (response2.code() == 201) {
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response2.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.attachmentsSaveError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Attachment> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
	}

	private void showDatePickerDialog() {

		MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
		builder.setSelection(Calendar.getInstance().getTimeInMillis());
		builder.setTitleText(R.string.newIssueDueDateTitle);
		MaterialDatePicker<Long> materialDatePicker = builder.build();

		String[] locale_ =
				AppDatabaseSettings.getSettingsValue(ctx, AppDatabaseSettings.APP_LOCALE_KEY)
						.split("\\|");

		viewBinding.newIssueDueDate.setOnClickListener(
				v -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

		materialDatePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					SimpleDateFormat format =
							new SimpleDateFormat("yyyy-MM-dd", new Locale(locale_[1]));
					String formattedDate = format.format(calendar.getTime());
					viewBinding.newIssueDueDate.setText(formattedDate);
				});
	}

	@Override
	public void assigneesInterface(List<String> data) {

		String assigneesSetter = String.valueOf(data);
		viewBinding.newIssueAssigneesList.setText(
				assigneesSetter.replace("]", "").replace("[", ""));
		assigneesListData = data;
	}

	@Override
	public void labelsInterface(List<String> data) {

		String labelsSetter = String.valueOf(data);
		viewBinding.newIssueLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	private void showAssignees() {

		viewBinding.progressBar.setVisibility(View.VISIBLE);

		CustomAssigneesSelectionDialogBinding assigneesBinding =
				CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = assigneesBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setNeutralButton(R.string.close, null);

		AssigneesActions.getRepositoryAssignees(
				ctx,
				repository.getOwner(),
				repository.getName(),
				assigneesList,
				materialAlertDialogBuilder,
				assigneesAdapter,
				assigneesBinding,
				viewBinding.progressBar);
	}

	private void showLabels() {

		viewBinding.progressBar.setVisibility(View.VISIBLE);

		CustomLabelsSelectionDialogBinding labelsBinding =
				CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = labelsBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setNeutralButton(R.string.close, null);

		LabelsActions.getRepositoryLabels(
				ctx,
				repository.getOwner(),
				repository.getName(),
				labelsList,
				materialAlertDialogBuilder,
				labelsAdapter,
				labelsBinding,
				viewBinding.progressBar);
	}

	private void processNewIssue() {

		String newIssueTitleForm =
				Objects.requireNonNull(viewBinding.newIssueTitle.getText()).toString();
		String newIssueDescriptionForm =
				Objects.requireNonNull(viewBinding.newIssueDescription.getText()).toString();
		String newIssueDueDateForm =
				Objects.requireNonNull(viewBinding.newIssueDueDate.getText()).toString();

		if (newIssueTitleForm.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.issueTitleEmpty));
			return;
		}

		createNewIssueFunc(
				repository.getOwner(),
				repository.getName(),
				newIssueDescriptionForm,
				milestoneId,
				newIssueTitleForm,
				newIssueDueDateForm);
	}

	private void createNewIssueFunc(
			String repoOwner,
			String repoName,
			String newIssueDescriptionForm,
			int newIssueMilestoneIdForm,
			String newIssueTitleForm,
			String newIssueDueDateForm) {

		ArrayList<Long> labelIds = new ArrayList<>();
		for (Integer i : labelsIds) {
			labelIds.add((long) i);
		}

		CreateIssueOption createNewIssueJson = new CreateIssueOption();
		createNewIssueJson.setBody(newIssueDescriptionForm);
		createNewIssueJson.setMilestone((long) newIssueMilestoneIdForm);
		String[] date = newIssueDueDateForm.split("-");
		if (!newIssueDueDateForm.equalsIgnoreCase("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
			calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
			calendar.set(Calendar.DATE, Integer.parseInt(date[2]));
			Date dueDate = calendar.getTime();
			createNewIssueJson.setDueDate(dueDate);
		}
		createNewIssueJson.setTitle(newIssueTitleForm);
		createNewIssueJson.setAssignees(assigneesListData);
		createNewIssueJson.setLabels(labelIds);

		Call<Issue> call3 =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateIssue(repoOwner, repoName, createNewIssueJson);

		call3.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response2) {

						if (response2.code() == 201) {

							IssuesFragment.resumeIssues = true;
							RepoDetailActivity.updateRepo = true;
							MainActivity.reloadRepos = true;

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.issueCreated));

							assert response2.body() != null;

							if (contentUri.size() > 0) {
								processAttachments(response2.body().getNumber());
								contentUri.clear();
							} else {
								new Handler().postDelayed(() -> finish(), 3000);
							}

						} else if (response2.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
	}

	private void getMilestones(String repoOwner, String repoName, int resultLimit) {

		String msState = "open";
		Call<List<Milestone>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetMilestonesList(repoOwner, repoName, msState, null, 1, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestone>> call,
							@NonNull retrofit2.Response<List<Milestone>> response) {

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								List<Milestone> milestonesList_ = response.body();

								Milestone ms = new Milestone();
								ms.setId(0L);
								ms.setTitle(getString(R.string.issueCreatedNoMilestone));
								milestonesList.put(ms.getTitle(), ms);
								assert milestonesList_ != null;

								if (milestonesList_.size() > 0) {

									for (Milestone milestone : milestonesList_) {

										// Don't translate "open" is a enum
										if (milestone.getState().equals("open")) {
											milestonesList.put(milestone.getTitle(), milestone);
										}
									}
								}

								ArrayAdapter<String> adapter =
										new ArrayAdapter<>(
												CreateIssueActivity.this,
												R.layout.list_spinner_items,
												new ArrayList<>(milestonesList.keySet()));

								viewBinding.newIssueMilestoneSpinner.setAdapter(adapter);
								// enableProcessButton();

								viewBinding.newIssueMilestoneSpinner.setOnItemClickListener(
										(parent, view, position, id) -> {
											if (position == 0) {
												milestoneId = 0;
											} else if (view instanceof TextView) {
												milestoneId =
														Math.toIntExact(
																Objects.requireNonNull(
																				milestonesList.get(
																						((TextView)
																										view)
																								.getText()
																								.toString()))
																		.getId());
											}
										});
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
