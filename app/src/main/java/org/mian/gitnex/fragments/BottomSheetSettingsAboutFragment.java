package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomsheetSettingsAboutBinding;
import org.mian.gitnex.databinding.ItemSettingsMoreAppsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class BottomSheetSettingsAboutFragment extends BottomSheetDialogFragment {

	private BottomsheetSettingsAboutBinding binding;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetSettingsAboutBinding.inflate(inflater, container, false);

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

		setupMiniApp(binding.appLabNex, "LabNex", R.drawable.app_labnex, "https://labnex.app");
		setupMiniApp(
				binding.appOceanNex,
				"OceanNex",
				R.drawable.app_oceannex,
				"https://oceannex.swatian.com");
		setupMiniApp(
				binding.appNexNode,
				"NexNode",
				R.drawable.app_nexnode,
				"https://nexnode.swatian.com");

		if (AppUtil.isPro(requireContext())) {
			binding.donationLinkPatreon.setVisibility(View.GONE);
		}

		return binding.getRoot();
	}

	private void setupMiniApp(
			ItemSettingsMoreAppsBinding itemBinding, String name, int iconRes, String url) {
		itemBinding.appName.setText(name);
		itemBinding.appIcon.setImageResource(iconRes);
		itemBinding
				.getRoot()
				.setOnClickListener(v -> AppUtil.openUrlInBrowser(requireContext(), url));
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
