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

public class RepoStargazersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> stargazersList;

    public LiveData<List<UserInfo>> getRepoStargazers(String token, String repoOwner, String repoName, Context ctx) {

        stargazersList = new MutableLiveData<>();
        loadRepoStargazers(token, repoOwner, repoName, ctx);

        return stargazersList;
    }

    private static void loadRepoStargazers(String token, String repoOwner, String repoName, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .getRepoStargazers(token, repoOwner, repoName);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        stargazersList.postValue(response.body());

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
