package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.CreateOrgOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateOrganizationBinding;
import org.mian.gitnex.fragments.OrganizationsFragment;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class CreateOrganizationActivity extends BaseActivity {

	public ImageView closeActivity;
	private View.OnClickListener onClickListener;
	private Button createOrganizationButton;

	private EditText orgName;
	private EditText orgDesc;
	private final View.OnClickListener createOrgListener = v -> processNewOrganization();

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityCreateOrganizationBinding activityCreateOrganizationBinding = ActivityCreateOrganizationBinding.inflate(getLayoutInflater());
		setContentView(activityCreateOrganizationBinding.getRoot());

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		closeActivity = activityCreateOrganizationBinding.close;
		orgName = activityCreateOrganizationBinding.newOrganizationName;
		orgDesc = activityCreateOrganizationBinding.newOrganizationDescription;

		orgName.requestFocus();
		assert imm != null;
		imm.showSoftInput(orgName, InputMethodManager.SHOW_IMPLICIT);

		orgDesc.setOnTouchListener((touchView, motionEvent) -> {

			touchView.getParent().requestDisallowInterceptTouchEvent(true);

			if((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

				touchView.getParent().requestDisallowInterceptTouchEvent(false);
			}
			return false;
		});

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		createOrganizationButton = activityCreateOrganizationBinding.createNewOrganizationButton;

		if(!connToInternet) {

			createOrganizationButton.setEnabled(false);
		}
		else {

			createOrganizationButton.setOnClickListener(createOrgListener);
		}

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private void processNewOrganization() {

		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		String newOrgName = orgName.getText().toString();
		String newOrgDesc = orgDesc.getText().toString();

		if(!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if(!newOrgDesc.equals("")) {

			if(newOrgDesc.length() > 255) {

				Toasty.warning(ctx, getString(R.string.orgDescError));
				return;
			}
		}

		if(newOrgName.equals("")) {

			Toasty.error(ctx, getString(R.string.orgNameErrorEmpty));
		}
		else if(!AppUtil.checkStrings(newOrgName)) {

			Toasty.warning(ctx, getString(R.string.orgNameErrorInvalid));
		}
		else {

			disableProcessButton();
			createNewOrganization(newOrgName, newOrgDesc);
		}

	}

	private void createNewOrganization(String orgName, String orgDesc) {

		CreateOrgOption createOrganization = new CreateOrgOption();
		createOrganization.setDescription(orgDesc);
		createOrganization.setUsername(orgName);

		Call<Organization> call = RetrofitClient.getApiInterface(ctx).orgCreate(createOrganization);

		call.enqueue(new Callback<Organization>() {

			@Override
			public void onResponse(@NonNull Call<Organization> call, @NonNull retrofit2.Response<Organization> response) {

				if(response.code() == 201) {
					OrganizationsFragment.orgCreated = true;
					enableProcessButton();
					Toasty.success(ctx, getString(R.string.orgCreated));
					finish();
				}
				else if(response.code() == 401) {

					enableProcessButton();
					AlertDialogs.authorizationTokenRevokedDialog(ctx);
				}
				else if(response.code() == 409) {

					enableProcessButton();
					Toasty.warning(ctx, getString(R.string.orgExistsError));
				}
				else if(response.code() == 422) {

					enableProcessButton();
					Toasty.warning(ctx, getString(R.string.orgExistsError));
				}
				else {

					if(response.code() == 404) {

						enableProcessButton();
						Toasty.warning(ctx, getString(R.string.apiNotFound));
					}
					else {

						enableProcessButton();
						Toasty.error(ctx, getString(R.string.genericError));
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}
		});

	}

	private void disableProcessButton() {

		createOrganizationButton.setEnabled(false);
	}

	private void enableProcessButton() {

		createOrganizationButton.setEnabled(true);
	}

}
