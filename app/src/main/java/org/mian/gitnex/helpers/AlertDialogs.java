package org.mian.gitnex.helpers;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.actions.PullRequestActions;
import org.mian.gitnex.actions.TeamActions;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
 */

public class AlertDialogs {

    public static void authorizationTokenRevokedDialog(final Context context) {
		new AlertDialog.Builder(context)
	        .setTitle(R.string.alertDialogTokenRevokedTitle)
            .setMessage(R.string.alertDialogTokenRevokedMessage)
            .setCancelable(true)
            .setIcon(R.drawable.ic_warning)
            .setNeutralButton(R.string.cancelButton, null)
            .setPositiveButton(R.string.navLogout, (dialog, which) -> AppUtil.logout(context))
	        .show();
    }

	public static void labelDeleteDialog(final Context context, final String labelTitle, final String labelId, String type, String orgName,
	    RepositoryContext repository) {

        new AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.deleteGenericTitle, labelTitle))
            .setMessage(R.string.labelDeleteMessage)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(R.string.menuDeleteText, (dialog, whichButton) -> {

                Intent intent = new Intent(context, CreateLabelActivity.class);
                intent.putExtra("labelId", labelId);
                intent.putExtra("labelAction", "delete");
	            intent.putExtra("type", type);
	            intent.putExtra("orgName", orgName);
	            intent.putExtra(RepositoryContext.INTENT_EXTRA, repository);
                context.startActivity(intent);

            })
            .setNeutralButton(R.string.cancelButton, null).show();

    }

    public static void collaboratorRemoveDialog(final Context context, final String userNameMain, RepositoryContext repository) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.removeCollaboratorDialogTitle, userNameMain))
                .setMessage(R.string.removeCollaboratorMessage)
                .setPositiveButton(R.string.removeButton, (dialog, whichButton) -> CollaboratorActions.deleteCollaborator(context, userNameMain, repository))
                .setNeutralButton(R.string.cancelButton, null).show();

    }

    public static void addMemberDialog(final Context context, final String userNameMain, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.addTeamMember, userNameMain))
                .setMessage(R.string.addTeamMemberMessage)
                .setPositiveButton(R.string.addButton, (dialog, whichButton) -> TeamActions.addTeamMember(context, userNameMain, teamId))
                .setNeutralButton(R.string.cancelButton, null).show();

    }

    public static void removeMemberDialog(final Context context, final String userNameMain, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.removeTeamMember, userNameMain))
                .setMessage(R.string.removeTeamMemberMessage)
                .setPositiveButton(R.string.removeButton, (dialog, whichButton) -> TeamActions.removeTeamMember(context, userNameMain, teamId))
                .setNeutralButton(R.string.cancelButton, null).show();

    }

	public static void addRepoDialog(final Context context, final String orgName, String repo, int teamId, String teamName) {

		new AlertDialog.Builder(context)
			.setTitle(context.getResources().getString(R.string.addTeamMember) + repo)
			.setMessage(context.getResources().getString(R.string.repoAddToTeamMessage, repo, orgName, teamName))
			.setPositiveButton(context.getResources().getString(R.string.addButton), (dialog, whichButton) -> TeamActions.addTeamRepo(context, orgName, teamId, repo))
			.setNeutralButton(context.getResources().getString(R.string.cancelButton), null).show();

	}

	public static void removeRepoDialog(final Context context, final String orgName, String repo, int teamId, String teamName) {

		new AlertDialog.Builder(context)
			.setTitle(context.getResources().getString(R.string.removeTeamMember) + repo)
			.setMessage(context.getResources().getString(R.string.repoRemoveTeamMessage, repo, teamName))
			.setPositiveButton(context.getResources().getString(R.string.removeButton), (dialog, whichButton) -> TeamActions.removeTeamRepo(context, orgName, teamId, repo))
			.setNeutralButton(context.getResources().getString(R.string.cancelButton), null).show();

	}

    public static void selectPullUpdateStrategy(Context context, String repoOwner, String repo, String issueNumber) {
    	Dialog dialog = new Dialog(context, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

	    if (dialog.getWindow() != null) {
		    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	    }

    	dialog.setContentView(R.layout.custom_pr_update_strategy_dialog);
    	Button mergeBtn = dialog.findViewById(R.id.updatePullMerge);
    	Button rebaseBtn = dialog.findViewById(R.id.updatePullRebase);
    	Button cancelBtn = dialog.findViewById(R.id.cancelPullUpdate);
    	mergeBtn.setOnClickListener((v) -> {
    		PullRequestActions.updatePr(context, repoOwner, repo, issueNumber, false);
    		dialog.dismiss();
	    });
	    rebaseBtn.setOnClickListener((v) -> {
		    PullRequestActions.updatePr(context, repoOwner, repo, issueNumber, true);
		    dialog.dismiss();
	    });
	    cancelBtn.setOnClickListener((v) -> dialog.dismiss());
	    dialog.show();
    }

}
