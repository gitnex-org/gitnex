package org.mian.gitnex.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import io.mikael.urlbuilder.UrlBuilder;
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
import org.mian.gitnex.databinding.ActivityAddNewAccountBinding;
import org.mian.gitnex.helpers.AppUtil;
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
public class AddNewAccountActivity extends BaseActivity {

	private ActivityAddNewAccountBinding viewBinding;

	private String spinnerSelectedValue;
	private Version giteaVersion;
	private int maxResponseItems = 50;
	private int defaultPagingNumber = 30;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityAddNewAccountBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

		viewBinding.instanceUrl.setText(getIntent().getStringExtra("instanceUrl"));
		viewBinding.loginToken.setText(getIntent().getStringExtra("token"));
		String scheme = getIntent().getStringExtra("scheme");
		if (scheme != null && scheme.equals("http")) {
			viewBinding.protocolSpinner.setText(Protocol.HTTP.toString());
			spinnerSelectedValue = Protocol.HTTP.toString();
		} else { // default is https
			viewBinding.protocolSpinner.setText(Protocol.HTTPS.toString());
			spinnerSelectedValue = Protocol.HTTPS.toString();
		}

		viewBinding.tokenHelper.setOnClickListener(token -> showTokenHelpDialog());

		ArrayAdapter<Protocol> adapterProtocols =
				new ArrayAdapter<>(ctx, R.layout.list_spinner_items, Protocol.values());

		viewBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		viewBinding.protocolSpinner.setAdapter(adapterProtocols);
		viewBinding.protocolSpinner.setOnItemClickListener(
				(parent, view1, position, id) ->
						spinnerSelectedValue = String.valueOf(parent.getItemAtPosition(position)));

		viewBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.addAccount) {
						processLogin();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
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

	private void processLogin() {

		try {

			String instanceUrlET =
					String.valueOf(viewBinding.instanceUrl.getText())
							.replaceAll("[\\uFEFF|#]", "")
							.trim();
			String loginToken =
					String.valueOf(viewBinding.loginToken.getText())
							.replaceAll("[\\uFEFF|#]", "")
							.trim();
			String protocol = spinnerSelectedValue;

			if (protocol == null) {

				SnackBar.error(
						ctx,
						findViewById(android.R.id.content),
						getString(R.string.protocolEmptyError));
				return;
			}

			if (instanceUrlET.isEmpty()) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.emptyFieldURL));
				return;
			}

			if (loginToken.isEmpty()) {

				SnackBar.error(
						ctx,
						findViewById(android.R.id.content),
						getString(R.string.loginTokenError));
				return;
			}

			URI rawInstanceUrl =
					UrlBuilder.fromString(UrlHelper.fixScheme(instanceUrlET, "http")).toUri();

			URI instanceUrl =
					UrlBuilder.fromUri(rawInstanceUrl)
							.withScheme(protocol.toLowerCase())
							.withPath(PathsHelper.join(rawInstanceUrl.getPath(), "/api/v1/"))
							.toUri();

			versionCheck(instanceUrl.toString(), loginToken);
			serverPageLimitSettings();

		} catch (Exception e) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.malformedUrl));
		}
	}

	private void versionCheck(final String instanceUrl, final String loginToken) {

		Call<ServerVersion> callVersion =
				RetrofitClient.getApiInterface(ctx, instanceUrl, "token " + loginToken, null)
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
														getString(R.string.cancelButton), null)
												.setPositiveButton(
														getString(R.string.textContinue),
														(dialog, which) -> {
															dialog.dismiss();
															login(instanceUrl, loginToken);
														});

								materialAlertDialogBuilder.create().show();
							} else if (giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {

								login(instanceUrl, loginToken);
							} else {

								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.versionUnsupportedNew));
								login(instanceUrl, loginToken);
							}
						} else if (responseVersion.code() == 401) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.unauthorizedApiError));
						} else if (responseVersion.code() == 403) {

							login(instanceUrl, loginToken);
						}
					}

					private void login(String instanceUrl, String loginToken) {

						setupNewAccountWithToken(instanceUrl, loginToken);
					}

					@Override
					public void onFailure(
							@NonNull Call<ServerVersion> callVersion, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
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

	private void setupNewAccountWithToken(String instanceUrl, final String loginToken) {

		Call<User> call =
				RetrofitClient.getApiInterface(ctx, instanceUrl, "token " + loginToken, null)
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
								boolean userAccountExists =
										Objects.requireNonNull(userAccountsApi)
												.userAccountExists(accountName);

								if (!userAccountExists) {

									long id =
											userAccountsApi.createNewAccount(
													accountName,
													instanceUrl,
													userDetails.getLogin(),
													loginToken,
													giteaVersion.toString(),
													maxResponseItems,
													defaultPagingNumber);
									UserAccount account = userAccountsApi.getAccountById((int) id);
									AppUtil.switchToAccount(AddNewAccountActivity.this, account);
									SnackBar.success(
											ctx,
											findViewById(android.R.id.content),
											getString(R.string.accountAddedMessage));
									MainActivity.refActivity = true;
									new Handler().postDelayed(() -> finish(), 3000);
								} else {
									UserAccount account =
											userAccountsApi.getAccountByName(accountName);
									if (account.isLoggedIn()) {
										SnackBar.error(
												ctx,
												findViewById(android.R.id.content),
												getString(R.string.accountAlreadyExistsError));
										AppUtil.switchToAccount(ctx, account);
									} else {
										userAccountsApi.updateTokenByAccountName(
												accountName, loginToken);
										userAccountsApi.login(account.getAccountId());
										AppUtil.switchToAccount(
												AddNewAccountActivity.this, account);
									}
								}
								break;

							case 401:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.unauthorizedApiError));
								break;

							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericApiError, response.code()));
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {

						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericError));
					}
				});
	}
}
