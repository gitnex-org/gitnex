package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.MergePullRequest;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MergePullRequestActivity extends BaseActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;

    final Context ctx = this;

    private SocialAutoCompleteTextView mergePR;
    private ArrayAdapter<Mention> defaultMentionAdapter;
    private Button mergeButton;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_merge_pull_request;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mergePR = findViewById(R.id.mergePR);
        mergePR.setShowSoftInputOnFocus(true);

        mergePR.requestFocus();
        assert imm != null;
        imm.showSoftInput(mergePR, InputMethodManager.SHOW_IMPLICIT);

        defaultMentionAdapter = new MentionArrayAdapter<>(this);
        loadCollaboratorsList();

        mergePR.setMentionAdapter(defaultMentionAdapter);

        closeActivity = findViewById(R.id.close);
        TextView toolbar_title = findViewById(R.id.toolbar_title);

        if(!tinyDb.getString("issueTitle").isEmpty()) {
            toolbar_title.setText(tinyDb.getString("issueTitle"));
        }

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        mergeButton = findViewById(R.id.mergeButton);

        if(!connToInternet) {

            disableProcessButton();

        } else {

            mergeButton.setOnClickListener(mergePullRequest);

        }

    }

    public void loadCollaboratorsList() {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

                if (response.isSuccessful()) {

                    assert response.body() != null;
                    String fullName = "";
                    for (int i = 0; i < response.body().size(); i++) {
                        if(!response.body().get(i).getFull_name().equals("")) {
                            fullName = response.body().get(i).getFull_name();
                        }
                        defaultMentionAdapter.add(
                                new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
                    }

                }
                else {

                    Log.i("onResponse", String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
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

    private View.OnClickListener mergePullRequest = new View.OnClickListener() {
        public void onClick(View v) {
            processMergePullRequest();
        }
    };

    private void processMergePullRequest() {

        String mergePRDT = mergePR.getText().toString();
        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        disableProcessButton();
        String doWhat = "merge";
        mergeFunction(doWhat, mergePRDT);

    }

    private void mergeFunction(String doWhat, String mergePRDT) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final int prIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

        MergePullRequest mergePR = new MergePullRequest(doWhat, mergePRDT, null);

        Call<ResponseBody> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .mergePullRequest(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, prIndex, mergePR);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

                if(response.code() == 200) {

                    Toasty.info(getApplicationContext(), getString(R.string.mergePRSuccessMsg));
                    tinyDb.putBoolean("prMerged", true);
                    tinyDb.putBoolean("resumePullRequests", true);
                    finish();

                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 404) {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.mergePR404ErrorMsg));

                }
                else {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.genericError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }

        });

    }

    private void disableProcessButton() {

        mergeButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        mergeButton.setBackground(shape);

    }

    private void enableProcessButton() {

        mergeButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        mergeButton.setBackground(shape);

    }

}
