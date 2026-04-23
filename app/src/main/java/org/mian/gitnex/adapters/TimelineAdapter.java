package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.Reaction;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ItemTimelineCommentBinding;
import org.mian.gitnex.databinding.ItemTimelineEventBinding;
import org.mian.gitnex.databinding.ItemTimelinePushBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.TimelineItem;
import org.mian.gitnex.views.reactions.ReactionsManager;

/**
 * @author mmarif
 */
public class TimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_COMMENT = 0;
	private static final int TYPE_EVENT = 1;
	private static final int TYPE_PUSH = 2;

	private final Context context;
	private final String owner;
	private final String repo;
	private final String currentUser;
	private final RepositoryContext repositoryContext;
	private List<TimelineItem> items = new ArrayList<>();
	private final OnTimelineItemClickListener clickListener;
	private List<String> allowedReactions = new ArrayList<>();

	public void setAllowedReactions(List<String> allowed) {
		this.allowedReactions = allowed != null ? allowed : new ArrayList<>();
	}

	public interface OnTimelineItemClickListener {
		void onCommentMenuClick(TimelineItem comment, View anchor);

		void onCommentReactionClick(TimelineItem comment, String content, boolean isUserReaction);

		void onCommentAddReactionClick(TimelineItem comment, View anchor);

		void onCommentReactionLongClick(TimelineItem comment, String content, List<User> users);

		void onAttachmentClick(Attachment attachment);

		void onCommitClick(String sha);

		void onUserClick(String username);
	}

	public TimelineAdapter(
			Context context,
			String owner,
			String repo,
			String currentUser,
			OnTimelineItemClickListener listener) {
		this.context = context;
		this.owner = owner;
		this.repo = repo;
		this.currentUser = currentUser;
		this.clickListener = listener;
		this.repositoryContext = new RepositoryContext(owner, repo, context);
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setItems(List<TimelineItem> newItems) {
		this.items = newItems != null ? newItems : new ArrayList<>();
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		TimelineItem item = items.get(position);
		String type = item.getType();
		if ("comment".equalsIgnoreCase(type)) {
			return TYPE_COMMENT;
		} else if ("pull_push".equalsIgnoreCase(type)) {
			return TYPE_PUSH;
		} else {
			return TYPE_EVENT;
		}
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return switch (viewType) {
			case TYPE_COMMENT -> {
				ItemTimelineCommentBinding binding =
						ItemTimelineCommentBinding.inflate(inflater, parent, false);
				yield new CommentViewHolder(binding);
			}
			case TYPE_PUSH -> {
				ItemTimelinePushBinding binding =
						ItemTimelinePushBinding.inflate(inflater, parent, false);
				yield new PushViewHolder(binding);
			}
			default -> {
				ItemTimelineEventBinding binding =
						ItemTimelineEventBinding.inflate(inflater, parent, false);
				yield new EventViewHolder(binding);
			}
		};
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		TimelineItem item = items.get(position);
		if (holder instanceof CommentViewHolder) {
			((CommentViewHolder) holder).bind(item);
			((CommentViewHolder) holder)
					.binding
					.getRoot()
					.updateAppearance(position, getItemCount());
		} else if (holder instanceof PushViewHolder) {
			((PushViewHolder) holder).bind(item);
			((PushViewHolder) holder).binding.getRoot().updateAppearance(position, getItemCount());
		} else if (holder instanceof EventViewHolder) {
			((EventViewHolder) holder).bind(item);
			((EventViewHolder) holder).binding.getRoot().updateAppearance(position, getItemCount());
		}
	}

	@Override
	public int getItemCount() {
		return items.size();
	}

	public void updateCommentReactions(long commentId, List<Reaction> reactions) {
		for (int i = 0; i < items.size(); i++) {
			TimelineItem item = items.get(i);
			if (item.getId() == commentId) {
				item.setReactions(reactions);
				notifyItemChanged(i);
				break;
			}
		}
	}

	public class CommentViewHolder extends RecyclerView.ViewHolder {
		public final ItemTimelineCommentBinding binding;
		private TimelineItem comment;
		private ReactionsManager reactionsManager;

		CommentViewHolder(ItemTimelineCommentBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.commentMenu.setOnClickListener(
					v -> {
						if (clickListener != null && comment != null) {
							clickListener.onCommentMenuClick(comment, binding.commentMenu);
						}
					});

			binding.commentAvatar.setOnClickListener(
					v -> {
						if (clickListener != null && comment != null && comment.getUser() != null) {
							clickListener.onUserClick(comment.getUser().getLogin());
						}
					});
		}

		@SuppressLint("SetTextI18n")
		void bind(TimelineItem item) {
			this.comment = item;

			if (item.getUser() != null) {
				Glide.with(context)
						.load(item.getUser().getAvatarUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.loader_animated)
						.error(
								AvatarGenerator.getLetterAvatar(
										context, item.getUser().getLogin(), 32))
						.centerCrop()
						.into(binding.commentAvatar);
				binding.commentAuthor.setText(item.getUser().getLogin());
			}

			if (item.getCreatedAt() != null) {
				binding.commentTime.setText(
						TimeHelper.formatTime(item.getCreatedAt(), Locale.getDefault()));
				binding.commentTime.setOnClickListener(
						v -> {
							Toasty.show(
									context,
									TimeHelper.getFullDateTime(
											item.getCreatedAt(), Locale.getDefault()));
						});
			}

			if (item.getBody() != null && !item.getBody().isEmpty()) {
				binding.commentBody.setVisibility(View.VISIBLE);
				Markdown.render(context, item.getBody(), binding.commentBody, repositoryContext);
			} else {
				binding.commentBody.setVisibility(View.GONE);
			}

			setupReactionsManager(item);
			displayAttachments(item.getAttachments());
		}

		private void setupReactionsManager(TimelineItem item) {
			if (reactionsManager == null) {
				reactionsManager =
						new ReactionsManager(
								context,
								binding.commentReactions,
								binding.commentAddReaction,
								new ReactionsManager.ReactionListener() {
									@Override
									public void onAddReaction(String content) {
										if (clickListener != null) {
											clickListener.onCommentReactionClick(
													comment, content, false);
										}
									}

									@Override
									public void onRemoveReaction(String content) {
										if (clickListener != null) {
											clickListener.onCommentReactionClick(
													comment, content, true);
										}
									}

									@Override
									public void onShowUsers(
											String emoji, String content, List<User> users) {
										if (clickListener != null) {
											clickListener.onCommentReactionLongClick(
													comment, content, users);
										}
									}

									@Override
									public void onReactionsLoaded() {}
								},
								currentUser);
				reactionsManager.setReactionSettings(allowedReactions, new ArrayList<>());
			}

			reactionsManager.setReactions(item.getReactions());
		}

		private void displayAttachments(List<Attachment> attachments) {
			binding.commentAttachments.removeAllViews();

			if (attachments == null || attachments.isEmpty()) {
				binding.commentAttachments.setVisibility(View.GONE);
				return;
			}

			binding.commentAttachments.setVisibility(View.VISIBLE);

			for (Attachment attachment : attachments) {
				View attachmentView = createAttachmentView(attachment);
				binding.commentAttachments.addView(attachmentView);
			}
		}

		private View createAttachmentView(Attachment attachment) {
			String extension = FilenameUtils.getExtension(attachment.getName()).toLowerCase();
			boolean isImage =
					Arrays.asList("bmp", "gif", "jpg", "jpeg", "png", "webp", "heic", "heif")
							.contains(extension);

			int size = (int) (32 * context.getResources().getDisplayMetrics().density);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
			params.setMargins(
					0, 0, (int) (12 * context.getResources().getDisplayMetrics().density), 0);

			if (isImage) {
				ShapeableImageView imageView = new ShapeableImageView(context);
				imageView.setLayoutParams(params);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

				float cornerSize = context.getResources().getDimension(R.dimen.dimen8dp);
				imageView.setShapeAppearanceModel(
						imageView.getShapeAppearanceModel().toBuilder()
								.setAllCorners(CornerFamily.ROUNDED, cornerSize)
								.build());

				Glide.with(context)
						.load(attachment.getBrowserDownloadUrl())
						.diskCacheStrategy(DiskCacheStrategy.ALL)
						.placeholder(R.drawable.loader_animated)
						.error(R.drawable.ic_image)
						.centerCrop()
						.into(imageView);

				imageView.setOnClickListener(
						v -> {
							if (clickListener != null) {
								clickListener.onAttachmentClick(attachment);
							}
						});
				return imageView;

			} else {
				com.google.android.material.card.MaterialCardView card =
						new com.google.android.material.card.MaterialCardView(context);
				card.setLayoutParams(params);
				card.setRadius(12);
				card.setStrokeWidth(0);
				card.setClickable(false);
				card.setFocusable(false);
				card.setCardBackgroundColor(android.graphics.Color.TRANSPARENT);

				ImageView icon = new ImageView(context);
				int iconSize = (int) (36 * context.getResources().getDisplayMetrics().density);
				FrameLayout.LayoutParams iconParams =
						new FrameLayout.LayoutParams(iconSize, iconSize);
				iconParams.gravity = Gravity.CENTER;
				icon.setLayoutParams(iconParams);
				icon.setImageResource(FileIcon.getIconResource(attachment.getName(), "file"));

				icon.setClickable(true);
				icon.setFocusable(true);
				icon.setOnClickListener(
						v -> {
							if (clickListener != null) {
								clickListener.onAttachmentClick(attachment);
							}
						});

				card.addView(icon);
				return card;
			}
		}

		public TimelineItem getComment() {
			return comment;
		}
	}

	class EventViewHolder extends RecyclerView.ViewHolder {
		private final ItemTimelineEventBinding binding;

		EventViewHolder(ItemTimelineEventBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		@SuppressLint("SetTextI18n")
		void bind(TimelineItem item) {
			String type = item.getType();
			String username = item.getUser() != null ? item.getUser().getLogin() : "";
			String timeAgo =
					item.getCreatedAt() != null
							? TimeHelper.formatTime(item.getCreatedAt(), Locale.getDefault())
							: "";

			int iconRes = R.drawable.ic_history;
			String text = "";

			switch (type.toLowerCase()) {
				case "label":
					iconRes = R.drawable.ic_tag;
					if (item.getLabel() != null) {
						text =
								context.getString(
										R.string.timeline_added_label,
										username,
										item.getLabel().getName(),
										timeAgo);
					}
					break;
				case "milestone":
					iconRes = R.drawable.ic_milestone;
					if (item.getMilestone() != null) {
						text =
								context.getString(
										R.string.timeline_added_milestone,
										username,
										item.getMilestone().getTitle(),
										timeAgo);
					} else if (item.getOldMilestone() != null) {
						text =
								context.getString(
										R.string.timeline_removed_milestone,
										username,
										item.getOldMilestone().getTitle(),
										timeAgo);
					}
					break;
				case "assignees":
					iconRes = R.drawable.ic_person;
					if (item.getAssignee() != null) {
						if (item.isRemovedAssignee()) {
							text =
									context.getString(
											R.string.timeline_unassigned,
											username,
											item.getAssignee().getLogin(),
											timeAgo);
						} else {
							text =
									context.getString(
											R.string.timeline_assigned,
											username,
											item.getAssignee().getLogin(),
											timeAgo);
						}
					}
					break;
				case "review_request":
					iconRes = R.drawable.ic_followers;
					if (item.getAssignee() != null) {
						text =
								context.getString(
										R.string.timeline_review_requested,
										username,
										item.getAssignee().getLogin(),
										timeAgo);
					}
					break;
				case "change_title":
					iconRes = R.drawable.ic_edit;
					if (item.getOldTitle() != null && item.getNewTitle() != null) {
						text =
								context.getString(
										R.string.timeline_changed_title,
										username,
										item.getOldTitle(),
										item.getNewTitle(),
										timeAgo);
					}
					break;
				case "close":
				case "reopen":
					iconRes = R.drawable.ic_issue_closed;
					text =
							context.getString(
									type.equals("close")
											? R.string.timeline_closed
											: R.string.timeline_reopened,
									username,
									timeAgo);
					break;
				default:
					String readableType = getReadableEventType(type);
					text = username + " " + readableType + " • " + timeAgo;
					break;
			}

			binding.timelineIcon.setImageResource(iconRes);
			binding.timelineIcon.setColorFilter(
					AppUtil.getColorFromAttribute(context, R.attr.iconsColor));
			binding.timelineText.setText(text);
		}

		private String getReadableEventType(String type) {
			return switch (type.toLowerCase()) {
				case "add_dependency" -> "added a dependency";
				case "remove_dependency" -> "removed a dependency";
				case "lock" -> "locked the conversation";
				case "unlock" -> "unlocked the conversation";
				case "pin" -> "pinned this";
				case "merge_pull" -> "merged this";
				case "review" -> "left a review";
				case "dismiss_review" -> "dismissed a review";
				case "change_target_branch" -> "changed the target branch";
				case "delete_branch" -> "deleted a branch";
				case "added_deadline" -> "added a due date";
				case "modified_deadline" -> "modified the due date";
				case "removed_deadline" -> "removed the due date";
				case "start_tracking" -> "started time tracking";
				case "stop_tracking" -> "stopped time tracking";
				case "cancel_tracking" -> "canceled time tracking";
				case "add_time_manual" -> "added spent time";
				case "delete_time_manual" -> "deleted spent time";
				case "project", "project_board" -> "modified project";
				case "issue_ref", "pull_ref", "comment_ref", "change_issue_ref" ->
						"added a reference";
				case "commit_ref" -> "referenced a commit";
				default -> type.replace("_", " ");
			};
		}
	}

	class PushViewHolder extends RecyclerView.ViewHolder {
		private final ItemTimelinePushBinding binding;

		PushViewHolder(ItemTimelinePushBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		@SuppressLint("SetTextI18n")
		void bind(TimelineItem item) {
			String username = item.getUser() != null ? item.getUser().getLogin() : "";
			String timeAgo =
					item.getCreatedAt() != null
							? TimeHelper.formatTime(item.getCreatedAt(), Locale.getDefault())
							: "";

			List<String> commitIds = item.getCommitIds();
			if (commitIds != null && !commitIds.isEmpty()) {
				binding.pushHeader.setText(
						context.getString(
								R.string.timeline_pushed_commits,
								username,
								commitIds.size(),
								timeAgo));

				binding.pushCommits.removeAllViews();
				for (String sha : commitIds) {
					String shortSha = sha.length() > 7 ? sha.substring(0, 7) : sha;

					TextView commitView = new TextView(context);
					commitView.setText(shortSha);
					commitView.setTextColor(ContextCompat.getColor(context, R.color.lightBlue));
					commitView.setTypeface(Typeface.MONOSPACE);
					commitView.setPadding(
							0,
							(int) (4 * context.getResources().getDisplayMetrics().density),
							0,
							0);
					commitView.setOnClickListener(
							v -> {
								if (clickListener != null) {
									clickListener.onCommitClick(sha);
								}
							});

					binding.pushCommits.addView(commitView);
				}
			} else {
				binding.pushHeader.setText(username + " pushed commits • " + timeAgo);
			}
		}
	}
}
