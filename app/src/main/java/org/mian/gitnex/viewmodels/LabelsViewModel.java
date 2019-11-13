package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Labels;
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

public class LabelsViewModel extends ViewModel {

    private static MutableLiveData<List<Labels>> labelsList;

    public LiveData<List<Labels>> getLabelsList(String instanceUrl, String token, String owner, String repo, Context ctx) {

        labelsList = new MutableLiveData<>();
        loadLabelsList(instanceUrl, token, owner, repo, ctx);

        return labelsList;
    }

    public static void loadLabelsList(String instanceUrl, String token, String owner, String repo, Context ctx) {

        Call<List<Labels>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getlabels(token, owner, repo);

        call.enqueue(new Callback<List<Labels>>() {

            @Override
            public void onResponse(@NonNull Call<List<Labels>> call, @NonNull Response<List<Labels>> response) {

                if(response.isSuccessful()) {
                    labelsList.postValue(response.body());
                }
                else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Labels>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
