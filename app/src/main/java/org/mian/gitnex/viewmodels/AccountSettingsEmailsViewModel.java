package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailsViewModel extends ViewModel {

	private MutableLiveData<List<Email>> emailsList;

	public LiveData<List<Email>> getEmailsList(Context ctx) {

		emailsList = new MutableLiveData<>();
		loadEmailsList(ctx);

		return emailsList;
	}

	public void loadEmailsList(Context ctx) {

		Call<List<Email>> call = RetrofitClient.getApiInterface(ctx).userListEmails();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Email>> call,
							@NonNull Response<List<Email>> response) {

						if (response.isSuccessful()) {
							emailsList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
