package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.FileDiffActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetSingleIssueBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.views.ReactionSpinner;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class BottomSheetSingleIssueFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetSingleIssueBinding bottomSheetSingleIssueBinding = BottomSheetSingleIssueBinding.inflate(inflater, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = TinyDB.getInstance(ctx);

		TextView editIssue = bottomSheetSingleIssueBinding.editIssue;
		TextView editLabels = bottomSheetSingleIssueBinding.editLabels;
		TextView closeIssue = bottomSheetSingleIssueBinding.closeIssue;
		TextView reOpenIssue = bottomSheetSingleIssueBinding.reOpenIssue;
		TextView addRemoveAssignees = bottomSheetSingleIssueBinding.addRemoveAssignees;
		TextView copyIssueUrl = bottomSheetSingleIssueBinding.copyIssueUrl;
		TextView openFilesDiff = bottomSheetSingleIssueBinding.openFilesDiff;
		TextView mergePullRequest = bottomSheetSingleIssueBinding.mergePullRequest;
		TextView deletePullRequestBranch = bottomSheetSingleIssueBinding.deletePrHeadBranch;
		TextView shareIssue = bottomSheetSingleIssueBinding.shareIssue;
		TextView subscribeIssue = bottomSheetSingleIssueBinding.subscribeIssue;
		TextView unsubscribeIssue = bottomSheetSingleIssueBinding.unsubscribeIssue;

		LinearLayout linearLayout = bottomSheetSingleIssueBinding.commentReactionButtons;

		Bundle bundle1 = new Bundle();

		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");

		bundle1.putString("repoOwner", parts[0]);
		bundle1.putString("repoName", parts[1]);
		bundle1.putInt("issueId", Integer.parseInt(tinyDB.getString("issueNumber")));

		ReactionSpinner reactionSpinner = new ReactionSpinner(ctx, bundle1);
		reactionSpinner.setOnInteractedListener(() -> {

			tinyDB.putBoolean("singleIssueUpdate", true);

			bmListener.onButtonClicked("onResume");
			dismiss();

		});

		linearLayout.addView(reactionSpinner);

		if(tinyDB.getString("issueType").equalsIgnoreCase("Pull")) {

			editIssue.setText(R.string.editPrText);
			copyIssueUrl.setText(R.string.copyPrUrlText);
			shareIssue.setText(R.string.sharePr);

			if(tinyDB.getBoolean("prMerged") || tinyDB.getString("repoPrState").equals("closed")) {
				mergePullRequest.setVisibility(View.GONE);
				deletePullRequestBranch.setVisibility(View.VISIBLE);
			}
			else {
				mergePullRequest.setVisibility(View.VISIBLE);
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

			mergePullRequest.setVisibility(View.GONE);
			deletePullRequestBranch.setVisibility(View.GONE);
		}

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

		if(tinyDB.getString("issueType").equalsIgnoreCase("Issue")) {

			if(tinyDB.getString("issueState").equals("open")) { // close issue

				reOpenIssue.setVisibility(View.GONE);
				closeIssue.setVisibility(View.VISIBLE);

				closeIssue.setOnClickListener(closeSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "closed");
					dismiss();

				});

			}
			else if(tinyDB.getString("issueState").equals("closed")) {

				closeIssue.setVisibility(View.GONE);
				reOpenIssue.setVisibility(View.VISIBLE);

				reOpenIssue.setOnClickListener(reOpenSingleIssue -> {

					IssueActions.closeReopenIssue(ctx, Integer.parseInt(tinyDB.getString("issueNumber")), "open");
					dismiss();

				});

			}

		}
		else {

			reOpenIssue.setVisibility(View.GONE);
			closeIssue.setVisibility(View.GONE);

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

		return bottomSheetSingleIssueBinding.getRoot();
	}

	public interface BottomSheetListener {

		void onButtonClicked(String text);
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
