package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Branches;
import org.mian.gitnex.models.NewFile;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class NewFileActivity extends AppCompatActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;
    private Button newFileCreate;

    private EditText newFileName;
    private EditText newFileContent;
    private EditText newFileBranchName;
    private EditText newFileCommitMessage;
    private Spinner newFileBranchesSpinner;
    final Context ctx = this;

    List<Branches> branchesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_file);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        closeActivity = findViewById(R.id.close);
        newFileName = findViewById(R.id.newFileName);
        newFileContent = findViewById(R.id.newFileContent);
        newFileBranchName = findViewById(R.id.newFileBranchName);
        newFileCommitMessage = findViewById(R.id.newFileCommitMessage);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        newFileCreate = findViewById(R.id.newFileCreate);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        newFileBranchesSpinner = findViewById(R.id.newFileBranchesSpinner);
        newFileBranchesSpinner.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        getBranches(instanceUrl, instanceToken, repoOwner, repoName, loginUid);

        newFileBranchesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> arg0,
                                       View arg1, int arg2, long arg3)
            {
                Branches bModelValue = (Branches) newFileBranchesSpinner.getSelectedItem();
                Log.i("bModelSelected", bModelValue.toString());

                if(bModelValue.toString().equals("No branch")) {
                    newFileBranchName.setEnabled(true);
                }
                else {
                    newFileBranchName.setEnabled(false);
                    newFileBranchName.setText("");
                }

            }

            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        disableProcessButton();

        if(!connToInternet) {

            newFileCreate.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            newFileCreate.setBackground(shape);

        } else {

            newFileCreate.setOnClickListener(createFileListener);

        }

    }

    private View.OnClickListener createFileListener = new View.OnClickListener() {
        public void onClick(View v) {
            processNewFile();
        }
    };

    private void processNewFile() {

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        AppUtil appUtil = new AppUtil();
        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        String newFileName_ = newFileName.getText().toString();
        String newFileContent_ = newFileContent.getText().toString();
        String newFileBranchName_ = newFileBranchName.getText().toString();
        String newFileCommitMessage_ = newFileCommitMessage.getText().toString();

        Branches currentBranch = (Branches) newFileBranchesSpinner.getSelectedItem();

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newFileName_.equals("") || newFileContent_.equals("") || newFileCommitMessage_.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.newFileRequiredFields));
            return;

        }

        if(currentBranch.toString().equals("No branch")) {

            if(newFileBranchName_.equals("")) {
                Toasty.info(getApplicationContext(), getString(R.string.newFileRequiredFieldNewBranchName));
                return;
            }
            else {
                if(!appUtil.checkStringsWithDash(newFileBranchName_)) {

                    Toasty.info(getApplicationContext(), getString(R.string.newFileInvalidBranchName));
                    return;

                }
            }

        }

        if(appUtil.charactersLength(newFileCommitMessage_) > 255) {

            Toasty.info(getApplicationContext(), getString(R.string.newFileCommitMessageError));

        }
        else {

            disableProcessButton();
            createNewFile(instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, newFileName_, appUtil.encodeBase64(newFileContent_), newFileBranchName_, newFileCommitMessage_, currentBranch.toString());

        }

    }

    private void createNewFile(final String instanceUrl, final String token, String repoOwner, String repoName, String fileName, String fileContent, String fileBranchName, String fileCommitMessage, String currentBranch) {

        NewFile createNewFileJsonStr;
        if(currentBranch.equals("No branch")) {
            createNewFileJsonStr = new NewFile("", fileContent, fileCommitMessage, fileBranchName);
        }
        else {
            createNewFileJsonStr = new NewFile(currentBranch, fileContent, fileCommitMessage, "");
        }

        Call<JsonElement> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .createNewFile(token, repoOwner, repoName, fileName, createNewFileJsonStr);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

                if(response.code() == 201) {

                    enableProcessButton();
                    Toasty.info(getApplicationContext(), getString(R.string.newFileSuccessMessage));
                    finish();

                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    if(response.code() == 404) {
                        enableProcessButton();
                        Toasty.info(getApplicationContext(), getString(R.string.apiNotFound));
                    }
                    else {
                        enableProcessButton();
                        Toasty.info(getApplicationContext(), getString(R.string.orgCreatedError));
                    }

                }

            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void getBranches(String instanceUrl, String instanceToken, String repoOwner, String repoName, String loginUid) {

        Call<List<Branches>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getBranches(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Branches>>() {

            @Override
            public void onResponse(@NonNull Call<List<Branches>> call, @NonNull retrofit2.Response<List<Branches>> response) {

                if(response.isSuccessful()) {
                    if(response.code() == 200) {

                        List<Branches> branchesList_ = response.body();

                        branchesList.add(new Branches("No branch"));
                        assert branchesList_ != null;
                        if(branchesList_.size() > 0) {
                            for (int i = 0; i < branchesList_.size(); i++) {

                                Branches data = new Branches(
                                        branchesList_.get(i).getName()
                                );
                                branchesList.add(data);

                            }
                        }

                        ArrayAdapter<Branches> adapter = new ArrayAdapter<>(getApplicationContext(),
                                R.layout.spinner_item, branchesList);

                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                        newFileBranchesSpinner.setAdapter(adapter);
                        enableProcessButton();

                    }
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
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

        newFileCreate.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        newFileCreate.setBackground(shape);

    }

    private void enableProcessButton() {

        newFileCreate.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        newFileCreate.setBackground(shape);

    }

}
