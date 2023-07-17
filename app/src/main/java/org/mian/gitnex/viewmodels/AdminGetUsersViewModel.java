package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class AdminGetUsersViewModel extends ViewModel {

	private MutableLiveData<List<User>> usersList;

	public LiveData<List<User>> getUsersList(int page, int resultLimit, Context ctx) {

		usersList = new MutableLiveData<>();
		loadUsersList(page, resultLimit, ctx);

		return usersList;
	}

	public void loadUsersList(int page, int resultLimit, Context ctx) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).adminSearchUsers(null, null, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							usersList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public void loadMoreUsersList(
			int page, int resultLimit, Context ctx, AdminGetUsersAdapter adapter) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).adminSearchUsers(null, null, page, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {

							List<User> list = usersList.getValue();
							assert list != null;
							assert response.body() != null;

							if (response.body().size() != 0) {
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
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
