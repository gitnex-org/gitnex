package org.mian.gitnex.viewmodels;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.Teams;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class TeamsByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<Teams>> teamsList;

    public LiveData<List<Teams>> getTeamsByOrg(String token, String orgName, Context ctx, TextView noDataTeams, ProgressBar mProgressBar) {

        teamsList = new MutableLiveData<>();
        loadTeamsByOrgList(token, orgName, ctx, noDataTeams, mProgressBar);

        return teamsList;
    }

    public static void loadTeamsByOrgList(String token, String orgName, Context ctx, TextView noDataTeams, ProgressBar mProgressBar) {

        Call<List<Teams>> call = RetrofitClient
                .getApiInterface(ctx)
                .getTeamsByOrg(token, orgName);

        call.enqueue(new Callback<List<Teams>>() {

            @Override
            public void onResponse(@NonNull Call<List<Teams>> call, @NonNull Response<List<Teams>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {
                        teamsList.postValue(response.body());

                    }
                }
                else if(response.code() == 403) {
	                Toasty.error(ctx, ctx.getString(R.string.authorizeError));
	                mProgressBar.setVisibility(View.GONE);
	                noDataTeams.setText(R.string.authorizeError);
                }
                else {
	                mProgressBar.setVisibility(View.GONE);
	                noDataTeams.setText(R.string.genericError);
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Teams>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
	            mProgressBar.setVisibility(View.GONE);
	            noDataTeams.setText(R.string.genericError);
            }

        });

    }

}
