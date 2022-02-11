package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.FileDiffActivity;
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

		BottomSheetSingleIssueBinding bottomSheetSingleIssueBinding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		boolean userIsCreator = issueCreator.equals(tinyDB.getString("loginUid"));
		boolean isRepoAdmin = tinyDB.getBoolean("isRepoAdmin");
		boolean canPush = tinyDB.getBoolean("canPush");
		boolean archived = tinyDB.getBoolean("isArchived");

		TextView editIssue = bottomSheetSingleIssueBinding.editIssue;
		TextView editLabels = bottomSheetSingleIssueBinding.editLabels;
		TextView closeIssue = bottomSheetSingleIssueBinding.closeIssue;
		TextView addRemoveAssignees = bottomSheetSingleIssueBinding.addRemoveAssignees;
		TextView copyIssueUrl = bottomSheetSingleIssueBinding.copyIssueUrl;
		TextView openFilesDiff = bottomSheetSingleIssueBinding.openFilesDiff;
		TextView updatePullRequest = bottomSheetSingleIssueBinding.updatePullRequest;
		TextView mergePullRequest = bottomSheetSingleIssueBinding.mergePullRequest;
		TextView deletePullRequestBranch = bottomSheetSingleIssueBinding.deletePrHeadBranch;
		TextView shareIssue = bottomSheetSingleIssueBinding.shareIssue;
		TextView subscribeIssue = bottomSheetSingleIssueBinding.subscribeIssue;
		TextView unsubscribeIssue = bottomSheetSingleIssueBinding.unsubscribeIssue;
		View closeReopenDivider = bottomSheetSingleIssueBinding.dividerCloseReopenIssue;

		LinearLayout linearLayout = bottomSheetSingleIssueBinding.commentReactionButtons;

		Bundle bundle1 = new Bundle();

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		bundle1.putString("repoOwner", parts[0]);
		bundle1.putString("repoName", parts[1]);
		bundle1.putInt("issueId", Integer.parseInt(tinyDB.getString("issueNumber")));

		TextView loadReactions = new TextView(ctx);
		loadReactions.setText(Objects.requireNonNull(ctx).getString(R.string.genericWaitFor));
		loadReactions.setGravity(Gravity.CENTER);
		loadReactions.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
		linearLayout.addView(loadReactions);

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle1);
		reactionSpinner.setOnInteractedListener(() -> {

			tinyDB.putBoolean("singleIssueUpdate", true);

			bmListener.onButtonClicked("onResume");
			dismiss();
		});

		Handler handler = new Handler();
		handler.postDelayed(() -> {
			linearLayout.removeView(loadReactions);
			reactionSpinner.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 160));
			linearLayout.addView(reactionSpinner);
		}, 2500);

		if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

			editIssue.setText(R.string.editPrText);
			copyIssueUrl.setText(R.string.copyPrUrlText);
			shareIssue.setText(R.string.sharePr);

			boolean canPushPullSource = tinyDB.getBoolean("canPushPullSource");
			if(tinyDB.getBoolean("prMerged") || tinyDB.getString("repoPrState").equals("closed")) {
				updatePullRequest.setVisibility(View.GONE);
				mergePullRequest.setVisibility(View.GONE);
				if(canPushPullSource) {
					deletePullRequestBranch.setVisibility(View.VISIBLE);
				}
				else {
					if(!canPush) {
						editIssue.setVisibility(View.GONE);
					}
					deletePullRequestBranch.setVisibility(View.GONE);
				}
			}
			else {
				if(canPushPullSource) {
					updatePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					updatePullRequest.setVisibility(View.GONE);
				}
				if(!userIsCreator && !canPush) {
					editIssue.setVisibility(View.GONE);
				}
				if(canPush && !tinyDB.getString("prMergeable").equals("false")) {
					mergePullRequest.setVisibility(View.VISIBLE);
				}
				else {
					mergePullRequest.setVisibility(View.GONE);
				}
				deletePullRequestBranch.setVisibility(View.GONE);
			}

			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.13.0")) {
				openFilesDiff.setVisibility(View.VISIBLE);
			}
			else if(tinyDB.getString("repoType").equals("public")) {
				openFilesDiff.setVisibility(View.VISIBLE);
			}
			else {
				openFilesDiff.setVisibility(View.GONE);
			}

		}
		else {
			if(!userIsCreator && !canPush) {
				editIssue.setVisibility(View.GONE);
			}
			updatePullRequest.setVisibility(View.GONE);
			mergePullRequest.setVisibility(View.GONE);
			deletePullRequestBranch.setVisibility(View.GONE);
		}

		updatePullRequest.setOnClickListener(v -> {
			if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.16.0")) {
				AlertDialogs.selectPullUpdateStrategy(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"));
			}
			else {
				PullRequestActions.updatePr(requireContext(), parts[0], parts[1], tinyDB.getString("issueNumber"), null);
			}
			dismiss();
		});

		mergePullRequest.setOnClickListener(v13 -> {

			startActivity(new Intent(ctx, MergePullRequestActivity.class));
			dismiss();
		});

		deletePullRequestBranch.setOnClickListener(v -> {

			PullRequestActions.deleteHeadBranch(ctx, parts[0], parts[1], tinyDB.getString("prHeadBranch"), true);
			dismiss();
		});

		openFilesDiff.setOnClickListener(v14 -> {

			startActivity(new Intent(ctx, FileDiffActivity.class));
			dismiss();
		});

		editIssue.setOnClickListener(v15 -> {

			startActivity(new Intent(ctx, EditIssueActivity.class));
			dismiss();
		});

		editLabels.setOnClickListener(v16 -> {

			bmListener.onButtonClicked("showLabels");
			dismiss();
		});

		addRemoveAssignees.setOnClickListener(v17 -> {

			bmListener.onButtonClicked("showAssignees");
			dismiss();
		});

		shareIssue.setOnClickListener(v1 -> {

			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle"));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, tinyDB.getString("singleIssueHtmlUrl"));
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle")));

			dismiss();
		});

		copyIssueUrl.setOnClickListener(v12 -> {

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
				closeIssue.setVisibility(View.GONE);
				closeReopenDivider.setVisibility(View.GONE);
			}
			else if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {
				closeIssue.setText(R.string.closePr);
			}
			closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "closed");
				dismiss();
			});
		}
		else if(tinyDB.getString("issueState").equals("closed")) {
			if(userIsCreator || canPush) {
				if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {
					closeIssue.setText(R.string.reopenPr);
				}
				else {
					closeIssue.setText(R.string.reOpenIssue);
				}
			}
			else {
				closeIssue.setVisibility(View.GONE);
				closeReopenDivider.setVisibility(View.GONE);
			}
			closeIssue.setOnClickListener(closeSingleIssue -> {
				IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "open");
				dismiss();
			});
		}

		subscribeIssue.setOnClickListener(subscribeToIssue -> {

			IssueActions.subscribe(ctx);
			dismiss();
		});

		unsubscribeIssue.setOnClickListener(unsubscribeToIssue -> {

			IssueActions.unsubscribe(ctx);
			dismiss();
		});

		if(new Version(tinyDB.getString("giteaVersion")).less("1.12.0")) {
			subscribeIssue.setVisibility(View.GONE);
			unsubscribeIssue.setVisibility(View.GONE);
		}
		else if(tinyDB.getBoolean("issueSubscribed")) {
			subscribeIssue.setVisibility(View.GONE);
			unsubscribeIssue.setVisibility(View.VISIBLE);
		}
		else {
			subscribeIssue.setVisibility(View.VISIBLE);
			unsubscribeIssue.setVisibility(View.GONE);
		}

		if(archived) {
			subscribeIssue.setVisibility(View.GONE);
			unsubscribeIssue.setVisibility(View.GONE);
			editIssue.setVisibility(View.GONE);
			editLabels.setVisibility(View.GONE);
			closeIssue.setVisibility(View.GONE);
			closeReopenDivider.setVisibility(View.GONE);
			addRemoveAssignees.setVisibility(View.GONE);
			linearLayout.setVisibility(View.GONE);
			bottomSheetSingleIssueBinding.shareDivider.setVisibility(View.GONE);
		}

		return bottomSheetSingleIssueBinding.getRoot();
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
