package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class MembersByOrgViewModel extends ViewModel {

	private MutableLiveData<List<User>> membersList;

	public LiveData<List<User>> getMembersList(String owner, Context ctx) {

		membersList = new MutableLiveData<>();
		loadMembersList(owner, ctx);

		return membersList;
	}

	private void loadMembersList(String owner, Context ctx) {

		Call<List<User>> call =
				RetrofitClient.getApiInterface(ctx).orgListMembers(owner, null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<User>> call,
							@NonNull Response<List<User>> response) {

						if (response.isSuccessful()) {
							membersList.postValue(response.body());
						} else {
							Toasty.error(ctx, ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
