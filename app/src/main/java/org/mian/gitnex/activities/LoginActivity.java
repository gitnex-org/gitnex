package org.mian.gitnex.activities;

import static org.mian.gitnex.helpers.BackupUtil.checkpointIfWALEnabled;
import static org.mian.gitnex.helpers.BackupUtil.copyFile;
import static org.mian.gitnex.helpers.BackupUtil.getTempDir;
import static org.mian.gitnex.helpers.BackupUtil.unzip;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.mikael.urlbuilder.UrlBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.GeneralAPISettings;
import org.gitnex.tea4j.v2.models.ServerVersion;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.ActivityLoginBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.structs.Protocol;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class LoginActivity extends BaseActivity {

	private ActivityLoginBinding activityLoginBinding;
	private String selectedProtocol;
	private URI instanceUrl;
	private Version giteaVersion;
	private int maxResponseItems = 50;
	private int defaultPagingNumber = 25;
	private final String DATABASE_NAME = "gitnex";
	private boolean hasShownInitialNetworkError = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
		setContentView(activityLoginBinding.getRoot());

		NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.getInstance(ctx);

		activityLoginBinding.appVersion.setText(AppUtil.getAppVersion(appCtx));

		ArrayAdapter<Protocol> adapterProtocols =
				new ArrayAdapter<>(
						LoginActivity.this, R.layout.list_spinner_items, Protocol.values());

		activityLoginBinding.instanceUrl.setText(getIntent().getStringExtra("instanceUrl"));

		activityLoginBinding.httpsSpinner.setAdapter(adapterProtocols);
		activityLoginBinding.httpsSpinner.setSelection(0);
		activityLoginBinding.httpsSpinner.setOnItemClickListener(
				(parent, view, position, id) -> {
					selectedProtocol = String.valueOf(parent.getItemAtPosition(position));

					if (selectedProtocol.equals(String.valueOf(Protocol.HTTP))) {
						SnackBar.warning(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.protocolError));
					}
				});

		if (AppUtil.hasNetworkConnection(ctx)) {
			enableProcessButton();
		} else {
			disableProcessButton();
		}

		activityLoginBinding.tokenHelper.setOnClickListener(token -> showTokenHelpDialog());

		networkStatusObserver.registerNetworkStatusListener(
				hasNetworkConnection ->
						runOnUiThread(
								() -> {
									if (hasNetworkConnection) {
										enableProcessButton();
									} else {
										disableProcessButton();
										activityLoginBinding.loginButton.setText(
												getResources().getString(R.string.btnLogin));
										if (hasShownInitialNetworkError) {
											SnackBar.error(
													ctx,
													findViewById(android.R.id.content),
													getString(R.string.checkNetConnection));
										}
									}
									hasShownInitialNetworkError = true;
								}));

		activityLoginBinding.loginButton.setOnClickListener(
				view -> {
					disableProcessButton();
					login();
				});

		activityLoginBinding.restoreFromBackup.setOnClickListener(
				restoreDb -> {
					MaterialAlertDialogBuilder materialAlertDialogBuilder =
							new MaterialAlertDialogBuilder(ctx)
									.setTitle(R.string.restore)
									.setMessage(
											getResources()
													.getString(R.string.restoreFromBackupPopupText))
									.setNeutralButton(
											R.string.cancelButton,
											(dialog, which) -> dialog.dismiss())
									.setPositiveButton(
											R.string.restore,
											(dialog, which) -> requestRestoreFile());

					materialAlertDialogBuilder.create().show();
				});
	}

	private void showTokenHelpDialog() {

		MaterialAlertDialogBuilder dialogBuilder =
				new MaterialAlertDialogBuilder(this)
						.setMessage(
								HtmlCompat.fromHtml(
										getString(R.string.where_to_get_token_message),
										HtmlCompat.FROM_HTML_MODE_LEGACY))
						.setPositiveButton(R.string.close, null)
						.setCancelable(true);

		AlertDialog dialog = dialogBuilder.create();
		dialog.show();

		TextView messageView = dialog.findViewById(android.R.id.message);
		if (messageView != null) {
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
			int paddingTop =
					(int)
							TypedValue.applyDimension(
									TypedValue.COMPLEX_UNIT_DIP,
									16,
									getResources().getDisplayMetrics());
			messageView.setPadding(
					messageView.getPaddingLeft(),
					paddingTop,
					messageView.getPaddingRight(),
					messageView.getPaddingBottom());
		}
	}

	private void login() {

		try {

			if (selectedProtocol == null) {

				SnackBar.error(
						ctx,
						findViewById(android.R.id.content),
						getString(R.string.protocolEmptyError));
				enableProcessButton();
				return;
			}

			String loginToken =
					Objects.requireNonNull(activityLoginBinding.loginTokenCode.getText())
							.toString()
							.replaceAll("[\\uFEFF|#]", "")
							.trim();

			URI rawInstanceUrl =
					UrlBuilder.fromString(
									UrlHelper.fixScheme(
											Objects.requireNonNull(
															activityLoginBinding.instanceUrl
																	.getText())
													.toString()
													.replaceAll("[\\uFEFF|#]", "")
													.trim(),
											"http"))
							.toUri();

			instanceUrl =
					UrlBuilder.fromUri(rawInstanceUrl)
							.withScheme(selectedProtocol.toLowerCase())
							.withPath(PathsHelper.join(rawInstanceUrl.getPath(), "/api/v1/"))
							.toUri();

			if (activityLoginBinding.instanceUrl.getText().toString().isEmpty()) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.emptyFieldURL));
				enableProcessButton();
				return;
			}

			if (loginToken.isEmpty()) {

				SnackBar.error(
						ctx,
						findViewById(android.R.id.content),
						getString(R.string.loginTokenError));
				enableProcessButton();
				return;
			}

			versionCheck(loginToken);
			serverPageLimitSettings(String.valueOf(instanceUrl), loginToken);

		} catch (Exception e) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.malformedUrl));
			enableProcessButton();
		}
	}

	private void serverPageLimitSettings(String instanceUrl, String loginToken) {
		Call<GeneralAPISettings> generalAPISettings =
				RetrofitClient.getApiInterface(ctx, instanceUrl, "token " + loginToken, null)
						.getGeneralAPISettings();
		generalAPISettings.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<GeneralAPISettings> generalAPISettings,
							@NonNull retrofit2.Response<GeneralAPISettings> response) {

						if (response.code() == 200 && response.body() != null) {

							if (response.body().getMaxResponseItems() != null) {
								maxResponseItems =
										Math.toIntExact(response.body().getMaxResponseItems());
							}
							if (response.body().getDefaultPagingNum() != null) {
								defaultPagingNumber =
										Math.toIntExact(response.body().getDefaultPagingNum());
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<GeneralAPISettings> generalAPISettings,
							@NonNull Throwable t) {}
				});
	}

	private void versionCheck(final String loginToken) {

		Call<ServerVersion> callVersion =
				RetrofitClient.getApiInterface(
								ctx, instanceUrl.toString(), "token " + loginToken, null)
						.getVersion();

		callVersion.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull final Call<ServerVersion> callVersion,
							@NonNull retrofit2.Response<ServerVersion> responseVersion) {

						if (responseVersion.code() == 200) {

							ServerVersion version = responseVersion.body();
							assert version != null;

							if (!Version.valid(version.getVersion())) {

								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.versionUnknown));
								enableProcessButton();
								return;
							}

							giteaVersion = new Version(version.getVersion());

							if (giteaVersion.less(getString(R.string.versionLow))) {

								MaterialAlertDialogBuilder materialAlertDialogBuilder =
										new MaterialAlertDialogBuilder(ctx)
												.setTitle(
														getString(
																R.string.versionAlertDialogHeader))
												.setMessage(
														getResources()
																.getString(
																		R.string
																				.versionUnsupportedOld,
																		version.getVersion()))
												.setNeutralButton(
														getString(R.string.cancelButton),
														(dialog, which) -> {
															dialog.dismiss();
															enableProcessButton();
														})
												.setPositiveButton(
														getString(R.string.textContinue),
														(dialog, which) -> {
															dialog.dismiss();
															login(loginToken);
														});

								materialAlertDialogBuilder.create().show();
							} else if (giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {

								login(loginToken);
							} else {

								SnackBar.warning(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.versionUnsupportedNew));
								login(loginToken);
							}

						} else if (responseVersion.code() == 403) {

							login(loginToken);
						}
					}

					private void login(String loginToken) {

						setupUsingExistingToken(loginToken);
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> callVersion, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
						enableProcessButton();
					}
				});
	}

	private void setupUsingExistingToken(final String loginToken) {

		Call<User> call =
				RetrofitClient.getApiInterface(
								ctx, instanceUrl.toString(), "token " + loginToken, null)
						.userGetCurrent();

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						User userDetails = response.body();

						switch (response.code()) {
							case 200:
								assert userDetails != null;

								// insert new account to db if does not exist
								String accountName = userDetails.getLogin() + "@" + instanceUrl;
								UserAccountsApi userAccountsApi =
										BaseApi.getInstance(ctx, UserAccountsApi.class);
								assert userAccountsApi != null;
								boolean userAccountExists =
										userAccountsApi.userAccountExists(accountName);
								UserAccount account;
								if (!userAccountExists) {
									long accountId =
											userAccountsApi.createNewAccount(
													accountName,
													instanceUrl.toString(),
													userDetails.getLogin(),
													loginToken,
													giteaVersion.toString(),
													maxResponseItems,
													defaultPagingNumber);
									account = userAccountsApi.getAccountById((int) accountId);
								} else {
									userAccountsApi.updateTokenByAccountName(
											accountName, loginToken);
									userAccountsApi.login(
											userAccountsApi
													.getAccountByName(accountName)
													.getAccountId());
									account = userAccountsApi.getAccountByName(accountName);
								}

								AppUtil.switchToAccount(LoginActivity.this, account);

								enableProcessButton();
								startActivity(new Intent(LoginActivity.this, MainActivity.class));
								finish();
								break;
							case 401:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.unauthorizedApiError));
								enableProcessButton();
								break;
							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericApiError, response.code()));
								enableProcessButton();
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
						enableProcessButton();
					}
				});
	}

	private void disableProcessButton() {

		activityLoginBinding.loginButton.setText(R.string.processingText);
		activityLoginBinding.loginButton.setEnabled(false);
	}

	private void enableProcessButton() {

		activityLoginBinding.loginButton.setText(R.string.btnLogin);
		activityLoginBinding.loginButton.setEnabled(true);
	}

	private enum LoginType {
		BASIC,
		TOKEN
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
