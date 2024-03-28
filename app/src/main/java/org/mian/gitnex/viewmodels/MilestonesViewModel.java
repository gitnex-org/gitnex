package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class MilestonesViewModel extends ViewModel {

	private MutableLiveData<List<Milestone>> milestonesList;
	private int resultLimit;

	public LiveData<List<Milestone>> getMilestonesList(
			String repoOwner, String repoName, String milestoneState, Context ctx) {

		milestonesList = new MutableLiveData<>();
		loadMilestonesList(repoOwner, repoName, milestoneState, ctx);
		resultLimit = Constants.getCurrentResultLimit(ctx);
		return milestonesList;
	}

	public void loadMilestonesList(
			String repoOwner, String repoName, String milestoneState, Context ctx) {

		Call<List<Milestone>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetMilestonesList(
								repoOwner, repoName, milestoneState, null, 1, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestone>> call,
							@NonNull Response<List<Milestone>> response) {

						if (response.isSuccessful()) {
							milestonesList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMoreMilestones(
			String repoOwner,
			String repoName,
			int page,
			String milestoneState,
			Context ctx,
			MilestonesAdapter adapter) {

		Call<List<Milestone>> call =
				RetrofitClient.getApiInterface(ctx)
						.issueGetMilestonesList(
								repoOwner, repoName, milestoneState, null, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Milestone>> call,
							@NonNull Response<List<Milestone>> response) {

						if (response.isSuccessful()) {

							List<Milestone> list = milestonesList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Milestone>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
