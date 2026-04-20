package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.CreateWikiPageOptions;
import org.gitnex.tea4j.v2.models.WikiPage;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class WikiViewModel extends ViewModel {

	private final MutableLiveData<List<WikiPageMetaData>> wikiPages = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Integer> actionResult = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isLoadingPage = new MutableLiveData<>(false);
	private final MutableLiveData<WikiPage> pageContent = new MutableLiveData<>();
	private final MutableLiveData<String> pageError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
	private final MutableLiveData<WikiPage> createdPage = new MutableLiveData<>();
	private final MutableLiveData<WikiPage> updatedPage = new MutableLiveData<>();
	private final MutableLiveData<String> createError = new MutableLiveData<>();
	private final MutableLiveData<String> updateError = new MutableLiveData<>();

	private final List<WikiPageMetaData> fullList = new ArrayList<>();

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<WikiPageMetaData>> getWikiPages() {
		return wikiPages;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Integer> getActionResult() {
		return actionResult;
	}

	public LiveData<Boolean> getIsLoadingPage() {
		return isLoadingPage;
	}

	public LiveData<WikiPage> getPageContent() {
		return pageContent;
	}

	public LiveData<String> getPageError() {
		return pageError;
	}

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<Boolean> getIsUpdating() {
		return isUpdating;
	}

	public LiveData<WikiPage> getCreatedPage() {
		return createdPage;
	}

	public LiveData<WikiPage> getUpdatedPage() {
		return updatedPage;
	}

	public LiveData<String> getCreateError() {
		return createError;
	}

	public LiveData<String> getUpdateError() {
		return updateError;
	}

	public void resetPagination() {
		fullList.clear();
		isLastPage = false;
		totalCount = -1;
		wikiPages.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
	}

	public void clearPageContent() {
		pageContent.setValue(null);
	}

	public void clearPageError() {
		pageError.setValue(null);
	}

	public void clearCreatedPage() {
		createdPage.setValue(null);
	}

	public void clearUpdatedPage() {
		updatedPage.setValue(null);
	}

	public void clearCreateError() {
		createError.setValue(null);
	}

	public void clearUpdateError() {
		updateError.setValue(null);
	}

	public void fetchWikiPages(
			Context ctx, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue()) && !isRefresh) return;

		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.repoGetWikiPages(owner, repo, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<WikiPageMetaData>> call,
									@NonNull Response<List<WikiPageMetaData>> response) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);

								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										totalCount = Integer.parseInt(totalHeader);
									}

									List<WikiPageMetaData> body = response.body();
									if (isRefresh) {
										fullList.clear();
									}

									for (WikiPageMetaData pageData : body) {
										if (!fullList.contains(pageData)) {
											fullList.add(pageData);
										}
									}
									wikiPages.setValue(new ArrayList<>(fullList));

									if (body.size() < limit
											|| (totalCount != -1
													&& fullList.size() >= totalCount)) {
										isLastPage = true;
									}
								} else if (response.code() == 404) {
									if (isRefresh) {
										wikiPages.setValue(new ArrayList<>());
									}
									isLastPage = true;
								} else {
									error.setValue("API error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<WikiPageMetaData>> call,
									@NonNull Throwable t) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);
								error.setValue(t.getMessage());
							}
						});
	}

	public void fetchWikiPageContent(Context ctx, String owner, String repo, String pageName) {
		isLoadingPage.setValue(true);
		pageError.setValue(null);

		Call<WikiPage> call =
				RetrofitClient.getApiInterface(ctx).repoGetWikiPage(owner, repo, pageName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<WikiPage> call, @NonNull Response<WikiPage> response) {
						isLoadingPage.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							pageContent.setValue(response.body());
						} else {
							switch (response.code()) {
								case 401:
									pageError.setValue("UNAUTHORIZED");
									break;
								case 403:
									pageError.setValue(ctx.getString(R.string.authorizeError));
									break;
								case 404:
									pageError.setValue(ctx.getString(R.string.apiNotFound));
									break;
								default:
									pageError.setValue(ctx.getString(R.string.genericError));
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {
						isLoadingPage.setValue(false);
						pageError.setValue(t.getMessage());
					}
				});
	}

	public void deleteWikiPage(Context ctx, String owner, String repo, String pageName) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.repoDeleteWikiPage(owner, repo, pageName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isLoading.setValue(false);
								if (response.isSuccessful()) {
									fullList.removeIf(p -> p.getTitle().equals(pageName));
									wikiPages.setValue(new ArrayList<>(fullList));

									if (totalCount > 0) {
										totalCount--;
									}

									actionResult.setValue(204);
								} else {
									error.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								error.setValue(t.getMessage());
							}
						});
	}

	public void resetActionResult() {
		actionResult.setValue(-1);
	}

	public void createWikiPage(
			Context ctx, String owner, String repo, String title, String content) {
		isCreating.setValue(true);
		createError.setValue(null);

		CreateWikiPageOptions options = new CreateWikiPageOptions();
		options.setTitle(title);
		options.setContentBase64(AppUtil.encodeBase64(content));

		Call<WikiPage> call =
				RetrofitClient.getApiInterface(ctx).repoCreateWikiPage(owner, repo, options);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<WikiPage> call, @NonNull Response<WikiPage> response) {
						isCreating.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							createdPage.setValue(response.body());
							actionResult.setValue(201);
						} else {
							handleCreateUpdateError(response.code(), ctx, true);
						}
					}

					@Override
					public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {
						isCreating.setValue(false);
						createError.setValue(t.getMessage());
					}
				});
	}

	public void updateWikiPage(
			Context ctx, String owner, String repo, String pageName, String title, String content) {
		isUpdating.setValue(true);
		updateError.setValue(null);

		CreateWikiPageOptions options = new CreateWikiPageOptions();
		options.setTitle(title);
		options.setContentBase64(AppUtil.encodeBase64(content));

		Call<WikiPage> call =
				RetrofitClient.getApiInterface(ctx)
						.repoEditWikiPage(owner, repo, pageName, options);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<WikiPage> call, @NonNull Response<WikiPage> response) {
						isUpdating.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							updatedPage.setValue(response.body());
							actionResult.setValue(200);
						} else {
							handleCreateUpdateError(response.code(), ctx, false);
						}
					}

					@Override
					public void onFailure(@NonNull Call<WikiPage> call, @NonNull Throwable t) {
						isUpdating.setValue(false);
						updateError.setValue(t.getMessage());
					}
				});
	}

	private void handleCreateUpdateError(int code, Context ctx, boolean isCreate) {
		String errorMsg =
				switch (code) {
					case 401 -> "UNAUTHORIZED";
					case 403 -> ctx.getString(R.string.authorizeError);
					case 404 -> ctx.getString(R.string.apiNotFound);
					case 409 -> ctx.getString(R.string.wikiPageAlreadyExists);
					default -> ctx.getString(R.string.genericError);
				};

		if (isCreate) {
			createError.setValue(errorMsg);
		} else {
			updateError.setValue(errorMsg);
		}
	}
}
