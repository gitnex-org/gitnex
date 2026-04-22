package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CommitStatus;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CommitStatusesViewModel extends ViewModel {

	private final MutableLiveData<List<CommitStatus>> statuses = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> error = new MutableLiveData<>();
	private final MutableLiveData<Boolean> hasStatuses = new MutableLiveData<>(false);

	public LiveData<List<CommitStatus>> getStatuses() {
		return statuses;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return error;
	}

	public LiveData<Boolean> getHasStatuses() {
		return hasStatuses;
	}

	public void clearError() {
		error.setValue(null);
	}

	public void fetchStatuses(Context ctx, String owner, String repo, String sha) {
		isLoading.setValue(true);
		error.setValue(null);

		Call<List<CommitStatus>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListStatuses(owner, repo, sha, null, null, null, null);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<CommitStatus>> call,
							@NonNull Response<List<CommitStatus>> response) {
						isLoading.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							if (response.body().isEmpty()) {
								hasStatuses.setValue(false);
								statuses.setValue(new ArrayList<>());
								return;
							}

							ArrayList<CommitStatus> merged = new ArrayList<>();
							for (CommitStatus c : response.body()) {
								boolean exists = false;
								for (int i = 0; i < merged.size(); i++) {
									if (Objects.equals(
											merged.get(i).getContext(), c.getContext())) {
										if (merged.get(i).getCreatedAt() != null
												&& c.getCreatedAt() != null
												&& merged.get(i)
														.getCreatedAt()
														.before(c.getCreatedAt())) {
											merged.set(i, c);
										}
										exists = true;
										break;
									}
								}
								if (!exists) {
									merged.add(c);
								}
							}

							hasStatuses.setValue(!merged.isEmpty());
							statuses.setValue(merged);
						} else {
							hasStatuses.setValue(false);
							statuses.setValue(new ArrayList<>());
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<CommitStatus>> call, @NonNull Throwable t) {
						isLoading.setValue(false);
						hasStatuses.setValue(false);
						error.setValue(ctx.getString(R.string.genericError));
					}
				});
	}
}
