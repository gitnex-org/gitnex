package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
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

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

	            if (response.isSuccessful()) {
		            if(response.code() == 200) {
			            usersList.postValue(response.body());
		            }
	            }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }

        });
    }

	public static void loadMoreUsersList(String token, int page, int resultLimit, Context ctx, AdminGetUsersAdapter adapter) {

		Call<List<UserInfo>> call = RetrofitClient
			.getApiInterface(ctx)
			.adminGetUsers(token, page, resultLimit);

		call.enqueue(new Callback<List<UserInfo>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

				if (response.isSuccessful()) {
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
					Log.e("onResponse", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});
	}
}
