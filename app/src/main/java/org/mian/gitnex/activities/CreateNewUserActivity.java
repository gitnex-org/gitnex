package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public class CreateNewUserActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;
    private EditText fullName;
    private EditText userUserName;
    private EditText userEmail;
    private EditText userPassword;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_user);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        ImageView closeActivity = findViewById(R.id.close);
        Button createUserButton = findViewById(R.id.createUserButton);
        fullName = findViewById(R.id.fullName);
        userUserName = findViewById(R.id.userUserName);
        userEmail = findViewById(R.id.userEmail);
        userPassword = findViewById(R.id.userPassword);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(!connToInternet) {

            createUserButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            createUserButton.setBackground(shape);

        } else {

            createUserButton.setOnClickListener(createNewUserListener);

        }

    }

    private void processCreateNewUser() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newFullName = fullName.getText().toString().trim();
        String newUserName = userUserName.getText().toString().trim();
        String newUserEmail = userEmail.getText().toString().trim();
        String newUserPassword = userPassword.getText().toString();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newFullName.equals("") || newUserName.equals("") | newUserEmail.equals("") || newUserPassword.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.emptyFields));
            return;

        }

        if(!appUtil.checkStrings(newFullName)) {

            Toasty.info(getApplicationContext(), getString(R.string.userInvalidFullName));
            return;

        }

        if(!appUtil.checkStringsWithAlphaNumeric(newUserName)) {

            Toasty.info(getApplicationContext(), getString(R.string.userInvalidUserName));
            return;

        }

        if(!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

            Toasty.info(getApplicationContext(), getString(R.string.userInvalidEmail));
            return;

        }

        createNewUser(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), newFullName, newUserName, newUserEmail, newUserPassword);

    }

    private void createNewUser(final String instanceUrl, final String instanceToken, String newFullName, String newUserName, String newUserEmail, String newUserPassword) {

        UserInfo createUser = new UserInfo(newUserEmail, newFullName, newUserName, newUserPassword, newUserName, 0, true);

        Call<UserInfo> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .createNewUser(instanceToken, createUser);

        call.enqueue(new Callback<UserInfo>() {

            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

                if(response.code() == 201) {

                    Toasty.info(getApplicationContext(), getString(R.string.userCreatedText));
                    finish();

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private View.OnClickListener createNewUserListener = new View.OnClickListener() {
        public void onClick(View v) {
            processCreateNewUser();
        }
    };

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }
}
