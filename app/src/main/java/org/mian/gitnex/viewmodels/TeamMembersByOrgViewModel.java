package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class TeamMembersByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> teamMembersList;

    public LiveData<List<UserInfo>> getMembersByOrgList(String token, int teamId, Context ctx) {

        teamMembersList = new MutableLiveData<>();
        loadMembersByOrgList(token, teamId, ctx);

        return teamMembersList;
    }

    private static void loadMembersByOrgList(String token, int teamId, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .getTeamMembersByOrg(token, teamId);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if (response.isSuccessful()) {
                    teamMembersList.postValue(response.body());
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
