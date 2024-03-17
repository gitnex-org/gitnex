package org.mian.gitnex.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueLabelsOption;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.User;
import org.gitnex.tea4j.v2.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.AssigneesActions;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.adapters.IssueCommentsAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityIssueDetailBinding;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomImageViewDialogBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomPrInfoDialogBinding;
import org.mian.gitnex.fragments.BottomSheetReplyFragment;
import org.mian.gitnex.fragments.BottomSheetSingleIssueFragment;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.viewmodels.IssueCommentsViewModel;
import org.mian.gitnex.views.ReactionList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class IssueDetailActivity extends BaseActivity
		implements LabelsListAdapter.LabelsListAdapterListener,
				AssigneesListAdapter.AssigneesListAdapterListener,
				BottomSheetListener {

	private Typeface myTypeface;
	public static boolean singleIssueUpdate = false;
	public static boolean commentPosted = false;
	private final List<Label> labelsList = new ArrayList<>();
	private final List<User> assigneesList = new ArrayList<>();
	public boolean commentEdited = false;
	private IssueCommentsAdapter adapter;
	private String repoOwner;
	private String repoName;
	private int issueIndex;
	private String issueCreator;
	public IssueContext issue;
	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;
	private List<Integer> currentLabelsIds = new ArrayList<>();
	private List<Integer> labelsIds = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();
	private List<String> currentAssignees = new ArrayList<>();
	private ActivityIssueDetailBinding viewBinding;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private IssueCommentsViewModel issueCommentsModel;
	private Runnable showMenu = () -> {};
	private boolean loadingFinishedIssue = false;
	private boolean loadingFinishedPr = false;
	private boolean loadingFinishedRepo = false;
	private String filename;
	private Long filesize;
	private String filehash;
	private String instanceUrlOnly;
	private String token;
	private int page = 1;

	public ActivityResultLauncher<Intent> editIssueLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == 200) {
							assert result.getData() != null;
							if (result.getData().getBooleanExtra("issueEdited", false)) {
								new Handler(Looper.getMainLooper())
										.postDelayed(
												() -> {
													viewBinding.frameAssignees.removeAllViews();
													viewBinding.frameLabels.removeAllViews();
													issue.setIssue(null);
													getSingleIssue(repoOwner, repoName, issueIndex);
												},
												500);
							}
						}
					});

	public ActivityResultLauncher<Intent> downloadAttachmentLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {

							assert result.getData() != null;

							try {

								OutputStream outputStream =
										getContentResolver()
												.openOutputStream(
														Objects.requireNonNull(
																result.getData().getData()));

								NotificationCompat.Builder builder =
										new NotificationCompat.Builder(ctx, ctx.getPackageName())
												.setContentTitle(
														getString(
																R.string
																		.fileViewerNotificationTitleStarted))
												.setContentText(
														getString(
																R.string
																		.fileViewerNotificationDescriptionStarted,
																filename))
												.setSmallIcon(R.drawable.gitnex_transparent)
												.setPriority(NotificationCompat.PRIORITY_LOW)
												.setChannelId(
														Constants.downloadNotificationChannelId)
												.setProgress(100, 0, false)
												.setOngoing(true);

								int notificationId = Notifications.uniqueNotificationId(ctx);

								NotificationManager notificationManager =
										(NotificationManager)
												getSystemService(Context.NOTIFICATION_SERVICE);
								notificationManager.notify(notificationId, builder.build());

								Thread thread =
										new Thread(
												() -> {
													try {

														Call<ResponseBody> call =
																RetrofitClient.getWebInterface(
																				ctx,
																				instanceUrlOnly)
																		.getAttachment(filehash);

														Response<ResponseBody> response =
																call.execute();

														assert response.body() != null;

														builder.setOngoing(false)
																.setContentTitle(
																		getString(
																				R.string
																						.fileViewerNotificationTitleFinished))
																.setContentText(
																		getString(
																				R.string
																						.fileViewerNotificationDescriptionFinished,
																				filename));

														AppUtil.copyProgress(
																response.body().byteStream(),
																outputStream,
																filesize,
																progress -> {
																	builder.setProgress(
																			100, progress, false);
																	notificationManager.notify(
																			notificationId,
																			builder.build());
																});

													} catch (IOException ignored) {

														builder.setOngoing(false)
																.setContentTitle(
																		getString(
																				R.string
																						.fileViewerNotificationTitleFailed))
																.setContentText(
																		getString(
																				R.string
																						.fileViewerNotificationDescriptionFailed,
																				filename));

													} finally {

														builder.setProgress(0, 0, false)
																.setOngoing(false);

														notificationManager.notify(
																notificationId, builder.build());
													}
												});

								thread.start();

							} catch (IOException ignored) {
							}
						}
					});

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityIssueDetailBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		issue = IssueContext.fromIntent(getIntent());
		repoOwner = issue.getRepository().getOwner();
		repoName = issue.getRepository().getName();
		issueIndex = issue.getIssueIndex();

		setSupportActionBar(viewBinding.toolbar);
		Objects.requireNonNull(getSupportActionBar()).setTitle(repoName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String instanceUrl = ((BaseActivity) ctx).getAccount().getAccount().getInstanceUrl();
		instanceUrlOnly = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));

		token = ((BaseActivity) ctx).getAccount().getAccount().getToken();

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		issueCommentsModel = new ViewModelProvider(this).get(IssueCommentsViewModel.class);

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setNestedScrollingEnabled(false);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		viewBinding.addNewComment.setOnClickListener(
				v -> {
					BottomSheetReplyFragment bottomSheetReplyFragment =
							BottomSheetReplyFragment.newInstance(new Bundle(), issue);
					bottomSheetReplyFragment.setOnInteractedListener(this::onResume);
					bottomSheetReplyFragment.show(getSupportFragmentManager(), "replyBottomSheet");
				});

		labelsAdapter =
				new LabelsListAdapter(labelsList, IssueDetailActivity.this, currentLabelsIds);
		assigneesAdapter =
				new AssigneesListAdapter(
						ctx, assigneesList, IssueDetailActivity.this, currentAssignees);
		LabelsActions.getCurrentIssueLabels(ctx, repoOwner, repoName, issueIndex, currentLabelsIds);
		AssigneesActions.getCurrentIssueAssignees(
				ctx, repoOwner, repoName, issueIndex, currentAssignees);

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											viewBinding.pullToRefresh.setRefreshing(false);
											issueCommentsModel.loadIssueComments(
													repoOwner, repoName, issueIndex, ctx, null);
										},
										50));

		myTypeface = AppUtil.getTypeface(this);
		viewBinding.toolbarTitle.setTypeface(myTypeface);
		viewBinding.toolbarTitle.setText(repoName);

		getSingleIssue(repoOwner, repoName, issueIndex);
		getAttachments();
		fetchDataAsync(repoOwner, repoName, issueIndex);

		if (getIntent().getStringExtra("openPrDiff") != null
				&& Objects.equals(getIntent().getStringExtra("openPrDiff"), "true")) {
			startActivity(issue.getIntent(ctx, DiffActivity.class));
		}
	}

	@Override
	public void onButtonClicked(String text) {

		switch (text) {
			case "onResume":
				onResume();
				break;

			case "showLabels":
				showLabels();
				break;

			case "showAssignees":
				showAssignees();
				break;
		}
	}

	@Override
	public void labelsInterface(List<String> data) {}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	@Override
	public void assigneesInterface(List<String> data) {

		assigneesListData = data;
	}

	private void showAssignees() {

		assigneesAdapter.updateList(currentAssignees);
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		CustomAssigneesSelectionDialogBinding assigneesBinding =
				CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = assigneesBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setPositiveButton(
				R.string.saveButton,
				(dialog, whichButton) -> {
					currentAssignees = new ArrayList<>(new LinkedHashSet<>(currentAssignees));
					assigneesListData = new ArrayList<>(new LinkedHashSet<>(assigneesListData));
					Collections.sort(assigneesListData);
					Collections.sort(currentAssignees);

					if (!assigneesListData.equals(currentAssignees)) {

						updateIssueAssignees();
					}
				});

		AssigneesActions.getRepositoryAssignees(
				ctx,
				repoOwner,
				repoName,
				assigneesList,
				materialAlertDialogBuilder,
				assigneesAdapter,
				assigneesBinding,
				viewBinding.progressBar);
	}

	public void showLabels() {

		labelsAdapter.updateList(currentLabelsIds);
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		CustomLabelsSelectionDialogBinding labelsBinding =
				CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = labelsBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setPositiveButton(
				R.string.saveButton,
				(dialog, whichButton) -> {
					currentLabelsIds = new ArrayList<>(new LinkedHashSet<>(currentLabelsIds));
					labelsIds = new ArrayList<>(new LinkedHashSet<>(labelsIds));
					Collections.sort(labelsIds);
					Collections.sort(currentLabelsIds);

					if (!labelsIds.equals(currentLabelsIds)) {

						updateIssueLabels();
					}
				});

		LabelsActions.getRepositoryLabels(
				ctx,
				repoOwner,
				repoName,
				labelsList,
				materialAlertDialogBuilder,
				labelsAdapter,
				labelsBinding,
				viewBinding.progressBar);
	}

	private void updateIssueAssignees() {

		EditIssueOption updateAssigneeJson = new EditIssueOption().assignees(assigneesListData);

		Call<Issue> call3 =
				RetrofitClient.getApiInterface(ctx)
						.issueEditIssue(repoOwner, repoName, (long) issueIndex, updateAssigneeJson);

		call3.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call,
							@NonNull retrofit2.Response<Issue> response2) {

						if (response2.code() == 201) {

							Toasty.success(ctx, ctx.getString(R.string.assigneesUpdated));

							viewBinding.frameAssignees.removeAllViews();
							viewBinding.frameLabels.removeAllViews();
							issue.setIssue(response2.body());
							getSingleIssue(repoOwner, repoName, issueIndex);
							currentAssignees.clear();
							new Handler(Looper.getMainLooper())
									.postDelayed(
											() ->
													AssigneesActions.getCurrentIssueAssignees(
															ctx,
															repoOwner,
															repoName,
															issueIndex,
															currentAssignees),
											1000);
						} else if (response2.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response2.code() == 403) {

							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response2.code() == 404) {

							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
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

	private void updateIssueLabels() {

		ArrayList<Long> labelIds = new ArrayList<>();
		for (Integer i : labelsIds) {
			labelIds.add((long) i);
		}

		IssueLabelsOption patchIssueLabels = new IssueLabelsOption();
		patchIssueLabels.setLabels(labelIds);

		Call<List<Label>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueReplaceLabels(
								repoOwner, repoName, (long) issueIndex, patchIssueLabels);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull retrofit2.Response<List<Label>> response) {

						if (response.code() == 200) {

							Toasty.success(ctx, ctx.getString(R.string.labelsUpdated));

							viewBinding.frameAssignees.removeAllViews();
							viewBinding.frameLabels.removeAllViews();
							issue.setIssue(null);
							getSingleIssue(repoOwner, repoName, issueIndex);
							currentLabelsIds.clear();
							new Handler(Looper.getMainLooper())
									.postDelayed(
											() ->
													LabelsActions.getCurrentIssueLabels(
															ctx,
															repoOwner,
															repoName,
															issueIndex,
															currentLabelsIds),
											1000);
							IssuesFragment.resumeIssues = true;
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else {

							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						Log.e("onFailure", t.toString());
					}
				});
	}

	private void updateMenuState() {
		if (loadingFinishedIssue && loadingFinishedPr && loadingFinishedRepo) {
			showMenu.run();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(@NonNull Menu menu) {

		MenuInflater inflater = getMenuInflater();
		showMenu =
				() -> {
					inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
					if (issue.getIssueType() != null) {
						if (issue.getIssueType().equalsIgnoreCase("pull")) {
							inflater.inflate(R.menu.pr_info_menu, menu);
						}
					}
					showMenu = () -> {}; // reset Runnable
				};
		updateMenuState();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		if (id == android.R.id.home) {

			if (issue.hasIssue()
					&& getIntent().getStringExtra("openedFromLink") != null
					&& Objects.equals(getIntent().getStringExtra("openedFromLink"), "true")) {
				Intent intent = issue.getRepository().getIntent(ctx, RepoDetailActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
			finish();
			return true;
		} else if (id == R.id.genericMenu) {

			if (issue.hasIssue()) {
				BottomSheetSingleIssueFragment bottomSheet =
						new BottomSheetSingleIssueFragment(issue, issueCreator);
				bottomSheet.show(getSupportFragmentManager(), "singleIssueBottomSheet");
			}
			return true;
		} else if (id == R.id.prInfo) {

			if (issue.getPullRequest() != null) {

				MaterialAlertDialogBuilder materialAlertDialogBuilderPrInfo =
						new MaterialAlertDialogBuilder(ctx);
				CustomPrInfoDialogBinding customPrInfoDialogBinding =
						CustomPrInfoDialogBinding.inflate(LayoutInflater.from(ctx));
				View view = customPrInfoDialogBinding.getRoot();
				materialAlertDialogBuilderPrInfo.setView(view);

				customPrInfoDialogBinding.baseBranch.setText(
						issue.getPullRequest().getBase().getRef());
				customPrInfoDialogBinding.headBranch.setText(
						issue.getPullRequest().getHead().getRef());

				materialAlertDialogBuilderPrInfo.setTitle(
						getResources().getString(R.string.prMergeInfo));
				materialAlertDialogBuilderPrInfo.setNeutralButton(getString(R.string.close), null);
				materialAlertDialogBuilderPrInfo.create().show();
			}
			return true;
		} else {

			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {

		super.onResume();
		issue.getRepository().checkAccountSwitch(this);

		if (commentPosted) {

			viewBinding.scrollViewComments.post(
					() -> {
						issueCommentsModel.loadIssueComments(
								repoOwner,
								repoName,
								issueIndex,
								ctx,
								() ->
										viewBinding.scrollViewComments.fullScroll(
												ScrollView.FOCUS_DOWN));

						commentPosted = false;
					});
		}

		if (commentEdited) {

			viewBinding.scrollViewComments.post(
					() -> {
						issueCommentsModel.loadIssueComments(
								repoOwner, repoName, issueIndex, ctx, null);
						commentEdited = false;
					});
		}

		if (singleIssueUpdate) {

			new Handler(Looper.getMainLooper())
					.postDelayed(
							() -> {
								viewBinding.frameAssignees.removeAllViews();
								viewBinding.frameLabels.removeAllViews();
								issue.setIssue(null);
								getSingleIssue(repoOwner, repoName, issueIndex);
								singleIssueUpdate = false;
							},
							500);
		}
	}

	private void fetchDataAsync(String owner, String repo, int index) {

		issueCommentsModel
				.getIssueCommentList(owner, repo, index, ctx)
				.observe(
						this,
						issueCommentsMain -> {
							Bundle bundle = new Bundle();
							bundle.putString("repoOwner", repoOwner);
							bundle.putString("repoName", repoName);
							bundle.putInt("issueNumber", issueIndex);

							adapter =
									new IssueCommentsAdapter(
											ctx,
											bundle,
											issueCommentsMain,
											getSupportFragmentManager(),
											this::onResume,
											issue);
							adapter.setLoadMoreListener(
									new IssueCommentsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											issueCommentsModel.loadMoreIssueComments(
													owner, repo, index, ctx, page, adapter);
											viewBinding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											viewBinding.progressBar.setVisibility(View.GONE);
										}
									});

							adapter.notifyDataChanged();
							viewBinding.recyclerView.setAdapter(adapter);
							viewBinding.progressBar.setVisibility(View.GONE);
						});
	}

	private void getSingleIssue(String repoOwner, String repoName, int issueIndex) {

		if (issue.hasIssue()) {
			viewBinding.progressBar.setVisibility(View.GONE);
			getSubscribed();
			initWithIssue();
			return;
		}

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetIssue(repoOwner, repoName, (long) issueIndex);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {

						viewBinding.progressBar.setVisibility(View.GONE);

						if (response.code() == 200) {

							Issue singleIssue = response.body();
							assert singleIssue != null;

							issue.setIssue(singleIssue);
							initWithIssue();
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 404) {

							Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
							finish();
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

						viewBinding.progressBar.setVisibility(View.GONE);
					}
				});

		getSubscribed();
	}

	private void getSubscribed() {
		RetrofitClient.getApiInterface(ctx)
				.issueCheckSubscription(repoOwner, repoName, (long) issueIndex)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<WatchInfo> call,
									@NonNull Response<WatchInfo> response) {

								if (response.isSuccessful()) {
									assert response.body() != null;
									issue.setSubscribed(response.body().isSubscribed());
								} else {
									issue.setSubscribed(false);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

								issue.setSubscribed(false);
							}
						});
	}

	private void initWithIssue() {

		if (!issue.getRepository().hasRepository()) {
			getRepoInfo();
		} else {
			loadingFinishedRepo = true;
		}
		loadingFinishedIssue = true;
		updateMenuState();

		viewBinding.issuePrState.setVisibility(View.VISIBLE);

		if (issue.getIssue().getPullRequest() != null) {
			getPullRequest();
			if (issue.getIssue().getPullRequest().isMerged()) { // merged

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				ImageViewCompat.setImageTintList(
						viewBinding.issuePrState,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.iconPrMergedColor, null)));
			} else if (!issue.getIssue().getPullRequest().isMerged()
					&& issue.getIssue().getState().equals("closed")) { // closed

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				ImageViewCompat.setImageTintList(
						viewBinding.issuePrState,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.iconIssuePrClosedColor, null)));
			} else if (issue.getIssue().getTitle().contains("[WIP]")
					|| issue.getIssue().getTitle().contains("[wip]")) { // draft

				viewBinding.issuePrState.setImageResource(R.drawable.ic_draft);
				ImageViewCompat.setImageTintList(
						viewBinding.issuePrState,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.colorWhite, null)));
				viewBinding.issuePrState.setBackgroundResource(R.drawable.shape_draft_release);
				viewBinding.issuePrState.setPadding(
						(int) ctx.getResources().getDimension(R.dimen.dimen4dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen2dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen4dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen2dp));

				viewBinding.toolbarTitle.setPadding(
						(int) ctx.getResources().getDimension(R.dimen.dimen12dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen0dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen0dp),
						(int) ctx.getResources().getDimension(R.dimen.dimen0dp));
			} else { // open

				viewBinding.issuePrState.setImageResource(R.drawable.ic_pull_request);
				if (Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_KEY))
						== 3) {
					ImageViewCompat.setImageTintList(
							viewBinding.issuePrState,
							ColorStateList.valueOf(
									ctx.getResources()
											.getColor(R.color.retroThemeColorPrimary, null)));
				} else if (Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_KEY))
						== 4) {
					if (TimeHelper.timeBetweenHours(
							Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY)),
							Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											ctx,
											AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY)),
							Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY)),
							Integer.parseInt(
									AppDatabaseSettings.getSettingsValue(
											ctx,
											AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY)))) {
						ImageViewCompat.setImageTintList(
								viewBinding.issuePrState,
								ColorStateList.valueOf(
										ctx.getResources().getColor(R.color.darkGreen, null)));
					} else {
						ImageViewCompat.setImageTintList(
								viewBinding.issuePrState,
								ColorStateList.valueOf(
										ctx.getResources()
												.getColor(R.color.retroThemeColorPrimary, null)));
					}
				} else if (Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_KEY))
						== 8) {
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
						ImageViewCompat.setImageTintList(
								viewBinding.issuePrState,
								ColorStateList.valueOf(
										ctx.getResources()
												.getColor(
														android.R.color.system_accent1_300, null)));
					}
				} else {
					ImageViewCompat.setImageTintList(
							viewBinding.issuePrState,
							ColorStateList.valueOf(
									ctx.getResources().getColor(R.color.darkGreen, null)));
				}
			}
		} else if (issue.getIssue().getState().equals("closed")) { // issue closed
			loadingFinishedPr = true;
			updateMenuState();
			viewBinding.issuePrState.setImageResource(R.drawable.ic_issue);
			ImageViewCompat.setImageTintList(
					viewBinding.issuePrState,
					ColorStateList.valueOf(
							ctx.getResources().getColor(R.color.iconIssuePrClosedColor, null)));
		} else {
			loadingFinishedPr = true;
			updateMenuState();
			viewBinding.issuePrState.setImageResource(R.drawable.ic_issue);
			if (Integer.parseInt(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_THEME_KEY))
					== 3) {
				ImageViewCompat.setImageTintList(
						viewBinding.issuePrState,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.retroThemeColorPrimary, null)));
			} else if (Integer.parseInt(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_THEME_KEY))
					== 4) {
				if (TimeHelper.timeBetweenHours(
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_HOUR_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_DARK_MIN_KEY)),
						Integer.parseInt(
								AppDatabaseSettings.getSettingsValue(
										ctx, AppDatabaseSettings.APP_THEME_AUTO_LIGHT_MIN_KEY)))) {
					ImageViewCompat.setImageTintList(
							viewBinding.issuePrState,
							ColorStateList.valueOf(
									ctx.getResources().getColor(R.color.darkGreen, null)));
				} else {
					ImageViewCompat.setImageTintList(
							viewBinding.issuePrState,
							ColorStateList.valueOf(
									ctx.getResources()
											.getColor(R.color.retroThemeColorPrimary, null)));
				}
			} else if (Integer.parseInt(
							AppDatabaseSettings.getSettingsValue(
									ctx, AppDatabaseSettings.APP_THEME_KEY))
					== 8) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
					ImageViewCompat.setImageTintList(
							viewBinding.issuePrState,
							ColorStateList.valueOf(
									ctx.getResources()
											.getColor(android.R.color.system_accent1_300, null)));
				}
			} else {
				ImageViewCompat.setImageTintList(
						viewBinding.issuePrState,
						ColorStateList.valueOf(
								ctx.getResources().getColor(R.color.darkGreen, null)));
			}
		}

		final Locale locale = getResources().getConfiguration().locale;
		issueCreator = issue.getIssue().getUser().getLogin();

		PicassoService.getInstance(ctx)
				.get()
				.load(issue.getIssue().getUser().getAvatarUrl())
				.placeholder(R.drawable.loader_animated)
				.transform(new RoundedTransformation(8, 0))
				.resize(120, 120)
				.centerCrop()
				.into(viewBinding.assigneeAvatar);
		String issueNumber_ =
				"<font color='"
						+ ResourcesCompat.getColor(getResources(), R.color.lightGray, null)
						+ "'>"
						+ appCtx.getResources().getString(R.string.hash)
						+ issue.getIssue().getNumber()
						+ "</font>";
		viewBinding.issueTitle.setText(
				HtmlCompat.fromHtml(
						issueNumber_
								+ " "
								+ EmojiParser.parseToUnicode(issue.getIssue().getTitle()),
						HtmlCompat.FROM_HTML_MODE_LEGACY));
		String cleanIssueDescription = issue.getIssue().getBody().trim();

		if (!AppUtil.checkGhostUsers(issue.getIssue().getUser().getLogin())) {

			viewBinding.assigneeAvatar.setOnClickListener(
					loginId -> {
						Intent intent = new Intent(ctx, ProfileActivity.class);
						intent.putExtra("username", issue.getIssue().getUser().getLogin());
						ctx.startActivity(intent);
					});

			viewBinding.assigneeAvatar.setOnLongClickListener(
					loginId -> {
						AppUtil.copyToClipboard(
								ctx,
								issue.getIssue().getUser().getLogin(),
								ctx.getString(
										R.string.copyLoginIdToClipBoard,
										issue.getIssue().getUser().getLogin()));
						return true;
					});
		}

		viewBinding.author.setText(issue.getIssue().getUser().getLogin());

		if (!cleanIssueDescription.isEmpty()) {
			viewBinding.issueDescription.setVisibility(View.VISIBLE);
			Markdown.render(
					ctx,
					cleanIssueDescription,
					viewBinding.issueDescription,
					issue.getRepository());
		} else {
			viewBinding.issueDescription.setVisibility(View.GONE);
		}

		LinearLayout.LayoutParams paramsAssignees = new LinearLayout.LayoutParams(64, 64);
		paramsAssignees.setMargins(15, 0, 0, 0);

		if (issue.getIssue().getAssignees() != null) {

			viewBinding.assigneesScrollView.setVisibility(View.VISIBLE);

			for (int i = 0; i < issue.getIssue().getAssignees().size(); i++) {

				ImageView assigneesView = new ImageView(ctx);

				PicassoService.getInstance(ctx)
						.get()
						.load(issue.getIssue().getAssignees().get(i).getAvatarUrl())
						.placeholder(R.drawable.loader_animated)
						.transform(new RoundedTransformation(36, 0))
						.resize(72, 72)
						.centerCrop()
						.into(assigneesView);

				viewBinding.frameAssignees.addView(assigneesView);
				assigneesView.setLayoutParams(paramsAssignees);

				int finalI = i;

				if (!AppUtil.checkGhostUsers(
						issue.getIssue().getAssignees().get(finalI).getLogin())) {

					assigneesView.setOnClickListener(
							loginId -> {
								Intent intent = new Intent(ctx, ProfileActivity.class);
								intent.putExtra(
										"username",
										issue.getIssue().getAssignees().get(finalI).getLogin());
								ctx.startActivity(intent);
							});

					assigneesView.setOnLongClickListener(
							loginId -> {
								AppUtil.copyToClipboard(
										ctx,
										issue.getIssue().getAssignees().get(finalI).getLogin(),
										ctx.getString(
												R.string.copyLoginIdToClipBoard,
												issue.getIssue()
														.getAssignees()
														.get(finalI)
														.getLogin()));
								return true;
							});
				}
			}
		} else {

			viewBinding.assigneesScrollView.setVisibility(View.GONE);
		}

		LinearLayout.LayoutParams paramsLabels =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
		paramsLabels.setMargins(0, 0, 15, 0);

		if (issue.getIssue().getLabels().size() > 0) {

			viewBinding.labelsScrollView.setVisibility(View.VISIBLE);

			for (int i = 0; i < issue.getIssue().getLabels().size(); i++) {

				String labelColor = issue.getIssue().getLabels().get(i).getColor();
				String labelName = issue.getIssue().getLabels().get(i).getName();
				int color = Color.parseColor("#" + labelColor);

				ImageView labelsView = new ImageView(ctx);
				viewBinding.frameLabels.setOrientation(LinearLayout.HORIZONTAL);
				viewBinding.frameLabels.setGravity(Gravity.START | Gravity.TOP);
				labelsView.setLayoutParams(paramsLabels);

				int height = AppUtil.getPixelsFromDensity(ctx, 20);
				int textSize = AppUtil.getPixelsFromScaledDensity(ctx, 12);

				TextDrawable drawable =
						TextDrawable.builder()
								.beginConfig()
								.useFont(myTypeface)
								.textColor(new ColorInverter().getContrastColor(color))
								.fontSize(textSize)
								.width(
										LabelWidthCalculator.calculateLabelWidth(
												labelName,
												myTypeface,
												textSize,
												AppUtil.getPixelsFromDensity(ctx, 10)))
								.height(height)
								.endConfig()
								.buildRoundRect(
										labelName, color, AppUtil.getPixelsFromDensity(ctx, 18));

				labelsView.setImageDrawable(drawable);
				viewBinding.frameLabels.addView(labelsView);
			}
		} else {

			viewBinding.labelsScrollView.setVisibility(View.GONE);
		}

		if (issue.getIssue().getDueDate() != null) {

			viewBinding.dueDateFrame.setVisibility(View.VISIBLE);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
			String dueDate = formatter.format(issue.getIssue().getDueDate());
			viewBinding.issueDueDate.setText(dueDate);
			viewBinding.issueDueDate.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(
									issue.getIssue().getDueDate()),
							ctx));
		} else {

			viewBinding.dueDateFrame.setVisibility(View.GONE);
		}

		String edited;

		if (!issue.getIssue().getUpdatedAt().equals(issue.getIssue().getUpdatedAt())) {

			edited = getString(R.string.colorfulBulletSpan) + getString(R.string.modifiedText);
			viewBinding.issueModified.setVisibility(View.VISIBLE);
			viewBinding.issueModified.setText(edited);
			viewBinding.issueModified.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(
									issue.getIssue().getUpdatedAt()),
							ctx));
		} else {

			viewBinding.issueModified.setVisibility(View.INVISIBLE);
		}

		viewBinding.issueCreatedTime.setVisibility(View.VISIBLE);
		viewBinding.issueCreatedTime.setText(
				TimeHelper.formatTime(issue.getIssue().getCreatedAt(), locale));
		viewBinding.issueCreatedTime.setOnClickListener(
				new ClickListener(
						TimeHelper.customDateFormatForToastDateFormat(
								issue.getIssue().getCreatedAt()),
						ctx));

		Bundle bundle = new Bundle();
		bundle.putString("repoOwner", repoOwner);
		bundle.putString("repoName", repoName);
		bundle.putInt("issueId", Math.toIntExact(issue.getIssue().getNumber()));

		ReactionList reactionList = new ReactionList(ctx, bundle);

		viewBinding.commentReactionBadges.removeAllViews();
		viewBinding.commentReactionBadges.addView(reactionList);

		reactionList.setOnReactionAddedListener(
				() -> {
					if (viewBinding.commentReactionBadges.getVisibility() != View.VISIBLE) {
						viewBinding.commentReactionBadges.post(
								() ->
										viewBinding.commentReactionBadges.setVisibility(
												View.VISIBLE));
					}
				});

		if (issue.getIssue().getMilestone() != null) {

			viewBinding.milestoneFrame.setVisibility(View.VISIBLE);
			viewBinding.issueMilestone.setText(issue.getIssue().getMilestone().getTitle());
		} else {

			viewBinding.milestoneFrame.setVisibility(View.GONE);
		}
	}

	private void getPullRequest() {
		RetrofitClient.getApiInterface(this)
				.repoGetPullRequest(repoOwner, repoName, (long) issueIndex)
				.enqueue(
						new Callback<>() {

							@Override
							public void onResponse(
									@NonNull Call<PullRequest> call,
									@NonNull Response<PullRequest> response) {
								if (response.isSuccessful() && response.body() != null) {
									issue.setPullRequest(response.body());
									loadingFinishedPr = true;
									updateMenuState();
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<PullRequest> call, @NonNull Throwable t) {}
						});
	}

	private void getRepoInfo() {
		Call<Repository> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGet(issue.getRepository().getOwner(), issue.getRepository().getName());
		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull retrofit2.Response<Repository> response) {

						Repository repoInfo = response.body();

						if (response.code() == 200) {
							assert repoInfo != null;
							issue.getRepository().setRepository(repoInfo);
							loadingFinishedRepo = true;
							updateMenuState();
						} else {
							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

						Toasty.error(ctx, getString(R.string.genericError));
					}
				});
	}

	private void getAttachments() {

		Call<List<Attachment>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssueAttachments(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issueIndex);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Attachment>> call,
							@NonNull retrofit2.Response<List<Attachment>> response) {

						List<Attachment> attachment = response.body();

						if (response.code() == 200) {
							assert attachment != null;

							if (attachment.size() > 0) {

								viewBinding.attachmentFrame.setVisibility(View.VISIBLE);
								LinearLayout.LayoutParams paramsAttachment =
										new LinearLayout.LayoutParams(96, 96);
								paramsAttachment.setMargins(0, 0, 48, 0);

								for (int i = 0; i < attachment.size(); i++) {

									ImageView attachmentView = new ImageView(ctx);
									MaterialCardView materialCardView = new MaterialCardView(ctx);
									materialCardView.setLayoutParams(paramsAttachment);
									materialCardView.setStrokeWidth(0);
									materialCardView.setCardBackgroundColor(Color.TRANSPARENT);

									if (Arrays.asList(
													"bmp", "gif", "jpg", "jpeg", "png", "webp",
													"heic", "heif")
											.contains(
													FilenameUtils.getExtension(
																	attachment.get(i).getName())
															.toLowerCase())) {

										PicassoService.getInstance(ctx)
												.get()
												.load(
														attachment.get(i).getBrowserDownloadUrl()
																+ "?token="
																+ token)
												.placeholder(R.drawable.loader_animated)
												.resize(120, 120)
												.centerCrop()
												.error(R.drawable.ic_close)
												.into(attachmentView);

										viewBinding.attachmentsView.addView(materialCardView);
										attachmentView.setLayoutParams(paramsAttachment);
										materialCardView.addView(attachmentView);

										int finalI1 = i;
										materialCardView.setOnClickListener(
												v1 ->
														imageViewDialog(
																attachment
																		.get(finalI1)
																		.getBrowserDownloadUrl()));

									} else {

										attachmentView.setImageResource(
												R.drawable.ic_file_download);
										attachmentView.setPadding(4, 4, 4, 4);
										viewBinding.attachmentsView.addView(materialCardView);
										attachmentView.setLayoutParams(paramsAttachment);
										materialCardView.addView(attachmentView);

										int finalI = i;
										materialCardView.setOnClickListener(
												v1 -> {
													filesize = attachment.get(finalI).getSize();
													filename = attachment.get(finalI).getName();
													filehash = attachment.get(finalI).getUuid();
													requestFileDownload();
												});
									}
								}
							} else {
								viewBinding.attachmentFrame.setVisibility(View.GONE);
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Attachment>> call, @NonNull Throwable t) {}
				});
	}

	private void requestFileDownload() {

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, filename);
		intent.setType("*/*");

		downloadAttachmentLauncher.launch(intent);
	}

	private void imageViewDialog(String url) {

		CustomImageViewDialogBinding imageViewDialogBinding =
				CustomImageViewDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = imageViewDialogBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		materialAlertDialogBuilder.setNeutralButton(getString(R.string.close), null);
		PicassoService.getInstance(ctx)
				.get()
				.load(url + "?token=" + token)
				.placeholder(R.drawable.loader_animated)
				.resize(0, 1600)
				.onlyScaleDown()
				.centerCrop()
				.error(R.drawable.ic_close)
				.into(imageViewDialogBinding.imageView);
		materialAlertDialogBuilder.create().show();
	}
}
