package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitStatusesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityPullRequestDetailsBinding;
import org.mian.gitnex.databinding.ItemPrMetaRowBinding;
import org.mian.gitnex.databinding.LayoutPrHeaderBinding;
import org.mian.gitnex.fragments.BottomSheetContentViewer;
import org.mian.gitnex.fragments.BottomSheetCreatePullRequest;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.viewmodels.AttachmentsViewModel;
import org.mian.gitnex.viewmodels.CommitStatusesViewModel;
import org.mian.gitnex.viewmodels.PullRequestDetailsViewModel;
import org.mian.gitnex.viewmodels.ReactionsViewModel;
import org.mian.gitnex.views.reactions.ReactionUsersBottomSheet;
import org.mian.gitnex.views.reactions.ReactionsManager;
import retrofit2.Call;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class PullRequestDetailsActivity extends BaseActivity {

	private ActivityPullRequestDetailsBinding binding;
	private PullRequestDetailsViewModel viewModel;
	private ReactionsViewModel reactionsViewModel;
	private CommitStatusesViewModel statusesViewModel;
	private AttachmentsViewModel attachmentsViewModel;
	private Attachment pendingAttachment;
	private ReactionsManager reactionsManager;
	private String owner;
	private String repo;
	private long prNumber;
	private boolean isDataLoaded = false;
	private RepositoryContext repositoryContext;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPullRequestDetailsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repositoryContext = new RepositoryContext(owner, repo, this);

		viewModel = new ViewModelProvider(this).get(PullRequestDetailsViewModel.class);
		reactionsViewModel = new ViewModelProvider(this).get(ReactionsViewModel.class);
		statusesViewModel = new ViewModelProvider(this).get(CommitStatusesViewModel.class);
		attachmentsViewModel = new ViewModelProvider(this).get(AttachmentsViewModel.class);

		UIHelper.applyEdgeToEdge(
				this,
				binding.dockedToolbar,
				binding.scrollView,
				binding.pullToRefresh,
				binding.headerSection.getRoot());

		Intent intent = getIntent();
		String source = intent.getStringExtra("source");

		if ("pr_repo_fragment".equals(source)) {
			PullRequest pr = (PullRequest) intent.getSerializableExtra("prObject");
			if (pr != null && pr.getBase() != null && pr.getBase().getRepo() != null) {
				owner = pr.getBase().getRepo().getOwner().getLogin();
				repo = pr.getBase().getRepo().getName();
				prNumber = pr.getNumber();
			}
		} else if ("my_prs_activity".equals(source)) {
			Issue issue = (Issue) intent.getSerializableExtra("prIssue");
			if (issue != null && issue.getRepository() != null) {
				String fullName = issue.getRepository().getFullName();
				if (fullName != null && fullName.contains("/")) {
					String[] parts = fullName.split("/");
					owner = parts[0];
					repo = parts[1];
				}
				prNumber = issue.getNumber();
			}
		}

		if (owner == null || repo == null || prNumber <= 0) {
			Toasty.show(this, R.string.invalid_pr);
			finish();
			return;
		}

		setupReactions();
		hideAllContent();
		setupListeners();
		observeViewModel();
		observeReactionsViewModel();
		observeStatusesViewModel();
		observeAttachmentsViewModel();
		fetchPullRequestData();
		fetchReactionSettings();
	}

	private void hideAllContent() {
		binding.headerSection.getRoot().setVisibility(View.GONE);
		binding.descriptionCard.getRoot().setVisibility(View.GONE);
		binding.checksCard.getRoot().setVisibility(View.GONE);
		binding.timelineSection.getRoot().setVisibility(View.GONE);
		binding.expressiveLoader.setVisibility(View.GONE);
	}

	private void showContent() {
		binding.headerSection.getRoot().setVisibility(View.VISIBLE);
		binding.descriptionCard.getRoot().setVisibility(View.VISIBLE);
		binding.checksCard.getRoot().setVisibility(View.VISIBLE);
		binding.timelineSection.getRoot().setVisibility(View.VISIBLE);
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.headerSection.btnEdit.setOnClickListener(
				v -> {
					if (isDataLoaded && viewModel.getPrData().getValue() != null) {
						PullRequest pr = viewModel.getPrData().getValue();
						BottomSheetCreatePullRequest.newInstance(repositoryContext, pr)
								.show(getSupportFragmentManager(), "EDIT_PULL_REQUEST");
					}
				});

		binding.checksCard.checksHeader.setOnClickListener(
				v -> {
					if (!isDataLoaded) return;
					boolean isExpanded =
							binding.checksCard.checksContent.getVisibility() == View.VISIBLE;
					binding.checksCard.checksContent.setVisibility(
							isExpanded ? View.GONE : View.VISIBLE);
					binding.checksCard.checksExpandIcon.setImageResource(
							isExpanded ? R.drawable.ic_chevron_down : R.drawable.ic_chevron_up);
				});

		binding.btnMenu.setOnClickListener(
				v -> {
					// TODO: Open global menu bottom sheet
				});

		binding.pullToRefresh.setOnRefreshListener(this::fetchPullRequestData);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (loading) {
								binding.expressiveLoader.setVisibility(View.VISIBLE);
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		viewModel
				.getPrData()
				.observe(
						this,
						pr -> {
							if (pr != null) {
								isDataLoaded = true;
								showContent();
								populateUI(pr);
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								viewModel.clearError();
							}
						});
	}

	private void fetchPullRequestData() {
		viewModel.fetchPullRequest(this, owner, repo, prNumber);
	}

	private void populateUI(PullRequest pr) {
		updateRepositoryContext(pr);
		populateHeader(pr);
		populateDescription(pr);
		populateChecks(pr);
		// populateTimeline(pr);
		fetchReactions();
	}

	private void updateRepositoryContext(PullRequest pr) {
		if (pr.getBase() != null && pr.getBase().getRepo() != null) {
			Repository fullRepo = pr.getBase().getRepo();
			repositoryContext = new RepositoryContext(fullRepo, this);
		}
	}

	private void populateHeader(PullRequest pr) {
		LayoutPrHeaderBinding header = binding.headerSection;

		setStatusBadge(header, pr);
		setEditButtonVisibility(header, pr);
		setTitle(header, pr.getTitle(), pr.getNumber());
		setAuthor(header, pr);
		setBranchInfo(header, pr);
		setLabels(header, pr.getLabels());
		setMilestone(header, pr);
		setAssignees(header, pr);
		setDueDate(header, pr);
		setReviewers(header, pr);
	}

	private void setStatusBadge(LayoutPrHeaderBinding header, PullRequest pr) {
		String state = pr.getState();
		int statusColor;
		String statusText;

		if ("open".equalsIgnoreCase(state)) {
			statusColor = getColor(R.color.colorDarkGreen);
			statusText = getString(R.string.isOpen).toUpperCase();
		} else if (Boolean.TRUE.equals(pr.isMerged())) {
			statusColor = getColor(R.color.alert_important_border);
			statusText = getString(R.string.merged).toUpperCase();
		} else {
			statusColor = getColor(R.color.colorRed);
			statusText = getString(R.string.isClosed).toUpperCase();
		}

		header.statusBadgeImg.setImageDrawable(
				AvatarGenerator.getLabelDrawable(this, statusText, statusColor, 20));
	}

	private void setEditButtonVisibility(LayoutPrHeaderBinding header, PullRequest pr) {
		boolean canEdit = false;

		if (pr.getBase() != null
				&& pr.getBase().getRepo() != null
				&& pr.getBase().getRepo().getPermissions() != null) {
			canEdit = Boolean.TRUE.equals(pr.getBase().getRepo().getPermissions().isPush());
		}

		boolean isOpen = "open".equalsIgnoreCase(pr.getState());
		boolean isMerged = Boolean.TRUE.equals(pr.isMerged());

		header.btnEdit.setVisibility(canEdit && isOpen && !isMerged ? View.VISIBLE : View.GONE);
	}

	private void setTitle(LayoutPrHeaderBinding header, String title, long number) {
		String numSuffix = " #" + number;
		SpannableStringBuilder ssb = new SpannableStringBuilder(title + numSuffix);

		int color =
				AppUtil.getColorFromAttribute(
						this, com.google.android.material.R.attr.colorOnSurfaceVariant);
		int colorWithAlpha = (color & 0x00FFFFFF) | (0xB2 << 24);

		ssb.setSpan(
				new ForegroundColorSpan(colorWithAlpha),
				title.length(),
				ssb.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		ssb.setSpan(
				new StyleSpan(Typeface.BOLD),
				0,
				title.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		header.prTitle.setText(ssb);
	}

	@SuppressLint("SetTextI18n")
	private void setAuthor(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getUser() == null) {
			header.authorRow.setVisibility(View.GONE);
			return;
		}

		User author = pr.getUser();
		String timeAgo = TimeHelper.formatTime(pr.getCreatedAt(), Locale.getDefault());
		header.authorAndTime.setText(author.getLogin() + " opened " + timeAgo);

		Glide.with(this)
				.load(author.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.error(AvatarGenerator.getLetterAvatar(this, author.getLogin(), 20))
				.centerCrop()
				.into(header.authorAvatar);

		header.authorAvatar.setOnClickListener(v -> openProfile(author.getLogin()));
		header.authorAndTime.setOnClickListener(
				v ->
						Toasty.show(
								ctx,
								TimeHelper.getFullDateTime(
										pr.getCreatedAt(), Locale.getDefault())));
	}

	@SuppressLint("SetTextI18n")
	private void setBranchInfo(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getBase() == null || pr.getHead() == null) {
			header.branchRow.setVisibility(View.GONE);
			return;
		}
		header.branchInfo.setText(pr.getBase().getRef() + " ← " + pr.getHead().getRef());
	}

	private void setLabels(LayoutPrHeaderBinding header, List<Label> labels) {
		if (labels == null || labels.isEmpty()) {
			header.labelsChipGroup.setVisibility(View.GONE);
			return;
		}

		header.labelsChipGroup.setVisibility(View.VISIBLE);
		header.labelsChipGroup.removeAllViews();

		for (Label label : labels) {
			int color = Color.parseColor("#" + label.getColor());
			ImageView iv = new ImageView(this);
			iv.setImageDrawable(AvatarGenerator.getLabelDrawable(this, label.getName(), color, 22));
			iv.setLayoutParams(new ChipGroup.LayoutParams(-2, -2));
			header.labelsChipGroup.addView(iv);
		}
	}

	private void setMilestone(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getMilestone() == null) {
			header.rowMilestone.getRoot().setVisibility(View.GONE);
			return;
		}
		setupMetaRow(header.rowMilestone, R.drawable.ic_milestone, pr.getMilestone().getTitle());
	}

	private void setDueDate(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getDueDate() == null) {
			header.rowDueDate.getRoot().setVisibility(View.GONE);
			return;
		}
		setupMetaRow(header.rowDueDate, R.drawable.ic_calendar, formatDate(pr.getDueDate()));
	}

	private void setAssignees(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getAssignees() == null || pr.getAssignees().isEmpty()) {
			header.rowAssignees.getRoot().setVisibility(View.GONE);
			return;
		}
		setupPeopleRow(header.rowAssignees, R.drawable.ic_person, pr.getAssignees());
	}

	private void setReviewers(LayoutPrHeaderBinding header, PullRequest pr) {
		if (pr.getRequestedReviewers() == null || pr.getRequestedReviewers().isEmpty()) {
			header.rowReviewers.getRoot().setVisibility(View.GONE);
			return;
		}
		setupPeopleRow(header.rowReviewers, R.drawable.ic_followers, pr.getRequestedReviewers());
	}

	private void setupPeopleRow(ItemPrMetaRowBinding row, int iconRes, List<User> users) {
		row.metaIcon.setImageResource(iconRes);
		row.avatarContainer.setVisibility(View.VISIBLE);
		row.avatarContainer.removeAllViews();
		row.metaText.setVisibility(View.GONE);
		row.getRoot().setVisibility(View.VISIBLE);

		for (User user : users) {
			ImageView avatar = createAvatarImageView();
			row.avatarContainer.addView(avatar);

			Glide.with(this)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(AvatarGenerator.getLetterAvatar(this, user.getLogin(), 20))
					.centerCrop()
					.into(avatar);

			avatar.setOnClickListener(v -> openProfile(user.getLogin()));
		}
	}

	private void openProfile(String username) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtra("username", username);
		startActivity(intent);
	}

	private ImageView createAvatarImageView() {
		ShapeableImageView avatar = new ShapeableImageView(this);
		int size = (int) (24 * getResources().getDisplayMetrics().density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(0, 0, (int) (6 * getResources().getDisplayMetrics().density), 0);
		avatar.setLayoutParams(params);
		avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
		float cornerSize = getResources().getDimension(R.dimen.dimen4dp);
		avatar.setShapeAppearanceModel(
				avatar.getShapeAppearanceModel().toBuilder()
						.setAllCorners(CornerFamily.ROUNDED, cornerSize)
						.build());
		return avatar;
	}

	private void populateDescription(PullRequest pr) {
		if (pr.getBody() == null || pr.getBody().isEmpty()) {
			binding.descriptionCard.getRoot().setVisibility(View.GONE);
			return;
		}

		binding.descriptionCard.getRoot().setVisibility(View.VISIBLE);
		Markdown.render(
				this,
				pr.getBody(),
				binding.descriptionCard.descriptionContent,
				new RepositoryContext(owner, repo, this));

		fetchAttachments(pr.getNumber());
	}

	private void setupMetaRow(ItemPrMetaRowBinding row, int iconRes, String text) {
		row.metaIcon.setImageResource(iconRes);
		row.metaText.setText(text);
		row.avatarContainer.setVisibility(View.GONE);
		row.metaText.setVisibility(View.VISIBLE);
		row.getRoot().setVisibility(View.VISIBLE);
	}

	private void setupReactions() {
		String currentUser = getAccount().getAccount().getUserName();

		reactionsManager =
				new ReactionsManager(
						this,
						binding.headerSection.reactionsChipGroup,
						binding.headerSection.addReactionButton,
						new ReactionsManager.ReactionListener() {
							@Override
							public void onAddReaction(String content) {
								reactionsViewModel.addReaction(
										PullRequestDetailsActivity.this, content);
							}

							@Override
							public void onRemoveReaction(String content) {
								reactionsViewModel.removeReaction(
										PullRequestDetailsActivity.this, content);
							}

							@Override
							public void onShowUsers(
									String emoji, String content, List<User> users) {
								ReactionUsersBottomSheet.newInstance(emoji, users)
										.show(getSupportFragmentManager(), "REACTION_USERS");
							}

							@Override
							public void onReactionsLoaded() {}
						},
						currentUser);
	}

	private void fetchReactionSettings() {
		reactionsViewModel.fetchReactionSettings(this);
	}

	private void fetchReactions() {
		if (owner != null && repo != null && prNumber > 0) {
			reactionsViewModel.initTarget(
					owner, repo, prNumber, ReactionsViewModel.TargetType.ISSUE);
			reactionsViewModel.fetchReactions(this);
		}
	}

	private void observeReactionsViewModel() {
		reactionsViewModel
				.getAllowedReactions()
				.observe(
						this,
						allowed -> {
							List<String> custom = reactionsViewModel.getCustomEmojis().getValue();
							reactionsManager.setReactionSettings(
									allowed, custom != null ? custom : new ArrayList<>());
						});

		reactionsViewModel
				.getCustomEmojis()
				.observe(
						this,
						custom -> {
							List<String> allowed =
									reactionsViewModel.getAllowedReactions().getValue();
							reactionsManager.setReactionSettings(
									allowed != null ? allowed : new ArrayList<>(), custom);
						});

		reactionsViewModel
				.getReactions()
				.observe(
						this,
						reactions -> {
							if (reactions != null) {
								reactionsManager.setReactions(reactions);
							}
						});

		reactionsViewModel.getIsLoading().observe(this, loading -> {});

		reactionsViewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								reactionsViewModel.clearError();
							}
						});
	}

	private void observeStatusesViewModel() {
		statusesViewModel
				.getStatuses()
				.observe(
						this,
						statuses -> {
							if (statuses != null && !statuses.isEmpty()) {
								binding.checksCard.getRoot().setVisibility(View.VISIBLE);
								setupChecksList(statuses);
							} else {
								binding.checksCard.getRoot().setVisibility(View.GONE);
							}
						});

		statusesViewModel
				.getHasStatuses()
				.observe(
						this,
						hasStatuses -> {
							if (!hasStatuses) {
								binding.checksCard.getRoot().setVisibility(View.GONE);
							}
						});

		statusesViewModel.getIsLoading().observe(this, loading -> {});

		statusesViewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) {
								binding.checksCard.getRoot().setVisibility(View.GONE);
								statusesViewModel.clearError();
							}
						});
	}

	private void fetchStatuses(String sha) {
		if (owner != null && repo != null && sha != null) {
			statusesViewModel.fetchStatuses(this, owner, repo, sha);
		}
	}

	private void setupChecksList(List<CommitStatus> statuses) {
		binding.checksCard.checksList.setLayoutManager(new LinearLayoutManager(this));
		binding.checksCard.checksList.setAdapter(new CommitStatusesAdapter(statuses));

		long successCount =
				statuses.stream()
						.filter(s -> "success".equalsIgnoreCase(s.getStatus().toString()))
						.count();
		long totalCount = statuses.size();

		String summary;
		if (successCount == totalCount) {
			summary = getString(R.string.checks_all_passed, totalCount);
		} else {
			summary = getString(R.string.checks_summary, successCount, totalCount);
		}
		binding.checksCard.checksSummary.setText(summary);
	}

	private void populateChecks(PullRequest pr) {
		if (pr.getHead() != null && pr.getHead().getSha() != null) {
			fetchStatuses(pr.getHead().getSha());
		} else {
			binding.checksCard.getRoot().setVisibility(View.GONE);
		}
	}

	private void observeAttachmentsViewModel() {
		attachmentsViewModel
				.getAttachments()
				.observe(
						this,
						attachments -> {
							if (attachments != null && !attachments.isEmpty()) {
								displayAttachments(attachments);
							} else {
								binding.descriptionCard.attachmentsContainer.setVisibility(
										View.GONE);
							}
						});

		attachmentsViewModel.getIsLoadingAttachments().observe(this, loading -> {});

		attachmentsViewModel
				.getFetchError()
				.observe(
						this,
						error -> {
							if (error != null) {
								binding.descriptionCard.attachmentsContainer.setVisibility(
										View.GONE);
								attachmentsViewModel.clearFetchError();
							}
						});
	}

	private void fetchAttachments(long prNumber) {
		attachmentsViewModel.fetchIssueAttachments(this, owner, repo, prNumber);
	}

	private void downloadAttachment(Attachment attachment) {
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, attachment.getName());
		intent.setType("*/*");

		pendingAttachment = attachment;
		downloadAttachmentLauncher.launch(intent);
	}

	private final ActivityResultLauncher<Intent> downloadAttachmentLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							Uri targetUri = result.getData().getData();
							if (targetUri != null && pendingAttachment != null) {
								downloadAttachmentToUri(pendingAttachment, targetUri);
								pendingAttachment = null;
							}
						}
					});

	private void downloadAttachmentToUri(Attachment attachment, Uri targetUri) {
		String fileName = attachment.getName();
		String fileUuid = attachment.getUuid();
		long fileSize = attachment.getSize();

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(this, Constants.downloadNotificationChannelId)
						.setContentTitle(getString(R.string.download_started_title))
						.setContentText(getString(R.string.download_started_desc, fileName))
						.setSmallIcon(R.drawable.gitnex_transparent)
						.setPriority(NotificationCompat.PRIORITY_LOW)
						.setProgress(100, 0, false)
						.setOngoing(true);

		int notificationId = Notifications.uniqueNotificationId(this);
		NotificationManager notificationManager =
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(notificationId, builder.build());

		new Thread(
						() -> {
							try {
								Call<ResponseBody> call =
										RetrofitClient.getWebInterface(this)
												.getAttachment(fileUuid);

								Response<ResponseBody> response = call.execute();

								if (response.isSuccessful() && response.body() != null) {
									try (OutputStream os =
											getContentResolver().openOutputStream(targetUri)) {
										AppUtil.copyProgress(
												response.body().byteStream(),
												os,
												fileSize,
												progress -> {
													builder.setProgress(100, progress, false);
													notificationManager.notify(
															notificationId, builder.build());
												});

										builder.setContentTitle(
														getString(R.string.download_complete_title))
												.setContentText(
														getString(
																R.string.download_complete_desc,
																fileName))
												.setOngoing(false)
												.setProgress(0, 0, false);
										notificationManager.notify(notificationId, builder.build());

										runOnUiThread(
												() ->
														Toasty.show(
																this, R.string.downloadFileSaved));
									}
								} else {
									throw new IOException("Download failed: " + response.code());
								}
							} catch (Exception e) {
								builder.setContentTitle(getString(R.string.download_failed_title))
										.setContentText(
												getString(R.string.download_failed_desc, fileName))
										.setOngoing(false)
										.setProgress(0, 0, false);
								notificationManager.notify(notificationId, builder.build());

								runOnUiThread(
										() -> Toasty.show(this, R.string.download_failed_title));
							}
						})
				.start();
	}

	private void openAttachmentPreview(Attachment attachment) {
		String fileUuid = attachment.getUuid();
		String fileName = attachment.getName();

		new Thread(
						() -> {
							try {
								Call<ResponseBody> call =
										RetrofitClient.getWebInterface(this)
												.getAttachment(fileUuid);

								Response<ResponseBody> response = call.execute();

								if (response.isSuccessful() && response.body() != null) {
									byte[] imageBytes = response.body().bytes();

									runOnUiThread(
											() -> {
												BottomSheetContentViewer.newInstance(
																imageBytes,
																fileName,
																repositoryContext,
																BottomSheetContentViewer.Feature
																		.IMAGE_PREVIEW,
																BottomSheetContentViewer.Feature
																		.SHOW_TITLE,
																BottomSheetContentViewer.Feature
																		.ALLOW_SHARE)
														.show(
																getSupportFragmentManager(),
																"ATTACHMENT_PREVIEW");
											});
								} else {
									runOnUiThread(
											() -> Toasty.show(this, R.string.image_load_error));
								}
							} catch (Exception e) {
								runOnUiThread(() -> Toasty.show(this, R.string.image_load_error));
							}
						})
				.start();
	}

	private View createAttachmentView(Attachment attachment) {
		MaterialCardView card = new MaterialCardView(this);
		int size = (int) (36 * getResources().getDisplayMetrics().density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(0, 0, (int) (8 * getResources().getDisplayMetrics().density), 0);
		card.setLayoutParams(params);
		card.setRadius(16);
		card.setStrokeWidth(0);

		ImageView icon = new ImageView(this);
		int iconSize = (int) (36 * getResources().getDisplayMetrics().density);
		LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
		icon.setLayoutParams(iconParams);
		icon.setScaleType(ImageView.ScaleType.CENTER_CROP);

		String extension = FilenameUtils.getExtension(attachment.getName()).toLowerCase();
		boolean isImage =
				Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif")
						.contains(extension);

		if (isImage) {
			String thumbnailUrl = attachment.getBrowserDownloadUrl();

			Glide.with(this)
					.load(thumbnailUrl)
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_image)
					.centerCrop()
					.into(icon);

			card.setOnClickListener(v -> openAttachmentPreview(attachment));
		} else {
			icon.setImageResource(FileIcon.getIconResource(attachment.getName(), "file"));
			card.setOnClickListener(v -> downloadAttachment(attachment));
		}

		card.addView(icon);
		return card;
	}

	private void displayAttachments(List<Attachment> attachments) {
		binding.descriptionCard.attachmentsContainer.removeAllViews();
		binding.descriptionCard.attachmentsContainer.setVisibility(View.VISIBLE);

		for (Attachment attachment : attachments) {
			View attachmentView = createAttachmentView(attachment);
			binding.descriptionCard.attachmentsContainer.addView(attachmentView);
		}
	}

	private String formatDate(Date date) {
		if (date == null) return "";
		SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
		return format.format(date);
	}

	@Override
	protected void onGlobalRefresh() {
		fetchPullRequestData();
	}
}
