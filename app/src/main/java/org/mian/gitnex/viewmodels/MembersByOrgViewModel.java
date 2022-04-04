package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.models.UserInfo;
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

public class MembersByOrgViewModel extends ViewModel {

    private static MutableLiveData<List<UserInfo>> membersList;

    public LiveData<List<UserInfo>> getMembersList(String token, String owner, Context ctx) {

        membersList = new MutableLiveData<>();
        loadMembersList(token, owner, ctx);

        return membersList;
    }

    private static void loadMembersList(String token, String owner, Context ctx) {

        Call<List<UserInfo>> call = RetrofitClient
                .getApiInterface(ctx)
                .getMembersByOrg(token, owner);

        call.enqueue(new Callback<>() {

	        @Override
	        public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {

		        if(response.isSuccessful()) {
			        membersList.postValue(response.body());
		        }
		        else {
			        Toasty.error(ctx, ctx.getString(R.string.genericError));
		        }
	        }

	        @Override
	        public void onFailure(@NonNull Call<List<UserInfo>> call, Throwable t) {

		        Toasty.error(ctx, ctx.getString(R.string.errorOnLogin));
	        }
        });
    }
}
