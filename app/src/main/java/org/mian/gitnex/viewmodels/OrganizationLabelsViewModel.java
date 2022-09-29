package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class OrganizationLabelsViewModel extends ViewModel {

	private static MutableLiveData<List<Label>> orgLabelsList;

	public static void loadOrgLabelsList(
			String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		Call<List<Label>> call =
				RetrofitClient.getApiInterface(ctx).orgListLabels(owner, null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull Response<List<Label>> response) {

						if (response.isSuccessful()) {

							orgLabelsList.postValue(response.body());
						} else {

							progressBar.setVisibility(View.GONE);
							noData.setVisibility(View.VISIBLE);
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	public LiveData<List<Label>> getOrgLabelsList(
			String owner, Context ctx, ProgressBar progressBar, TextView noData) {

		orgLabelsList = new MutableLiveData<>();
		loadOrgLabelsList(owner, ctx, progressBar, noData);

		return orgLabelsList;
	}
}
