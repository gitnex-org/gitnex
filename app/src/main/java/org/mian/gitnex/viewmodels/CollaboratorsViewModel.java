package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Collaborators;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CollaboratorsViewModel extends ViewModel {

    private static MutableLiveData<List<Collaborators>> collaboratorsList;

    public LiveData<List<Collaborators>> getCollaboratorsList(String token, String owner, String repo, Context ctx) {

        collaboratorsList = new MutableLiveData<>();
        loadCollaboratorsListList(token, owner, repo, ctx);

        return collaboratorsList;
    }

    private static void loadCollaboratorsListList(String token, String owner, String repo, Context ctx) {

        Call<List<Collaborators>> call = RetrofitClient
                .getApiInterface(ctx)
                .getCollaborators(token, owner, repo);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

                if (response.isSuccessful()) {
                    collaboratorsList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
