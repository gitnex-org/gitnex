package org.mian.gitnex.viewmodels;

import android.util.Log;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.IssueComments;
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

public class IssueCommentsViewModel extends ViewModel {

    private static MutableLiveData<List<IssueComments>> issueComments;

    public LiveData<List<IssueComments>> getIssueCommentList(String instanceUrl, String token, String owner, String repo, int index) {

        issueComments = new MutableLiveData<>();
        loadIssueComments(instanceUrl, token, owner, repo, index);

        return issueComments;
    }

    public static void loadIssueComments(String instanceUrl, String token, String owner, String repo, int index) {

        Call<List<IssueComments>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getIssueComments(token, owner, repo, index);

        call.enqueue(new Callback<List<IssueComments>>() {

            @Override
            public void onResponse(@NonNull Call<List<IssueComments>> call, @NonNull Response<List<IssueComments>> response) {

                if(response.isSuccessful()) {

                    issueComments.postValue(response.body());

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
