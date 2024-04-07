package org.mian.gitnex.fragments.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import okhttp3.ResponseBody;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.UpdateUserAvatarOption;
import org.gitnex.tea4j.v2.models.User;
import org.gitnex.tea4j.v2.models.UserSettings;
import org.gitnex.tea4j.v2.models.UserSettingsOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomEditAvatarDialogBinding;
import org.mian.gitnex.databinding.CustomEditProfileBinding;
import org.mian.gitnex.databinding.FragmentProfileDetailBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class DetailFragment extends Fragment {

	private static final String usernameBundle = "";
	Locale locale;
	private Context context;
	private FragmentProfileDetailBinding binding;
	private String username;
	private CustomEditProfileBinding customEditProfileBinding;
	private CustomEditAvatarDialogBinding customEditAvatarDialogBinding;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;
	private AlertDialog dialogEditSettings;
	private AlertDialog dialogEditAvatar;
	private int imgRadius;
	private static Uri avatarUri = null;
	public static boolean refProfile = false;

	public DetailFragment() {}

	public static DetailFragment newInstance(String username) {
		DetailFragment fragment = new DetailFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentProfileDetailBinding.inflate(inflater, container, false);
		context = getContext();
		locale = getResources().getConfiguration().locale;
		imgRadius = AppUtil.getPixelsFromDensity(context, 3);

		getProfileDetail(username);
		getProfileRepository(username);

		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		binding.userFollowersCount.setOnClickListener(
				metaFollowersFrame ->
						((ProfileActivity) requireActivity()).viewPager.setCurrentItem(4));
		binding.userFollowingCount.setOnClickListener(
				metaFollowingFrame ->
						((ProfileActivity) requireActivity()).viewPager.setCurrentItem(5));
		binding.userStarredReposCount.setOnClickListener(
				metaStarredReposFrame ->
						((ProfileActivity) requireActivity()).viewPager.setCurrentItem(2));

		if (username.equals(((BaseActivity) context).getAccount().getAccount().getUserName())) {
			binding.metaProfile.setVisibility(View.VISIBLE);
		} else {
			binding.metaProfile.setVisibility(View.GONE);
		}

		binding.updateProfile.setOnClickListener(
				editProfileSettings -> {
					customEditProfileBinding =
							CustomEditProfileBinding.inflate(LayoutInflater.from(context));
					showEditProfileDialog();
				});

		binding.updateAvatar.setOnClickListener(updateAvatar -> openFileAttachment());

		return binding.getRoot();
	}

	public void onDestroy() {
		avatarUri = null;
		super.onDestroy();
	}

	ActivityResultLauncher<Intent> activityForAvatarUpdate =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							Intent data = result.getData();
							assert data != null;
							avatarUri = data.getData();
							if (avatarUri != null) {
								customEditAvatarDialogBinding =
										CustomEditAvatarDialogBinding.inflate(
												LayoutInflater.from(context));
								showUpdateAvatarDialog();
							}
						}
					});

	private void openFileAttachment() {

		String[] mimeTypes = {"image/webp", "image/gif", "image/jpg", "image/jpeg", "image/png"};
		Intent data = new Intent(Intent.ACTION_GET_CONTENT);
		data.addCategory(Intent.CATEGORY_OPENABLE);
		data.setType("image/*");
		data.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		Intent intent = Intent.createChooser(data, "Choose an image");
		activityForAvatarUpdate.launch(intent);
	}

	private void showUpdateAvatarDialog() {

		View view = customEditAvatarDialogBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		PicassoService.getInstance(context)
				.get()
				.load(avatarUri)
				.transform(new RoundedTransformation(imgRadius, 0))
				.placeholder(R.drawable.loader_animated)
				.resize(180, 180)
				.centerCrop()
				.into(customEditAvatarDialogBinding.userAvatar);

		customEditAvatarDialogBinding.save.setOnClickListener(
				saveUserAvatar -> saveUserAvatar(avatarUri));

		dialogEditAvatar = materialAlertDialogBuilder.show();
	}

	private void saveUserAvatar(Uri avatar) {

		InputStream imageStream = null;
		try {
			imageStream = context.getContentResolver().openInputStream(avatar);
		} catch (FileNotFoundException e) {
			e.getMessage();
		}
		Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

		String encodedString = AppUtil.imageEncodeToBase64(selectedImage);

		UpdateUserAvatarOption updateUserAvatarOption = new UpdateUserAvatarOption();
		updateUserAvatarOption.setImage(encodedString);

		Call<Void> saveUserAvatar =
				RetrofitClient.getApiInterface(context).userUpdateAvatar(updateUserAvatarOption);

		saveUserAvatar.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

						if (response.code() == 204) {

							dialogEditAvatar.dismiss();
							getProfileDetail(username);
							Toasty.success(context, getString(R.string.profileUpdate));
							refProfile = true;
						} else {

							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
	}

	private void showEditProfileDialog() {

		View view = customEditProfileBinding.getRoot();
		materialAlertDialogBuilder.setView(view);

		customEditProfileBinding.save.setOnClickListener(
				saveKey ->
						saveUserProfile(
								String.valueOf(customEditProfileBinding.fullname.getText()),
								String.valueOf(customEditProfileBinding.description.getText()),
								String.valueOf(customEditProfileBinding.location.getText()),
								String.valueOf(customEditProfileBinding.website.getText()),
								customEditProfileBinding.hideEmail.isChecked(),
								customEditProfileBinding.hideActivity.isChecked()));

		dialogEditSettings = materialAlertDialogBuilder.show();

		getUserProfileSettings();
	}

	private void saveUserProfile(
			String fullname,
			String description,
			String location,
			String website,
			boolean hideEmail,
			boolean hideActivity) {

		UserSettingsOptions userSettings = new UserSettingsOptions();
		userSettings.setFullName(fullname);
		userSettings.setDescription(description);
		userSettings.setLocation(location);
		userSettings.setWebsite(website);
		userSettings.setHideEmail(hideEmail);
		userSettings.setHideActivity(hideActivity);

		Call<UserSettings> saveUserSettings =
				RetrofitClient.getApiInterface(context).customUpdateUserSettings(userSettings);

		saveUserSettings.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<UserSettings> call,
							@NonNull retrofit2.Response<UserSettings> response) {

						if (response.code() == 200) {

							dialogEditSettings.dismiss();
							getProfileDetail(username);
							Toasty.success(context, getString(R.string.profileUpdate));
							refProfile = true;
						} else {

							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<UserSettings> call, @NonNull Throwable t) {

						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
	}

	public void getUserProfileSettings() {

		Call<UserSettings> call1 = RetrofitClient.getApiInterface(context).customGetUserSettings();

		call1.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<UserSettings> call,
							@NonNull retrofit2.Response<UserSettings> response) {

						if (response.isSuccessful() && response.body() != null) {
							if (response.code() == 200) {

								if (!response.body().getFullName().isEmpty()) {
									customEditProfileBinding.fullname.setText(
											response.body().getFullName());
								}
								if (!response.body().getDescription().isEmpty()) {
									customEditProfileBinding.fullname.setText(
											response.body().getDescription());
								}
								if (!response.body().getLocation().isEmpty()) {
									customEditProfileBinding.fullname.setText(
											response.body().getLocation());
								}
								if (!response.body().getWebsite().isEmpty()) {
									customEditProfileBinding.fullname.setText(
											response.body().getWebsite());
								}
								customEditProfileBinding.hideEmail.setChecked(
										response.body().isHideEmail());
								customEditProfileBinding.hideActivity.setChecked(
										response.body().isHideActivity());
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<UserSettings> call, @NonNull Throwable t) {
						Toasty.error(
								context, context.getResources().getString(R.string.genericError));
					}
				});
	}

	public void getProfileDetail(String username) {

		Call<User> call = RetrofitClient.getApiInterface(context).userGet(username);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						if (response.isSuccessful() && response.body() != null) {

							switch (response.code()) {
								case 200:
									String username =
											!response.body().getFullName().isEmpty()
													? response.body().getFullName()
													: response.body().getLogin();
									String email =
											!response.body().getEmail().isEmpty()
													? response.body().getEmail()
													: "";

									binding.userFullName.setText(username);
									binding.userLogin.setText(
											getString(
													R.string.usernameWithAt,
													response.body().getLogin()));
									binding.userEmail.setText(email);

									binding.userFollowersCount.setText(
											String.format(
													response.body().getFollowersCount()
															+ " "
															+ getString(
																	R.string.profileTabFollowers)));
									binding.userFollowingCount.setText(
											String.format(
													response.body().getFollowingCount()
															+ " "
															+ getString(
																	R.string.profileTabFollowing)));
									binding.userStarredReposCount.setText(
											String.format(
													response.body().getStarredReposCount()
															+ " "
															+ getString(R.string.starredRepos)));

									String[] userLanguageCodes =
											response.body().getLanguage().split("-");

									if (userLanguageCodes.length >= 2) {
										Locale locale =
												new Locale(
														userLanguageCodes[0], userLanguageCodes[1]);
										binding.userLang.setText(locale.getDisplayLanguage());
									} else {
										binding.userLang.setText(locale.getDisplayLanguage());
									}

									PicassoService.getInstance(context)
											.get()
											.load(response.body().getAvatarUrl())
											.transform(new RoundedTransformation(imgRadius, 0))
											.placeholder(R.drawable.loader_animated)
											.resize(120, 120)
											.centerCrop()
											.into(binding.userAvatar);

									binding.userJoinedOn.setText(
											TimeHelper.formatTime(
													response.body().getCreated(), locale));
									binding.userJoinedOn.setOnClickListener(
											new ClickListener(
													TimeHelper.customDateFormatForToastDateFormat(
															response.body().getCreated()),
													context));
									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(context);
									break;

								case 403:
									Toasty.error(
											context, context.getString(R.string.authorizeError));
									break;

								default:
									Toasty.error(context, getString(R.string.genericError));
									break;
							}
						}
						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Toasty.error(
								context, context.getResources().getString(R.string.genericError));
					}
				});
	}

	public void getProfileRepository(String username) {

		Call<Repository> call =
				RetrofitClient.getApiInterface(context).repoGet(username, ".profile");

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Repository> call,
							@NonNull retrofit2.Response<Repository> response) {

						if (response.isSuccessful() && response.body() != null) {

							switch (response.code()) {
								case 200:
									String defBranch = response.body().getDefaultBranch();
									binding.profileRepoView.setVisibility(View.VISIBLE);

									Call<ResponseBody> call_profile =
											RetrofitClient.getWebInterface(getContext())
													.getFileContents(
															username,
															".profile",
															defBranch,
															"README.md");

									call_profile.enqueue(
											new Callback<>() {

												@Override
												public void onResponse(
														@NonNull Call<ResponseBody> call_profile,
														@NonNull retrofit2.Response<ResponseBody>
																		response) {

													if (isAdded()) {

														switch (response.code()) {
															case 200:
																assert response.body() != null;
																new Thread(
																				() -> {
																					try {
																						Markdown
																								.render(
																										context,
																										response.body()
																												.string(),
																										binding.profileRepoContent);
																					} catch (
																							IOException
																									e) {
																						requireActivity()
																								.runOnUiThread(
																										() ->
																												Toasty
																														.error(
																																context,
																																context
																																		.getString(
																																				R
																																						.string
																																						.genericError)));
																					}
																				})
																		.start();
																break;

															case 401:
																binding.profileRepoView
																		.setVisibility(View.GONE);
																AlertDialogs
																		.authorizationTokenRevokedDialog(
																				context);
																break;

															case 403:
																binding.profileRepoView
																		.setVisibility(View.GONE);
																Toasty.error(
																		context,
																		context.getString(
																				R.string
																						.authorizeError));
																break;

															default:
																break;
														}
													}
												}

												@Override
												public void onFailure(
														@NonNull Call<ResponseBody> call_profile,
														@NonNull Throwable t) {}
											});

									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(context);
									binding.profileRepoView.setVisibility(View.GONE);
									break;

								case 403:
									binding.profileRepoView.setVisibility(View.GONE);
									Toasty.error(
											context, context.getString(R.string.authorizeError));
									break;

								default:
									binding.profileRepoView.setVisibility(View.GONE);
									Toasty.error(context, getString(R.string.genericError));
									break;
							}
						}
						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {
						binding.profileRepoView.setVisibility(View.GONE);
					}
				});
	}
}
