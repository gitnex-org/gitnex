package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Release;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
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

    private MutableLiveData<List<Release>> releasesList;
	private int resultLimit;

    public LiveData<List<Release>> getReleasesList(String owner, String repo, Context ctx) {

        releasesList = new MutableLiveData<>();
	    resultLimit = Constants.getCurrentResultLimit(ctx);
        loadReleasesList(owner, repo, ctx);
        return releasesList;
    }

    public void loadReleasesList(String owner, String repo, Context ctx) {

        Call<List<Release>> call = RetrofitClient
                .getApiInterface(ctx)
                .repoListReleases(owner, repo, null, null, null, 1, resultLimit);

        call.enqueue(new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<List<Release>> call, @NonNull Response<List<Release>> response) {

		        if(response.isSuccessful()) {
			        releasesList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<Release>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }

	public void loadMoreReleases(String owner, String repo, int page, Context ctx, ReleasesAdapter adapter) {

		Call<List<Release>> call = RetrofitClient
			.getApiInterface(ctx)
			.repoListReleases(owner, repo, null, null, null, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Release>> call, @NonNull Response<List<Release>> response) {

				if(response.isSuccessful()) {
					List<Release> list = releasesList.getValue();
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
			public void onFailure(@NonNull Call<List<Release>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private MutableLiveData<List<Tag>> tagsList;

	public LiveData<List<Tag>> getTagsList(String owner, String repo, Context ctx) {

		tagsList = new MutableLiveData<>();
		resultLimit = Constants.getCurrentResultLimit(ctx);
		loadTagsList(owner, repo, ctx);
		return tagsList;
	}

	public void loadTagsList(String owner, String repo, Context ctx) {

		Call<List<Tag>> call = RetrofitClient
			.getApiInterface(ctx)
			.repoListTags(owner, repo, 1, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Tag>> call, @NonNull Response<List<Tag>> response) {

				if(response.isSuccessful()) {
					tagsList.postValue(response.body());
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Tag>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public void loadMoreTags(String owner, String repo, int page, Context ctx, TagsAdapter adapter) {

		Call<List<Tag>> call = RetrofitClient
			.getApiInterface(ctx)
			.repoListTags(owner, repo, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Tag>> call, @NonNull Response<List<Tag>> response) {

				if(response.isSuccessful()) {

					List<Tag> list = tagsList.getValue();
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
			public void onFailure(@NonNull Call<List<Tag>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
