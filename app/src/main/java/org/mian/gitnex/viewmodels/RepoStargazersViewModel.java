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
import org.mian.gitnex.databinding.ActivityRepoStargazersBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class RepoStargazersViewModel extends ViewModel {

	private MutableLiveData<List<User>> stargazersList;

	public LiveData<List<User>> getRepoStargazers(
			String repoOwner, String repoName, Context ctx, int page, int resultLimit) {

		stargazersList = new MutableLiveData<>();
		loadRepoStargazers(repoOwner, repoName, ctx, page, resultLimit);

		return stargazersList;
	}

	private void loadRepoStargazers(
			String repoOwner, String repoName, Context ctx, int page, int resultLimit) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListStargazers(repoOwner, repoName, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							stargazersList.postValue(response.body());
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
			ActivityRepoStargazersBinding activityRepoStargazersBinding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListStargazers(repoOwner, repoName, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {
						assert response.body() != null;
						if (response.isSuccessful()) {
							List<User> list = stargazersList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
							activityRepoStargazersBinding.progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
