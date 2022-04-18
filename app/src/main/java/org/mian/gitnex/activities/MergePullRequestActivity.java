package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.MergePullRequestOption;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityMergePullRequestBinding;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.MergePullRequestSpinner;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import java.util.ArrayList;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class MergePullRequestActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private IssueContext issue;
	private ActivityMergePullRequestBinding viewBinding;
	private String Do;

	@SuppressLint("SetTextI18n")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityMergePullRequestBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		issue = IssueContext.fromIntent(getIntent());

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		viewBinding.mergeTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(viewBinding.mergeTitle, InputMethodManager.SHOW_IMPLICIT);

		setMergeAdapter();

		if(!issue.getPullRequest().getTitle().isEmpty()) {

			viewBinding.toolbarTitle.setText(issue.getPullRequest().getTitle());
			viewBinding.mergeTitle.setText(issue.getPullRequest().getTitle() + " (#" + issue.getIssueIndex() + ")");
		}

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		// if gitea version is greater/equal(1.12.0) than user installed version (installed.higherOrEqual(compareVer))
		if(getAccount().requiresVersion("1.12.0")) {

			viewBinding.deleteBranch.setVisibility(View.VISIBLE);
		}

		if(!issue.getPullRequest().isMergeable()) {

			disableProcessButton();
			viewBinding.mergeInfoDisabledMessage.setVisibility(View.VISIBLE);
		}
		else {

			viewBinding.mergeInfoDisabledMessage.setVisibility(View.GONE);
		}

		if(issue.prIsFork()) {

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

		if(!issue.getPullRequest().getHead().getRepo().getPermissions().isPush()) {
			viewBinding.deleteBranch.setVisibility(View.GONE);
			viewBinding.deleteBranchForkInfo.setVisibility(View.GONE);
		}
	}

	private void setMergeAdapter() {

		ArrayList<MergePullRequestSpinner> mergeList = new ArrayList<>();

		mergeList.add(new MergePullRequestSpinner("merge", getResources().getString(R.string.mergeOptionMerge)));
		mergeList.add(new MergePullRequestSpinner("rebase", getResources().getString(R.string.mergeOptionRebase)));
		mergeList.add(new MergePullRequestSpinner("rebase-merge", getResources().getString(R.string.mergeOptionRebaseCommit)));
		// squash merge works only on gitea > v1.11.4 due to a bug
		if(getAccount().requiresVersion("1.12.0")) {

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

	private final View.OnClickListener mergePullRequest = v -> processMergePullRequest();

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

		MergePullRequestOption mergePR = new MergePullRequestOption();
		mergePR.setDeleteBranchAfterMerge(deleteBranch);
		mergePR.setMergeTitleField(mergeTitle);
		mergePR.setMergeMessageField(mergePRDT);
		switch(Do) {
			case "merge":
				mergePR.setDo(MergePullRequestOption.DoEnum.MERGE);
				break;
			case "rebase":
				mergePR.setDo(MergePullRequestOption.DoEnum.REBASE);
				break;
			case "rebase-merge":
				mergePR.setDo(MergePullRequestOption.DoEnum.REBASE_MERGE);
				break;
			case "squash":
				mergePR.setDo(MergePullRequestOption.DoEnum.SQUASH);
				break;
		}

		Call<Void> call = RetrofitClient.getApiInterface(ctx).repoMergePullRequest(issue.getRepository().getOwner(), issue.getRepository().getName(), (long) issue.getIssueIndex(), mergePR);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				if(response.code() == 200) {

					if(deleteBranch) {

						if(issue.prIsFork()) {
							String repoFullName = issue.getPullRequest().getHead().getRepo().getFullName();
							String[] parts = repoFullName.split("/");
							final String repoOwner = parts[0];
							final String repoName = parts[1];

							PullRequestActions.deleteHeadBranch(ctx, repoOwner, repoName, issue.getPullRequest().getHead().getRef(), false);
						}
						else {
							PullRequestActions.deleteHeadBranch(ctx, issue.getRepository().getOwner(), issue.getRepository().getName(),
								issue.getPullRequest().getHead().getRef(), false);
						}

					}

					Toasty.success(ctx, getString(R.string.mergePRSuccessMsg));
					Intent result = new Intent();
					PullRequestsFragment.resumePullRequests = true;
					IssueDetailActivity.singleIssueUpdate = true;
					RepoDetailActivity.updateRepo = true;
					setResult(200, result);
					finish();
				}
				else if(response.code() == 401) {

					enableProcessButton();
					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.cancelButton),
						getResources().getString(R.string.navLogout));
				}
				else if(response.code() == 404) {

					enableProcessButton();
					Toasty.warning(ctx, getString(R.string.mergePR404ErrorMsg));
				}
				else if(response.code() == 405) {

					enableProcessButton();
					Toasty.warning(ctx, getString(R.string.mergeNotAllowed));
				}
				else {

					enableProcessButton();
					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
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

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}
}
