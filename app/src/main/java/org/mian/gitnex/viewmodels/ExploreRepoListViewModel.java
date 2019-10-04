package org.mian.gitnex.viewmodels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.UserRepositories;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Template Author M M Arif
 * Author 6543
 */

public class ExploreRepoListViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> reposList;

    public LiveData<List<UserRepositories>> getUserRepositories(String instanceUrl, String token, String searchKeyword) {

        //if (reposList == null) {
            reposList = new MutableLiveData<>();
            loadReposList(instanceUrl, token, searchKeyword);
        //}

        return reposList;
    }


    public static void loadReposList(String instanceUrl, String token, String searchKeyword) {

        int limit = 10;          //page size of results, maximum page size is 50
        String mode = "";        //type of repository to search for. Supported values are "fork", "source", “mirror” and “collaborative”
        String sort = "alpha";   //sort repos by attribute. Supported values are "alpha", "created", "updated", "size", and "id". Default is “alpha”
        String order = "asc";    //sort order, either “asc” (ascending) or “desc” (descending). Default is "asc", ignored if “sort” is not specified.

        Call<List<UserRepositories>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .queryRepos(token, searchKeyword, limit, mode, sort, order);

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
