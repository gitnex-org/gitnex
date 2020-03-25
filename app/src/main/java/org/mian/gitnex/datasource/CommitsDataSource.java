package org.mian.gitnex.datasource;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.paging.PageKeyedDataSource;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Commits;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CommitsDataSource extends PageKeyedDataSource<Integer, Commits> {

    private String TAG = "CommitsDataSource";
    private Context ctx;

    private static final int FIRST_PAGE = 1;

    private String instanceUrl;
    private String instanceToken;
    private String owner;
    private String repo;

    CommitsDataSource(Context ctx, String instanceUrl, String instanceToken, String owner, String repo) {

        this.ctx = ctx;
        this.instanceUrl = instanceUrl;
        this.instanceToken = instanceToken;
        this.owner = owner;
        this.repo = repo;

    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Commits> callback) {

        RetrofitClient.getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getRepositoryCommits(instanceToken, owner, repo, FIRST_PAGE)
                .enqueue(new Callback<List<Commits>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                        if (response.body() != null) {

                            callback.onResult(response.body(), null, FIRST_PAGE + 1);

                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

                        Log.e(TAG, Objects.requireNonNull(t.getMessage()));

                    }
                });

    }

    @Override
    public void loadBefore(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Commits> callback) {

        RetrofitClient.getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getRepositoryCommits(instanceToken, owner, repo, params.key)
                .enqueue(new Callback<List<Commits>>() {

                    @Override
                    public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                        Integer adjacentKey = (params.key > 1) ? params.key - 1 : null;

                        if (response.body() != null) {

                            callback.onResult(response.body(), adjacentKey);

                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

                        Log.e(TAG, Objects.requireNonNull(t.getMessage()));

                    }

                });

    }

    @Override
    public void loadAfter(@NonNull final LoadParams<Integer> params, @NonNull final LoadCallback<Integer, Commits> callback) {

        RetrofitClient.getInstance(instanceUrl, ctx)
                .getApiInterface()
                .getRepositoryCommits(instanceToken, owner, repo, params.key)
                .enqueue(new Callback<List<Commits>>() {

                    @Override
                    public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

                        if (response.body() != null) {

                            boolean next = false;
                            if(response.body().size() > 0) {
                                next = true;
                            }

                            Integer key = next ? params.key + 1 : null;

                            callback.onResult(response.body(), key);

                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

                        Log.e(TAG, Objects.requireNonNull(t.getMessage()));

                    }

                });

    }

}
