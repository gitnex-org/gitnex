package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class RepositoriesViewModel extends ViewModel {

	private MutableLiveData<List<Repository>> reposList;

	public LiveData<List<Repository>> getRepositories(
			int page,
			int resultLimit,
			String userLogin,
			String type,
			String orgName,
			Context ctx,
			FragmentRepositoriesBinding fragmentRepositoriesBinding,
			String sort) {

		reposList = new MutableLiveData<>();
		loadReposList(
				page,
				resultLimit,
				userLogin,
				type,
				orgName,
				ctx,
				fragmentRepositoriesBinding,
				sort);

		return reposList;
	}

	public void loadReposList(
			int page,
			int resultLimit,
			String userLogin,
			String type,
			String orgName,
			Context ctx,
			FragmentRepositoriesBinding fragmentRepositoriesBinding,
			String sort) {

		Call<List<Repository>> call =
				switch (type) {
					case "starredRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListStarred(page, resultLimit);
					case "myRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.customUserListRepos(userLogin, page, resultLimit, sort);
					case "org" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListRepos(orgName, page, resultLimit);
					case "team" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListTeamRepos(Long.valueOf(userLogin), page, resultLimit);
					case "watched" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListSubscriptions(page, resultLimit);
					default ->
							RetrofitClient.getApiInterface(ctx)
									.customUserCurrentListRepos(page, resultLimit, sort);
				};

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {

						if (response.isSuccessful()) {
							if (response.code() == 200) {
								reposList.postValue(response.body());
							}
						} else if (response.code() == 403) {
							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
							fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMoreRepos(
			int page,
			int resultLimit,
			String userLogin,
			String type,
			String orgName,
			Context ctx,
			ReposListAdapter adapter,
			String sort) {

		Call<List<Repository>> call =
				switch (type) {
					case "starredRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListStarred(page, resultLimit);
					case "myRepos" ->
							RetrofitClient.getApiInterface(ctx)
									.customUserListRepos(userLogin, page, resultLimit, sort);
					case "org" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListRepos(orgName, page, resultLimit);
					case "team" ->
							RetrofitClient.getApiInterface(ctx)
									.orgListTeamRepos(Long.valueOf(userLogin), page, resultLimit);
					case "watched" ->
							RetrofitClient.getApiInterface(ctx)
									.userCurrentListSubscriptions(page, resultLimit);
					default ->
							RetrofitClient.getApiInterface(ctx)
									.customUserCurrentListRepos(page, resultLimit, sort);
				};

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Repository>> call,
							@NonNull Response<List<Repository>> response) {

						if (response.isSuccessful()) {
							List<Repository> list = reposList.getValue();
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
							@NonNull Call<List<Repository>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
