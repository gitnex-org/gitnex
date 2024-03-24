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
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.mikael.urlbuilder.UrlBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import okhttp3.Credentials;
import org.gitnex.tea4j.v2.models.AccessToken;
import org.gitnex.tea4j.v2.models.CreateAccessTokenOption;
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
	private String device_id = "token";
	private String selectedProtocol;
	private URI instanceUrl;
	private Version giteaVersion;
	private int maxResponseItems = 50;
	private int defaultPagingNumber = 25;
	private final String DATABASE_NAME = "gitnex";

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

		if (R.id.loginToken == activityLoginBinding.loginMethod.getCheckedRadioButtonId()) {
			AppUtil.setMultiVisibility(
					View.GONE,
					findViewById(R.id.login_uidLayout),
					findViewById(R.id.login_passwdLayout),
					findViewById(R.id.otpCodeLayout));
			findViewById(R.id.loginTokenCodeLayout).setVisibility(View.VISIBLE);
		} else {
			AppUtil.setMultiVisibility(
					View.VISIBLE,
					findViewById(R.id.login_uidLayout),
					findViewById(R.id.login_passwdLayout),
					findViewById(R.id.otpCodeLayout));
			findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
		}

		activityLoginBinding.loginMethod.setOnCheckedChangeListener(
				(group, checkedId) -> {
					if (checkedId == R.id.loginToken) {
						AppUtil.setMultiVisibility(
								View.GONE,
								findViewById(R.id.login_uidLayout),
								findViewById(R.id.login_passwdLayout),
								findViewById(R.id.otpCodeLayout));
						findViewById(R.id.loginTokenCodeLayout).setVisibility(View.VISIBLE);
					} else {
						AppUtil.setMultiVisibility(
								View.VISIBLE,
								findViewById(R.id.login_uidLayout),
								findViewById(R.id.login_passwdLayout),
								findViewById(R.id.otpCodeLayout));
						findViewById(R.id.loginTokenCodeLayout).setVisibility(View.GONE);
					}
				});

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
										SnackBar.error(
												ctx,
												findViewById(android.R.id.content),
												getString(R.string.checkNetConnection));
									}
								}));

		loadDefaults();

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

			String loginUid =
					Objects.requireNonNull(activityLoginBinding.loginUid.getText())
							.toString()
							.replaceAll("[\\uFEFF]", "")
							.trim();
			String loginPass =
					Objects.requireNonNull(activityLoginBinding.loginPasswd.getText())
							.toString()
							.trim();
			String loginToken =
					Objects.requireNonNull(activityLoginBinding.loginTokenCode.getText())
							.toString()
							.replaceAll("[\\uFEFF|#]", "")
							.trim();

			LoginType loginType =
					(activityLoginBinding.loginMethod.getCheckedRadioButtonId()
									== R.id.loginUsernamePassword)
							? LoginType.BASIC
							: LoginType.TOKEN;

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

			// cache values to make them available the next time the user wants to log in
			tinyDB.putString("loginType", loginType.name().toLowerCase());
			tinyDB.putString(
					"instanceUrlRaw", activityLoginBinding.instanceUrl.getText().toString());

			if (activityLoginBinding.instanceUrl.getText().toString().isEmpty()) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.emptyFieldURL));
				enableProcessButton();
				return;
			}

			if (loginType == LoginType.BASIC) {

				if (activityLoginBinding.otpCode.length() != 0
						&& activityLoginBinding.otpCode.length() != 6) {

					SnackBar.error(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.loginOTPTypeError));
					enableProcessButton();
					return;
				}

				if (loginUid.isEmpty()) {
					SnackBar.error(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.emptyFieldUsername));
					enableProcessButton();
					return;
				}

				if (loginPass.isEmpty()) {
					SnackBar.error(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.emptyFieldPassword));
					enableProcessButton();
					return;
				}

				int loginOTP =
						(activityLoginBinding.otpCode.length() > 0)
								? Integer.parseInt(
										Objects.requireNonNull(
														activityLoginBinding.otpCode.getText())
												.toString()
												.trim())
								: 0;

				versionCheck(loginUid, loginPass, loginOTP, loginToken, loginType);

			} else {

				if (loginToken.isEmpty()) {

					SnackBar.error(
							ctx,
							findViewById(android.R.id.content),
							getString(R.string.loginTokenError));
					enableProcessButton();
					return;
				}

				versionCheck(loginUid, loginPass, 123, loginToken, loginType);
				serverPageLimitSettings();
			}

		} catch (Exception e) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.malformedUrl));
			enableProcessButton();
		}
	}

	private void serverPageLimitSettings() {

		Call<GeneralAPISettings> generalAPISettings =
				RetrofitClient.getApiInterface(ctx).getGeneralAPISettings();
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

	private void versionCheck(
			final String loginUid,
			final String loginPass,
			final int loginOTP,
			final String loginToken,
			final LoginType loginType) {

		Call<ServerVersion> callVersion;

		if (!loginToken.isEmpty()) {

			callVersion =
					RetrofitClient.getApiInterface(
									ctx, instanceUrl.toString(), "token " + loginToken, null)
							.getVersion();
		} else {

			String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

			if (loginOTP != 0) {

				callVersion =
						RetrofitClient.getApiInterface(
										ctx, instanceUrl.toString(), credential, null)
								.getVersion(loginOTP);
			} else {

				callVersion =
						RetrofitClient.getApiInterface(
										ctx, instanceUrl.toString(), credential, null)
								.getVersion();
			}
		}

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
															login(
																	loginType,
																	loginUid,
																	loginPass,
																	loginOTP,
																	loginToken);
														});

								materialAlertDialogBuilder.create().show();
							} else if (giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {

								login(loginType, loginUid, loginPass, loginOTP, loginToken);
							} else {

								SnackBar.warning(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.versionUnsupportedNew));
								login(loginType, loginUid, loginPass, loginOTP, loginToken);
							}

						} else if (responseVersion.code() == 403) {

							login(loginType, loginUid, loginPass, loginOTP, loginToken);
						}
					}

					private void login(
							LoginType loginType,
							String loginUid,
							String loginPass,
							int loginOTP,
							String loginToken) {

						// ToDo: before store/create token: get UserInfo to check DB/AccountManager
						// if there already exist a token
						// the setup methods then can better handle all different cases

						if (loginType == LoginType.BASIC) {

							setup(loginUid, loginPass, loginOTP);
						} else if (loginType == LoginType.TOKEN) { // Token

							setupUsingExistingToken(loginToken);
						}
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

	private void setup(final String loginUid, final String loginPass, final int loginOTP) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);
		final String tokenName = "gitnex-app-" + device_id;

		Call<List<AccessToken>> call;
		if (loginOTP != 0) {

			call =
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential, null)
							.userGetTokens(loginOTP, loginUid, null, null);
		} else {

			call =
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential, null)
							.userGetTokens(loginUid, null, null);
		}

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<AccessToken>> call,
							@NonNull retrofit2.Response<List<AccessToken>> response) {

						List<AccessToken> userTokens = response.body();

						if (response.code() == 200) {

							assert userTokens != null;

							for (AccessToken t : userTokens) {

								if (t.getName().equals(tokenName)) {

									// this app had created an token on this instance before
									// -> since it looks like GitNex forgot the secret we have to
									// delete it first

									Call<Void> delToken;
									if (loginOTP != 0) {

										delToken =
												RetrofitClient.getApiInterface(
																ctx,
																instanceUrl.toString(),
																credential,
																null)
														.userDeleteAccessToken(
																loginOTP,
																loginUid,
																String.valueOf(t.getId()));
									} else {

										delToken =
												RetrofitClient.getApiInterface(
																ctx,
																instanceUrl.toString(),
																credential,
																null)
														.userDeleteAccessToken(
																loginUid,
																String.valueOf(t.getId()));
									}

									delToken.enqueue(
											new Callback<>() {

												@Override
												public void onResponse(
														@NonNull Call<Void> delToken,
														@NonNull retrofit2.Response<Void> response) {

													if (response.code() == 204) {

														setupToken(
																loginUid, loginPass, loginOTP,
																tokenName);
													} else {

														SnackBar.error(
																ctx,
																findViewById(android.R.id.content),
																getString(
																		R.string.genericApiError,
																		response.code()));
														enableProcessButton();
													}
												}

												@Override
												public void onFailure(
														@NonNull Call<Void> delToken,
														@NonNull Throwable t) {

													SnackBar.error(
															ctx,
															findViewById(android.R.id.content),
															getString(R.string.malformedJson));
													enableProcessButton();
												}
											});
									return;
								}
							}

							setupToken(loginUid, loginPass, loginOTP, tokenName);
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericApiError, response.code()));
							enableProcessButton();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<AccessToken>> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.malformedJson));
						enableProcessButton();
					}
				});
	}

	private void setupToken(
			final String loginUid,
			final String loginPass,
			final int loginOTP,
			final String tokenName) {

		final String credential = Credentials.basic(loginUid, loginPass, StandardCharsets.UTF_8);

		CreateAccessTokenOption createUserToken = new CreateAccessTokenOption().name(tokenName);
		if (giteaVersion.higherOrEqual("1.20.0")) {
			createUserToken.addScopesItem("all");
		} else if (giteaVersion.less("1.20.0") && (giteaVersion.higherOrEqual("1.19.0"))) {
			createUserToken.addScopesItem("all");
			createUserToken.addScopesItem("sudo");
		}
		Call<AccessToken> callCreateToken;

		if (loginOTP != 0) {

			callCreateToken =
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential, null)
							.userCreateToken(loginOTP, loginUid, createUserToken);
		} else {

			callCreateToken =
					RetrofitClient.getApiInterface(ctx, instanceUrl.toString(), credential, null)
							.userCreateToken(loginUid, createUserToken);
		}

		callCreateToken.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<AccessToken> callCreateToken,
							@NonNull retrofit2.Response<AccessToken> responseCreate) {

						if (responseCreate.code() == 201) {

							AccessToken newToken = responseCreate.body();
							assert newToken != null;

							if (!newToken.getSha1().isEmpty()) {

								Call<User> call =
										RetrofitClient.getApiInterface(
														ctx,
														instanceUrl.toString(),
														"token " + newToken.getSha1(),
														null)
												.userGetCurrent();

								call.enqueue(
										new Callback<>() {

											@Override
											public void onResponse(
													@NonNull Call<User> call,
													@NonNull retrofit2.Response<User> response) {

												User userDetails = response.body();

												switch (response.code()) {
													case 200:
														assert userDetails != null;

														// insert new account to db if does not
														// exist
														String accountName =
																userDetails.getLogin()
																		+ "@"
																		+ instanceUrl;
														UserAccountsApi userAccountsApi =
																BaseApi.getInstance(
																		ctx, UserAccountsApi.class);
														assert userAccountsApi != null;
														boolean userAccountExists =
																userAccountsApi.userAccountExists(
																		accountName);

														UserAccount account;
														if (!userAccountExists) {
															long accountId =
																	userAccountsApi
																			.createNewAccount(
																					accountName,
																					instanceUrl
																							.toString(),
																					userDetails
																							.getLogin(),
																					newToken
																							.getSha1(),
																					giteaVersion
																							.toString(),
																					maxResponseItems,
																					defaultPagingNumber);
															account =
																	userAccountsApi.getAccountById(
																			(int) accountId);
														} else {
															userAccountsApi
																	.updateTokenByAccountName(
																			accountName,
																			newToken.getSha1());
															account =
																	userAccountsApi
																			.getAccountByName(
																					accountName);
														}

														AppUtil.switchToAccount(
																LoginActivity.this, account);

														startActivity(
																new Intent(
																		LoginActivity.this,
																		MainActivity.class));
														finish();
														break;
													case 401:
														SnackBar.error(
																ctx,
																findViewById(android.R.id.content),
																getString(
																		R.string
																				.unauthorizedApiError));
														enableProcessButton();
														break;
													default:
														SnackBar.error(
																ctx,
																findViewById(android.R.id.content),
																getString(
																		R.string.genericApiError,
																		response.code()));
														enableProcessButton();
												}
											}

											@Override
											public void onFailure(
													@NonNull Call<User> call,
													@NonNull Throwable t) {

												SnackBar.error(
														ctx,
														findViewById(android.R.id.content),
														getString(R.string.genericError));
												enableProcessButton();
											}
										});
							}
						} else if (responseCreate.code() == 500) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericApiError, responseCreate.code()));
							enableProcessButton();
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<AccessToken> createUserToken, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadDefaults() {

		if (tinyDB.getString("loginType").equals(LoginType.BASIC.name().toLowerCase())) {

			activityLoginBinding.loginMethod.check(R.id.loginUsernamePassword);
		} else {

			activityLoginBinding.loginMethod.check(R.id.loginToken);
		}

		if (!tinyDB.getString("instanceUrlRaw").isEmpty()) {

			activityLoginBinding.instanceUrl.setText(tinyDB.getString("instanceUrlRaw"));
		}

		if (getAccount() != null && getAccount().getAccount() != null) {

			activityLoginBinding.loginUid.setText(getAccount().getAccount().getUserName());
		}

		if (!tinyDB.getString("uniqueAppId").isEmpty()) {
			device_id = tinyDB.getString("uniqueAppId");
		} else {

			device_id = UUID.randomUUID().toString();
			tinyDB.putString("uniqueAppId", device_id);
		}
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
