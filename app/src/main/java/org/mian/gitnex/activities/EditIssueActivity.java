package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.vdurmont.emoji.EmojiParser;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityEditIssueBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class EditIssueActivity extends BaseActivity {

	private ActivityEditIssueBinding binding;
	private final String msState = "open";
	private final LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	private int milestoneId = 0;
	private IssueContext issue;
	private boolean renderMd = false;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityEditIssueBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		int resultLimit = Constants.getCurrentResultLimit(ctx);
		issue = IssueContext.fromIntent(getIntent());

		binding.topAppBar.setNavigationOnClickListener(v -> finish());

		MenuItem attachment = binding.topAppBar.getMenu().getItem(0);
		MenuItem create = binding.topAppBar.getMenu().getItem(2);
		attachment.setVisible(false);
		create.setTitle(getString(R.string.menuEditText));

		binding.editIssueDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		if (issue.getIssueType().equalsIgnoreCase("Pull")) {
			binding.topAppBar.setTitle(
					getString(R.string.editPrNavHeader, String.valueOf(issue.getIssueIndex())));
		} else {
			binding.topAppBar.setTitle(
					getString(R.string.editIssueNavHeader, String.valueOf(issue.getIssueIndex())));
		}

		showDatePickerDialog();

		binding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.markdown) {

						if (!renderMd) {

							Markdown.render(
									ctx,
									EmojiParser.parseToUnicode(
											Objects.requireNonNull(
															binding.editIssueDescription.getText())
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
					} else if (id == R.id.create) {
						processEditIssue();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});

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

	private void processEditIssue() {

		String editIssueTitleForm =
				Objects.requireNonNull(binding.editIssueTitle.getText()).toString();
		String editIssueDescriptionForm =
				Objects.requireNonNull(binding.editIssueDescription.getText()).toString();
		String dueDate = Objects.requireNonNull(binding.editIssueDueDate.getText()).toString();

		if (editIssueTitleForm.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.issueTitleEmpty));
			return;
		}

		editIssue(
				issue.getRepository().getOwner(),
				issue.getRepository().getName(),
				issue.getIssueIndex(),
				editIssueTitleForm,
				editIssueDescriptionForm,
				milestoneId,
				dueDate);
	}

	private void editIssue(
			String repoOwner,
			String repoName,
			int issueIndex,
			String title,
			String description,
			int milestoneId,
			String dueDate) {

		EditIssueOption issueData = new EditIssueOption();
		issueData.setTitle(title);
		issueData.setBody(description);
		String[] date = dueDate.split("-");
		if (!dueDate.equalsIgnoreCase("")) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, Integer.parseInt(date[0]));
			calendar.set(Calendar.MONTH, Integer.parseInt(date[1]));
			calendar.set(Calendar.DATE, Integer.parseInt(date[2]));
			Date dueDate_ = calendar.getTime();
			issueData.setDueDate(dueDate_);
		}
		issueData.setMilestone((long) milestoneId);

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueEditIssue(repoOwner, repoName, (long) issueIndex, issueData);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response) {

						if (response.code() == 201) {

							if (issue.getIssueType().equalsIgnoreCase("Pull")) {

								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.editPrSuccessMessage));
							} else {

								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.editIssueSuccessMessage));
							}

							Intent result = new Intent();
							result.putExtra("issueEdited", true);
							IssuesFragment.resumeIssues = issue.getIssue().getPullRequest() == null;
							PullRequestsFragment.resumePullRequests =
									issue.getIssue().getPullRequest() != null;
							setResult(200, result);
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {}
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

		binding.editIssueDueDate.setOnClickListener(
				v -> materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER"));

		materialDatePicker.addOnPositiveButtonClickListener(
				selection -> {
					Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
					calendar.setTimeInMillis(selection);
					SimpleDateFormat format =
							new SimpleDateFormat("yyyy-MM-dd", new Locale(locale_[1]));
					String formattedDate = format.format(calendar.getTime());
					binding.editIssueDueDate.setText(formattedDate);
				});
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

						} else if (response.code() == 401) {

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

						// Log.e("onFailure", t.toString());
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}
}
