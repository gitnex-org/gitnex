package org.mian.gitnex.viewmodels;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Files;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class FilesViewModel extends ViewModel {

    private static MutableLiveData<List<Files>> filesList;

    public LiveData<List<Files>> getFilesList(String instanceUrl, String token, String owner, String repo) {

        filesList = new MutableLiveData<>();
        loadFilesList(instanceUrl, token, owner, repo);

        return filesList;
    }

    public static void loadFilesList(String instanceUrl, String token, String owner, String repo) {

        Call<List<Files>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getFiles(token, owner, repo);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                if (response.isSuccessful()) {
                    filesList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Files>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
