package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.GitTag;
import org.gitnex.tea4j.models.Releases;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.adapters.TagsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class ReleasesViewModel extends ViewModel {

    private static MutableLiveData<List<Releases>> releasesList;
	private static int resultLimit = Constants.resultLimitOldGiteaInstances;

    public LiveData<List<Releases>> getReleasesList(String token, String owner, String repo, Context ctx) {

        releasesList = new MutableLiveData<>();

	    // if gitea is 1.12 or higher use the new limit
	    if(((BaseActivity) ctx).getAccount().requiresVersion("1.12.0")) {
		    resultLimit = Constants.resultLimitNewGiteaInstances;
	    }

        loadReleasesList(token, owner, repo, ctx);

        return releasesList;
    }

    public static void loadReleasesList(String token, String owner, String repo, Context ctx) {

        Call<List<Releases>> call = RetrofitClient
                .getApiInterface(ctx)
                .getReleases(token, owner, repo, 1, resultLimit);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<Releases>> call, @NonNull Response<List<Releases>> response) {

		        if(response.isSuccessful()) {
			        releasesList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<Releases>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }

	public static void loadMoreReleases(String token, String owner, String repo, int page, Context ctx, ReleasesAdapter adapter) {

		Call<List<Releases>> call = RetrofitClient
			.getApiInterface(ctx)
			.getReleases(token, owner, repo, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Releases>> call, @NonNull Response<List<Releases>> response) {

				if(response.isSuccessful()) {
					List<Releases> list = releasesList.getValue();
					assert list != null;
					assert response.body() != null;

					if(response.body().size() != 0) {
						list.addAll(response.body());
						adapter.updateList(list);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Releases>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private static MutableLiveData<List<GitTag>> tagsList;

	public LiveData<List<GitTag>> getTagsList(String token, String owner, String repo, Context ctx) {

		tagsList = new MutableLiveData<>();

		// if gitea is 1.12 or higher use the new limit
		if(((BaseActivity) ctx).getAccount().requiresVersion("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		loadTagsList(token, owner, repo, ctx);

		return tagsList;
	}

	public static void loadTagsList(String token, String owner, String repo, Context ctx) {

		Call<List<GitTag>> call = RetrofitClient
			.getApiInterface(ctx)
			.getTags(token, owner, repo, 1, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<GitTag>> call, @NonNull Response<List<GitTag>> response) {

				if(response.isSuccessful()) {
					tagsList.postValue(response.body());
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<GitTag>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public static void loadMoreTags(String token, String owner, String repo, int page, Context ctx, TagsAdapter adapter) {

		Call<List<GitTag>> call = RetrofitClient
			.getApiInterface(ctx)
			.getTags(token, owner, repo, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<GitTag>> call, @NonNull Response<List<GitTag>> response) {

				if(response.isSuccessful()) {

					List<GitTag> list = tagsList.getValue();
					assert list != null;
					assert response.body() != null;

					if(response.body().size() != 0) {
						list.addAll(response.body());
						adapter.updateList(list);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<GitTag>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
