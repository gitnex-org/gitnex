package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class LabelsViewModel extends ViewModel {

	private static MutableLiveData<List<Label>> orgLabelsList;

	public LiveData<List<Label>> getLabelsList(
			String owner,
			String repo,
			String type,
			Context ctx,
			FragmentLabelsBinding fragmentLabelsBinding,
			int page,
			int resultLimit) {

		orgLabelsList = new MutableLiveData<>();
		loadLabelsList(owner, repo, type, ctx, fragmentLabelsBinding, page, resultLimit);

		return orgLabelsList;
	}

	public static void loadLabelsList(
			String owner,
			String repo,
			String type,
			Context ctx,
			FragmentLabelsBinding fragmentLabelsBinding,
			int page,
			int resultLimit) {

		Call<List<Label>> call;
		if (type.equalsIgnoreCase("repo")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueListLabels(owner, repo, page, resultLimit);
		} else {
			call = RetrofitClient.getApiInterface(ctx).orgListLabels(owner, page, resultLimit);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull Response<List<Label>> response) {

						if (response.isSuccessful()) {

							orgLabelsList.postValue(response.body());
						} else {

							fragmentLabelsBinding.progressBar.setVisibility(View.GONE);
							fragmentLabelsBinding.noData.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMore(
			String owner,
			String repo,
			String type,
			Context ctx,
			FragmentLabelsBinding fragmentLabelsBinding,
			int page,
			int resultLimit,
			LabelsAdapter adapter) {

		Call<List<Label>> call;
		if (type.equalsIgnoreCase("repo")) {
			call =
					RetrofitClient.getApiInterface(ctx)
							.issueListLabels(owner, repo, page, resultLimit);
		} else {
			call = RetrofitClient.getApiInterface(ctx).orgListLabels(owner, page, resultLimit);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull Response<List<Label>> response) {

						if (response.isSuccessful()) {

							List<Label> list = orgLabelsList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
