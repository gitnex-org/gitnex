package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;
    private Button newFileCreate;

    private EditText newFileName;
    private EditText newFileContent;
    private EditText newFileCommitMessage;
    private AutoCompleteTextView newFileBranches;
	private String filePath;
	private String fileSha;

	public static final int FILE_ACTION_CREATE = 0;
	public static final int FILE_ACTION_DELETE = 1;
	public static final int FILE_ACTION_EDIT = 2;

	private int fileAction = FILE_ACTION_CREATE;

    private final List<String> branches = new ArrayList<>();

	private String repoOwner;
	private String repoName;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateFileBinding activityCreateFileBinding = ActivityCreateFileBinding.inflate(getLayoutInflater());
	    setContentView(activityCreateFileBinding.getRoot());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];

        closeActivity = activityCreateFileBinding.close;
        newFileName = activityCreateFileBinding.newFileName;
        newFileContent = activityCreateFileBinding.newFileContent;
        newFileCommitMessage = activityCreateFileBinding.newFileCommitMessage;
	    TextView toolbarTitle = activityCreateFileBinding.toolbarTitle;

        newFileName.requestFocus();
        assert imm != null;
        imm.showSoftInput(newFileName, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();

        closeActivity.setOnClickListener(onClickListener);

        newFileCreate = activityCreateFileBinding.newFileCreate;
	    newFileContent.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

	    if(getIntent().getStringExtra("filePath") != null && getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE) == FILE_ACTION_DELETE) {

		    fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE);

		    filePath = getIntent().getStringExtra("filePath");
		    String fileContents = getIntent().getStringExtra("fileContents");
		    fileSha = getIntent().getStringExtra("fileSha");

		    toolbarTitle.setText(getString(R.string.deleteFileText, filePath));

		    newFileCreate.setText(R.string.deleteFile);
		    newFileName.setText(filePath);
		    newFileName.setEnabled(false);
		    newFileName.setFocusable(false);

		    newFileContent.setText(fileContents);
		    newFileContent.setEnabled(false);
		    newFileContent.setFocusable(false);

	    }

	    if(getIntent().getStringExtra("filePath") != null && getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT) == FILE_ACTION_EDIT) {

		    fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT);

		    filePath = getIntent().getStringExtra("filePath");
		    String fileContents = getIntent().getStringExtra("fileContents");
		    fileSha = getIntent().getStringExtra("fileSha");

		    toolbarTitle.setText(getString(R.string.editFileText, filePath));

		    newFileCreate.setText(R.string.editFile);
		    newFileName.setText(filePath);
		    newFileName.setEnabled(false);
		    newFileName.setFocusable(false);

		    newFileContent.setText(fileContents);

	    }

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        newFileBranches = activityCreateFileBinding.newFileBranches;
        getBranches(repoOwner, repoName);

        disableProcessButton();

	    NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.get(ctx);
	    networkStatusObserver.registerNetworkStatusListener(hasNetworkConnection -> newFileCreate.setEnabled(hasNetworkConnection));

	    newFileCreate.setOnClickListener(createFileListener);

    }

    private final View.OnClickListener createFileListener = v -> processNewFile();

    private void processNewFile() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newFileName_ = newFileName.getText().toString();
        String newFileContent_ = newFileContent.getText().toString();
        String newFileBranchName_ = newFileBranches.getText().toString();
        String newFileCommitMessage_ = newFileCommitMessage.getText().toString();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newFileName_.equals("") || newFileContent_.equals("") || newFileCommitMessage_.equals("")) {

            Toasty.error(ctx, getString(R.string.newFileRequiredFields));
            return;
        }

	    if(!AppUtil.checkStringsWithDash(newFileBranchName_)) {

		    Toasty.error(ctx, getString(R.string.newFileInvalidBranchName));
		    return;
	    }

        if(newFileCommitMessage_.length() > 255) {

            Toasty.warning(ctx, getString(R.string.newFileCommitMessageError));
        }
        else {

            disableProcessButton();

            switch(fileAction) {

	            case FILE_ACTION_CREATE:
		            createNewFile(Authorization.get(ctx), repoOwner, repoName, newFileName_, AppUtil.encodeBase64(newFileContent_), newFileCommitMessage_, newFileBranchName_);
		            break;

	            case FILE_ACTION_DELETE:
		            deleteFile(Authorization.get(ctx), repoOwner, repoName, filePath, newFileCommitMessage_, newFileBranchName_, fileSha);
		            break;

	            case FILE_ACTION_EDIT:
		            editFile(Authorization.get(ctx), repoOwner, repoName, filePath,
			            AppUtil.encodeBase64(newFileContent_), newFileCommitMessage_, newFileBranchName_, fileSha);
	            	break;

            }
        }
    }

    private void createNewFile(final String token, String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName) {

        NewFile createNewFileJsonStr = branches.contains(branchName) ?
	        new NewFile(branchName, fileContent, fileCommitMessage, "") :
	        new NewFile("", fileContent, fileCommitMessage, branchName);

        Call<JsonElement> call = RetrofitClient
                .getApiInterface(ctx)
                .createNewFile(token, repoOwner, repoName, fileName, createNewFileJsonStr);

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
				            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
				            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
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

	private void deleteFile(final String token, String repoOwner, String repoName, String fileName, String fileCommitMessage, String branchName, String fileSha) {

    	DeleteFile deleteFileJsonStr = branches.contains(branchName) ?
		    new DeleteFile(branchName, fileCommitMessage, "", fileSha) :
		    new DeleteFile("", fileCommitMessage, branchName, fileSha);

		Call<JsonElement> call = RetrofitClient
			.getApiInterface(ctx)
			.deleteFile(token, repoOwner, repoName, fileName, deleteFileJsonStr);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				if(response.code() == 200) {

					enableProcessButton();
					Toasty.info(ctx, getString(R.string.deleteFileMessage, tinyDB.getString("repoBranch")));
					getIntent().removeExtra("filePath");
					getIntent().removeExtra("fileSha");
					getIntent().removeExtra("fileContents");
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
						Toasty.info(ctx, getString(R.string.apiNotFound));
					}
					else {

						enableProcessButton();
						Toasty.info(ctx, getString(R.string.genericError));
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

	private void editFile(final String token, String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName, String fileSha) {

		EditFile editFileJsonStr = branches.contains(branchName) ?
			new EditFile(branchName, fileCommitMessage, "", fileSha, fileContent) :
			new EditFile("", fileCommitMessage, branchName, fileSha, fileContent);

		Call<JsonElement> call = RetrofitClient
			.getApiInterface(ctx)
			.editFile(token, repoOwner, repoName, fileName, editFileJsonStr);

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
							getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
							getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));
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

                    newFileBranches.setAdapter(adapter);
                    newFileBranches.setText(tinyDB.getString("repoBranch"), false);

	                enableProcessButton();

                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {

        newFileCreate.setEnabled(false);
    }

    private void enableProcessButton() {

        newFileCreate.setEnabled(true);
    }

}
