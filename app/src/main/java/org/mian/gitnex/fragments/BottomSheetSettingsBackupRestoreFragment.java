package org.mian.gitnex.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
import org.mian.gitnex.databinding.BottomSheetSettingsBackupRestoreBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.BackupUtil;
import org.mian.gitnex.helpers.SnackBar;

/**
 * @author mmarif
 */
public class BottomSheetSettingsBackupRestoreFragment extends BottomSheetDialogFragment {

	private BottomSheetSettingsBackupRestoreBinding binding;
	private Context ctx;
	private final String DATABASE_NAME = "gitnex";
	private String BACKUP_DATABASE_NAME;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomSheetSettingsBackupRestoreBinding.inflate(inflater, container, false);
		ctx = requireContext();

		BACKUP_DATABASE_NAME = ctx.getString(R.string.appName) + "-" + LocalDate.now() + ".backup";

		binding.backupButton.setOnClickListener(v -> requestBackupFileDownload());
		binding.restoreButton.setOnClickListener(v -> requestRestoreFile());

		binding.bottomSheetHeader.setText(
				getString(
						R.string.backupRestore,
						getString(R.string.backup),
						getString(R.string.restore)));

		return binding.getRoot();
	}

	private final ActivityResultLauncher<Intent> activityBackupFileLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
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
							File tempDir = BackupUtil.getTempDir(ctx);
							try {
								BackupUtil.checkpointIfWALEnabled(ctx, DATABASE_NAME);
								File databaseBackupFile =
										BackupUtil.backupDatabaseFile(
												ctx.getDatabasePath(DATABASE_NAME).getPath(),
												tempDir.getPath() + "/" + DATABASE_NAME);
								filesToZip.add(databaseBackupFile);
								String tempZipFilename = "temp.backup";
								boolean zipFileStatus =
										BackupUtil.zip(
												filesToZip, tempDir.getPath(), tempZipFilename);
								if (zipFileStatus) {
									File tempZipFile = new File(tempDir, tempZipFilename);
									Uri zipFileUri = Uri.fromFile(tempZipFile);
									InputStream inputStream =
											ctx.getContentResolver().openInputStream(zipFileUri);
									OutputStream outputStream =
											ctx.getContentResolver()
													.openOutputStream(backupFileUri);
									boolean copySucceeded =
											BackupUtil.copyFileWithStreams(
													inputStream, outputStream);
									requireActivity()
											.runOnUiThread(
													() -> {
														if (copySucceeded) {
															SnackBar.success(
																	ctx,
																	requireActivity()
																			.findViewById(
																					android.R.id
																							.content),
																	getString(
																			R.string
																					.backupFileSuccess));
														} else {
															SnackBar.error(
																	ctx,
																	requireActivity()
																			.findViewById(
																					android.R.id
																							.content),
																	getString(
																			R.string
																					.backupFileError));
														}
													});
									if (copySucceeded) {
										tempZipFile.delete();
									}
								} else {
									requireActivity()
											.runOnUiThread(
													() ->
															SnackBar.error(
																	ctx,
																	requireActivity()
																			.findViewById(
																					android.R.id
																							.content),
																	getString(
																			R.string
																					.backupFileError)));
								}
							} catch (Exception e) {
								requireActivity()
										.runOnUiThread(
												() ->
														SnackBar.error(
																ctx,
																requireActivity()
																		.findViewById(
																				android.R.id
																						.content),
																getString(
																		R.string.backupFileError)));
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

	private final ActivityResultLauncher<Intent> activityRestoreFileLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							Uri restoreFileUri = result.getData().getData();
							try {
								assert restoreFileUri != null;
								InputStream inputStream =
										ctx.getContentResolver().openInputStream(restoreFileUri);
								restoreDatabaseThread(inputStream);
							} catch (FileNotFoundException e) {
								SnackBar.error(
										ctx,
										requireActivity().findViewById(android.R.id.content),
										getString(R.string.restoreError));
							}
						}
					});

	private void requestRestoreFile() {
		Intent intentRestore = new Intent(Intent.ACTION_OPEN_DOCUMENT);
		intentRestore.addCategory(Intent.CATEGORY_OPENABLE);
		intentRestore.setType("*/*");
		String[] mimeTypes = {"application/octet-stream", "application/x-zip"};
		intentRestore.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
		activityRestoreFileLauncher.launch(intentRestore);
	}

	private void restoreDatabaseThread(InputStream inputStream) {
		Thread restoreDatabaseThread =
				new Thread(
						() -> {
							boolean exceptionOccurred = false;
							try {
								String tempDir = BackupUtil.getTempDir(ctx).getPath();
								BackupUtil.unzip(inputStream, tempDir);
								BackupUtil.checkpointIfWALEnabled(ctx, DATABASE_NAME);
								restoreDatabaseFile(ctx, tempDir, DATABASE_NAME);
								UserAccountsApi userAccountsApi =
										BaseApi.getInstance(ctx, UserAccountsApi.class);
								assert userAccountsApi != null;
								UserAccount account = userAccountsApi.getAccountById(1);
								AppUtil.switchToAccount(ctx, account);
							} catch (Exception e) {
								exceptionOccurred = true;
								requireActivity()
										.runOnUiThread(
												() ->
														SnackBar.error(
																ctx,
																requireActivity()
																		.findViewById(
																				android.R.id
																						.content),
																getString(R.string.restoreError)));
							} finally {
								if (!exceptionOccurred) {
									requireActivity().runOnUiThread(this::restartApp);
								}
							}
						});
		restoreDatabaseThread.setDaemon(false);
		restoreDatabaseThread.start();
	}

	private void restoreDatabaseFile(Context context, String tempDir, String nameOfFileToRestore)
			throws IOException {
		File currentDbFile = new File(context.getDatabasePath(DATABASE_NAME).getPath());
		File newDbFile = new File(tempDir + "/" + nameOfFileToRestore);
		if (newDbFile.exists()) {
			BackupUtil.copyFile(newDbFile, currentDbFile, false);
		}
	}

	private void restartApp() {
		Intent i = ctx.getPackageManager().getLaunchIntentForPackage(ctx.getPackageName());
		assert i != null;
		startActivity(Intent.makeRestartActivityTask(i.getComponent()));
		Runtime.getRuntime().exit(0);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null; // Prevent memory leaks
	}
}
