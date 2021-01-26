package org.mian.gitnex.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.FilesDiffAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityFileDiffBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ParseDiff;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.FileDiffView;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class FileDiffActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private TextView toolbarTitle;
	private ListView mListView;
	private ProgressBar mProgressBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityFileDiffBinding activityFileDiffBinding = ActivityFileDiffBinding.inflate(getLayoutInflater());
		setContentView(activityFileDiffBinding.getRoot());

		Toolbar toolbar = activityFileDiffBinding.toolbar;
		setSupportActionBar(toolbar);

		final TinyDB tinyDb = TinyDB.getInstance(appCtx);
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = activityFileDiffBinding.close;
		toolbarTitle = activityFileDiffBinding.toolbarTitle;
		mListView = activityFileDiffBinding.listView;
		mProgressBar = activityFileDiffBinding.progressBar;

		mListView.setDivider(null);

		toolbarTitle.setText(R.string.processingText);
		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		mProgressBar.setVisibility(View.VISIBLE);

		String pullIndex = tinyDb.getString("issueNumber");

		boolean apiCall = !new Version(tinyDb.getString("giteaVersion")).less("1.13.0");
		getPullDiffContent(repoOwner, repoName, pullIndex, instanceToken, apiCall);

	}

	private void getPullDiffContent(String owner, String repo, String pullIndex, String token, boolean apiCall) {

		Call<ResponseBody> call;
		if(apiCall) {

			call = RetrofitClient.getApiInterface(ctx).getPullDiffContent(token, owner, repo, pullIndex);
		}
		else {

			call = RetrofitClient.getWebInterface(ctx).getPullDiffContent(owner, repo, pullIndex);
		}

		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

				if(response.code() == 200) {

					try {

						assert response.body() != null;

						List<FileDiffView> fileContentsArray = ParseDiff.getFileDiffViewArray(response.body().string());

						int filesCount = fileContentsArray.size();
						if(filesCount > 1) {

							toolbarTitle.setText(getResources().getString(R.string.fileDiffViewHeader, Integer.toString(filesCount)));
						}
						else {

							toolbarTitle.setText(getResources().getString(R.string.fileDiffViewHeaderSingle, Integer.toString(filesCount)));
						}

						FilesDiffAdapter adapter = new FilesDiffAdapter(ctx, getSupportFragmentManager(), fileContentsArray);
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

					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
				}
				else if(response.code() == 404) {

					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
				}
				else {

					Toasty.error(ctx, getString(R.string.labelGeneralError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});

	}

	private void initCloseListener() {

		onClickListener = view -> {

			getIntent().removeExtra("singleFileName");
			finish();
		};
	}

}
