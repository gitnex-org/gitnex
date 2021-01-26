package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateRepoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.OrgOwner;
import org.mian.gitnex.models.OrganizationRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateRepoActivity extends BaseActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;
    private AutoCompleteTextView spinner;
    private Button createRepo;
    private EditText repoName;
    private EditText repoDesc;
    private CheckBox repoAccess;

	private String loginUid;
	private String userLogin;

	private String selectedOwner;

	List<OrgOwner> organizationsList = new ArrayList<>();

    //https://github.com/go-gitea/gitea/blob/52cfd2743c0e85b36081cf80a850e6a5901f1865/models/repo.go#L964-L967
    final List<String> reservedRepoNames = Arrays.asList(".", "..");
    final Pattern reservedRepoPatterns = Pattern.compile("\\.(git|wiki)$");

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_create_repo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateRepoBinding activityCreateRepoBinding = ActivityCreateRepoBinding.inflate(getLayoutInflater());

        boolean connToInternet = AppUtil.hasNetworkConnection(ctx);

        loginUid = tinyDB.getString("loginUid");
        userLogin = tinyDB.getString("userLogin");

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        closeActivity = activityCreateRepoBinding.close;
        repoName = activityCreateRepoBinding.newRepoName;
        repoDesc = activityCreateRepoBinding.newRepoDescription;
        repoAccess = activityCreateRepoBinding.newRepoPrivate;

        repoName.requestFocus();
        assert imm != null;
        imm.showSoftInput(repoName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        spinner = activityCreateRepoBinding.ownerSpinner;
        getOrganizations(Authorization.get(ctx), userLogin);

        createRepo = activityCreateRepoBinding.createNewRepoButton;
        disableProcessButton();

        if(!connToInternet) {

            disableProcessButton();
        }
        else {

            createRepo.setOnClickListener(createRepoListener);
        }
    }

    private final View.OnClickListener createRepoListener = v -> processNewRepo();

    private void processNewRepo() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        AppUtil appUtil = new AppUtil();

        String newRepoName = repoName.getText().toString();
        String newRepoDesc = repoDesc.getText().toString();
        boolean newRepoAccess = repoAccess.isChecked();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(!newRepoDesc.equals("")) {

            if (appUtil.charactersLength(newRepoDesc) > 255) {

                Toasty.warning(ctx, getString(R.string.repoDescError));
                return;
            }
        }

        if(newRepoName.equals("")) {

            Toasty.error(ctx, getString(R.string.repoNameErrorEmpty));
        }
        else if(!appUtil.checkStrings(newRepoName)) {

            Toasty.warning(ctx, getString(R.string.repoNameErrorInvalid));
        }
        else if (reservedRepoNames.contains(newRepoName)) {

            Toasty.warning(ctx, getString(R.string.repoNameErrorReservedName));
        }
        else if (reservedRepoPatterns.matcher(newRepoName).find()) {

            Toasty.warning(ctx, getString(R.string.repoNameErrorReservedPatterns));
        }
	    else if(selectedOwner == null) {

		    Toasty.error(ctx, getString(R.string.repoOwnerError));
        }
        else {

            disableProcessButton();
            createNewRepository(Authorization.get(ctx), loginUid, newRepoName, newRepoDesc, selectedOwner, newRepoAccess);
        }
    }

    private void createNewRepository(final String token, String loginUid, String repoName, String repoDesc, String selectedOwner, boolean isPrivate) {

        OrganizationRepository createRepository = new OrganizationRepository(true, repoDesc, null, null, repoName, isPrivate, "Default");

        Call<OrganizationRepository> call;
        if(selectedOwner.equals(loginUid)) {

            call = RetrofitClient
                    .getApiInterface(ctx)
                    .createNewUserRepository(token, createRepository);
        }
        else {

            call = RetrofitClient
                    .getApiInterface(ctx)
                    .createNewUserOrgRepository(token, selectedOwner, createRepository);
        }

        call.enqueue(new Callback<OrganizationRepository>() {

            @Override
            public void onResponse(@NonNull Call<OrganizationRepository> call, @NonNull retrofit2.Response<OrganizationRepository> response) {

                if(response.code() == 201) {

                    TinyDB tinyDb = TinyDB.getInstance(appCtx);
                    tinyDb.putBoolean("repoCreated", true);
                    Toasty.success(ctx, getString(R.string.repoCreated));
                    enableProcessButton();
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
                }
                else if(response.code() == 409) {

                    enableProcessButton();
                    Toasty.warning(ctx, getString(R.string.repoExistsError));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.repoCreatedError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<OrganizationRepository> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });
    }

    private void getOrganizations(String instanceToken, final String userLogin) {

        Call<List<OrgOwner>> call = RetrofitClient
                .getApiInterface(ctx)
                .getOrgOwners(instanceToken);

        call.enqueue(new Callback<List<OrgOwner>>() {

            @Override
            public void onResponse(@NonNull Call<List<OrgOwner>> call, @NonNull retrofit2.Response<List<OrgOwner>> response) {

	            if(response.code() == 200) {

		            int organizationId = 0;

		            List<OrgOwner> organizationsList_ = response.body();

		            organizationsList.add(new OrgOwner(userLogin));
		            assert organizationsList_ != null;

		            if(organizationsList_.size() > 0) {

			            for(int i = 0; i < organizationsList_.size(); i++) {

				            if(!tinyDB.getString("organizationId").isEmpty()) {

					            if(Integer.parseInt(tinyDB.getString("organizationId")) == organizationsList_.get(i).getId()) {
						            organizationId = i + 1;
					            }
				            }

				            OrgOwner data = new OrgOwner(organizationsList_.get(i).getUsername());
				            organizationsList.add(data);
			            }
		            }

		            ArrayAdapter<OrgOwner> adapter = new ArrayAdapter<>(CreateRepoActivity.this, R.layout.list_spinner_items, organizationsList);

		            spinner.setAdapter(adapter);

		            spinner.setOnItemClickListener ((parent, view, position, id) -> selectedOwner = organizationsList.get(position).getUsername());

		            if(tinyDB.getBoolean("organizationAction") & organizationId != 0) {

			            int selectOwnerById = organizationId;
			            new Handler(Looper.getMainLooper()).postDelayed(() -> {

				            spinner.setText(organizationsList.get(selectOwnerById).getUsername(), false);
				            selectedOwner = organizationsList.get(selectOwnerById).getUsername();
			            }, 500);

			            tinyDB.putBoolean("organizationAction", false);
		            }

		            enableProcessButton();
	            }

	            else if(response.code() == 401) {

		            enableProcessButton();
		            AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage),
			            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
	            }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrgOwner>> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });
    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {

        createRepo.setEnabled(false);
    }

    private void enableProcessButton() {

        createRepo.setEnabled(true);
    }

}
