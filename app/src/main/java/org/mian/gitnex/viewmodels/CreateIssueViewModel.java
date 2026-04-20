package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.CreateIssueOption;
import org.gitnex.tea4j.v2.models.EditIssueOption;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueLabelsOption;
import org.gitnex.tea4j.v2.models.IssueTemplate;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
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
	private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
	private final MutableLiveData<Issue> updatedIssue = new MutableLiveData<>();
	private final MutableLiveData<String> updateError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isUpdatingLabels = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> labelsUpdated = new MutableLiveData<>();
	private final MutableLiveData<String> labelsUpdateError = new MutableLiveData<>();

	public LiveData<Boolean> getIsCreating() {
		return isCreating;
	}

	public LiveData<Issue> getCreatedIssue() {
		return createdIssue;
	}

	public LiveData<String> getCreateError() {
		return createError;
	}

	public LiveData<Boolean> getIsUpdating() {
		return isUpdating;
	}

	public LiveData<Issue> getUpdatedIssue() {
		return updatedIssue;
	}

	public LiveData<String> getUpdateError() {
		return updateError;
	}

	public LiveData<Boolean> getIsUpdatingLabels() {
		return isUpdatingLabels;
	}

	public LiveData<Boolean> getLabelsUpdated() {
		return labelsUpdated;
	}

	public LiveData<String> getLabelsUpdateError() {
		return labelsUpdateError;
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

	public void clearUpdatedIssue() {
		updatedIssue.setValue(null);
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

	public void clearCreateError() {
		createError.setValue(null);
	}

	public void clearUpdateError() {
		updateError.setValue(null);
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
							createError.setValue(context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						isCreating.setValue(false);
						createError.setValue(t.getMessage());
					}
				});
	}

	public void updateIssue(
			Context context,
			RepositoryContext repoContext,
			long issueIndex,
			EditIssueOption issueData) {
		isUpdating.setValue(true);

		Call<Issue> call =
				RetrofitClient.getApiInterface(context)
						.issueEditIssue(
								repoContext.getOwner(),
								repoContext.getName(),
								issueIndex,
								issueData);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						isUpdating.setValue(false);
						if (response.isSuccessful() && response.body() != null) {
							updatedIssue.setValue(response.body());
						} else if (response.code() == 401) {
							updateError.setValue("UNAUTHORIZED");
						} else {
							updateError.setValue(context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						isUpdating.setValue(false);
						updateError.setValue(t.getMessage());
					}
				});
	}

	public void updateIssueLabels(
			Context context,
			String repoOwner,
			String repoName,
			long issueIndex,
			List<Long> labelIds) {
		isUpdatingLabels.setValue(true);

		List<Object> labelIdsObj = new ArrayList<>(labelIds);
		IssueLabelsOption patchIssueLabels = new IssueLabelsOption();
		patchIssueLabels.setLabels(labelIdsObj);

		Call<List<Label>> call =
				RetrofitClient.getApiInterface(context)
						.issueReplaceLabels(repoOwner, repoName, issueIndex, patchIssueLabels);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Label>> call,
							@NonNull Response<List<Label>> response) {
						isUpdatingLabels.setValue(false);
						if (response.code() == 200) {
							labelsUpdated.setValue(true);
						} else if (response.code() == 401) {
							labelsUpdateError.setValue("UNAUTHORIZED");
						} else if (response.code() == 403) {
							labelsUpdateError.setValue(context.getString(R.string.authorizeError));
						} else if (response.code() == 404) {
							labelsUpdateError.setValue(context.getString(R.string.apiNotFound));
						} else {
							labelsUpdateError.setValue(context.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {
						isUpdatingLabels.setValue(false);
						labelsUpdateError.setValue(t.getMessage());
					}
				});
	}

	public void clearLabelsUpdated() {
		labelsUpdated.setValue(null);
	}

	public void clearLabelsUpdateError() {
		labelsUpdateError.setValue(null);
	}
}
