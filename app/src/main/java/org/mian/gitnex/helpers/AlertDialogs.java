package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.databinding.CustomPrUpdateStrategyDialogBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class AlertDialogs {

	private static MaterialAlertDialogBuilder materialAlertDialogBuilder;

	public static void authorizationTokenRevokedDialog(final Context context) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(R.string.alertDialogTokenRevokedTitle)
				.setMessage(R.string.alertDialogTokenRevokedMessage)
				.setCancelable(true)
				.setNeutralButton(R.string.cancelButton, null)
				.setPositiveButton(
						R.string.update_account,
						(dialog, which) -> {
							Intent intent = new Intent(context, LoginActivity.class);
							intent.putExtra("mode", "update_account");
							context.startActivity(intent);
						})
				.show();
	}

	public static void collaboratorRemoveDialog(
			final Context context, final String userNameMain, RepositoryContext repository) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getString(R.string.removeCollaboratorDialogTitle, userNameMain))
				.setMessage(R.string.removeCollaboratorMessage)
				.setPositiveButton(
						R.string.removeButton,
						(dialog, whichButton) ->
								CollaboratorActions.deleteCollaborator(
										context, userNameMain, repository))
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	public static void selectPullUpdateStrategy(
			Context context, String repoOwner, String repo, String issueNumber) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		CustomPrUpdateStrategyDialogBinding binding =
				CustomPrUpdateStrategyDialogBinding.inflate(LayoutInflater.from(context));

		View view = binding.getRoot();
		materialAlertDialogBuilder.setView(view);

		AlertDialog dialog = materialAlertDialogBuilder.show();

		binding.updatePullMerge.setOnClickListener(
				(v) -> {
					PullRequestActions.updatePr(context, repoOwner, repo, issueNumber, false);
					dialog.dismiss();
				});
		binding.updatePullRebase.setOnClickListener(
				(v) -> {
					PullRequestActions.updatePr(context, repoOwner, repo, issueNumber, true);
					dialog.dismiss();
				});
	}
}
