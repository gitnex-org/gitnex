package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class AccountSettingsSSHKeysViewModel extends ViewModel {

	private MutableLiveData<List<PublicKey>> keysList;
	private int resultLimit;

	public LiveData<List<PublicKey>> getKeysList(Context ctx) {

		keysList = new MutableLiveData<>();
		resultLimit = Constants.getCurrentResultLimit(ctx);
		loadKeysList(ctx);

		return keysList;
	}

	public void loadKeysList(Context ctx) {

		Call<List<PublicKey>> call =
				RetrofitClient.getApiInterface(ctx).userCurrentListKeys("", 1, resultLimit);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<PublicKey>> call,
							@NonNull Response<List<PublicKey>> response) {

						if (response.isSuccessful()) {
							keysList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<PublicKey>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
