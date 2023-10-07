package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.MergePullRequestOption;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityMergePullRequestBinding;
import org.mian.gitnex.fragments.PullRequestsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.MergePullRequestSpinner;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class MergePullRequestActivity extends BaseActivity {

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

		setMergeAdapter();

		if (!issue.getPullRequest().getTitle().isEmpty()) {
			viewBinding.topAppBar.setTitle(issue.getPullRequest().getTitle());
			viewBinding.mergeTitle.setText(
					issue.getPullRequest().getTitle() + " (#" + issue.getIssueIndex() + ")");
		}

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		MenuItem attachment = viewBinding.topAppBar.getMenu().getItem(0);
		MenuItem markdown = viewBinding.topAppBar.getMenu().getItem(1);
		MenuItem create = viewBinding.topAppBar.getMenu().getItem(2);
		attachment.setVisible(false);
		markdown.setVisible(false);
		create.setTitle(getString(R.string.mergePullRequestButtonText));

		// if gitea version is greater/equal(1.12.0) than user installed version
		// (installed.higherOrEqual(compareVer))
		if (getAccount().requiresVersion("1.12.0")) {

			viewBinding.deleteBranch.setVisibility(View.VISIBLE);
		}

		if (!issue.getPullRequest().isMergeable()) {
			viewBinding.mergeInfoDisabledMessage.setVisibility(View.VISIBLE);
			create.setVisible(false);
		} else {
			viewBinding.mergeInfoDisabledMessage.setVisibility(View.GONE);
			create.setVisible(true);
		}

		if (issue.prIsFork()) {
			viewBinding.deleteBranchForkInfo.setVisibility(View.VISIBLE);
		} else {
			viewBinding.deleteBranchForkInfo.setVisibility(View.GONE);
		}

		if (!(issue.getPullRequest().getHead().getRepo() != null
				? issue.getPullRequest().getHead().getRepo().getPermissions().isPush()
				: false)) {
			viewBinding.deleteBranch.setVisibility(View.GONE);
			viewBinding.deleteBranchForkInfo.setVisibility(View.GONE);
		}

		viewBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processMergePullRequest();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void setMergeAdapter() {

		ArrayList<MergePullRequestSpinner> mergeList = new ArrayList<>();

		mergeList.add(
				new MergePullRequestSpinner(
						"merge", getResources().getString(R.string.mergeOptionMerge)));
		mergeList.add(
				new MergePullRequestSpinner(
						"rebase", getResources().getString(R.string.mergeOptionRebase)));
		mergeList.add(
				new MergePullRequestSpinner(
						"rebase-merge",
						getResources().getString(R.string.mergeOptionRebaseCommit)));
		// squash merge works only on gitea > v1.11.4 due to a bug
		if (getAccount().requiresVersion("1.12.0")) {

			mergeList.add(
					new MergePullRequestSpinner(
							"squash", getResources().getString(R.string.mergeOptionSquash)));
		}

		ArrayAdapter<MergePullRequestSpinner> adapter =
				new ArrayAdapter<>(
						MergePullRequestActivity.this, R.layout.list_spinner_items, mergeList);
		viewBinding.mergeSpinner.setAdapter(adapter);

		viewBinding.mergeSpinner.setOnItemClickListener(
				(parent, view, position, id) -> {
					Do = mergeList.get(position).getId();
				});
	}

	private void processMergePullRequest() {

		String mergePRDesc =
				Objects.requireNonNull(viewBinding.mergeDescription.getText()).toString();
		String mergePRTitle = Objects.requireNonNull(viewBinding.mergeTitle.getText()).toString();
		boolean deleteBranch = viewBinding.deleteBranch.isChecked();

		if (Do == null) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.selectMergeStrategy));
		} else {

			mergeFunction(Do, mergePRDesc, mergePRTitle, deleteBranch);
		}
	}

	private void mergeFunction(
			String Do, String mergePRDT, String mergeTitle, boolean deleteBranch) {

		MergePullRequestOption mergePR = new MergePullRequestOption();
		mergePR.setDeleteBranchAfterMerge(deleteBranch);
		mergePR.setMergeTitleField(mergeTitle);
		mergePR.setMergeMessageField(mergePRDT);
		switch (Do) {
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

		Call<Void> call =
				RetrofitClient.getApiInterface(ctx)
						.repoMergePullRequest(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								mergePR);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.code() == 200) {

							if (deleteBranch) {

								if (issue.prIsFork()) {
									String repoFullName =
											issue.getPullRequest()
													.getHead()
													.getRepo()
													.getFullName();
									String[] parts = repoFullName.split("/");
									final String repoOwner = parts[0];
									final String repoName = parts[1];

									PullRequestActions.deleteHeadBranch(
											ctx,
											repoOwner,
											repoName,
											issue.getPullRequest().getHead().getRef(),
											false);
								} else {
									PullRequestActions.deleteHeadBranch(
											ctx,
											issue.getRepository().getOwner(),
											issue.getRepository().getName(),
											issue.getPullRequest().getHead().getRef(),
											false);
								}
							}

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.mergePRSuccessMsg));

							Intent result = new Intent();
							PullRequestsFragment.resumePullRequests = true;
							IssueDetailActivity.singleIssueUpdate = true;
							RepoDetailActivity.updateRepo = true;
							setResult(200, result);
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 404) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.mergePR404ErrorMsg));
						} else if (response.code() == 405) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.mergeNotAllowed));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		issue.getRepository().checkAccountSwitch(this);
	}
}
