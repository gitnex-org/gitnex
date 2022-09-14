package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.TimelineComment;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.IssueCommentsAdapter;
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

public class IssueCommentsViewModel extends ViewModel {

	private MutableLiveData<List<TimelineComment>> issueComments;
	private int resultLimit;

	public LiveData<List<TimelineComment>> getIssueCommentList(String owner, String repo, int index, Context ctx) {

		issueComments = new MutableLiveData<>();
		resultLimit = Constants.getCurrentResultLimit(ctx);
		loadIssueComments(owner, repo, index, ctx, null);
		return issueComments;
	}

	public void loadIssueComments(String owner, String repo, int index, Context ctx, Runnable onLoadingFinished) {

		Call<List<TimelineComment>> call = RetrofitClient.getApiInterface(ctx).issueGetCommentsAndTimeline(owner, repo, (long) index, null, 1, resultLimit, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<TimelineComment>> call, @NonNull Response<List<TimelineComment>> response) {

				if(response.isSuccessful()) {

					if(response.body() != null) {

						issueComments.postValue(response.body());
						if(onLoadingFinished != null) {
							onLoadingFinished.run();
						}
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<TimelineComment>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	public void loadMoreIssueComments(String owner, String repo, int index, Context ctx, int page, IssueCommentsAdapter adapter) {

		Call<List<TimelineComment>> call = RetrofitClient.getApiInterface(ctx).issueGetCommentsAndTimeline(owner, repo, (long) index, null, page, resultLimit, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<TimelineComment>> call, @NonNull Response<List<TimelineComment>> response) {

				if(response.isSuccessful()) {

					if(response.body() != null) {

						List<TimelineComment> list = issueComments.getValue();
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
						adapter.setMoreDataAvailable(false);
					}
				}
				else {
					Toasty.error(ctx, ctx.getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<TimelineComment>> call, @NonNull Throwable t) {
				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

}
