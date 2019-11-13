package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.UserRepositories;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MyRepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> myReposList;

    public LiveData<List<UserRepositories>> getCurrentUserRepositories(String instanceUrl, String token, String username, Context ctx) {

        //if (myReposList == null) {
        myReposList = new MutableLiveData<>();
        loadMyReposList(instanceUrl, token, username, ctx);
        //}

        return myReposList;
    }

    public static void loadMyReposList(String instanceUrl, String token, String username, Context ctx) {

        Call<List<UserRepositories>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getCurrentUserRepositories(token, username);

        call.enqueue(new Callback<List<UserRepositories>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        myReposList.postValue(response.body());

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
