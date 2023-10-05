package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateOrgOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateOrganizationBinding;
import org.mian.gitnex.fragments.OrganizationsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class CreateOrganizationActivity extends BaseActivity {

	private ActivityCreateOrganizationBinding activityCreateOrganizationBinding;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityCreateOrganizationBinding =
				ActivityCreateOrganizationBinding.inflate(getLayoutInflater());
		setContentView(activityCreateOrganizationBinding.getRoot());

		MenuItem attachment = activityCreateOrganizationBinding.topAppBar.getMenu().getItem(0);
		MenuItem markdown = activityCreateOrganizationBinding.topAppBar.getMenu().getItem(1);
		attachment.setVisible(false);
		markdown.setVisible(false);

		activityCreateOrganizationBinding.topAppBar.setNavigationOnClickListener(v -> finish());

		activityCreateOrganizationBinding.newOrganizationDescription.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}
					return false;
				});

		activityCreateOrganizationBinding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processNewOrganization();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	private void processNewOrganization() {

		String newOrgName =
				Objects.requireNonNull(
								activityCreateOrganizationBinding.newOrganizationName.getText())
						.toString();
		String newOrgDesc =
				Objects.requireNonNull(
								activityCreateOrganizationBinding.newOrganizationDescription
										.getText())
						.toString();

		if (!newOrgDesc.equals("")) {

			if (newOrgDesc.length() > 255) {

				SnackBar.error(
						ctx, findViewById(android.R.id.content), getString(R.string.orgDescError));
				return;
			}
		}

		if (newOrgName.equals("")) {

			SnackBar.error(
					ctx, findViewById(android.R.id.content), getString(R.string.orgNameErrorEmpty));
		} else if (!AppUtil.checkStrings(newOrgName)) {

			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.orgNameErrorInvalid));
		} else {

			createNewOrganization(newOrgName, newOrgDesc);
		}
	}

	private void createNewOrganization(String orgName, String orgDesc) {

		CreateOrgOption createOrganization = new CreateOrgOption();
		createOrganization.setDescription(orgDesc);
		createOrganization.setUsername(orgName);

		Call<Organization> call = RetrofitClient.getApiInterface(ctx).orgCreate(createOrganization);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Organization> call,
							@NonNull retrofit2.Response<Organization> response) {

						if (response.code() == 201) {
							OrganizationsFragment.orgCreated = true;
							SnackBar.success(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.orgCreated));
							new Handler().postDelayed(() -> finish(), 3000);
						} else if (response.code() == 401) {

							AlertDialogs.authorizationTokenRevokedDialog(ctx);
						} else if (response.code() == 409) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.orgExistsError));
						} else if (response.code() == 422) {

							SnackBar.error(
									ctx,
									findViewById(android.R.id.content),
									getString(R.string.orgExistsError));
						} else {

							if (response.code() == 404) {

								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.apiNotFound));
							} else {

								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericError));
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {}
				});
	}
}
