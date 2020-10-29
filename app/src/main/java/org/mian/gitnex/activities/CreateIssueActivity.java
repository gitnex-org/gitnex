package org.mian.gitnex.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.AssigneesActions;
import org.mian.gitnex.actions.LabelsActions;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateIssueBinding;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.Milestones;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateIssueActivity extends BaseActivity implements View.OnClickListener, LabelsListAdapter.LabelsListAdapterListener, AssigneesListAdapter.AssigneesListAdapterListener {

	private ActivityCreateIssueBinding viewBinding;
	private CustomLabelsSelectionDialogBinding labelsBinding;
	private CustomAssigneesSelectionDialogBinding assigneesBinding;
    private View.OnClickListener onClickListener;
    final Context ctx = this;
    private Context appCtx;
    private TinyDB tinyDb;
    private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private Dialog dialogLabels;
	private Dialog dialogAssignees;
	private String labelsSetter;
	private String assigneesSetter;
	private int milestoneId;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;

	private LabelsListAdapter labelsAdapter;
	private AssigneesListAdapter assigneesAdapter;

	private List<Integer> labelsIds = new ArrayList<>();
	private List<Labels> labelsList = new ArrayList<>();
	private List<Milestones> milestonesList = new ArrayList<>();
	private List<Collaborators> assigneesList = new ArrayList<>();
	private List<String> assigneesListData = new ArrayList<>();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_create_issue;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();
	    tinyDb = new TinyDB(appCtx);

	    viewBinding = ActivityCreateIssueBinding.inflate(getLayoutInflater());
	    View view = viewBinding.getRoot();
	    setContentView(view);

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        instanceUrl = tinyDb.getString("instanceUrl");
        loginUid = tinyDb.getString("loginUid");
        final String loginFullName = tinyDb.getString("userFullname");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];
        instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        // require gitea 1.12 or higher
        if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {

            resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
        }

	    viewBinding.newIssueTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(viewBinding.newIssueTitle, InputMethodManager.SHOW_IMPLICIT);

	    labelsAdapter = new LabelsListAdapter(labelsList, CreateIssueActivity.this, labelsIds);
	    assigneesAdapter = new AssigneesListAdapter(ctx, assigneesList, CreateIssueActivity.this, assigneesListData);

        initCloseListener();
	    viewBinding.close.setOnClickListener(onClickListener);

	    viewBinding.newIssueAssigneesList.setOnClickListener(this);
	    viewBinding.newIssueLabels.setOnClickListener(this);
	    viewBinding.newIssueDueDate.setOnClickListener(this);

        getMilestones(instanceUrl, instanceToken, repoOwner, repoName, loginUid, resultLimit);

        disableProcessButton();

	    viewBinding.newIssueLabels.setOnClickListener(newIssueLabels -> showLabels());

	    viewBinding.newIssueAssigneesList.setOnClickListener(newIssueAssigneesList -> showAssignees());

        if(!connToInternet) {

	        viewBinding.createNewIssueButton.setEnabled(false);
        }
        else {

	        viewBinding.createNewIssueButton.setOnClickListener(this);
        }

    }

	@Override
	public void assigneesInterface(List<String> data) {

		assigneesSetter = String.valueOf(data);
		viewBinding.newIssueAssigneesList.setText(assigneesSetter.replace("]", "").replace("[", ""));
		assigneesListData = data;
	}

	@Override
	public void labelsInterface(List<String> data) {

		labelsSetter = String.valueOf(data);
		viewBinding.newIssueLabels.setText(labelsSetter.replace("]", "").replace("[", ""));
	}

	@Override
	public void labelsIdsInterface(List<Integer> data) {

		labelsIds = data;
	}

	private void showAssignees() {

		dialogAssignees = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogAssignees.getWindow() != null) {

			dialogAssignees.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		assigneesBinding = CustomAssigneesSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = assigneesBinding.getRoot();
		dialogAssignees.setContentView(view);

		assigneesBinding.cancel.setOnClickListener(assigneesBinding_ -> dialogAssignees.dismiss());

		dialogAssignees.show();
		AssigneesActions.getRepositoryAssignees(ctx, instanceUrl, instanceToken, repoOwner, repoName, assigneesList, dialogAssignees, assigneesAdapter, assigneesBinding);
	}

	private void showLabels() {

		dialogLabels = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogLabels.getWindow() != null) {

			dialogLabels.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		labelsBinding = CustomLabelsSelectionDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = labelsBinding.getRoot();
		dialogLabels.setContentView(view);

		labelsBinding.cancel.setOnClickListener(labelsBinding_ -> dialogLabels.dismiss());

		dialogLabels.show();
		LabelsActions.getRepositoryLabels(ctx, instanceUrl, instanceToken, repoOwner, repoName, labelsList, dialogLabels, labelsAdapter, labelsBinding);
	}

    private void processNewIssue() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newIssueTitleForm = Objects.requireNonNull(viewBinding.newIssueTitle.getText()).toString();
        String newIssueDescriptionForm = Objects.requireNonNull(viewBinding.newIssueDescription.getText()).toString();
        String newIssueDueDateForm = Objects.requireNonNull(viewBinding.newIssueDueDate.getText()).toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if (newIssueTitleForm.equals("")) {

            Toasty.error(ctx, getString(R.string.issueTitleEmpty));
            return;
        }

        if (newIssueDueDateForm.equals("")) {

            newIssueDueDateForm = null;
        }
        else {

            newIssueDueDateForm = (AppUtil.customDateCombine(AppUtil.customDateFormat(newIssueDueDateForm)));
        }

        disableProcessButton();
        createNewIssueFunc(instanceUrl, instanceToken, repoOwner, repoName, loginUid, newIssueDescriptionForm, newIssueDueDateForm, milestoneId, newIssueTitleForm);
    }

    private void createNewIssueFunc(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String loginUid, String newIssueDescriptionForm, String newIssueDueDateForm, int newIssueMilestoneIdForm, String newIssueTitleForm) {

        CreateIssue createNewIssueJson = new CreateIssue(loginUid, newIssueDescriptionForm, false, newIssueDueDateForm, newIssueMilestoneIdForm, newIssueTitleForm, assigneesListData, labelsIds);

        Call<JsonElement> call3;

        call3 = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createNewIssue(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, createNewIssueJson);

        call3.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response2) {

				if(response2.code() == 201) {

                    TinyDB tinyDb = new TinyDB(appCtx);
                    tinyDb.putBoolean("resumeIssues", true);

                    Toasty.success(ctx, getString(R.string.issueCreated));
                    enableProcessButton();
                    finish();
                }
                else if(response2.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
                }
                else {

                    Toasty.error(ctx, getString(R.string.issueCreatedError));
                    enableProcessButton();
                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

	            Toasty.error(ctx, getString(R.string.genericServerResponseError));
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
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

                if(response.isSuccessful()) {

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

                        ArrayAdapter<Milestones> adapter = new ArrayAdapter<>(CreateIssueActivity.this,
                                R.layout.list_spinner_items, milestonesList);

	                    viewBinding.newIssueMilestoneSpinner.setAdapter(adapter);
                        enableProcessButton();

	                    viewBinding.newIssueMilestoneSpinner.setOnItemClickListener ((parent, view, position, id) ->

		                    milestoneId = milestonesList.get(position).getId()
	                    );

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

	            Toasty.error(ctx, getString(R.string.genericServerResponseError));
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == viewBinding.newIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
	            (view, year, monthOfYear, dayOfMonth) -> viewBinding.newIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth)), mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(v == viewBinding.createNewIssueButton) {

            processNewIssue();
        }
    }

    private void disableProcessButton() {

	    viewBinding.createNewIssueButton.setEnabled(false);
    }

    private void enableProcessButton() {

	    viewBinding.createNewIssueButton.setEnabled(true);
    }
}
