package org.mian.gitnex.activities;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.databinding.ActivityAddNewAccountBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.PathsHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UrlHelper;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.GiteaVersion;
import org.mian.gitnex.models.UserInfo;
import java.net.URI;
import io.mikael.urlbuilder.UrlBuilder;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class AddNewAccountActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private ActivityAddNewAccountBinding viewBinding;

	private enum Protocol {HTTPS, HTTP}
	private String spinnerSelectedValue;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_add_new_account;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityAddNewAccountBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		getWindow().getDecorView().setBackground(new ColorDrawable(Color.TRANSPARENT));

		initCloseListener();
		viewBinding.close.setOnClickListener(onClickListener);

		ArrayAdapter<Protocol> adapterProtocols = new ArrayAdapter<>(ctx, R.layout.list_spinner_items, Protocol.values());

		viewBinding.protocolSpinner.setAdapter(adapterProtocols);
		viewBinding.protocolSpinner.setOnItemClickListener((parent, view1, position, id) -> spinnerSelectedValue = String.valueOf(parent.getItemAtPosition(position)));
		viewBinding.addNewAccount.setOnClickListener(login -> {

			boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

			if(!connToInternet) {

				Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			}
			else {

				processLogin();
			}
		});

	}

	private void processLogin() {

		try {

			String instanceUrlET = String.valueOf(viewBinding.instanceUrl.getText());
			String loginToken = String.valueOf(viewBinding.loginToken.getText());
			String protocol = spinnerSelectedValue;

			if(protocol == null) {

				Toasty.error(ctx, getResources().getString(R.string.protocolEmptyError));
				return;
			}

			if(instanceUrlET.equals("")) {

				Toasty.error(ctx, getResources().getString(R.string.emptyFieldURL));
				return;
			}

			if(loginToken.equals("")) {

				Toasty.error(ctx, getResources().getString(R.string.loginTokenError));
				return;
			}

			URI rawInstanceUrl = UrlBuilder.fromString(UrlHelper.fixScheme(instanceUrlET, "http")).toUri();

			URI instanceUrl = UrlBuilder.fromUri(rawInstanceUrl).withScheme(protocol.toLowerCase()).withPath(PathsHelper.join(rawInstanceUrl.getPath(), "/api/v1/"))
				.toUri();

			versionCheck(instanceUrl.toString(), loginToken);

		}
		catch(Exception e) {

			Log.e("onFailure-login", e.toString());
			Toasty.error(ctx, getResources().getString(R.string.malformedUrl));
		}

	}

	private void versionCheck(final String instanceUrl, final String loginToken) {

		Call<GiteaVersion> callVersion;

		callVersion = RetrofitClient.getApiInterface(ctx).getGiteaVersionWithToken("token " + loginToken);

		callVersion.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull final Call<GiteaVersion> callVersion, @NonNull retrofit2.Response<GiteaVersion> responseVersion) {

				if(responseVersion.code() == 200) {

					GiteaVersion version = responseVersion.body();

					assert version != null;

					if(!Version.valid(version.getVersion())) {

						Toasty.error(ctx, getResources().getString(R.string.versionUnknown));
						return;
					}

					tinyDB.putString("giteaVersion", version.getVersion());
					Version giteaVersion = new Version(version.getVersion());

					if(giteaVersion.less(getString(R.string.versionLow))) {

						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ctx).setTitle(getString(R.string.versionAlertDialogHeader))
							.setMessage(getResources().getString(R.string.versionUnsupportedOld, version.getVersion())).setIcon(R.drawable.ic_warning)
							.setCancelable(true);

						alertDialogBuilder.setNegativeButton(getString(R.string.cancelButton), (dialog, which) -> {

							dialog.dismiss();
						});

						alertDialogBuilder.setPositiveButton(getString(R.string.textContinue), (dialog, which) -> {

							dialog.dismiss();
							login(instanceUrl, loginToken);
						});

						alertDialogBuilder.create().show();
					}
					else if(giteaVersion.lessOrEqual(getString(R.string.versionHigh))) {

						login(instanceUrl, loginToken);
					}
					else {

						Toasty.warning(ctx, getResources().getString(R.string.versionUnsupportedNew));
						login(instanceUrl, loginToken);
					}

				}
				else if(responseVersion.code() == 403) {

					login(instanceUrl, loginToken);
				}
			}

			private void login(String instanceUrl, String loginToken) {

				setupNewAccountWithToken(instanceUrl, loginToken);
			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> callVersion, @NonNull Throwable t) {

				Log.e("onFailure-versionCheck", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.errorOnLogin));
			}
		});
	}

	private void setupNewAccountWithToken(String instanceUrl, final String loginToken) {

		Call<UserInfo> call = RetrofitClient.getApiInterface(ctx, instanceUrl).getUserInfo("token " + loginToken);

		call.enqueue(new Callback<UserInfo>() {

			@Override
			public void onResponse(@NonNull Call<UserInfo> call, @NonNull retrofit2.Response<UserInfo> response) {

				UserInfo userDetails = response.body();

				switch(response.code()) {

					case 200:

						assert userDetails != null;
						// insert new account to db if does not exist
						String accountName = userDetails.getUsername() + "@" + instanceUrl;
						UserAccountsApi userAccountsApi = new UserAccountsApi(ctx);
						int checkAccount = userAccountsApi.getCount(accountName);

						if(checkAccount == 0) {

							userAccountsApi.insertNewAccount(accountName, instanceUrl, userDetails.getUsername(), loginToken, "");
							Toasty.success(ctx, getResources().getString(R.string.accountAddedMessage));
							finish();
						}
						else {

							Toasty.warning(ctx, getResources().getString(R.string.accountAlreadyExistsError));
						}
						break;

					case 401:

						Toasty.error(ctx, getResources().getString(R.string.unauthorizedApiError));
						break;

					default:

						Toasty.error(ctx, getResources().getString(R.string.genericApiStatusError) + response.code());
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserInfo> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				Toasty.error(ctx, getResources().getString(R.string.genericError));
			}
		});

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

}
