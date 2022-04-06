package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Labels;
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

public class OrganizationLabelsViewModel extends ViewModel {

	private static MutableLiveData<List<Labels>> orgLabelsList;

	public LiveData<List<Labels>> getOrgLabelsList(String token, String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		orgLabelsList = new MutableLiveData<>();
		loadOrgLabelsList(token, owner, ctx, progressBar, noData);

		return orgLabelsList;
	}

	public static void loadOrgLabelsList(String token, String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		Call<List<Labels>> call = RetrofitClient
			.getApiInterface(ctx)
			.getOrganizationLabels(token, owner);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Labels>> call, @NonNull Response<List<Labels>> response) {

				if(response.isSuccessful()) {

					orgLabelsList.postValue(response.body());
				}
				else {

					progressBar.setVisibility(View.GONE);
					noData.setVisibility(View.VISIBLE);
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Labels>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
