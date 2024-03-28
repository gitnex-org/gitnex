package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.IOException;
import java.util.List;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.json.JSONException;
import org.json.JSONObject;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.WikiListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentWikiBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class WikiViewModel extends ViewModel {

	private MutableLiveData<List<WikiPageMetaData>> wikiList;

	public LiveData<List<WikiPageMetaData>> getWiki(
			String owner,
			String repo,
			int page,
			int resultLimit,
			Context ctx,
			FragmentWikiBinding fragmentWikiBinding) {

		wikiList = new MutableLiveData<>();
		loadWikiList(owner, repo, page, resultLimit, ctx, fragmentWikiBinding);

		return wikiList;
	}

	public void loadWikiList(
			String owner,
			String repo,
			int page,
			int resultLimit,
			Context ctx,
			FragmentWikiBinding fragmentWikiBinding) {

		Call<List<WikiPageMetaData>> call;
		call = RetrofitClient.getApiInterface(ctx).repoGetWikiPages(owner, repo, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<WikiPageMetaData>> call,
							@NonNull Response<List<WikiPageMetaData>> response) {

						if (response.isSuccessful()) {
							if (response.code() == 200) {
								wikiList.postValue(response.body());
							}
						} else if (response.code() == 404) {
							fragmentWikiBinding.progressBar.setVisibility(View.GONE);
							fragmentWikiBinding.noData.setVisibility(View.VISIBLE);
							fragmentWikiBinding.recyclerView.setVisibility(View.GONE);
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}

						try {
							if (response.errorBody() != null) {
								new JSONObject(response.errorBody().string());
							}
						} catch (IOException | JSONException e) {
							fragmentWikiBinding.noData.setText(
									ctx.getResources().getString(R.string.apiNotFound));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<WikiPageMetaData>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMoreWiki(
			String owner,
			String repo,
			int page,
			int resultLimit,
			Context ctx,
			FragmentWikiBinding fragmentWikiBinding,
			WikiListAdapter adapter) {

		Call<List<WikiPageMetaData>> call;
		call = RetrofitClient.getApiInterface(ctx).repoGetWikiPages(owner, repo, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<WikiPageMetaData>> call,
							@NonNull Response<List<WikiPageMetaData>> response) {

						if (response.isSuccessful()) {
							List<WikiPageMetaData> list = wikiList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<WikiPageMetaData>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
