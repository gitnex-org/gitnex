package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.gitnex.tea4j.v2.models.EditRepoOption;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.TransferRepoOption;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepositorySettingsViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isUpdating = new MutableLiveData<>(false);
	private final MutableLiveData<Repository> updatedRepo = new MutableLiveData<>();
	private final MutableLiveData<String> updateError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isTransferring = new MutableLiveData<>(false);
	private final MutableLiveData<Repository> transferredRepo = new MutableLiveData<>();
	private final MutableLiveData<String> transferError = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isDeleting = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> repoDeleted = new MutableLiveData<>();
	private final MutableLiveData<String> deleteError = new MutableLiveData<>();
	private final MutableLiveData<Repository> repositoryProperties = new MutableLiveData<>();
	private final MutableLiveData<String> repositoryPropertiesError = new MutableLiveData<>();

	public LiveData<Boolean> getIsUpdating() {
		return isUpdating;
	}

	public LiveData<Repository> getUpdatedRepo() {
		return updatedRepo;
	}

	public LiveData<String> getUpdateError() {
		return updateError;
	}

	public LiveData<Boolean> getIsTransferring() {
		return isTransferring;
	}

	public LiveData<Repository> getTransferredRepo() {
		return transferredRepo;
	}

	public LiveData<String> getTransferError() {
		return transferError;
	}

	public LiveData<Boolean> getIsDeleting() {
		return isDeleting;
	}

	public LiveData<Boolean> getRepoDeleted() {
		return repoDeleted;
	}

	public LiveData<String> getDeleteError() {
		return deleteError;
	}

	public LiveData<Repository> getRepository() {
		return repositoryProperties;
	}

	public LiveData<String> getRepositoryError() {
		return repositoryPropertiesError;
	}

	public void clearUpdatedRepo() {
		updatedRepo.setValue(null);
	}

	public void clearUpdateError() {
		updateError.setValue(null);
	}

	public void clearTransferredRepo() {
		transferredRepo.setValue(null);
	}

	public void clearTransferError() {
		transferError.setValue(null);
	}

	public void clearRepoDeleted() {
		repoDeleted.setValue(null);
	}

	public void clearDeleteError() {
		deleteError.setValue(null);
	}

	public void clearRepositoryProperties() {
		repositoryProperties.setValue(null);
	}

	public void clearRepositoryPropertiesError() {
		repositoryPropertiesError.setValue(null);
	}

	public void fetchRepositoryProperties(Context ctx, String owner, String repo) {
		Call<Repository> call = RetrofitClient.getApiInterface(ctx).repoGet(owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull Response<Repository> response) {
						if (response.isSuccessful() && response.body() != null) {
							repositoryProperties.setValue(response.body());
						} else {
							repositoryPropertiesError.setValue(
									ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						repositoryPropertiesError.setValue(t.getMessage());
					}
				});
	}

	public void updateRepository(Context ctx, String owner, String repo, EditRepoOption repoProps) {
		isUpdating.setValue(true);
		updateError.setValue(null);

		Call<Repository> call =
				RetrofitClient.getApiInterface(ctx).repoEdit(owner, repo, repoProps);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull Response<Repository> response) {
						isUpdating.setValue(false);

						if (response.isSuccessful() && response.body() != null) {
							updatedRepo.setValue(response.body());
						} else {
							updateError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						isUpdating.setValue(false);
						updateError.setValue(t.getMessage());
					}
				});
	}

	public void transferRepository(Context ctx, String owner, String repo, String newOwner) {
		isTransferring.setValue(true);
		transferError.setValue(null);

		TransferRepoOption transferOption = new TransferRepoOption();
		transferOption.setNewOwner(newOwner);

		Call<Repository> call =
				RetrofitClient.getApiInterface(ctx).repoTransfer(transferOption, owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull Response<Repository> response) {
						isTransferring.setValue(false);

						if ((response.code() == 202 || response.code() == 201)
								&& response.body() != null) {
							transferredRepo.setValue(response.body());
						} else if (response.code() == 404) {
							transferError.setValue(ctx.getString(R.string.repoTransferError));
						} else {
							transferError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						isTransferring.setValue(false);
						transferError.setValue(t.getMessage());
					}
				});
	}

	public void deleteRepository(Context ctx, String owner, String repo) {
		isDeleting.setValue(true);
		deleteError.setValue(null);

		Call<Void> call = RetrofitClient.getApiInterface(ctx).repoDelete(owner, repo);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isDeleting.setValue(false);

						if (response.code() == 204) {
							repoDeleted.setValue(true);
						} else {
							deleteError.setValue(ctx.getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isDeleting.setValue(false);
						deleteError.setValue(t.getMessage());
					}
				});
	}
}
