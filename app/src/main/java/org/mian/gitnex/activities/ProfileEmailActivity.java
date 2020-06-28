package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.AddEmail;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileEmailActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private EditText userEmail;
    final Context ctx = this;
    private Context appCtx;
    private Button addEmailButton;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_profile_email;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageView closeActivity = findViewById(R.id.close);
        userEmail = findViewById(R.id.userEmail);
        addEmailButton = findViewById(R.id.addEmailButton);

        userEmail.requestFocus();
        assert imm != null;
        imm.showSoftInput(userEmail, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(!connToInternet) {

            disableProcessButton();

        } else {

            addEmailButton.setOnClickListener(addEmailListener);

        }

    }

    private View.OnClickListener addEmailListener = new View.OnClickListener() {
        public void onClick(View v) {
            processAddNewEmail();
        }
    };

    private void processAddNewEmail() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newUserEmail = userEmail.getText().toString().trim();

        if(!connToInternet) {

            Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newUserEmail.equals("")) {

            Toasty.info(ctx, getString(R.string.emailErrorEmpty));
            return;

        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

            Toasty.info(ctx, getString(R.string.emailErrorInvalid));
            return;

        }

        List<String> newEmailList = new ArrayList<>(Arrays.asList(newUserEmail.split(",")));

        disableProcessButton();
        addNewEmail(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), newEmailList);

    }

    private void addNewEmail(final String instanceUrl, final String token, List<String> newUserEmail) {

        AddEmail addEmailFunc = new AddEmail(newUserEmail);
        final TinyDB tinyDb = new TinyDB(appCtx);

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .addNewEmail(token, addEmailFunc);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    Toasty.info(ctx, getString(R.string.emailAddedText));
                    tinyDb.putBoolean("emailsRefresh", true);
                    enableProcessButton();
                    finish();

                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else if(response.code() == 422) {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.emailErrorInUse));

                }
                else {

                    enableProcessButton();
                    Toasty.info(ctx, getString(R.string.labelGeneralError));

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

    private void disableProcessButton() {

        addEmailButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        addEmailButton.setBackground(shape);

    }

    private void enableProcessButton() {

        addEmailButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        addEmailButton.setBackground(shape);

    }

}
