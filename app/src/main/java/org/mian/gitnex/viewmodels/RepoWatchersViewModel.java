package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.UserInfo;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepoWatchersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> watchersList;

    public LiveData<List<UserInfo>> getRepoWatchers(String token, String repoOwner, String repoName, Context ctx) {

        watchersList = new MutableLiveData<>();
        loadRepoWatchers(token, repoOwner, repoName, ctx);

        return watchersList;
    }

    private static void loadRepoWatchers(String token, String repoOwner, String repoName, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .getRepoWatchers(token, repoOwner, repoName);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        watchersList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }
}
