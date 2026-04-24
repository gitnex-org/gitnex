package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import org.mian.gitnex.adapters.TimelineAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityPullRequestDetailsBinding;
import org.mian.gitnex.databinding.ItemPrMetaRowBinding;
import org.mian.gitnex.databinding.LayoutPrHeaderBinding;
import org.mian.gitnex.fragments.BottomSheetCommentMenu;
import org.mian.gitnex.fragments.BottomSheetContentViewer;
import org.mian.gitnex.fragments.BottomSheetCreatePullRequest;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.TimelineItem;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.viewmodels.AttachmentsViewModel;
import org.mian.gitnex.viewmodels.CommitStatusesViewModel;
import org.mian.gitnex.viewmodels.PullRequestDetailsViewModel;
import org.mian.gitnex.viewmodels.ReactionsViewModel;
import org.mian.gitnex.viewmodels.TimelineViewModel;
import org.mian.gitnex.views.reactions.EmojiPickerPopup;
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
	private TimelineViewModel timelineViewModel;
	private Attachment pendingAttachment;
	private ReactionsManager reactionsManager;
	private TimelineAdapter timelineAdapter;
	private String owner;
	private String repo;
	private long prNumber;
	private boolean isDataLoaded = false;
	private RepositoryContext repositoryContext;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener timelineScrollListener;
	private long editingCommentId = -1;
	private boolean isEditing = false;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityPullRequestDetailsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		resultLimit = Constants.getCurrentResultLimit(this);

		repositoryContext = new RepositoryContext(owner, repo, this);

		viewModel = new ViewModelProvider(this).get(PullRequestDetailsViewModel.class);
		reactionsViewModel = new ViewModelProvider(this).get(ReactionsViewModel.class);
		statusesViewModel = new ViewModelProvider(this).get(CommitStatusesViewModel.class);
		attachmentsViewModel = new ViewModelProvider(this).get(AttachmentsViewModel.class);
		timelineViewModel = new ViewModelProvider(this).get(TimelineViewModel.class);

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

		setupCommentBox();

		setupTimeline();
		observeTimelineViewModel();
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

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					fetchPullRequestData();
					refreshTimeline();
				});
	}

	private void setupCommentBox() {
		binding.btnReply.setOnClickListener(
				v -> {
					if (binding.commentReply.getRoot().getVisibility() == View.GONE) {
						showReplyBox();
					} else {
						hideReplyBox();
					}
				});

		binding.commentReply.btnCloseReply.setOnClickListener(
				v -> {
					binding.commentReply.etQuickComment.setText("");
					clearCommentBox();
					AppUtil.hideKeyboard(this);
					hideReplyBox();
				});

		binding.commentReply.btnQuickSend.setOnClickListener(
				v -> {
					String body = binding.commentReply.etQuickComment.getText().toString().trim();
					if (body.isEmpty()) return;

					if (isEditing) {
						timelineViewModel.editComment(this, editingCommentId, body);
					} else {
						timelineViewModel.addComment(this, body);
					}
				});

		binding.commentReply.etQuickComment.setOnFocusChangeListener(
				(v, hasFocus) -> {
					if (hasFocus) {
						AppUtil.showKeyboard(this, binding.commentReply.etQuickComment);
					}
				});

		binding.getRoot()
				.getViewTreeObserver()
				.addOnGlobalLayoutListener(
						() -> {
							Rect r = new Rect();
							binding.getRoot().getWindowVisibleDisplayFrame(r);
							int screenHeight = binding.getRoot().getRootView().getHeight();
							int keypadHeight = screenHeight - r.bottom;

							ViewGroup.MarginLayoutParams params =
									(ViewGroup.MarginLayoutParams)
											binding.commentReply.getRoot().getLayoutParams();
							if (keypadHeight > screenHeight * 0.15) {
								params.bottomMargin = keypadHeight;
							} else {
								params.bottomMargin =
										(int) (36 * getResources().getDisplayMetrics().density);
							}
							binding.commentReply.getRoot().setLayoutParams(params);
						});
	}

	private void toggleCommentSectionSpace(boolean expand) {
		float density = getResources().getDisplayMetrics().density;
		int targetHeight = expand ? (int) (190 * density) : 0;

		android.animation.ValueAnimator animator =
				android.animation.ValueAnimator.ofInt(
						binding.scrollSpacer.getLayoutParams().height, targetHeight);

		animator.setDuration(300);
		animator.addUpdateListener(
				animation -> {
					binding.scrollSpacer.getLayoutParams().height =
							(int) animation.getAnimatedValue();
					binding.scrollSpacer.requestLayout();
				});
		animator.start();
	}

	private void showReplyBox() {
		binding.commentReply.getRoot().setVisibility(View.VISIBLE);

		toggleCommentSectionSpace(true);

		binding.commentReply.getRoot().setAlpha(0f);
		binding.commentReply.getRoot().setTranslationY(100f);
		binding.commentReply
				.getRoot()
				.animate()
				.alpha(1f)
				.translationY(0f)
				.setDuration(300)
				.setInterpolator(new android.view.animation.DecelerateInterpolator())
				.withEndAction(
						() -> {
							binding.commentReply.etQuickComment.requestFocus();
						})
				.start();
	}

	private void hideReplyBox() {
		toggleCommentSectionSpace(false);

		binding.commentReply
				.getRoot()
				.animate()
				.alpha(0f)
				.translationY(100f)
				.setDuration(250)
				.withEndAction(() -> binding.commentReply.getRoot().setVisibility(View.GONE))
				.start();
	}

	public void startEditComment(long commentId, String currentBody) {
		editingCommentId = commentId;
		isEditing = true;
		binding.commentReply.etQuickComment.setText(currentBody);
		binding.commentReply.btnQuickSend.setIconResource(R.drawable.ic_edit);
		binding.commentReply.btnExpandEditor.setVisibility(View.GONE);
		showReplyBox();
	}

	private void clearCommentBox() {
		binding.commentReply.etQuickComment.setText("");
		binding.commentReply.etQuickComment.setHint(R.string.commentButtonText);
		binding.commentReply.btnQuickSend.setIconResource(R.drawable.ic_send);
		binding.commentReply.btnExpandEditor.setVisibility(View.VISIBLE);
		editingCommentId = -1;
		isEditing = false;
	}

	private void setupTimeline() {
		timelineViewModel.fetchReactionSettings(this);

		String currentUser = getAccount().getAccount().getUserName();

		timelineAdapter =
				new TimelineAdapter(
						this,
						owner,
						repo,
						currentUser,
						new TimelineAdapter.OnTimelineItemClickListener() {
							@Override
							public void onCommentMenuClick(TimelineItem comment, View anchor) {
								showCommentMenu(comment);
							}

							@Override
							public void onCommentReactionClick(
									TimelineItem comment, String content, boolean isUserReaction) {
								if (isUserReaction) {
									timelineViewModel.removeCommentReaction(
											PullRequestDetailsActivity.this,
											comment.getId(),
											content);
								} else {
									timelineViewModel.addCommentReaction(
											PullRequestDetailsActivity.this,
											comment.getId(),
											content);
								}
							}

							@Override
							public void onCommentAddReactionClick(
									TimelineItem comment, View anchor) {
								showCommentEmojiPicker(comment, anchor);
							}

							@Override
							public void onCommentReactionLongClick(
									TimelineItem comment, String content, List<User> users) {
								ReactionUsersBottomSheet.newInstance(content, users)
										.show(getSupportFragmentManager(), "REACTION_USERS");
							}

							@Override
							public void onAttachmentClick(Attachment attachment) {
								String extension =
										FilenameUtils.getExtension(attachment.getName())
												.toLowerCase();
								boolean isImage =
										Arrays.asList(
														"bmp", "gif", "jpg", "jpeg", "png", "webp",
														"heic", "heif")
												.contains(extension);
								if (isImage) {
									openAttachmentPreview(attachment);
								} else {
									downloadAttachment(attachment);
								}
							}

							@Override
							public void onCommitClick(String sha) {
								Intent intent =
										new Intent(
												PullRequestDetailsActivity.this,
												CommitDetailActivity.class);
								intent.putExtra("sha", sha);
								intent.putExtra("owner", owner);
								intent.putExtra("repo", repo);
								startActivity(intent);
							}

							@Override
							public void onUserClick(String username) {
								Intent intent =
										new Intent(
												PullRequestDetailsActivity.this,
												ProfileActivity.class);
								intent.putExtra("username", username);
								startActivity(intent);
							}
						});

		timelineViewModel
				.getAllowedReactions()
				.observe(
						this,
						allowed -> {
							if (allowed != null) {
								timelineAdapter.setAllowedReactions(allowed);
							}
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.timelineSection.timelineRecyclerView.setLayoutManager(layoutManager);
		binding.timelineSection.timelineRecyclerView.setAdapter(timelineAdapter);
		binding.timelineSection.timelineRecyclerView.setNestedScrollingEnabled(false);

		timelineScrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						timelineViewModel.fetchTimeline(
								PullRequestDetailsActivity.this, page, resultLimit, false);
					}
				};
		binding.timelineSection.timelineRecyclerView.addOnScrollListener(timelineScrollListener);
	}

	private void scrollTimelineToBottom() {
		binding.scrollView.post(
				() -> {
					binding.scrollView.smoothScrollTo(
							0, binding.scrollView.getChildAt(0).getHeight());
				});
	}

	private void observeTimelineViewModel() {
		timelineViewModel.init(owner, repo, prNumber);

		timelineViewModel
				.getSubmittedComment()
				.observe(
						this,
						comment -> {
							if (comment != null) {
								Toasty.show(this, R.string.commentSuccess);
								clearCommentBox();
								AppUtil.hideKeyboard(this);
								hideReplyBox();
								refreshTimeline();
								scrollTimelineToBottom();
								timelineViewModel.clearSubmittedComment();
								binding.timelineSection.timelineRecyclerView.postDelayed(
										this::scrollTimelineToBottom, 800);
							}
						});

		timelineViewModel
				.getEditedComment()
				.observe(
						this,
						comment -> {
							if (comment != null) {
								Toasty.show(this, R.string.editCommentUpdatedText);
								clearCommentBox();
								AppUtil.hideKeyboard(this);
								hideReplyBox();
								refreshTimeline();
								timelineViewModel.clearEditedComment();
							}
						});

		timelineViewModel
				.getIsSubmitting()
				.observe(
						this,
						isSubmitting -> {
							if (isSubmitting != null && isSubmitting) {
								binding.commentReply.btnQuickSend.setVisibility(View.GONE);
								binding.commentReply.commentLoader.setVisibility(View.VISIBLE);
							} else {
								binding.commentReply.commentLoader.setVisibility(View.GONE);
								binding.commentReply.btnQuickSend.setVisibility(View.VISIBLE);
							}
						});

		timelineViewModel
				.getTimeline()
				.observe(
						this,
						timeline -> {
							if (timeline != null) {
								timelineAdapter.setItems(timeline);
								updateTimelineVisibility(!timeline.isEmpty());
							}
						});

		timelineViewModel.getIsLoading().observe(this, loading -> {});

		timelineViewModel
				.getIsRefreshing()
				.observe(
						this,
						refreshing -> {
							if (refreshing != null && !refreshing) {
								binding.pullToRefresh.setRefreshing(false);
							}
						});

		timelineViewModel
				.getError()
				.observe(
						this,
						error -> {
							if (error != null) {
								Toasty.show(this, error);
								timelineViewModel.clearError();
							}
						});

		timelineViewModel
				.getSubmittedComment()
				.observe(
						this,
						comment -> {
							if (comment != null) {
								Toasty.show(this, R.string.commentSuccess);
								refreshTimeline();
								timelineViewModel.clearSubmittedComment();
							}
						});

		timelineViewModel
				.getEditedComment()
				.observe(
						this,
						comment -> {
							if (comment != null) {
								Toasty.show(this, R.string.editCommentUpdatedText);
								refreshTimeline();
								timelineViewModel.clearEditedComment();
							}
						});

		timelineViewModel
				.getCommentDeleted()
				.observe(
						this,
						deleted -> {
							if (deleted != null && deleted) {
								Toasty.show(this, R.string.deleteCommentSuccess);
								refreshTimeline();
								timelineViewModel.clearCommentDeleted();
							}
						});

		timelineViewModel
				.getCommentReactionsUpdate()
				.observe(
						this,
						pair -> {
							if (pair != null && timelineAdapter != null) {
								timelineAdapter.updateCommentReactions(pair.first, pair.second);
							}
						});

		timelineViewModel.getIsDeleting().observe(this, isDeleting -> {});

		timelineViewModel
				.getActionError()
				.observe(
						this,
						error -> {
							if (error != null) {
								if ("UNAUTHORIZED".equals(error)) {
									AlertDialogs.authorizationTokenRevokedDialog(this);
								} else {
									Toasty.show(this, error);
								}
								timelineViewModel.clearActionError();
							}
						});
	}

	private void fetchTimeline() {
		if (owner != null && repo != null && prNumber > 0) {
			timelineViewModel.fetchTimeline(this, 1, resultLimit, true);
		}
	}

	private void refreshTimeline() {
		if (timelineScrollListener != null) {
			timelineScrollListener.resetState();
		}
		timelineViewModel.resetPagination();
		fetchTimeline();
	}

	private void updateTimelineVisibility(boolean hasItems) {
		binding.timelineSection.getRoot().setVisibility(hasItems ? View.VISIBLE : View.GONE);
	}

	private void showCommentEmojiPicker(TimelineItem comment, View anchor) {
		List<String> allowed = reactionsViewModel.getAllowedReactions().getValue();

		if (allowed != null && !allowed.isEmpty()) {
			EmojiPickerPopup popup = new EmojiPickerPopup(this, allowed);
			popup.setOnEmojiSelectedListener(
					content -> {
						timelineViewModel.addCommentReaction(
								PullRequestDetailsActivity.this, comment.getId(), content);
					});
			popup.show(anchor);
		} else {
			reactionsViewModel.fetchReactionSettings(this);
			reactionsViewModel
					.getAllowedReactions()
					.observe(
							this,
							new androidx.lifecycle.Observer<>() {
								@Override
								public void onChanged(List<String> allowedList) {
									if (allowedList != null && !allowedList.isEmpty()) {
										reactionsViewModel
												.getAllowedReactions()
												.removeObserver(this);

										EmojiPickerPopup popup =
												new EmojiPickerPopup(
														PullRequestDetailsActivity.this,
														allowedList);
										popup.setOnEmojiSelectedListener(
												content -> {
													timelineViewModel.addCommentReaction(
															PullRequestDetailsActivity.this,
															comment.getId(),
															content);
												});
										popup.show(anchor);
									}
								}
							});
		}
	}

	private void showCommentMenu(TimelineItem comment) {
		String currentUser = getAccount().getAccount().getUserName();
		String commentAuthor = comment.getUser() != null ? comment.getUser().getLogin() : "";

		BottomSheetCommentMenu sheet =
				BottomSheetCommentMenu.newInstance(
						comment.getId(),
						comment.getBody(),
						comment.getHtmlUrl(),
						commentAuthor,
						currentUser);

		sheet.setCommentMenuListener(
				new BottomSheetCommentMenu.CommentMenuListener() {
					@Override
					public void onEditComment(long commentId, String body) {
						startEditComment(commentId, body);
					}

					@Override
					public void onDeleteComment(long commentId) {
						new MaterialAlertDialogBuilder(PullRequestDetailsActivity.this)
								.setTitle(R.string.delete_comment_title)
								.setMessage(R.string.delete_comment_message)
								.setPositiveButton(
										R.string.menuDeleteText,
										(dialog, which) -> {
											timelineViewModel.deleteComment(
													PullRequestDetailsActivity.this, commentId);
										})
								.setNegativeButton(R.string.cancelButton, null)
								.show();
					}

					@Override
					public void onQuoteReply(long commentId, String body) {
						String quoted = "> " + body.replace("\n", "\n> ") + "\n\n";
						binding.commentReply.etQuickComment.setText(quoted);
						binding.commentReply.etQuickComment.setSelection(quoted.length());
						showReplyBox();
					}

					@Override
					public void onCopyUrl(String url) {
						AppUtil.copyToClipboard(
								PullRequestDetailsActivity.this,
								url,
								getString(R.string.copied_to_clipboard));
					}

					@Override
					public void onShareComment(String body, String url) {
						AppUtil.sharingIntent(PullRequestDetailsActivity.this, body + "\n\n" + url);
					}

					@Override
					public void onOpenInBrowser(String url) {
						AppUtil.openUrlInBrowser(PullRequestDetailsActivity.this, url);
					}
				});

		sheet.show(getSupportFragmentManager(), "COMMENT_MENU");
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
		fetchReactions();
		fetchTimeline();
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
		String extension = FilenameUtils.getExtension(attachment.getName()).toLowerCase();
		boolean isImage =
				Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif")
						.contains(extension);

		int size = (int) (32 * getResources().getDisplayMetrics().density);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
		params.setMargins(0, 0, (int) (12 * getResources().getDisplayMetrics().density), 0);

		if (isImage) {
			com.google.android.material.imageview.ShapeableImageView imageView =
					new com.google.android.material.imageview.ShapeableImageView(this);
			imageView.setLayoutParams(params);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

			float cornerSize = getResources().getDimension(R.dimen.dimen8dp);
			imageView.setShapeAppearanceModel(
					imageView.getShapeAppearanceModel().toBuilder()
							.setAllCorners(CornerFamily.ROUNDED, cornerSize)
							.build());

			Glide.with(this)
					.load(attachment.getBrowserDownloadUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.error(R.drawable.ic_image)
					.centerCrop()
					.into(imageView);

			imageView.setOnClickListener(v -> openAttachmentPreview(attachment));
			return imageView;

		} else {
			com.google.android.material.card.MaterialCardView card =
					new com.google.android.material.card.MaterialCardView(this);
			card.setLayoutParams(params);
			card.setRadius(12);
			card.setStrokeWidth(0);
			card.setClickable(false);
			card.setFocusable(false);
			card.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);

			ImageView icon = new ImageView(this);
			int iconSize = (int) (36 * getResources().getDisplayMetrics().density);
			FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(iconSize, iconSize);
			iconParams.gravity = Gravity.CENTER;
			icon.setLayoutParams(iconParams);
			icon.setImageResource(FileIcon.getIconResource(attachment.getName(), "file"));

			icon.setClickable(true);
			icon.setFocusable(true);
			icon.setOnClickListener(v -> downloadAttachment(attachment));

			card.addView(icon);
			return card;
		}
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
		refreshTimeline();
	}
}
