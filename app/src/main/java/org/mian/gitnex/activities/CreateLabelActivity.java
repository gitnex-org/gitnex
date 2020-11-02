package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
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

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_label;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        final TinyDB tinyDb = TinyDB.getInstance(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("delete")) {

            deleteLabel(instanceToken, repoOwner, repoName, Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("labelId"))), loginUid);
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
        colorPicker.setOnClickListener(v -> cp.show());

        cp.setCallback(color -> {

            //Log.i("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
            colorPicker.setBackgroundColor(color);
            tinyDb.putString("labelColor", String.format("#%06X", (0xFFFFFF & color)));
            cp.dismiss();
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
        }
        else {

            createLabelButton.setOnClickListener(createLabelListener);
        }

    }

    private final View.OnClickListener createLabelListener = v -> processCreateLabel();

    private final View.OnClickListener updateLabelListener = v -> processUpdateLabel();

    private void processUpdateLabel() {

        final TinyDB tinyDb = TinyDB.getInstance(appCtx);
        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
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

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(updateLabelName.equals("")) {

            Toasty.error(ctx, getString(R.string.labelEmptyError));
            return;
        }

        if(!appUtil.checkStrings(updateLabelName)) {

            Toasty.error(ctx, getString(R.string.labelNameError));
            return;
        }

        disableProcessButton();
        patchLabel(instanceToken, repoOwner, repoName, updateLabelName, updateLabelColor, Integer.parseInt(
	        Objects.requireNonNull(getIntent().getStringExtra("labelId"))), loginUid);

    }

    private void processCreateLabel() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = TinyDB.getInstance(appCtx);
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
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

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newLabelName.equals("")) {

            Toasty.error(ctx, getString(R.string.labelEmptyError));
            return;
        }

        if(!appUtil.checkStrings(newLabelName)) {

            Toasty.error(ctx, getString(R.string.labelNameError));
            return;
        }

        disableProcessButton();
        createNewLabel(instanceToken, repoOwner, repoName, newLabelName, newLabelColor, loginUid);
    }

    private void createNewLabel(final String instanceToken, String repoOwner, String repoName, String newLabelName, String newLabelColor, String loginUid) {

        CreateLabel createLabelFunc = new CreateLabel(newLabelName, newLabelColor);
        final TinyDB tinyDb = TinyDB.getInstance(appCtx);

        Call<CreateLabel> call;

        call = RetrofitClient
                .getApiInterface(ctx)
                .createLabel(Authorization.get(ctx), repoOwner, repoName, createLabelFunc);

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.code() == 201) {

                    Toasty.success(ctx, getString(R.string.labelCreated));
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
                    Toasty.error(ctx, getString(R.string.labelGeneralError));
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

    private void patchLabel(final String instanceToken, String repoOwner, String repoName, String updateLabelName, String updateLabelColor, int labelId, String loginUid) {

        CreateLabel createLabelFunc = new CreateLabel(updateLabelName, updateLabelColor);
        final TinyDB tinyDb = TinyDB.getInstance(appCtx);

        Call<CreateLabel> call;

        call = RetrofitClient
                .getApiInterface(appCtx)
                .patchLabel(Authorization.get(ctx), repoOwner, repoName, labelId, createLabelFunc);

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 200) {

                        Toasty.success(ctx, getString(R.string.labelUpdated));
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
                    Toasty.error(ctx, getString(R.string.labelGeneralError));
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

        onClickListener = view -> {

            getIntent().removeExtra("labelAction");
            getIntent().removeExtra("labelId");
            getIntent().removeExtra("labelTitle");
            getIntent().removeExtra("labelColor");
            finish();
        };
    }

    private void deleteLabel(final String instanceToken, final String repoOwner, final String repoName, int labelId, String loginUid) {

        Call<Labels> call;

        call = RetrofitClient
                .getApiInterface(appCtx)
                .deleteLabel(Authorization.get(ctx), repoOwner, repoName, labelId);

        call.enqueue(new Callback<Labels>() {

            @Override
            public void onResponse(@NonNull Call<Labels> call, @NonNull retrofit2.Response<Labels> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 204) {

                        Toasty.success(ctx, getString(R.string.labelDeleteText));
                        LabelsViewModel.loadLabelsList(instanceToken, repoOwner, repoName, ctx);
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

                    Toasty.error(ctx, getString(R.string.labelDeleteErrorText));
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
    }

    private void enableProcessButton() {

        createLabelButton.setEnabled(true);
    }

}
