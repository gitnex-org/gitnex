package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreatePullRequestOption;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.Milestone;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreatePullRequestActivity extends BaseActivity
		implements LabelsListAdapter.LabelsListAdapterListener {

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
	private boolean renderMd = false;
	private RepositoryContext repositoryContext;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityCreatePrBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repositoryContext = RepositoryContext.fromIntent(getIntent());

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		repository = RepositoryContext.fromIntent(getIntent());

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
					// contentUri.clear();
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
						/*} else if (id == R.id.attachment) {
						checkForAttachments();
						return true;*/
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});

		getMilestones(repository.getOwner(), repository.getName(), resultLimit);
		getBranches(repository.getOwner(), repository.getName());

		viewBinding.prLabels.setOnClickListener(prLabels -> showLabels());

		if (!repository.getPermissions().isPush()) {
			viewBinding.prDueDateLayout.setVisibility(View.GONE);
			viewBinding.prLabelsLayout.setVisibility(View.GONE);
			viewBinding.milestonesSpinnerLayout.setVisibility(View.GONE);
		}
	}

	private void processPullRequest() {

		String prTitle = String.valueOf(viewBinding.prTitle.getText());
		String prDescription = String.valueOf(viewBinding.prBody.getText());
		String mergeInto = viewBinding.mergeIntoBranchSpinner.getText().toString();
		String pullFrom = viewBinding.pullFromBranchSpinner.getText().toString();
		String prDueDate = Objects.requireNonNull(viewBinding.prDueDate.getText()).toString();

		assignees.add("");

		if (labelsIds.size() == 0) {

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
							new Handler().postDelayed(() -> finish(), 3000);
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

		viewBinding.prDueDate.setOnClickListener(
				v -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

		materialDatePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					SimpleDateFormat format =
							new SimpleDateFormat(
									"yyyy-MM-dd", new Locale(tinyDB.getString("locale")));
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
