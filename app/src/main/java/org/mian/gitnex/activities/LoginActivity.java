package org.mian.gitnex.activities;

import static org.mian.gitnex.helpers.BackupUtil.checkpointIfWALEnabled;
import static org.mian.gitnex.helpers.BackupUtil.copyFile;
import static org.mian.gitnex.helpers.BackupUtil.getTempDir;
import static org.mian.gitnex.helpers.BackupUtil.unzip;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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
import org.mian.gitnex.databinding.BottomsheetManualVersionBinding;
import org.mian.gitnex.databinding.BottomsheetProxyAuthBinding;
import org.mian.gitnex.databinding.BottomsheetTokenHelpBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.structs.Protocol;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author mmarif
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
	private int btnText;
	private String selectedProvider = "gitea";
	private String proxyAuthUsername = null;
	private String proxyAuthPassword = null;
	private String manualVersion = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityLoginBinding = ActivityLoginBinding.inflate(getLayoutInflater());
		setContentView(activityLoginBinding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, null, activityLoginBinding.mainScrollView, null, activityLoginBinding.topRow);

		String mode = getIntent().getStringExtra("mode");
		if (mode == null) {
			mode = "login";
			btnText = R.string.btnLogin;
		} else if (mode.equals("update_account")) {
			btnText = R.string.update_account;
		} else {
			btnText = R.string.addNewAccountText;
		}

		if (mode.equals("new_account")) {
			activityLoginBinding.loginButton.setText(btnText);
			activityLoginBinding.restoreFromBackup.setVisibility(View.GONE);
		} else if (mode.equals("update_account")) {
			activityLoginBinding.loginButton.setText(btnText);
			activityLoginBinding.restoreFromBackup.setVisibility(View.GONE);

			int accountId = tinyDB.getInt("currentActiveAccountId", -1);
			if (accountId != -1) {
				UserAccountsApi userAccountsApi = BaseApi.getInstance(ctx, UserAccountsApi.class);

				if (userAccountsApi != null) {

					UserAccount account = userAccountsApi.getAccountById(accountId);
					if (account != null) {

						proxyAuthUsername = account.getProxyAuthUsername();
						proxyAuthPassword = account.getProxyAuthPassword();

						selectedProvider = account.getProvider();
						String[] providerOptions =
								getResources().getStringArray(R.array.provider_options);

						if ("gitea".equalsIgnoreCase(selectedProvider)) {
							activityLoginBinding.providerSpinner.setText(providerOptions[0], false);
						} else if ("forgejo".equalsIgnoreCase(selectedProvider)) {
							activityLoginBinding.providerSpinner.setText(providerOptions[1], false);
						} else {
							activityLoginBinding.providerSpinner.setText(providerOptions[2], false);
						}

						String url = account.getInstanceUrl();
						activityLoginBinding.instanceUrl.setText(
								UrlHelper.getCleanUrlForDisplay(url));

						try {
							URI uri = new URI(url);
							String scheme = uri.getScheme();
							selectedProtocol =
									(scheme != null && scheme.equalsIgnoreCase("http"))
											? Protocol.HTTP.toString()
											: Protocol.HTTPS.toString();
						} catch (Exception e) {
							selectedProtocol = Protocol.HTTPS.toString();
						}
						activityLoginBinding.httpsSpinner.setText(selectedProtocol, false);
					}
				}
			}
		} else {
			activityLoginBinding.loginButton.setText(btnText);
			activityLoginBinding.restoreFromBackup.setVisibility(View.VISIBLE);
		}

		activityLoginBinding.setupProxyAuth.setOnClickListener(view -> showProxyAuthSheet());

		NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.getInstance(ctx);

		activityLoginBinding.appVersion.setText(AppUtil.getAppVersion(appCtx));

		ArrayAdapter<Protocol> adapterProtocols =
				new ArrayAdapter<>(
						LoginActivity.this, R.layout.list_spinner_items, Protocol.values());

		ArrayAdapter<CharSequence> adapterProviders =
				ArrayAdapter.createFromResource(
						this, R.array.provider_options, R.layout.list_spinner_items);

		String instanceUrlExtra = getIntent().getStringExtra("instanceUrl");
		String scheme = getIntent().getStringExtra("scheme");
		if (instanceUrlExtra != null && !instanceUrlExtra.isEmpty()) {
			activityLoginBinding.instanceUrl.setText(instanceUrlExtra);
			if (scheme != null && scheme.equals("http")) {
				activityLoginBinding.httpsSpinner.setText(Protocol.HTTP.toString());
				selectedProtocol = Protocol.HTTP.toString();
			} else {
				activityLoginBinding.httpsSpinner.setText(Protocol.HTTPS.toString());
				selectedProtocol = Protocol.HTTPS.toString();
			}
		} else {
			activityLoginBinding.httpsSpinner.setText(Protocol.HTTPS.toString());
			selectedProtocol = Protocol.HTTPS.toString();
		}

		activityLoginBinding.httpsSpinner.setAdapter(adapterProtocols);
		activityLoginBinding.httpsSpinner.setSelection(0);
		activityLoginBinding.httpsSpinner.setOnItemClickListener(
				(parent, view, position, id) -> {
					selectedProtocol = String.valueOf(parent.getItemAtPosition(position));
					if (selectedProtocol.equals(String.valueOf(Protocol.HTTP))) {
						Toasty.show(ctx, getString(R.string.protocolError));
					}
				});

		activityLoginBinding.providerSpinner.setAdapter(adapterProviders);
		activityLoginBinding.providerSpinner.setSelection(0);
		activityLoginBinding.providerSpinner.setText(adapterProviders.getItem(0), false);
		activityLoginBinding.providerSpinner.setOnItemClickListener(
				(parent, view, position, id) ->
						selectedProvider =
								position == 0
										? "gitea"
										: position == 1 || position == 2 ? "forgejo" : "infer");

		if (AppUtil.hasNetworkConnection(ctx)) {
			enableProcessButton();
		} else {
			disableProcessButton();
		}

		activityLoginBinding.tokenHelper.setOnClickListener(token -> showTokenHelpSheet());

		networkStatusObserver.registerNetworkStatusListener(
				hasNetworkConnection ->
						runOnUiThread(
								() -> {
									if (hasNetworkConnection) {
										enableProcessButton();
									} else {
										disableProcessButton();
										activityLoginBinding.loginButton.setText(btnText);
										if (hasShownInitialNetworkError) {
											Toasty.show(
													ctx, getString(R.string.checkNetConnection));
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

	private void showProxyAuthSheet() {
		ProxyAuthSheet sheet = ProxyAuthSheet.newInstance(proxyAuthUsername, proxyAuthPassword);
		sheet.setCallback(
				(username, password) -> {
					this.proxyAuthUsername = username;
					this.proxyAuthPassword = password;
				});
		sheet.show(getSupportFragmentManager(), "ProxyAuthSheet");
	}

	public static class ProxyAuthSheet extends BottomSheetDialogFragment {
		private BottomsheetProxyAuthBinding binding;
		private String initialUser, initialPass;
		private ProxyAuthCallback callback;

		public interface ProxyAuthCallback {
			void onResult(String username, String password);
		}

		public static ProxyAuthSheet newInstance(String user, String pass) {
			ProxyAuthSheet fragment = new ProxyAuthSheet();
			fragment.initialUser = user;
			fragment.initialPass = pass;
			return fragment;
		}

		public void setCallback(ProxyAuthCallback callback) {
			this.callback = callback;
		}

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			binding = BottomsheetProxyAuthBinding.inflate(inflater, container, false);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			if (initialUser != null) binding.proxyUsername.setText(initialUser);
			if (initialPass != null) binding.proxyPassword.setText(initialPass);

			binding.btnSave.setOnClickListener(
					v -> {
						String u =
								binding.proxyUsername.getText() != null
										? binding.proxyUsername.getText().toString().trim()
										: "";
						String p =
								binding.proxyPassword.getText() != null
										? binding.proxyPassword.getText().toString().trim()
										: "";

						if (!u.isEmpty() && !p.isEmpty()) {
							if (callback != null) callback.onResult(u, p);
							Toasty.show(getContext(), getString(R.string.proxy_creds_saved));
							dismiss();
						} else if (u.isEmpty() && p.isEmpty()) {
							handleClear();
						} else {
							Toasty.show(getContext(), getString(R.string.proxy_creds_required_msg));
						}
					});

			binding.btnClear.setOnClickListener(v -> handleClear());
			binding.btnClose.setOnClickListener(v -> dismiss());
		}

		@Override
		public void onStart() {
			super.onStart();
			Dialog dialog = getDialog();
			if (dialog instanceof BottomSheetDialog) {
				AppUtil.applySheetStyle((BottomSheetDialog) dialog, false);
			}
		}

		private void handleClear() {
			if (callback != null) callback.onResult(null, null);
			Toasty.show(getContext(), getString(R.string.proxy_creds_cleared));
			dismiss();
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
			binding = null;
		}
	}

	private void showTokenHelpSheet() {
		TokenHelpSheet sheet = new TokenHelpSheet();
		sheet.show(getSupportFragmentManager(), "TokenHelpSheet");
	}

	public static class TokenHelpSheet extends BottomSheetDialogFragment {
		private BottomsheetTokenHelpBinding binding;

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			binding = BottomsheetTokenHelpBinding.inflate(inflater, container, false);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);
		}

		@Override
		public void onStart() {
			super.onStart();
			Dialog dialog = getDialog();
			if (dialog instanceof BottomSheetDialog) {
				AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
			}
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
			binding = null;
		}
	}

	private void login() {

		try {

			if (selectedProvider == null || selectedProvider.isEmpty()) {
				Toasty.show(ctx, getString(R.string.provider_empty_error));
				enableProcessButton();
				return;
			}

			if (selectedProtocol == null) {

				Toasty.show(ctx, getString(R.string.protocolEmptyError));
				enableProcessButton();
				return;
			}

			if (Objects.requireNonNull(activityLoginBinding.instanceUrl.getText())
					.toString()
					.isEmpty()) {

				Toasty.show(ctx, getString(R.string.emptyFieldURL));
				enableProcessButton();
				return;
			}

			String loginToken =
					Objects.requireNonNull(activityLoginBinding.loginTokenCode.getText())
							.toString()
							.replaceAll("[\\uFEFF|#]", "")
							.trim();

			if (loginToken.isEmpty()) {

				Toasty.show(ctx, getString(R.string.loginTokenError));
				enableProcessButton();
				return;
			}

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

			versionCheck(loginToken);
			serverPageLimitSettings(String.valueOf(instanceUrl), loginToken);

		} catch (Exception e) {

			Toasty.show(ctx, getString(R.string.malformedUrl));
			enableProcessButton();
		}
	}

	private void serverPageLimitSettings(String instanceUrl, String loginToken) {
		Call<GeneralAPISettings> generalAPISettings =
				RetrofitClient.getApiInterface(
								ctx,
								instanceUrl,
								"token " + loginToken,
								null,
								proxyAuthUsername,
								proxyAuthPassword)
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
								ctx,
								instanceUrl.toString(),
								"token " + loginToken,
								null,
								proxyAuthUsername,
								proxyAuthPassword)
						.getVersion();

		callVersion.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull final Call<ServerVersion> callVersion,
							@NonNull retrofit2.Response<ServerVersion> responseVersion) {
						if (responseVersion.code() == 200) {
							ServerVersion version = responseVersion.body();

							if (version != null && Version.valid(version.getVersion())) {
								giteaVersion = new Version(version.getVersion());
								manualVersion = null;
								proceedWithLogin(loginToken);
							} else {
								showManualVersionSheet(loginToken);
							}
						} else if (responseVersion.code() == 403) {
							giteaVersion = new Version("1.26.1");
							manualVersion = null;
							proceedWithLogin(loginToken);
						} else {
							showManualVersionSheet(loginToken);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> callVersion, @NonNull Throwable t) {
						showManualVersionSheet(loginToken);
					}
				});
	}

	private void showManualVersionSheet(String loginToken) {
		ManualVersionSheet sheet = ManualVersionSheet.newInstance("1.26.1");
		sheet.setCallback(
				version -> {
					manualVersion = version;
					giteaVersion = new Version(version);
					proceedWithLogin(loginToken);
				});
		sheet.show(getSupportFragmentManager(), "MANUAL_VERSION");
	}

	private void proceedWithLogin(String loginToken) {
		if (selectedProvider.equals("infer")) {
			selectedProvider = AppUtil.inferProvider(giteaVersion.toString());
		}

		if (giteaVersion.less(getString(R.string.versionLow))) {
			new MaterialAlertDialogBuilder(ctx)
					.setTitle(getString(R.string.versionAlertDialogHeader))
					.setMessage(
							getResources()
									.getString(
											R.string.versionUnsupportedOld,
											giteaVersion.toString()))
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
								setupUsingExistingToken(loginToken);
							})
					.create()
					.show();
		} else if (giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {
			setupUsingExistingToken(loginToken);
		} else {
			Toasty.show(ctx, getString(R.string.versionUnsupportedNew));
			setupUsingExistingToken(loginToken);
		}
	}

	private void setupUsingExistingToken(final String loginToken) {

		Call<User> call =
				RetrofitClient.getApiInterface(
								ctx,
								instanceUrl.toString(),
								"token " + loginToken,
								null,
								proxyAuthUsername,
								proxyAuthPassword)
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

								String accountName = userDetails.getLogin() + "@" + instanceUrl;
								UserAccountsApi userAccountsApi =
										BaseApi.getInstance(ctx, UserAccountsApi.class);
								assert userAccountsApi != null;
								boolean userAccountExists =
										userAccountsApi.userAccountExists(accountName);
								UserAccount account;
								if (!userAccountExists) {
									String versionToSave =
											manualVersion != null
													? manualVersion
													: giteaVersion.toString();
									long accountId =
											userAccountsApi.createNewAccount(
													accountName,
													instanceUrl.toString(),
													userDetails.getLogin(),
													loginToken,
													versionToSave,
													maxResponseItems,
													defaultPagingNumber,
													selectedProvider);

									userAccountsApi.updateProxyAuthCredentials(
											(int) accountId,
											(proxyAuthUsername != null
															&& !proxyAuthUsername.isEmpty()
															&& proxyAuthPassword != null
															&& !proxyAuthPassword.isEmpty())
													? proxyAuthUsername
													: null,
											(proxyAuthUsername != null
															&& !proxyAuthUsername.isEmpty()
															&& proxyAuthPassword != null
															&& !proxyAuthPassword.isEmpty())
													? proxyAuthPassword
													: null);

									account = userAccountsApi.getAccountById((int) accountId);
								} else {
									userAccountsApi.updateTokenByAccountName(
											accountName, loginToken);
									userAccountsApi.updateProvider(
											selectedProvider,
											userAccountsApi
													.getAccountByName(accountName)
													.getAccountId());

									UserAccount existingAccount =
											userAccountsApi.getAccountByName(accountName);
									userAccountsApi.updateProxyAuthCredentials(
											existingAccount.getAccountId(),
											(proxyAuthUsername != null
															&& !proxyAuthUsername.isEmpty()
															&& proxyAuthPassword != null
															&& !proxyAuthPassword.isEmpty())
													? proxyAuthUsername
													: null,
											(proxyAuthUsername != null
															&& !proxyAuthUsername.isEmpty()
															&& proxyAuthPassword != null
															&& !proxyAuthPassword.isEmpty())
													? proxyAuthPassword
													: null);

									userAccountsApi.login(
											userAccountsApi
													.getAccountByName(accountName)
													.getAccountId());
									account = userAccountsApi.getAccountByName(accountName);
								}

								AppUtil.switchToAccount(LoginActivity.this, account);

								Fragment sheet =
										getSupportFragmentManager()
												.findFragmentByTag("MANUAL_VERSION");
								if (sheet instanceof ManualVersionSheet) {
									((ManualVersionSheet) sheet).dismiss();
								}

								enableProcessButton();
								startActivity(new Intent(LoginActivity.this, MainActivity.class));
								finish();
								break;
							case 401:
								Fragment sheet401 =
										getSupportFragmentManager()
												.findFragmentByTag("MANUAL_VERSION");
								if (sheet401 instanceof ManualVersionSheet) {
									((ManualVersionSheet) sheet401).dismiss();
								}
								Toasty.show(ctx, getString(R.string.unauthorizedApiError));
								enableProcessButton();
								break;
							default:
								Fragment sheetDefault =
										getSupportFragmentManager()
												.findFragmentByTag("MANUAL_VERSION");
								if (sheetDefault instanceof ManualVersionSheet) {
									((ManualVersionSheet) sheetDefault).dismiss();
								}
								Toasty.show(
										ctx, getString(R.string.genericApiError, response.code()));
								enableProcessButton();
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

						Fragment sheet =
								getSupportFragmentManager().findFragmentByTag("MANUAL_VERSION");
						if (sheet instanceof ManualVersionSheet) {
							((ManualVersionSheet) sheet).dismiss();
						}
						Toasty.show(ctx, getString(R.string.genericServerResponseError));
						enableProcessButton();
					}
				});
	}

	private void disableProcessButton() {
		activityLoginBinding.loginButton.setText("");
		activityLoginBinding.loginButton.setEnabled(false);
		activityLoginBinding.loadingIndicator.setVisibility(View.VISIBLE);

		activityLoginBinding.setupProxyAuth.setEnabled(false);
		activityLoginBinding.restoreFromBackup.setEnabled(false);
	}

	private void enableProcessButton() {
		activityLoginBinding.loginButton.setText(btnText);
		activityLoginBinding.loginButton.setEnabled(true);
		activityLoginBinding.loadingIndicator.setVisibility(View.GONE);

		activityLoginBinding.setupProxyAuth.setEnabled(true);
		activityLoginBinding.restoreFromBackup.setEnabled(true);
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
								Toasty.show(ctx, getString(R.string.restoreError));
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
								Toasty.show(ctx, getString(R.string.restoreError));
							} finally {
								if (!exceptionOccurred) {

									runOnUiThread(this::restartApp);
								}
							}
						});

		restoreDatabaseThread.setDaemon(false);
		restoreDatabaseThread.start();
	}

	public static class ManualVersionSheet extends BottomSheetDialogFragment {

		private BottomsheetManualVersionBinding binding;
		private VersionCallback callback;
		private String suggestedVersion;

		public interface VersionCallback {
			void onVersionEntered(String version);
		}

		public static ManualVersionSheet newInstance(String suggestedVersion) {
			ManualVersionSheet fragment = new ManualVersionSheet();
			Bundle args = new Bundle();
			args.putString("suggestedVersion", suggestedVersion);
			fragment.setArguments(args);
			return fragment;
		}

		public void setCallback(VersionCallback callback) {
			this.callback = callback;
		}

		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			if (getArguments() != null) {
				suggestedVersion = getArguments().getString("suggestedVersion", "1.25.4");
			}
		}

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			binding = BottomsheetManualVersionBinding.inflate(inflater, container, false);
			return binding.getRoot();
		}

		@Override
		public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			binding.versionInput.setText(suggestedVersion);

			binding.btnClose.setOnClickListener(v -> dismiss());

			binding.btnSave.setOnClickListener(
					v -> {
						String version =
								binding.versionInput.getText() != null
										? binding.versionInput.getText().toString().trim()
										: "";

						if (Version.valid(version)) {
							binding.btnSave.setEnabled(false);
							binding.btnSave.setText("");
							binding.loadingIndicator.setVisibility(View.VISIBLE);

							if (callback != null) {
								callback.onVersionEntered(version);
							}
						} else {
							Toasty.show(requireContext(), R.string.invalid_version_format);
						}
					});
		}

		@Override
		public void onStart() {
			super.onStart();
			if (getDialog() instanceof BottomSheetDialog) {
				AppUtil.applySheetStyle((BottomSheetDialog) getDialog(), false);
			}
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
			binding = null;
		}
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
