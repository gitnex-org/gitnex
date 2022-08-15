package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Team;
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

public class TeamsByOrgViewModel extends ViewModel {

	private MutableLiveData<List<Team>> teamsList;

	public LiveData<List<Team>> getTeamsByOrg(String orgName, Context ctx, TextView noDataTeams, ProgressBar mProgressBar) {

		teamsList = new MutableLiveData<>();
		loadTeamsByOrgList(orgName, ctx, noDataTeams, mProgressBar);

		return teamsList;
	}

	public void loadTeamsByOrgList(String orgName, Context ctx, TextView noDataTeams, ProgressBar mProgressBar) {

		Call<List<Team>> call = RetrofitClient.getApiInterface(ctx).orgListTeams(orgName, null, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Team>> call, @NonNull Response<List<Team>> response) {

				if(response.isSuccessful()) {
					teamsList.postValue(response.body());
				}
				else if(response.code() == 403) {
					Toasty.error(ctx, ctx.getString(R.string.authorizeError));
					mProgressBar.setVisibility(View.GONE);
					noDataTeams.setVisibility(View.GONE);
				}
				else {
					mProgressBar.setVisibility(View.GONE);
					noDataTeams.setVisibility(View.GONE);
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Team>> call, @NonNull Throwable t) {

				mProgressBar.setVisibility(View.GONE);
				noDataTeams.setVisibility(View.GONE);
				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

}
