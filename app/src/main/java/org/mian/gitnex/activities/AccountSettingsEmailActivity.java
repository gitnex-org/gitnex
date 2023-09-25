package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
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
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class AccountSettingsEmailActivity extends BaseActivity {

	private ActivityAccountSettingsEmailBinding activityAccountSettingsEmailBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityAccountSettingsEmailBinding =
				ActivityAccountSettingsEmailBinding.inflate(getLayoutInflater());
		setContentView(activityAccountSettingsEmailBinding.getRoot());

		activityAccountSettingsEmailBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		activityAccountSettingsEmailBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.save) {
						processAddNewEmail();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void processAddNewEmail() {

		String newUserEmail =
				Objects.requireNonNull(activityAccountSettingsEmailBinding.userEmail.getText())
						.toString()
						.trim();

		if (newUserEmail.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.emailErrorEmpty));
			return;
		} else if (!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.emailErrorInvalid));
			return;
		}

		List<String> newEmailList = new ArrayList<>(Arrays.asList(newUserEmail.split(",")));

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

							SnackBar.info(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.emailAddedText));
							AccountSettingsEmailsFragment.refreshEmails = true;
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 403) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.authorizeError));
						} else if (response.code() == 404) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.apiNotFound));
						} else if (response.code() == 422) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.emailErrorInUse));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {}
				});
	}
}
