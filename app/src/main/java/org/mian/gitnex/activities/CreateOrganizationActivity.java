package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserOrganizations;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateOrganizationActivity extends BaseActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;
    private Button createOrganizationButton;

    private EditText orgName;
    private EditText orgDesc;
    final Context ctx = this;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_new_organization;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        closeActivity = findViewById(R.id.close);
        orgName = findViewById(R.id.newOrganizationName);
        orgDesc = findViewById(R.id.newOrganizationDescription);

        orgName.requestFocus();
        assert imm != null;
        imm.showSoftInput(orgName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        createOrganizationButton = findViewById(R.id.createNewOrganizationButton);

        if(!connToInternet) {

            createOrganizationButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createOrganizationButton.setBackground(shape);

        } else {

            createOrganizationButton.setOnClickListener(createOrgListener);

        }

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private View.OnClickListener createOrgListener = new View.OnClickListener() {
        public void onClick(View v) {
            processNewOrganization();
        }
    };

    private void processNewOrganization() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newOrgName = orgName.getText().toString();
        String newOrgDesc = orgDesc.getText().toString();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(!newOrgDesc.equals("")) {
            if (appUtil.charactersLength(newOrgDesc) > 255) {

                Toasty.info(getApplicationContext(), getString(R.string.orgDescError));
                return;

            }
        }

        if(newOrgName.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.orgNameErrorEmpty));

        }
        else if(!appUtil.checkStrings(newOrgName)) {

            Toasty.info(getApplicationContext(), getString(R.string.orgNameErrorInvalid));

        }
        else {

            disableProcessButton();
            createNewOrganization(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), newOrgName, newOrgDesc);

        }

    }

    private void createNewOrganization(final String instanceUrl, final String token, String orgName, String orgDesc) {

        UserOrganizations createOrganization = new UserOrganizations(orgName, null, orgDesc, null, null);

        Call<UserOrganizations> call = RetrofitClient
            .getInstance(instanceUrl, getApplicationContext())
            .getApiInterface()
            .createNewOrganization(token, createOrganization);

        call.enqueue(new Callback<UserOrganizations>() {

            @Override
            public void onResponse(@NonNull Call<UserOrganizations> call, @NonNull retrofit2.Response<UserOrganizations> response) {

                if(response.code() == 201) {

                    TinyDB tinyDb = new TinyDB(getApplicationContext());
                    tinyDb.putBoolean("orgCreated", true);
                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.orgCreated));
                    finish();

                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 409) {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.orgExistsError));

                }
                else if(response.code() == 422) {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.orgExistsError));

                }
                else {

                    if(response.code() == 404) {
                        enableProcessButton();
                        Toasty.info(getApplicationContext(), getString(R.string.apiNotFound));
                    }
                    else {
                        enableProcessButton();
                        Toasty.info(getApplicationContext(), getString(R.string.orgCreatedError));
                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<UserOrganizations> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void disableProcessButton() {

        createOrganizationButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createOrganizationButton.setBackground(shape);

    }

    private void enableProcessButton() {

        createOrganizationButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createOrganizationButton.setBackground(shape);

    }

}
