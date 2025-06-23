package org.mian.gitnex.fragments;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsBackupRestoreActivity;
import org.mian.gitnex.activities.SettingsCodeEditorActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.databinding.BottomSheetAboutBinding;
import org.mian.gitnex.databinding.FragmentSettingsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author mmarif
 */
public class SettingsFragment extends Fragment {

	public static boolean refreshParent = false;
	private Context ctx;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		FragmentSettingsBinding fragmentSettingsBinding =
				FragmentSettingsBinding.inflate(inflater, container, false);

		ctx = getContext();
		assert ctx != null;

		fragmentSettingsBinding.notificationsFrame.setVisibility(View.VISIBLE);

		fragmentSettingsBinding.generalFrame.setOnClickListener(
				generalFrameCall -> startActivity(new Intent(ctx, SettingsGeneralActivity.class)));

		fragmentSettingsBinding.appearanceFrame.setOnClickListener(
				v1 -> startActivity(new Intent(ctx, SettingsAppearanceActivity.class)));

		fragmentSettingsBinding.codeEditorFrame.setOnClickListener(
				v1 -> startActivity(new Intent(ctx, SettingsCodeEditorActivity.class)));

		fragmentSettingsBinding.securityFrame.setOnClickListener(
				v1 -> startActivity(new Intent(ctx, SettingsSecurityActivity.class)));

		fragmentSettingsBinding.notificationsFrame.setOnClickListener(
				v1 -> startActivity(new Intent(ctx, SettingsNotificationsActivity.class)));

		fragmentSettingsBinding.backupData.setText(
				getString(
						R.string.backupRestore,
						getString(R.string.backup),
						getString(R.string.restore)));
		fragmentSettingsBinding.backupFrame.setOnClickListener(
				v1 -> startActivity(new Intent(ctx, SettingsBackupRestoreActivity.class)));

		fragmentSettingsBinding.rateAppFrame.setOnClickListener(rateApp -> rateThisApp());

		fragmentSettingsBinding.aboutAppFrame.setOnClickListener(
				aboutApp ->
						new AboutBottomSheetFragment()
								.show(getChildFragmentManager(), "AboutBottomSheet"));

		return fragmentSettingsBinding.getRoot();
	}

	public static class AboutBottomSheetFragment extends BottomSheetDialogFragment {

		private BottomSheetAboutBinding binding;

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			binding = BottomSheetAboutBinding.inflate(inflater, container, false);

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
						AppUtil.openUrlInBrowser(
								requireContext(), getString(R.string.appWebsiteLink));
						dismiss();
					});

			binding.feedback.setOnClickListener(
					v -> {
						AppUtil.openUrlInBrowser(
								requireContext(), getString(R.string.feedbackLink));
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

	public void rateThisApp() {

		try {
			startActivity(
					new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(
									"market://details?id=" + requireActivity().getPackageName())));
		} catch (ActivityNotFoundException e) {
			startActivity(
					new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(
									"https://play.google.com/store/apps/details?id="
											+ requireActivity().getPackageName())));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (refreshParent) {
			requireActivity().recreate();
			requireActivity().overridePendingTransition(0, 0);
			refreshParent = false;
		}
	}
}
