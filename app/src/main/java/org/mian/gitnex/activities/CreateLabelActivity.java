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
import org.mian.gitnex.databinding.ActivityCreateLabelBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.CreateLabel;
import org.mian.gitnex.models.Labels;
import org.mian.gitnex.viewmodels.LabelsViewModel;
import org.mian.gitnex.viewmodels.OrganizationLabelsViewModel;
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
    private TinyDB tinyDB;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_label;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateLabelBinding activityCreateLabelBinding = ActivityCreateLabelBinding.inflate(getLayoutInflater());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        tinyDB = TinyDB.getInstance(appCtx);
        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("delete")) {

            deleteLabel(repoOwner, repoName, Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("labelId"))));
            finish();
            return;
        }

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        ImageView closeActivity = activityCreateLabelBinding.close;
        colorPicker = activityCreateLabelBinding.colorPicker;
        labelName = activityCreateLabelBinding.labelName;
        createLabelButton = activityCreateLabelBinding.createLabelButton;

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
	        tinyDB.putString("labelColor", String.format("#%06X", (0xFFFFFF & color)));
            cp.dismiss();
        });

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("edit")) {

            labelName.setText(getIntent().getStringExtra("labelTitle"));
            int labelColor_ = Color.parseColor("#" + getIntent().getStringExtra("labelColor"));
            colorPicker.setBackgroundColor(labelColor_);
	        tinyDB.putString("labelColorDefault", "#" + getIntent().getStringExtra("labelColor"));

            TextView toolbar_title = activityCreateLabelBinding.toolbarTitle;
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

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String updateLabelName = labelName.getText().toString();

        String updateLabelColor;
        if(tinyDB.getString("labelColor").isEmpty()) {

            updateLabelColor = tinyDB.getString("labelColorDefault");
        }
        else {

            updateLabelColor = tinyDB.getString("labelColor");
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
        patchLabel(repoOwner, repoName, updateLabelName, updateLabelColor, Integer.parseInt(
	        Objects.requireNonNull(getIntent().getStringExtra("labelId"))));

    }

    private void processCreateLabel() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();

        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String newLabelName = labelName.getText().toString();
        String newLabelColor;

        if(tinyDB.getString("labelColor").isEmpty()) {

            newLabelColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, R.color.releasePre)));
        }
        else {

            newLabelColor = tinyDB.getString("labelColor");
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
        createNewLabel(repoOwner, repoName, newLabelName, newLabelColor);
    }

    private void createNewLabel(String repoOwner, String repoName, String newLabelName, String newLabelColor) {

        CreateLabel createLabelFunc = new CreateLabel(newLabelName, newLabelColor);

        Call<CreateLabel> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

	    	call = RetrofitClient.getApiInterface(ctx).createOrganizationLabel(Authorization.get(ctx), getIntent().getStringExtra("orgName"), createLabelFunc);
	    }
	    else {

		    call = RetrofitClient.getApiInterface(ctx).createLabel(Authorization.get(ctx), repoOwner, repoName, createLabelFunc);
	    }

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.code() == 201) {

                    Toasty.success(ctx, getString(R.string.labelCreated));
	                tinyDB.putString("labelColor", "");
	                tinyDB.putBoolean("labelsRefresh", true);
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
	                tinyDB.putString("labelColor", "");
                    Toasty.error(ctx, getString(R.string.labelGeneralError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateLabel> call, @NonNull Throwable t) {

	            tinyDB.putString("labelColor", "");
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void patchLabel(String repoOwner, String repoName, String updateLabelName, String updateLabelColor, int labelId) {

        CreateLabel createLabelFunc = new CreateLabel(updateLabelName, updateLabelColor);

        Call<CreateLabel> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

		    call = RetrofitClient.getApiInterface(ctx).patchOrganizationLabel(Authorization.get(ctx), getIntent().getStringExtra("orgName"), labelId, createLabelFunc);
	    }
	    else {

		    call = RetrofitClient.getApiInterface(appCtx).patchLabel(Authorization.get(ctx), repoOwner, repoName, labelId, createLabelFunc);
	    }

        call.enqueue(new Callback<CreateLabel>() {

            @Override
            public void onResponse(@NonNull Call<CreateLabel> call, @NonNull retrofit2.Response<CreateLabel> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 200) {

                        Toasty.success(ctx, getString(R.string.labelUpdated));
	                    tinyDB.putString("labelColor", "");
	                    tinyDB.putBoolean("labelsRefresh", true);
	                    tinyDB.putString("labelColorDefault", "");
                        getIntent().removeExtra("labelAction");
                        getIntent().removeExtra("labelId");
                        getIntent().removeExtra("labelTitle");
                        getIntent().removeExtra("labelColor");
	                    getIntent().removeExtra("type");
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
	                tinyDB.putString("labelColor", "");
	                tinyDB.putString("labelColorDefault", "");
                    Toasty.error(ctx, getString(R.string.labelGeneralError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreateLabel> call, @NonNull Throwable t) {

	            tinyDB.putString("labelColor", "");
	            tinyDB.putString("labelColorDefault", "");
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
	        getIntent().removeExtra("type");
            finish();
        };
    }

    private void deleteLabel(final String repoOwner, final String repoName, int labelId) {

        Call<Labels> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

		    call = RetrofitClient.getApiInterface(appCtx).deleteOrganizationLabel(Authorization.get(ctx), getIntent().getStringExtra("orgName"), labelId);
	    }
	    else {

		    call = RetrofitClient.getApiInterface(appCtx).deleteLabel(Authorization.get(ctx), repoOwner, repoName, labelId);
	    }

        call.enqueue(new Callback<Labels>() {

            @Override
            public void onResponse(@NonNull Call<Labels> call, @NonNull retrofit2.Response<Labels> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 204) {

                        Toasty.success(ctx, getString(R.string.labelDeleteText));
	                    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

	                    	OrganizationLabelsViewModel.loadOrgLabelsList(Authorization.get(ctx), getIntent().getStringExtra("orgName"), ctx, null, null);
	                    }
	                    else {

		                    LabelsViewModel.loadLabelsList(Authorization.get(ctx), repoOwner, repoName, ctx);
	                    }
                        getIntent().removeExtra("labelAction");
                        getIntent().removeExtra("labelId");
	                    getIntent().removeExtra("type");
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
