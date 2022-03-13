package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsDraftsActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.databinding.CustomAboutDialogBinding;
import org.mian.gitnex.databinding.FragmentSettingsBinding;
import org.mian.gitnex.helpers.AppUtil;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	public static boolean refreshParent = false;

	private Context ctx;
	private Dialog aboutAppDialog;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		FragmentSettingsBinding fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

		ctx = getContext();
		aboutAppDialog = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navSettings));

		if(((BaseActivity) requireActivity()).getAccount().requiresVersion("1.12.3")) {

			fragmentSettingsBinding.notificationsFrame.setVisibility(View.VISIBLE);
		}

		fragmentSettingsBinding.generalFrame.setOnClickListener(generalFrameCall -> startActivity(new Intent(ctx, SettingsGeneralActivity.class)));

		fragmentSettingsBinding.appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsAppearanceActivity.class)));

		fragmentSettingsBinding.draftsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsDraftsActivity.class)));

		fragmentSettingsBinding.securityFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsSecurityActivity.class)));

		fragmentSettingsBinding.notificationsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsNotificationsActivity.class)));

		fragmentSettingsBinding.languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsTranslationActivity.class)));

		fragmentSettingsBinding.reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsReportsActivity.class)));

		fragmentSettingsBinding.rateAppFrame.setOnClickListener(rateApp -> rateThisApp());

		fragmentSettingsBinding.aboutAppFrame.setOnClickListener(aboutApp -> showAboutAppDialog());

		return fragmentSettingsBinding.getRoot();
	}

	public void showAboutAppDialog() {

		if (aboutAppDialog.getWindow() != null) {
			aboutAppDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		CustomAboutDialogBinding aboutAppDialogBinding = CustomAboutDialogBinding.inflate(LayoutInflater.from(ctx));
		View view = aboutAppDialogBinding.getRoot();
		aboutAppDialog.setContentView(view);

		aboutAppDialogBinding.appVersionBuild.setText(getString(R.string.appVersionBuild, AppUtil.getAppVersion(ctx), AppUtil.getAppBuildNo(ctx)));
		aboutAppDialogBinding.userServerVersion.setText(((BaseActivity) requireActivity()).getAccount().getServerVersion().toString());

		aboutAppDialogBinding.donationLinkPatreon.setOnClickListener(v12 -> {
			AppUtil.openUrlInBrowser(requireContext(), getResources().getString(R.string.supportLinkPatreon));
		});

		aboutAppDialogBinding.translateLink.setOnClickListener(v13 -> {
			AppUtil.openUrlInBrowser(requireContext(), getResources().getString(R.string.crowdInLink));
		});

		aboutAppDialogBinding.appWebsite.setOnClickListener(v14 -> {
			AppUtil.openUrlInBrowser(requireContext(), getResources().getString(R.string.appWebsiteLink));
		});

		if(AppUtil.isPro(requireContext())) {
			aboutAppDialogBinding.supportHeader.setVisibility(View.GONE);
			aboutAppDialogBinding.dividerSupport.setVisibility(View.GONE);
			aboutAppDialogBinding.donationLinkPatreon.setVisibility(View.GONE);
		}

		aboutAppDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		aboutAppDialog.show();
	}

	public void rateThisApp() {

		try {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + requireActivity().getPackageName())));
		}
		catch(ActivityNotFoundException e) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + requireActivity().getPackageName())));
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if(refreshParent) {
			requireActivity().recreate();
			requireActivity().overridePendingTransition(0, 0);
			refreshParent = false;
		}
	}
}
