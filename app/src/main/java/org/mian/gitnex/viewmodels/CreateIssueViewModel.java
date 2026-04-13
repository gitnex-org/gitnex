package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.List;
import org.gitnex.tea4j.v2.models.CreateIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
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
	private final MutableLiveData<Boolean> isCreating = new MutableLiveData<>(false);
	private final MutableLiveData<Issue> createdIssue = new MutableLiveData<>();
	private final MutableLiveData<String> createError = new MutableLiveData<>();

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<Issue> getCreatedIssue() {
		return createdIssue;
	}

	public LiveData<String> getCreateError() {
		return createError;
	}

	public LiveData<List<IssueTemplate>> getTemplates() {
		return templatesLiveData;
	}

	public LiveData<Boolean> getTemplatesLoading() {
		return templatesLoading;
	}

	public LiveData<String> getError() {
		return errorLiveData;
	}

	public void clearCreatedIssue() {
		createdIssue.setValue(null);
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

	public void createIssue(
			Context context, RepositoryContext repoContext, CreateIssueOption issueData) {
		isCreating.setValue(true);

		Call<Issue> call =
				RetrofitClient.getApiInterface(context)
						.issueCreateIssue(repoContext.getOwner(), repoContext.getName(), issueData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						isCreating.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							createdIssue.setValue(response.body());
						} else {
							createError.setValue("Failed to create issue");
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						isCreating.setValue(false);
						createError.setValue(t.getMessage());
					}
				});
	}

	public void clearCreateError() {
		createError.setValue(null);
	}
}
