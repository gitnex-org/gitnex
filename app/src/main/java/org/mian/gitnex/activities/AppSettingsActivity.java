package org.mian.gitnex.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityAppSettingsBinding;
import org.mian.gitnex.databinding.ItemSettingsRowBinding;
import org.mian.gitnex.fragments.BottomSheetSettingsAboutFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsAppearanceFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsBackupRestoreFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsCodeEditorFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsGeneralFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsNotificationsFragment;
import org.mian.gitnex.fragments.BottomSheetSettingsSecurityFragment;

/**
 * @author mmarif
 */
public class AppSettingsActivity extends BaseActivity {

	private ActivityAppSettingsBinding binding;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAppSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		getOnBackPressedDispatcher()
				.addCallback(
						this,
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								finishWithTransition();
							}
						});

		initListeners();
	}

	private void initListeners() {

		// general row
		setupRow(
				binding.rowGeneral,
				R.drawable.ic_otp,
				R.string.settingsGeneralHeader,
				R.string.generalHintText,
				true);
		binding.rowGeneral
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsGeneralFragment()
										.show(getSupportFragmentManager(), "General"));

		// appearance row
		setupRow(
				binding.rowAppearance,
				R.drawable.ic_appearance,
				R.string.settingsAppearanceHeader,
				R.string.appearanceHintText,
				true);
		binding.rowAppearance
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsAppearanceFragment()
										.show(getSupportFragmentManager(), "Appearance"));

		// code editor row
		setupRow(
				binding.rowCodeEditor,
				R.drawable.ic_code_v2,
				R.string.codeEditor,
				R.string.codeEditorHintText,
				true);
		binding.rowCodeEditor
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsCodeEditorFragment()
										.show(getSupportFragmentManager(), "CodeEditor"));

		// security row
		setupRow(
				binding.rowSecurity,
				R.drawable.ic_security,
				R.string.settingsSecurityHeader,
				R.string.securityHintText,
				true);
		binding.rowSecurity
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsSecurityFragment()
										.show(getSupportFragmentManager(), "Security"));

		// notifications row
		setupRow(
				binding.rowNotifications,
				R.drawable.ic_notifications,
				R.string.pageTitleNotifications,
				R.string.notificationsHintText,
				true);
		binding.rowNotifications
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsNotificationsFragment()
										.show(getSupportFragmentManager(), "Notifications"));

		// backup row
		setupRow(
				binding.rowBackup,
				R.drawable.ic_export,
				R.string.backup,
				R.string.backupRestoreHintText,
				true);
		binding.rowBackup
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsBackupRestoreFragment()
										.show(getSupportFragmentManager(), "Backup"));

		// about row
		setupRow(
				binding.rowAbout,
				R.drawable.ic_info,
				R.string.navAbout,
				R.string.aboutAppHintText,
				true);
		binding.rowAbout
				.getRoot()
				.setOnClickListener(
						v ->
								new BottomSheetSettingsAboutFragment()
										.show(getSupportFragmentManager(), "About"));

		// rate GitNex row
		setupRow(
				binding.rowRateApp,
				R.drawable.ic_like,
				R.string.navRate,
				R.string.rateAppHintText,
				false);
		binding.rowRateApp.getRoot().setOnClickListener(v -> rateThisApp());

		binding.btnBack.setOnClickListener(v -> finishWithTransition());
	}

	private void setupRow(
			ItemSettingsRowBinding row, int icon, int title, int hint, boolean showChevron) {
		row.itemIcon.setImageResource(icon);
		row.itemTitle.setText(getString(title));
		row.itemHint.setText(getString(hint));
		row.itemChevron.setVisibility(showChevron ? View.VISIBLE : View.GONE);
	}

	public void rateThisApp() {
		try {
			startActivity(
					new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=" + getPackageName())));
		} catch (ActivityNotFoundException e) {
			startActivity(
					new Intent(
							Intent.ACTION_VIEW,
							Uri.parse(
									"https://play.google.com/store/apps/details?id="
											+ getPackageName())));
		}
	}

	private void finishWithTransition() {
		finish();
		overridePendingTransition(0, android.R.anim.fade_out);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}
