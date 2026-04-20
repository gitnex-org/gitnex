package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.ActionRunner;
import org.gitnex.tea4j.v2.models.ActionRunnersResponse;
import org.gitnex.tea4j.v2.models.ActionVariable;
import org.gitnex.tea4j.v2.models.ActionWorkflow;
import org.gitnex.tea4j.v2.models.ActionWorkflowResponse;
import org.gitnex.tea4j.v2.models.CreateVariableOption;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoryActionsViewModel extends ViewModel {

	private final MutableLiveData<List<ActionRunner>> runners =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingRunners = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedRunnersOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> runnersError = new MutableLiveData<>();
	private final MutableLiveData<Integer> result = new MutableLiveData<>(-1);
	private final MutableLiveData<List<ActionWorkflow>> workflows =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingWorkflows = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedWorkflowsOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> workflowsError = new MutableLiveData<>();
	private final MutableLiveData<List<ActionVariable>> variables =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingVariables = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedVariablesOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> variablesError = new MutableLiveData<>();

	private boolean isVariablesLastPage = false;
	private int variablesTotalCount = -1;

	private final MutableLiveData<Boolean> isCreatingVariable = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> variableCreated = new MutableLiveData<>();
	private final MutableLiveData<String> createVariableError = new MutableLiveData<>();

	public LiveData<List<ActionRunner>> getRunners() {
		return runners;
	}

	public LiveData<Boolean> getIsLoadingRunners() {
		return isLoadingRunners;
	}

	public LiveData<Boolean> getHasLoadedRunnersOnce() {
		return hasLoadedRunnersOnce;
	}

	public LiveData<String> getRunnersError() {
		return runnersError;
	}

	public LiveData<List<ActionWorkflow>> getWorkflows() {
		return workflows;
	}

	public LiveData<Boolean> getIsLoadingWorkflows() {
		return isLoadingWorkflows;
	}

	public LiveData<Boolean> getHasLoadedWorkflowsOnce() {
		return hasLoadedWorkflowsOnce;
	}

	public LiveData<String> getWorkflowsError() {
		return workflowsError;
	}

	public LiveData<List<ActionVariable>> getVariables() {
		return variables;
	}

	public LiveData<Boolean> getIsLoadingVariables() {
		return isLoadingVariables;
	}

	public LiveData<Boolean> getHasLoadedVariablesOnce() {
		return hasLoadedVariablesOnce;
	}

	public LiveData<String> getVariablesError() {
		return variablesError;
	}

	public LiveData<Boolean> getIsCreatingVariable() {
		return isCreatingVariable;
	}

	public LiveData<Boolean> getVariableCreated() {
		return variableCreated;
	}

	public LiveData<String> getCreateVariableError() {
		return createVariableError;
	}

	public LiveData<Integer> getResult() {
		return result;
	}

	public void resetResults() {
		result.setValue(-1);
	}

	public void resetVariablesPagination() {
		isVariablesLastPage = false;
		variablesTotalCount = -1;
		variables.setValue(new ArrayList<>());
		hasLoadedVariablesOnce.setValue(false);
	}

	public void clearVariableCreated() {
		variableCreated.setValue(null);
	}

	public void clearCreateVariableError() {
		createVariableError.setValue(null);
	}

	public void clearErrors() {
		runnersError.setValue(null);
		workflowsError.setValue(null);
		variablesError.setValue(null);
	}

	public void fetchRunners(Context ctx, String owner, String repo, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingRunners.getValue()) && !isRefresh) return;

		isLoadingRunners.setValue(true);
		runnersError.setValue(null);

		Call<ActionRunnersResponse> call =
				RetrofitClient.getApiInterface(ctx).customGetRepoRunners(owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionRunnersResponse> call,
							@NonNull Response<ActionRunnersResponse> response) {
						isLoadingRunners.setValue(false);
						hasLoadedRunnersOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							List<ActionRunner> runnerList = response.body().getRunners();
							runners.setValue(runnerList != null ? runnerList : new ArrayList<>());
						} else {
							if (response.code() == 404 && isRefresh) {
								runners.setValue(new ArrayList<>());
							} else if (response.code() != 404) {
								runnersError.setValue("API error: " + response.code());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionRunnersResponse> call, @NonNull Throwable t) {
						isLoadingRunners.setValue(false);
						hasLoadedRunnersOnce.setValue(true);
						runnersError.setValue(t.getMessage());
					}
				});
	}

	public void fetchWorkflows(Context ctx, String owner, String repo, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingWorkflows.getValue()) && !isRefresh) return;

		isLoadingWorkflows.setValue(true);
		workflowsError.setValue(null);

		Call<ActionWorkflowResponse> call =
				RetrofitClient.getApiInterface(ctx).actionsListRepositoryWorkflows(owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionWorkflowResponse> call,
							@NonNull Response<ActionWorkflowResponse> response) {
						isLoadingWorkflows.setValue(false);
						hasLoadedWorkflowsOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							List<ActionWorkflow> workflowList = response.body().getWorkflows();
							workflows.setValue(
									workflowList != null ? workflowList : new ArrayList<>());
						} else {
							if (response.code() == 404 && isRefresh) {
								workflows.setValue(new ArrayList<>());
							} else if (response.code() != 404) {
								workflowsError.setValue("API error: " + response.code());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionWorkflowResponse> call, @NonNull Throwable t) {
						isLoadingWorkflows.setValue(false);
						hasLoadedWorkflowsOnce.setValue(true);
						workflowsError.setValue(t.getMessage());
					}
				});
	}

	public void fetchVariables(
			Context ctx, String owner, String repo, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingVariables.getValue()) && !isRefresh) return;
		if (!isRefresh && isVariablesLastPage) return;

		isLoadingVariables.setValue(true);

		Call<List<ActionVariable>> call =
				RetrofitClient.getApiInterface(ctx).getRepoVariablesList(owner, repo, page, limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ActionVariable>> call,
							@NonNull Response<List<ActionVariable>> response) {
						isLoadingVariables.setValue(false);
						hasLoadedVariablesOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							String totalHeader = response.headers().get("x-total-count");
							if (totalHeader != null) {
								variablesTotalCount = Integer.parseInt(totalHeader);
							}

							List<ActionVariable> body = response.body();
							List<ActionVariable> currentList =
									isRefresh
											? new ArrayList<>()
											: new ArrayList<>(
													Objects.requireNonNull(variables.getValue()));

							for (ActionVariable v : body) {
								if (!currentList.contains(v)) {
									currentList.add(v);
								}
							}
							variables.setValue(currentList);

							if (body.size() < limit
									|| (variablesTotalCount != -1
											&& currentList.size() >= variablesTotalCount)) {
								isVariablesLastPage = true;
							}
						} else {
							if (response.code() == 404 && isRefresh) {
								variables.setValue(new ArrayList<>());
							}
							isVariablesLastPage = true;
							if (response.code() != 404) {
								variablesError.setValue("Error: " + response.code());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ActionVariable>> call, @NonNull Throwable t) {
						isLoadingVariables.setValue(false);
						hasLoadedVariablesOnce.setValue(true);
						variablesError.setValue(t.getMessage());
					}
				});
	}

	public void createVariable(
			Context ctx, String owner, String repo, String name, String value, String description) {
		isCreatingVariable.setValue(true);
		createVariableError.setValue(null);

		CreateVariableOption option = new CreateVariableOption();
		option.setValue(value);
		if (description != null && !description.isEmpty()) {
			option.setDescription(description);
		}

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx).createRepoVariable(owner, repo, name, option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isCreatingVariable.setValue(false);
						if (response.isSuccessful() || response.code() == 201) {
							variableCreated.setValue(true);
							result.setValue(201);
						} else {
							createVariableError.setValue(
									ctx.getString(R.string.variable_create_failed));
							result.setValue(response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isCreatingVariable.setValue(false);
						createVariableError.setValue(
								ctx.getString(R.string.variable_create_failed));
						result.setValue(-1);
					}
				});
	}

	public void deleteVariable(
			Context ctx,
			String owner,
			String repo,
			String variableName,
			int position,
			int resultLimit) {
		List<ActionVariable> current =
				new ArrayList<>(Objects.requireNonNull(variables.getValue()));
		if (position >= 0 && position < current.size()) {
			current.remove(position);
			variables.setValue(current);
		}

		Call<ActionVariable> call =
				RetrofitClient.getApiInterface(ctx).deleteRepoVariable(owner, repo, variableName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionVariable> call,
							@NonNull Response<ActionVariable> response) {
						if (response.isSuccessful() || response.code() == 204) {
							result.setValue(204);
						} else {
							fetchVariables(ctx, owner, repo, 1, resultLimit, true);
							result.setValue(response.code());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionVariable> call, @NonNull Throwable t) {
						fetchVariables(ctx, owner, repo, 1, resultLimit, true);
						result.setValue(-1);
					}
				});
	}
}
