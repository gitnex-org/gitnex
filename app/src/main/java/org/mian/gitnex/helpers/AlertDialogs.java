package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.TeamActions;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.actions.CollaboratorActions;
import org.mian.gitnex.util.TinyDB;
import androidx.appcompat.app.AlertDialog;

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
            .setNegativeButton(copyNegativeButton, (dialog, which) -> dialog.dismiss())
            .setPositiveButton(copyPositiveButton, (dialog, which) -> {

                final TinyDB tinyDb = new TinyDB(context);
                tinyDb.putBoolean("loggedInMode", false);
                tinyDb.remove("basicAuthPassword");
                tinyDb.putBoolean("basicAuthFlag", false);
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
                dialog.dismiss();

            });

        alertDialogBuilder.create().show();

    }

    public static void labelDeleteDialog(final Context context, final String labelTitle, final String labelId, String title, String message, String positiveButton, String negativeButton) {

        new AlertDialog.Builder(context)
            .setTitle(title + labelTitle)
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(positiveButton, (dialog, whichButton) -> {

                Intent intent = new Intent(context, CreateLabelActivity.class);
                intent.putExtra("labelId", labelId);
                intent.putExtra("labelAction", "delete");
                context.startActivity(intent);

            })
            .setNegativeButton(negativeButton, null).show();

    }

    public static void collaboratorRemoveDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, final String searchKeyword) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> CollaboratorActions.deleteCollaborator(context,  searchKeyword, userNameMain))
                .setNegativeButton(negativeButton, null).show();

    }

    public static void addMemberDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> TeamActions.addTeamMember(context, userNameMain, teamId))
                .setNegativeButton(negativeButton, null).show();

    }

    public static void removeMemberDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, int teamId) {

        new AlertDialog.Builder(context)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, whichButton) -> TeamActions.removeTeamMember(context, userNameMain, teamId))
                .setNegativeButton(negativeButton, null).show();

    }

}
