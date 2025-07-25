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
import org.mian.gitnex.databinding.FragmentSettingsBinding;

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
				v1 ->
						new BottomSheetSettingsGeneralFragment()
								.show(getChildFragmentManager(), "BottomSheetSettingsGeneral"));

		fragmentSettingsBinding.appearanceFrame.setOnClickListener(
				v1 ->
						new BottomSheetSettingsAppearanceFragment()
								.show(getChildFragmentManager(), "BottomSheetSettingsAppearance"));

		fragmentSettingsBinding.codeEditorFrame.setOnClickListener(
				v1 ->
						new BottomSheetSettingsCodeEditorFragment()
								.show(getChildFragmentManager(), "BottomSheetSettingsCodeEditor"));

		fragmentSettingsBinding.securityFrame.setOnClickListener(
				v1 ->
						new BottomSheetSettingsSecurityFragment()
								.show(getChildFragmentManager(), "BottomSheetSettingsSecurity"));

		fragmentSettingsBinding.notificationsFrame.setOnClickListener(
				v1 ->
						new BottomSheetSettingsNotificationsFragment()
								.show(
										getChildFragmentManager(),
										"BottomSheetSettingsNotifications"));

		fragmentSettingsBinding.backupData.setText(
				getString(
						R.string.backupRestore,
						getString(R.string.backup),
						getString(R.string.restore)));
		fragmentSettingsBinding.backupFrame.setOnClickListener(
				v1 ->
						new BottomSheetSettingsBackupRestoreFragment()
								.show(
										getChildFragmentManager(),
										"BottomSheetSettingsBackupRestore"));

		fragmentSettingsBinding.rateAppFrame.setOnClickListener(rateApp -> rateThisApp());

		fragmentSettingsBinding.aboutAppFrame.setOnClickListener(
				aboutApp ->
						new BottomSheetSettingsAboutFragment()
								.show(getChildFragmentManager(), "AboutBottomSheet"));

		return fragmentSettingsBinding.getRoot();
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
