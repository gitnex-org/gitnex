package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class OrgTeamsViewModel extends ViewModel {

	private final MutableLiveData<List<User>> users = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<String> actionSuccessMessage = new MutableLiveData<>();
	private final MutableLiveData<List<Repository>> repositories =
			new MutableLiveData<>(new ArrayList<>());

	public LiveData<List<User>> getUsers() {
		return users;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<String> getActionSuccessMessage() {
		return actionSuccessMessage;
	}

	public LiveData<List<Repository>> getRepositories() {
		return repositories;
	}

	public boolean wasMemberModified() {
		return wasMemberModified;
	}

	private final Set<String> currentMemberLogins = new HashSet<>();
	private boolean wasMemberModified = false;
	private final Set<String> currentTeamRepoNames = new HashSet<>();
	private boolean wasRepoModified = false;

	public boolean isRepoInTeam(String repoName) {
		return currentTeamRepoNames.contains(repoName);
	}

	public void loadCurrentMembers(Context context, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgListTeamMembers(teamId, 1, 100)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<User>> call,
									@NonNull Response<List<User>> response) {
								if (response.isSuccessful() && response.body() != null) {
									currentMemberLogins.clear();
									for (User u : response.body()) {
										currentMemberLogins.add(u.getLogin());
									}
									users.setValue(users.getValue());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<User>> call, @NonNull Throwable t) {}
						});
	}

	public boolean isUserInTeam(String login) {
		return currentMemberLogins.contains(login);
	}

	public void fetchUsers(Context ctx, String query, int page, int limit, boolean isRefresh) {

		if (isLoading.getValue() != null && isLoading.getValue()) return;
		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.userSearch(query, null, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<InlineResponse2001> call,
									@NonNull Response<InlineResponse2001> response) {
								handleResponse(
										response.isSuccessful() && response.body() != null
												? response.body().getData()
												: null,
										response,
										isRefresh,
										limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	public void addTeamMember(Context context, String userName, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgAddTeamMember(teamId, userName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.code() == 204) {
									currentMemberLogins.add(userName);
									wasMemberModified = true;
									users.setValue(users.getValue());
									actionSuccessMessage.setValue(
											context.getString(R.string.memberAddedMessage));
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	public void removeTeamMember(Context context, String userName, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgRemoveTeamMember(teamId, userName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.code() == 204) {
									currentMemberLogins.remove(userName);
									wasMemberModified = true;
									users.setValue(users.getValue());
									actionSuccessMessage.setValue(
											context.getString(R.string.memberRemovedMessage));
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	public void loadTeamRepos(Context context, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgListTeamRepos(teamId, 1, 100)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Repository>> call,
									@NonNull Response<List<Repository>> response) {
								if (response.isSuccessful() && response.body() != null) {
									currentTeamRepoNames.clear();
									for (Repository r : response.body()) {
										currentTeamRepoNames.add(r.getName());
									}
									repositories.setValue(repositories.getValue());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Repository>> call, @NonNull Throwable t) {}
						});
	}

	public void fetchOrgRepos(Context context, String orgName, int page, int limit) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(context)
				.orgListRepos(orgName, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Repository>> call,
									@NonNull Response<List<Repository>> response) {
								if (response.isSuccessful() && response.body() != null) {
									repositories.setValue(response.body());
								}
								isLoading.setValue(false);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	public void addRepoToTeam(Context context, String orgName, String repoName, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgAddTeamRepository(teamId, orgName, repoName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.code() == 204) {
									currentTeamRepoNames.add(repoName);
									wasRepoModified = true;
									repositories.setValue(repositories.getValue());
									actionSuccessMessage.setValue(
											context.getString(R.string.repoAddedMessage));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	public void removeRepoFromTeam(Context context, String orgName, String repoName, long teamId) {
		RetrofitClient.getApiInterface(context)
				.orgRemoveTeamRepository(teamId, orgName, repoName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.code() == 204) {
									currentTeamRepoNames.remove(repoName);
									wasRepoModified = true;
									repositories.setValue(repositories.getValue());
									actionSuccessMessage.setValue(
											context.getString(R.string.repoRemovedMessage));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								handleError(t);
							}
						});
	}

	private void handleResponse(
			List<User> body, Response<?> response, boolean isRefresh, int limit) {
		if (response.isSuccessful() && body != null) {
			List<User> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(users.getValue()));
			currentList.addAll(body);
			users.setValue(currentList);
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
		isLoading.setValue(false);
	}

	private void handleError(Throwable t) {
		errorMessage.setValue(t.getMessage());
		isLoading.setValue(false);
	}
}
