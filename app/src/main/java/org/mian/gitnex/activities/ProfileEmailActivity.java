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
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
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

public class ProfileEmailActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;
    private EditText userEmail;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_email);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        ImageView closeActivity = findViewById(R.id.close);
        userEmail = findViewById(R.id.userEmail);
        Button addEmailButton = findViewById(R.id.addEmailButton);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(!connToInternet) {

            addEmailButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            addEmailButton.setBackground(shape);

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

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newUserEmail = userEmail.getText().toString().trim();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newUserEmail.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.emailErrorEmpty));
            return;

        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

            Toasty.info(getApplicationContext(), getString(R.string.emailErrorInvalid));
            return;

        }

        List<String> newEmailList = new ArrayList<>(Arrays.asList(newUserEmail.split(",")));

        addNewEmail(instanceUrl, instanceToken, newEmailList);

    }

    private void addNewEmail(final String instanceUrl, final String token, List<String> newUserEmail) {

        AddEmail addEmailFunc = new AddEmail(newUserEmail);
        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<JsonElement> call;

        call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .addNewEmail(token, addEmailFunc);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    Toasty.info(getApplicationContext(), getString(R.string.emailAddedText));
                    tinyDb.putBoolean("emailsRefresh", true);
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
                else if(response.code() == 422) {

                    Toasty.info(ctx, ctx.getString(R.string.emailErrorInUse));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
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
}
