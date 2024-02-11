package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateUserOption;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateNewUserBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateNewUserActivity extends BaseActivity {

	private ActivityCreateNewUserBinding activityCreateNewUserBinding;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateNewUserBinding = ActivityCreateNewUserBinding.inflate(getLayoutInflater());
		setContentView(activityCreateNewUserBinding.getRoot());

		activityCreateNewUserBinding.topAppBar.setNavigationOnClickListener(
				v -> {
					finish();
				});

		MenuItem attachment = activityCreateNewUserBinding.topAppBar.getMenu().getItem(0);
		MenuItem markdown = activityCreateNewUserBinding.topAppBar.getMenu().getItem(1);
		attachment.setVisible(false);
		markdown.setVisible(false);

		activityCreateNewUserBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processCreateNewUser();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void processCreateNewUser() {

		String newFullName =
				Objects.requireNonNull(activityCreateNewUserBinding.fullName.getText())
						.toString()
						.trim();
		String newUserName =
				Objects.requireNonNull(activityCreateNewUserBinding.userUserName.getText())
						.toString()
						.trim();
		String newUserEmail =
				Objects.requireNonNull(activityCreateNewUserBinding.userEmail.getText())
						.toString()
						.trim();
		String newUserPassword =
				Objects.requireNonNull(activityCreateNewUserBinding.userPassword.getText())
						.toString();

		if (newFullName.equals("")
				|| newUserName.equals("") | newUserEmail.equals("")
				|| newUserPassword.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.emptyFields));
			return;
		}

		if (!AppUtil.checkStrings(newFullName)) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.userInvalidFullName));
			return;
		}

		if (!AppUtil.checkStringsWithAlphaNumeric(newUserName)) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.userInvalidUserName));
			return;
		}

		if (!Patterns.EMAIL_ADDRESS.matcher(newUserEmail).matches()) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.userInvalidEmail));
			return;
		}

		createNewUser(newFullName, newUserName, newUserEmail, newUserPassword);
	}

	private void createNewUser(
			String newFullName, String newUserName, String newUserEmail, String newUserPassword) {

		CreateUserOption createUser = new CreateUserOption();
		createUser.setEmail(newUserEmail);
		createUser.setFullName(newFullName);
		createUser.setUsername(newUserName);
		createUser.setPassword(newUserPassword);
		createUser.setMustChangePassword(true);

		Call<User> call = RetrofitClient.getApiInterface(ctx).adminCreateUser(createUser);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						if (response.code() == 201) {

							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.userCreatedText));
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

							SnackBar.warning(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.userExistsError));
						} else {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {}
				});
	}
}
