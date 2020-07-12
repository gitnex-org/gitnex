package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Files;
import java.util.Collections;
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

    public LiveData<List<Files>> getFilesList(String instanceUrl, String token, String owner, String repo, String ref, Context ctx) {

        filesList = new MutableLiveData<>();
        loadFilesList(instanceUrl, token, owner, repo, ref, ctx);

        return filesList;
    }

    private static void loadFilesList(String instanceUrl, String token, String owner, String repo, String ref, final Context ctx) {

        Call<List<Files>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getFiles(token, owner, repo, ref);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                if (response.isSuccessful()) {

	                assert response.body() != null;
	                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
                    filesList.postValue(response.body());

                } else {

                    Toasty.info(ctx, ctx.getString(R.string.noDataFilesTab));
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Files>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

    public LiveData<List<Files>> getFilesList2(String instanceUrl, String token, String owner, String repo, String filesDir, String ref, Context ctx) {

        filesList2 = new MutableLiveData<>();
        loadFilesList2(instanceUrl, token, owner, repo, filesDir, ref, ctx);

        return filesList2;
    }

    private static void loadFilesList2(String instanceUrl, String token, String owner, String repo, String filesDir, String ref, final Context ctx) {

        Call<List<Files>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getDirFiles(token, owner, repo, filesDir, ref);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                if (response.isSuccessful()) {

	                assert response.body() != null;
	                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
                    filesList2.postValue(response.body());

                } else {

                    Toasty.info(ctx, ctx.getString(R.string.noDataFilesTab));
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Files>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
