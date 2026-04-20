package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateOrgOption;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetCreateOrganizationBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetCreateOrganization extends BottomSheetDialogFragment {

	private BottomsheetCreateOrganizationBinding binding;
	private OrganizationsViewModel viewModel;

	public static BottomSheetCreateOrganization newInstance() {
		return new BottomSheetCreateOrganization();
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetCreateOrganizationBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(OrganizationsViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());
		binding.btnCreate.setOnClickListener(v -> validateAndCreate());

		binding.orgDescription.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		observeViewModel();
	}

	private void validateAndCreate() {
		String username = Objects.requireNonNull(binding.orgUsername.getText()).toString().trim();
		String fullName = Objects.requireNonNull(binding.orgFullName.getText()).toString().trim();
		String email = Objects.requireNonNull(binding.orgEmail.getText()).toString().trim();
		String desc = Objects.requireNonNull(binding.orgDescription.getText()).toString().trim();
		String location = Objects.requireNonNull(binding.orgLocation.getText()).toString().trim();
		String website = Objects.requireNonNull(binding.orgWebsite.getText()).toString().trim();

		if (username.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.orgNameErrorEmpty));
			return;
		}

		String visibility = "public";
		int checkedId = binding.visibilityGroup.getCheckedChipId();
		if (checkedId == R.id.chipLimited) visibility = "limited";
		else if (checkedId == R.id.chipPrivate) visibility = "private";

		CreateOrgOption option = new CreateOrgOption();
		option.setUsername(username);
		option.setFullName(fullName);
		option.setEmail(email);
		option.setDescription(desc);
		option.setLocation(location);
		option.setWebsite(website);
		option.setRepoAdminChangeTeamAccess(binding.switchAdminChangeTeam.isChecked());

		viewModel.createOrganization(requireContext(), option, visibility);
	}

	private void observeViewModel() {
		viewModel
				.getIsCreating()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.btnCreate.setEnabled(!loading);
							binding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							binding.btnCreate.setText(
									loading ? "" : getString(R.string.newCreateButtonCopy));
						});

		viewModel
				.getCreateSuccess()
				.observe(
						getViewLifecycleOwner(),
						success -> {
							if (success) {
								Toasty.show(requireContext(), getString(R.string.orgCreated));
								viewModel.resetCreateStatus();
								dismiss();
							}
						});

		viewModel
				.getCreateError()
				.observe(
						getViewLifecycleOwner(),
						errorMsg -> {
							if (errorMsg != null) {
								Toasty.show(requireContext(), errorMsg);
								viewModel.resetCreateStatus();
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, false);
		}
	}
}
