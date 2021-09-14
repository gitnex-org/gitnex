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
import org.mian.gitnex.activities.LoginActivity;

/**
 * Author M M Arif
 */

public class AlertDialogs {

    public static void authorizationTokenRevokedDialog(final Context context, String title, String message, String copyNegativeButton, String copyPositiveButton) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
	        .setTitle(title)
            .setMessage(message)
            .setCancelable(true)
            .setIcon(R.drawable.ic_warning)
            .setNeutralButton(copyNegativeButton, (dialog, which) -> dialog.dismiss())
            .setPositiveButton(copyPositiveButton, (dialog, which) -> {

                final TinyDB tinyDb = TinyDB.getInstance(context);
                tinyDb.putBoolean("loggedInMode", false);
                tinyDb.remove("basicAuthPassword");
                tinyDb.putBoolean("basicAuthFlag", false);
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                dialog.dismiss();

            });

        alertDialogBuilder.create().show();

    }

    public static void forceLogoutDialog(final Context context, String title, String message, String copyPositiveButton) {

	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context)
		    .setTitle(title)
		    .setMessage(message)
		    .setCancelable(false)
		    .setIcon(R.drawable.ic_info)
		    .setPositiveButton(copyPositiveButton, (dialog, which) -> {

			    final TinyDB tinyDb = TinyDB.getInstance(context);
			    tinyDb.putBoolean("loggedInMode", false);
			    tinyDb.remove("basicAuthPassword");
			    tinyDb.putBoolean("basicAuthFlag", false);

			    Intent intent = new Intent(context, LoginActivity.class);
			    context.startActivity(intent);
			    dialog.dismiss();

		    });

	    alertDialogBuilder.create().show();
    }

    public static void labelDeleteDialog(final Context context, final String labelTitle, final String labelId, String title, String message, String positiveButton, String negativeButton, String type, String orgName) {

        new AlertDialog.Builder(context)
            .setTitle(title + labelTitle)
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(positiveButton, (dialog, whichButton) -> {

                Intent intent = new Intent(context, CreateLabelActivity.class);
                intent.putExtra("labelId", labelId);
                intent.putExtra("labelAction", "delete");
	            intent.putExtra("type", type);
	            intent.putExtra("orgName", orgName);
                context.startActivity(intent);

            })
            .setNeutralButton(negativeButton, null).show();

    }

    public static void collaboratorRemoveDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, final String searchKeyword) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> CollaboratorActions.deleteCollaborator(context,  searchKeyword, userNameMain))
                .setNeutralButton(negativeButton, null).show();

    }

    public static void addMemberDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> TeamActions.addTeamMember(context, userNameMain, teamId))
                .setNeutralButton(negativeButton, null).show();

    }

    public static void removeMemberDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> TeamActions.removeTeamMember(context, userNameMain, teamId))
                .setNeutralButton(negativeButton, null).show();

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
