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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsDraftsActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsNotificationsActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.databinding.FragmentSettingsBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	private Context ctx;
	private TinyDB tinyDB;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		FragmentSettingsBinding fragmentSettingsBinding = FragmentSettingsBinding.inflate(inflater, container, false);

		ctx = getContext();
		tinyDB = TinyDB.getInstance(ctx);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navSettings));

		if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.3")) {

			fragmentSettingsBinding.notificationsFrame.setVisibility(View.VISIBLE);
		}

		fragmentSettingsBinding.generalFrame.setOnClickListener(generalFrameCall -> startActivity(new Intent(ctx, SettingsGeneralActivity.class)));

		fragmentSettingsBinding.appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsAppearanceActivity.class)));

		fragmentSettingsBinding.draftsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsDraftsActivity.class)));

		fragmentSettingsBinding.securityFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsSecurityActivity.class)));

		fragmentSettingsBinding.notificationsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsNotificationsActivity.class)));

		fragmentSettingsBinding.languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsTranslationActivity.class)));

		fragmentSettingsBinding.reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(ctx, SettingsReportsActivity.class)));

		fragmentSettingsBinding.rateAppFrame.setOnClickListener(aboutApp -> rateThisApp());

		fragmentSettingsBinding.aboutAppFrame.setOnClickListener(aboutApp -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit());

		return fragmentSettingsBinding.getRoot();

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

		if(tinyDB.getBoolean("refreshParent")) {

			requireActivity().recreate();
			requireActivity().overridePendingTransition(0, 0);
			tinyDB.putBoolean("refreshParent", false);
		}
	}

}
