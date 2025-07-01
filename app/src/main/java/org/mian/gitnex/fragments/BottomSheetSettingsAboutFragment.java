package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomSheetSettingsAboutBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BottomSheetSettingsAboutFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsAboutBinding binding;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsAboutBinding.inflate(inflater, container, false);

		// Set app version and build
		binding.appVersionBuild.setText(
				getString(
						R.string.appVersionBuild,
						AppUtil.getAppVersion(requireContext()),
						AppUtil.getAppBuildNo(requireContext())));

		// Set server version
		binding.userServerVersion.setText(
				((BaseActivity) requireActivity()).getAccount().getServerVersion().toString());

		// Set up link click listeners
		binding.donationLinkPatreon.setOnClickListener(
				v -> {
					AppUtil.openUrlInBrowser(
							requireContext(), getString(R.string.supportLinkPatreon));
					dismiss();
				});

		binding.translateLink.setOnClickListener(
				v -> {
					AppUtil.openUrlInBrowser(requireContext(), getString(R.string.crowdInLink));
					dismiss();
				});

		binding.appWebsite.setOnClickListener(
				v -> {
					AppUtil.openUrlInBrowser(requireContext(), getString(R.string.appWebsiteLink));
					dismiss();
				});

		binding.feedback.setOnClickListener(
				v -> {
					AppUtil.openUrlInBrowser(requireContext(), getString(R.string.feedbackLink));
					dismiss();
				});

		// Hide donation link for pro users
		if (AppUtil.isPro(requireContext())) {
			binding.layoutFrame1.setVisibility(View.GONE);
		}

		return binding.getRoot();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // Prevent memory leaks
	}
}
