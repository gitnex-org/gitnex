package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Attachment;
import org.gitnex.tea4j.v2.models.Comment;
import org.gitnex.tea4j.v2.models.CreateIssueCommentOption;
import org.gitnex.tea4j.v2.models.EditIssueCommentOption;
import org.gitnex.tea4j.v2.models.EditReactionOption;
import org.gitnex.tea4j.v2.models.GeneralUISettings;
import org.gitnex.tea4j.v2.models.Reaction;
import org.gitnex.tea4j.v2.models.TimelineComment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.TimelineItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class TimelineViewModel extends ViewModel {

	private final MutableLiveData<List<TimelineItem>> timeline =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Comment> submittedComment = new MutableLiveData<>();
	private final MutableLiveData<Comment> editedComment = new MutableLiveData<>();
	private final MutableLiveData<Boolean> commentDeleted = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isSubmitting = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
	private final MutableLiveData<String> actionError = new MutableLiveData<>();
	private final MutableLiveData<List<String>> allowedReactions = new MutableLiveData<>();
	private final MutableLiveData<List<String>> customEmojis = new MutableLiveData<>();
	private final MutableLiveData<Pair<Long, List<Reaction>>> commentReactionsUpdate =
			new MutableLiveData<>();

	private boolean isLastPage = false;
	private int totalCount = -1;
	private String currentOwner;
	private String currentRepo;
	private long currentPrNumber;

	public LiveData<List<TimelineItem>> getTimeline() {
		return timeline;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsRefreshing() {
		return isRefreshing;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Comment> getSubmittedComment() {
		return submittedComment;
	}

	public LiveData<Comment> getEditedComment() {
		return editedComment;
	}

	public LiveData<Boolean> getCommentDeleted() {
		return commentDeleted;
	}

	public LiveData<Boolean> getIsSubmitting() {
		return isSubmitting;
	}

	public LiveData<Boolean> getIsDeleting() {
		return isDeleting;
	}

	public LiveData<String> getActionError() {
		return actionError;
	}

	public LiveData<List<String>> getAllowedReactions() {
		return allowedReactions;
	}

	public LiveData<List<String>> getCustomEmojis() {
		return customEmojis;
	}

	public LiveData<Pair<Long, List<Reaction>>> getCommentReactionsUpdate() {
		return commentReactionsUpdate;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void clearSubmittedComment() {
		submittedComment.setValue(null);
	}

	public void clearEditedComment() {
		editedComment.setValue(null);
	}

	public void clearCommentDeleted() {
		commentDeleted.setValue(null);
	}

	public void clearActionError() {
		actionError.setValue(null);
	}

	public void resetPagination() {
		isLastPage = false;
		totalCount = -1;
		timeline.setValue(new ArrayList<>());
	}

	public void init(String owner, String repo, long prNumber) {
		this.currentOwner = owner;
		this.currentRepo = repo;
		this.currentPrNumber = prNumber;
	}

	public void fetchReactionSettings(Context ctx) {
		Call<GeneralUISettings> call = RetrofitClient.getApiInterface(ctx).getGeneralUISettings();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<GeneralUISettings> call,
							@NonNull Response<GeneralUISettings> response) {
						if (response.isSuccessful() && response.body() != null) {
							allowedReactions.setValue(response.body().getAllowedReactions());
							customEmojis.setValue(response.body().getCustomEmojis());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralUISettings> call, @NonNull Throwable t) {}
				});
	}

	public void fetchTimeline(Context ctx, int page, int limit, boolean isRefresh) {
		if (currentOwner == null || currentRepo == null) return;
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;
		if (!isRefresh && isLastPage) return;

		if (isRefresh) {
			isRefreshing.setValue(true);
		} else {
			isLoading.setValue(true);
		}
		error.setValue(null);

		Call<List<TimelineComment>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetCommentsAndTimeline(
								currentOwner,
								currentRepo,
								currentPrNumber,
								null,
								page,
								limit,
								null);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<TimelineComment>> call,
							@NonNull Response<List<TimelineComment>> response) {

						if (response.isSuccessful() && response.body() != null) {
							String totalHeader = response.headers().get("x-total-count");
							if (totalHeader != null) {
								totalCount = Integer.parseInt(totalHeader);
							}

							List<TimelineItem> items = convertToTimelineItems(response.body());
							fetchEmbeddedData(ctx, items, isRefresh);
						} else {
							if (response.body() == null) {
								error.setValue(ctx.getString(R.string.timeline_empty));
							}
							if (response.code() == 404 && isRefresh) {
								timeline.setValue(new ArrayList<>());
							}
							isLastPage = true;
							isLoading.setValue(false);
							isRefreshing.setValue(false);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<TimelineComment>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						isRefreshing.setValue(false);
						error.setValue(t.getMessage());
					}
				});
	}

	private List<TimelineItem> convertToTimelineItems(List<TimelineComment> comments) {
		List<TimelineItem> items = new ArrayList<>();
		for (TimelineComment tc : comments) {
			TimelineItem item = new TimelineItem();
			item.setId(tc.getId());
			item.setType(tc.getType());
			item.setUser(tc.getUser());
			item.setBody(tc.getBody());
			item.setCreatedAt(tc.getCreatedAt());
			item.setUpdatedAt(tc.getUpdatedAt());
			item.setHtmlUrl(tc.getHtmlUrl());

			item.setLabel(tc.getLabel());
			item.setMilestone(tc.getMilestone());
			item.setOldMilestone(tc.getOldMilestone());
			item.setAssignee(tc.getAssignee());
			item.setRemovedAssignee(tc.isRemovedAssignee());
			item.setOldTitle(tc.getOldTitle());
			item.setNewTitle(tc.getNewTitle());
			item.setOldRef(tc.getOldRef());
			item.setNewRef(tc.getNewRef());

			if ("pull_push".equalsIgnoreCase(tc.getType()) && tc.getBody() != null) {
				try {
					JSONObject body = new JSONObject(tc.getBody());
					JSONArray commitIds = body.getJSONArray("commit_ids");
					List<String> commits = new ArrayList<>();
					for (int i = 0; i < commitIds.length(); i++) {
						commits.add(commitIds.getString(i));
					}
					item.setCommitIds(commits);
				} catch (Exception ignored) {
				}
			}

			items.add(item);
		}
		return items;
	}

	private void fetchEmbeddedData(Context ctx, List<TimelineItem> items, boolean isRefresh) {
		fetchNextEmbedded(ctx, items, 0, isRefresh);
	}

	private void fetchNextEmbedded(
			Context ctx, List<TimelineItem> items, int index, boolean isRefresh) {
		if (index >= items.size()) {
			List<TimelineItem> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(
									timeline.getValue() != null
											? timeline.getValue()
											: new ArrayList<>());
			currentList.addAll(items);
			timeline.setValue(currentList);
			isLastPage =
					items.size() < 20 || (totalCount != -1 && currentList.size() >= totalCount);
			isLoading.setValue(false);
			isRefreshing.setValue(false);
			return;
		}

		TimelineItem item = items.get(index);
		if (!"comment".equalsIgnoreCase(item.getType())) {
			fetchNextEmbedded(ctx, items, index + 1, isRefresh);
			return;
		}

		Call<List<Reaction>> reactionCall =
				RetrofitClient.getApiInterface(ctx)
						.issueGetCommentReactions(currentOwner, currentRepo, item.getId());

		reactionCall.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Reaction>> call,
							@NonNull Response<List<Reaction>> response) {
						if (response.isSuccessful() && response.body() != null) {
							item.setReactions(response.body());
						}

						Call<List<Attachment>> attachmentCall =
								RetrofitClient.getApiInterface(ctx)
										.issueListIssueCommentAttachments(
												currentOwner, currentRepo, item.getId());

						attachmentCall.enqueue(
								new Callback<>() {
									@Override
									public void onResponse(
											@NonNull Call<List<Attachment>> call,
											@NonNull Response<List<Attachment>> response) {
										if (response.isSuccessful() && response.body() != null) {
											item.setAttachments(response.body());
										}
										fetchNextEmbedded(ctx, items, index + 1, isRefresh);
									}

									@Override
									public void onFailure(
											@NonNull Call<List<Attachment>> call,
											@NonNull Throwable t) {
										fetchNextEmbedded(ctx, items, index + 1, isRefresh);
									}
								});
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Reaction>> call, @NonNull Throwable t) {
						fetchNextEmbedded(ctx, items, index + 1, isRefresh);
					}
				});
	}

	public void addCommentReaction(Context ctx, long commentId, String content) {
		EditReactionOption option = new EditReactionOption();
		option.setContent(content);

		Call<Reaction> call =
				RetrofitClient.getApiInterface(ctx)
						.issuePostCommentReaction(currentOwner, currentRepo, commentId, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Reaction> call, @NonNull Response<Reaction> response) {
						if (response.isSuccessful()) {
							new android.os.Handler(android.os.Looper.getMainLooper())
									.postDelayed(
											() -> {
												refreshCommentReactions(ctx, commentId);
											},
											300);
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Reaction> call, @NonNull Throwable t) {
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void removeCommentReaction(Context ctx, long commentId, String content) {
		EditReactionOption option = new EditReactionOption();
		option.setContent(content);

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.issueDeleteCommentReactionWithBody(
								currentOwner, currentRepo, commentId, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							new android.os.Handler(android.os.Looper.getMainLooper())
									.postDelayed(
											() -> {
												refreshCommentReactions(ctx, commentId);
											},
											300);
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void refreshCommentReactions(Context ctx, long commentId) {
		Call<List<Reaction>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetCommentReactions(currentOwner, currentRepo, commentId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Reaction>> call,
							@NonNull Response<List<Reaction>> response) {
						if (response.isSuccessful()) {
							List<Reaction> reactions = response.body();
							if (reactions == null) {
								reactions = new ArrayList<>();
							}

							List<TimelineItem> current = timeline.getValue();
							if (current != null) {
								for (TimelineItem item : current) {
									if (item.getId() == commentId) {
										item.setReactions(reactions);
										break;
									}
								}
							}

							commentReactionsUpdate.setValue(new Pair<>(commentId, reactions));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Reaction>> call, @NonNull Throwable t) {
						Log.e("TimelineVM", "Failed to refresh reactions", t);
					}
				});
	}

	public void addComment(Context ctx, String body) {
		isSubmitting.setValue(true);
		actionError.setValue(null);

		CreateIssueCommentOption option = new CreateIssueCommentOption();
		option.setBody(body);

		Call<Comment> call =
				RetrofitClient.getApiInterface(ctx)
						.issueCreateComment(currentOwner, currentRepo, currentPrNumber, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
						isSubmitting.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							submittedComment.setValue(response.body());
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
						isSubmitting.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void editComment(Context ctx, long commentId, String body) {
		isSubmitting.setValue(true);
		actionError.setValue(null);

		EditIssueCommentOption option = new EditIssueCommentOption();
		option.setBody(body);

		Call<Comment> call =
				RetrofitClient.getApiInterface(ctx)
						.issueEditComment(currentOwner, currentRepo, commentId, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Comment> call, @NonNull Response<Comment> response) {
						isSubmitting.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							editedComment.setValue(response.body());
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Comment> call, @NonNull Throwable t) {
						isSubmitting.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void deleteComment(Context ctx, long commentId) {
		isDeleting.setValue(true);
		actionError.setValue(null);

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.issueDeleteComment(currentOwner, currentRepo, commentId);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isDeleting.setValue(false);
						if (response.isSuccessful()) {
							commentDeleted.setValue(true);
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isDeleting.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}
}
