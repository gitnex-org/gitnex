package org.mian.gitnex.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.FilesDiffAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.FileDiffView;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class FileDiffActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private TextView toolbar_title;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_file_diff;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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

        ImageView closeActivity = findViewById(R.id.close);
        toolbar_title = findViewById(R.id.toolbar_title);
        mRecyclerView = findViewById(R.id.recyclerView);
        mProgressBar = findViewById(R.id.progress_bar);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        toolbar_title.setText(R.string.processingText);
        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        mProgressBar.setVisibility(View.VISIBLE);

        String fileDiffName = tinyDb.getString("issueNumber")+".diff";

        getFileContents(tinyDb.getString("instanceUrlWithProtocol"), repoOwner, repoName, fileDiffName);

    }

    private void getFileContents(String instanceUrl, String owner, String repo, String filename) {

        Call<ResponseBody> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getFileDiffContents(owner, repo, filename);

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

                if (response.code() == 200) {

                    try {
                        assert response.body() != null;

                        AppUtil appUtil = new AppUtil();
                        List<FileDiffView> fileContentsArray = new ArrayList<>();

                        String[] lines = response.body().string().split("diff");

                        if(lines.length > 0) {

                            for (int i = 1; i < lines.length; i++) {

                                if(lines[i].contains("@@ -")) {

                                    String[] level2nd = lines[i].split("@@ -"); // main content part of single diff view

                                    String[] fileName_ = level2nd[0].split("\\+\\+\\+ b/"); // filename part
                                    String fileNameFinal = fileName_[1];

                                    String[] fileContents_ = level2nd[1].split("@@"); // file info / content part
                                    String fileInfoFinal = fileContents_[0];
                                    String fileContentsFinal = (fileContents_[1]);

                                    if(level2nd.length > 2) {
                                        for (int j = 2; j < level2nd.length; j++) {
                                            fileContentsFinal += (level2nd[j]);
                                        }
                                    }

                                    String fileExtension = FilenameUtils.getExtension(fileNameFinal);

                                    String fileContentsFinalWithBlankLines = fileContentsFinal.replaceAll( ".*@@.*", "" );
                                    String fileContentsFinalWithoutBlankLines = fileContentsFinal.replaceAll( ".*@@.*(\r?\n|\r)?", "" );
                                    fileContentsFinalWithoutBlankLines = fileContentsFinalWithoutBlankLines.replaceAll( ".*\\ No newline at end of file.*(\r?\n|\r)?", "" );

                                    fileContentsArray.add(new FileDiffView(fileNameFinal, appUtil.imageExtension(fileExtension), fileInfoFinal, fileContentsFinalWithoutBlankLines));
                                }
                                else {

                                    String[] getFileName = lines[i].split("--git a/");

                                    String[] getFileName_ = getFileName[1].split("b/");
                                    String getFileNameFinal = getFileName_[0].trim();

                                    String[] binaryFile = getFileName_[1].split("GIT binary patch");
                                    String binaryFileRaw = binaryFile[1].substring(binaryFile[1].indexOf('\n')+1);
                                    String binaryFileFinal = binaryFile[1].substring(binaryFileRaw.indexOf('\n')+1);

                                    String fileExtension = FilenameUtils.getExtension(getFileNameFinal);

                                    fileContentsArray.add(new FileDiffView(getFileNameFinal, appUtil.imageExtension(fileExtension),"", binaryFileFinal));
                                }

                            }

                        }

                        int filesCount = fileContentsArray.size();
                        if(filesCount > 1) {
                            toolbar_title.setText(getResources().getString(R.string.fileDiffViewHeader, Integer.toString(filesCount)));
                        }
                        else {
                            toolbar_title.setText(getResources().getString(R.string.fileDiffViewHeaderSingle, Integer.toString(filesCount)));
                        }

                        FilesDiffAdapter adapter = new FilesDiffAdapter(fileContentsArray, getApplicationContext());
                        mRecyclerView.setAdapter(adapter);

                        mProgressBar.setVisibility(View.GONE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(getApplicationContext(), getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(getApplicationContext(), getApplicationContext().getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(getApplicationContext(), getApplicationContext().getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.labelGeneralError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
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
