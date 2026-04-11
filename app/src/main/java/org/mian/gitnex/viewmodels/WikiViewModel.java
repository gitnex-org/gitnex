package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.clients.RetrofitClient;
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

	public void resetPagination() {
		fullList.clear();
		isLastPage = false;
		totalCount = -1;
		wikiPages.setValue(new ArrayList<>());
		hasLoadedOnce.setValue(false);
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
}
