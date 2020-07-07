package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityMergePullRequestBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.MergePullRequest;
import org.mian.gitnex.models.MergePullRequestSpinner;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MergePullRequestActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	final Context ctx = this;
	private Context appCtx;
	private ActivityMergePullRequestBinding viewBinding;

	private ArrayAdapter<Mention> defaultMentionAdapter;
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

		viewBinding = ActivityMergePullRequestBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
		TinyDB tinyDb = new TinyDB(appCtx);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		viewBinding.mergeTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(viewBinding.mergeTitle, InputMethodManager.SHOW_IMPLICIT);

		setMergeAdapter();

		viewBinding.mergeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				MergePullRequestSpinner mergeId = (MergePullRequestSpinner) parent.getSelectedItem();
				Do = mergeId.getId();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		defaultMentionAdapter = new MentionArrayAdapter<>(this);
		loadCollaboratorsList();

		viewBinding.mergeDescription.setMentionAdapter(defaultMentionAdapter);

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

		TinyDB tinyDb = new TinyDB(appCtx);

		ArrayList<MergePullRequestSpinner> mergeList = new ArrayList<>();

		mergeList.add(new MergePullRequestSpinner("merge", getResources().getString(R.string.mergeOptionMerge)));
		mergeList.add(new MergePullRequestSpinner("rebase", getResources().getString(R.string.mergeOptionRebase)));
		mergeList.add(new MergePullRequestSpinner("rebase-merge", getResources().getString(R.string.mergeOptionRebaseCommit)));
		// squash merge works only on gitea > v1.11.4 due to a bug
		if(new Version(tinyDb.getString("giteaVersion")).higher("1.11.4")) {
			mergeList.add(new MergePullRequestSpinner("squash", getResources().getString(R.string.mergeOptionSquash)));
		}

		ArrayAdapter<MergePullRequestSpinner> adapter = new ArrayAdapter<>(MergePullRequestActivity.this, R.layout.spinner_item, mergeList);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		viewBinding.mergeSpinner.setAdapter(adapter);

	}

	public void loadCollaboratorsList() {

		final TinyDB tinyDb = new TinyDB(appCtx);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		Call<List<Collaborators>> call = RetrofitClient.getInstance(instanceUrl, ctx).getApiInterface().getCollaborators(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

		call.enqueue(new Callback<List<Collaborators>>() {

			@Override
			public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					String fullName = "";
					for(int i = 0; i < response.body().size(); i++) {
						if(!response.body().get(i).getFull_name().equals("")) {
							fullName = response.body().get(i).getFull_name();
						}
						defaultMentionAdapter.add(new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
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

		onClickListener = view -> finish();
	}

	private View.OnClickListener mergePullRequest = v -> processMergePullRequest();

	private void processMergePullRequest() {

		String mergePRDesc = viewBinding.mergeDescription.getText().toString();
		String mergePRTitle = viewBinding.mergeTitle.getText().toString();
		boolean deleteBranch = viewBinding.deleteBranch.isChecked();

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		if(!connToInternet) {

			Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
			return;

		}

		disableProcessButton();
		mergeFunction(Do, mergePRDesc, mergePRTitle, deleteBranch);

	}

	private void mergeFunction(String Do, String mergePRDT, String mergeTitle, boolean deleteBranch) {

		final TinyDB tinyDb = new TinyDB(appCtx);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final int prIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

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

							Toasty.info(ctx, getString(R.string.mergePRSuccessMsg));
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

							Toasty.info(ctx, getString(R.string.mergePRSuccessMsg));
							tinyDb.putBoolean("prMerged", true);
							tinyDb.putBoolean("resumePullRequests", true);
							finish();

						}

					}
					else {

						Toasty.info(ctx, getString(R.string.mergePRSuccessMsg));
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
					Toasty.info(ctx, getString(R.string.mergePR404ErrorMsg));

				}
				else {

					enableProcessButton();
					Toasty.info(ctx, getString(R.string.genericError));

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

		TinyDB tinyDb = new TinyDB(appCtx);

		String instanceUrl = tinyDb.getString("instanceUrl");
		String loginUid = tinyDb.getString("loginUid");
		String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
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
		viewBinding.mergeButton.setBackground(getResources().getDrawable(R.drawable.shape_buttons_disabled));

	}

	private void enableProcessButton() {

		viewBinding.mergeButton.setEnabled(true);
		viewBinding.mergeButton.setBackground(getResources().getDrawable(R.drawable.shape_buttons));

	}

}
