package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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
import org.mian.gitnex.models.UserInfo;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateNewUserActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private EditText fullName;
    private EditText userUserName;
    private EditText userEmail;
    private EditText userPassword;
    private Button createUserButton;
    final Context ctx = this;
    private Context appCtx;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_new_user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageView closeActivity = findViewById(R.id.close);
        createUserButton = findViewById(R.id.createUserButton);
        fullName = findViewById(R.id.fullName);
        userUserName = findViewById(R.id.userUserName);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);

        fullName.requestFocus();
        assert imm != null;
        imm.showSoftInput(fullName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(!connToInternet) {

            disableProcessButton();
        }
        else {

            createUserButton.setOnClickListener(createNewUserListener);
        }
    }

    private void processCreateNewUser() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newFullName = fullName.getText().toString().trim();
        String newUserName = userUserName.getText().toString().trim();
        String newUserEmail = userEmail.getText().toString().trim();
        String newUserPassword = userPassword.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newFullName.equals("") || newUserName.equals("") | newUserEmail.equals("") || newUserPassword.equals("")) {

            Toasty.error(ctx, getString(R.string.emptyFields));
            return;
        }

        if(!appUtil.checkStrings(newFullName)) {

            Toasty.error(ctx, getString(R.string.userInvalidFullName));
            return;
        }

        if(!appUtil.checkStringsWithAlphaNumeric(newUserName)) {

            Toasty.error(ctx, getString(R.string.userInvalidUserName));
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

            Toasty.error(ctx, getString(R.string.userInvalidEmail));
            return;
        }

        disableProcessButton();
        createNewUser(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), newFullName, newUserName, newUserEmail, newUserPassword);
    }

    private void createNewUser(final String instanceUrl, final String instanceToken, String newFullName, String newUserName, String newUserEmail, String newUserPassword) {

        UserInfo createUser = new UserInfo(newUserEmail, newFullName, newUserName, newUserPassword, newUserName, 0, true);

        Call<UserInfo> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createNewUser(instanceToken, createUser);

        call.enqueue(new Callback<UserInfo>() {

            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

                if(response.code() == 201) {

                    Toasty.success(ctx, getString(R.string.userCreatedText));
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
                    Toasty.error(ctx, ctx.getString(R.string.authorizeError));
                }
                else if(response.code() == 404) {

                    enableProcessButton();
                    Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
                }
                else if(response.code() == 422) {

                    enableProcessButton();
                    Toasty.warning(ctx, ctx.getString(R.string.userExistsError));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private final View.OnClickListener createNewUserListener = v -> processCreateNewUser();

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {

        createUserButton.setEnabled(false);
    }

    private void enableProcessButton() {

        createUserButton.setEnabled(true);
    }

}
