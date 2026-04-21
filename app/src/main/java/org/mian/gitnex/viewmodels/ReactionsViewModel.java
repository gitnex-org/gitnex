package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.EditReactionOption;
import org.gitnex.tea4j.v2.models.GeneralUISettings;
import org.gitnex.tea4j.v2.models.Reaction;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ReactionsViewModel extends ViewModel {

	private static List<String> cachedAllowedReactions = null;
	private static List<String> cachedCustomEmojis = null;

	private final MutableLiveData<List<Reaction>> reactions = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isLoadingSettings = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<List<String>> allowedReactions = new MutableLiveData<>();
	private final MutableLiveData<List<String>> customEmojis = new MutableLiveData<>();

	public enum TargetType {
		ISSUE,
		COMMENT
	}

	private TargetType currentTargetType;
	private String repoOwner;
	private String repoName;
	private long targetId;

	public LiveData<List<Reaction>> getReactions() {
		return reactions;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getIsLoadingSettings() {
		return isLoadingSettings;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<List<String>> getAllowedReactions() {
		return allowedReactions;
	}

	public LiveData<List<String>> getCustomEmojis() {
		return customEmojis;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void initTarget(
			String repoOwner, String repoName, long targetId, TargetType targetType) {
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.targetId = targetId;
		this.currentTargetType = targetType;
	}

	public void fetchReactionSettings(Context ctx) {
		if (cachedAllowedReactions != null && cachedCustomEmojis != null) {
			allowedReactions.setValue(cachedAllowedReactions);
			customEmojis.setValue(cachedCustomEmojis);
			return;
		}

		isLoadingSettings.setValue(true);

		Call<GeneralUISettings> call = RetrofitClient.getApiInterface(ctx).getGeneralUISettings();
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<GeneralUISettings> call,
							@NonNull Response<GeneralUISettings> response) {
						isLoadingSettings.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							GeneralUISettings settings = response.body();

							cachedAllowedReactions =
									settings.getAllowedReactions() != null
											? settings.getAllowedReactions()
											: new ArrayList<>();
							cachedCustomEmojis =
									settings.getCustomEmojis() != null
											? settings.getCustomEmojis()
											: new ArrayList<>();

							allowedReactions.setValue(cachedAllowedReactions);
							customEmojis.setValue(cachedCustomEmojis);
						} else {
							allowedReactions.setValue(new ArrayList<>());
							customEmojis.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralUISettings> call, @NonNull Throwable t) {
						isLoadingSettings.setValue(false);
						error.setValue(t.getMessage());
						allowedReactions.setValue(new ArrayList<>());
						customEmojis.setValue(new ArrayList<>());
					}
				});
	}

	public void fetchReactions(Context ctx) {
		if (repoOwner == null || repoName == null) {
			error.setValue("Target not initialized");
			return;
		}

		isLoading.setValue(true);

		Call<List<Reaction>> call;
		if (currentTargetType == TargetType.ISSUE) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueGetIssueReactions(repoOwner, repoName, targetId, null, null);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueGetCommentReactions(repoOwner, repoName, targetId);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Reaction>> call,
							@NonNull Response<List<Reaction>> response) {
						isLoading.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							reactions.setValue(response.body());
						} else {
							reactions.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Reaction>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						error.setValue(t.getMessage());
						reactions.setValue(new ArrayList<>());
					}
				});
	}

	public void addReaction(Context ctx, String content) {
		if (repoOwner == null || repoName == null) {
			error.setValue("Target not initialized");
			return;
		}

		EditReactionOption option = new EditReactionOption();
		option.setContent(content);

		Call<Reaction> call;
		if (currentTargetType == TargetType.ISSUE) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issuePostIssueReaction(repoOwner, repoName, targetId, option);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issuePostCommentReaction(repoOwner, repoName, targetId, option);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Reaction> call, @NonNull Response<Reaction> response) {
						if (response.isSuccessful()) {
							fetchReactions(ctx);
						} else {
							error.setValue("Failed to add reaction");
						}
					}

					@Override
					public void onFailure(@NonNull Call<Reaction> call, @NonNull Throwable t) {
						error.setValue(t.getMessage());
					}
				});
	}

	public void removeReaction(Context ctx, String content) {
		if (repoOwner == null || repoName == null) {
			error.setValue("Target not initialized");
			return;
		}

		EditReactionOption option = new EditReactionOption();
		option.setContent(content);

		Call<Void> call;
		if (currentTargetType == TargetType.ISSUE) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueDeleteIssueReactionWithBody(
									repoOwner, repoName, targetId, option);
		} else {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueDeleteCommentReactionWithBody(
									repoOwner, repoName, targetId, option);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							fetchReactions(ctx);
						} else {
							error.setValue("Failed to remove reaction");
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						error.setValue(t.getMessage());
					}
				});
	}
}
