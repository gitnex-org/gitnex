package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import okhttp3.ResponseBody;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.WatchInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.topics.Topics;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.languagestatistics.LanguageColor;
import org.mian.gitnex.helpers.languagestatistics.SeekbarItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoryDetailsViewModel extends ViewModel {

	private final MutableLiveData<Repository> repoData = new MutableLiveData<>();
	private final MutableLiveData<List<String>> topicsData = new MutableLiveData<>();
	private final MutableLiveData<Map<String, Long>> languagesData = new MutableLiveData<>();
	private final MutableLiveData<ArrayList<SeekbarItem>> processedLanguages =
			new MutableLiveData<>();
	private final MutableLiveData<String> readmeData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> actionSuccessEvent = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isStarred = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isWatched = new MutableLiveData<>(false);
	private final MutableLiveData<Long> localStarCount = new MutableLiveData<>();
	private final MutableLiveData<Long> localWatchCount = new MutableLiveData<>();

	public LiveData<Repository> getRepoData() {
		return repoData;
	}

	public LiveData<List<String>> getTopicsData() {
		return topicsData;
	}

	public LiveData<Map<String, Long>> getLanguagesData() {
		return languagesData;
	}

	public LiveData<ArrayList<SeekbarItem>> getProcessedLanguages() {
		return processedLanguages;
	}

	public LiveData<String> getReadmeData() {
		return readmeData;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<Integer> getActionSuccessEvent() {
		return actionSuccessEvent;
	}

	public LiveData<Boolean> getIsStarred() {
		return isStarred;
	}

	public LiveData<Boolean> getIsWatched() {
		return isWatched;
	}

	public LiveData<Long> getLocalStarCount() {
		return localStarCount;
	}

	public LiveData<Long> getLocalWatchCount() {
		return localWatchCount;
	}

	private Boolean previousStarredState = null;
	private Boolean previousWatchedState = null;

	public void loadRepositoryDetails(Context ctx, String owner, String name, String branch) {
		isLoading.setValue(true);
		fetchRepoLanguages(ctx, owner, name);
		fetchRepoTopics(ctx, owner, name);
		fetchReadme(ctx, owner, name, branch);
	}

	public void fetchRepository(Context ctx, String owner, String name) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.repoGet(owner, name)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Repository> call,
									@NonNull Response<Repository> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									repoData.setValue(response.body());
								} else {
									errorMessage.setValue(
											ctx.getString(R.string.repository_not_exist));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Repository> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void fetchRepoLanguages(Context ctx, String owner, String name) {
		RetrofitClient.getApiInterface(ctx)
				.repoGetLanguages(owner, name)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Map<String, Long>> call,
									@NonNull Response<Map<String, Long>> response) {
								if (response.isSuccessful()
										&& response.body() != null
										&& !response.body().isEmpty()) {
									languagesData.setValue(response.body());

									ArrayList<SeekbarItem> seekbarItemList = new ArrayList<>();
									float totalSpan =
											(float)
													response.body().values().stream()
															.mapToDouble(a -> a)
															.sum();

									for (Map.Entry<String, Long> entry :
											response.body().entrySet()) {
										SeekbarItem seekbarItem = new SeekbarItem();
										seekbarItem.progressItemPercentage =
												(entry.getValue() / totalSpan) * 100;
										seekbarItem.color =
												LanguageColor.languageColor(entry.getKey());
										seekbarItemList.add(seekbarItem);
									}
									processedLanguages.setValue(seekbarItemList);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Map<String, Long>> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void fetchRepoTopics(Context ctx, String owner, String name) {
		ApiRetrofitClient.getInstance(ctx)
				.getRepoTopics(owner, name, 1, 100)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Topics> call,
									@NonNull Response<Topics> response) {
								if (response.isSuccessful() && response.body() != null) {
									topicsData.setValue(response.body().getTopics());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Topics> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void fetchReadme(Context ctx, String owner, String name, String branch) {
		RetrofitClient.getWebInterface(ctx)
				.getFileContents(owner, name, branch, "README.md")
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ResponseBody> call,
									@NonNull Response<ResponseBody> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									try {
										readmeData.setValue(response.body().string());
									} catch (IOException e) {
										readmeData.setValue(null);
									}
								} else {
									readmeData.setValue(null);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								readmeData.setValue(null);
							}
						});
	}

	public void addNewTopic(Context ctx, String owner, String name, String topic) {
		isActionLoading.setValue(true);
		ApiRetrofitClient.getInstance(ctx)
				.addRepoTopic(owner, name, topic)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isActionLoading.setValue(false);
								if (response.code() == 204) {

									actionSuccessEvent.setValue(R.string.topicAddedSuccessfully);
									fetchRepoTopics(ctx, owner, name);
								} else {
									errorMessage.setValue(ctx.getString(R.string.errorAddingTopic));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void deleteTopic(Context ctx, String owner, String name, String topic) {
		isActionLoading.setValue(true);
		ApiRetrofitClient.getInstance(ctx)
				.deleteRepoTopic(owner, name, topic)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isActionLoading.setValue(false);
								if (response.code() == 204) {
									actionSuccessEvent.setValue(R.string.topicDeletedSuccessfully);
									fetchRepoTopics(ctx, owner, name);
								} else {
									errorMessage.setValue(
											ctx.getString(R.string.errorDeletingTopic));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void consumeActionEvents() {
		actionSuccessEvent.setValue(null);
		errorMessage.setValue(null);
	}

	public void setInitialCounts(long stars, long watchers) {
		if (localStarCount.getValue() == null) localStarCount.setValue(stars);
		if (localWatchCount.getValue() == null) localWatchCount.setValue(watchers);
	}

	public void checkRepoStatus(Context ctx, String owner, String name) {
		RetrofitClient.getApiInterface(ctx)
				.userCurrentCheckStarring(owner, name)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								boolean starred = response.code() == 204;
								previousStarredState = starred;
								isStarred.setValue(starred);
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
						});

		RetrofitClient.getApiInterface(ctx)
				.userCurrentCheckSubscription(owner, name)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<WatchInfo> call,
									@NonNull Response<WatchInfo> response) {
								if (response.isSuccessful() && response.body() != null) {
									boolean watched = response.body().isSubscribed();
									previousWatchedState = watched;
									isWatched.setValue(watched);
								} else {
									previousWatchedState = false;
									isWatched.setValue(false);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<WatchInfo> call, @NonNull Throwable t) {}
						});
	}

	public void toggleStar(Context ctx, String owner, String name) {
		boolean currentStatus = isStarred.getValue() != null && isStarred.getValue();
		isActionLoading.setValue(true);

		Call<Void> call;
		if (currentStatus) {
			call = RetrofitClient.getApiInterface(ctx).userCurrentDeleteStar(owner, name);
		} else {
			call = RetrofitClient.getApiInterface(ctx).userCurrentPutStar(owner, name);
		}

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isActionLoading.setValue(false);
						if (response.isSuccessful() && response.code() == 204) {
							boolean newStatus = !currentStatus;
							if (previousStarredState != null && previousStarredState != newStatus) {
								updateStarCountLocal(newStatus);
							}

							previousStarredState = newStatus;
							isStarred.setValue(newStatus);
							actionSuccessEvent.setValue(
									currentStatus
											? R.string.unStarRepositorySuccess
											: R.string.starRepositorySuccess);
						} else {
							handleActionError(ctx, response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						errorMessage.setValue(ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void toggleWatch(Context ctx, String owner, String name) {
		boolean currentStatus = isWatched.getValue() != null && isWatched.getValue();
		isActionLoading.setValue(true);

		if (currentStatus) {
			RetrofitClient.getApiInterface(ctx)
					.userCurrentDeleteSubscription(owner, name)
					.enqueue(
							new Callback<>() {
								@Override
								public void onResponse(
										@NonNull Call<Void> call,
										@NonNull Response<Void> response) {
									isActionLoading.setValue(false);
									if (response.code() == 204) {
										boolean newStatus = false;

										if (previousWatchedState != null
												&& previousWatchedState != newStatus) {
											updateWatchCountLocal(newStatus);
										}

										previousWatchedState = newStatus;
										isWatched.setValue(newStatus);
										actionSuccessEvent.setValue(
												R.string.unWatchRepositorySuccess);
									} else {
										handleActionError(ctx, response.code());
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<Void> call, @NonNull Throwable t) {
									isActionLoading.setValue(false);
									errorMessage.setValue(
											ctx.getString(R.string.genericServerResponseError));
								}
							});
		} else {
			RetrofitClient.getApiInterface(ctx)
					.userCurrentPutSubscription(owner, name)
					.enqueue(
							new Callback<>() {
								@Override
								public void onResponse(
										@NonNull Call<WatchInfo> call,
										@NonNull Response<WatchInfo> response) {
									isActionLoading.setValue(false);
									if (response.isSuccessful() && response.code() == 200) {
										boolean newStatus = true;

										if (previousWatchedState != null
												&& previousWatchedState != newStatus) {
											updateWatchCountLocal(newStatus);
										}

										previousWatchedState = newStatus;
										isWatched.setValue(newStatus);
										actionSuccessEvent.setValue(
												R.string.watchRepositorySuccess);
									} else {
										handleActionError(ctx, response.code());
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<WatchInfo> call, @NonNull Throwable t) {
									isActionLoading.setValue(false);
									errorMessage.setValue(
											ctx.getString(R.string.genericServerResponseError));
								}
							});
		}
	}

	private void updateStarCountLocal(boolean isNowStarred) {
		Long current = localStarCount.getValue();
		if (current != null) {
			localStarCount.setValue(isNowStarred ? current + 1 : Math.max(0, current - 1));
		}
	}

	private void updateWatchCountLocal(boolean isNowWatched) {
		Long current = localWatchCount.getValue();
		if (current != null) {
			localWatchCount.setValue(isNowWatched ? current + 1 : Math.max(0, current - 1));
		}
	}

	private void handleActionError(Context ctx, int code) {
		if (code == 401) {
			errorMessage.setValue("UNAUTHORIZED");
		} else if (code == 403) {
			errorMessage.setValue(ctx.getString(R.string.authorizeError));
		} else {
			errorMessage.setValue(ctx.getString(R.string.genericError));
		}
	}
}
