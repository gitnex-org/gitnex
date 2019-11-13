package org.mian.gitnex.viewmodels;

import android.content.Context;
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

public class ProfileFollowingViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> followingList;

    public LiveData<List<UserInfo>> getFollowingList(String instanceUrl, String token, Context ctx) {

        followingList = new MutableLiveData<>();
        loadFollowingList(instanceUrl, token, ctx);

        return followingList;
    }

    public static void loadFollowingList(String instanceUrl, String token, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getFollowing(token);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if (response.isSuccessful()) {
                    followingList.postValue(response.body());
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
