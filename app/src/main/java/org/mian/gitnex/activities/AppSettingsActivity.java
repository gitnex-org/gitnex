package org.mian.gitnex.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import org.mian.gitnex.bottomsheets.SettingsAboutBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsAppearanceBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsBackupRestoreBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsCodeEditorBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsGeneralBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsNotificationsBottomSheet;
import org.mian.gitnex.bottomsheets.SettingsSecurityBottomSheet;
import org.mian.gitnex.databinding.ActivityAppSettingsBinding;
import org.mian.gitnex.databinding.ItemSettingsRowBinding;
import org.mian.gitnex.helpers.UIHelper;

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

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		getOnBackPressedDispatcher()
				.addCallback(
						this,
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								finish();
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
								new SettingsGeneralBottomSheet()
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
								new SettingsAppearanceBottomSheet()
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
								new SettingsCodeEditorBottomSheet()
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
								new SettingsSecurityBottomSheet()
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
								new SettingsNotificationsBottomSheet()
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
								new SettingsBackupRestoreBottomSheet()
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
								new SettingsAboutBottomSheet()
										.show(getSupportFragmentManager(), "About"));

		// rate GitNex row
		setupRow(
				binding.rowRateApp,
				R.drawable.ic_like,
				R.string.navRate,
				R.string.rateAppHintText,
				false);
		binding.rowRateApp.getRoot().setOnClickListener(v -> rateThisApp());

		binding.btnBack.setOnClickListener(v -> finish());
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

	@Override
	public void onResume() {
		super.onResume();
	}
}
