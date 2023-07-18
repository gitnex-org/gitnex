package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoForksAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class RepositoryForksViewModel extends ViewModel {

	private MutableLiveData<List<Repository>> forksList;
	private int resultLimit;

	public LiveData<List<Repository>> getForksList(String repoOwner, String repoName, Context ctx) {

		forksList = new MutableLiveData<>();
		resultLimit = Constants.getCurrentResultLimit(ctx);
		loadInitialList(repoOwner, repoName, ctx);
		return forksList;
	}

	public void loadInitialList(String repoOwner, String repoName, Context ctx) {

		Call<List<Repository>> call =
				RetrofitClient.getApiInterface(ctx).listForks(repoOwner, repoName, 1, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {

						if (response.isSuccessful()) {
							forksList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMore(
			String repoOwner, String repoName, int page, Context ctx, RepoForksAdapter adapter) {

		Call<List<Repository>> call =
				RetrofitClient.getApiInterface(ctx)
						.listForks(repoOwner, repoName, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {

						if (response.isSuccessful()) {

							List<Repository> list = forksList.getValue();
							assert list != null;
							assert response.body() != null;

							if (response.body().size() != 0) {
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
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
