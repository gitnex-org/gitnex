package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.CronTasks;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class AdminCronTasksViewModel extends ViewModel {

	private static MutableLiveData<List<CronTasks>> tasksList;

	public LiveData<List<CronTasks>> getCronTasksList(Context ctx, String token, int page, int limit) {

		tasksList = new MutableLiveData<>();
		loadCronTasksList(ctx, token, page, limit);

		return tasksList;
	}

	public static void loadCronTasksList(final Context ctx, String token, int page, int limit) {

		Call<List<CronTasks>> call = RetrofitClient
			.getApiInterface(ctx)
			.adminGetCronTasks(token, page, limit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<CronTasks>> call, @NonNull Response<List<CronTasks>> response) {

				if(response.isSuccessful()) {
					tasksList.postValue(response.body());
				}
				else if(response.code() == 401) {
					AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
						ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage), ctx.getResources().getString(R.string.cancelButton),
						ctx.getResources().getString(R.string.navLogout));
				}
				else if(response.code() == 403) {
					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
				}
				else if(response.code() == 404) {
					Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<CronTasks>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
