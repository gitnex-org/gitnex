package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 * @author qwerty287
 */
public class AdminUnadoptedReposViewModel extends ViewModel {

	private MutableLiveData<List<String>> tasksList;

	public LiveData<List<String>> getUnadoptedRepos(
			Context ctx, int page, int limit, String query) {

		tasksList = new MutableLiveData<>();
		loadRepos(ctx, page, limit, query);

		return tasksList;
	}

	public void loadRepos(final Context ctx, final int page, int limit, String query) {

		Call<List<String>> call =
				RetrofitClient.getApiInterface(ctx).adminUnadoptedList(page, limit, query);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<String>> call,
							@NonNull Response<List<String>> response) {

						if (response.isSuccessful()) {
							if (page <= 1 || tasksList.getValue() == null) {
								tasksList.postValue(response.body());
							} else {
								List<String> repos =
										new ArrayList<>(
												Objects.requireNonNull(tasksList.getValue()));
								assert response.body() != null;
								repos.addAll(response.body());
								tasksList.postValue(repos);
							}
						} else if (response.code() == 401) {
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {
							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<String>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
