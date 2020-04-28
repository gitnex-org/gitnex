package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class CreateReleaseActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    public ImageView closeActivity;
    private EditText releaseTagName;
    private Spinner releaseBranch;
    private EditText releaseTitle;
    private EditText releaseContent;
    private CheckBox releaseType;
    private CheckBox releaseDraft;
    private Button createNewRelease;
    final Context ctx = this;
    private Context appCtx;

    List<Branches> branchesList = new ArrayList<>();

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_release;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        appCtx = getApplicationContext();

        boolean connToInternet = AppUtil.haveNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        closeActivity = findViewById(R.id.close);
        releaseTagName = findViewById(R.id.releaseTagName);
        releaseTitle = findViewById(R.id.releaseTitle);
        releaseContent = findViewById(R.id.releaseContent);
        releaseType = findViewById(R.id.releaseType);
        releaseDraft = findViewById(R.id.releaseDraft);

        releaseTagName.requestFocus();
        assert imm != null;
        imm.showSoftInput(releaseTagName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        releaseBranch = findViewById(R.id.releaseBranch);
        releaseBranch.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getBranches(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);
        releaseBranch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Branches branch = (Branches) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createNewRelease = findViewById(R.id.createNewRelease);
        disableProcessButton();

        if(!connToInternet) {

            disableProcessButton();

        } else {

            createNewRelease.setOnClickListener(createReleaseListener);

        }

    }

    private View.OnClickListener createReleaseListener = new View.OnClickListener() {
        public void onClick(View v) {
            processNewRelease();
        }
    };

    private void processNewRelease() {

        boolean connToInternet = AppUtil.haveNetworkConnection(appCtx);

        TinyDB tinyDb = new TinyDB(appCtx);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String newReleaseTagName = releaseTagName.getText().toString();
        String newReleaseTitle = releaseTitle.getText().toString();
        String newReleaseContent = releaseContent.getText().toString();
        String newReleaseBranch = releaseBranch.getSelectedItem().toString();
        boolean newReleaseType = releaseType.isChecked();
        boolean newReleaseDraft = releaseDraft.isChecked();

        if(!connToInternet) {

            Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newReleaseTagName.equals("")) {

            Toasty.info(ctx, getString(R.string.tagNameErrorEmpty));
            return;

        }

        if(newReleaseTitle.equals("")) {

            Toasty.info(ctx, getString(R.string.titleErrorEmpty));
            return;

        }

        disableProcessButton();
        createNewReleaseFunc(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, newReleaseTagName, newReleaseTitle, newReleaseContent, newReleaseBranch, newReleaseType, newReleaseDraft);

    }

    private void createNewReleaseFunc(final String instanceUrl, final String token, String repoOwner, String repoName, String newReleaseTagName, String newReleaseTitle, String newReleaseContent, String newReleaseBranch, boolean newReleaseType, boolean newReleaseDraft) {

        Releases createReleaseJson = new Releases(newReleaseContent, newReleaseDraft, newReleaseTitle, newReleaseType, newReleaseTagName, newReleaseBranch);

        Call<Releases> call;

        call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .createNewRelease(token, repoOwner, repoName, createReleaseJson);

        call.enqueue(new Callback<Releases>() {

            @Override
            public void onResponse(@NonNull Call<Releases> call, @NonNull retrofit2.Response<Releases> response) {

                if (response.code() == 201) {

                    TinyDB tinyDb = new TinyDB(appCtx);
                    tinyDb.putBoolean("updateReleases", true);
                    Toasty.info(ctx, getString(R.string.releaseCreatedText));
                    enableProcessButton();
                    finish();

                }
                else if(response.code() == 401) {

                    enableProcessButton();
                     AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                             ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                             ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                             ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else {

                    enableProcessButton();
                    Toasty.info(ctx, ctx.getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Releases> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void getBranches(String instanceUrl, String instanceToken, final String repoOwner, final String repoName) {

        Call<List<Branches>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getBranches(instanceToken, repoOwner, repoName);

        call.enqueue(new Callback<List<Branches>>() {

            @Override
            public void onResponse(@NonNull Call<List<Branches>> call, @NonNull retrofit2.Response<List<Branches>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Branches> branchesList_ = response.body();

                        assert branchesList_ != null;
                        if(branchesList_.size() > 0) {
                            for (int i = 0; i < branchesList_.size(); i++) {

                                Branches data = new Branches(
                                        branchesList_.get(i).getName()
                                );
                                branchesList.add(data);

                            }
                        }

                        ArrayAdapter<Branches> adapter = new ArrayAdapter<>(CreateReleaseActivity.this,
                                R.layout.spinner_item, branchesList);

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        releaseBranch.setAdapter(adapter);
                        enableProcessButton();

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {
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

    private void disableProcessButton() {

        createNewRelease.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createNewRelease.setBackground(shape);

    }

    private void enableProcessButton() {

        createNewRelease.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createNewRelease.setBackground(shape);

    }

}
