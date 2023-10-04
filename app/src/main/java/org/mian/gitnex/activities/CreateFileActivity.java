package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreateFileOptions;
import org.gitnex.tea4j.v2.models.DeleteFileOptions;
import org.gitnex.tea4j.v2.models.FileDeleteResponse;
import org.gitnex.tea4j.v2.models.FileResponse;
import org.gitnex.tea4j.v2.models.UpdateFileOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateFileBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
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
	ActivityResultLauncher<Intent> codeEditorActivityResultLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK) {
							Intent data = result.getData();
							assert data != null;
							binding.newFileContent.setText(
									data.getStringExtra("fileContentFromActivity"));
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

		binding.topAppBar.setNavigationOnClickListener(v -> finish());

		MenuItem create = binding.topAppBar.getMenu().getItem(0);
		MenuItem update = binding.topAppBar.getMenu().getItem(1);
		MenuItem delete = binding.topAppBar.getMenu().getItem(2);
		update.setVisible(false);
		delete.setVisible(false);

		binding.newFileContent.setOnTouchListener(
				(touchView, motionEvent) -> {
					touchView.getParent().requestDisallowInterceptTouchEvent(true);

					if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0
							&& (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

						touchView.getParent().requestDisallowInterceptTouchEvent(false);
					}

					return false;
				});

		if (getIntent().getStringExtra("filePath") != null
				&& getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE)
						== FILE_ACTION_DELETE) {

			fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_DELETE);
			filePath = getIntent().getStringExtra("filePath");
			fileSha = getIntent().getStringExtra("fileSha");

			binding.topAppBar.setTitle(getString(R.string.deleteGenericTitle, filePath));

			binding.newFileNameLayout.setVisibility(View.GONE);
			binding.newFileContentLayout.setVisibility(View.GONE);
			delete.setVisible(true);
			create.setVisible(false);
			update.setVisible(false);
		}

		if (getIntent().getStringExtra("filePath") != null
				&& getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT) == FILE_ACTION_EDIT) {

			fileAction = getIntent().getIntExtra("fileAction", FILE_ACTION_EDIT);
			filePath = getIntent().getStringExtra("filePath");
			fileSha = getIntent().getStringExtra("fileSha");

			binding.topAppBar.setTitle(getString(R.string.editFileText, filePath));

			binding.newFileName.setText(filePath);
			binding.newFileName.setEnabled(false);
			binding.newFileName.setFocusable(false);

			binding.newFileContent.setText(getIntent().getStringExtra("fileContents"));
			update.setVisible(true);
			create.setVisible(false);
			delete.setVisible(false);
		}

		getBranches(repository.getOwner(), repository.getName());

		binding.openCodeEditor.setOnClickListener(
				v ->
						launchCodeEditorActivityForResult(
								Objects.requireNonNull(binding.newFileContent.getText()).toString(),
								FilenameUtils.getExtension(
										String.valueOf(binding.newFileName.getText()))));

		binding.topAppBar.setOnMenuItemClickListener(
				menuItem -> {
					int id = menuItem.getItemId();

					if (id == R.id.create) {
						processNewFile();
						return true;
					} else if (id == R.id.update) {
						processNewFile();
						return true;
					} else if (id == R.id.delete) {
						processNewFile();
						return true;
					} else {
						return super.onOptionsItemSelected(menuItem);
					}
				});
	}

	public void launchCodeEditorActivityForResult(String fileContent, String fileExtension) {
		Intent intent = new Intent(this, CodeEditorActivity.class);
		intent.putExtra("fileExtension", fileExtension);
		intent.putExtra("fileContent", fileContent);
		codeEditorActivityResultLauncher.launch(intent);
	}

	private void processNewFile() {

		String newFileName =
				binding.newFileName.getText() != null
						? binding.newFileName.getText().toString()
						: "";
		String newFileContent =
				binding.newFileContent.getText() != null
						? binding.newFileContent.getText().toString()
						: "";
		String newFileBranchName =
				binding.newFileBranches.getText() != null
						? binding.newFileBranches.getText().toString()
						: "";
		String newFileCommitMessage =
				binding.newFileCommitMessage.getText() != null
						? binding.newFileCommitMessage.getText().toString()
						: "";

		if (!AppUtil.hasNetworkConnection(appCtx)) {
			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.checkNetConnection));
			return;
		}

		if (((newFileName.isEmpty() || newFileContent.isEmpty())
						&& fileAction != FILE_ACTION_DELETE)
				|| newFileCommitMessage.isEmpty()) {
			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.newFileRequiredFields));
			return;
		}

		if (!AppUtil.checkStringsWithDash(newFileBranchName)) {
			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.newFileInvalidBranchName));
			return;
		}

		if (newFileCommitMessage.length() > 255) {
			SnackBar.error(
					ctx,
					findViewById(android.R.id.content),
					getString(R.string.newFileCommitMessageError));
			return;
		}

		switch (fileAction) {
			case FILE_ACTION_CREATE:
				createNewFile(
						repository.getOwner(),
						repository.getName(),
						newFileName,
						AppUtil.encodeBase64(newFileContent),
						newFileCommitMessage,
						newFileBranchName);
				break;

			case FILE_ACTION_DELETE:
				deleteFile(
						repository.getOwner(),
						repository.getName(),
						filePath,
						newFileCommitMessage,
						newFileBranchName,
						fileSha);
				break;

			case FILE_ACTION_EDIT:
				editFile(
						repository.getOwner(),
						repository.getName(),
						filePath,
						AppUtil.encodeBase64(newFileContent),
						newFileCommitMessage,
						newFileBranchName,
						fileSha);
				break;
		}
	}

	private void createNewFile(
			String repoOwner,
			String repoName,
			String fileName,
			String fileContent,
			String fileCommitMessage,
			String branchName) {

		CreateFileOptions createNewFileJsonStr = new CreateFileOptions();
		createNewFileJsonStr.setContent(fileContent);
		createNewFileJsonStr.setMessage(fileCommitMessage);

		if (branches.contains(branchName)) {
			createNewFileJsonStr.setBranch(branchName);
		} else {
			createNewFileJsonStr.setNewBranch(branchName);
		}

		Call<FileResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.repoCreateFile(createNewFileJsonStr, repoOwner, repoName, fileName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<FileResponse> call,
							@NonNull retrofit2.Response<FileResponse> response) {

						switch (response.code()) {
							case 201:
								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.newFileSuccessMessage));
								Intent result = new Intent();
								result.putExtra("fileModified", true);
								result.putExtra("fileAction", fileAction);
								setResult(200, result);
								RepoDetailActivity.updateFABActions = true;
								new Handler().postDelayed(() -> finish(), 3000);
								break;

							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
								break;

							case 404:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.apiNotFound));
								break;

							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericError));
								break;
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {}
				});
	}

	private void deleteFile(
			String repoOwner,
			String repoName,
			String fileName,
			String fileCommitMessage,
			String branchName,
			String fileSha) {

		DeleteFileOptions deleteFileJsonStr = new DeleteFileOptions();
		deleteFileJsonStr.setMessage(fileCommitMessage);
		deleteFileJsonStr.setSha(fileSha);

		if (branches.contains(branchName)) {
			deleteFileJsonStr.setBranch(branchName);
		} else {
			deleteFileJsonStr.setNewBranch(branchName);
		}

		Call<FileDeleteResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.repoDeleteFileWithBody(repoOwner, repoName, fileName, deleteFileJsonStr);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<FileDeleteResponse> call,
							@NonNull retrofit2.Response<FileDeleteResponse> response) {

						switch (response.code()) {
							case 200:
								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(
												R.string.deleteFileMessage,
												repository.getBranchRef()));
								Intent result = new Intent();
								result.putExtra("fileModified", true);
								result.putExtra("fileAction", fileAction);
								setResult(200, result);
								new Handler().postDelayed(() -> finish(), 3000);
								break;

							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
								break;

							case 404:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.apiNotFound));
								break;

							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericError));
								break;
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<FileDeleteResponse> call, @NonNull Throwable t) {}
				});
	}

	private void editFile(
			String repoOwner,
			String repoName,
			String fileName,
			String fileContent,
			String fileCommitMessage,
			String branchName,
			String fileSha) {

		UpdateFileOptions editFileJsonStr = new UpdateFileOptions();
		editFileJsonStr.setContent(fileContent);
		editFileJsonStr.setMessage(fileCommitMessage);
		editFileJsonStr.setSha(fileSha);

		if (branches.contains(branchName)) {
			editFileJsonStr.setBranch(branchName);
		} else {
			editFileJsonStr.setNewBranch(branchName);
		}

		Call<FileResponse> call =
				RetrofitClient.getApiInterface(ctx)
						.repoUpdateFile(editFileJsonStr, repoOwner, repoName, fileName);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<FileResponse> call,
							@NonNull retrofit2.Response<FileResponse> response) {

						switch (response.code()) {
							case 200:
								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.editFileMessage, branchName));
								Intent result = new Intent();
								result.putExtra("fileModified", true);
								result.putExtra("fileAction", fileAction);
								setResult(200, result);
								new Handler().postDelayed(() -> finish(), 3000);
								break;

							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
								break;

							case 404:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.apiNotFound));
								break;

							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericError));
								break;
						}
					}

					@Override
					public void onFailure(@NonNull Call<FileResponse> call, @NonNull Throwable t) {}
				});
	}

	private void getBranches(String repoOwner, String repoName) {

		Call<List<Branch>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoListBranches(repoOwner, repoName, null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branch>> call,
							@NonNull retrofit2.Response<List<Branch>> response) {

						if (response.code() == 200) {

							assert response.body() != null;
							for (Branch branch : response.body()) branches.add(branch.getName());

							ArrayAdapter<String> adapter =
									new ArrayAdapter<>(
											CreateFileActivity.this,
											R.layout.list_spinner_items,
											branches);

							binding.newFileBranches.setAdapter(adapter);
							binding.newFileBranches.setText(repository.getBranchRef(), false);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
