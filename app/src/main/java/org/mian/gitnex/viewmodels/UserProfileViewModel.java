package org.mian.gitnex.viewmodels;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import okhttp3.ResponseBody;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.UpdateUserAvatarOption;
import org.gitnex.tea4j.v2.models.User;
import org.gitnex.tea4j.v2.models.UserHeatmapData;
import org.gitnex.tea4j.v2.models.UserSettings;
import org.gitnex.tea4j.v2.models.UserSettingsOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class UserProfileViewModel extends ViewModel {

	private final MutableLiveData<Boolean> isFollowing = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isActionLoading = new MutableLiveData<>(false);
	private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
	private final MutableLiveData<User> userProfile = new MutableLiveData<>();
	private final MutableLiveData<List<UserHeatmapData>> heatmapData = new MutableLiveData<>();
	private final MutableLiveData<String> profileReadme = new MutableLiveData<>();
	private final MutableLiveData<UserSettings> userSettings = new MutableLiveData<>();
	private final MutableLiveData<Boolean> isProfileLoading = new MutableLiveData<>(false);
	private final MutableLiveData<User> userData = new MutableLiveData<>();
	private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

	public LiveData<Boolean> getIsFollowing() {
		return isFollowing;
	}

	public LiveData<Boolean> getIsActionLoading() {
		return isActionLoading;
	}

	public LiveData<String> getErrorMessage() {
		return errorMessage;
	}

	public LiveData<User> getUserProfile() {
		return userProfile;
	}

	public LiveData<List<UserHeatmapData>> getHeatmapData() {
		return heatmapData;
	}

	public LiveData<String> getProfileReadme() {
		return profileReadme;
	}

	public LiveData<UserSettings> getUserSettings() {
		return userSettings;
	}

	public LiveData<Boolean> getIsProfileLoading() {
		return isProfileLoading;
	}

	public LiveData<String> getToastMessage() {
		return toastMessage;
	}

	public void clearMessages() {
		errorMessage.setValue(null);
		toastMessage.setValue(null);
	}

	public void loadFullProfile(Context ctx, String username) {
		isProfileLoading.setValue(true);
		fetchProfileDetail(ctx, username);
		fetchHeatmap(ctx, username);
		fetchProfileRepository(ctx, username);
	}

	private void fetchProfileDetail(Context ctx, String username) {
		RetrofitClient.getApiInterface(ctx)
				.userGet(username)
				.enqueue(
						new Callback<User>() {
							@Override
							public void onResponse(
									@NonNull Call<User> call, @NonNull Response<User> response) {
								isProfileLoading.setValue(false);
								if (response.isSuccessful()) {
									userProfile.setValue(response.body());
								} else if (response.code() == 401) {
									errorMessage.setValue("AUTH_REVOKED");
								}
							}

							@Override
							public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
								isProfileLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	private void fetchHeatmap(Context ctx, String username) {
		RetrofitClient.getApiInterface(ctx)
				.userGetHeatmapData(username)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<List<UserHeatmapData>> call,
									@NonNull Response<List<UserHeatmapData>> response) {
								if (response.isSuccessful()) {
									heatmapData.setValue(response.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<List<UserHeatmapData>> call,
									@NonNull Throwable t) {}
						});
	}

	private void fetchProfileRepository(Context ctx, String username) {
		RetrofitClient.getApiInterface(ctx)
				.repoGet(username, ".profile")
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Repository> call,
									@NonNull Response<Repository> response) {
								if (response.isSuccessful() && response.body() != null) {
									fetchReadmeContent(
											ctx, username, response.body().getDefaultBranch());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<Repository> call, @NonNull Throwable t) {}
						});
	}

	private void fetchReadmeContent(Context ctx, String username, String branch) {
		RetrofitClient.getWebInterface(ctx)
				.getFileContents(username, ".profile", branch, "README.md")
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<ResponseBody> call,
									@NonNull Response<ResponseBody> response) {
								try {
									if (response.isSuccessful() && response.body() != null) {
										profileReadme.setValue(response.body().string());
									}
								} catch (IOException ignored) {
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
						});
	}

	public void fetchUserSettings(Context ctx) {
		RetrofitClient.getApiInterface(ctx)
				.customGetUserSettings()
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<UserSettings> call,
									@NonNull Response<UserSettings> response) {
								if (response.isSuccessful()) {
									userSettings.setValue(response.body());
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<UserSettings> call, @NonNull Throwable t) {}
						});
	}

	public void updateUserAvatar(Context ctx, String encodedBase64) {
		isActionLoading.setValue(true);
		UpdateUserAvatarOption option = new UpdateUserAvatarOption();
		option.setImage(encodedBase64);

		RetrofitClient.getApiInterface(ctx)
				.userUpdateAvatar(option)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isActionLoading.setValue(false);
								if (response.code() == 204) {
									fetchProfileDetail(
											ctx,
											Objects.requireNonNull(userProfile.getValue())
													.getLogin());
									toastMessage.setValue("AVATAR_UPDATED");
								} else {
									errorMessage.setValue(ctx.getString(R.string.genericError));
								}
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void updateUserProfile(Context ctx, UserSettingsOptions options) {
		isActionLoading.setValue(true);
		RetrofitClient.getApiInterface(ctx)
				.customUpdateUserSettings(options)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<UserSettings> call,
									@NonNull Response<UserSettings> response) {
								isActionLoading.setValue(false);
								if (response.isSuccessful()) {
									fetchProfileDetail(
											ctx,
											Objects.requireNonNull(userProfile.getValue())
													.getLogin());
									toastMessage.setValue("PROFILE_UPDATED");
								} else if (response.code() == 401) {
									errorMessage.setValue("AUTH_REVOKED");
								} else {
									errorMessage.setValue(ctx.getString(R.string.genericError));
								}
							}

							@Override
							public void onFailure(
									@NonNull Call<UserSettings> call, @NonNull Throwable t) {
								isActionLoading.setValue(false);
								errorMessage.setValue(t.getMessage());
							}
						});
	}

	public void checkFollowStatus(Context ctx, String username) {
		RetrofitClient.getApiInterface(ctx)
				.userCurrentCheckFollowing(username)
				.enqueue(
						new Callback<>() {
							@Override
							public void onResponse(
									@NonNull Call<Void> call, @NonNull Response<Void> response) {
								isFollowing.setValue(response.code() == 204);
							}

							@Override
							public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
								isFollowing.setValue(false);
							}
						});
	}

	public void toggleFollow(Context ctx, String username) {
		if (Boolean.TRUE.equals(isActionLoading.getValue())) return;

		isActionLoading.setValue(true);
		boolean currentlyFollowing = Boolean.TRUE.equals(isFollowing.getValue());

		Call<Void> call =
				currentlyFollowing
						? RetrofitClient.getApiInterface(ctx).userCurrentDeleteFollow(username)
						: RetrofitClient.getApiInterface(ctx).userCurrentPutFollow(username);

		call.enqueue(
				new Callback<Void>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						isActionLoading.setValue(false);
						if (response.isSuccessful()) {
							isFollowing.setValue(!currentlyFollowing);
							toastMessage.setValue(
									currentlyFollowing ? "USER_UNFOLLOWED" : "USER_FOLLOWED");
						} else {
							errorMessage.setValue("Action failed: " + response.code());
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						isActionLoading.setValue(false);
						errorMessage.setValue(t.getMessage());
					}
				});
	}
}
