package org.mian.gitnex.activities;

import static org.mian.gitnex.helpers.BackupUtil.backupDatabaseFile;
import static org.mian.gitnex.helpers.BackupUtil.checkpointIfWALEnabled;
import static org.mian.gitnex.helpers.BackupUtil.copyFile;
import static org.mian.gitnex.helpers.BackupUtil.copyFileWithStreams;
import static org.mian.gitnex.helpers.BackupUtil.getTempDir;
import static org.mian.gitnex.helpers.BackupUtil.unzip;
import static org.mian.gitnex.helpers.BackupUtil.zip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivitySettingsBackupRestoreBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author M M Arif
 */
public class SettingsBackupRestoreActivity extends BaseActivity {

	private final String DATABASE_NAME = "gitnex";
	private String BACKUP_DATABASE_NAME;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsBackupRestoreBinding viewBinding =
				ActivitySettingsBackupRestoreBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());
		viewBinding.topAppBar.setTitle(
				getResources()
						.getString(
								R.string.backupRestore,
								getString(R.string.backup),
								getString(R.string.restore)));

		BACKUP_DATABASE_NAME = ctx.getString(R.string.appName) + "-" + LocalDate.now() + ".backup";

		viewBinding.backupDataFrame.setOnClickListener(
				v -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.backup)
									.setMessage(
											getResources().getString(R.string.backupFilePopupText))
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											R.string.backup,
											(dialog, which) -> requestBackupFileDownload());

					materialAlertDialogBuilder.create().show();
				});

		viewBinding.restoreDataFrame.setOnClickListener(
				restoreDb -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.restore)
									.setMessage(
											getResources().getString(R.string.restoreFilePopupText))
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											R.string.restore,
											(dialog, which) -> requestRestoreFile());

					materialAlertDialogBuilder.create().show();
				});
	}

	ActivityResultLauncher<Intent> activityBackupFileLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {

							assert result.getData() != null;

							Uri backupFileUri = result.getData().getData();
							backupDatabaseThread(backupFileUri);
						}
					});

	private void requestBackupFileDownload() {

		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, BACKUP_DATABASE_NAME);
		intent.setType("application/octet-stream");

		activityBackupFileLauncher.launch(intent);
	}

	private void backupDatabaseThread(Uri backupFileUri) {

		List<File> filesToZip = new ArrayList<>();

		Thread backupDatabaseThread =
				new Thread(
						() -> {
							File tempDir = getTempDir(ctx);

							try {

								checkpointIfWALEnabled(ctx, DATABASE_NAME);

								File databaseBackupFile =
										backupDatabaseFile(
												getDatabasePath(DATABASE_NAME).getPath(),
												tempDir.getPath() + "/" + DATABASE_NAME);

								filesToZip.add(databaseBackupFile);
								String tempZipFilename = "temp.backup";

								boolean zipFileStatus =
										zip(filesToZip, tempDir.getPath(), tempZipFilename);

								if (zipFileStatus) {

									File tempZipFile = new File(tempDir, tempZipFilename);
									Uri zipFileUri = Uri.fromFile(tempZipFile);

									InputStream inputStream =
											getContentResolver().openInputStream(zipFileUri);
									OutputStream outputStream =
											getContentResolver().openOutputStream(backupFileUri);

									boolean copySucceeded =
											copyFileWithStreams(inputStream, outputStream);

									SnackBar.success(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.backupFileSuccess));

									if (copySucceeded) {
										tempZipFile.delete();
									} else {
										SnackBar.error(
												ctx,
												findViewById(android.R.id.content),
												getString(R.string.backupFileError));
									}
								} else {
									SnackBar.error(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.backupFileError));
								}

							} catch (final Exception e) {
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.backupFileError));
							} finally {
								for (File file : filesToZip) {
									if (file != null && file.exists()) {
										file.delete();
									}
								}
							}
						});

		backupDatabaseThread.setDaemon(false);
		backupDatabaseThread.start();
	}

	private void requestRestoreFile() {

		Intent intentRestore = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intentRestore.addCategory(Intent.CATEGORY_OPENABLE);
		intentRestore.setType("*/*");
		String[] mimeTypes = {"application/octet-stream", "application/x-zip"};
		intentRestore.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

		activityRestoreFileLauncher.launch(intentRestore);
	}

	ActivityResultLauncher<Intent> activityRestoreFileLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {

							assert result.getData() != null;

							Uri restoreFileUri = result.getData().getData();
							assert restoreFileUri != null;

							try {
								InputStream inputStream =
										getContentResolver().openInputStream(restoreFileUri);
								restoreDatabaseThread(inputStream);
							} catch (FileNotFoundException e) {
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.restoreError));
							}
						}
					});

	private void restoreDatabaseThread(InputStream inputStream) {

		Thread restoreDatabaseThread =
				new Thread(
						() -> {
							boolean exceptionOccurred = false;

							try {

								String tempDir = getTempDir(ctx).getPath();

								unzip(inputStream, tempDir);
								checkpointIfWALEnabled(ctx, DATABASE_NAME);
								restoreDatabaseFile(ctx, tempDir, DATABASE_NAME);

								UserAccountsApi userAccountsApi =
										BaseApi.getInstance(ctx, UserAccountsApi.class);
								assert userAccountsApi != null;
								UserAccount account = userAccountsApi.getAccountById(1);
								AppUtil.switchToAccount(ctx, account);
							} catch (final Exception e) {

								exceptionOccurred = true;
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.restoreError));
							} finally {
								if (!exceptionOccurred) {

									runOnUiThread(this::restartApp);
								}
							}
						});

		restoreDatabaseThread.setDaemon(false);
		restoreDatabaseThread.start();
	}

	public void restoreDatabaseFile(Context context, String tempDir, String nameOfFileToRestore)
			throws IOException {

		File currentDbFile = new File(context.getDatabasePath(DATABASE_NAME).getPath());
		File newDbFile = new File(tempDir + "/" + nameOfFileToRestore);
		if (newDbFile.exists()) {
			copyFile(newDbFile, currentDbFile, false);
		}
	}

	public void restartApp() {
		Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
		assert i != null;
		startActivity(Intent.makeRestartActivityTask(i.getComponent()));
		Runtime.getRuntime().exit(0);
	}
}
