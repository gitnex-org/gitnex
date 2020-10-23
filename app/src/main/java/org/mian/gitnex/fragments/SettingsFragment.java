package org.mian.gitnex.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsDraftsActivity;
import org.mian.gitnex.activities.SettingsFileViewerActivity;
import org.mian.gitnex.activities.SettingsGeneralActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_settings, container, false);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navSettings));

		LinearLayout generalFrame = v.findViewById(R.id.generalFrame);
		LinearLayout appearanceFrame = v.findViewById(R.id.appearanceFrame);
		LinearLayout fileViewerFrame = v.findViewById(R.id.fileViewerFrame);
		LinearLayout draftsFrame = v.findViewById(R.id.draftsFrame);
		LinearLayout securityFrame = v.findViewById(R.id.securityFrame);
		LinearLayout languagesFrame = v.findViewById(R.id.languagesFrame);
		LinearLayout reportsFrame = v.findViewById(R.id.reportsFrame);
		LinearLayout rateAppFrame = v.findViewById(R.id.rateAppFrame);
		LinearLayout aboutAppFrame = v.findViewById(R.id.aboutAppFrame);

		generalFrame.setOnClickListener(generalFrameCall -> startActivity(new Intent(getContext(), SettingsGeneralActivity.class)));

		appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsAppearanceActivity.class)));

		fileViewerFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsFileViewerActivity.class)));

		draftsFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsDraftsActivity.class)));

		securityFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsSecurityActivity.class)));

		languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsTranslationActivity.class)));

		reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsReportsActivity.class)));

		rateAppFrame.setOnClickListener(aboutApp -> rateThisApp());

		aboutAppFrame.setOnClickListener(aboutApp -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit());

		return v;

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

		TinyDB tinyDb = new TinyDB(getContext());

		if(tinyDb.getBoolean("refreshParent")) {
			requireActivity().recreate();
			requireActivity().overridePendingTransition(0, 0);
			tinyDb.putBoolean("refreshParent", false);
		}

	}

}
