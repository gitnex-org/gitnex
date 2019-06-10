package org.mian.gitnex.viewmodels;

import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Teams;
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

public class TeamsByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<Teams>> teamsList;

    public LiveData<List<Teams>> getTeamsByOrg(String instanceUrl, String token, String orgName) {

        teamsList = new MutableLiveData<>();
        loadTeamsByOrgList(instanceUrl, token, orgName);

        return teamsList;
    }

    public static void loadTeamsByOrgList(String instanceUrl, String token, String orgName) {

        Call<List<Teams>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getTeamsByOrg(token, orgName);

        call.enqueue(new Callback<List<Teams>>() {

            @Override
            public void onResponse(@NonNull Call<List<Teams>> call, @NonNull Response<List<Teams>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        teamsList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Teams>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
