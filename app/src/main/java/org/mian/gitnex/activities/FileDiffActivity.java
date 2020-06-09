package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.FilesDiffAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
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
	private ListView mListView;
	private ProgressBar mProgressBar;
	final Context ctx = this;
	private Context appCtx;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_file_diff;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		final TinyDB tinyDb = new TinyDB(appCtx);
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);
		toolbar_title = findViewById(R.id.toolbar_title);
		mListView = findViewById(R.id.listView);
		mProgressBar = findViewById(R.id.progress_bar);

		mListView.setDivider(null);

		toolbar_title.setText(R.string.processingText);
		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		mProgressBar.setVisibility(View.VISIBLE);

		String pullIndex = tinyDb.getString("issueNumber");

		boolean apiCall = true;
		String instanceUrl = tinyDb.getString("instanceUrl");

		// fallback for old gitea instances
		if(new Version(tinyDb.getString("giteaVersion")).less("1.13.0")) {
			apiCall = true;
			instanceUrl = tinyDb.getString("instanceUrlWithProtocol");
		}

		getPullDiffContent(instanceUrl, repoOwner, repoName, pullIndex, instanceToken, apiCall);

	}

	private void getPullDiffContent(String instanceUrl, String owner, String repo, String pullIndex, String token, boolean apiCall) {

		Call<ResponseBody> call;
		if(apiCall) {
			call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getPullDiffContent(token, owner, repo, pullIndex);
		}
		else {
			call = RetrofitClient.getInstance(instanceUrl, ctx).getWebInterface().getPullDiffContent(owner, repo, pullIndex);
		}

		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

				if(response.code() == 200) {

					try {
						assert response.body() != null;

						AppUtil appUtil = new AppUtil();
						List<FileDiffView> fileContentsArray = new ArrayList<>();

						String[] lines = response.body().string().split("diff");

						if(lines.length > 0) {

							for(int i = 1; i < lines.length; i++) {

								if(lines[i].contains("@@ -")) {

									String[] level2nd = lines[i].split("@@ -"); // main content part of single diff view

									String[] fileName_ = level2nd[0].split("\\+\\+\\+ b/"); // filename part
									String fileNameFinal = fileName_[1];

									String[] fileContents_ = level2nd[1].split("@@"); // file info / content part
									String fileInfoFinal = fileContents_[0];
									StringBuilder fileContentsFinal = new StringBuilder(fileContents_[1]);

									if(level2nd.length > 2) {
										for(int j = 2; j < level2nd.length; j++) {
											fileContentsFinal.append(level2nd[j]);
										}
									}

									String fileExtension = FileUtils.getExtension(fileNameFinal);

									String fileContentsFinalWithBlankLines = fileContentsFinal.toString().replaceAll(".*@@.*", "");
									String fileContentsFinalWithoutBlankLines = fileContentsFinal.toString().replaceAll(".*@@.*(\r?\n|\r)?", "");
									fileContentsFinalWithoutBlankLines = fileContentsFinalWithoutBlankLines.replaceAll(".*\\ No newline at end of file.*(\r?\n|\r)?", "");

									fileContentsArray.add(new FileDiffView(fileNameFinal, appUtil.imageExtension(fileExtension), fileInfoFinal, fileContentsFinalWithoutBlankLines));
								}
								else {

									String[] getFileName = lines[i].split("--git a/");

									String[] getFileName_ = getFileName[1].split("b/");
									String getFileNameFinal = getFileName_[0].trim();

									String[] binaryFile = getFileName_[1].split("GIT binary patch");
									String binaryFileRaw = binaryFile[1].substring(binaryFile[1].indexOf('\n') + 1);
									String binaryFileFinal = binaryFile[1].substring(binaryFileRaw.indexOf('\n') + 1);

									String fileExtension = FileUtils.getExtension(getFileNameFinal);

									if(appUtil.imageExtension(FileUtils.getExtension(getFileNameFinal))) {

										fileContentsArray.add(new FileDiffView(getFileNameFinal, appUtil.imageExtension(fileExtension), "", binaryFileFinal));
									}

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

						FilesDiffAdapter adapter = new FilesDiffAdapter(ctx, fileContentsArray);
						mListView.setAdapter(adapter);

						mProgressBar.setVisibility(View.GONE);

					}
					catch(IOException e) {
						e.printStackTrace();
					}

				}
				else if(response.code() == 401) {

					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 403) {

					Toasty.info(ctx, ctx.getString(R.string.authorizeError));

				}
				else if(response.code() == 404) {

					Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

				}
				else {

					Toasty.info(ctx, getString(R.string.labelGeneralError));

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
