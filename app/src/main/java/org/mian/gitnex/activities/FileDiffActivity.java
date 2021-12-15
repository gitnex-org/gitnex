package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import org.gitnex.tea4j.models.FileDiffView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.FilesDiffAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityFileDiffBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ParseDiff;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

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
		getPullDiffContent(repoOwner, repoName, pullIndex, apiCall);

	}

	private void getPullDiffContent(String owner, String repo, String pullIndex, boolean apiCall) {

		Thread thread = new Thread(() -> {

			Call<ResponseBody> call = apiCall ?
				RetrofitClient.getApiInterface(ctx).getPullDiffContent(Authorization.get(ctx), owner, repo, pullIndex) :
				RetrofitClient.getWebInterface(ctx).getPullDiffContent(Authorization.getWeb(ctx), owner, repo, pullIndex);

			try {

				Response<ResponseBody> response = call.execute();
				assert response.body() != null;

				switch(response.code()) {

					case 200:
						List<FileDiffView> fileDiffViews = ParseDiff.getFileDiffViewArray(response.body().string());

						int filesCount = fileDiffViews.size();

						String toolbarTitleText = (filesCount > 1) ?
							getResources().getString(R.string.fileDiffViewHeader, Integer.toString(filesCount)) :
							getResources().getString(R.string.fileDiffViewHeaderSingle, Integer.toString(filesCount));

						FilesDiffAdapter adapter = new FilesDiffAdapter(ctx, getSupportFragmentManager(), fileDiffViews);

						runOnUiThread(() -> {
							toolbarTitle.setText(toolbarTitleText);
							mListView.setAdapter(adapter);
							mProgressBar.setVisibility(View.GONE);
						});
						break;

					case 401:
						runOnUiThread(() -> AlertDialogs.authorizationTokenRevokedDialog(ctx,
							getString(R.string.alertDialogTokenRevokedTitle),
							getString(R.string.alertDialogTokenRevokedMessage),
							getString(R.string.cancelButton),
							getString(R.string.navLogout)));
						break;

					case 403:
						runOnUiThread(() -> Toasty.error(ctx, ctx.getString(R.string.authorizeError)));
						break;

					case 404:
						runOnUiThread(() -> Toasty.warning(ctx, ctx.getString(R.string.apiNotFound)));
						break;

					default:
						runOnUiThread(() -> Toasty.error(ctx, getString(R.string.labelGeneralError)));

				}
			} catch(IOException ignored) {}

		});

		thread.start();

	}

	private void initCloseListener() {

		onClickListener = view -> {

			getIntent().removeExtra("singleFileName");
			finish();

		};
	}

}
