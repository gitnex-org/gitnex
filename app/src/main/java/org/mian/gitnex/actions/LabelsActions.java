package org.mian.gitnex.actions;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.Labels;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class LabelsActions {

	public static void getCurrentIssueLabels(Context ctx, String repoOwner, String repoName, int issueIndex, List<Integer> currentLabelsIds) {

		Call<List<Labels>> callSingleIssueLabels = RetrofitClient
			.getApiInterface(ctx)
			.getIssueLabels(((BaseActivity) ctx).getAccount().getAuthorization(), repoOwner, repoName, issueIndex);

		callSingleIssueLabels.enqueue(new Callback<List<Labels>>() {

			@Override
			public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

				if(response.code() == 200) {

					List<Labels> issueLabelsList = response.body();

					assert issueLabelsList != null;

					if(issueLabelsList.size() > 0) {

						for (int i = 0; i < issueLabelsList.size(); i++) {

							currentLabelsIds.add(issueLabelsList.get(i).getId());
						}
					}

				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});
	}

	public static void getRepositoryLabels(Context ctx, String repoOwner, String repoName, List<Labels> labelsList, Dialog dialogLabels, LabelsListAdapter labelsAdapter, CustomLabelsSelectionDialogBinding labelsBinding) {

		Call<List<Labels>> call = RetrofitClient
			.getApiInterface(ctx)
			.getLabels(((BaseActivity) ctx).getAccount().getAuthorization(), repoOwner, repoName);

		call.enqueue(new Callback<List<Labels>>() {

			@Override
			public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> response) {

				labelsList.clear();

				if (response.code() == 200) {

					if(response.body() != null) {

						labelsList.addAll(response.body());
					}

					// Load organization labels
					Call<List<Labels>> callOrgLabels = RetrofitClient
						.getApiInterface(ctx)
						.getOrganizationLabels(((BaseActivity) ctx).getAccount().getAuthorization(), repoOwner);

					callOrgLabels.enqueue(new Callback<List<Labels>>() {

						@Override
						public void onResponse(@NonNull Call<List<Labels>> call, @NonNull retrofit2.Response<List<Labels>> responseOrg) {

							labelsBinding.progressBar.setVisibility(View.GONE);
							labelsBinding.dialogFrame.setVisibility(View.VISIBLE);

							if(responseOrg.body() != null) {

								labelsList.addAll(responseOrg.body());
							}

							if(labelsList.isEmpty()) {

								dialogLabels.dismiss();
								Toasty.warning(ctx, ctx.getResources().getString(R.string.noLabelsFound));

							}

							labelsBinding.labelsRecyclerView.setAdapter(labelsAdapter);
						}

						@Override public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {}

					});

				}
				else {

					Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getResources().getString(R.string.genericServerResponseError));
			}
		});

	}

}
