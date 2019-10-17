package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class FileViewActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;
    private TextView singleFileContents;
    final Context ctx = this;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TinyDB tinyDb = new TinyDB(getApplicationContext());
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        ImageView closeActivity = findViewById(R.id.close);
        singleFileContents = findViewById(R.id.singleFileContents);
        singleFileContents.setVisibility(View.GONE);
        mProgressBar = findViewById(R.id.progress_bar);

        String singleFileName = getIntent().getStringExtra("singleFileName");

        TextView toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_title.setMovementMethod(new ScrollingMovementMethod());
        toolbar_title.setText(singleFileName);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        if(connToInternet) {
            getSingleFileContents(instanceUrl, instanceToken, repoOwner, repoName, singleFileName);
        }
        else {
            Toasty.info(getApplicationContext(), getString(R.string.checkNetConnection));
        }

    }

    private void getSingleFileContents(String instanceUrl, String token, final String owner, String repo, final String filename) {

        Call<Files> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getSingleFileContents(token, owner, repo, filename);

        call.enqueue(new Callback<Files>() {

            @Override
            public void onResponse(@NonNull Call<Files> call, @NonNull retrofit2.Response<Files> response) {

                if (response.code() == 200) {

                    AppUtil appUtil = new AppUtil();
                    assert response.body() != null;

                    if(!response.body().getContent().equals("")) {
                        singleFileContents.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                        singleFileContents.setText(appUtil.decodeBase64(response.body().getContent()));
                    }
                    else {
                        singleFileContents.setText("");
                        mProgressBar.setVisibility(View.GONE);
                    }

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

                    Toasty.info(getApplicationContext(), getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Files> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIntent().removeExtra("singleFileName");
                finish();
            }
        };
    }

}
