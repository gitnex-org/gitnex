package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepositoriesListViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> reposList;

    public LiveData<List<UserRepositories>> getUserRepositories(String token, Context ctx, int page, int limit) {

        //if (reposList == null) {
            reposList = new MutableLiveData<>();
            loadReposList(token, ctx, page, limit);
        //}

        return reposList;
    }

    public static void loadReposList(String token, Context ctx, int page, int limit) {

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

    }

}
