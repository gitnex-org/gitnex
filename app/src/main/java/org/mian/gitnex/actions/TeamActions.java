package org.mian.gitnex.actions;

import android.content.Context;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddNewTeamMemberActivity;
import org.mian.gitnex.activities.AddNewTeamRepoActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.fragments.OrganizationTeamInfoMembersFragment;
import org.mian.gitnex.fragments.OrganizationTeamInfoReposFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class TeamActions {

	public static void removeTeamMember(final Context context, String userName, int teamId) {

		Call<Void> call =
				RetrofitClient.getApiInterface(context)
						.orgRemoveTeamMember((long) teamId, userName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 204) {

								OrganizationTeamInfoMembersFragment.refreshMembers = true;
								Toasty.success(
										context, context.getString(R.string.memberRemovedMessage));
								((AddNewTeamMemberActivity) context).finish();
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

	public static void addTeamMember(final Context context, String userName, int teamId) {

		Call<Void> call =
				RetrofitClient.getApiInterface(context).orgAddTeamMember((long) teamId, userName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 204) {

								OrganizationTeamInfoMembersFragment.refreshMembers = true;
								Toasty.success(
										context, context.getString(R.string.memberAddedMessage));
								((AddNewTeamMemberActivity) context).finish();
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

	public static void removeTeamRepo(
			final Context context, String orgName, int teamId, String repo) {

		Call<Void> call =
				RetrofitClient.getApiInterface(context)
						.orgRemoveTeamRepository((long) teamId, orgName, repo);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 204) {

								OrganizationTeamInfoReposFragment.repoAdded = true;
								Toasty.success(
										context, context.getString(R.string.repoRemovedMessage));
								((AddNewTeamRepoActivity) context).finish();
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

	public static void addTeamRepo(final Context context, String orgName, int teamId, String repo) {

		Call<Void> call =
				RetrofitClient.getApiInterface(context)
						.orgAddTeamRepository((long) teamId, orgName, repo);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.isSuccessful()) {

							if (response.code() == 204) {

								OrganizationTeamInfoReposFragment.repoAdded = true;
								Toasty.success(
										context, context.getString(R.string.repoAddedMessage));
								((AddNewTeamRepoActivity) context).finish();
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
