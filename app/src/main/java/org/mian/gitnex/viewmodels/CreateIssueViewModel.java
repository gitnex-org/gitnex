package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.IssueTemplate;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class CreateIssueViewModel extends ViewModel {

	private final MutableLiveData<List<IssueTemplate>> templatesLiveData = new MutableLiveData<>();
	private final MutableLiveData<Boolean> templatesLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

	public LiveData<List<IssueTemplate>> getTemplates() {
		return templatesLiveData;
	}

	public LiveData<Boolean> getTemplatesLoading() {
		return templatesLoading;
	}

	public LiveData<String> getError() {
		return errorLiveData;
	}

	public void fetchIssueTemplates(Context context, RepositoryContext repoContext) {
		templatesLoading.setValue(true);

		Call<List<IssueTemplate>> call =
				RetrofitClient.getApiInterface(context)
						.repoGetIssueTemplates(repoContext.getOwner(), repoContext.getName());

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<IssueTemplate>> call,
							@NonNull Response<List<IssueTemplate>> response) {
						templatesLoading.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							templatesLiveData.setValue(response.body());
						} else {
							templatesLiveData.setValue(null);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<IssueTemplate>> call, @NonNull Throwable t) {
						templatesLoading.setValue(false);
						errorLiveData.setValue(t.getMessage());
					}
				});
	}

	public void clearError() {
		errorLiveData.setValue(null);
	}
}
