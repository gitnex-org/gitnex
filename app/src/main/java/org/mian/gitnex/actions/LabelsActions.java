package org.mian.gitnex.actions;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomLabelsSelectionDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class LabelsActions {

	public static void getCurrentIssueLabels(
			Context ctx,
			String repoOwner,
			String repoName,
			int issueIndex,
			List<Integer> currentLabelsIds) {

		Call<List<Label>> callSingleIssueLabels =
				RetrofitClient.getApiInterface(ctx)
						.issueGetLabels(repoOwner, repoName, (long) issueIndex);

		callSingleIssueLabels.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull retrofit2.Response<List<Label>> response) {

						if (response.isSuccessful()) {

							List<Label> issueLabelsList = response.body();

							assert issueLabelsList != null;

							if (!issueLabelsList.isEmpty()) {

								for (Label label : issueLabelsList) {

									currentLabelsIds.add(Math.toIntExact(label.getId()));
								}
							}
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public static void getRepositoryLabels(
			Context ctx,
			String repoOwner,
			String repoName,
			List<Label> labelsList,
			MaterialAlertDialogBuilder materialAlertDialogBuilder,
			LabelsListAdapter labelsAdapter,
			CustomLabelsSelectionDialogBinding labelsBinding,
			ProgressBar progressBar) {

		Call<List<Label>> call =
				RetrofitClient.getApiInterface(ctx).issueListLabels(repoOwner, repoName, 1, 100);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull retrofit2.Response<List<Label>> response) {

						labelsList.clear();

						if (response.isSuccessful()) {

							if (response.body() != null) {

								labelsList.addAll(response.body());
							}

							// Load organization labels
							Call<List<Label>> callOrgLabels =
									RetrofitClient.getApiInterface(ctx)
											.orgListLabels(repoOwner, null, null);

							callOrgLabels.enqueue(
									new Callback<>() {

										@Override
										public void onResponse(
												@NonNull Call<List<Label>> call,
												@NonNull retrofit2.Response<List<Label>>
																responseOrg) {

											if (responseOrg.isSuccessful()
													&& responseOrg.body() != null) {

												labelsList.addAll(responseOrg.body());
											}

											if (labelsList.isEmpty()) {

												Toasty.warning(
														ctx,
														ctx.getResources()
																.getString(R.string.noDataFound));
											} else {
												materialAlertDialogBuilder.show();
											}

											labelsBinding.labelsRecyclerView.setAdapter(
													labelsAdapter);
										}

										@Override
										public void onFailure(
												@NonNull Call<List<Label>> call,
												@NonNull Throwable t) {

											Toasty.error(
													ctx,
													ctx.getString(
															R.string.genericServerResponseError));
										}
									});
						} else {

							Toasty.error(ctx, ctx.getResources().getString(R.string.genericError));
						}
						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						if (progressBar != null) {
							progressBar.setVisibility(View.GONE);
						}
						Toasty.error(
								ctx,
								ctx.getResources().getString(R.string.genericServerResponseError));
					}
				});
	}
}
