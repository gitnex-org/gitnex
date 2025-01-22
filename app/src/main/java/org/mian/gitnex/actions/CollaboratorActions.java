package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.AddCollaboratorOption;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddCollaboratorToRepositoryActivity;
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

	public static void deleteCollaborator(
			final Context context, String userName, RepositoryContext repository) {

		Call<Void> call =
				RetrofitClient.getApiInterface(context)
						.repoDeleteCollaborator(
								repository.getOwner(), repository.getName(), userName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {
							if (response.code() == 204) {

								CollaboratorsFragment.refreshCollaborators = true;
								Toasty.success(
										context,
										context.getString(R.string.removeCollaboratorToastText));
								((AddCollaboratorToRepositoryActivity) context).finish();
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(context);
						} else if (response.code() == 403) {

							Toasty.error(context, context.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							Toasty.warning(context, context.getString(R.string.apiNotFound));
						} else {

							Toasty.error(context, context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(
								context,
								context.getResources()
										.getString(R.string.genericServerResponseError));
					}
				});
	}

	public static void addCollaborator(
			final Context context,
			String permission,
			String userName,
			RepositoryContext repository) {

		AddCollaboratorOption permissionString = new AddCollaboratorOption();
		permissionString.setPermission(
				AddCollaboratorOption.PermissionEnum.valueOf(permission.toUpperCase()));

		Call<Void> call =
				RetrofitClient.getApiInterface(context)
						.repoAddCollaborator(
								repository.getOwner(),
								repository.getName(),
								userName,
								permissionString);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {
							if (response.code() == 204) {

								CollaboratorsFragment.refreshCollaborators = true;
								Toasty.success(
										context,
										context.getString(R.string.addCollaboratorToastText));
								((AddCollaboratorToRepositoryActivity) context).finish();
							}
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(context);
						} else if (response.code() == 403) {

							Toasty.error(context, context.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							Toasty.warning(context, context.getString(R.string.apiNotFound));
						} else {

							Toasty.error(context, context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(
								context,
								context.getResources()
										.getString(R.string.genericServerResponseError));
					}
				});
	}
}
