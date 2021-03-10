package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserOrganizations;
import org.mian.gitnex.clients.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class OrganizationListViewModel extends ViewModel {

    private static MutableLiveData<List<UserOrganizations>> orgsList;

    public LiveData<List<UserOrganizations>> getUserOrgs(String token, Context ctx) {

        //if (orgsList == null) {
            orgsList = new MutableLiveData<>();
            loadOrgsList(token, ctx);
        //}

        return orgsList;
    }

    public static void loadOrgsList(String token, Context ctx) {

        Call<List<UserOrganizations>> call = RetrofitClient
                .getApiInterface(ctx)
                .getUserOrgs(token);

        call.enqueue(new Callback<List<UserOrganizations>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserOrganizations>> call, @NonNull Response<List<UserOrganizations>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        orgsList.postValue(response.body());

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserOrganizations>> call, Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });

    }

}
