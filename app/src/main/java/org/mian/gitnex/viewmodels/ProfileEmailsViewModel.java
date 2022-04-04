package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Emails;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
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

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<Emails>> call, @NonNull Response<List<Emails>> response) {

		        if(response.isSuccessful()) {
			        emailsList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<Emails>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }
}
