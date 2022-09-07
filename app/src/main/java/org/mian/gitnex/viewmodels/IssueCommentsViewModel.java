package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.Comment;
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

public class IssueCommentsViewModel extends ViewModel {

	private MutableLiveData<List<Comment>> issueComments;

	public LiveData<List<Comment>> getIssueCommentList(String owner, String repo, int index, Context ctx) {

		issueComments = new MutableLiveData<>();
		loadIssueComments(owner, repo, index, ctx);

		return issueComments;
	}

	public void loadIssueComments(String owner, String repo, int index, Context ctx) {
		loadIssueComments(owner, repo, index, ctx, null);
	}

	public void loadIssueComments(String owner, String repo, int index, Context ctx, Runnable onLoadingFinished) {

		Call<List<Comment>> call = RetrofitClient.getApiInterface(ctx).issueGetComments(owner, repo, (long) index, null, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Comment>> call, @NonNull Response<List<Comment>> response) {

				if(response.isSuccessful()) {
					issueComments.postValue(response.body());
					if(onLoadingFinished != null) {
						onLoadingFinished.run();
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Comment>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

}
