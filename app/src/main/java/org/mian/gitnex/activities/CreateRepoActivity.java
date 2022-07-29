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
import org.gitnex.tea4j.v2.models.CreateRepoOption;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateRepoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
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

	private String selectedOwner;

	List<String> organizationsList = new ArrayList<>();

    //https://github.com/go-gitea/gitea/blob/52cfd2743c0e85b36081cf80a850e6a5901f1865/models/repo.go#L964-L967
    final List<String> reservedRepoNames = Arrays.asList(".", "..");
    final Pattern reservedRepoPatterns = Pattern.compile("\\.(git|wiki)$");

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateRepoBinding activityCreateRepoBinding = ActivityCreateRepoBinding.inflate(getLayoutInflater());
	    setContentView(activityCreateRepoBinding.getRoot());

        boolean connToInternet = AppUtil.hasNetworkConnection(ctx);

        loginUid = getAccount().getAccount().getUserName();

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
        getOrganizations(loginUid);

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

        String newRepoName = repoName.getText().toString();
        String newRepoDesc = repoDesc.getText().toString();
        boolean newRepoAccess = repoAccess.isChecked();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(!newRepoDesc.equals("")) {

            if (newRepoDesc.length() > 255) {

                Toasty.warning(ctx, getString(R.string.repoDescError));
                return;
            }
        }

        if(newRepoName.equals("")) {

            Toasty.error(ctx, getString(R.string.repoNameErrorEmpty));
        }
        else if(!AppUtil.checkStrings(newRepoName)) {

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
            createNewRepository(loginUid, newRepoName, newRepoDesc, selectedOwner, newRepoAccess);
        }
    }

    private void createNewRepository(String loginUid, String repoName, String repoDesc, String selectedOwner, boolean isPrivate) {

	    CreateRepoOption createRepository = new CreateRepoOption();
		createRepository.setAutoInit(true);
		createRepository.setDescription(repoDesc);
		createRepository.setPrivate(isPrivate);
		createRepository.setReadme("Default");
		createRepository.setName(repoName);

        Call<Repository> call;
        if(selectedOwner.equals(loginUid)) {

            call = RetrofitClient
                    .getApiInterface(ctx)
                    .createCurrentUserRepo(createRepository);
        }
        else {

            call = RetrofitClient
                    .getApiInterface(ctx)
                    .createOrgRepo(selectedOwner, createRepository);
        }

        call.enqueue(new Callback<Repository>() {

            @Override
            public void onResponse(@NonNull Call<Repository> call, @NonNull retrofit2.Response<Repository> response) {

                if(response.code() == 201) {

                    MainActivity.reloadRepos = true;
                    Toasty.success(ctx, getString(R.string.repoCreated));
                    enableProcessButton();
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx);
                }
                else if(response.code() == 409) {

                    enableProcessButton();
                    Toasty.warning(ctx, getString(R.string.repoExistsError));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });
    }

    private void getOrganizations(final String userLogin) {

        Call<List<Organization>> call = RetrofitClient
                .getApiInterface(ctx)
                .orgListCurrentUserOrgs(1, 50);

        call.enqueue(new Callback<List<Organization>>() {

            @Override
            public void onResponse(@NonNull Call<List<Organization>> call, @NonNull retrofit2.Response<List<Organization>> response) {

	            if(response.code() == 200) {

		            int organizationId = 0;

		            List<Organization> organizationsList_ = response.body();

		            organizationsList.add(userLogin);
		            assert organizationsList_ != null;

		            if(organizationsList_.size() > 0) {

			            for(int i = 0; i < organizationsList_.size(); i++) {

				            if(getIntent().getStringExtra("orgName") != null && !"".equals(getIntent().getStringExtra("orgName"))) {
					            if(getIntent().getStringExtra("orgName").equals(organizationsList_.get(i).getUsername())) {
						            organizationId = i + 1;
					            }
				            }

							organizationsList.add(organizationsList_.get(i).getUsername());
			            }
		            }

		            ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateRepoActivity.this, R.layout.list_spinner_items, organizationsList);

		            spinner.setAdapter(adapter);

		            spinner.setOnItemClickListener ((parent, view, position, id) -> selectedOwner = organizationsList.get(position));

		            if(getIntent().getBooleanExtra("organizationAction", false) && organizationId != 0) {

			            int selectOwnerById = organizationId;
			            new Handler(Looper.getMainLooper()).postDelayed(() -> {

				            spinner.setText(organizationsList.get(selectOwnerById), false);
				            selectedOwner = organizationsList.get(selectOwnerById);
			            }, 500);
			            getIntent().removeExtra("organizationAction");
		            }

		            enableProcessButton();
	            }

	            else if(response.code() == 401) {

		            enableProcessButton();
		            AlertDialogs.authorizationTokenRevokedDialog(ctx);
	            }
            }

            @Override
            public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {

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
