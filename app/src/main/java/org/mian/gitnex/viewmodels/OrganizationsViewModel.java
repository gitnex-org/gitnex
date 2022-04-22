package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class OrganizationsViewModel extends ViewModel {

    private MutableLiveData<List<Organization>> orgList;

    public LiveData<List<Organization>> getUserOrg(int page, int resultLimit, Context ctx) {

	    orgList = new MutableLiveData<>();
    	loadOrgList(page, resultLimit, ctx);

        return orgList;
    }

    public void loadOrgList(int page, int resultLimit, Context ctx) {

        Call<List<Organization>> call = RetrofitClient
                .getApiInterface(ctx)
                .orgListCurrentUserOrgs(page, resultLimit);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

		        if(response.isSuccessful()) {
		        	orgList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
	        }
        });
    }

	public void loadMoreOrgList(int page, int resultLimit, Context ctx, OrganizationsListAdapter adapter) {

		Call<List<Organization>> call = RetrofitClient
			.getApiInterface(ctx)
			.orgListCurrentUserOrgs(page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

				if(response.isSuccessful()) {
					List<Organization> list = orgList.getValue();
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
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}
}
