package org.mian.gitnex.actions;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddCollaboratorToRepositoryActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Permission;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CollaboratorActions {

    public static void deleteCollaborator(final Context context, final String searchKeyword, String userName) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Call<Collaborators> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .deleteCollaborator(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, userName);

        call.enqueue(new Callback<Collaborators>() {

            @Override
            public void onResponse(@NonNull Call<Collaborators> call, @NonNull retrofit2.Response<Collaborators> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        Toasty.success(context, context.getString(R.string.removeCollaboratorToastText));
                        ((AddCollaboratorToRepositoryActivity)context).finish();
                        //Log.i("addCollaboratorSearch", addCollaboratorSearch.getText().toString());
                        //tinyDb.putBoolean("updateDataSet", true);
                        //AddCollaboratorToRepositoryActivity usersSearchData = new AddCollaboratorToRepositoryActivity();
                        //usersSearchData.loadUserSearchList(instanceUrl, instanceToken, searchKeyword, context);

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

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
                Log.e("onFailure", t.toString());
            }
        });

    }

    public static void addCollaborator(final Context context, String permission, String userName) {

        final TinyDB tinyDb = new TinyDB(context);
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        Permission permissionString = new Permission(permission);
        Call<Permission> call;

        call = RetrofitClient
                .getInstance(instanceUrl, context)
                .getApiInterface()
                .addCollaborator(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, userName, permissionString);

        call.enqueue(new Callback<Permission>() {

            @Override
            public void onResponse(@NonNull Call<Permission> call, @NonNull retrofit2.Response<Permission> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 204) {

                        Toasty.success(context, context.getString(R.string.addCollaboratorToastText));
                        ((AddCollaboratorToRepositoryActivity)context).finish();
                        //AddCollaboratorToRepositoryActivity usersSearchData = new AddCollaboratorToRepositoryActivity();
                        //usersSearchData.loadUserSearchList(instanceUrl, instanceToken, searchKeyword, context);

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(context, context.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            context.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            context.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

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
                Log.e("onFailure", t.toString());
            }

        });

    }

	public static ActionResult<List<Collaborators>> getCollaborators(Context context) {

		ActionResult<List<Collaborators>> actionResult = new ActionResult<>();
		TinyDB tinyDb = new TinyDB(context);

		String instanceUrl = tinyDb.getString("instanceUrl");
		String loginUid = tinyDb.getString("loginUid");
		String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		String repoOwner = parts[0];
		String repoName = parts[1];

		Call<List<Collaborators>> call = RetrofitClient
			.getInstance(instanceUrl, context)
			.getApiInterface()
			.getCollaborators(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName);

		call.enqueue(new Callback<List<Collaborators>>() {

			@Override
			public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

				if (response.isSuccessful()) {

					assert response.body() != null;
					actionResult.finish(ActionResult.Status.SUCCESS, response.body());
				}
				else {

					actionResult.finish(ActionResult.Status.FAILED);
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {

				actionResult.finish(ActionResult.Status.FAILED);
			}
		});

		return actionResult;

	}

}
