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
import org.mian.gitnex.actions.TeamActions;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.databinding.CustomPrUpdateStrategyDialogBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
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
						R.string.addNewAccountText,
						(dialog, which) -> {
							Intent intent = new Intent(context, LoginActivity.class);
							intent.putExtra("mode", "new_account");
							context.startActivity(intent);
						})
				.show();
	}

	public static void labelDeleteDialog(
			final Context context,
			final String labelTitle,
			final String labelId,
			String type,
			String orgName,
			RepositoryContext repository) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getString(R.string.deleteGenericTitle, labelTitle))
				.setMessage(R.string.labelDeleteMessage)
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, whichButton) -> {
							Intent intent = new Intent(context, CreateLabelActivity.class);
							intent.putExtra("labelId", labelId);
							intent.putExtra("labelAction", "delete");
							intent.putExtra("type", type);
							intent.putExtra("orgName", orgName);
							intent.putExtra(RepositoryContext.INTENT_EXTRA, repository);
							context.startActivity(intent);
						})
				.setNeutralButton(R.string.cancelButton, null)
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

	public static void addMemberDialog(
			final Context context, final String userNameMain, int teamId) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getResources().getString(R.string.addTeamMember, userNameMain))
				.setMessage(R.string.addTeamMemberMessage)
				.setPositiveButton(
						R.string.addButton,
						(dialog, whichButton) ->
								TeamActions.addTeamMember(context, userNameMain, teamId))
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	public static void removeMemberDialog(
			final Context context, final String userNameMain, int teamId) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getResources().getString(R.string.removeTeamMember, userNameMain))
				.setMessage(R.string.removeTeamMemberMessage)
				.setPositiveButton(
						R.string.removeButton,
						(dialog, whichButton) ->
								TeamActions.removeTeamMember(context, userNameMain, teamId))
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	public static void addRepoDialog(
			final Context context, final String orgName, String repo, int teamId, String teamName) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getResources().getString(R.string.addTeamMember, repo))
				.setMessage(
						context.getResources()
								.getString(R.string.repoAddToTeamMessage, repo, orgName, teamName))
				.setPositiveButton(
						context.getResources().getString(R.string.addButton),
						(dialog, whichButton) ->
								TeamActions.addTeamRepo(context, orgName, teamId, repo))
				.setNeutralButton(context.getResources().getString(R.string.cancelButton), null)
				.show();
	}

	public static void removeRepoDialog(
			final Context context, final String orgName, String repo, int teamId, String teamName) {

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder
				.setTitle(context.getResources().getString(R.string.removeTeamMember, repo))
				.setMessage(
						context.getResources()
								.getString(R.string.repoRemoveTeamMessage, repo, teamName))
				.setPositiveButton(
						context.getResources().getString(R.string.removeButton),
						(dialog, whichButton) ->
								TeamActions.removeTeamRepo(context, orgName, teamId, repo))
				.setNeutralButton(context.getResources().getString(R.string.cancelButton), null)
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
