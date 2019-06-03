package org.mian.gitnex.viewmodels;

import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Collaborators;
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

public class CollaboratorsViewModel extends ViewModel {

    private static MutableLiveData<List<Collaborators>> collaboratorsList;

    public LiveData<List<Collaborators>> getCollaboratorsList(String instanceUrl, String token, String owner, String repo) {

        collaboratorsList = new MutableLiveData<>();
        loadCollaboratorsListList(instanceUrl, token, owner, repo);

        return collaboratorsList;
    }

    private static void loadCollaboratorsListList(String instanceUrl, String token, String owner, String repo) {

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
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
