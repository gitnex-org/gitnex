package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import org.gitnex.tea4j.v2.models.CreateRepoOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.SearchResults;
import org.mian.gitnex.R;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.license.License;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoriesViewModel extends ViewModel {

	private final MutableLiveData<List<Repository>> repos =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<List<String>> organizations =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<String>> licensesDisplay =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<String>> licensesKeys =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<String>> gitignores =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isInitialLoading = new MutableLiveData<>(true);
	private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorAction = new MutableLiveData<>();
	private final MutableLiveData<List<String>> issueLabels =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<List<Repository>> searchResults =
			new MutableLiveData<>(new ArrayList<>());

	private int totalCount = -1;
	private boolean isLastPage = false;
	private int loadCount = 0;
	private static final int TOTAL_LOADS = 3;
	private final List<String> reservedRepoNames = Arrays.asList(".", "..");
	private final Pattern reservedRepoPatterns = Pattern.compile("\\.(git|wiki)$");

	public LiveData<List<Repository>> getRepos() {
		return repos;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<Boolean> getHasLoadedOnce() {
		return hasLoadedOnce;
	}

	public LiveData<List<String>> getOrganizations() {
		return organizations;
	}

	public LiveData<List<String>> getLicensesDisplay() {
		return licensesDisplay;
	}

	public LiveData<List<String>> getGitignores() {
		return gitignores;
	}

	public LiveData<Boolean> getIsInitialLoading() {
		return isInitialLoading;
	}

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<Boolean> getCreateSuccess() {
		return createSuccess;
	}

	public LiveData<String> getErrorAction() {
		return errorAction;
	}

	public LiveData<List<String>> getIssueLabels() {
		return issueLabels;
	}

	public LiveData<List<Repository>> getSearchResults() {
		return searchResults;
	}

	public void fetchRepos(
			Context ctx,
			String type,
			String userLogin,
			String orgName,
			int page,
			int limit,
			String sort,
			boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		String fetchType = type != null ? type : "repos";

		Call<List<Repository>> call =
				switch (fetchType) {
					case "userRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.userListRepos(userLogin, page, limit);

					case "starredRepos" -> {
						if (userLogin != null && !userLogin.isEmpty()) {
							yield RetrofitClient.getApiInterface(ctx)
									.userListStarred(userLogin, page, limit);
						} else {
							yield RetrofitClient.getApiInterface(ctx)
									.userCurrentListStarred(page, limit);
						}
					}

					case "myRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.customUserListRepos(userLogin, page, limit, sort);

					case "org" ->
							RetrofitClient.getApiInterface(ctx).orgListRepos(orgName, page, limit);

					case "team" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListTeamRepos(Long.valueOf(userLogin), page, limit);

					case "watched" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListSubscriptions(page, limit);

					case "forks" ->
							RetrofitClient.getApiInterface(ctx)
									.listForks(userLogin, orgName, page, limit);

					default ->
							RetrofitClient.getApiInterface(ctx)
									.customUserCurrentListRepos(page, limit, sort);
				};

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {
						handleResponse(response, isRefresh, limit);
						hasLoadedOnce.setValue(true);
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
						hasLoadedOnce.setValue(true);
					}
				});
	}

	private void handleResponse(Response<List<Repository>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		hasLoadedOnce.setValue(true);

		if (response.isSuccessful() && response.body() != null) {
			if (isRefresh) {
				isLastPage = false;
				totalCount = 0;
			}

			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) totalCount = Integer.parseInt(totalHeader);

			List<Repository> currentList =
					(isRefresh || repos.getValue() == null)
							? new ArrayList<>()
							: new ArrayList<>(repos.getValue());

			currentList.addAll(response.body());
			repos.setValue(currentList);

			if (response.body().size() < limit
					|| (totalCount > 0 && currentList.size() >= totalCount)) {
				isLastPage = true;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
	}

	public void searchRepos(
			Context ctx,
			String query,
			Boolean includeTopic,
			Boolean includeDesc,
			Long uid,
			Long priorityOwnerId,
			Long teamId,
			Long starredBy,
			Boolean includePrivate,
			Boolean isPrivate,
			Boolean includeTemplate,
			Boolean onlyArchived,
			String mode,
			Boolean exclusive,
			String currentSort,
			String order,
			int page,
			int limit,
			Boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		Call<SearchResults> call =
				RetrofitClient.getApiInterface(ctx)
						.repoSearch(
								query,
								includeTopic,
								includeDesc,
								uid,
								priorityOwnerId,
								teamId,
								starredBy,
								includePrivate,
								isPrivate,
								includeTemplate,
								onlyArchived,
								mode,
								exclusive,
								currentSort,
								order,
								page,
								limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<SearchResults> call,
							@NonNull Response<SearchResults> response) {
						handleSearchResponse(response, isRefresh, limit);
						hasLoadedOnce.setValue(true);
					}

					@Override
					public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
						hasLoadedOnce.setValue(true);
					}
				});
	}

	private void handleSearchResponse(
			Response<SearchResults> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);

		if (response.isSuccessful() && response.body() != null) {
			SearchResults results = response.body();
			List<Repository> repos =
					results.getData() != null ? results.getData() : new ArrayList<>();

			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) {
				totalCount = Integer.parseInt(totalHeader);
			}

			List<Repository> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(searchResults.getValue()));
			currentList.addAll(repos);
			searchResults.setValue(currentList);

			isLastPage =
					repos.size() < limit || (totalCount != -1 && currentList.size() >= totalCount);
		} else {
			if (isRefresh) searchResults.setValue(new ArrayList<>());
			errorMessage.setValue("API error: " + response.code());
		}
	}

	public void resetPagination() {
		this.isLastPage = false;
		this.totalCount = -1;
		this.hasLoadedOnce.setValue(false);
	}

	public void loadSetupData(Context ctx, String loginUid) {
		loadCount = 0;
		isInitialLoading.setValue(true);

		fetchOrgs(ctx, loginUid);
		fetchLicenses(ctx);
		fetchGitignores(ctx);

		List<String> labels = new ArrayList<>();
		labels.add(ctx.getString(R.string.advanced));
		labels.add(ctx.getString(R.string.defaultText));
		issueLabels.setValue(labels);
	}

	private void checkLoadStatus() {
		loadCount++;
		if (loadCount >= TOTAL_LOADS) {
			isInitialLoading.setValue(false);
		}
	}

	private void fetchOrgs(Context ctx, String userLogin) {
		RetrofitClient.getApiInterface(ctx)
				.orgListCurrentUserOrgs(1, 100)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								List<String> list = new ArrayList<>();
								list.add(userLogin);
								if (response.isSuccessful() && response.body() != null) {
									for (Organization org : response.body())
										list.add(org.getUsername());
								}
								organizations.setValue(list);
								checkLoadStatus();
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								checkLoadStatus();
							}
						});
	}

	private void fetchLicenses(Context ctx) {
		ApiRetrofitClient.getInstance(ctx)
				.getLicenses()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<License>> call,
									@NonNull Response<List<License>> response) {
								List<String> disp = new ArrayList<>();
								List<String> keys = new ArrayList<>();
								disp.add(ctx.getString(R.string.no_license));
								keys.add("");
								if (response.isSuccessful() && response.body() != null) {
									for (License l : response.body()) {
										disp.add(l.getName());
										keys.add(l.getKey());
									}
								}
								licensesDisplay.setValue(disp);
								licensesKeys.setValue(keys);
								checkLoadStatus();
							}

							@Override
							public void onFailure(
									@NonNull Call<List<License>> call, @NonNull Throwable t) {
								checkLoadStatus();
							}
						});
	}

	private void fetchGitignores(Context ctx) {
		ApiRetrofitClient.getInstance(ctx)
				.getGitignoreTemplates()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<String>> call,
									@NonNull Response<List<String>> response) {
								List<String> list = new ArrayList<>();
								list.add(ctx.getString(R.string.no_template));
								if (response.isSuccessful() && response.body() != null) {
									List<String> remote = response.body();
									Collections.sort(remote);
									list.addAll(remote);
								}
								gitignores.setValue(list);
								checkLoadStatus();
							}

							@Override
							public void onFailure(
									@NonNull Call<List<String>> call, @NonNull Throwable t) {
								checkLoadStatus();
							}
						});
	}

	public void validateAndCreate(
			Context ctx,
			String name,
			String desc,
			String owner,
			String loginUid,
			String branch,
			int licensePos,
			String gitignore,
			String issueLabels,
			boolean isPrivate,
			boolean isTemplate) {

		if (name.isEmpty()) {
			errorAction.setValue(ctx.getString(R.string.repoNameErrorEmpty));
			return;
		}
		if (!AppUtil.checkStrings(name)) {
			errorAction.setValue(ctx.getString(R.string.repoNameErrorInvalid));
			return;
		}
		if (reservedRepoNames.contains(name)) {
			errorAction.setValue(ctx.getString(R.string.repoNameErrorReservedName));
			return;
		}
		if (reservedRepoPatterns.matcher(name).find()) {
			errorAction.setValue(ctx.getString(R.string.repoNameErrorReservedPatterns));
			return;
		}
		if (branch.isEmpty()) {
			errorAction.setValue(ctx.getString(R.string.repoDefaultBranchError));
			return;
		}
		if (owner == null || owner.isEmpty()) {
			errorAction.setValue(ctx.getString(R.string.repoOwnerError));
			return;
		}

		isCreating.setValue(true);

		CreateRepoOption option = new CreateRepoOption();
		option.setName(name);
		option.setDescription(desc);
		option.setPrivate(isPrivate);
		option.setTemplate(isTemplate);
		option.setDefaultBranch(branch);
		option.setIssueLabels(issueLabels);
		option.setAutoInit(true);
		option.setReadme("Default");

		if (licensePos > 0 && licensesKeys.getValue() != null) {
			option.setLicense(licensesKeys.getValue().get(licensePos));
		}

		if (gitignore != null && !gitignore.equals(ctx.getString(R.string.no_template))) {
			option.setGitignores(gitignore);
		}

		Call<Repository> call =
				owner.equals(loginUid)
						? RetrofitClient.getApiInterface(ctx).createCurrentUserRepo(option)
						: RetrofitClient.getApiInterface(ctx).createOrgRepo(owner, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull Response<Repository> response) {
						isCreating.setValue(false);
						if (response.code() == 201) {
							createSuccess.setValue(true);
						} else if (response.code() == 409) {
							errorAction.setValue(ctx.getString(R.string.repoExistsError));
						} else {
							errorAction.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						isCreating.setValue(false);
						errorAction.setValue(t.getMessage());
					}
				});
	}

	public void resetStatus() {
		createSuccess.setValue(false);
		errorAction.setValue(null);
	}
}
