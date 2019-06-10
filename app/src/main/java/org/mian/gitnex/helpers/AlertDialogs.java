package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import org.mian.gitnex.R;
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

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.confirmDialog);

        alertDialogBuilder
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setIcon(R.drawable.ic_warning)
                .setNegativeButton(copyNegativeButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(copyPositiveButton, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final TinyDB tinyDb = new TinyDB(context);
                        tinyDb.putBoolean("loggedInMode", false);
                        Intent intent = new Intent(context, LoginActivity.class);
                        context.startActivity(intent);
                        dialog.dismiss();

                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    public static void labelDeleteDialog(final Context context, final String labelTitle, final String labelId, String title, String message, String positiveButton, String negativeButton) {

        new AlertDialog.Builder(context, R.style.confirmDialog)
            .setTitle(title + labelTitle)
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    Intent intent = new Intent(context, CreateLabelActivity.class);
                    intent.putExtra("labelId", labelId);
                    intent.putExtra("labelAction", "delete");
                    context.startActivity(intent);

                }})
            .setNegativeButton(negativeButton, null).show();

    }

    public static void collaboratorRemoveDialog(final Context context, final String userNameMain, String title, String message, String positiveButton, String negativeButton, final String searchKeyword) {

        new AlertDialog.Builder(context, R.style.confirmDialog)
                .setTitle(title + userNameMain)
                .setMessage(message)
                .setIcon(R.drawable.ic_warning)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        CollaboratorActions.deleteCollaborator(context,  searchKeyword, userNameMain);

                    }})
                .setNegativeButton(negativeButton, null).show();

    }

}
