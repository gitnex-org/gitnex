package org.mian.gitnex.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.User;
import org.gitnex.tea4j.v2.models.UserHeatmapData;
import org.gitnex.tea4j.v2.models.UserSettingsOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.adapters.HeatmapAdapter;
import org.mian.gitnex.databinding.BottomsheetEditProfileBinding;
import org.mian.gitnex.databinding.BottomsheetUpdateAvatarBinding;
import org.mian.gitnex.databinding.FragmentProfileDetailBinding;
import org.mian.gitnex.databinding.ItemProfileInfoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.UserProfileViewModel;

/**
 * @author mmarif
 */
public class DetailFragment extends Fragment {

	private static final String ARG_USERNAME = "username";
	private FragmentProfileDetailBinding binding;
	private UserProfileViewModel viewModel;
	private String username;
	private boolean itsMe = false;
	private static Uri avatarUri = null;
	private BottomSheetDialog editSheet;
	private boolean isFirstLoad = true;
	private boolean isViewReady = false;

	public DetailFragment() {}

	public static DetailFragment newInstance(String username) {
		DetailFragment fragment = new DetailFragment();
		Bundle args = new Bundle();
		args.putString(ARG_USERNAME, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.detailScrollView, null, null);
		isViewReady = true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) username = getArguments().getString(ARG_USERNAME);
		viewModel = new ViewModelProvider(requireActivity()).get(UserProfileViewModel.class);
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentProfileDetailBinding.inflate(inflater, container, false);
		setupClickListeners();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		if (viewModel.getUserProfile().getValue() == null) {
			viewModel.loadFullProfile(requireContext(), username);
		}
	}

	private void setupClickListeners() {
		binding.userFollowersCountLayout
				.getRoot()
				.setOnClickListener(
						v -> ((ProfileActivity) requireActivity()).switchTabFromFragment(4));
		binding.userFollowingCountLayout
				.getRoot()
				.setOnClickListener(
						v -> ((ProfileActivity) requireActivity()).switchTabFromFragment(5));
		binding.userStarsLayout
				.getRoot()
				.setOnClickListener(
						v -> ((ProfileActivity) requireActivity()).switchTabFromFragment(2));
		binding.updateProfile.setOnClickListener(v -> showEditProfile());
		binding.updateAvatar.setOnClickListener(v -> openFilePicker());
		binding.followUser.setOnClickListener(
				v -> viewModel.toggleFollow(requireContext(), username));
	}

	private void observeViewModel() {
		viewModel
				.getUserProfile()
				.observe(
						getViewLifecycleOwner(),
						user -> {
							if (user == null) return;
							checkOwnership(user.getLogin());
							populateUi(user);
						});

		viewModel.getHeatmapData().observe(getViewLifecycleOwner(), this::displayHeatmap);

		viewModel
				.getProfileReadme()
				.observe(
						getViewLifecycleOwner(),
						markdown -> {
							if (markdown != null) {
								binding.profileRepoView.setVisibility(View.VISIBLE);
								Markdown.render(
										requireContext(), markdown, binding.profileRepoContent);
							}
						});

		viewModel
				.getToastMessage()
				.observe(
						getViewLifecycleOwner(),
						message -> {
							if (message == null) return;
							switch (message) {
								case "PROFILE_UPDATED":
								case "AVATAR_UPDATED":
									Toasty.show(
											requireContext(), getString(R.string.profileUpdate));
									if (editSheet != null && editSheet.isShowing())
										editSheet.dismiss();
									break;
								case "USER_FOLLOWED":
									Toasty.show(
											requireContext(),
											getString(R.string.userFollowed, username));
									break;
								case "USER_UNFOLLOWED":
									Toasty.show(
											requireContext(),
											getString(R.string.userUnFollowed, username));
									break;
								default:
									Toasty.show(requireContext(), message);
									break;
							}
							viewModel.clearMessages();
						});

		viewModel
				.getIsFollowing()
				.observe(
						getViewLifecycleOwner(),
						following -> {
							if (itsMe) {
								binding.followUser.setVisibility(View.GONE);
								return;
							}
							binding.followUser.setVisibility(View.VISIBLE);
							updateFollowButtonUI(following);
						});

		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.followUser.setEnabled(!loading);
							binding.updateProfile.setEnabled(!loading);
							binding.followUser.setAlpha(loading ? 0.5f : 1.0f);
						});

		viewModel
				.getIsProfileLoading()
				.observe(
						getViewLifecycleOwner(),
						loading ->
								binding.expressiveLoader.setVisibility(
										loading ? View.VISIBLE : View.GONE));

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error == null) return;
							if (error.equals("AUTH_REVOKED")) {
								AlertDialogs.authorizationTokenRevokedDialog(requireContext());
							} else {
								Toasty.show(requireContext(), error);
							}
							viewModel.clearMessages();
						});
	}

	private void updateFollowButtonUI(boolean isFollowing) {
		binding.followUser.setText(isFollowing ? R.string.unfollowUser : R.string.userFollow);
		binding.followUser.setIconResource(
				isFollowing ? R.drawable.ic_person_remove : R.drawable.ic_person_add);
	}

	private void populateUi(User user) {
		binding.userFullName.setText(
				user.getFullName().isEmpty() ? user.getLogin() : user.getFullName());
		binding.userLogin.setText(getString(R.string.usernameWithAt, user.getLogin()));

		Glide.with(this)
				.load(user.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.centerCrop()
				.into(binding.userAvatar);

		binding.userFollowersCountLayout.statValue.setText(
				String.valueOf(user.getFollowersCount()));
		binding.userFollowersCountLayout.statLabel.setText(R.string.profileTabFollowers);
		binding.userFollowingCountLayout.statValue.setText(
				String.valueOf(user.getFollowingCount()));
		binding.userFollowingCountLayout.statLabel.setText(R.string.profileTabFollowing);
		binding.userStarsLayout.statValue.setText(String.valueOf(user.getStarredReposCount()));
		binding.userStarsLayout.statLabel.setText(R.string.pageTitleStarredRepos);

		boolean shouldHide =
				itsMe
						&& Boolean.parseBoolean(
								AppDatabaseSettings.getSettingsValue(
										requireContext(),
										AppDatabaseSettings
												.APP_USER_PROFILE_HIDE_EMAIL_LANGUAGE_KEY));

		setupInfoItem(
				binding.layoutEmail,
				R.drawable.ic_email,
				shouldHide ? getString(R.string.strPrivate).toUpperCase() : user.getEmail(),
				shouldHide);

		setupInfoItem(binding.layoutWebsite, R.drawable.ic_link, user.getWebsite(), false);
		setupInfoItem(binding.layoutLocation, R.drawable.ic_location, user.getLocation(), false);

		String langName = null;
		if (user.getLanguage() != null && !user.getLanguage().isEmpty()) {
			String[] codes = user.getLanguage().split("-");
			Locale userLoc =
					(codes.length >= 2) ? new Locale(codes[0], codes[1]) : Locale.getDefault();
			langName = userLoc.getDisplayLanguage();
		}
		setupInfoItem(
				binding.layoutLang,
				R.drawable.ic_language,
				shouldHide ? getString(R.string.strPrivate).toUpperCase() : langName,
				shouldHide);

		setupInfoItem(
				binding.layoutJoined,
				R.drawable.ic_calendar,
				TimeHelper.formatTime(user.getCreated(), Locale.getDefault()),
				false);
		binding.layoutJoined
				.getRoot()
				.setOnClickListener(
						v ->
								Toasty.show(
										requireContext(),
										TimeHelper.getFullDateTime(
												user.getCreated(), Locale.getDefault())));

		String bioText =
				user.getDescription().isEmpty()
						? getString(R.string.noDataBio)
						: user.getDescription();
		if (!user.getDescription().isEmpty()) {
			Markdown.render(requireContext(), bioText, binding.bio);
		} else {
			binding.bio.setText(bioText);
		}
	}

	private void setupInfoItem(
			ItemProfileInfoBinding item, int iconRes, String value, boolean isPrivate) {
		item.infoIcon.setImageResource(iconRes);
		boolean isEmpty = (value == null || value.isEmpty());
		item.infoText.setText(isEmpty ? "—" : value);
		item.getRoot().setAlpha(isPrivate || isEmpty ? 0.6f : 1.0f);
	}

	private void checkOwnership(String profileOwner) {
		String me = ((BaseActivity) requireActivity()).getAccount().getAccount().getUserName();
		itsMe = profileOwner.equals(me);
		binding.updateAvatar.setVisibility(itsMe ? View.VISIBLE : View.GONE);
		binding.updateProfile.setVisibility(itsMe ? View.VISIBLE : View.GONE);
		if (itsMe) binding.followUser.setVisibility(View.GONE);
	}

	private void displayHeatmap(List<UserHeatmapData> heatmapData) {
		if (heatmapData == null || heatmapData.isEmpty()) {
			binding.heatmapCard.setVisibility(View.GONE);
			return;
		}
		binding.heatmapCard.setVisibility(View.VISIBLE);
		int[] contributions = new int[60];
		LocalDate today = LocalDate.now(ZoneId.systemDefault());
		LocalDate startDate = today.minusDays(59);

		for (UserHeatmapData entry : heatmapData) {
			LocalDate entryDate =
					Instant.ofEpochSecond(entry.getTimestamp())
							.atZone(ZoneId.systemDefault())
							.toLocalDate();
			long dayIndex = ChronoUnit.DAYS.between(startDate, entryDate);
			if (dayIndex >= 0 && dayIndex < 60)
				contributions[(int) dayIndex] += entry.getContributions();
		}

		binding.heatmapGrid.setAdapter(new HeatmapAdapter(requireContext(), contributions));
		binding.heatmapGrid.setOnItemClickListener(
				(parent, view, position, id) -> {
					String date =
							startDate
									.plusDays(position)
									.format(
											DateTimeFormatter.ofPattern(
													"dd-MM-yyyy", Locale.getDefault()));
					Toasty.show(
							requireContext(),
							getString(
									R.string.heatmap_contribution, contributions[position], date));
				});
	}

	private void openFilePicker() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
		pickerLauncher.launch(Intent.createChooser(intent, getString(R.string.choose_image)));
	}

	private final ActivityResultLauncher<Intent> pickerLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							avatarUri = result.getData().getData();
							if (avatarUri != null) showUpdateAvatarDialog();
						}
					});

	private void showUpdateAvatarDialog() {

		BottomsheetUpdateAvatarBinding avatarBinding =
				BottomsheetUpdateAvatarBinding.inflate(getLayoutInflater());
		BottomSheetDialog avatarSheet = new BottomSheetDialog(requireContext());
		avatarSheet.setContentView(avatarBinding.getRoot());
		AppUtil.applySheetStyle(avatarSheet, false);

		Glide.with(this).load(avatarUri).centerCrop().into(avatarBinding.userAvatar);

		avatarBinding.btnClose.setOnClickListener(v -> avatarSheet.dismiss());

		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							avatarBinding.save.setEnabled(!loading);
							avatarBinding.save.setText(
									loading ? "" : getString(R.string.saveButton));
						});

		avatarBinding.save.setOnClickListener(
				v -> {
					try {
						InputStream is =
								requireContext().getContentResolver().openInputStream(avatarUri);
						viewModel.updateUserAvatar(
								requireContext(),
								AppUtil.imageEncodeToBase64(BitmapFactory.decodeStream(is)));
						avatarSheet.dismiss();
					} catch (Exception e) {
						Toasty.show(requireContext(), getString(R.string.genericError));
					}
				});
		avatarSheet.show();
	}

	private void updateProfileData(BottomsheetEditProfileBinding sheetBinding) {
		String name = Objects.requireNonNull(sheetBinding.fullname.getText()).toString().trim();
		String web = Objects.requireNonNull(sheetBinding.website.getText()).toString().trim();

		if (name.isEmpty()) {
			Toasty.show(requireContext(), R.string.emptyFieldName);
			return;
		}
		if (!web.isEmpty() && !android.util.Patterns.WEB_URL.matcher(web).matches()) {
			Toasty.show(requireContext(), R.string.userInvalidWebsite);
			return;
		}

		UserSettingsOptions options = new UserSettingsOptions();
		options.setFullName(name);
		options.setDescription(
				Objects.requireNonNull(sheetBinding.description.getText()).toString().trim());
		options.setLocation(
				Objects.requireNonNull(sheetBinding.location.getText()).toString().trim());
		options.setWebsite(web);
		options.setHideEmail(sheetBinding.hideEmail.isChecked());
		options.setHideActivity(sheetBinding.hideActivity.isChecked());

		viewModel.updateUserProfile(requireContext(), options);
	}

	private void showEditProfile() {
		viewModel.fetchUserSettings(requireContext());
		BottomsheetEditProfileBinding sheetBinding =
				BottomsheetEditProfileBinding.inflate(getLayoutInflater());
		editSheet = new BottomSheetDialog(requireContext());
		editSheet.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(editSheet, false);

		viewModel
				.getUserSettings()
				.observe(
						getViewLifecycleOwner(),
						settings -> {
							if (settings != null) {
								sheetBinding.fullname.setText(settings.getFullName());
								sheetBinding.description.setText(settings.getDescription());
								sheetBinding.location.setText(settings.getLocation());
								sheetBinding.website.setText(settings.getWebsite());
								sheetBinding.hideEmail.setChecked(settings.isHideEmail());
								sheetBinding.hideActivity.setChecked(settings.isHideActivity());
							}
						});

		viewModel
				.getIsActionLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							sheetBinding.save.setEnabled(!loading);
							sheetBinding.save.setText(
									loading ? "" : getString(R.string.saveButton));
							sheetBinding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
						});

		sheetBinding.btnClose.setOnClickListener(v -> editSheet.dismiss());
		sheetBinding.save.setOnClickListener(v -> updateProfileData(sheetBinding));
		editSheet.show();
	}
}
