package org.mian.gitnex.viewmodels;

import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.UserInfo;
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

public class ProfileFollowersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> followersList;

    public LiveData<List<UserInfo>> getFollowersList(String instanceUrl, String token) {

        followersList = new MutableLiveData<>();
        loadFollowersList(instanceUrl, token);

        return followersList;
    }

    public static void loadFollowersList(String instanceUrl, String token) {

        Call<List<UserInfo>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getFollowers(token);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if (response.isSuccessful()) {
                    followersList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
