package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.Branches;
import org.gitnex.tea4j.models.CreatePullRequest;
import org.gitnex.tea4j.models.Labels;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreatePullRequestActivity extends BaseActivity implements LabelsListAdapter.LabelsListAdapterListener {

	private View.OnClickListener onClickListener;
	private ActivityCreatePrBinding viewBinding;
	private int resultLimit = Constants.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private List<Integer> labelsIds = new ArrayList<>();
	private final List<String> assignees = new ArrayList<>();
	private int milestoneId;

	private RepositoryContext repository;

	private LabelsListAdapter labelsAdapter;

	List<Milestones> milestonesList = new ArrayList<>();
	List<Branches> branchesList = new ArrayList<>();
	List<Labels> labelsList = new ArrayList<>();

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityCreatePrBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		// require gitea 1.12 or higher
		if(getAccount().requiresVersion("1.12.0")) {

			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		viewBinding.prBody.setOnTouchListener((touchView, motionEvent) -> {

			touchView.getParent().requestDisallowInterceptTouchEvent(true);

			if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

				touchView.getParent().requestDisallowInterceptTouchEvent(false);
			}
			return false;
		});

		labelsAdapter =  new LabelsListAdapter(labelsList, CreatePullRequestActivity.this, labelsIds);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		viewBinding.prDueDate.setOnClickListener(dueDate ->
			setDueDate()
		);

		disableProcessButton();

		getMilestones(repository.getOwner(), repository.getName(), resultLimit);
		getBranches(repository.getOwner(), repository.getName());

		viewBinding.prLabels.setOnClickListener(prLabels -> showLabels());

		viewBinding.createPr.setOnClickListener(createPr -> processPullRequest());

		if(!repository.getPermissions().canPush()) {
			viewBinding.prDueDateLayout.setVisibility(View.GONE);
			viewBinding.prLabelsLayout.setVisibility(View.GONE);
		}
	}

	private void processPullRequest() {

		String prTitle = String.valueOf(viewBinding.prTitle.getText());
		String prDescription = String.valueOf(viewBinding.prBody.getText());
		String mergeInto = viewBinding.mergeIntoBranchSpinner.getText().toString();
		String pullFrom = viewBinding.pullFromBranchSpinner.getText().toString();
		String dueDate = String.valueOf(viewBinding.prDueDate.getText());

		assignees.add("");

		if (labelsIds.size() == 0) {

			labelsIds.add(0);
		}

		if (dueDate.matches("")) {

			dueDate = null;
		}
		else {

			dueDate = AppUtil.customDateCombine(AppUtil.customDateFormat(dueDate));
		}

		if(prTitle.matches("")) {

			Toasty.error(ctx, getString(R.string.titleError));
		}
		else if(mergeInto.matches("")) {

			Toasty.error(ctx, getString(R.string.mergeIntoError));
		}
		else if(pullFrom.matches("")) {

			Toasty.error(ctx, getString(R.string.pullFromError));
		}
		else if(pullFrom.equals(mergeInto)) {

			Toasty.error(ctx, getString(R.string.sameBranchesError));
		}
		else {

			createPullRequest(prTitle, prDescription, mergeInto, pullFrom, milestoneId, dueDate, assignees);
		}
	}

	private void createPullRequest(String prTitle, String prDescription, String mergeInto, String pullFrom, int milestoneId, String dueDate, List<String> assignees) {

		CreatePullRequest createPullRequest = new CreatePullRequest(prTitle, prDescription, getAccount().getAccount().getUserName(), mergeInto, pullFrom, milestoneId, dueDate, assignees, labelsIds);

		Call<Void> transferCall = RetrofitClient
			.getApiInterface(ctx)
			.createPullRequest(getAccount().getAuthorization(), repository.getOwner(), repository.getName(), createPullRequest);

		transferCall.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				disableProcessButton();

				if (response.code() == 201) {

					Toasty.success(ctx, getString(R.string.prCreateSuccess));
					finish();
				}
				else if (response.code() == 409 && response.message().equals("Conflict")) {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.prAlreadyExists));
				}
				else if (response.code() == 404) {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.apiNotFound));
				}
				else {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				enableProcessButton();
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void labelsInterface(List<String> data) {

		String labelsSetter = String.valueOf(data);
		viewBinding.prLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	private void showLabels() {

		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding labelsBinding = CustomLabelsSelectionDialogBinding
			.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.save.setOnClickListener(editProperties -> dialogLabels.dismiss());

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, repository.getOwner(), repository.getName(), labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

	private void getBranches(String repoOwner, String repoName) {

		Call<List<Branches>> call = RetrofitClient
			.getApiInterface(ctx)
			.getBranches(getAccount().getAuthorization(), repoOwner, repoName);

		call.enqueue(new Callback<List<Branches>>() {

			@Override
			public void onResponse(@NonNull Call<List<Branches>> call, @NonNull retrofit2.Response<List<Branches>> response) {

				if(response.isSuccessful()) {

					if(response.code() == 200) {

						List<Branches> branchesList_ = response.body();
						assert branchesList_ != null;

						if(branchesList_.size() > 0) {

							for (int i = 0; i < branchesList_.size(); i++) {

								Branches data = new Branches(branchesList_.get(i).getName());
								branchesList.add(data);
							}
						}

						ArrayAdapter<Branches> adapter = new ArrayAdapter<>(CreatePullRequestActivity.this,
							R.layout.list_spinner_items, branchesList);

						viewBinding.mergeIntoBranchSpinner.setAdapter(adapter);
						viewBinding.pullFromBranchSpinner.setAdapter(adapter);
						enableProcessButton();

					}
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

	}

	private void getMilestones(String repoOwner, String repoName, int resultLimit) {

		String msState = "open";
		Call<List<Milestones>> call = RetrofitClient
			.getApiInterface(ctx)
			.getMilestones(getAccount().getAuthorization(), repoOwner, repoName, 1, resultLimit, msState);

		call.enqueue(new Callback<List<Milestones>>() {

			@Override
			public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull retrofit2.Response<List<Milestones>> response) {

				if(response.code() == 200) {

					List<Milestones> milestonesList_ = response.body();

					milestonesList.add(new Milestones(0,getString(R.string.issueCreatedNoMilestone)));
					assert milestonesList_ != null;

					if(milestonesList_.size() > 0) {

						for (int i = 0; i < milestonesList_.size(); i++) {

							//Don't translate "open" is a enum
							if(milestonesList_.get(i).getState().equals("open")) {
								Milestones data = new Milestones(
									milestonesList_.get(i).getId(),
									milestonesList_.get(i).getTitle()
								);
								milestonesList.add(data);
							}
						}
					}

					ArrayAdapter<Milestones> adapter = new ArrayAdapter<>(CreatePullRequestActivity.this,
						R.layout.list_spinner_items, milestonesList);

					viewBinding.milestonesSpinner.setAdapter(adapter);
					enableProcessButton();

					viewBinding.milestonesSpinner.setOnItemClickListener ((parent, view, position, id) ->

						milestoneId = milestonesList.get(position).getId()
					);

				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

	}

	private void setDueDate() {

		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		final int mMonth = c.get(Calendar.MONTH);
		final int mDay = c.get(Calendar.DAY_OF_MONTH);

		DatePickerDialog datePickerDialog = new DatePickerDialog(this,
			(view, year, monthOfYear, dayOfMonth) -> viewBinding.prDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth)), mYear, mMonth, mDay);
		datePickerDialog.show();
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		viewBinding.createPr.setEnabled(false);
	}

	private void enableProcessButton() {

		viewBinding.createPr.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
