package org.mian.gitnex.viewmodels;

import android.content.Context;
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

public class LabelsViewModel extends ViewModel {

    private static MutableLiveData<List<Labels>> labelsList;

    public LiveData<List<Labels>> getLabelsList(String token, String owner, String repo, Context ctx) {

        labelsList = new MutableLiveData<>();
        loadLabelsList(token, owner, repo, ctx);

        return labelsList;
    }

    public static void loadLabelsList(String token, String owner, String repo, Context ctx) {

        Call<List<Labels>> call = RetrofitClient
                .getApiInterface(ctx)
                .getLabels(token, owner, repo);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<Labels>> call, @NonNull Response<List<Labels>> response) {

		        if(response.isSuccessful()) {
			        labelsList.postValue(response.body());
		        }
		        else {
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
