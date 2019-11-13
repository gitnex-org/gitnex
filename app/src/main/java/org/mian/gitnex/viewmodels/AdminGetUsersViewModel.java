package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserInfo;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class AdminGetUsersViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> usersList;

    public LiveData<List<UserInfo>> getUsersList(Context ctx, String instanceUrl, String token) {

        usersList = new MutableLiveData<>();
        loadUsersList(ctx, instanceUrl, token);

        return usersList;
    }

    public static void loadUsersList(final Context ctx, String instanceUrl, String token) {

        Call<List<UserInfo>> call = RetrofitClient
                .getInstance(instanceUrl, ctx)
                .getApiInterface()
                .adminGetUsers(token);

        call.enqueue(new Callback<List<UserInfo>>() {

            @Override
            public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

                if (response.code() == 200) {
                    usersList.postValue(response.body());
                }

                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            ctx.getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else if(response.code() == 403) {

                    Toasty.info(ctx, ctx.getString(R.string.authorizeError));

                }
                else if(response.code() == 404) {

                    Toasty.info(ctx, ctx.getString(R.string.apiNotFound));

                }
                else {

                    Toasty.info(ctx, ctx.getString(R.string.genericError));
                    Log.i("onResponse", String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
