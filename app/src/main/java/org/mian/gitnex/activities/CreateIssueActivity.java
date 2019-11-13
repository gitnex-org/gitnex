package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.gson.JsonElement;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.models.MultiSelectModel;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class CreateIssueActivity extends AppCompatActivity implements View.OnClickListener {

    private View.OnClickListener onClickListener;
    MultiSelectDialog multiSelectDialog;
    MultiSelectDialog multiSelectDialogLabels;
    private TextView assigneesList;
    private TextView newIssueLabels;
    private TextView newIssueDueDate;
    private Spinner newIssueMilestoneSpinner;
    private EditText newIssueTitle;
    private SocialAutoCompleteTextView newIssueDescription;
    private Button createNewIssueButton;
    private TextView labelsIdHolder;
    private boolean assigneesFlag;
    private boolean labelsFlag;
    final Context ctx = this;

    List<Milestones> milestonesList = new ArrayList<>();
    ArrayList<MultiSelectModel> listOfAssignees = new ArrayList<>();
    ArrayList<MultiSelectModel> listOfLabels= new ArrayList<>();
    private ArrayAdapter<Mention> defaultMentionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_issue);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        assigneesList = findViewById(R.id.newIssueAssigneesList);
        newIssueLabels = findViewById(R.id.newIssueLabels);
        newIssueDueDate = findViewById(R.id.newIssueDueDate);
        createNewIssueButton = findViewById(R.id.createNewIssueButton);
        newIssueTitle = findViewById(R.id.newIssueTitle);
        newIssueDescription = findViewById(R.id.newIssueDescription);
        labelsIdHolder = findViewById(R.id.labelsIdHolder);

        defaultMentionAdapter = new MentionArrayAdapter<>(this);
        loadCollaboratorsList();

        newIssueDescription.setMentionAdapter(defaultMentionAdapter);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        assigneesList.setOnClickListener(this);
        newIssueLabels.setOnClickListener(this);
        newIssueDueDate.setOnClickListener(this);

        newIssueMilestoneSpinner = findViewById(R.id.newIssueMilestoneSpinner);
        newIssueMilestoneSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getMilestones(instanceUrl, instanceToken, repoOwner, repoName, loginUid);

        getLabels(instanceUrl, instanceToken, repoOwner, repoName, loginUid);
        getCollaborators(instanceUrl, instanceToken, repoOwner, repoName, loginUid);

        disableProcessButton();

        if(!connToInternet) {

            createNewIssueButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createNewIssueButton.setBackground(shape);

        } else {

            createNewIssueButton.setOnClickListener(this);

        }

    }

    private void processNewIssue() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        Milestones mModel = (Milestones) newIssueMilestoneSpinner.getSelectedItem();

        int newIssueMilestoneIdForm = mModel.getId();
        String newIssueTitleForm = newIssueTitle.getText().toString();
        String newIssueDescriptionForm = newIssueDescription.getText().toString();
        String newIssueAssigneesListForm = assigneesList.getText().toString();
        //String newIssueLabelsForm = newIssueLabels.getText().toString();
        String newIssueDueDateForm = newIssueDueDate.getText().toString();
        String newIssueLabelsIdHolderForm = labelsIdHolder.getText().toString();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if (newIssueTitleForm.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.issueTitleEmpty));
            return;

        }

        /*if (newIssueDescriptionForm.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.issueDescriptionEmpty));
            return;

        }*/

        if (newIssueDueDateForm.equals("")) {
            newIssueDueDateForm = null;
        } else {
            newIssueDueDateForm = (AppUtil.customDateCombine(AppUtil.customDateFormat(newIssueDueDateForm)));
        }

        List<String> newIssueAssigneesListForm_ = new ArrayList<>(Arrays.asList(newIssueAssigneesListForm.split(",")));

        for (int i = 0; i < newIssueAssigneesListForm_.size(); i++) {
            newIssueAssigneesListForm_.set(i, newIssueAssigneesListForm_.get(i).trim());
        }

        int[] integers;
        if (!newIssueLabelsIdHolderForm.equals("")) {

            String[] items = newIssueLabelsIdHolderForm.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\s", "").split(",");
            integers = new int[items.length];
            for (int i = 0; i < integers.length; i++) {
                integers[i] = Integer.parseInt(items[i]);
            }

        }
        else {
            integers = new int[0];
        }

        //Log.i("FormData", String.valueOf(newIssueLabelsForm));
        disableProcessButton();
        createNewIssueFunc(instanceUrl, instanceToken, repoOwner, repoName, loginUid, newIssueDescriptionForm, newIssueDueDateForm, newIssueMilestoneIdForm, newIssueTitleForm, newIssueAssigneesListForm_, integers);

    }

    public void loadCollaboratorsList() {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

                if (response.isSuccessful()) {

                    assert response.body() != null;
                    String fullName = "";
                    for (int i = 0; i < response.body().size(); i++) {
                        if(!response.body().get(i).getFull_name().equals("")) {
                            fullName = response.body().get(i).getFull_name();
                        }
                        defaultMentionAdapter.add(
                                new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
                    }

                } else {

                    Log.i("onResponse", String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

    private void createNewIssueFunc(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String loginUid, String newIssueDescriptionForm, String newIssueDueDateForm, int newIssueMilestoneIdForm, String newIssueTitleForm, List<String> newIssueAssigneesListForm, int[] newIssueLabelsForm) {

        CreateIssue createNewIssueJson = new CreateIssue(loginUid, newIssueDescriptionForm, false, newIssueDueDateForm, newIssueMilestoneIdForm, newIssueTitleForm, newIssueAssigneesListForm, newIssueLabelsForm);

        Call<JsonElement> call3;

        call3 = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .createNewIssue(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, createNewIssueJson);

        call3.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response2) {

                if(response2.isSuccessful()) {
                    if(response2.code() == 201) {

                        //Log.i("isSuccessful1", String.valueOf(response2.body()));
                        TinyDB tinyDb = new TinyDB(getApplicationContext());
                        tinyDb.putBoolean("resumeIssues", true);

                        Toasty.info(getApplicationContext(), getString(R.string.issueCreated));
                        enableProcessButton();
                        finish();

                    }

                }
                else if(response2.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.issueCreatedError));
                    enableProcessButton();
                    //Log.i("isSuccessful2", String.valueOf(response2.body()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private void getMilestones(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid) {

        String msState = "open";
        Call<List<Milestones>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getMilestones(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, msState);

        call.enqueue(new Callback<List<Milestones>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull retrofit2.Response<List<Milestones>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Milestones> milestonesList_ = response.body();

                        milestonesList.add(new Milestones(0,"No milestone"));
                        assert milestonesList_ != null;
                        if(milestonesList_.size() > 0) {
                            for (int i = 0; i < milestonesList_.size(); i++) {

                                //String mStone = getString(R.string.spinnerMilestoneText, milestonesList_.get(i).getTitle(), milestonesList_.get(i).getState());
                                if(milestonesList_.get(i).getState().equals(getString(R.string.issueStatusOpen))) {
                                    Milestones data = new Milestones(
                                            milestonesList_.get(i).getId(),
                                            milestonesList_.get(i).getTitle()
                                    );
                                    milestonesList.add(data);
                                }

                            }
                        }

                        ArrayAdapter<Milestones> adapter = new ArrayAdapter<>(getApplicationContext(),
                                R.layout.spinner_item, milestonesList);

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        newIssueMilestoneSpinner.setAdapter(adapter);
                        enableProcessButton();

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void getCollaborators(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid) {

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull retrofit2.Response<List<Collaborators>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Collaborators> assigneesList_ = response.body();

                        assert assigneesList_ != null;
                        if(assigneesList_.size() > 0) {
                            for (int i = 0; i < assigneesList_.size(); i++) {

                                /*String assigneesCopy;
                                if(!assigneesList_.get(i).getFull_name().equals("")) {
                                    assigneesCopy = getString(R.string.dialogAssignessText, assigneesList_.get(i).getFull_name(), assigneesList_.get(i).getLogin());
                                }
                                else {
                                    assigneesCopy = assigneesList_.get(i).getLogin();
                                }*/
                                listOfAssignees.add(new MultiSelectModel(assigneesList_.get(i).getId(), assigneesList_.get(i).getLogin().trim()));

                            }
                            assigneesFlag = true;
                        }

                        multiSelectDialog = new MultiSelectDialog()
                                .title(getResources().getString(R.string.newIssueSelectAssigneesListTitle))
                                .titleSize(25)
                                .positiveText(getResources().getString(R.string.okButton))
                                .negativeText(getResources().getString(R.string.cancelButton))
                                .setMinSelectionLimit(0)
                                .setMaxSelectionLimit(listOfAssignees.size())
                                .multiSelectList(listOfAssignees)
                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                    @Override
                                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                                        assigneesList.setText(dataString);

                                    }

                                    @Override
                                    public void onCancel() {
                                        //Log.d("multiSelect","Dialog cancelled");

                                    }
                                });

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void getLabels(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid) {

        Call<List<Labels>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getlabels(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Labels>>() {

            @Override
            public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Labels> labelsList_ = response.body();

                        assert labelsList_ != null;
                        if(labelsList_.size() > 0) {
                            for (int i = 0; i < labelsList_.size(); i++) {

                                listOfLabels.add(new MultiSelectModel(labelsList_.get(i).getId(), labelsList_.get(i).getName().trim()));

                            }
                            labelsFlag = true;
                        }

                        multiSelectDialogLabels = new MultiSelectDialog()
                                .title(getResources().getString(R.string.newIssueSelectLabelsListTitle))
                                .titleSize(25)
                                .positiveText(getResources().getString(R.string.okButton))
                                .negativeText(getResources().getString(R.string.cancelButton))
                                .setMinSelectionLimit(0)
                                .setMaxSelectionLimit(listOfLabels.size())
                                .multiSelectList(listOfLabels)
                                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                                    @Override
                                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {

                                        newIssueLabels.setText(dataString.trim());
                                        labelsIdHolder.setText(selectedIds.toString());

                                    }

                                    @Override
                                    public void onCancel() {
                                        //Log.d("multiSelect","Dialog cancelled");

                                    }
                                });

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == assigneesList) {
            if(assigneesFlag) {
                multiSelectDialog.show(getSupportFragmentManager(), "multiSelectDialog");
            }
            else {
                Toasty.info(getApplicationContext(), getResources().getString(R.string.noAssigneesFound));
            }
        }
        else if (v == newIssueLabels) {
            if(labelsFlag) {
                multiSelectDialogLabels.show(getSupportFragmentManager(), "multiSelectDialogLabels");
            }
            else {
                Toasty.info(getApplicationContext(), getResources().getString(R.string.noLabelsFound));
            }
        }
        else if (v == newIssueDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            newIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(v == createNewIssueButton) {
            processNewIssue();
        }

    }

    private void disableProcessButton() {

        createNewIssueButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createNewIssueButton.setBackground(shape);

    }

    private void enableProcessButton() {

        createNewIssueButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createNewIssueButton.setBackground(shape);

    }
}
