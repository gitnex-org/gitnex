package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.IssueComments;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssueCommentsViewModel extends ViewModel {

    private static MutableLiveData<List<IssueComments>> issueComments;

    public LiveData<List<IssueComments>> getIssueCommentList(String token, String owner, String repo, int index, Context ctx) {

        issueComments = new MutableLiveData<>();
        loadIssueComments(token, owner, repo, index, ctx);

        return issueComments;
    }

	public static void loadIssueComments(String token, String owner, String repo, int index, Context ctx) {
		loadIssueComments(token, owner, repo, index, ctx, null);
	}

    public static void loadIssueComments(String token, String owner, String repo, int index, Context ctx, Runnable onLoadingFinished) {

        Call<List<IssueComments>> call = RetrofitClient
                .getApiInterface(ctx)
                .getIssueComments(token, owner, repo, index);

        call.enqueue(new Callback<List<IssueComments>>() {

            @Override
            public void onResponse(@NonNull Call<List<IssueComments>> call, @NonNull Response<List<IssueComments>> response) {

                if(response.isSuccessful()) {

                    issueComments.postValue(response.body());
					if(onLoadingFinished != null) {
						onLoadingFinished.run();
					}

                }
                else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<IssueComments>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
