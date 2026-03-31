package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.gitnex.tea4j.v2.models.CreateOrgOption;
import org.gitnex.tea4j.v2.models.CreateTeamOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class OrganizationsViewModel extends ViewModel {

	private final MutableLiveData<List<Organization>> orgs =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<Organization> singleOrg = new MutableLiveData<>();
	private final MutableLiveData<OrganizationPermissions> permissions = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isMember = new MutableLiveData<>(false);
	private final MutableLiveData<List<Team>> teams = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isTeamsLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> teamsLoadedOnce = new MutableLiveData<>(false);
	private final MutableLiveData<Map<Long, TeamMemberData>> teamMembersMap =
			new MutableLiveData<>(new HashMap<>());
	private final MutableLiveData<Boolean> isCreatingTeam = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> createTeamResult = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
	private final MutableLiveData<String> createError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> createSuccess = new MutableLiveData<>(false);

	private boolean isLastPage = false;
	private int totalCount = -1;

	public LiveData<List<Organization>> getOrgs() {
		return orgs;
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

	public LiveData<Organization> getSingleOrg() {
		return singleOrg;
	}

	public LiveData<OrganizationPermissions> getPermissions() {
		return permissions;
	}

	public LiveData<Boolean> getIsMember() {
		return isMember;
	}

	public LiveData<List<Team>> getTeams() {
		return teams;
	}

	public LiveData<Boolean> getIsTeamsLoading() {
		return isTeamsLoading;
	}

	public LiveData<Boolean> getTeamsLoadedOnce() {
		return teamsLoadedOnce;
	}

	public LiveData<Map<Long, OrganizationsViewModel.TeamMemberData>> getTeamMembersMap() {
		return teamMembersMap;
	}

	public LiveData<Boolean> getIsCreatingTeam() {
		return isCreatingTeam;
	}

	public LiveData<Integer> getCreateTeamResult() {
		return createTeamResult;
	}

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<String> getCreateError() {
		return createError;
	}

	public LiveData<Boolean> getCreateSuccess() {
		return createSuccess;
	}

	public void resetCreateTeamResult() {
		createTeamResult.setValue(-1);
	}

	public void createOrganization(Context ctx, CreateOrgOption option, String visibilityStr) {
		isCreating.setValue(true);

		CreateOrgOption.VisibilityEnum visibility;
		if ("private".equalsIgnoreCase(visibilityStr)) {
			visibility = CreateOrgOption.VisibilityEnum.PRIVATE;
		} else if ("limited".equalsIgnoreCase(visibilityStr)) {
			visibility = CreateOrgOption.VisibilityEnum.LIMITED;
		} else {
			visibility = CreateOrgOption.VisibilityEnum.PUBLIC;
		}
		option.setVisibility(visibility);

		RetrofitClient.getApiInterface(ctx)
				.orgCreate(option)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Organization> call,
									@NonNull Response<Organization> response) {
								isCreating.setValue(false);
								if (response.isSuccessful() && response.code() == 201) {
									createSuccess.setValue(true);
									fetchOrganizations(
											ctx, 1, Constants.getCurrentResultLimit(ctx), true);
								} else {
									if (response.code() == 409 || response.code() == 422) {
										createError.setValue(
												ctx.getString(R.string.orgExistsError));
									} else if (response.code() == 403) {
										createError.setValue("Permission denied");
									} else {
										createError.setValue("Error: " + response.code());
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Organization> call, @NonNull Throwable t) {
								isCreating.setValue(false);
								createError.setValue(t.getMessage());
							}
						});
	}

	public void resetCreateStatus() {
		createSuccess.setValue(false);
		createError.setValue(null);
	}

	public void createTeam(Context ctx, String orgName, CreateTeamOption options) {
		isCreatingTeam.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgCreateTeam(orgName, options)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Team> call, @NonNull Response<Team> response) {
								isCreatingTeam.setValue(false);
								if (response.isSuccessful() && response.code() == 201) {
									createTeamResult.setValue(201);
								} else {
									createTeamResult.setValue(response.code());
								}
							}

							@Override
							public void onFailure(@NonNull Call<Team> call, @NonNull Throwable t) {
								isCreatingTeam.setValue(false);
								createTeamResult.setValue(500);
							}
						});
	}

	public void fetchTeams(Context ctx, String orgName) {
		isTeamsLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgListTeams(orgName, null, null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Team>> call,
									@NonNull Response<List<Team>> response) {
								isTeamsLoading.setValue(false);
								teamsLoadedOnce.setValue(true);
								if (response.isSuccessful() && response.body() != null) {
									teams.setValue(response.body());
									for (Team team : response.body()) {
										fetchTeamMembersPreview(ctx, team.getId());
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Team>> call, @NonNull Throwable t) {
								isTeamsLoading.setValue(false);
								teamsLoadedOnce.setValue(true);
							}
						});
	}

	private void fetchTeamMembersPreview(Context ctx, long teamId) {
		RetrofitClient.getApiInterface(ctx)
				.orgListTeamMembers(teamId, null, null)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<User>> call,
									@NonNull Response<List<User>> response) {
								if (response.isSuccessful() && response.body() != null) {

									String totalCountHeader =
											response.headers().get("x-total-count");
									int totalCount =
											(totalCountHeader != null)
													? Integer.parseInt(totalCountHeader)
													: response.body().size();

									List<User> limited =
											response.body().stream()
													.limit(6)
													.collect(Collectors.toList());

									Map<Long, TeamMemberData> currentMap =
											teamMembersMap.getValue();
									if (currentMap != null) {
										currentMap.put(
												teamId, new TeamMemberData(limited, totalCount));
										teamMembersMap.postValue(currentMap);
									}
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<User>> call, @NonNull Throwable t) {}
						});
	}

	public record TeamMemberData(List<User> previewMembers, int membersPreviewTotalCount) {}

	public void loadOrganizationContext(Context ctx, String orgName, String userName) {
		isLoading.setValue(true);
		AtomicInteger pendingCalls = new AtomicInteger(3);

		Runnable decrementAndCheck =
				() -> {
					int remaining = pendingCalls.decrementAndGet();
					if (remaining <= 0) {
						isLoading.postValue(false);
					}
				};

		RetrofitClient.getApiInterface(ctx)
				.orgGet(orgName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Organization> call,
									@NonNull Response<Organization> response) {
								if (response.isSuccessful()) {
									singleOrg.setValue(response.body());
								}
								decrementAndCheck.run();
							}

							@Override
							public void onFailure(
									@NonNull Call<Organization> call, @NonNull Throwable t) {
								decrementAndCheck.run();
							}
						});

		RetrofitClient.getApiInterface(ctx)
				.orgIsMember(orgName, userName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isMember.setValue(response.code() == 204);
								decrementAndCheck.run();
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isMember.setValue(false);
								decrementAndCheck.run();
							}
						});

		RetrofitClient.getApiInterface(ctx)
				.orgGetUserPermissions(userName, orgName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<OrganizationPermissions> call,
									@NonNull Response<OrganizationPermissions> response) {
								if (response.isSuccessful()) {
									permissions.setValue(response.body());
								}
								decrementAndCheck.run();
							}

							@Override
							public void onFailure(
									@NonNull Call<OrganizationPermissions> call,
									@NonNull Throwable t) {
								decrementAndCheck.run();
							}
						});
	}

	public void fetchOrgDetails(Context ctx, String orgName) {
		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgGet(orgName)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Organization> call,
									@NonNull Response<Organization> response) {
								isLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									singleOrg.setValue(response.body());
								} else {
									errorMessage.setValue("Error: " + response.code());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Organization> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchOrganizations(Context ctx, int page, int limit, boolean isRefresh) {

		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);

		RetrofitClient.getApiInterface(ctx)
				.orgListCurrentUserOrgs(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchAllPublicOrgs(Context ctx, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgGetAll(page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
								hasLoadedOnce.setValue(true);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								hasLoadedOnce.setValue(true);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void fetchUserOrgs(
			Context ctx, String username, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.orgListUserOrgs(username, page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Organization>> call,
									@NonNull Response<List<Organization>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleResponse(
			Response<List<Organization>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);

		if (isRefresh) {
			isLastPage = false;
			totalCount = -1;
		}

		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) {
				totalCount = Integer.parseInt(totalHeader);
			}

			List<Organization> incomingList = response.body();
			List<Organization> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(orgs.getValue()));

			currentList.addAll(incomingList);
			orgs.setValue(currentList);

			if (incomingList.size() < limit
					|| (totalCount != -1 && currentList.size() >= totalCount)) {
				isLastPage = true;
			} else if (isRefresh) {
				isLastPage = false;
			}
		} else {
			errorMessage.setValue("Error: " + response.code());
		}
	}
}
