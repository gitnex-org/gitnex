package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class RepoWatchersViewModel extends ViewModel {

    private MutableLiveData<List<User>> watchersList;

    public LiveData<List<User>> getRepoWatchers(String repoOwner, String repoName, Context ctx) {

        watchersList = new MutableLiveData<>();
        loadRepoWatchers(repoOwner, repoName, ctx);

        return watchersList;
    }

    private void loadRepoWatchers(String repoOwner, String repoName, Context ctx) {

        Call<List<User>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoListSubscribers(repoOwner, repoName, null, null);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

		        if(response.isSuccessful()) {
			        watchersList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<User>> call, Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }
}
