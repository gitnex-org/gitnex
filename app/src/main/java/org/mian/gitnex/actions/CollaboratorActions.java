package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.Collaborators;
import org.gitnex.tea4j.models.Permission;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddCollaboratorToRepositoryActivity;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.CollaboratorsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class CollaboratorActions {

    public static void deleteCollaborator(final Context context, String userName, RepositoryContext repository) {

        Call<Collaborators> call = RetrofitClient
                .getApiInterface(context)
                .deleteCollaborator(((BaseActivity) context).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), userName);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<Collaborators> call, @NonNull retrofit2.Response<Collaborators> response) {

		        if(response.isSuccessful()) {
			        if(response.code() == 204) {

				        CollaboratorsFragment.refreshCollaborators = true;
				        Toasty.success(context, context.getString(R.string.removeCollaboratorToastText));
				        ((AddCollaboratorToRepositoryActivity) context).finish();
			        }
		        }
		        else if(response.code() == 401) {

			        AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
				        context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.cancelButton),
				        context.getResources().getString(R.string.navLogout));
		        }
		        else if(response.code() == 403) {

			        Toasty.error(context, context.getString(R.string.authorizeError));
		        }
		        else if(response.code() == 404) {

			        Toasty.warning(context, context.getString(R.string.apiNotFound));
		        }
		        else {

			        Toasty.error(context, context.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<Collaborators> call, @NonNull Throwable t) {

		        Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));
	        }
        });

    }

    public static void addCollaborator(final Context context, String permission, String userName, RepositoryContext repository) {

        Permission permissionString = new Permission(permission);

        Call<Permission> call = RetrofitClient
                .getApiInterface(context)
                .addCollaborator(((BaseActivity) context).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), userName, permissionString);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<Permission> call, @NonNull retrofit2.Response<Permission> response) {

		        if(response.isSuccessful()) {
			        if(response.code() == 204) {

				        CollaboratorsFragment.refreshCollaborators = true;
				        Toasty.success(context, context.getString(R.string.addCollaboratorToastText));
				        ((AddCollaboratorToRepositoryActivity) context).finish();
			        }
		        }
		        else if(response.code() == 401) {

			        AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
				        context.getResources().getString(R.string.alertDialogTokenRevokedMessage), context.getResources().getString(R.string.cancelButton),
				        context.getResources().getString(R.string.navLogout));
		        }
		        else if(response.code() == 403) {

			        Toasty.error(context, context.getString(R.string.authorizeError));
		        }
		        else if(response.code() == 404) {

			        Toasty.warning(context, context.getString(R.string.apiNotFound));
		        }
		        else {

			        Toasty.error(context, context.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<Permission> call, @NonNull Throwable t) {

		        Toasty.error(context, context.getResources().getString(R.string.genericServerResponseError));
	        }
        });
    }
}
