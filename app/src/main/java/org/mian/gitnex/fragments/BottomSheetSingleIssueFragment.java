package org.mian.gitnex.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.activities.AddRemoveAssigneesActivity;
import org.mian.gitnex.activities.AddRemoveLabelsActivity;
import org.mian.gitnex.activities.EditIssueActivity;
import org.mian.gitnex.activities.FileDiffActivity;
import org.mian.gitnex.activities.MergePullRequestActivity;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class BottomSheetSingleIssueFragment extends BottomSheetDialogFragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.bottom_sheet_single_issue, container, false);

		final Context ctx = getContext();
		final TinyDB tinyDB = new TinyDB(ctx);

		TextView editIssue = v.findViewById(R.id.editIssue);
		TextView editLabels = v.findViewById(R.id.editLabels);
		TextView closeIssue = v.findViewById(R.id.closeIssue);
		TextView reOpenIssue = v.findViewById(R.id.reOpenIssue);
		TextView addRemoveAssignees = v.findViewById(R.id.addRemoveAssignees);
		TextView copyIssueUrl = v.findViewById(R.id.copyIssueUrl);
		TextView openFilesDiff = v.findViewById(R.id.openFilesDiff);
		TextView mergePullRequest = v.findViewById(R.id.mergePullRequest);
		TextView shareIssue = v.findViewById(R.id.shareIssue);
		TextView subscribeIssue = v.findViewById(R.id.subscribeIssue);
		TextView unsubscribeIssue = v.findViewById(R.id.unsubscribeIssue);

		if(tinyDB.getString("issueType").equals("pr")) {

			editIssue.setText(R.string.editPrText);
			copyIssueUrl.setText(R.string.copyPrUrlText);
			shareIssue.setText(R.string.sharePr);

			if(tinyDB.getBoolean("prMerged") || tinyDB.getString("repoPrState").equals("closed")) {
				mergePullRequest.setVisibility(View.GONE);
			}
			else {
				mergePullRequest.setVisibility(View.VISIBLE);
			}

			if(tinyDB.getString("repoType").equals("public")) {
				openFilesDiff.setVisibility(View.VISIBLE);
			}
			else {
				openFilesDiff.setVisibility(View.GONE);
			}

		}
		else {

			mergePullRequest.setVisibility(View.GONE);

		}

		mergePullRequest.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(ctx, MergePullRequestActivity.class));
				dismiss();

			}
		});

		openFilesDiff.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(ctx, FileDiffActivity.class));
				dismiss();

			}
		});

		editIssue.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(ctx, EditIssueActivity.class));
				dismiss();

			}
		});

		editLabels.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(ctx, AddRemoveLabelsActivity.class));
				dismiss();

			}
		});

		addRemoveAssignees.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(ctx, AddRemoveAssigneesActivity.class));
				dismiss();

			}
		});

		shareIssue.setOnClickListener(v1 -> {

			// get url of repo
			String repoFullName = tinyDB.getString("repoFullName");
			String instanceUrlWithProtocol = "https://" + tinyDB.getString("instanceUrlRaw");
			if(!tinyDB.getString("instanceUrlWithProtocol").isEmpty()) {
				instanceUrlWithProtocol = tinyDB.getString("instanceUrlWithProtocol");
			}

			// get issue Url
			String issueUrl = instanceUrlWithProtocol + "/" + repoFullName + "/issues/" + tinyDB.getString("issueNumber");

			// share issue
			Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle"));
			sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, issueUrl);
			startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.hash) + tinyDB.getString("issueNumber") + " " + tinyDB.getString("issueTitle")));

			dismiss();

		});

		copyIssueUrl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// get url of repo
				String repoFullName = tinyDB.getString("repoFullName");
				String instanceUrlWithProtocol = "https://" + tinyDB.getString("instanceUrlRaw");
				if(!tinyDB.getString("instanceUrlWithProtocol").isEmpty()) {
					instanceUrlWithProtocol = tinyDB.getString("instanceUrlWithProtocol");
				}

				// get issue Url
				String issueUrl = instanceUrlWithProtocol + "/" + repoFullName + "/issues/" + tinyDB.getString("issueNumber");

				// copy to clipboard
				ClipboardManager clipboard = (ClipboardManager) Objects.requireNonNull(ctx).getSystemService(android.content.Context.CLIPBOARD_SERVICE);
				ClipData clip = ClipData.newPlainText("issueUrl", issueUrl);
				assert clipboard != null;
				clipboard.setPrimaryClip(clip);

				dismiss();

				Toasty.info(ctx, ctx.getString(R.string.copyIssueUrlToastMsg));

			}
		});

		if(tinyDB.getString("issueType").equals("issue")) {

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

		return v;
	}

}
