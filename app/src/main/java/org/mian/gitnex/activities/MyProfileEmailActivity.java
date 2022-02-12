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
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.AddEmail;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityProfileEmailBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MyProfileEmailActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private EditText userEmail;
    private Button addEmailButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityProfileEmailBinding activityProfileEmailBinding = ActivityProfileEmailBinding.inflate(getLayoutInflater());
	    setContentView(activityProfileEmailBinding.getRoot());

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageView closeActivity = activityProfileEmailBinding.close;
        userEmail = activityProfileEmailBinding.userEmail;
        addEmailButton = activityProfileEmailBinding.addEmailButton;

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

    private final View.OnClickListener addEmailListener = v -> processAddNewEmail();

    private void processAddNewEmail() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newUserEmail = userEmail.getText().toString().trim();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newUserEmail.equals("")) {

            Toasty.error(ctx, getString(R.string.emailErrorEmpty));
            return;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

            Toasty.warning(ctx, getString(R.string.emailErrorInvalid));
            return;
        }

        List<String> newEmailList = new ArrayList<>(Arrays.asList(newUserEmail.split(",")));

        disableProcessButton();
        addNewEmail(Authorization.get(ctx), newEmailList);
    }

    private void addNewEmail(final String token, List<String> newUserEmail) {

        AddEmail addEmailFunc = new AddEmail(newUserEmail);
        final TinyDB tinyDb = TinyDB.getInstance(appCtx);

        Call<JsonElement> call;

        call = RetrofitClient
                .getApiInterface(appCtx)
                .addNewEmail(token, addEmailFunc);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    Toasty.success(ctx, getString(R.string.emailAddedText));
                    tinyDb.putBoolean("emailsRefresh", true);
                    enableProcessButton();
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.cancelButton),
                            getResources().getString(R.string.navLogout));
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
                    Toasty.warning(ctx, ctx.getString(R.string.emailErrorInUse));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.labelGeneralError));
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

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {

        addEmailButton.setEnabled(false);
    }

    private void enableProcessButton() {

        addEmailButton.setEnabled(true);
    }

}
