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

public class MembersByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> membersList;

    public LiveData<List<UserInfo>> getMembersList(String instanceUrl, String token, String owner, Context ctx) {

        membersList = new MutableLiveData<>();
        loadMembersList(instanceUrl, token, owner, ctx);

        return membersList;
    }

    private static void loadMembersList(String instanceUrl, String token, String owner, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getMembersByOrg(token, owner);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if (response.isSuccessful()) {
                    membersList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
