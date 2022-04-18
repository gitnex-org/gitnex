package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AssigneesListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomAssigneesSelectionDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class AssigneesActions {

	public static void getCurrentIssueAssignees(Context ctx, String repoOwner, String repoName, int issueIndex, List<String> currentAssignees) {

		Call<Issue> callSingleIssueLabels = RetrofitClient
			.getApiInterface(ctx)
			.issueGetIssue(repoOwner, repoName, (long) issueIndex);

		callSingleIssueLabels.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<Issue> call, @NonNull retrofit2.Response<Issue> response) {

				if(response.code() == 200) {

					Issue issueAssigneesList = response.body();
					assert issueAssigneesList != null;

					if(issueAssigneesList.getAssignees() != null) {

						if(issueAssigneesList.getAssignees().size() > 0) {

							for(int i = 0; i < issueAssigneesList.getAssignees().size(); i++) {

								currentAssignees.add(issueAssigneesList.getAssignees().get(i).getLogin());
							}
						}
					}
				}
				else {

					Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void getRepositoryAssignees(Context ctx, String repoOwner, String repoName, List<User> assigneesList, Dialog dialogAssignees, AssigneesListAdapter assigneesAdapter, CustomAssigneesSelectionDialogBinding assigneesBinding) {

		Call<List<User>> call = RetrofitClient
			.getApiInterface(ctx)
			.repoGetAssignees(repoOwner, repoName);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<User>> call, @NonNull retrofit2.Response<List<User>> response) {

				assigneesList.clear();
				List<User> assigneesList_ = response.body();

				assigneesBinding.progressBar.setVisibility(View.GONE);
				assigneesBinding.dialogFrame.setVisibility(View.VISIBLE);

				if(response.code() == 200) {

					assert assigneesList_ != null;

					if(assigneesList_.size() > 0) {

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
			public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}
}
