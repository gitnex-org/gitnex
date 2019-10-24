package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.tooltip.Tooltip;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.UserTokens;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button login_button;
    private EditText instance_url, login_uid, login_passwd, otpCode, loginTokenCode;
    private Spinner protocolSpinner;
    private TextView otpInfo;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        login_button = findViewById(R.id.login_button);
        instance_url = findViewById(R.id.instance_url);
        login_uid = findViewById(R.id.login_uid);
        login_passwd = findViewById(R.id.login_passwd);
        otpCode = findViewById(R.id.otpCode);
        otpInfo = findViewById(R.id.otpInfo);
        ImageView info_button = findViewById(R.id.info);
        final TextView viewTextAppVersion = findViewById(R.id.appVersion);
        protocolSpinner = findViewById(R.id.httpsSpinner);
        RadioGroup loginMethod = findViewById(R.id.loginMethod);
        loginTokenCode = findViewById(R.id.loginTokenCode);

        viewTextAppVersion.setText(AppUtil.getAppVersion(getApplicationContext()));

        Resources res = getResources();
        String[] allProtocols = res.getStringArray(R.array.protocolValues);

        final ArrayAdapter<String> adapterProtocols = new ArrayAdapter<String>(Objects.requireNonNull(getApplicationContext()),
                R.layout.spinner_item, allProtocols);

        adapterProtocols.setDropDownViewResource(R.layout.spinner_dropdown_item);
        protocolSpinner.setAdapter(adapterProtocols);

        protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                String value = getResources().getStringArray(R.array.protocolValues)[pos];
                if(value.toLowerCase().equals("http")) {
                    Toasty.info(getApplicationContext(), getResources().getString(R.string.protocolError));
                }

            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        info_button.setOnClickListener(infoListener);

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        loginMethod.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.loginUsernamePassword){
                    login_uid.setVisibility(View.VISIBLE);
                    login_passwd.setVisibility(View.VISIBLE);
                    otpCode.setVisibility(View.VISIBLE);
                    otpInfo.setVisibility(View.VISIBLE);
                    loginTokenCode.setVisibility(View.GONE);
                } else {
                    login_uid.setVisibility(View.GONE);
                    login_passwd.setVisibility(View.GONE);
                    otpCode.setVisibility(View.GONE);
                    otpInfo.setVisibility(View.GONE);
                    loginTokenCode.setVisibility(View.VISIBLE);
                }
            }
        });

        //login_button.setOnClickListener(this);
        if(!tinyDb.getString("instanceUrlRaw").isEmpty()) {
            instance_url.setText(tinyDb.getString("instanceUrlRaw"));
        }
        if(!tinyDb.getString("loginUid").isEmpty()) {
            login_uid.setText(tinyDb.getString("loginUid"));
        }

        if(tinyDb.getBoolean("loggedInMode")) {

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        }

        login_button.setOnClickListener(loginListener);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {

            case R.id.login_button:
                login();
                break;
            default:

        }

    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        public void onClick(View v) {

            disableProcessButton();
            login_button.setText(R.string.processingText);
            login();

        }
    };

    private View.OnClickListener infoListener = new View.OnClickListener() {
        public void onClick(View v) {
        new Tooltip.Builder(v)
            .setText(R.string.urlInfoTooltip)
            .setTextColor(getResources().getColor(R.color.white))
            .setBackgroundColor(getResources().getColor(R.color.tooltipBackground))
            .setCancelable(true)
            .setDismissOnClick(true)
            .setPadding(30)
            .setCornerRadius(R.dimen.tooltipCornor)
            .setGravity(Gravity.BOTTOM)
            .show();
        }
    };

    @SuppressLint("ResourceAsColor")
    private void login() {

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        AppUtil appUtil = new AppUtil();
        boolean connToInternet = AppUtil.haveNetworkConnection(LoginActivity.this);

        String instanceUrl = instance_url.getText().toString().trim();
        String loginUid = login_uid.getText().toString();
        String loginPass = login_passwd.getText().toString();
        String protocol = protocolSpinner.getSelectedItem().toString();
        String loginOTP_ = otpCode.getText().toString().trim();

        if(instanceUrl.contains("@")) {

            String[] urlForHttpAuth = instanceUrl.split("@");

            tinyDb.putString("basicAuthPassword", loginPass);
            tinyDb.putBoolean("basicAuthFlag", true);

            instanceUrl = urlForHttpAuth[1];
            loginUid = urlForHttpAuth[0];

        }

        String instanceHost;
        if(AppUtil.httpCheck(instanceUrl)) {

            URI uri = null;
            try {
                uri = new URI(instanceUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            assert uri != null;
            instanceHost = uri.getHost();

        }
        else {
            instanceHost = instanceUrl;
        }

        String instanceUrlWithProtocol;
        if(protocol.toLowerCase().equals("https")) {
            instanceUrl = "https://" + instanceHost + "/api/v1/";
            instanceUrlWithProtocol = "https://" + instanceHost;
        }
        else {
            instanceUrl = "http://" + instanceHost + "/api/v1/";
            instanceUrlWithProtocol = "https://" + instanceHost;
        }

        tinyDb.putString("instanceUrlRaw", instanceHost);
        tinyDb.putString("loginUid", loginUid);
        tinyDb.putString("instanceUrl", instanceUrl);
        tinyDb.putString("instanceUrlWithProtocol", instanceUrlWithProtocol);

        if(connToInternet) {

            if(instance_url.getText().toString().equals("")) {

                Toasty.info(getApplicationContext(), getString(R.string.emptyFieldURL));
                enableProcessButton();
                login_button.setText(R.string.btnLogin);
                return;

            }
            if(loginUid.equals("")) {

                Toasty.info(getApplicationContext(), getString(R.string.emptyFieldUsername));
                enableProcessButton();
                login_button.setText(R.string.btnLogin);
                return;

            }
            if(login_passwd.getText().toString().equals("")) {

                Toasty.info(getApplicationContext(), getString(R.string.emptyFieldPassword));
                enableProcessButton();
                login_button.setText(R.string.btnLogin);
                return;

            }

            int loginOTP = 0;
            if(loginOTP_.length() == 6) {

                if(appUtil.checkIntegers(loginOTP_)) {

                    loginOTP = Integer.valueOf(loginOTP_);
                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.loginOTPTypeError));
                    enableProcessButton();
                    login_button.setText(R.string.btnLogin);
                    return;

                }

            }

            versionCheck(instanceUrl, loginUid, loginPass, loginOTP);

        }
        else {

            Toasty.info(getApplicationContext(), getString(R.string.checkNetConnection));

        }

    }

    private void versionCheck(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<GiteaVersion> callVersion = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getGiteaVersion();

        callVersion.enqueue(new Callback<GiteaVersion>() {

            @Override
            public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

                if (responseVersion.code() == 200) {

                    GiteaVersion version = responseVersion.body();
                    assert version != null;

                    VersionCheck vt = VersionCheck.check(getString(R.string.versionLow), getString(R.string.versionHigh), version.getVersion());
                    tinyDb.putString("giteaVersion", version.getVersion());

                    switch (vt) {
                        case UNSUPPORTED_NEW:
                            //Toasty.info(getApplicationContext(), getString(R.string.versionUnsupportedNew));
                        case SUPPORTED_LATEST:
                        case SUPPORTED_OLD:
                        case DEVELOPMENT:
                            letTheUserIn(instanceUrl, loginUid, loginPass, loginOTP);
                            return;
                        case UNSUPPORTED_OLD:

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx, R.style.confirmDialog);

                            alertDialogBuilder
                                    .setTitle(getString(R.string.versionAlertDialogHeader))
                                    .setMessage(getResources().getString(R.string.versionUnsupportedOld, version.getVersion()))
                                    .setCancelable(true)
                                    .setIcon(R.drawable.ic_warning)
                                    .setNegativeButton(getString(R.string.versionAlertDialogCopyNegative), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            enableProcessButton();
                                        }
                                    })
                                    .setPositiveButton(getString(R.string.versionAlertDialogCopyPositive), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();
                                            letTheUserIn(instanceUrl, loginUid, loginPass, loginOTP);

                                        }
                                    });

                            AlertDialog alertDialog = alertDialogBuilder.create();

                            alertDialog.show();
                            return;
                        default: // UNKNOWN
                            Toasty.info(getApplicationContext(), getString(R.string.versionUnknow));
                            enableProcessButton();

                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<GiteaVersion> callVersion, Throwable t) {

                Log.e("onFailure-version", t.toString());

            }

        });

    }

    private void letTheUserIn(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP) {

        final String credential = Credentials.basic(loginUid, loginPass);

        Call<List<UserTokens>> call;
        if(loginOTP != 0) {
            call = RetrofitClient
                    .getInstance(instanceUrl)
                    .getApiInterface()
                    .getUserTokensWithOTP(credential, loginOTP, loginUid);
        }
        else {
            call = RetrofitClient
                    .getInstance(instanceUrl)
                    .getApiInterface()
                    .getUserTokens(credential, loginUid);
        }

        call.enqueue(new Callback<List<UserTokens>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserTokens>> call, @NonNull retrofit2.Response<List<UserTokens>> response) {

                List<UserTokens> userTokens = response.body();
                final TinyDB tinyDb = new TinyDB(getApplicationContext());
                //Headers responseHeaders = response.headers();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {

                        boolean setTokenFlag = false;

                        assert userTokens != null;
                        if (userTokens.size() > 0) {
                            for (int i = 0; i < userTokens.size(); i++) {
                                if (userTokens.get(i).getSha1().equals(tinyDb.getString(loginUid + "-token"))) {
                                    setTokenFlag = true;
                                    break;
                                }
                                //Log.i("Tokens: ", userTokens.get(i).getSha1());
                            }
                        }

                        if(tinyDb.getString(loginUid + "-token").isEmpty() || !setTokenFlag) {

                            UserTokens createUserToken = new UserTokens("gitnex-app-token");

                            Call<UserTokens> callCreateToken;
                            if(loginOTP != 0) {
                                callCreateToken = RetrofitClient
                                        .getInstance(instanceUrl)
                                        .getApiInterface()
                                        .createNewTokenWithOTP(credential, loginOTP, loginUid, createUserToken);
                            }
                            else {
                                callCreateToken = RetrofitClient
                                        .getInstance(instanceUrl)
                                        .getApiInterface()
                                        .createNewToken(credential, loginUid, createUserToken);
                            }

                            callCreateToken.enqueue(new Callback<UserTokens>() {

                                @Override
                                public void onResponse(@NonNull Call<UserTokens> callCreateToken, @NonNull retrofit2.Response<UserTokens> responseCreate) {

                                    if(responseCreate.isSuccessful()) {

                                        if(responseCreate.code() == 201) {

                                            UserTokens newToken = responseCreate.body();
                                            assert newToken != null;
                                            //Log.i("Tokens-NEW", "new:" + newToken.getSha1());

                                            if (!newToken.getSha1().equals("")) {

                                                tinyDb.remove("loginPass");
                                                tinyDb.putBoolean("loggedInMode", true);
                                                tinyDb.putString(loginUid + "-token", newToken.getSha1());
                                                //Log.i("Tokens", "new:" + newToken.getSha1() + " old:" + tinyDb.getString(loginUid + "-token"));

                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();

                                            }

                                        }

                                    }
                                    else if(responseCreate.code() == 500) {

                                        String toastError = getResources().getString(R.string.genericApiStatusError) + String.valueOf(responseCreate.code());
                                        Toasty.info(getApplicationContext(), toastError);
                                        enableProcessButton();
                                        login_button.setText(R.string.btnLogin);

                                    }

                                }

                                @Override
                                public void onFailure(@NonNull Call<UserTokens> createUserToken, Throwable t) {

                                }

                            });
                        }
                        else {

                            //Log.i("Current Token", tinyDb.getString(loginUid + "-token"));
                            tinyDb.putBoolean("loggedInMode", true);
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        }

                    }

                }
                else if(response.code() == 500) {

                    String toastError = getResources().getString(R.string.genericApiStatusError) + String.valueOf(response.code());
                    Toasty.info(getApplicationContext(), toastError);
                    enableProcessButton();
                    login_button.setText(R.string.btnLogin);

                }
                else {

                    String toastError = getResources().getString(R.string.genericApiStatusError) + String.valueOf(response.code());
                    //Log.i("error message else4", String.valueOf(response.code()));

                    Toasty.info(getApplicationContext(), toastError);
                    enableProcessButton();
                    login_button.setText(R.string.btnLogin);

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserTokens>> call, @NonNull Throwable t) {
                Log.e("onFailure-login", t.toString());
                Toasty.info(getApplicationContext(), getResources().getString(R.string.malformedJson));
                enableProcessButton();
                login_button.setText(R.string.btnLogin);
            }
        });

    }

    private void disableProcessButton() {

        login_button.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        login_button.setBackground(shape);

    }

    private void enableProcessButton() {

        login_button.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        login_button.setBackground(shape);

    }

}
