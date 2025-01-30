package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class RepoWatchersViewModel extends ViewModel {

	private MutableLiveData<List<User>> watchersList;

	public LiveData<List<User>> getRepoWatchers(
			String repoOwner, String repoName, Context ctx, int page, int resultLimit) {

		watchersList = new MutableLiveData<>();
		loadRepoWatchers(repoOwner, repoName, ctx, page, resultLimit);

		return watchersList;
	}

	private void loadRepoWatchers(
			String repoOwner, String repoName, Context ctx, int page, int resultLimit) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListSubscribers(repoOwner, repoName, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							watchersList.postValue(response.body());
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMore(
			String repoOwner,
			String repoName,
			Context ctx,
			int page,
			int resultLimit,
			UserGridAdapter adapter,
			org.mian.gitnex.databinding.ActivityRepoWatchersBinding activityRepoWatchersBinding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListSubscribers(repoOwner, repoName, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							List<User> list = watchersList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
							activityRepoWatchersBinding.progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
