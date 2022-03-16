package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Releases;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> reposList;

    public LiveData<List<UserRepositories>> getRepositories(String token, int page, int resultLimit, Context ctx) {

    	reposList = new MutableLiveData<>();
    	loadReposList(token, page, resultLimit, ctx);

        return reposList;
    }

    public static void loadReposList(String token, int page, int resultLimit, Context ctx) {

        Call<List<UserRepositories>> call = RetrofitClient
                .getApiInterface(ctx)
                .getUserRepositories(token, page, resultLimit);

        call.enqueue(new Callback<List<UserRepositories>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        reposList.postValue(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserRepositories>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

	public static void loadMoreRepos(String token, int page, int resultLimit, Context ctx, ReposListAdapter adapter) {

		Call<List<UserRepositories>> call = RetrofitClient.getApiInterface(ctx).getUserRepositories(token, page, resultLimit);

		call.enqueue(new Callback<List<UserRepositories>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {
					List<UserRepositories> list = reposList.getValue();
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
					Log.i("onResponse", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserRepositories>> call, @NonNull Throwable t) {

				Log.i("onFailure", t.toString());
			}
		});
	}

	/*public static void loadReposList(String token, Context ctx, int page, int limit) {

		Call<List<UserRepositories>> call = RetrofitClient
			.getApiInterface(ctx)
			.getUserRepositories(token, page, limit);

		call.enqueue(new Callback<List<UserRepositories>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {
					if(response.code() == 200) {
						reposList.postValue(response.body());
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserRepositories>> call, Throwable t) {
				Log.i("onFailure", t.toString());
			}

		});
	}*/
}
