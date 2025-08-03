package org.mian.gitnex.activities;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.List;
import org.gitnex.tea4j.v2.models.ActionArtifactsResponse;
import org.gitnex.tea4j.v2.models.ActionRunner;
import org.gitnex.tea4j.v2.models.ActionRunnerLabel;
import org.gitnex.tea4j.v2.models.ActionRunnersResponse;
import org.gitnex.tea4j.v2.models.ActionTaskResponse;
import org.gitnex.tea4j.v2.models.ActionVariable;
import org.gitnex.tea4j.v2.models.ActionWorkflow;
import org.gitnex.tea4j.v2.models.ActionWorkflowResponse;
import org.gitnex.tea4j.v2.models.CreateVariableOption;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityRepositoryActionsBinding;
import org.mian.gitnex.databinding.BottomSheetCreateActionVariableBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.ActionsVariablesViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositoryActions extends BaseActivity {

	private ActivityRepositoryActionsBinding activityRepositoryActionsBinding;
	private RepositoryContext repositoryContext;
	private int resultLimit;
	private String taskCount = "0";
	private String artifactCount = "0";
	private String runnerCount = "0";
	private ActionsVariablesViewModel variablesViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		activityRepositoryActionsBinding =
				ActivityRepositoryActionsBinding.inflate(getLayoutInflater());
		setContentView(activityRepositoryActionsBinding.getRoot());

		resultLimit = Constants.getCurrentResultLimit(ctx);
		repositoryContext = RepositoryContext.fromIntent(getIntent());
		variablesViewModel = new ViewModelProvider(this).get(ActionsVariablesViewModel.class);

		activityRepositoryActionsBinding.bottomAppBar.setNavigationOnClickListener(
				bottomAppBar -> finish());

		setActiveCard(activityRepositoryActionsBinding.runnersCard);

		activityRepositoryActionsBinding.runnersCard.setOnClickListener(
				v -> {
					setActiveCard(activityRepositoryActionsBinding.runnersCard);
					fetchRunners();
				});
		activityRepositoryActionsBinding.workflowsCard.setOnClickListener(
				v -> {
					setActiveCard(activityRepositoryActionsBinding.workflowsCard);
					fetchWorkflows();
				});
		activityRepositoryActionsBinding.variablesCard.setOnClickListener(
				v -> {
					setActiveCard(activityRepositoryActionsBinding.variablesCard);
					variablesViewModel.resetPagination();
					fetchVariables();
				});

		activityRepositoryActionsBinding.newVariable.setOnClickListener(
				v -> showCreateVariableBottomSheet());

		setupScrollListener();
		variablesViewModel.getVariables().observe(this, this::displayVariables);

		fetchRunnerCount();
		fetchTaskCount();
		fetchArtifactCount();
		fetchRunners();
	}

	private void showCreateVariableBottomSheet() {
		BottomSheetCreateActionVariableBinding sheetBinding =
				BottomSheetCreateActionVariableBinding.inflate(
						LayoutInflater.from(this), null, false);
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		View bottomSheetView =
				bottomSheetDialog.findViewById(
						com.google.android.material.R.id.design_bottom_sheet);
		if (bottomSheetView != null) {
			BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheetView);
			behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
			behavior.setFitToContents(true);
		}

		sheetBinding.cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());
		sheetBinding.createButton.setOnClickListener(
				v -> createVariable(sheetBinding, bottomSheetDialog));

		bottomSheetDialog.show();
	}

	private void createVariable(
			BottomSheetCreateActionVariableBinding binding, BottomSheetDialog dialog) {

		String name =
				binding.variableName.getText() != null
						? binding.variableName.getText().toString().trim()
						: "";
		String value =
				binding.variableValue.getText() != null
						? binding.variableValue.getText().toString().trim()
						: "";
		String description =
				binding.variableDescription.getText() != null
						? binding.variableDescription.getText().toString().trim()
						: "";

		if (name.isEmpty()) {
			Toasty.error(this, getString(R.string.variable_name_error));
			return;
		}
		if (!name.matches("^[a-zA-Z0-9_]+$")) {
			Toasty.error(this, getString(R.string.variable_name_invalid));
			return;
		}
		if (value.isEmpty()) {
			Toasty.error(this, getString(R.string.variable_value_error));
			return;
		}

		CreateVariableOption option = new CreateVariableOption();
		option.setValue(value);
		if (!description.isEmpty()) {
			option.setDescription(description);
		}

		Call<Void> call =
				RetrofitClient.getApiInterface(this)
						.createRepoVariable(
								repositoryContext.getOwner(),
								repositoryContext.getName(),
								name,
								option);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							Toasty.success(
									RepositoryActions.this,
									getString(R.string.variable_create_success));
							variablesViewModel.resetPagination();
							fetchVariables();
							dialog.dismiss();
						} else {
							Toasty.error(
									RepositoryActions.this,
									getString(R.string.variable_create_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						Toasty.error(
								RepositoryActions.this, getString(R.string.variable_create_failed));
					}
				});
	}

	private void setupScrollListener() {
		activityRepositoryActionsBinding.dataScrollView.setOnScrollChangeListener(
				(NestedScrollView.OnScrollChangeListener)
						(v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
							if (!v.canScrollVertically(1)
									&& !variablesViewModel.isLoading()
									&& activityRepositoryActionsBinding.variablesCard
													.getStrokeWidth()
											> 0) {
								fetchVariables();
							}
						});
	}

	private void fetchTaskCount() {

		Call<ActionTaskResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.listActionTasks(
								repositoryContext.getOwner(),
								repositoryContext.getName(),
								1,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionTaskResponse> call,
							@NonNull Response<ActionTaskResponse> response) {
						if (response.code() == 200 && response.body() != null) {
							taskCount = AppUtil.numberFormatter(response.body().getTotalCount());
						} else {
							taskCount = String.valueOf(0);
						}
						updateCountText();
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionTaskResponse> call, @NonNull Throwable t) {
						taskCount = String.valueOf(0);
						updateCountText();
					}
				});
	}

	private void fetchArtifactCount() {

		Call<ActionArtifactsResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.getArtifacts(
								repositoryContext.getOwner(), repositoryContext.getName(), null);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionArtifactsResponse> call,
							@NonNull Response<ActionArtifactsResponse> response) {
						if (response.code() == 200 && response.body() != null) {
							artifactCount =
									AppUtil.numberFormatter(response.body().getTotalCount());
						} else {
							artifactCount = String.valueOf(0);
						}
						updateCountText();
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionArtifactsResponse> call, @NonNull Throwable t) {
						artifactCount = String.valueOf(0);
						updateCountText();
					}
				});
	}

	private void fetchRunnerCount() {

		Call<ActionRunnersResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.customGetRepoRunners(
								repositoryContext.getOwner(), repositoryContext.getName());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionRunnersResponse> call,
							@NonNull Response<ActionRunnersResponse> response) {
						if (response.code() == 200 && response.body() != null) {
							runnerCount = AppUtil.numberFormatter(response.body().getTotalCount());
						} else {
							runnerCount = "0";
						}
						updateCountText();
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionRunnersResponse> call, @NonNull Throwable t) {
						runnerCount = "0";
						updateCountText();
					}
				});
	}

	private void updateCountText() {
		String countText =
				getString(R.string.actions_counts, taskCount, artifactCount, runnerCount);
		activityRepositoryActionsBinding.count.setText(countText);
	}

	private void fetchRunners() {
		Call<ActionRunnersResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.customGetRepoRunners(
								repositoryContext.getOwner(), repositoryContext.getName());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionRunnersResponse> call,
							@NonNull Response<ActionRunnersResponse> response) {
						if (response.code() == 200 && response.body() != null) {
							runnerCount = AppUtil.numberFormatter(response.body().getTotalCount());
							displayRunners(response.body().getRunners());
						} else {
							runnerCount = "0";
							displayRunners(null);
						}
						updateCountText();
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionRunnersResponse> call, @NonNull Throwable t) {
						runnerCount = "0";
						displayRunners(null);
						updateCountText();
					}
				});
	}

	private void fetchWorkflows() {

		Call<ActionWorkflowResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.actionsListRepositoryWorkflows(
								repositoryContext.getOwner(), repositoryContext.getName());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<ActionWorkflowResponse> call,
							@NonNull Response<ActionWorkflowResponse> response) {
						if (response.code() == 200 && response.body() != null) {
							displayWorkflows(response.body().getWorkflows());
						} else {
							displayWorkflows(null);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ActionWorkflowResponse> call, @NonNull Throwable t) {
						displayWorkflows(null);
					}
				});
	}

	private void fetchVariables() {
		variablesViewModel.fetchVariables(
				ctx, repositoryContext.getOwner(), repositoryContext.getName(), resultLimit);
	}

	private void displayRunners(List<ActionRunner> runners) {

		LinearLayout container = activityRepositoryActionsBinding.dataContainer;
		container.removeAllViews();

		if (runners == null || runners.isEmpty()) {
			displayNoData(container);
			return;
		}

		for (ActionRunner runner : runners) {
			View runnerView =
					LayoutInflater.from(ctx)
							.inflate(R.layout.list_action_runners, container, false);

			TextView name = runnerView.findViewById(R.id.runner_name);
			TextView status = runnerView.findViewById(R.id.runner_status);
			TextView busy = runnerView.findViewById(R.id.runner_busy);
			TextView labels = runnerView.findViewById(R.id.runner_labels);

			name.setText(runner.getName() != null ? runner.getName() : getString(R.string.na));
			status.setText(
					runner.getStatus() != null ? runner.getStatus() : getString(R.string.na));
			busy.setText(
					runner.isBusy() != null
							? (runner.isBusy()
									? getString(R.string.busy)
									: getString(R.string.idle))
							: getString(R.string.na));

			List<ActionRunnerLabel> runnerLabels = runner.getLabels();
			StringBuilder labelsText = new StringBuilder();
			if (runnerLabels != null && !runnerLabels.isEmpty()) {
				for (ActionRunnerLabel label : runnerLabels) {
					if (label.getName() != null) {
						labelsText.append(label.getName()).append(", ");
					}
				}
				if (labelsText.length() > 0) {
					labelsText.setLength(labelsText.length() - 2);
				}
			} else {
				labelsText.append(getString(R.string.none));
			}
			labels.setText(labelsText.toString());

			container.addView(runnerView);
		}
	}

	private void displayWorkflows(List<ActionWorkflow> workflows) {

		LinearLayout container = activityRepositoryActionsBinding.dataContainer;
		container.removeAllViews();

		if (workflows == null || workflows.isEmpty()) {
			displayNoData(container);
			return;
		}

		for (ActionWorkflow workflow : workflows) {
			View workflowView =
					LayoutInflater.from(ctx)
							.inflate(R.layout.list_action_workflows, container, false);

			TextView name = workflowView.findViewById(R.id.workflow_name);
			TextView path = workflowView.findViewById(R.id.workflow_path);
			TextView state = workflowView.findViewById(R.id.workflow_state);

			name.setText(workflow.getName() != null ? workflow.getName() : getString(R.string.na));
			path.setText(workflow.getPath() != null ? workflow.getPath() : getString(R.string.na));
			state.setText(
					workflow.getState() != null ? workflow.getState() : getString(R.string.na));

			container.addView(workflowView);
		}
	}

	private void displayVariables(List<ActionVariable> variables) {

		LinearLayout container = activityRepositoryActionsBinding.dataContainer;
		if (activityRepositoryActionsBinding.variablesCard.getStrokeWidth() == 0) {
			return;
		}

		container.removeAllViews();

		if (variables == null || variables.isEmpty()) {
			displayNoData(container);
			return;
		}

		for (ActionVariable variable : variables) {
			View variableView =
					LayoutInflater.from(ctx)
							.inflate(R.layout.list_action_variables, container, false);

			TextView name = variableView.findViewById(R.id.variable_name);
			TextView data = variableView.findViewById(R.id.variable_data);
			TextView description = variableView.findViewById(R.id.variable_description);

			name.setText(variable.getName() != null ? variable.getName() : getString(R.string.na));
			data.setText(variable.getData() != null ? variable.getData() : getString(R.string.na));
			if (!variable.getDescription().isEmpty()) {
				description.setText(
						variable.getDescription() != null
								? variable.getDescription()
								: getString(R.string.na));
			} else {
				description.setVisibility(View.GONE);
			}

			container.addView(variableView);
		}
	}

	private void displayNoData(LinearLayout container) {

		TextView noData = new TextView(ctx);
		noData.setText(R.string.noDataFound);
		LinearLayout.LayoutParams params =
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.WRAP_CONTENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin =
				(int)
						TypedValue.applyDimension(
								TypedValue.COMPLEX_UNIT_DIP,
								32,
								ctx.getResources().getDisplayMetrics());
		noData.setLayoutParams(params);
		noData.setGravity(android.view.Gravity.CENTER);
		container.addView(noData);
	}

	private void setActiveCard(com.google.android.material.card.MaterialCardView activeCard) {

		activityRepositoryActionsBinding.runnersCard.setStrokeWidth(
				activeCard == activityRepositoryActionsBinding.runnersCard
						? getResources().getDimensionPixelSize(R.dimen.dimen2dp)
						: 0);
		activityRepositoryActionsBinding.workflowsCard.setStrokeWidth(
				activeCard == activityRepositoryActionsBinding.workflowsCard
						? getResources().getDimensionPixelSize(R.dimen.dimen2dp)
						: 0);
		activityRepositoryActionsBinding.variablesCard.setStrokeWidth(
				activeCard == activityRepositoryActionsBinding.variablesCard
						? getResources().getDimensionPixelSize(R.dimen.dimen2dp)
						: 0);

		activityRepositoryActionsBinding.dataContainer.setVisibility(View.VISIBLE);
		activityRepositoryActionsBinding.runnersRecyclerView.setVisibility(View.GONE);
		activityRepositoryActionsBinding.workflowsRecyclerView.setVisibility(View.GONE);
		activityRepositoryActionsBinding.variablesRecyclerView.setVisibility(View.GONE);

		if (activeCard != activityRepositoryActionsBinding.runnersCard
				&& activeCard != activityRepositoryActionsBinding.workflowsCard
				&& activeCard != activityRepositoryActionsBinding.variablesCard) {
			activityRepositoryActionsBinding.dataContainer.removeAllViews();
		}
	}
}
