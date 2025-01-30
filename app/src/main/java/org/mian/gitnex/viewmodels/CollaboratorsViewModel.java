package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class CollaboratorsViewModel extends ViewModel {

	private static MutableLiveData<List<User>> collaboratorsList;

	public LiveData<List<User>> getCollaboratorsList(
			String owner, String repo, Context ctx, int page, int resultLimit) {

		collaboratorsList = new MutableLiveData<>();
		loadCollaboratorsListList(owner, repo, ctx, page, resultLimit);

		return collaboratorsList;
	}

	public static void loadCollaboratorsListList(
			String owner, String repo, Context ctx, int page, int resultLimit) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListCollaborators(owner, repo, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							collaboratorsList.postValue(response.body());
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMore(
			String owner,
			String repo,
			Context ctx,
			int page,
			int resultLimit,
			CollaboratorsAdapter adapter,
			FragmentCollaboratorsBinding binding) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListCollaborators(owner, repo, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {
						assert response.body() != null;
						if (response.isSuccessful()) {
							List<User> list = collaboratorsList.getValue();
							assert list != null;
							assert response.body() != null;

							if (!response.body().isEmpty()) {
								list.addAll(response.body());
								adapter.updateList(list);
							} else {
								adapter.setMoreDataAvailable(false);
							}
							binding.progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
