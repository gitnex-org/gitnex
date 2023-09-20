package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateEmailOption;
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityAccountSettingsEmailBinding;
import org.mian.gitnex.fragments.AccountSettingsEmailsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private final View.OnClickListener addEmailListener = v -> processAddNewEmail();
	private ActivityAccountSettingsEmailBinding activityAccountSettingsEmailBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityAccountSettingsEmailBinding =
				ActivityAccountSettingsEmailBinding.inflate(getLayoutInflater());
		setContentView(activityAccountSettingsEmailBinding.getRoot());

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		activityAccountSettingsEmailBinding.userEmail.requestFocus();
		assert imm != null;
		imm.showSoftInput(
				activityAccountSettingsEmailBinding.userEmail, InputMethodManager.SHOW_IMPLICIT);

		initCloseListener();
		activityAccountSettingsEmailBinding.close.setOnClickListener(onClickListener);

		if (!connToInternet) {

			disableProcessButton();
		} else {

			activityAccountSettingsEmailBinding.addEmailButton.setOnClickListener(addEmailListener);
		}
	}

	private void processAddNewEmail() {

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String newUserEmail =
				Objects.requireNonNull(activityAccountSettingsEmailBinding.userEmail.getText())
						.toString()
						.trim();

		if (!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if (newUserEmail.equals("")) {

			Toasty.error(ctx, getString(R.string.emailErrorEmpty));
			return;
		} else if (!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

			Toasty.warning(ctx, getString(R.string.emailErrorInvalid));
			return;
		}

		List<String> newEmailList = new ArrayList<>(Arrays.asList(newUserEmail.split(",")));

		disableProcessButton();
		addNewEmail(newEmailList);
	}

	private void addNewEmail(List<String> newUserEmail) {

		CreateEmailOption addEmailFunc = new CreateEmailOption();
		addEmailFunc.setEmails(newUserEmail);

		Call<List<Email>> call = RetrofitClient.getApiInterface(ctx).userAddEmail(addEmailFunc);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Email>> call,
							@NonNull retrofit2.Response<List<Email>> response) {

						if (response.code() == 201) {

							Toasty.success(ctx, getString(R.string.emailAddedText));
							AccountSettingsEmailsFragment.refreshEmails = true;
							enableProcessButton();
							finish();
						} else if (response.code() == 401) {

							enableProcessButton();
							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							enableProcessButton();
							Toasty.error(ctx, ctx.getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							enableProcessButton();
							Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
						} else if (response.code() == 422) {

							enableProcessButton();
							Toasty.warning(ctx, ctx.getString(R.string.emailErrorInUse));
						} else {

							enableProcessButton();
							Toasty.error(ctx, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {

						Log.e("onFailure", t.toString());
						enableProcessButton();
					}
				});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void disableProcessButton() {

		activityAccountSettingsEmailBinding.addEmailButton.setEnabled(false);
	}

	private void enableProcessButton() {

		activityAccountSettingsEmailBinding.addEmailButton.setEnabled(true);
	}
}
