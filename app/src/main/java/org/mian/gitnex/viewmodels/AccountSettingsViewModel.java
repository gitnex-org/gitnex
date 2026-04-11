package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateEmailOption;
import org.gitnex.tea4j.v2.models.CreateKeyOption;
import org.gitnex.tea4j.v2.models.DeleteEmailOption;
import org.gitnex.tea4j.v2.models.Email;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class AccountSettingsViewModel extends ViewModel {

	private final MutableLiveData<List<Email>> emails = new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<Integer> addEmailStatus = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> deleteEmailStatus = new MutableLiveData<>(-1);
	private final MutableLiveData<Boolean> isAddingEmail = new MutableLiveData<>(false);

	private int totalCount = -1;
	private boolean isLastPage = false;

	public LiveData<List<Email>> getEmails() {
		return emails;
	}

	public LiveData<Boolean> getIsLoading() {
		return isLoading;
	}

	public LiveData<String> getError() {
		return errorMessage;
	}

	public LiveData<Integer> getAddEmailStatus() {
		return addEmailStatus;
	}

	public LiveData<Integer> getDeleteEmailStatus() {
		return deleteEmailStatus;
	}

	public LiveData<Boolean> getIsAddingEmail() {
		return isAddingEmail;
	}

	private final MutableLiveData<List<PublicKey>> sshKeys =
			new MutableLiveData<>(new ArrayList<>());
	private final MutableLiveData<Boolean> isKeysLoading = new MutableLiveData<>(false);
	private final MutableLiveData<Boolean> isAddingKey = new MutableLiveData<>(false);
	private final MutableLiveData<Integer> addKeyStatus = new MutableLiveData<>(-1);
	private final MutableLiveData<Integer> deleteKeyStatus = new MutableLiveData<>(-1);

	private int keysTotalCount = -1;
	private boolean isKeysLastPage = false;

	public LiveData<List<PublicKey>> getSshKeys() {
		return sshKeys;
	}

	public LiveData<Boolean> getIsKeysLoading() {
		return isKeysLoading;
	}

	public LiveData<Boolean> getIsAddingKey() {
		return isAddingKey;
	}

	public LiveData<Integer> getAddKeyStatus() {
		return addKeyStatus;
	}

	public LiveData<Integer> getDeleteKeyStatus() {
		return deleteKeyStatus;
	}

	public void fetchEmails(Context ctx, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isLoading.getValue())) return;
		if (!isRefresh && isLastPage) return;

		isLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.userListEmails()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Email>> call,
									@NonNull Response<List<Email>> response) {
								handleResponse(response, isRefresh, limit);
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Email>> call, @NonNull Throwable t) {
								isLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void handleResponse(Response<List<Email>> response, boolean isRefresh, int limit) {
		isLoading.setValue(false);
		if (response.isSuccessful() && response.body() != null) {
			String totalHeader = response.headers().get("x-total-count");
			if (totalHeader != null) totalCount = Integer.parseInt(totalHeader);

			List<Email> currentList =
					isRefresh
							? new ArrayList<>()
							: new ArrayList<>(Objects.requireNonNull(emails.getValue()));
			currentList.addAll(response.body());
			emails.setValue(currentList);

			isLastPage = response.body().size() < limit || currentList.size() >= totalCount;
		} else {
			errorMessage.setValue("Server Error: " + response.code());
		}
	}

	public void addNewEmail(Context context, String email, int limit) {
		isAddingEmail.setValue(true);

		List<String> newEmailList = new ArrayList<>(Collections.singletonList(email));
		CreateEmailOption addEmailFunc = new CreateEmailOption();
		addEmailFunc.setEmails(newEmailList);

		RetrofitClient.getApiInterface(context)
				.userAddEmail(addEmailFunc)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<Email>> call,
									@NonNull Response<List<Email>> response) {
								isAddingEmail.setValue(false);
								addEmailStatus.setValue(response.code());

								if (response.code() == 201) {
									fetchEmails(context, 1, limit, true);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<Email>> call, @NonNull Throwable t) {
								isAddingEmail.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void resetAddEmailStatus() {
		addEmailStatus.setValue(-1);
	}

	public void deleteEmail(Context context, String email, int position) {

		DeleteEmailOption deleteEmailOption = new DeleteEmailOption();
		deleteEmailOption.addEmailsItem(email);

		RetrofitClient.getApiInterface(context)
				.userDeleteEmailWithBody(deleteEmailOption)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									List<Email> current = emails.getValue();
									if (current != null && position < current.size()) {
										current.remove(position);
										emails.setValue(current);
									}
								}
								deleteEmailStatus.setValue(response.code());
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void resetDeleteStatus() {
		deleteEmailStatus.setValue(-1);
	}

	public void fetchSshKeys(Context ctx, int page, int limit, boolean isRefresh) {
		if (Boolean.TRUE.equals(isKeysLoading.getValue())) return;
		if (!isRefresh && isKeysLastPage) return;

		isKeysLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.userCurrentListKeys("", page, limit)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<PublicKey>> call,
									@NonNull Response<List<PublicKey>> response) {
								isKeysLoading.setValue(false);
								if (response.isSuccessful() && response.body() != null) {
									String totalHeader = response.headers().get("x-total-count");
									if (totalHeader != null) {
										keysTotalCount = Integer.parseInt(totalHeader);
									}

									List<PublicKey> currentList =
											isRefresh
													? new ArrayList<>()
													: new ArrayList<>(
															Objects.requireNonNull(
																	sshKeys.getValue()));
									currentList.addAll(response.body());
									sshKeys.setValue(currentList);

									isKeysLastPage =
											response.body().size() < limit
													|| currentList.size() >= keysTotalCount;
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<PublicKey>> call, @NonNull Throwable t) {
								isKeysLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void addNewSshKey(Context context, CreateKeyOption option, int limit) {
		isAddingKey.setValue(true);
		RetrofitClient.getApiInterface(context)
				.userCurrentPostKey(option)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<PublicKey> call,
									@NonNull Response<PublicKey> response) {
								isAddingKey.setValue(false);
								addKeyStatus.setValue(response.code());
								if (response.code() == 201 || response.code() == 202) {
									fetchSshKeys(context, 1, limit, true);
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<PublicKey> call, @NonNull Throwable t) {
								isAddingKey.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void deleteSshKey(Context context, long keyId, int position) {
		RetrofitClient.getApiInterface(context)
				.userCurrentDeleteKey(keyId)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								if (response.isSuccessful()) {
									List<PublicKey> current = sshKeys.getValue();
									if (current != null && position < current.size()) {
										current.remove(position);
										sshKeys.setValue(current);
									}
								}
								deleteKeyStatus.setValue(response.code());
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void resetAddKeyStatus() {
		addKeyStatus.setValue(-1);
	}

	public void resetDeleteKeyStatus() {
		deleteKeyStatus.setValue(-1);
	}
}
