package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.*;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateFileBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.NetworkStatusObserver;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class CreateFileActivity extends BaseActivity {

	public static final int FILE_ACTION_CREATE = 0;
	public static final int FILE_ACTION_DELETE = 1;
	public static final int FILE_ACTION_EDIT = 2;
	private final List<String> branches = new ArrayList<>();
	private ActivityCreateFileBinding binding;
	ActivityResultLauncher<Intent> codeEditorActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
		if(result.getResultCode() == Activity.RESULT_OK) {
			Intent data = result.getData();
			assert data != null;
			binding.newFileContent.setText(data.getStringExtra("fileContentFromActivity"));
		}
	});
	private int fileAction = FILE_ACTION_CREATE;
	private String filePath;
	private String fileSha;
	private RepositoryContext repository;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityCreateFileBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		TextView toolbarTitle = binding.toolbarTitle;

		binding.newFileName.requestFocus();

		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		assert inputMethodManager != null;
		inputMethodManager.showSoftInput(binding.newFileName, InputMethodManager.SHOW_IMPLICIT);

		binding.close.setOnClickListener(view -> finish());
		binding.newFileContent.setOnTouchListener((touchView, motionEvent) -> {

			touchView.getParent().requestDisallowInterceptTouchEvent(true);

			if((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

				touchView.getParent().requestDisallowInterceptTouchEvent(false);
			}

			return false;

		});

		if(getIntent().getStringExtra("filePath") != null && getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE) == FILE_ACTION_DELETE) {

			fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE);
			filePath = getIntent().getStringExtra("filePath");
			fileSha = getIntent().getStringExtra("fileSha");

			toolbarTitle.setText(getString(R.string.deleteGenericTitle, filePath));

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

		getBranches(repository.getOwner(), repository.getName());

		disableProcessButton();

		binding.openCodeEditor.setOnClickListener(
			v -> launchCodeEditorActivityForResult(Objects.requireNonNull(binding.newFileContent.getText()).toString(), FilenameUtils.getExtension(String.valueOf(binding.newFileName.getText()))));

		NetworkStatusObserver networkStatusObserver = NetworkStatusObserver.getInstance(ctx);
		networkStatusObserver.registerNetworkStatusListener(hasNetworkConnection -> runOnUiThread(() -> binding.newFileCreate.setEnabled(hasNetworkConnection)));

		binding.newFileCreate.setOnClickListener(v -> processNewFile());
	}

	public void launchCodeEditorActivityForResult(String fileContent, String fileExtension) {
		Intent intent = new Intent(this, CodeEditorActivity.class);
		intent.putExtra("fileExtension", fileExtension);
		intent.putExtra("fileContent", fileContent);
		codeEditorActivityResultLauncher.launch(intent);
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
				createNewFile(repository.getOwner(), repository.getName(), newFileName, AppUtil.encodeBase64(newFileContent), newFileCommitMessage, newFileBranchName);
				break;

			case FILE_ACTION_DELETE:
				deleteFile(repository.getOwner(), repository.getName(), filePath, newFileCommitMessage, newFileBranchName, fileSha);
				break;

			case FILE_ACTION_EDIT:
				editFile(repository.getOwner(), repository.getName(), filePath, AppUtil.encodeBase64(newFileContent), newFileCommitMessage, newFileBranchName, fileSha);
				break;
		}
	}

	private void createNewFile(String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName) {

		CreateFileOptions createNewFileJsonStr = new CreateFileOptions();
		createNewFileJsonStr.setContent(fileContent);
		createNewFileJsonStr.setMessage(fileCommitMessage);

		if(branches.contains(branchName)) {
			createNewFileJsonStr.setBranch(branchName);
		}
		else {
			createNewFileJsonStr.setNewBranch(branchName);
		}

		Call<FileResponse> call = RetrofitClient.getApiInterface(ctx).repoCreateFile(createNewFileJsonStr, repoOwner, repoName, fileName);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<FileResponse> call, @NonNull retrofit2.Response<FileResponse> response) {

				switch(response.code()) {

					case 201:
						enableProcessButton();
						Toasty.success(ctx, getString(R.string.newFileSuccessMessage));
						Intent result = new Intent();
						result.putExtra("fileModified", true);
						result.putExtra("fileAction", fileAction);
						setResult(200, result);
						finish();
						break;

					case 401:
						enableProcessButton();
						AlertDialogs.authorizationTokenRevokedDialog(ctx);
						break;

					case 404:
						enableProcessButton();
						Toasty.warning(ctx, getString(R.string.apiNotFound));
						break;

					default:
						enableProcessButton();
						Toasty.error(ctx, getString(R.string.genericError));
						break;
				}
			}

			@Override
			public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}
		});
	}

	private void deleteFile(String repoOwner, String repoName, String fileName, String fileCommitMessage, String branchName, String fileSha) {

		DeleteFileOptions deleteFileJsonStr = new DeleteFileOptions();
		deleteFileJsonStr.setMessage(fileCommitMessage);
		deleteFileJsonStr.setSha(fileSha);

		if(branches.contains(branchName)) {
			deleteFileJsonStr.setBranch(branchName);
		}
		else {
			deleteFileJsonStr.setNewBranch(branchName);
		}

		Call<FileDeleteResponse> call = RetrofitClient.getApiInterface(ctx).repoDeleteFileWithBody(repoOwner, repoName, fileName, deleteFileJsonStr);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<FileDeleteResponse> call, @NonNull retrofit2.Response<FileDeleteResponse> response) {

				switch(response.code()) {

					case 200:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.deleteFileMessage, repository.getBranchRef()));
						Intent result = new Intent();
						result.putExtra("fileModified", true);
						result.putExtra("fileAction", fileAction);
						setResult(200, result);
						finish();
						break;

					case 401:
						enableProcessButton();
						AlertDialogs.authorizationTokenRevokedDialog(ctx);
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
			public void onFailure(@NonNull Call<FileDeleteResponse> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}
		});
	}

	private void editFile(String repoOwner, String repoName, String fileName, String fileContent, String fileCommitMessage, String branchName, String fileSha) {

		UpdateFileOptions editFileJsonStr = new UpdateFileOptions();
		editFileJsonStr.setContent(fileContent);
		editFileJsonStr.setMessage(fileCommitMessage);
		editFileJsonStr.setSha(fileSha);

		if(branches.contains(branchName)) {
			editFileJsonStr.setBranch(branchName);
		}
		else {
			editFileJsonStr.setNewBranch(branchName);
		}

		Call<FileResponse> call = RetrofitClient.getApiInterface(ctx).repoUpdateFile(editFileJsonStr, repoOwner, repoName, fileName);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<FileResponse> call, @NonNull retrofit2.Response<FileResponse> response) {

				switch(response.code()) {

					case 200:
						enableProcessButton();
						Toasty.info(ctx, getString(R.string.editFileMessage, branchName));
						Intent result = new Intent();
						result.putExtra("fileModified", true);
						result.putExtra("fileAction", fileAction);
						setResult(200, result);
						finish();
						break;

					case 401:
						enableProcessButton();
						AlertDialogs.authorizationTokenRevokedDialog(ctx);
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
			public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}
		});
	}

	private void getBranches(String repoOwner, String repoName) {

		Call<List<Branch>> call = RetrofitClient.getApiInterface(ctx).repoListBranches(repoOwner, repoName, null, null);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Branch>> call, @NonNull retrofit2.Response<List<Branch>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					for(Branch branch : response.body())
						branches.add(branch.getName());

					ArrayAdapter<String> adapter = new ArrayAdapter<>(CreateFileActivity.this, R.layout.list_spinner_items, branches);

					binding.newFileBranches.setAdapter(adapter);
					binding.newFileBranches.setText(repository.getBranchRef(), false);

					enableProcessButton();
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}
		});
	}

	private void disableProcessButton() {
		binding.newFileCreate.setEnabled(false);
	}

	private void enableProcessButton() {
		binding.newFileCreate.setEnabled(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}
