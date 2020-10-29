package org.mian.gitnex.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreatePrBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.CreatePullRequest;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.Milestones;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreatePullRequestActivity extends BaseActivity implements LabelsListAdapter.LabelsListAdapterListener {

	private View.OnClickListener onClickListener;
	private Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;
	private ActivityCreatePrBinding viewBinding;
	private CustomLabelsSelectionDialogBinding labelsBinding;
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private String labelsSetter;
	private List<Integer> labelsIds = new ArrayList<>();
	private List<String> assignees = new ArrayList<>();
	private int milestoneId;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;

	private LabelsListAdapter labelsAdapter;

	List<Milestones> milestonesList = new ArrayList<>();
	List<Branches> branchesList = new ArrayList<>();
	List<Labels> labelsList = new ArrayList<>();

	public CreatePullRequestActivity() {
	}

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_create_pr;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		viewBinding = ActivityCreatePrBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		instanceUrl = tinyDb.getString("instanceUrl");
		loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		repoOwner = parts[0];
		repoName = parts[1];
		instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		// require gitea 1.12 or higher
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		labelsAdapter =  new LabelsListAdapter(labelsList, CreatePullRequestActivity.this, labelsIds);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		viewBinding.prDueDate.setOnClickListener(dueDate ->
			setDueDate()
		);

		disableProcessButton();

		getMilestones(instanceUrl, instanceToken, repoOwner, repoName, loginUid, resultLimit);
		getBranches(instanceUrl, instanceToken, repoOwner, repoName, loginUid);

		viewBinding.prLabels.setOnClickListener(prLabels -> showLabels());

		viewBinding.createPr.setOnClickListener(createPr -> processPullRequest());
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

		CreatePullRequest createPullRequest = new CreatePullRequest(prTitle, prDescription, loginUid, mergeInto, pullFrom, milestoneId, dueDate, assignees, labelsIds);

		Call<ResponseBody> transferCall = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.createPullRequest(instanceToken, repoOwner, repoName, createPullRequest);

		transferCall.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

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
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

				enableProcessButton();
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void labelsInterface(List<String> data) {

		labelsSetter = String.valueOf(data);
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

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.cancel.setOnClickListener(editProperties -> dialogLabels.dismiss());

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, instanceUrl, instanceToken, repoOwner, repoName, labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

	private void getBranches(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid) {

		Call<List<Branches>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getBranches(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

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

	private void getMilestones(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid, int resultLimit) {

		String msState = "open";
		Call<List<Milestones>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getMilestones(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, 1, resultLimit, msState);

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
}
