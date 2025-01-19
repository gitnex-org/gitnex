package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.vdurmont.emoji.EmojiParser;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AttachmentsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityEditIssueBinding;
import org.mian.gitnex.databinding.BottomSheetAttachmentsBinding;
import org.mian.gitnex.databinding.CustomImageViewDialogBinding;
import org.mian.gitnex.fragments.IssuesFragment;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.attachments.AttachmentUtils;
import org.mian.gitnex.helpers.attachments.AttachmentsModel;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.notifications.Notifications;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class EditIssueActivity extends BaseActivity
		implements AttachmentsAdapter.AttachmentsReceiverListener {

	private ActivityEditIssueBinding binding;
	private final String msState = "open";
	private final LinkedHashMap<String, Milestone> milestonesList = new LinkedHashMap<>();
	private int milestoneId = 0;
	private IssueContext issue;
	private boolean renderMd = false;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private String token;
	private String filename;
	private Long filesize;
	private String filehash;
	private String instanceUrlOnly;
	private AttachmentsAdapter attachmentsAdapter;
	private static List<AttachmentsModel> attachmentsList;
	private static final List<Uri> contentUri = new ArrayList<>();
	private MenuItem create;

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

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityEditIssueBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		int resultLimit = Constants.getCurrentResultLimit(ctx);
		issue = IssueContext.fromIntent(getIntent());

		binding.topAppBar.setNavigationOnClickListener(
				v -> {
					finish();
					contentUri.clear();
				});

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		token = ((BaseActivity) ctx).getAccount().getAccount().getToken();

		String instanceUrl = ((BaseActivity) ctx).getAccount().getAccount().getInstanceUrl();
		instanceUrlOnly = instanceUrl.substring(0, instanceUrl.lastIndexOf("api/v1/"));

		attachmentsList = new ArrayList<>();
		attachmentsAdapter = new AttachmentsAdapter(attachmentsList, ctx);

		AttachmentsAdapter.setAttachmentsReceiveListener(this);

		create = binding.topAppBar.getMenu().getItem(2);
		create.setTitle(getString(R.string.saveButton));

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
						create.setVisible(false);
						processEditIssue();
						if (!contentUri.isEmpty()) {
							processAttachments();
							contentUri.clear();
						}
						return true;
					} else if (id == R.id.attachment) {
						checkForAttachments();
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

		getAttachments();

		if (!issue.getRepository().getPermissions().isPush()) {
			findViewById(R.id.editIssueMilestoneSpinnerLayout).setVisibility(View.GONE);
			findViewById(R.id.editIssueDueDateLayout).setVisibility(View.GONE);
		}
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
			attachmentsAdapter.clearAdapter();
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

	private void processAttachments() {

		for (int i = 0; i < contentUri.size(); i++) {

			File file = AttachmentUtils.getFile(ctx, contentUri.get(i));

			RequestBody requestFile =
					RequestBody.create(
							file,
							MediaType.parse(
									Objects.requireNonNull(
											getContentResolver().getType(contentUri.get(i)))));

			uploadAttachments(requestFile, file.getName());
		}
	}

	private void uploadAttachments(RequestBody requestFile, String filename1) {

		Call<Attachment> call3 =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateIssueAttachment(
								requestFile,
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
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

							create.setVisible(true);
							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.attachmentsSaveError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Attachment> call, @NonNull Throwable t) {

						create.setVisible(true);
						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
	}

	private void processEditIssue() {

		String editIssueTitleForm =
				Objects.requireNonNull(binding.editIssueTitle.getText()).toString();
		String editIssueDescriptionForm =
				Objects.requireNonNull(binding.editIssueDescription.getText()).toString();
		String dueDate = Objects.requireNonNull(binding.editIssueDueDate.getText()).toString();

		if (editIssueTitleForm.isEmpty()) {

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

							create.setVisible(true);
							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						create.setVisible(true);
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

													if (!milestonesList_.isEmpty()) {

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

	private void getAttachments() {

		Call<List<Attachment>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueListIssueAttachments(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Attachment>> call,
							@NonNull retrofit2.Response<List<Attachment>> response) {

						List<Attachment> attachment = response.body();

						if (response.code() == 200) {
							assert attachment != null;

							if (!attachment.isEmpty()) {

								binding.attachmentFrame.setVisibility(View.VISIBLE);
								LinearLayout.LayoutParams paramsAttachment =
										new LinearLayout.LayoutParams(96, 96);
								paramsAttachment.setMargins(0, 0, 48, 0);

								for (int i = 0; i < attachment.size(); i++) {

									ImageView attachmentView = new ImageView(ctx);
									MaterialCardView materialCardView = new MaterialCardView(ctx);
									materialCardView.setLayoutParams(paramsAttachment);
									materialCardView.setStrokeWidth(0);
									materialCardView.setRadius(28);
									materialCardView.setCardBackgroundColor(Color.TRANSPARENT);

									if (Arrays.asList(
													"bmp", "gif", "jpg", "jpeg", "png", "webp",
													"heic", "heif")
											.contains(
													FilenameUtils.getExtension(
																	attachment.get(i).getName())
															.toLowerCase())) {

										Glide.with(ctx)
												.load(
														attachment.get(i).getBrowserDownloadUrl()
																+ "?token="
																+ token)
												.diskCacheStrategy(DiskCacheStrategy.ALL)
												.placeholder(R.drawable.loader_animated)
												.centerCrop()
												.error(R.drawable.ic_close)
												.into(attachmentView);

										binding.attachmentsView.addView(materialCardView);
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
										binding.attachmentsView.addView(materialCardView);
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
								binding.attachmentFrame.setVisibility(View.GONE);
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

		Glide.with(ctx)
				.asBitmap()
				.load(url + "?token=" + token)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.error(R.drawable.ic_close)
				.into(
						new CustomTarget<Bitmap>() {
							@Override
							public void onResourceReady(
									@NonNull Bitmap resource,
									Transition<? super Bitmap> transition) {
								imageViewDialogBinding.imageView.setImageBitmap(resource);
								imageViewDialogBinding.imageView.buildDrawingCache();
							}

							@Override
							public void onLoadCleared(Drawable placeholder) {}
						});

		materialAlertDialogBuilder.create().show();
	}

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}
}
