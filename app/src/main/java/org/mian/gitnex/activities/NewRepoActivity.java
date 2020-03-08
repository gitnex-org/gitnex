package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.OrgOwner;
import org.mian.gitnex.models.OrganizationRepository;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class NewRepoActivity extends BaseActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;
    private Spinner spinner;
    private Button createRepo;
    private EditText repoName;
    private EditText repoDesc;
    private CheckBox repoAccess;
    final Context ctx = this;

    List<OrgOwner> orgsList = new ArrayList<>();

    @Override
    protected int getLayoutResourceId(){
        return R.layout.activity_new_repo;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String userLogin = tinyDb.getString("userLogin");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        closeActivity = findViewById(R.id.close);
        repoName = findViewById(R.id.newRepoName);
        repoDesc = findViewById(R.id.newRepoDescription);
        repoAccess = findViewById(R.id.newRepoPrivate);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        spinner = findViewById(R.id.ownerSpinner);
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getOrgs(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), userLogin);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OrgOwner user = (OrgOwner) parent.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createRepo = findViewById(R.id.createNewRepoButton);
        disableProcessButton();

        if(!connToInternet) {

            disableProcessButton();

        }
        else {

            createRepo.setOnClickListener(createRepoListener);

        }

    }

    private View.OnClickListener createRepoListener = new View.OnClickListener() {
        public void onClick(View v) {
            processNewRepo();
        }
    };

    private void processNewRepo() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        String newRepoName = repoName.getText().toString();
        String newRepoDesc = repoDesc.getText().toString();
        String repoOwner = spinner.getSelectedItem().toString();
        boolean newRepoAccess = repoAccess.isChecked();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(!newRepoDesc.equals("")) {
            if (appUtil.charactersLength(newRepoDesc) > 255) {

                Toasty.info(getApplicationContext(), getString(R.string.repoDescError));
                return;

            }
        }

        if(newRepoName.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.repoNameErrorEmpty));

        }
        else if(!appUtil.checkStrings(newRepoName)) {

            Toasty.info(getApplicationContext(), getString(R.string.repoNameErrorInvalid));

        }
        else {

            //Log.i("repoOwner", String.valueOf(repoOwner));
            disableProcessButton();
            createNewRepository(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), loginUid, newRepoName, newRepoDesc, repoOwner, newRepoAccess);

        }

    }

    private void createNewRepository(final String instanceUrl, final String token, String loginUid, String repoName, String repoDesc, String repoOwner, boolean isPrivate) {

        OrganizationRepository createRepository = new OrganizationRepository(true, repoDesc, null, null, repoName, isPrivate, "Default");

        Call<OrganizationRepository> call;
        if(repoOwner.equals(loginUid)) {

            call = RetrofitClient
                    .getInstance(instanceUrl, getApplicationContext())
                    .getApiInterface()
                    .createNewUserRepository(token, createRepository);

        }
        else {

            call = RetrofitClient
                    .getInstance(instanceUrl, getApplicationContext())
                    .getApiInterface()
                    .createNewUserOrgRepository(token, repoOwner, createRepository);

        }

        call.enqueue(new Callback<OrganizationRepository>() {

            @Override
            public void onResponse(@NonNull Call<OrganizationRepository> call, @NonNull retrofit2.Response<OrganizationRepository> response) {

                if(response.code() == 201) {

                    TinyDB tinyDb = new TinyDB(getApplicationContext());
                    tinyDb.putBoolean("repoCreated", true);
                    Toasty.info(getApplicationContext(), getString(R.string.repoCreated));
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
                    Toasty.info(getApplicationContext(), getString(R.string.repoExistsError));

                }
                else {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.repoCreatedError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<OrganizationRepository> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void getOrgs(String instanceUrl, String instanceToken, final String userLogin) {

        Call<List<OrgOwner>> call = RetrofitClient
                .getInstance(instanceUrl, getApplicationContext())
                .getApiInterface()
                .getOrgOwners(instanceToken);

        call.enqueue(new Callback<List<OrgOwner>>() {

            @Override
            public void onResponse(@NonNull Call<List<OrgOwner>> call, @NonNull retrofit2.Response<List<OrgOwner>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<OrgOwner> orgsList_ = response.body();

                        orgsList.add(new OrgOwner(userLogin));
                        assert orgsList_ != null;
                        if(orgsList_.size() > 0) {
                            for (int i = 0; i < orgsList_.size(); i++) {

                                OrgOwner data = new OrgOwner(
                                        orgsList_.get(i).getUsername()
                                );
                                orgsList.add(data);

                            }
                        }

                        ArrayAdapter<OrgOwner> adapter = new ArrayAdapter<>(getApplicationContext(),
                                R.layout.spinner_item, orgsList);

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                        enableProcessButton();

                    }
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

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
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private void disableProcessButton() {

        createRepo.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        createRepo.setBackground(shape);

    }

    private void enableProcessButton() {

        createRepo.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        createRepo.setBackground(shape);

    }

}
