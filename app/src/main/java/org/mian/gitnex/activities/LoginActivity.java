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
import com.tooltip.Tooltip;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserTokens;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import okhttp3.Credentials;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private Button loginButton;
    private EditText instanceUrlET, loginUidET, loginPassword, otpCode, loginTokenCode;
    private Spinner protocolSpinner;
    private TextView otpInfo;
    private RadioGroup loginMethod;
    final Context ctx = this;
    private String device_id = "token";

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_login;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        loginButton = findViewById(R.id.login_button);
        instanceUrlET = findViewById(R.id.instance_url);
        loginUidET = findViewById(R.id.login_uid);
        loginPassword = findViewById(R.id.login_passwd);
        otpCode = findViewById(R.id.otpCode);
        otpInfo = findViewById(R.id.otpInfo);
        ImageView info_button = findViewById(R.id.info);
        final TextView viewTextAppVersion = findViewById(R.id.appVersion);
        protocolSpinner = findViewById(R.id.httpsSpinner);
        loginMethod = findViewById(R.id.loginMethod);
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
                    loginUidET.setVisibility(View.VISIBLE);
                    loginPassword.setVisibility(View.VISIBLE);
                    otpCode.setVisibility(View.VISIBLE);
                    otpInfo.setVisibility(View.VISIBLE);
                    loginTokenCode.setVisibility(View.GONE);
                } else {
                    loginUidET.setVisibility(View.GONE);
                    loginPassword.setVisibility(View.GONE);
                    otpCode.setVisibility(View.GONE);
                    otpInfo.setVisibility(View.GONE);
                    loginTokenCode.setVisibility(View.VISIBLE);
                }
            }
        });

        //login_button.setOnClickListener(this);
        if(!tinyDb.getString("instanceUrlRaw").isEmpty()) {
            instanceUrlET.setText(tinyDb.getString("instanceUrlRaw"));
        }
        if(!tinyDb.getString("loginUid").isEmpty()) {
            loginUidET.setText(tinyDb.getString("loginUid"));
        }

        if(tinyDb.getBoolean("loggedInMode")) {

            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();

        }

        loginButton.setOnClickListener(loginListener);

        if(!tinyDb.getString("uniqueAppId").isEmpty()) {
            device_id = tinyDb.getString("uniqueAppId");
        }
        else {
            device_id = UUID.randomUUID().toString();
            tinyDb.putString("uniqueAppId", device_id);
        }

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.login_button) {
            login();
        }

    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        public void onClick(View v) {

            disableProcessButton();
            loginButton.setText(R.string.processingText);
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

        String instanceUrl = instanceUrlET.getText().toString().trim();
        String loginUid = loginUidET.getText().toString();
        String loginPass = loginPassword.getText().toString();
        String protocol = protocolSpinner.getSelectedItem().toString();
        String loginOTP_ = otpCode.getText().toString().trim();
        int loginMethodType = loginMethod.getCheckedRadioButtonId();
        String loginToken_ = loginTokenCode.getText().toString().trim();

        if(loginMethodType == R.id.loginUsernamePassword) {

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

                if(instanceUrlET.getText().toString().equals("")) {

                    Toasty.info(getApplicationContext(), getString(R.string.emptyFieldURL));
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);
                    return;

                }
                if(loginUid.equals("")) {

                    Toasty.info(getApplicationContext(), getString(R.string.emptyFieldUsername));
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);
                    return;

                }
                if(loginPassword.getText().toString().equals("")) {

                    Toasty.info(getApplicationContext(), getString(R.string.emptyFieldPassword));
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);
                    return;

                }

                int loginOTP = 0;
                if(loginOTP_.length() == 6) {

                    if(appUtil.checkIntegers(loginOTP_)) {

                        loginOTP = Integer.parseInt(loginOTP_);
                    }
                    else {

                        Toasty.info(getApplicationContext(), getString(R.string.loginOTPTypeError));
                        enableProcessButton();
                        loginButton.setText(R.string.btnLogin);
                        return;

                    }

                }

                versionCheck(instanceUrl, loginUid, loginPass, loginOTP, loginToken_, 1);

            }
            else {

                Toasty.info(getApplicationContext(), getString(R.string.checkNetConnection));

            }

        }
        else {

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
            //tinyDb.putString("loginUid", loginUid);
            tinyDb.putString("instanceUrl", instanceUrl);
            tinyDb.putString("instanceUrlWithProtocol", instanceUrlWithProtocol);

            if(connToInternet) {

                if (instanceUrlET.getText().toString().equals("")) {

                    Toasty.info(getApplicationContext(), getString(R.string.emptyFieldURL));
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);
                    return;

                }
                if (loginToken_.equals("")) {

                    Toasty.info(getApplicationContext(), getString(R.string.loginTokenError));
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);
                    return;

                }

                versionCheck(instanceUrl, loginUid, loginPass, 123, loginToken_, 2);
            }
            else {

                Toasty.info(getApplicationContext(), getString(R.string.checkNetConnection));

            }

        }

    }

    private void versionCheck(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP, final String loginToken_, final int loginType) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<GiteaVersion> callVersion = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getGiteaVersion();

        callVersion.enqueue(new Callback<GiteaVersion>() {

            @Override
            public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

                if (responseVersion.code() == 200) {

                    GiteaVersion version = responseVersion.body();
                    assert version != null;

                    VersionCheck vt = VersionCheck.check(getString(R.string.versionLow), getString(R.string.versionHigh), version.getVersion());

                    switch (vt) {
                        case UNSUPPORTED_NEW:
                            //Toasty.info(getApplicationContext(), getString(R.string.versionUnsupportedNew));
                        case SUPPORTED_LATEST:
                        case SUPPORTED_OLD:
                        case DEVELOPMENT:
                            login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken_);
                            return;
                        case UNSUPPORTED_OLD:

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx);

                            alertDialogBuilder
                                    .setTitle(getString(R.string.versionAlertDialogHeader))
                                    .setMessage(getResources().getString(R.string.versionUnsupportedOld, version.getVersion()))
                                    .setCancelable(true)
                                    .setIcon(R.drawable.ic_warning)
                                    .setNegativeButton(getString(R.string.cancelButton), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            enableProcessButton();
                                        }
                                    })
                                    .setPositiveButton(getString(R.string.textContinue), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            dialog.dismiss();
                                            login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken_);

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
                else if (responseVersion.code() == 403) {
                    login(loginType, instanceUrl, loginUid, loginPass, loginOTP, loginToken_);
                }
            }

            private void login(int loginType, String instanceUrl, String loginUid, String loginPass, int loginOTP, String loginToken_) {
                if (loginType == 1) {
                    letTheUserIn(instanceUrl, loginUid, loginPass, loginOTP);
                }
                else if (loginType == 2) { // token
                    letTheUserInViaToken(instanceUrl, loginToken_);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GiteaVersion> callVersion, Throwable t) {

                Log.e("onFailure-version", t.toString());

            }

        });

    }

    private void letTheUserInViaToken(String instanceUrl, final String loginToken_) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        Call<UserInfo> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getUserInfo("token " + loginToken_);

        call.enqueue(new Callback<UserInfo>() {

            @Override
            public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

                UserInfo userDetails = response.body();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {
                        
                        tinyDb.putBoolean("loggedInMode", true);
                        assert userDetails != null;
                        tinyDb.putString(userDetails.getLogin() + "-token", loginToken_);
                        tinyDb.putString("loginUid", userDetails.getLogin());

                        enableProcessButton();
                        loginButton.setText(R.string.btnLogin);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();

                    }

                }
                else if(response.code() == 401) {

                    String toastError = getResources().getString(R.string.unauthorizedApiError);
                    Toasty.info(getApplicationContext(), toastError);

                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);

                }
                else {

                    String toastError = getResources().getString(R.string.genericApiStatusError) + response.code();
                    Toasty.info(getApplicationContext(), toastError);

                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);

                }

            }

            @Override
            public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                Toasty.info(getApplicationContext(), getResources().getString(R.string.genericError));
                enableProcessButton();
                loginButton.setText(R.string.btnLogin);
            }
        });

    }

    private void letTheUserIn(final String instanceUrl, final String loginUid, final String loginPass, final int loginOTP) {

        final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

        Call<List<UserTokens>> call;
        if(loginOTP != 0) {
            call = RetrofitClient
                    .getInstance(instanceUrl, getApplicationContext())
                    .getApiInterface()
                    .getUserTokensWithOTP(credential, loginOTP, loginUid);
        }
        else {
            call = RetrofitClient
                    .getInstance(instanceUrl, getApplicationContext())
                    .getApiInterface()
                    .getUserTokens(credential, loginUid);
        }

        call.enqueue(new Callback<List<UserTokens>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserTokens>> call, @NonNull retrofit2.Response<List<UserTokens>> response) {

                List<UserTokens> userTokens = response.body();
                final TinyDB tinyDb = new TinyDB(getApplicationContext());
                final AppUtil appUtil = new AppUtil();
                //Headers responseHeaders = response.headers();

                if (response.isSuccessful()) {

                    if (response.code() == 200) {

                        boolean setTokenFlag = false;

                        assert userTokens != null;
                        if (userTokens.size() > 0) {

                            if(userTokens.get(0).getToken_last_eight() != null) {

                                for (int i = 0; i < userTokens.size(); i++) {
                                    if (userTokens.get(i).getToken_last_eight().equals(tinyDb.getString(loginUid + "-token-last-eight"))) {
                                        setTokenFlag = true;
                                        break;
                                    }
                                    //Log.i("Tokens: ", userTokens.get(i).getToken_last_eight());
                                }

                            }
                            else {

                                for (int i = 0; i < userTokens.size(); i++) {
                                    if (userTokens.get(i).getSha1().equals(tinyDb.getString(loginUid + "-token"))) {
                                        setTokenFlag = true;
                                        break;
                                    }
                                    //Log.i("Tokens: ", userTokens.get(i).getSha1());
                                }

                            }

                        }

                        if(tinyDb.getString(loginUid + "-token").isEmpty() || !setTokenFlag) {

                            UserTokens createUserToken = new UserTokens("gitnex-app-" + device_id);

                            Call<UserTokens> callCreateToken;
                            if(loginOTP != 0) {
                                callCreateToken = RetrofitClient
                                        .getInstance(instanceUrl, getApplicationContext())
                                        .getApiInterface()
                                        .createNewTokenWithOTP(credential, loginOTP, loginUid, createUserToken);
                            }
                            else {
                                callCreateToken = RetrofitClient
                                        .getInstance(instanceUrl, getApplicationContext())
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
                                                tinyDb.putString(loginUid + "-token-last-eight", appUtil.getLastCharactersOfWord(newToken.getSha1(), 8));
                                                //Log.i("Tokens", "new:" + newToken.getSha1() + " old:" + tinyDb.getString(loginUid + "-token"));

                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                finish();

                                            }

                                        }

                                    }
                                    else if(responseCreate.code() == 500) {

                                        String toastError = getResources().getString(R.string.genericApiStatusError) + responseCreate.code();
                                        Toasty.info(getApplicationContext(), toastError);
                                        enableProcessButton();
                                        loginButton.setText(R.string.btnLogin);

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

                    String toastError = getResources().getString(R.string.genericApiStatusError) + response.code();
                    Toasty.info(getApplicationContext(), toastError);
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);

                }
                else {

                    String toastError = getResources().getString(R.string.genericApiStatusError) + response.code();
                    //Log.i("error message else4", String.valueOf(response.code()));

                    Toasty.info(getApplicationContext(), toastError);
                    enableProcessButton();
                    loginButton.setText(R.string.btnLogin);

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserTokens>> call, @NonNull Throwable t) {
                Log.e("onFailure-login", t.toString());
                Toasty.info(getApplicationContext(), getResources().getString(R.string.malformedJson));
                enableProcessButton();
                loginButton.setText(R.string.btnLogin);
            }
        });

    }

    private void disableProcessButton() {

        loginButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        loginButton.setBackground(shape);

    }

    private void enableProcessButton() {

        loginButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        loginButton.setBackground(shape);

    }

}
