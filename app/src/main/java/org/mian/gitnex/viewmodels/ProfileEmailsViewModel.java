package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.Emails;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ProfileEmailsViewModel extends ViewModel {

    private static MutableLiveData<List<Emails>> emailsList;

    public LiveData<List<Emails>> getEmailsList(String token, Context ctx) {

        emailsList = new MutableLiveData<>();
        loadEmailsList(token, ctx);

        return emailsList;
    }

    public static void loadEmailsList(String token, Context ctx) {

        Call<List<Emails>> call = RetrofitClient
                .getApiInterface(ctx)
                .getUserEmails(token);

        call.enqueue(new Callback<List<Emails>>() {

            @Override
            public void onResponse(@NonNull Call<List<Emails>> call, @NonNull Response<List<Emails>> response) {

                if (response.isSuccessful()) {
                    emailsList.postValue(response.body());
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Emails>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

}
