package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreatePullRequestOption;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.Milestone;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AttachmentsAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.databinding.BottomSheetAttachmentsBinding;
import org.mian.gitnex.databinding.CustomInsertNoteBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.attachments.AttachmentUtils;
import org.mian.gitnex.helpers.attachments.AttachmentsModel;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreatePullRequestActivity extends BaseActivity
		implements LabelsListAdapter.LabelsListAdapterListener,
				AttachmentsAdapter.AttachmentsReceiverListener {

	private final List<String> assignees = new ArrayList<>();
	LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	List<String> branchesList = new ArrayList<>();
	List<Label> labelsList = new ArrayList<>();
	private ActivityCreatePrBinding viewBinding;
	private List<Integer> labelsIds = new ArrayList<>();
	private int milestoneId;
	private RepositoryContext repository;
	private LabelsListAdapter labelsAdapter;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private MaterialAlertDialogBuilder materialAlertDialogBuilderNotes;
	private boolean renderMd = false;
	private RepositoryContext repositoryContext;
	private static List<AttachmentsModel> attachmentsList;
	private AttachmentsAdapter attachmentsAdapter;
	private static final List<Uri> contentUri = new ArrayList<>();
	private CustomInsertNoteBinding customInsertNoteBinding;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private List<Notes> notesList;
	public AlertDialog dialogNotes;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityCreatePrBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repositoryContext = RepositoryContext.fromIntent(getIntent());

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);
		materialAlertDialogBuilderNotes =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		repository = RepositoryContext.fromIntent(getIntent());

		attachmentsList = new ArrayList<>();
		attachmentsAdapter = new AttachmentsAdapter(attachmentsList, ctx);

		AttachmentsAdapter.setAttachmentsReceiveListener(this);

		int resultLimit = Constants.getCurrentResultLimit(ctx);

		viewBinding.prBody.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		labelsAdapter =
				new LabelsListAdapter(labelsList, CreatePullRequestActivity.this, labelsIds);

		showDatePickerDialog();

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
											Objects.requireNonNull(viewBinding.prBody.getText())
													.toString()),
									viewBinding.markdownPreview,
									repositoryContext);

							viewBinding.markdownPreview.setVisibility(View.VISIBLE);
							viewBinding.prBodyLayout.setVisibility(View.GONE);
							renderMd = true;
						} else {
							viewBinding.markdownPreview.setVisibility(View.GONE);
							viewBinding.prBodyLayout.setVisibility(View.VISIBLE);
							renderMd = false;
						}

						return true;
					} else if (id == R.id.create) {
						processPullRequest();
						return true;
					} else if (id == R.id.attachment) {
						checkForAttachments();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});

		viewBinding.insertNote.setOnClickListener(insertNote -> showAllNotes());

		getMilestones(repository.getOwner(), repository.getName(), resultLimit);
		getBranches(repository.getOwner(), repository.getName());

		viewBinding.prLabels.setOnClickListener(prLabels -> showLabels());

		if (!repository.getPermissions().isPush()) {
			viewBinding.prDueDateLayout.setVisibility(View.GONE);
			viewBinding.prLabelsLayout.setVisibility(View.GONE);
			viewBinding.milestonesSpinnerLayout.setVisibility(View.GONE);
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

	private void showAllNotes() {

		notesList = new ArrayList<>();
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		customInsertNoteBinding = CustomInsertNoteBinding.inflate(LayoutInflater.from(ctx));

		View view = customInsertNoteBinding.getRoot();
		materialAlertDialogBuilderNotes.setView(view);

		customInsertNoteBinding.recyclerView.setHasFixedSize(true);
		customInsertNoteBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new NotesAdapter(ctx, notesList, "insert", "pr");

		customInsertNoteBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											notesList.clear();
											customInsertNoteBinding.pullToRefresh.setRefreshing(
													false);
											customInsertNoteBinding.progressBar.setVisibility(
													View.VISIBLE);
											fetchNotes();
										},
										250));

		if (notesApi.getCount() > 0) {
			fetchNotes();
			dialogNotes = materialAlertDialogBuilderNotes.show();
		} else {
			Toasty.warning(ctx, getResources().getString(R.string.noNotes));
		}
	}

	private void fetchNotes() {

		notesApi.fetchAllNotes()
				.observe(
						this,
						allNotes -> {
							assert allNotes != null;
							if (!allNotes.isEmpty()) {

								notesList.clear();

								notesList.addAll(allNotes);
								adapter.notifyDataChanged();
								customInsertNoteBinding.recyclerView.setAdapter(adapter);
							}
							customInsertNoteBinding.progressBar.setVisibility(View.GONE);
						});
	}

	public void onDestroy() {
		AttachmentsAdapter.setAttachmentsReceiveListener(null);
		super.onDestroy();
	}

	@Override
	public void setAttachmentsData(Uri filename) {
		contentUri.remove(filename);
	}

	private void checkForAttachments() {

		if (!contentUri.isEmpty()) {

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

	private void processPullRequest() {

		String prTitle = String.valueOf(viewBinding.prTitle.getText());
		String prDescription = String.valueOf(viewBinding.prBody.getText());
		String mergeInto = viewBinding.mergeIntoBranchSpinner.getText().toString();
		String pullFrom = viewBinding.pullFromBranchSpinner.getText().toString();
		String prDueDate = Objects.requireNonNull(viewBinding.prDueDate.getText()).toString();

		assignees.add("");

		if (labelsIds.isEmpty()) {

			labelsIds.add(0);
		}

		if (prTitle.matches("")) {

			SnackBar.error(ctx, findViewById(android.R.id.content), getString(R.string.titleError));
		} else if (mergeInto.matches("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.mergeIntoError));
		} else if (pullFrom.matches("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.pullFromError));
		} else if (pullFrom.equals(mergeInto)) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.sameBranchesError));
		} else {

			createPullRequest(
					prTitle, prDescription, mergeInto, pullFrom, milestoneId, assignees, prDueDate);
		}
	}

	private void createPullRequest(
			String prTitle,
			String prDescription,
			String mergeInto,
			String pullFrom,
			int milestoneId,
			List<String> assignees,
			String prDueDate) {

		ArrayList<Long> labelIds = new ArrayList<>();
		for (Integer i : labelsIds) {
			labelIds.add((long) i);
		}

		CreatePullRequestOption createPullRequest = new CreatePullRequestOption();
		createPullRequest.setTitle(prTitle);
		createPullRequest.setMilestone((long) milestoneId);
		createPullRequest.setAssignees(assignees);
		createPullRequest.setBody(prDescription);
		createPullRequest.setBase(mergeInto);
		createPullRequest.setHead(pullFrom);
		createPullRequest.setLabels(labelIds);
		String[] date = prDueDate.split("-");
		if (!prDueDate.equalsIgnoreCase("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
			calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
			calendar.set(Calendar.DATE, Integer.parseInt(date[2]));
			Date dueDate = calendar.getTime();
			createPullRequest.setDueDate(dueDate);
		}

		Call<PullRequest> transferCall =
				RetrofitClient.getApiInterface(ctx)
						.repoCreatePullRequest(
								repository.getOwner(), repository.getName(), createPullRequest);

		transferCall.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<PullRequest> call,
							@NonNull retrofit2.Response<PullRequest> response) {

						if (response.code() == 201) {

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.prCreateSuccess));
							RepoDetailActivity.updateRepo = true;
							PullRequestsFragment.resumePullRequests = true;
							MainActivity.reloadRepos = true;

							if (!contentUri.isEmpty()) {
								assert response.body() != null;
								processAttachments(response.body().getNumber());
								contentUri.clear();
							} else {
								new Handler().postDelayed(() -> finish(), 3000);
							}
						} else if (response.code() == 409
								|| response.message().equals("Conflict")) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.prAlreadyExists));
						} else if (response.code() == 404) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.apiNotFound));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<PullRequest> call, @NonNull Throwable t) {

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

		viewBinding.prDueDate.setOnClickListener(
				v -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

		materialDatePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					SimpleDateFormat format =
							new SimpleDateFormat("yyyy-MM-dd", new Locale(locale_[1]));
					String formattedDate = format.format(calendar.getTime());
					viewBinding.prDueDate.setText(formattedDate);
				});
	}

	@Override
	public void labelsInterface(List<String> data) {

		String labelsSetter = String.valueOf(data);
		viewBinding.prLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
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

	private void getBranches(String repoOwner, String repoName) {

		Call<List<Branch>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListBranches(repoOwner, repoName, null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branch>> call,
							@NonNull retrofit2.Response<List<Branch>> response) {

						if (response.isSuccessful()) {

							if (response.code() == 200) {

								List<Branch> branchesList_ = response.body();
								assert branchesList_ != null;

								for (Branch i : branchesList_) {
									branchesList.add(i.getName());
								}

								ArrayAdapter<String> adapter =
										new ArrayAdapter<>(
												CreatePullRequestActivity.this,
												R.layout.list_spinner_items,
												branchesList);

								viewBinding.mergeIntoBranchSpinner.setAdapter(adapter);
								viewBinding.pullFromBranchSpinner.setAdapter(adapter);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {

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

						if (response.code() == 200) {

							List<Milestone> milestonesList_ = response.body();

							milestonesList.put(
									getString(R.string.issueCreatedNoMilestone),
									new Milestone()
											.id(0L)
											.title(getString(R.string.issueCreatedNoMilestone)));
							assert milestonesList_ != null;

							if (!milestonesList_.isEmpty()) {

								for (Milestone milestone : milestonesList_) {

									// Don't translate "open" is a enum
									if (milestone.getState().equals("open")) {
										milestonesList.put(milestone.getTitle(), milestone);
									}
								}
							}

							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											CreatePullRequestActivity.this,
											R.layout.list_spinner_items,
											new ArrayList<>(milestonesList.keySet()));

							viewBinding.milestonesSpinner.setAdapter(adapter);

							viewBinding.milestonesSpinner.setOnItemClickListener(
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
