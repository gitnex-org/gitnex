package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityEditIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Milestones;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class EditIssueActivity extends BaseActivity implements View.OnClickListener {

    private View.OnClickListener onClickListener;
    private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;

    private EditText editIssueTitle;
    private EditText editIssueDescription;
    private TextView editIssueDueDate;
    private Button editIssueButton;
    private AutoCompleteTextView editIssueMilestoneSpinner;

    private String msState = "open";
    private int milestoneId;

    List<Milestones> milestonesList = new ArrayList<>();

	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;
	private int issueIndex;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_edit_issue;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityEditIssueBinding activityEditIssueBinding = ActivityEditIssueBinding.inflate(getLayoutInflater());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        loginUid = tinyDB.getString("loginUid");
        instanceToken = "token " + tinyDB.getString(loginUid + "-token");
        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];
        issueIndex = Integer.parseInt(tinyDB.getString("issueNumber"));

        ImageView closeActivity = activityEditIssueBinding.close;
        editIssueButton = activityEditIssueBinding.editIssueButton;
        TextView toolbar_title = activityEditIssueBinding.toolbarTitle;
        editIssueTitle = activityEditIssueBinding.editIssueTitle;
        editIssueDescription = activityEditIssueBinding.editIssueDescription;
        editIssueDueDate = activityEditIssueBinding.editIssueDueDate;

        // if gitea is 1.12 or higher use the new limit
        if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.0")) {

            resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
        }

        editIssueTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(editIssueTitle, InputMethodManager.SHOW_IMPLICIT);

	    editIssueDescription.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

        editIssueMilestoneSpinner = findViewById(R.id.editIssueMilestoneSpinner);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        editIssueDueDate.setOnClickListener(this);
        editIssueButton.setOnClickListener(this);

        if(!tinyDB.getString("issueNumber").isEmpty()) {

            if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

                toolbar_title.setText(getString(R.string.editPrNavHeader, String.valueOf(issueIndex)));
            }
            else {

                toolbar_title.setText(getString(R.string.editIssueNavHeader, String.valueOf(issueIndex)));
            }
        }

        disableProcessButton();
        getIssue(instanceToken, loginUid, repoOwner, repoName, issueIndex, resultLimit);
    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void processEditIssue() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String editIssueTitleForm = editIssueTitle.getText().toString();
        String editIssueDescriptionForm = editIssueDescription.getText().toString();
        String editIssueDueDateForm = editIssueDueDate.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if (editIssueTitleForm.equals("")) {

            Toasty.error(ctx, getString(R.string.issueTitleEmpty));
            return;
        }

        if (editIssueDueDateForm.equals("")) {

            editIssueDueDateForm = null;
        }
        else {

            editIssueDueDateForm = (AppUtil.customDateCombine(AppUtil.customDateFormat(editIssueDueDateForm)));
        }

        disableProcessButton();
        editIssue(instanceToken, repoOwner, repoName, issueIndex, loginUid, editIssueTitleForm, editIssueDescriptionForm, editIssueDueDateForm, milestoneId);
    }

    private void editIssue(String instanceToken, String repoOwner, String repoName, int issueIndex, String loginUid, String title, String description, String dueDate, int milestoneId) {

        CreateIssue issueData = new CreateIssue(title, description, dueDate, milestoneId);

        Call<JsonElement> call = RetrofitClient
                .getApiInterface(ctx)
                .patchIssue(Authorization.get(ctx), repoOwner, repoName, issueIndex, issueData);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

                        Toasty.success(ctx, getString(R.string.editPrSuccessMessage));
                    }
                    else {

                        Toasty.success(ctx, getString(R.string.editIssueSuccessMessage));
                    }

                    tinyDB.putBoolean("issueEdited", true);
                    tinyDB.putBoolean("resumeIssues", true);
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == editIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
	            (view, year, monthOfYear, dayOfMonth) -> editIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth)), mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(v == editIssueButton) {

            processEditIssue();
        }

    }

    private void getIssue(final String instanceToken, final String loginUid, final String repoOwner, final String repoName, int issueIndex, int resultLimit) {

        Call<Issues> call = RetrofitClient
                .getApiInterface(ctx)
                .getIssueByIndex(Authorization.get(ctx), repoOwner, repoName, issueIndex);

        call.enqueue(new Callback<Issues>() {

            @Override
            public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

                if(response.code() == 200) {

                    assert response.body() != null;
                    editIssueTitle.setText(response.body().getTitle());
                    editIssueDescription.setText(response.body().getBody());

                    int currentMilestoneId = 0;
                    if(response.body().getMilestone() != null) {

	                    currentMilestoneId = response.body().getMilestone().getId();
                    }

                    // get milestones list
                    if(response.body().getId() > 0) {

                        Call<List<Milestones>> call_ = RetrofitClient
                                .getApiInterface(ctx)
                                .getMilestones(Authorization.get(ctx), repoOwner, repoName, 1, resultLimit, msState);

	                    int checkMilestoneId = currentMilestoneId;

	                    call_.enqueue(new Callback<List<Milestones>>() {

                            @Override
                            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull retrofit2.Response<List<Milestones>> response_) {

                                int getSelectedMilestoneId = 0;

                                if (response_.code() == 200) {

                                    List<Milestones> milestonesList_ = response_.body();

                                    milestonesList.add(new Milestones(0, "No milestone"));
                                    assert milestonesList_ != null;

                                    if (milestonesList_.size() > 0) {

	                                    milestonesList.addAll(milestonesList_);

                                        for (int i = 0; i < milestonesList_.size(); i++) {

                                            if(checkMilestoneId == milestonesList_.get(i).getId()) {
	                                            getSelectedMilestoneId = i + 1;
                                            }
                                        }
                                    }

                                    ArrayAdapter<Milestones> adapter = new ArrayAdapter<>(EditIssueActivity.this,
                                            R.layout.list_spinner_items, milestonesList);

                                    editIssueMilestoneSpinner.setAdapter(adapter);

	                                editIssueMilestoneSpinner.setOnItemClickListener ((parent, view, position, id) -> milestoneId = milestonesList.get(position).getId());

	                                int finalMsId = getSelectedMilestoneId;
	                                new Handler(Looper.getMainLooper()).postDelayed(() -> {

		                                editIssueMilestoneSpinner.setText(milestonesList.get(finalMsId).getTitle(),false);
		                                milestoneId = milestonesList.get(finalMsId).getId();
	                                }, 500);

                                    enableProcessButton();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

                                Log.e("onFailure", t.toString());
                            }
                        });

                    }
                    // get milestones list

                    if(response.body().getDue_date() != null) {

                        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy-M-dd");
                        String dueDate = formatter.format(response.body().getDue_date());
                        editIssueDueDate.setText(dueDate);
                    }
                    //enableProcessButton();

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
                }
                else {

                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
            }
        });

    }

    private void disableProcessButton() {

        editIssueButton.setEnabled(false);
    }

    private void enableProcessButton() {

        editIssueButton.setEnabled(true);
    }

}
