package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class LabelsActions {

	public static void getCurrentIssueLabels(Context ctx, String repoOwner, String repoName, int issueIndex, List<Integer> currentLabelsIds) {

		Call<List<Label>> callSingleIssueLabels = RetrofitClient
			.getApiInterface(ctx)
			.issueGetLabels(repoOwner, repoName, (long) issueIndex);

		callSingleIssueLabels.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Label>> call, @NonNull retrofit2.Response<List<Label>> response) {

				if(response.isSuccessful()) {

					List<Label> issueLabelsList = response.body();

					assert issueLabelsList != null;

					if(issueLabelsList.size() > 0) {

						for(int i = 0; i < issueLabelsList.size(); i++) {

							currentLabelsIds.add(Math.toIntExact(issueLabelsList.get(i).getId()));
						}
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void getRepositoryLabels(Context ctx, String repoOwner, String repoName, List<Label> labelsList, Dialog dialogLabels, LabelsListAdapter labelsAdapter, CustomLabelsSelectionDialogBinding labelsBinding) {

		Call<List<Label>> call = RetrofitClient
			.getApiInterface(ctx)
			.issueListLabels(repoOwner, repoName, null, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Label>> call, @NonNull retrofit2.Response<List<Label>> response) {

				labelsList.clear();

				if(response.isSuccessful()) {

					if(response.body() != null) {

						labelsList.addAll(response.body());
					}

					// Load organization labels
					Call<List<Label>> callOrgLabels = RetrofitClient
						.getApiInterface(ctx)
						.orgListLabels(repoOwner, null, null);

					callOrgLabels.enqueue(new Callback<List<Label>>() {

						@Override
						public void onResponse(@NonNull Call<List<Label>> call, @NonNull retrofit2.Response<List<Label>> responseOrg) {

							labelsBinding.progressBar.setVisibility(View.GONE);
							labelsBinding.dialogFrame.setVisibility(View.VISIBLE);

							if(responseOrg.body() != null) {

								labelsList.addAll(responseOrg.body());
							}

							if(labelsList.isEmpty()) {

								dialogLabels.dismiss();
								Toasty.warning(ctx, ctx.getResources().getString(R.string.noDataFound));

							}

							labelsBinding.labelsRecyclerView.setAdapter(labelsAdapter);
						}

						@Override
						public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

							Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
						}

					});

				}
				else {

					Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});
	}
}
