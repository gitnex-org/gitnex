package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Branches;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class BranchesViewModel extends ViewModel {

    private static MutableLiveData<List<Branches>> branchesList;

    public LiveData<List<Branches>> getBranchesList(String token, String owner, String repo, Context ctx) {

        branchesList = new MutableLiveData<>();
        loadBranchesList(token, owner, repo, ctx);

        return branchesList;
    }

    public static void loadBranchesList(String token, String owner, String repo, Context ctx) {

        Call<List<Branches>> call = RetrofitClient
                .getApiInterface(ctx)
                .getBranches(token, owner, repo);

        call.enqueue(new Callback<List<Branches>>() {

            @Override
            public void onResponse(@NonNull Call<List<Branches>> call, @NonNull Response<List<Branches>> response) {

                if (response.isSuccessful()) {
                    branchesList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
