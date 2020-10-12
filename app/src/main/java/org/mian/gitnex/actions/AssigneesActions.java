package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Issues;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class AssigneesActions {

	public static void getCurrentIssueAssignees(Context ctx, String instanceUrl, String loginUid, String instanceToken, String repoOwner, String repoName, int issueIndex, List<String> currentAssignees) {

		Call<Issues> callSingleIssueLabels = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getIssueByIndex(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex);

		callSingleIssueLabels.enqueue(new Callback<Issues>() {

			@Override
			public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

				if(response.code() == 200) {

					Issues issueAssigneesList = response.body();
					assert issueAssigneesList != null;

					if (issueAssigneesList.getAssignees() != null) {

						if(issueAssigneesList.getAssignees().size() > 0) {

							for(int i = 0; i < issueAssigneesList.getAssignees().size(); i++) {

								currentAssignees.add(issueAssigneesList.getAssignees().get(i).getLogin());
							}
						}
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});
	}

	public static void getRepositoryAssignees(Context ctx, String instanceUrl, String instanceToken, String repoOwner, String repoName, List<Collaborators> assigneesList, Dialog dialogAssignees, AssigneesListAdapter assigneesAdapter, CustomAssigneesSelectionDialogBinding assigneesBinding) {

		TinyDB tinyDB = new TinyDB(ctx);

		Call<List<Collaborators>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getCollaborators(instanceToken, repoOwner, repoName);

		call.enqueue(new Callback<List<Collaborators>>() {

			@Override
			public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull retrofit2.Response<List<Collaborators>> response) {

				assigneesList.clear();
				List<Collaborators> assigneesList_ = response.body();

				assigneesBinding.progressBar.setVisibility(View.GONE);
				assigneesBinding.dialogFrame.setVisibility(View.VISIBLE);

				if (response.code() == 200) {

					assert assigneesList_ != null;

					if(assigneesList_.size() > 0) {

						dialogAssignees.show();

						assigneesList.add(new Collaborators(tinyDB.getString("userFullname"), tinyDB.getString("loginUid"), tinyDB.getString("userAvatar")));
						assigneesList.addAll(assigneesList_);
					}
					else {

						dialogAssignees.dismiss();
						Toasty.warning(ctx, ctx.getResources().getString(R.string.noAssigneesFound));
					}

					assigneesBinding.assigneesRecyclerView.setAdapter(assigneesAdapter);

				}
				else {

					Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});

	}

}
