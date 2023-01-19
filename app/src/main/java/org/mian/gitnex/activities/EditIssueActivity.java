package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.vdurmont.emoji.EmojiParser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityEditIssueBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class EditIssueActivity extends BaseActivity implements View.OnClickListener {

	private ActivityEditIssueBinding binding;
	private final String msState = "open";
	private final LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	private View.OnClickListener onClickListener;
	private int resultLimit;
	private int milestoneId = 0;
	private Date currentDate = null;
	private IssueContext issue;
	private boolean renderMd = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityEditIssueBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setSupportActionBar(binding.toolbar);

		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		resultLimit = Constants.getCurrentResultLimit(ctx);
		issue = IssueContext.fromIntent(getIntent());

		binding.editIssueTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(binding.editIssueTitle, InputMethodManager.SHOW_IMPLICIT);

		binding.editIssueDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		initCloseListener();
		binding.close.setOnClickListener(onClickListener);

		binding.editIssueDueDate.setOnClickListener(this);
		binding.editIssueButton.setOnClickListener(this);

		if (issue.getIssueType().equalsIgnoreCase("Pull")) {

			binding.toolbarTitle.setText(
					getString(R.string.editPrNavHeader, String.valueOf(issue.getIssueIndex())));
		} else {

			binding.toolbarTitle.setText(
					getString(R.string.editIssueNavHeader, String.valueOf(issue.getIssueIndex())));
		}

		disableProcessButton();
		getIssue(
				issue.getRepository().getOwner(),
				issue.getRepository().getName(),
				issue.getIssueIndex(),
				resultLimit);

		if (!issue.getRepository().getPermissions().isPush()) {
			findViewById(R.id.editIssueMilestoneSpinnerLayout).setVisibility(View.GONE);
			findViewById(R.id.editIssueDueDateLayout).setVisibility(View.GONE);
		}
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.markdown_switcher, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == R.id.markdown) {

			if (!renderMd) {

				Markdown.render(
						ctx,
						EmojiParser.parseToUnicode(
								Objects.requireNonNull(binding.editIssueDescription.getText())
										.toString()),
						binding.markdownPreview,
						issue.getRepository());

				binding.markdownPreview.setVisibility(View.VISIBLE);
				binding.editIssueDescriptionLayout.setVisibility(View.GONE);
				renderMd = true;
			} else {
				binding.markdownPreview.setVisibility(View.GONE);
				binding.editIssueDescriptionLayout.setVisibility(View.VISIBLE);
				renderMd = false;
			}

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	private void processEditIssue() {

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String editIssueTitleForm =
				Objects.requireNonNull(binding.editIssueTitle.getText()).toString();
		String editIssueDescriptionForm =
				Objects.requireNonNull(binding.editIssueDescription.getText()).toString();

		if (!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if (editIssueTitleForm.equals("")) {

			Toasty.error(ctx, getString(R.string.issueTitleEmpty));
			return;
		}

		disableProcessButton();
		editIssue(
				issue.getRepository().getOwner(),
				issue.getRepository().getName(),
				issue.getIssueIndex(),
				editIssueTitleForm,
				editIssueDescriptionForm,
				milestoneId);
	}

	private void editIssue(
			String repoOwner,
			String repoName,
			int issueIndex,
			String title,
			String description,
			int milestoneId) {

		EditIssueOption issueData = new EditIssueOption();
		issueData.setTitle(title);
		issueData.setBody(description);
		issueData.setDueDate(currentDate);
		issueData.setMilestone((long) milestoneId);

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueEditIssue(repoOwner, repoName, (long) issueIndex, issueData);

		call.enqueue(
				new Callback<Issue>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response) {

						if (response.code() == 201) {

							if (issue.getIssueType().equalsIgnoreCase("Pull")) {

								Toasty.success(ctx, getString(R.string.editPrSuccessMessage));
							} else {

								Toasty.success(ctx, getString(R.string.editIssueSuccessMessage));
							}

							Intent result = new Intent();
							result.putExtra("issueEdited", true);
							IssuesFragment.resumeIssues = issue.getIssue().getPullRequest() == null;
							PullRequestsFragment.resumePullRequests =
									issue.getIssue().getPullRequest() != null;
							setResult(200, result);
							finish();
						} else if (response.code() == 401) {

							enableProcessButton();
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							enableProcessButton();
							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

						Log.e("onFailure", t.toString());
						enableProcessButton();
					}
				});
	}

	@Override
	public void onClick(View v) {

		if (v == binding.editIssueDueDate) {

			final Calendar c = Calendar.getInstance();
			int mYear = c.get(Calendar.YEAR);
			final int mMonth = c.get(Calendar.MONTH);
			final int mDay = c.get(Calendar.DAY_OF_MONTH);

			DatePickerDialog datePickerDialog =
					new DatePickerDialog(
							this,
							(view, year, monthOfYear, dayOfMonth) -> {
								binding.editIssueDueDate.setText(
										getString(
												R.string.setDueDate,
												year,
												(monthOfYear + 1),
												dayOfMonth));
								currentDate = new Date(year - 1900, monthOfYear, dayOfMonth);
							},
							mYear,
							mMonth,
							mDay);
			datePickerDialog.show();
		} else if (v == binding.editIssueButton) {

			processEditIssue();
		}
	}

	private void getIssue(
			final String repoOwner, final String repoName, int issueIndex, int resultLimit) {

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetIssue(repoOwner, repoName, (long) issueIndex);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response) {

						if (response.code() == 200) {

							assert response.body() != null;
							binding.editIssueTitle.setText(response.body().getTitle());
							binding.editIssueDescription.setText(response.body().getBody());

							Milestone currentMilestone = response.body().getMilestone();

							// get milestones list
							if (response.body().getId() > 0) {

								Call<List<Milestone>> call_ =
										RetrofitClient.getApiInterface(ctx)
												.issueGetMilestonesList(
														repoOwner,
														repoName,
														msState,
														null,
														1,
														resultLimit);

								call_.enqueue(
										new Callback<>() {

											@Override
											public void onResponse(
													@NonNull Call<List<Milestone>> call,
													@NonNull retrofit2.Response<List<Milestone>>
																	response_) {

												if (response_.code() == 200) {

													List<Milestone> milestonesList_ =
															response_.body();

													assert milestonesList_ != null;

													Milestone ms = new Milestone();
													ms.setId(0L);
													ms.setTitle(
															getString(
																	R.string
																			.issueCreatedNoMilestone));
													milestonesList.put(ms.getTitle(), ms);

													if (milestonesList_.size() > 0) {

														for (Milestone milestone :
																milestonesList_) {

															// Don't translate "open" is a enum
															if (milestone
																	.getState()
																	.equals("open")) {
																milestonesList.put(
																		milestone.getTitle(),
																		milestone);
															}
														}
													}

													ArrayAdapter<String> adapter =
															new ArrayAdapter<>(
																	EditIssueActivity.this,
																	R.layout.list_spinner_items,
																	new ArrayList<>(
																			milestonesList
																					.keySet()));

													binding.editIssueMilestoneSpinner.setAdapter(
															adapter);

													binding.editIssueMilestoneSpinner
															.setOnItemClickListener(
																	(parent,
																			view,
																			position,
																			id) -> {
																		if (position == 0) {
																			milestoneId = 0;
																		} else if (view
																				instanceof
																				TextView) {
																			milestoneId =
																					Math.toIntExact(
																							Objects
																									.requireNonNull(
																											milestonesList
																													.get(
																															((TextView)
																																			view)
																																	.getText()
																																	.toString()))
																									.getId());
																		}
																	});

													new Handler(Looper.getMainLooper())
															.postDelayed(
																	() -> {
																		if (currentMilestone
																				!= null) {
																			milestoneId =
																					Math.toIntExact(
																							currentMilestone
																									.getId());
																			binding
																					.editIssueMilestoneSpinner
																					.setText(
																							currentMilestone
																									.getTitle(),
																							false);
																		} else {
																			milestoneId = 0;
																			binding
																					.editIssueMilestoneSpinner
																					.setText(
																							getString(
																									R
																											.string
																											.issueCreatedNoMilestone),
																							false);
																		}
																	},
																	500);

													enableProcessButton();
												}
											}

											@Override
											public void onFailure(
													@NonNull Call<List<Milestone>> call,
													@NonNull Throwable t) {

												Log.e("onFailure", t.toString());
											}
										});
							}
							// get milestones list

							if (response.body().getDueDate() != null) {

								@SuppressLint("SimpleDateFormat")
								DateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
								String dueDate = formatter.format(response.body().getDueDate());
								binding.editIssueDueDate.setText(dueDate);
							}
							// enableProcessButton();

						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

						Log.e("onFailure", t.toString());
					}
				});
	}

	private void disableProcessButton() {

		binding.editIssueButton.setEnabled(false);
	}

	private void enableProcessButton() {

		binding.editIssueButton.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}
}
