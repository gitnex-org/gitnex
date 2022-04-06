package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserInfo;
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

public class RepoStargazersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> stargazersList;

    public LiveData<List<UserInfo>> getRepoStargazers(String token, String repoOwner, String repoName, Context ctx) {

        stargazersList = new MutableLiveData<>();
        loadRepoStargazers(token, repoOwner, repoName, ctx);

        return stargazersList;
    }

    private static void loadRepoStargazers(String token, String repoOwner, String repoName, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .getRepoStargazers(token, repoOwner, repoName);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

		        if(response.isSuccessful()) {
			        stargazersList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }
}
