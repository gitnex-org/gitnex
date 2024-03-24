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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsBackupRestoreActivity;
import org.mian.gitnex.activities.SettingsCodeEditorActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.databinding.CustomAboutDialogBinding;
import org.mian.gitnex.databinding.FragmentSettingsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * @author M M Arif
 */
public class SettingsFragment extends Fragment {

	public static boolean refreshParent = false;

	private Context ctx;
	private MaterialAlertDialogBuilder materialAlertDialogBuilder;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		FragmentSettingsBinding fragmentSettingsBinding =
				FragmentSettingsBinding.inflate(inflater, container, false);

		ctx = getContext();
		assert ctx != null;
		materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(ctx, R.style.ThemeOverlay_Material3_Dialog_Alert);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.navSettings));

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

		fragmentSettingsBinding.aboutAppFrame.setOnClickListener(aboutApp -> showAboutAppDialog());

		fragmentSettingsBinding.navLogout.setOnClickListener(
				logout -> {
					AppUtil.logout(ctx);
					requireActivity()
							.overridePendingTransition(
									android.R.anim.fade_in, android.R.anim.fade_out);
				});

		return fragmentSettingsBinding.getRoot();
	}

	public void showAboutAppDialog() {

		CustomAboutDialogBinding aboutAppDialogBinding =
				CustomAboutDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = aboutAppDialogBinding.getRoot();

		materialAlertDialogBuilder.setView(view);

		aboutAppDialogBinding.appVersionBuild.setText(
				getString(
						R.string.appVersionBuild,
						AppUtil.getAppVersion(ctx),
						AppUtil.getAppBuildNo(ctx)));
		aboutAppDialogBinding.userServerVersion.setText(
				((BaseActivity) requireActivity()).getAccount().getServerVersion().toString());

		aboutAppDialogBinding.donationLinkPatreon.setOnClickListener(
				v12 ->
						AppUtil.openUrlInBrowser(
								requireContext(),
								getResources().getString(R.string.supportLinkPatreon)));

		aboutAppDialogBinding.donationLinkBuyMeaCoffee.setOnClickListener(
				v11 ->
						AppUtil.openUrlInBrowser(
								requireContext(),
								getResources().getString(R.string.supportLinkBuyMeaCoffee)));

		aboutAppDialogBinding.translateLink.setOnClickListener(
				v13 ->
						AppUtil.openUrlInBrowser(
								requireContext(), getResources().getString(R.string.crowdInLink)));

		aboutAppDialogBinding.appWebsite.setOnClickListener(
				v14 ->
						AppUtil.openUrlInBrowser(
								requireContext(),
								getResources().getString(R.string.appWebsiteLink)));

		aboutAppDialogBinding.feedback.setOnClickListener(
				v14 ->
						AppUtil.openUrlInBrowser(
								requireContext(), getResources().getString(R.string.feedbackLink)));

		if (AppUtil.isPro(requireContext())) {
			aboutAppDialogBinding.layoutFrame1.setVisibility(View.GONE);
		}

		materialAlertDialogBuilder.show();
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
