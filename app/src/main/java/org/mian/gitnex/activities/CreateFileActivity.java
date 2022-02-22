package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.gson.JsonElement;
import org.gitnex.tea4j.models.Branches;
import org.gitnex.tea4j.models.DeleteFile;
import org.gitnex.tea4j.models.EditFile;
import org.gitnex.tea4j.models.NewFile;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateFileBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateFileActivity extends BaseActivity {

	private ActivityCreateFileBinding binding;

	public static final int FILE_ACTION_CREATE = 0;
	public static final int FILE_ACTION_DELETE = 1;
	public static final int FILE_ACTION_EDIT = 2;

	private int fileAction = FILE_ACTION_CREATE;

	private String filePath;
	private String fileSha;

    private final List<String> branches = new ArrayList<>();

	private String repoOwner;
	private String repoName;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ActivityCreateFileBinding.inflate(getLayoutInflater());
	    setContentView(binding.getRoot());

        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];

	    TextView toolbarTitle = binding.toolbarTitle;

        binding.newFileName.requestFocus();

	    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.showSoftInput(binding.newFileName, InputMethodManager.SHOW_IMPLICIT);

	    binding.close.setOnClickListener(view -> finish());
	    binding.newFileContent.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 &&
			    (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }

		    return false;

	    });

	    if(getIntent().getStringExtra("filePath") != null && getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE) == FILE_ACTION_DELETE) {

		    fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE);
		    filePath = getIntent().getStringExtra("filePath");
		    fileSha = getIntent().getStringExtra("fileSha");

		    toolbarTitle.setText(getString(R.string.deleteFileText, filePath));

		    binding.newFileCreate.setText(R.string.deleteFile);

		    binding.newFileNameLayout.setVisibility(View.GONE);
		    binding.newFileContentLayout.setVisibility(View.GONE);

	    }

	    if(getIntent().getStringExtra("filePath") != null && getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT) == FILE_ACTION_EDIT) {

		    fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT);
		    filePath = getIntent().getStringExtra("filePath");
		    fileSha = getIntent().getStringExtra("fileSha");

		    toolbarTitle.setText(getString(R.string.editFileText, filePath));

		    binding.newFileCreate.setText(R.string.editFile);
		    binding.newFileName.setText(filePath);
		    binding.newFileName.setEnabled(false);
		    binding.newFileName.setFocusable(false);

		    binding.newFileContent.setText(getIntent().getStringExtra("fileContents"));

	    }

        getBranches(repoOwner, repoName);

        disableProcessButton();

	    NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.getInstance(ctx);
	    networkStatusObserver.registerNetworkStatusListener(hasNetworkConnection -> runOnUiThread(() -> binding.newFileCreate.setEnabled(hasNetworkConnection)));

	    binding.newFileCreate.setOnClickListener(v -> processNewFile());

    }

    private void processNewFile() {

        String newFileName = binding.newFileName.getText() != null ? binding.newFileName.getText().toString() : "";
        String newFileContent = binding.newFileContent.getText() != null ? binding.newFileContent.getText().toString() : "";
        String newFileBranchName = binding.newFileBranches.getText() != null ? binding.newFileBranches.getText().toString() : "";
        String newFileCommitMessage = binding.newFileCommitMessage.getText() != null ? binding.newFileCommitMessage.getText().toString() : "";

        if(!AppUtil.hasNetworkConnection(appCtx)) {
            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(((newFileName.isEmpty() || newFileContent.isEmpty()) && fileAction != FILE_ACTION_DELETE) || newFileCommitMessage.isEmpty()) {
            Toasty.error(ctx, getString(R.string.newFileRequiredFields));
            return;
        }

	    if(!AppUtil.checkStringsWithDash(newFileBranchName)) {
		    Toasty.error(ctx, getString(R.string.newFileInvalidBranchName));
		    return;
	    }

        if(newFileCommitMessage.length() > 255) {
            Toasty.warning(ctx, getString(R.string.newFileCommitMessageError));
            return;
        }

        disableProcessButton();

        switch(fileAction) {

            case FILE_ACTION_CREATE:
	            createNewFile(repoOwner, repoName, newFileName, AppUtil.encodeBase64(newFileContent), newFileCommitMessage, newFileBranchName);
	            break;

            case FILE_ACTION_DELETE:
	            deleteFile(repoOwner, repoName, filePath, newFileCommitMessage, newFileBranchName, fileSha);
	            break;

            case FILE_ACTION_EDIT:
	            editFile(repoOwner, repoName, filePath, AppUtil.encodeBase64(newFileContent), newFileCommitMessage, newFileBranchName, fileSha);
                break;

        }
    }

    private void createNewFile(String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName) {

        NewFile createNewFileJsonStr = branches.contains(branchName) ?
	        new NewFile(branchName, fileContent, fileCommitMessage, "") :
	        new NewFile("", fileContent, fileCommitMessage, branchName);

        Call<JsonElement> call = RetrofitClient
                .getApiInterface(ctx)
                .createNewFile(Authorization.get(ctx), repoOwner, repoName, fileName, createNewFileJsonStr);

        call.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

            	switch(response.code()) {

		            case 201:
			            enableProcessButton();
			            Toasty.success(ctx, getString(R.string.newFileSuccessMessage));
			            finish();
		            	break;

		            case 401:
			            enableProcessButton();
			            AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
				            getResources().getString(R.string.alertDialogTokenRevokedMessage),
				            getResources().getString(R.string.cancelButton),
				            getResources().getString(R.string.navLogout));
		            	break;

		            case 404:
			            enableProcessButton();
			            Toasty.warning(ctx, getString(R.string.apiNotFound));
		            	break;

		            default:
			            enableProcessButton();
			            Toasty.error(ctx, getString(R.string.orgCreatedError));
		            	break;

	            }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();

            }
        });

    }

	private void deleteFile(String repoOwner, String repoName, String fileName, String fileCommitMessage, String branchName, String fileSha) {

    	DeleteFile deleteFileJsonStr = branches.contains(branchName) ?
		    new DeleteFile(branchName, fileCommitMessage, "", fileSha) :
		    new DeleteFile("", fileCommitMessage, branchName, fileSha);

		Call<JsonElement> call = RetrofitClient
			.getApiInterface(ctx)
			.deleteFile(Authorization.get(ctx), repoOwner, repoName, fileName, deleteFileJsonStr);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				switch(response.code()) {

					case 200:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.deleteFileMessage, tinyDB.getString("repoBranch")));
						getIntent().removeExtra("filePath");
						getIntent().removeExtra("fileSha");
						getIntent().removeExtra("fileContents");
						finish();
						break;

					case 401:
						enableProcessButton();
						AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
							getResources().getString(R.string.alertDialogTokenRevokedMessage),
							getResources().getString(R.string.cancelButton),
							getResources().getString(R.string.navLogout));
						break;

					case 404:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.apiNotFound));
						break;

					default:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.genericError));
						break;

				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}
		});

	}

	private void editFile(String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName, String fileSha) {

		EditFile editFileJsonStr = branches.contains(branchName) ?
			new EditFile(branchName, fileCommitMessage, "", fileSha, fileContent) :
			new EditFile("", fileCommitMessage, branchName, fileSha, fileContent);

		Call<JsonElement> call = RetrofitClient
			.getApiInterface(ctx)
			.editFile(Authorization.get(ctx), repoOwner, repoName, fileName, editFileJsonStr);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				switch(response.code()) {

					case 200:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.editFileMessage, branchName));
						getIntent().removeExtra("filePath");
						getIntent().removeExtra("fileSha");
						getIntent().removeExtra("fileContents");
						tinyDB.putBoolean("fileModified", true);
						finish();
						break;

					case 401:
						enableProcessButton();
						AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
							getResources().getString(R.string.alertDialogTokenRevokedMessage),
							getResources().getString(R.string.cancelButton),
							getResources().getString(R.string.navLogout));
						break;

					case 404:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.apiNotFound));
						break;

					default:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.genericError));
						break;

				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();

			}
		});

	}

    private void getBranches(String repoOwner, String repoName) {

        Call<List<Branches>> call = RetrofitClient
                .getApiInterface(ctx)
                .getBranches(Authorization.get(ctx), repoOwner, repoName);

        call.enqueue(new Callback<List<Branches>>() {

            @Override
            public void onResponse(@NonNull Call<List<Branches>> call, @NonNull retrofit2.Response<List<Branches>> response) {

                if(response.code() == 200) {

                	assert response.body() != null;
                    for(Branches branch : response.body()) branches.add(branch.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateFileActivity.this, R.layout.list_spinner_items, branches);

	                binding.newFileBranches.setAdapter(adapter);
	                binding.newFileBranches.setText(tinyDB.getString("repoBranch"), false);

	                enableProcessButton();

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
            }
        });

    }

    private void disableProcessButton() { binding.newFileCreate.setEnabled(false); }
    private void enableProcessButton() { binding.newFileCreate.setEnabled(true); }

}
