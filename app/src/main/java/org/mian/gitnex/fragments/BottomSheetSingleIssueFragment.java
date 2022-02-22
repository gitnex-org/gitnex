package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.DiffActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.databinding.BottomSheetSingleIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.views.ReactionSpinner;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class BottomSheetSingleIssueFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;
	private final String issueCreator;

	public BottomSheetSingleIssueFragment(String username) {
		issueCreator = username;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetSingleIssueBinding binding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		boolean userIsCreator = issueCreator.equals(tinyDB.getString("loginUid"));
		boolean isRepoAdmin = tinyDB.getBoolean("isRepoAdmin");
		boolean canPush = tinyDB.getBoolean("canPush");
		boolean archived = tinyDB.getBoolean("isArchived");

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		Bundle bundle = new Bundle();

		bundle.putString("repoOwner", parts[0]);
		bundle.putString("repoName", parts[1]);
		bundle.putInt("issueId", Integer.parseInt(tinyDB.getString("issueNumber")));

		TextView loadReactions = new TextView(ctx);
		loadReactions.setText(Objects.requireNonNull(ctx).getString(R.string.genericWaitFor));
		loadReactions.setGravity(Gravity.CENTER);
		loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
		binding.commentReactionButtons.addView(loadReactions);

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle);
		reactionSpinner.setOnInteractedListener(() -> {

			tinyDB.putBoolean("singleIssueUpdate", true);

			bmListener.onButtonClicked("onResume");
			dismiss();
		});
		reactionSpinner.setOnLoadingFinishedListener(() -> {
			reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			binding.commentReactionButtons.removeView(loadReactions);
			binding.commentReactionButtons.addView(reactionSpinner);
		});

		if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

			binding.editIssue.setText(R.string.editPrText);
			binding.copyIssueUrl.setText(R.string.copyPrUrlText);
			binding.shareIssue.setText(R.string.sharePr);

			boolean canPushPullSource = tinyDB.getBoolean("canPushPullSource");
			if(tinyDB.getBoolean("prMerged") || tinyDB.getString("repoPrState").equals("closed")) {
				binding.updatePullRequest.setVisibility(View.GONE);
				binding.mergePullRequest.setVisibility(View.GONE);
				if(canPushPullSource) {
					binding.deletePrHeadBranch.setVisibility(View.VISIBLE);
				}
				else {
					if(!canPush) {
						binding.editIssue.setVisibility(View.GONE);
					}
					binding.deletePrHeadBranch.setVisibility(View.GONE);
				}
			}
			else {
				if(canPushPullSource) {
					binding.updatePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					binding.updatePullRequest.setVisibility(View.GONE);
				}
				if(!userIsCreator && !canPush) {
					binding.editIssue.setVisibility(View.GONE);
				}
				if(canPush && !tinyDB.getString("prMergeable").equals("false")) {
					binding.mergePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					binding.mergePullRequest.setVisibility(View.GONE);
				}
				binding.deletePrHeadBranch.setVisibility(View.GONE);
			}

			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.13.0")) {
				binding.openFilesDiff.setVisibility(View.VISIBLE);
			}
			else if(tinyDB.getString("repoType").equals("public")) {
				binding.openFilesDiff.setVisibility(View.VISIBLE);
			}
			else {
				binding.openFilesDiff.setVisibility(View.GONE);
			}

		}
		else {
			if(!userIsCreator && !canPush) {
				binding.editIssue.setVisibility(View.GONE);
			}
			binding.updatePullRequest.setVisibility(View.GONE);
			binding.mergePullRequest.setVisibility(View.GONE);
			binding.deletePrHeadBranch.setVisibility(View.GONE);
		}

		binding.updatePullRequest.setOnClickListener(v -> {
			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.16.0")) {
				AlertDialogs.selectPullUpdateStrategy(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"));
			}
			else {
				PullRequestActions.updatePr(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"), null);
			}
			dismiss();
		});

		binding.mergePullRequest.setOnClickListener(v13 -> {
			startActivity(new Intent(ctx, MergePullRequestActivity.class));
			dismiss();
		});

		binding.openFilesDiff.setOnClickListener(v14 -> {
			startActivity(new Intent(ctx, DiffActivity.class));
			dismiss();
		});

		binding.deletePrHeadBranch.setOnClickListener(v -> {

			PullRequestActions.deleteHeadBranch(ctx, parts[0], parts[1], tinyDB.getString("prHeadBranch"), true);
			dismiss();
		});



		binding.editIssue.setOnClickListener(v15 -> {
			startActivity(new Intent(ctx, EditIssueActivity.class));
			dismiss();
		});

		binding.editLabels.setOnClickListener(v16 -> {
			bmListener.onButtonClicked("showLabels");
			dismiss();
		});

		binding.addRemoveAssignees.setOnClickListener(v17 -> {
			bmListener.onButtonClicked("showAssignees");
			dismiss();
		});

		binding.shareIssue.setOnClickListener(v1 -> {

			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle"));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDB.getString("singleIssueHtmlUrl"));
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle")));

			dismiss();
		});

		binding.copyIssueUrl.setOnClickListener(v12 -> {

			// copy to clipboard
			ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("issueUrl", tinyDB.getString("singleIssueHtmlUrl"));
			assert clipboard != null;
			clipboard.setPrimaryClip(clip);

			Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

			dismiss();
		});

		if(tinyDB.getString("issueState").equals("open")) { // close issue
			if(!userIsCreator && !canPush) {
				binding.closeIssue.setVisibility(View.GONE);
				binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			}
			else if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {
				binding.closeIssue.setText(R.string.closePr);
			}
			binding.closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "closed");
				dismiss();
			});
		}
		else if(tinyDB.getString("issueState").equals("closed")) {
			if(userIsCreator || canPush) {
				if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {
					binding.closeIssue.setText(R.string.reopenPr);
				}
				else {
					binding.closeIssue.setText(R.string.reOpenIssue);
				}
			}
			else {
				binding.closeIssue.setVisibility(View.GONE);
				binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			}
			binding.closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "open");
				dismiss();
			});
		}

		binding.subscribeIssue.setOnClickListener(subscribeToIssue -> {

			IssueActions.subscribe(ctx);
			dismiss();
		});

		binding.unsubscribeIssue.setOnClickListener(unsubscribeToIssue -> {

			IssueActions.unsubscribe(ctx);
			dismiss();
		});

		if(new Version(tinyDB.getString("giteaVersion")).less("1.12.0")) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
		}
		else if(tinyDB.getBoolean("issueSubscribed")) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.VISIBLE);
		}
		else {
			binding.subscribeIssue.setVisibility(View.VISIBLE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
		}

		if(archived) {
			binding.subscribeIssue.setVisibility(View.GONE);
			binding.unsubscribeIssue.setVisibility(View.GONE);
			binding.editIssue.setVisibility(View.GONE);
			binding.editLabels.setVisibility(View.GONE);
			binding.closeIssue.setVisibility(View.GONE);
			binding.dividerCloseReopenIssue.setVisibility(View.GONE);
			binding.addRemoveAssignees.setVisibility(View.GONE);
			binding.commentReactionButtons.setVisibility(View.GONE);
			binding.shareDivider.setVisibility(View.GONE);
		}

		return binding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {

			bmListener = (BottomSheetListener) context;
		}
		catch(ClassCastException e) {

			throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
		}
	}
}
