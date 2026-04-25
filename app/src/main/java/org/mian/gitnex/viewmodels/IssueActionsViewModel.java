package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class IssueActionsViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isTogglingState = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isTogglingPin = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isTogglingSubscribe = new MutableLiveData<>(false);
	private final MutableLiveData<String> actionMessage = new MutableLiveData<>();
	private final MutableLiveData<String> actionError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isSubscribed = new MutableLiveData<>(false);

	public LiveData<Boolean> getIsTogglingState() {
		return isTogglingState;
	}

	public LiveData<Boolean> getIsTogglingPin() {
		return isTogglingPin;
	}

	public LiveData<Boolean> getIsTogglingSubscribe() {
		return isTogglingSubscribe;
	}

	public LiveData<String> getActionMessage() {
		return actionMessage;
	}

	public LiveData<String> getActionError() {
		return actionError;
	}

	public LiveData<Boolean> getIsSubscribed() {
		return isSubscribed;
	}

	public void clearActionMessage() {
		actionMessage.setValue(null);
	}

	public void clearActionError() {
		actionError.setValue(null);
	}

	public void toggleState(
			Context ctx,
			String owner,
			String repo,
			long issueIndex,
			String currentState,
			boolean isPr) {
		isTogglingState.setValue(true);
		actionError.setValue(null);

		String newState = "closed".equals(currentState) ? "open" : "closed";
		EditIssueOption option = new EditIssueOption();
		option.setState(newState);

		Call<Issue> call =
				RetrofitClient.getApiInterface(ctx).issueEditIssue(owner, repo, issueIndex, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						isTogglingState.setValue(false);
						if (response.isSuccessful()) {
							if (isPr) {
								actionMessage.setValue(
										"closed".equals(newState)
												? ctx.getString(R.string.prClosed)
												: ctx.getString(R.string.prReopened));
							} else {
								actionMessage.setValue(
										"closed".equals(newState)
												? ctx.getString(R.string.issueStateClosed)
												: ctx.getString(R.string.issueStateReopened));
							}
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else if (response.code() == 403) {
							actionError.setValue(ctx.getString(R.string.authorizeError));
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						isTogglingState.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void togglePin(
			Context ctx, String owner, String repo, long issueIndex, boolean isPinned) {
		isTogglingPin.setValue(true);
		actionError.setValue(null);

		Call<Void> call =
				isPinned
						? RetrofitClient.getApiInterface(ctx).unpinIssue(owner, repo, issueIndex)
						: RetrofitClient.getApiInterface(ctx).pinIssue(owner, repo, issueIndex);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isTogglingPin.setValue(false);
						if (response.isSuccessful()) {
							actionMessage.setValue(
									isPinned
											? ctx.getString(R.string.issue_unpinned)
											: ctx.getString(R.string.issue_pinned));
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isTogglingPin.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}

	public void checkSubscription(Context ctx, String owner, String repo, long issueIndex) {
		RetrofitClient.getApiInterface(ctx)
				.issueCheckSubscription(owner, repo, issueIndex)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<WatchInfo> call,
									@NonNull Response<WatchInfo> response) {
								if (response.isSuccessful() && response.body() != null) {
									isSubscribed.setValue(response.body().isSubscribed());
								} else {
									isSubscribed.setValue(false);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<WatchInfo> call, @NonNull Throwable t) {
								isSubscribed.setValue(false);
							}
						});
	}

	public void toggleSubscribe(
			Context ctx,
			String owner,
			String repo,
			long issueIndex,
			String currentUser,
			boolean isSubscribed) {
		isTogglingSubscribe.setValue(true);
		actionError.setValue(null);

		Call<Void> call =
				isSubscribed
						? RetrofitClient.getApiInterface(ctx)
								.issueDeleteSubscription(owner, repo, issueIndex, currentUser)
						: RetrofitClient.getApiInterface(ctx)
								.issueAddSubscription(owner, repo, issueIndex, currentUser);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isTogglingSubscribe.setValue(false);
						if (response.isSuccessful()) {
							IssueActionsViewModel.this.isSubscribed.setValue(!isSubscribed);
							actionMessage.setValue(
									isSubscribed
											? ctx.getString(R.string.unsubscribedSuccessfully)
											: ctx.getString(R.string.subscribedSuccessfully));
						} else if (response.code() == 401) {
							actionError.setValue("UNAUTHORIZED");
						} else {
							actionError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isTogglingSubscribe.setValue(false);
						actionError.setValue(t.getMessage());
					}
				});
	}
}
