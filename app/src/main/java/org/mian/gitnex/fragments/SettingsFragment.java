package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.SettingsAppearanceActivity;
import org.mian.gitnex.activities.SettingsFileViewerActivity;
import org.mian.gitnex.activities.SettingsReportsActivity;
import org.mian.gitnex.activities.SettingsSecurityActivity;
import org.mian.gitnex.activities.SettingsTranslationActivity;
import org.mian.gitnex.util.TinyDB;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class SettingsFragment extends Fragment {

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_settings, container, false);

		LinearLayout appearanceFrame = v.findViewById(R.id.appearanceFrame);
		LinearLayout fileViewerFrame = v.findViewById(R.id.fileViewerFrame);
		LinearLayout securityFrame = v.findViewById(R.id.securityFrame);
		LinearLayout languagesFrame = v.findViewById(R.id.languagesFrame);
		LinearLayout reportsFrame = v.findViewById(R.id.reportsFrame);

		appearanceFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsAppearanceActivity.class)));

		fileViewerFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsFileViewerActivity.class)));

		securityFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsSecurityActivity.class)));

		languagesFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsTranslationActivity.class)));

		reportsFrame.setOnClickListener(v1 -> startActivity(new Intent(getContext(), SettingsReportsActivity.class)));

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();

		TinyDB tinyDb = new TinyDB(getContext());

		if(tinyDb.getBoolean("refreshParent")) {
			Objects.requireNonNull(getActivity()).recreate();
			getActivity().overridePendingTransition(0, 0);
			tinyDb.putBoolean("refreshParent", false);
		}

	}

}
