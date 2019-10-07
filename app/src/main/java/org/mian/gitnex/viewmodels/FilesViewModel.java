package org.mian.gitnex.viewmodels;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Files;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class FilesViewModel extends ViewModel {

    private static MutableLiveData<List<Files>> filesList;
    private static MutableLiveData<List<Files>> filesList2;

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

                Collections.sort(response.body(), new Comparator<Files>() {
                    @Override
                    public int compare(Files byType1, Files byType2) {
                        return byType1.getType().compareTo(byType2.getType());
                    }
                });

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

    public LiveData<List<Files>> getFilesList2(String instanceUrl, String token, String owner, String repo, String filesDir) {

        filesList = new MutableLiveData<>();
        loadFilesList2(instanceUrl, token, owner, repo, filesDir);

        return filesList;
    }

    public static void loadFilesList2(String instanceUrl, String token, String owner, String repo, String filesDir) {

        Call<List<Files>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getDirFiles(token, owner, repo, filesDir);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                Collections.sort(response.body(), new Comparator<Files>() {
                    @Override
                    public int compare(Files byType1, Files byType2) {
                        return byType1.getType().compareTo(byType2.getType());
                    }
                });

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
