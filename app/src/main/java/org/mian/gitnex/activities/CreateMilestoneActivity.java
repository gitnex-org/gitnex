package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Milestones;
import java.util.Calendar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateMilestoneActivity extends BaseActivity implements View.OnClickListener {

    private EditText milestoneDueDate;
    private View.OnClickListener onClickListener;
    private EditText milestoneTitle;
    private EditText milestoneDescription;
    private Button createNewMilestoneButton;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_new_milestone;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        milestoneDueDate = findViewById(R.id.milestoneDueDate);
        ImageView closeActivity = findViewById(R.id.close);
        createNewMilestoneButton = findViewById(R.id.createNewMilestoneButton);
        milestoneTitle = findViewById(R.id.milestoneTitle);
        milestoneDescription = findViewById(R.id.milestoneDescription);

        milestoneTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(milestoneTitle, InputMethodManager.SHOW_IMPLICIT);

	    milestoneDescription.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);
        milestoneDueDate.setOnClickListener(this);

        if(!connToInternet) {

            createNewMilestoneButton.setEnabled(false);
        }
        else {

            createNewMilestoneButton.setOnClickListener(createMilestoneListener);
        }

    }

    private final View.OnClickListener createMilestoneListener = v -> processNewMilestone();

    private void processNewMilestone() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = TinyDB.getInstance(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String newMilestoneTitle = milestoneTitle.getText().toString();
        String newMilestoneDescription = milestoneDescription.getText().toString();
        String newMilestoneDueDate = milestoneDueDate.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newMilestoneTitle.equals("")) {

            Toasty.error(ctx, getString(R.string.milestoneNameErrorEmpty));
            return;
        }

        if(!newMilestoneDescription.equals("")) {

            if (appUtil.charactersLength(newMilestoneDescription) > 255) {

                Toasty.warning(ctx, getString(R.string.milestoneDescError));
                return;
            }
        }

        String finalMilestoneDueDate = null;

        if(!newMilestoneDueDate.isEmpty()) {

            finalMilestoneDueDate = (AppUtil.customDateCombine(AppUtil.customDateFormat(newMilestoneDueDate)));
        }
        else if (new Version(tinyDb.getString("giteaVersion")).less("1.10.0")) {

            // if Gitea version is less than 1.10.0 DueDate is required
            Toasty.warning(ctx, getString(R.string.milestoneDateEmpty));
            return;
        }

        disableProcessButton();
        createNewMilestone(Authorization.get(ctx), repoOwner, repoName, newMilestoneTitle, newMilestoneDescription, finalMilestoneDueDate);
    }

    private void createNewMilestone(final String token, String repoOwner, String repoName, String newMilestoneTitle, String newMilestoneDescription, String newMilestoneDueDate) {

        Milestones createMilestone = new Milestones(newMilestoneDescription, newMilestoneTitle, newMilestoneDueDate);

        Call<Milestones> call;

        call = RetrofitClient
                .getApiInterface(appCtx)
                .createMilestone(token, repoOwner, repoName, createMilestone);

        call.enqueue(new Callback<Milestones>() {

            @Override
            public void onResponse(@NonNull Call<Milestones> call, @NonNull retrofit2.Response<Milestones> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 201) {

                        TinyDB tinyDb = TinyDB.getInstance(appCtx);
                        tinyDb.putBoolean("milestoneCreated", true);
                        Toasty.success(ctx, getString(R.string.milestoneCreated));
                        enableProcessButton();
                        finish();
                    }
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
                    Toasty.error(ctx, getString(R.string.milestoneCreatedError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Milestones> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v == milestoneDueDate) {

            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            final int mMonth = c.get(Calendar.MONTH);
            final int mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
	            (view, year, monthOfYear, dayOfMonth) -> milestoneDueDate.setText(getString(R.string.setDueDate, year, (monthOfYear + 1), dayOfMonth)), mYear, mMonth, mDay);
            datePickerDialog.show();
        }

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {

        createNewMilestoneButton.setEnabled(false);
    }

    private void enableProcessButton() {

        createNewMilestoneButton.setEnabled(true);
    }

}
