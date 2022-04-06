package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class AdminGetUsersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> usersList;

    public LiveData<List<UserInfo>> getUsersList(String token, int page, int resultLimit, Context ctx) {

        usersList = new MutableLiveData<>();
        loadUsersList(token, page, resultLimit, ctx);

        return usersList;
    }

    public static void loadUsersList(String token, int page, int resultLimit, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .adminGetUsers(token, page, resultLimit);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

		        if(response.isSuccessful()) {
			        usersList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }

	public static void loadMoreUsersList(String token, int page, int resultLimit, Context ctx, AdminGetUsersAdapter adapter) {

		Call<List<UserInfo>> call = RetrofitClient
			.getApiInterface(ctx)
			.adminGetUsers(token, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

				if(response.isSuccessful()) {

					List<UserInfo> list = usersList.getValue();
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
			public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
