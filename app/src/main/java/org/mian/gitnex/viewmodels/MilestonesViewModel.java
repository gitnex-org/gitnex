package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Milestones;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class MilestonesViewModel extends ViewModel {

	private static MutableLiveData<List<Milestones>> milestonesList;
	private static final int resultLimit = Constants.resultLimitNewGiteaInstances;

	public LiveData<List<Milestones>> getMilestonesList(String token, String repoOwner, String repoName, String milestoneState, Context ctx) {

		milestonesList = new MutableLiveData<>();
		loadMilestonesList(token, repoOwner, repoName, milestoneState, ctx);

		return milestonesList;
	}

	public static void loadMilestonesList(String token, String repoOwner, String repoName, String milestoneState, Context ctx) {

		Call<List<Milestones>> call = RetrofitClient.getApiInterface(ctx).getMilestones(token, repoOwner, repoName, 1, resultLimit, milestoneState);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

				if(response.isSuccessful()) {
					milestonesList.postValue(response.body());
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void loadMoreMilestones(String token, String repoOwner, String repoName, int page, String milestoneState, Context ctx, MilestonesAdapter adapter) {

		Call<List<Milestones>> call = RetrofitClient.getApiInterface(ctx).getMilestones(token, repoOwner, repoName, page, resultLimit, milestoneState);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

				if(response.isSuccessful()) {

					List<Milestones> list = milestonesList.getValue();
					assert list != null;
					assert response.body() != null;

					if(response.body().size() != 0) {
						list.addAll(response.body());
						adapter.updateList(list);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Milestones>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
