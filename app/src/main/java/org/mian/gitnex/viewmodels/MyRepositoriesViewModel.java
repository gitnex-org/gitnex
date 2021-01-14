package org.mian.gitnex.viewmodels;

import android.content.Context;
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
 * Author M M Arif
 */

public class MyRepositoriesViewModel extends ViewModel {

    private static MutableLiveData<List<UserRepositories>> myReposList;

    public LiveData<List<UserRepositories>> getCurrentUserRepositories(String token, String username, Context ctx, int page, int limit) {

        //if (myReposList == null) {
        myReposList = new MutableLiveData<>();
        loadMyReposList(token, username, ctx, page, limit);
        //}

        return myReposList;
    }

    public static void loadMyReposList(String token, String username, Context ctx, int page, int limit) {

        Call<List<UserRepositories>> call = RetrofitClient
                .getApiInterface(ctx)
                .getCurrentUserRepositories(token, username, page, limit);

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
