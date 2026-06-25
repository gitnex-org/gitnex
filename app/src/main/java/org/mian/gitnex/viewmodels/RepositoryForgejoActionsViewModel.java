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
import org.mian.gitnex.R;
import org.mian.gitnex.api.clients.ApiRetrofitClient;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoRunner;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoTask;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoTasksResponse;
import org.mian.gitnex.api.models.forgejo.actions.ForgejoVariable;
import org.mian.gitnex.helpers.Constants;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoryForgejoActionsViewModel extends ViewModel {

	private final MutableLiveData<List<ForgejoRunner>> runners =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingRunners = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedRunnersOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> runnersError = new MutableLiveData<>();
	private boolean isRunnersLastPage = false;
	private int runnersTotalCount = -1;

	private final MutableLiveData<List<ForgejoTask>> tasks =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingTasks = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedTasksOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> tasksError = new MutableLiveData<>();
	private boolean isTasksLastPage = false;
	private int tasksTotalCount = -1;

	private final MutableLiveData<List<ForgejoVariable>> variables =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoadingVariables = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> hasLoadedVariablesOnce = new MutableLiveData<>(false);
	private final MutableLiveData<String> variablesError = new MutableLiveData<>();
	private boolean isVariablesLastPage = false;
	private int variablesTotalCount = -1;

	private final MutableLiveData<Boolean> isCreatingVariable = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> variableCreated = new MutableLiveData<>();
	private final MutableLiveData<String> createVariableError = new MutableLiveData<>();

	private final MutableLiveData<Integer> result = new MutableLiveData<>(-1);

	public LiveData<List<ForgejoRunner>> getRunners() {
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

	public LiveData<List<ForgejoTask>> getTasks() {
		return tasks;
	}

	public LiveData<Boolean> getIsLoadingTasks() {
		return isLoadingTasks;
	}

	public LiveData<Boolean> getHasLoadedTasksOnce() {
		return hasLoadedTasksOnce;
	}

	public LiveData<String> getTasksError() {
		return tasksError;
	}

	public LiveData<List<ForgejoVariable>> getVariables() {
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

	public void resetResult() {
		result.setValue(-1);
	}

	public void resetTasksPagination() {
		isTasksLastPage = false;
		tasksTotalCount = -1;
		tasks.setValue(new ArrayList<>());
		hasLoadedTasksOnce.setValue(false);
	}

	public void resetVariablesPagination() {
		isVariablesLastPage = false;
		variablesTotalCount = -1;
		variables.setValue(new ArrayList<>());
		hasLoadedVariablesOnce.setValue(false);
	}

	public void resetRunnersPagination() {
		isRunnersLastPage = false;
		runnersTotalCount = -1;
		runners.setValue(new ArrayList<>());
		hasLoadedRunnersOnce.setValue(false);
	}

	public void clearVariableCreated() {
		variableCreated.setValue(null);
	}

	public void clearCreateVariableError() {
		createVariableError.setValue(null);
	}

	public void fetchRunners(
			Context ctx, String owner, String repository, int page, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingRunners.getValue()) && !isRefresh) return;
		if (!isRefresh && isRunnersLastPage) return;

		isLoadingRunners.setValue(true);
		runnersError.setValue(null);

		int limit = Constants.getCurrentResultLimit(ctx);

		Call<List<ForgejoRunner>> call =
				ApiRetrofitClient.getInstance(ctx)
						.getForgejoRunners(owner, repository, page, limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ForgejoRunner>> call,
							@NonNull Response<List<ForgejoRunner>> response) {
						isLoadingRunners.setValue(false);
						hasLoadedRunnersOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							String totalHeader = response.headers().get("x-total-count");
							if (totalHeader != null)
								runnersTotalCount = Integer.parseInt(totalHeader);

							List<ForgejoRunner> body = response.body();
							List<ForgejoRunner> currentList =
									isRefresh
											? new ArrayList<>()
											: new ArrayList<>(
													Objects.requireNonNull(runners.getValue()));
							currentList.addAll(body);
							runners.setValue(currentList);

							if (body.size() < limit
									|| (runnersTotalCount != -1
											&& currentList.size() >= runnersTotalCount)) {
								isRunnersLastPage = true;
							}
						} else {
							if (response.code() == 404 && isRefresh)
								runners.setValue(new ArrayList<>());
							isRunnersLastPage = true;
							if (response.code() != 404)
								runnersError.setValue("API error: " + response.code());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<ForgejoRunner>> call, @NonNull Throwable t) {
						isLoadingRunners.setValue(false);
						hasLoadedRunnersOnce.setValue(true);
						runnersError.setValue(t.getMessage());
					}
				});
	}

	public void fetchTasks(
			Context ctx, String owner, String repository, int page, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingTasks.getValue()) && !isRefresh) return;
		if (!isRefresh && isTasksLastPage) return;

		isLoadingTasks.setValue(true);
		tasksError.setValue(null);

		int limit = Constants.getCurrentResultLimit(ctx);

		Call<ForgejoTasksResponse> call =
				ApiRetrofitClient.getInstance(ctx).getForgejoTasks(owner, repository, page, limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ForgejoTasksResponse> call,
							@NonNull Response<ForgejoTasksResponse> response) {
						isLoadingTasks.setValue(false);
						hasLoadedTasksOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							ForgejoTasksResponse body = response.body();
							tasksTotalCount = (int) body.getTotalCount();

							List<ForgejoTask> currentList =
									isRefresh
											? new ArrayList<>()
											: new ArrayList<>(
													Objects.requireNonNull(tasks.getValue()));

							if (body.getWorkflowRuns() != null) {
								currentList.addAll(body.getWorkflowRuns());
							}
							tasks.setValue(currentList);

							if (body.getWorkflowRuns() == null
									|| body.getWorkflowRuns().size() < limit
									|| currentList.size() >= tasksTotalCount) {
								isTasksLastPage = true;
							}
						} else {
							if (response.code() == 404 && isRefresh) {
								tasks.setValue(new ArrayList<>());
							}
							isTasksLastPage = true;
							if (response.code() != 404) {
								tasksError.setValue("Error: " + response.code());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ForgejoTasksResponse> call, @NonNull Throwable t) {
						isLoadingTasks.setValue(false);
						hasLoadedTasksOnce.setValue(true);
						tasksError.setValue(t.getMessage());
					}
				});
	}

	public void fetchVariables(
			Context ctx, String owner, String repository, int page, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoadingVariables.getValue()) && !isRefresh) return;
		if (!isRefresh && isVariablesLastPage) return;

		isLoadingVariables.setValue(true);

		int limit = Constants.getCurrentResultLimit(ctx);

		Call<List<ForgejoVariable>> call =
				ApiRetrofitClient.getInstance(ctx)
						.getForgejoVariables(owner, repository, page, limit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<ForgejoVariable>> call,
							@NonNull Response<List<ForgejoVariable>> response) {
						isLoadingVariables.setValue(false);
						hasLoadedVariablesOnce.setValue(true);

						if (response.isSuccessful() && response.body() != null) {
							String totalHeader = response.headers().get("x-total-count");
							if (totalHeader != null) {
								variablesTotalCount = Integer.parseInt(totalHeader);
							}

							List<ForgejoVariable> body = response.body();
							List<ForgejoVariable> currentList =
									isRefresh
											? new ArrayList<>()
											: new ArrayList<>(
													Objects.requireNonNull(variables.getValue()));

							for (ForgejoVariable v : body) {
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
							@NonNull Call<List<ForgejoVariable>> call, @NonNull Throwable t) {
						isLoadingVariables.setValue(false);
						hasLoadedVariablesOnce.setValue(true);
						variablesError.setValue(t.getMessage());
					}
				});
	}

	public void createVariable(
			Context ctx, String owner, String repository, String name, String value) {
		isCreatingVariable.setValue(true);
		createVariableError.setValue(null);

		Map<String, String> body = new HashMap<>();
		body.put("value", value);

		Call<Void> call =
				ApiRetrofitClient.getInstance(ctx)
						.createForgejoVariable(owner, repository, name, body);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isCreatingVariable.setValue(false);
						if (response.isSuccessful()
								|| response.code() == 201
								|| response.code() == 204) {
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
			Context ctx, String owner, String repository, String variableName, int position) {
		List<ForgejoVariable> current =
				new ArrayList<>(Objects.requireNonNull(variables.getValue()));
		if (position >= 0 && position < current.size()) {
			current.remove(position);
			variables.setValue(current);
		}

		Call<Void> call =
				ApiRetrofitClient.getInstance(ctx)
						.deleteForgejoVariable(owner, repository, variableName);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful() || response.code() == 204) {
							result.setValue(204);
						} else {
							fetchVariables(ctx, owner, repository, 1, true);
							result.setValue(response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						fetchVariables(ctx, owner, repository, 1, true);
						result.setValue(-1);
					}
				});
	}
}
