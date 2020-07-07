package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.CreateLabel;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.viewmodels.LabelsViewModel;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateLabelActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private TextView colorPicker;
    private EditText labelName;
    private Button createLabelButton;
    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_label;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        final TinyDB tinyDb = new TinyDB(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("delete")) {

            deleteLabel(instanceUrl, instanceToken, repoOwner, repoName, Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("labelId"))), loginUid);
            finish();
            return;

        }

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        ImageView closeActivity = findViewById(R.id.close);
        colorPicker = findViewById(R.id.colorPicker);
        labelName = findViewById(R.id.labelName);
        createLabelButton = findViewById(R.id.createLabelButton);

        labelName.requestFocus();
        assert imm != null;
        imm.showSoftInput(labelName, InputMethodManager.SHOW_IMPLICIT);

        final ColorPicker cp = new ColorPicker(CreateLabelActivity.this, 235, 113, 33);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cp.show();
            }
        });

        cp.setCallback(new ColorPickerCallback() {
            @Override
            public void onColorChosen(@ColorInt int color) {

                //Log.i("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
                colorPicker.setBackgroundColor(color);
                tinyDb.putString("labelColor", String.format("#%06X", (0xFFFFFF & color)));
                cp.dismiss();

            }
        });

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("edit")) {

            labelName.setText(getIntent().getStringExtra("labelTitle"));
            int labelColor_ = Color.parseColor("#" + getIntent().getStringExtra("labelColor"));
            colorPicker.setBackgroundColor(labelColor_);
            tinyDb.putString("labelColorDefault", "#" + getIntent().getStringExtra("labelColor"));

            TextView toolbar_title = findViewById(R.id.toolbar_title);
            toolbar_title.setText(getResources().getString(R.string.pageTitleLabelUpdate));
            createLabelButton.setText(getResources().getString(R.string.newUpdateButtonCopy));

            createLabelButton.setOnClickListener(updateLabelListener);

            return;

        }

        if(!connToInternet) {

            createLabelButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createLabelButton.setBackground(shape);

        } else {

            createLabelButton.setOnClickListener(createLabelListener);

        }

    }

    private View.OnClickListener createLabelListener = new View.OnClickListener() {
        public void onClick(View v) {
            processCreateLabel();
        }
    };

    private View.OnClickListener updateLabelListener = new View.OnClickListener() {
        public void onClick(View v) {
            processUpdateLabel();
        }
    };

    private void processUpdateLabel() {

        final TinyDB tinyDb = new TinyDB(appCtx);
        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String updateLabelName = labelName.getText().toString();

        String updateLabelColor;
        if(tinyDb.getString("labelColor").isEmpty()) {
            updateLabelColor = tinyDb.getString("labelColorDefault");
        }
        else {
            updateLabelColor = tinyDb.getString("labelColor");
        }

        if(!connToInternet) {

            Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(updateLabelName.equals("")) {

            Toasty.info(ctx, getString(R.string.labelEmptyError));
            return;

        }

        if(!appUtil.checkStrings(updateLabelName)) {

            Toasty.info(ctx, getString(R.string.labelNameError));
            return;

        }

        disableProcessButton();
        patchLabel(instanceUrl, instanceToken, repoOwner, repoName, updateLabelName, updateLabelColor, Integer.valueOf(getIntent().getStringExtra("labelId")), loginUid);

    }

    private void processCreateLabel() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newLabelName = labelName.getText().toString();
        String newLabelColor;
        if(tinyDb.getString("labelColor").isEmpty()) {
            newLabelColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, R.color.releasePre)));
        }
        else {
            newLabelColor = tinyDb.getString("labelColor");
        }

        if(!connToInternet) {

            Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newLabelName.equals("")) {

            Toasty.info(ctx, getString(R.string.labelEmptyError));
            return;

        }

        if(!appUtil.checkStrings(newLabelName)) {

            Toasty.info(ctx, getString(R.string.labelNameError));
            return;

        }

        disableProcessButton();
        createNewLabel(instanceUrl, instanceToken, repoOwner, repoName, newLabelName, newLabelColor, loginUid);

    }

    private void createNewLabel(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String newLabelName, String newLabelColor, String loginUid) {

        CreateLabel createLabelFunc = new CreateLabel(newLabelName, newLabelColor);
        final TinyDB tinyDb = new TinyDB(appCtx);

        Call<CreateLabel> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createLabel(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, createLabelFunc);

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.code() == 201) {

                    Toasty.info(ctx, getString(R.string.labelCreated));
                    tinyDb.putString("labelColor", "");
                    tinyDb.putBoolean("labelsRefresh", true);
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
                    tinyDb.putString("labelColor", "");
                    Toasty.info(ctx, getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<CreateLabel> call, @NonNull Throwable t) {
                tinyDb.putString("labelColor", "");
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void patchLabel(final String instanceUrl, final String instanceToken, String repoOwner, String repoName, String updateLabelName, String updateLabelColor, int labelId, String loginUid) {

        CreateLabel createLabelFunc = new CreateLabel(updateLabelName, updateLabelColor);
        final TinyDB tinyDb = new TinyDB(appCtx);

        Call<CreateLabel> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .patchLabel(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, labelId, createLabelFunc);

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        Toasty.info(ctx, getString(R.string.labelUpdated));
                        tinyDb.putString("labelColor", "");
                        tinyDb.putBoolean("labelsRefresh", true);
                        tinyDb.putString("labelColorDefault", "");
                        getIntent().removeExtra("labelAction");
                        getIntent().removeExtra("labelId");
                        getIntent().removeExtra("labelTitle");
                        getIntent().removeExtra("labelColor");
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
                    tinyDb.putString("labelColor", "");
                    tinyDb.putString("labelColorDefault", "");
                    Toasty.info(ctx, getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<CreateLabel> call, @NonNull Throwable t) {
                tinyDb.putString("labelColor", "");
                tinyDb.putString("labelColorDefault", "");
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().removeExtra("labelAction");
                getIntent().removeExtra("labelId");
                getIntent().removeExtra("labelTitle");
                getIntent().removeExtra("labelColor");
                finish();
            }
        };
    }

    private void deleteLabel(final String instanceUrl, final String instanceToken, final String repoOwner, final String repoName, int labelId, String loginUid) {

        Call<Labels> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .deleteLabel(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, labelId);

        call.enqueue(new Callback<Labels>() {

            @Override
            public void onResponse(@NonNull Call<Labels> call, @NonNull retrofit2.Response<Labels> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        Toasty.info(ctx, getString(R.string.labelDeleteText));
                        LabelsViewModel.loadLabelsList(instanceUrl, instanceToken, repoOwner, repoName, ctx);
                        getIntent().removeExtra("labelAction");
                        getIntent().removeExtra("labelId");

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    Toasty.info(ctx, getString(R.string.labelDeleteErrorText));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Labels> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void disableProcessButton() {

        createLabelButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createLabelButton.setBackground(shape);

    }

    private void enableProcessButton() {

        createLabelButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createLabelButton.setBackground(shape);

    }

}
