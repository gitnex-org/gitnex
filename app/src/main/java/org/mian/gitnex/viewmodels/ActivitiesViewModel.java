package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Activity;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ActivitiesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentActivitiesBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ActivitiesViewModel extends ViewModel {

	private MutableLiveData<List<Activity>> activityList;
	private int resultLimit;

	public void setResultLimit(int resultLimit) {
		this.resultLimit = resultLimit;
	}

	public LiveData<List<Activity>> getActivitiesList(
			String username, Context ctx, FragmentActivitiesBinding binding) {

		activityList = new MutableLiveData<>();
		loadActivityList(username, ctx, binding);
		return activityList;
	}

	public void loadActivityList(String username, Context ctx, FragmentActivitiesBinding binding) {

		Call<List<Activity>> call =
				RetrofitClient.getApiInterface(ctx)
						.userListActivityFeeds(username, false, null, 1, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Activity>> call,
							@NonNull Response<List<Activity>> response) {

						if (response.isSuccessful()) {
							activityList.postValue(response.body());
						} else {
							binding.progressBar.setVisibility(View.GONE);
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Activity>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMoreActivities(
			String username,
			int page,
			Context ctx,
			ActivitiesAdapter adapter,
			FragmentActivitiesBinding binding) {

		Call<List<Activity>> call =
				RetrofitClient.getApiInterface(ctx)
						.userListActivityFeeds(username, false, null, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Activity>> call,
							@NonNull Response<List<Activity>> response) {

						if (response.isSuccessful()) {

							List<Activity> list = activityList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {
							binding.progressBar.setVisibility(View.GONE);
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Activity>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
