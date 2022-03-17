package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserOrganizations;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class OrganizationsViewModel extends ViewModel {

    private static MutableLiveData<List<UserOrganizations>> orgList;

    public LiveData<List<UserOrganizations>> getUserOrg(String token, int page, int resultLimit, Context ctx) {

	    orgList = new MutableLiveData<>();
    	loadOrgList(token, page, resultLimit, ctx);

        return orgList;
    }

    public static void loadOrgList(String token, int page, int resultLimit, Context ctx) {

        Call<List<UserOrganizations>> call = RetrofitClient
                .getApiInterface(ctx)
                .getUserOrgs(token, page, resultLimit);

        call.enqueue(new Callback<List<UserOrganizations>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserOrganizations>> call, @NonNull Response<List<UserOrganizations>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
	                    orgList.postValue(response.body());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<UserOrganizations>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }

        });
    }

	public static void loadMoreOrgList(String token, int page, int resultLimit, Context ctx, OrganizationsListAdapter adapter) {

		Call<List<UserOrganizations>> call = RetrofitClient
			.getApiInterface(ctx)
			.getUserOrgs(token, page, resultLimit);

		call.enqueue(new Callback<List<UserOrganizations>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserOrganizations>> call, @NonNull Response<List<UserOrganizations>> response) {

				if(response.isSuccessful()) {
					List<UserOrganizations> list = orgList.getValue();
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
			public void onFailure(@NonNull Call<List<UserOrganizations>> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}

		});
	}
}
