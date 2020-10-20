package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityMergePullRequestBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.MergePullRequest;
import org.mian.gitnex.models.MergePullRequestSpinner;
import java.util.ArrayList;
import java.util.Objects;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class MergePullRequestActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	final Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;
	private String repoOwner;
	private String repoName;
	private int prIndex;

	private ActivityMergePullRequestBinding viewBinding;

	private String Do;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_merge_pull_request;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		viewBinding = ActivityMergePullRequestBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		instanceUrl = tinyDb.getString("instanceUrl");
		loginUid = tinyDb.getString("loginUid");
		instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		repoOwner = parts[0];
		repoName = parts[1];
		prIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		viewBinding.mergeTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(viewBinding.mergeTitle, InputMethodManager.SHOW_IMPLICIT);

		setMergeAdapter();

		if(!tinyDb.getString("issueTitle").isEmpty()) {

			viewBinding.toolbarTitle.setText(tinyDb.getString("issueTitle"));
			viewBinding.mergeTitle.setText(tinyDb.getString("issueTitle") + " (#" + tinyDb.getString("issueNumber") + ")");
		}

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		// if gitea version is greater/equal(1.12.0) than user installed version (installed.higherOrEqual(compareVer))
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			viewBinding.deleteBranch.setVisibility(View.VISIBLE);
		}

		if(tinyDb.getString("prMergeable").equals("false")) {
			disableProcessButton();
			viewBinding.mergeInfoDisabledMessage.setVisibility(View.VISIBLE);
		}
		else {
			viewBinding.mergeInfoDisabledMessage.setVisibility(View.GONE);
		}

		if(tinyDb.getString("prIsFork").equals("true")) {
			viewBinding.deleteBranchForkInfo.setVisibility(View.VISIBLE);
		}
		else {
			viewBinding.deleteBranchForkInfo.setVisibility(View.GONE);
		}

		if(!connToInternet) {

			disableProcessButton();
		}
		else {

			viewBinding.mergeButton.setOnClickListener(mergePullRequest);
		}

	}

	private void setMergeAdapter() {

		ArrayList<MergePullRequestSpinner> mergeList = new ArrayList<>();

		mergeList.add(new MergePullRequestSpinner("merge", getResources().getString(R.string.mergeOptionMerge)));
		mergeList.add(new MergePullRequestSpinner("rebase", getResources().getString(R.string.mergeOptionRebase)));
		mergeList.add(new MergePullRequestSpinner("rebase-merge", getResources().getString(R.string.mergeOptionRebaseCommit)));
		// squash merge works only on gitea > v1.11.4 due to a bug
		if(new Version(tinyDb.getString("giteaVersion")).higher("1.11.4")) {
			mergeList.add(new MergePullRequestSpinner("squash", getResources().getString(R.string.mergeOptionSquash)));
		}

		ArrayAdapter<MergePullRequestSpinner> adapter = new ArrayAdapter<>(MergePullRequestActivity.this, R.layout.list_spinner_items, mergeList);
		viewBinding.mergeSpinner.setAdapter(adapter);

		viewBinding.mergeSpinner.setOnItemClickListener ((parent, view, position, id) -> {

			Do = mergeList.get(position).getId();
		});

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private View.OnClickListener mergePullRequest = v -> processMergePullRequest();

	private void processMergePullRequest() {

		String mergePRDesc = Objects.requireNonNull(viewBinding.mergeDescription.getText()).toString();
		String mergePRTitle = Objects.requireNonNull(viewBinding.mergeTitle.getText()).toString();
		boolean deleteBranch = viewBinding.deleteBranch.isChecked();

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		if(!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if(Do == null) {

			Toasty.error(ctx, getResources().getString(R.string.selectMergeStrategy));
		}
		else {

			disableProcessButton();
			mergeFunction(Do, mergePRDesc, mergePRTitle, deleteBranch);
		}
	}

	private void mergeFunction(String Do, String mergePRDT, String mergeTitle, boolean deleteBranch) {

		MergePullRequest mergePR = new MergePullRequest(Do, mergePRDT, mergeTitle);

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().mergePullRequest(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, prIndex, mergePR);

		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

				if(response.code() == 200) {

					if(deleteBranch) {

						if(tinyDb.getString("prIsFork").equals("true")) {

							String repoFullName = tinyDb.getString("prForkFullName");
							String[] parts = repoFullName.split("/");
							final String repoOwner = parts[0];
							final String repoName = parts[1];

							deleteBranchFunction(repoOwner, repoName);

							Toasty.success(ctx, getString(R.string.mergePRSuccessMsg));
							tinyDb.putBoolean("prMerged", true);
							tinyDb.putBoolean("resumePullRequests", true);
							finish();
						}
						else {

							String repoFullName = tinyDb.getString("repoFullName");
							String[] parts = repoFullName.split("/");
							final String repoOwner = parts[0];
							final String repoName = parts[1];

							deleteBranchFunction(repoOwner, repoName);

							Toasty.success(ctx, getString(R.string.mergePRSuccessMsg));
							tinyDb.putBoolean("prMerged", true);
							tinyDb.putBoolean("resumePullRequests", true);
							finish();
						}

					}
					else {

						Toasty.success(ctx, getString(R.string.mergePRSuccessMsg));
						tinyDb.putBoolean("prMerged", true);
						tinyDb.putBoolean("resumePullRequests", true);
						finish();
					}

				}
				else if(response.code() == 401) {

					enableProcessButton();
					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
				}
				else if(response.code() == 404) {

					enableProcessButton();
					Toasty.warning(ctx, getString(R.string.mergePR404ErrorMsg));
				}
				else {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}

		});

	}

	private void deleteBranchFunction(String repoOwner, String repoName) {

		String branchName = tinyDb.getString("prHeadBranch");

		Call<JsonElement> call = RetrofitClient
				.getInstance(instanceUrl, ctx)
				.getApiInterface()
				.deleteBranch(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, branchName);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 204) {

					Log.i("deleteBranch", "Branch deleted successfully");
				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}

		});

	}

	private void disableProcessButton() {

		viewBinding.mergeButton.setEnabled(false);
	}

	private void enableProcessButton() {

		viewBinding.mergeButton.setEnabled(true);
	}

}
