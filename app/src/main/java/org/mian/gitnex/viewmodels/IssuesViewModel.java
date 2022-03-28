package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class IssuesViewModel extends ViewModel {

	private static MutableLiveData<List<Issues>> issuesList;
	private static int resultLimit = Constants.resultLimitOldGiteaInstances;

	public LiveData<List<Issues>> getIssuesList(String token, String searchKeyword, String type, Boolean created, String state, Context ctx) {

		issuesList = new MutableLiveData<>();

		// if gitea is 1.12 or higher use the new limit
		if(((BaseActivity) ctx).getAccount().requiresVersion("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		loadIssuesList(token, searchKeyword, type, created, state, ctx);

		return issuesList;
	}

	public static void loadIssuesList(String token, String searchKeyword, String type, Boolean created, String state, Context ctx) {

		Call<List<Issues>> call = RetrofitClient
			.getApiInterface(ctx)
			.queryIssues(token, searchKeyword, type, created, state, resultLimit, 1);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.isSuccessful()) {
					issuesList.postValue(response.body());
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});
	}

	public static void loadMoreIssues(String token, String searchKeyword, String type, Boolean created, String state, int page, Context ctx, ExploreIssuesAdapter adapter) {

		Call<List<Issues>> call = RetrofitClient
			.getApiInterface(ctx)
			.queryIssues(token, searchKeyword, type, created, state, resultLimit, page);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if (response.isSuccessful()) {
					List<Issues> list = issuesList.getValue();
					assert list != null;
					assert response.body() != null;

					if(response.body().size() != 0) {
						list.addAll(response.body());
						adapter.updateList(list);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});
	}
}
