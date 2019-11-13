package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.annotation.SuppressLint;
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
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.CreateIssue;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Author M M Arif
 */

public class EditIssueActivity extends AppCompatActivity implements View.OnClickListener {

    final Context ctx = this;
    private View.OnClickListener onClickListener;

    private EditText editIssueTitle;
    private SocialAutoCompleteTextView editIssueDescription;
    private TextView editIssueDueDate;
    private Button editIssueButton;
    private Spinner editIssueMilestoneSpinner;

    private String msState = "open";

    List<Milestones> milestonesList = new ArrayList<>();
    private ArrayAdapter<Mention> defaultMentionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_issue);

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        ImageView closeActivity = findViewById(R.id.close);
        editIssueButton = findViewById(R.id.editIssueButton);
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        editIssueTitle = findViewById(R.id.editIssueTitle);
        editIssueDescription = findViewById(R.id.editIssueDescription);
        editIssueDueDate = findViewById(R.id.editIssueDueDate);

        defaultMentionAdapter = new MentionArrayAdapter<>(this);
        loadCollaboratorsList();

        editIssueMilestoneSpinner = findViewById(R.id.editIssueMilestoneSpinner);
        editIssueMilestoneSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        editIssueDescription.setMentionAdapter(defaultMentionAdapter);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        editIssueDueDate.setOnClickListener(this);
        editIssueButton.setOnClickListener(this);

        if(!tinyDb.getString("issueNumber").isEmpty()) {
            toolbar_title.setText(getString(R.string.editIssueNavHeader, String.valueOf(issueIndex)));
        }

        disableProcessButton();
        getIssue(instanceUrl, instanceToken, loginUid, repoOwner, repoName, issueIndex);


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

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private void processEditIssue() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        Milestones mModel = (Milestones) editIssueMilestoneSpinner.getSelectedItem();

        int editIssueMilestoneId = mModel.getId();

        String editIssueTitleForm = editIssueTitle.getText().toString();
        String editIssueDescriptionForm = editIssueDescription.getText().toString();
        String editIssueDueDateForm = editIssueDueDate.getText().toString();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if (editIssueTitleForm.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.issueTitleEmpty));
            return;

        }

        /*if (editIssueDescriptionForm.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.issueDescriptionEmpty));
            return;

        }*/

        if (editIssueDueDateForm.equals("")) {
            editIssueDueDateForm = null;
        } else {
            editIssueDueDateForm = (AppUtil.customDateCombine(AppUtil.customDateFormat(editIssueDueDateForm)));
        }

        //Log.i("editIssueDueDateForm", String.valueOf(editIssueDueDateForm));
        disableProcessButton();
        editIssue(instanceUrl, instanceToken, repoOwner, repoName, issueIndex, loginUid, editIssueTitleForm, editIssueDescriptionForm, editIssueDueDateForm, editIssueMilestoneId);

    }

    private void editIssue(String instanceUrl, String instanceToken, String repoOwner, String repoName, int issueIndex, String loginUid, String title, String description, String dueDate, int editIssueMilestoneId) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        CreateIssue issueData = new CreateIssue(title, description, dueDate, editIssueMilestoneId);

        Call<JsonElement> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .patchIssue(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, issueData);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    Toasty.info(getApplicationContext(), getString(R.string.editIssueSuccessMessage));
                    tinyDb.putBoolean("issueEdited", true);
                    tinyDb.putBoolean("resumeIssues", true);
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
                    Toasty.info(getApplicationContext(), getString(R.string.genericError));

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
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year,
                                              int monthOfYear, int dayOfMonth) {

                            editIssueDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth));

                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();

        }
        else if(v == editIssueButton) {
            processEditIssue();
        }

    }

    private void getIssue(final String instanceUrl, final String instanceToken, final String loginUid, final String repoOwner, final String repoName, int issueIndex) {

        Call<Issues> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getIssueByIndex(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex);

        call.enqueue(new Callback<Issues>() {

            @Override
            public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

                if(response.code() == 200) {

                    assert response.body() != null;
                    editIssueTitle.setText(response.body().getTitle());
                    editIssueDescription.setText(response.body().getBody());

                    int msId = 0;
                    if(response.body().getMilestone() != null) {
                        msId = response.body().getMilestone().getId();
                    }

                    // get milestones list
                    if(response.body().getId() > 0) {

                        Call<List<Milestones>> call_ = RetrofitClient
                                .getInstance(instanceUrl, getApplicationContext())
                                .getApiInterface()
                                .getMilestones(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, msState);

                        final int finalMsId = msId;

                        call_.enqueue(new Callback<List<Milestones>>() {

                            @Override
                            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull retrofit2.Response<List<Milestones>> response_) {

                                int finalMsId1 = 0;

                                if (response_.code() == 200) {

                                    List<Milestones> milestonesList_ = response_.body();

                                    milestonesList.add(new Milestones(0, "No milestone"));
                                    assert milestonesList_ != null;
                                    if (milestonesList_.size() > 0) {
                                        for (int i = 0; i < milestonesList_.size(); i++) {

                                            Milestones data = new Milestones(
                                                    milestonesList_.get(i).getId(),
                                                    milestonesList_.get(i).getTitle()
                                            );
                                            milestonesList.add(data);

                                            if(finalMsId == milestonesList_.get(i).getId()) {
                                                finalMsId1 = i + 1;
                                            }

                                        }
                                    }

                                    ArrayAdapter<Milestones> adapter_ = new ArrayAdapter<>(getApplicationContext(),
                                            R.layout.spinner_item, milestonesList);

                                    adapter_.setDropDownViewResource(R.layout.spinner_dropdown_item);
                                    editIssueMilestoneSpinner.setAdapter(adapter_);

                                    if(milestonesList_.size() > 0) {
                                        editIssueMilestoneSpinner.setSelection(finalMsId1);
                                    }
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

                    Toasty.info(getApplicationContext(), getString(R.string.genericError));

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
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        editIssueButton.setBackground(shape);

    }

    private void enableProcessButton() {

        editIssueButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        editIssueButton.setBackground(shape);

    }

}
