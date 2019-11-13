package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Milestones;
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

public class MilestonesViewModel extends ViewModel {

    private static MutableLiveData<List<Milestones>> milestonesList;

    public LiveData<List<Milestones>> getMilestonesList(String instanceUrl, String token, String owner, String repo, String msState, Context ctx) {

        milestonesList = new MutableLiveData<>();
        loadMilestonesList(instanceUrl, token, owner, repo, msState, ctx);

        return milestonesList;
    }

    public static void loadMilestonesList(String instanceUrl, String token, String owner, String repo, String msState, Context ctx) {

        Call<List<Milestones>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getMilestones(token, owner, repo, msState);

        call.enqueue(new Callback<List<Milestones>>() {

            @Override
            public void onResponse(@NonNull Call<List<Milestones>> call, @NonNull Response<List<Milestones>> response) {

                if(response.isSuccessful()) {
                    milestonesList.postValue(response.body());
                }
                else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Milestones>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
