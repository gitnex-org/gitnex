package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    public LiveData<List<Files>> getFilesList(String token, String owner, String repo, String ref, Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        filesList = new MutableLiveData<>();
        loadFilesList(token, owner, repo, ref, ctx, progressBar, noDataFiles);

        return filesList;
    }

    private static void loadFilesList(String token, String owner, String repo, String ref, final Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        Call<List<Files>> call = RetrofitClient
                .getApiInterface(ctx)
                .getFiles(token, owner, repo, ref);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                if (response.code() == 200) {

	                assert response.body() != null;

	                if(response.body().size() > 0) {

		                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
	                    filesList.postValue(response.body());
	                }
	                else {

		                progressBar.setVisibility(View.GONE);
		                noDataFiles.setVisibility(View.VISIBLE);
	                }
                }
                else {

	                progressBar.setVisibility(View.GONE);
	                noDataFiles.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Files>> call, @NonNull Throwable t) {

	            Toasty.error(ctx, ctx.getString(R.string.errorOnLogin));
            }

        });
    }

    public LiveData<List<Files>> getFilesList2(String token, String owner, String repo, String filesDir, String ref, Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        filesList2 = new MutableLiveData<>();
        loadFilesList2(token, owner, repo, filesDir, ref, ctx, progressBar, noDataFiles);

        return filesList2;
    }

    private static void loadFilesList2(String token, String owner, String repo, String filesDir, String ref, final Context ctx, ProgressBar progressBar, TextView noDataFiles) {

        Call<List<Files>> call = RetrofitClient
                .getApiInterface(ctx)
                .getDirFiles(token, owner, repo, filesDir, ref);

        call.enqueue(new Callback<List<Files>>() {

            @Override
            public void onResponse(@NonNull Call<List<Files>> call, @NonNull Response<List<Files>> response) {

                if (response.code() == 200) {

	                assert response.body() != null;

	                if(response.body().size() > 0) {

		                Collections.sort(response.body(), (byType1, byType2) -> byType1.getType().compareTo(byType2.getType()));
		                filesList2.postValue(response.body());
	                }
	                else {

		                progressBar.setVisibility(View.GONE);
		                noDataFiles.setVisibility(View.VISIBLE);
	                }
                }
                else {

	                progressBar.setVisibility(View.GONE);
	                noDataFiles.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Files>> call, @NonNull Throwable t) {

	            Toasty.error(ctx, ctx.getString(R.string.errorOnLogin));
            }

        });
    }

}
