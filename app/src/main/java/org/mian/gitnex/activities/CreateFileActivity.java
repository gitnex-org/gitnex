package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.CreateBranchRepoOption;
import org.gitnex.tea4j.v2.models.CreateFileOptions;
import org.gitnex.tea4j.v2.models.DeleteFileOptions;
import org.gitnex.tea4j.v2.models.FileDeleteResponse;
import org.gitnex.tea4j.v2.models.FileResponse;
import org.gitnex.tea4j.v2.models.UpdateFileOptions;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.BranchAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateFileBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class CreateFileActivity extends BaseActivity {

	public static final int FILE_ACTION_CREATE = 0;
	public static final int FILE_ACTION_DELETE = 1;
	public static final int FILE_ACTION_EDIT = 2;
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

		binding.newFileBranches.setKeyListener(null);
		binding.newFileBranches.setCursorVisible(false);
		binding.newFileBranches.setOnFocusChangeListener(
				(v, hasFocus) -> {
					if (hasFocus) {
						getBranches();
						binding.newFileBranches.clearFocus();
					}
				});
		binding.newFileBranchesLayout.setEndIconOnClickListener(v -> showCreateBranchDialog());

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

	private void showCreateBranchDialog() {

		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(ctx);
		builder.setTitle(getString(R.string.create_branch));

		LayoutInflater inflater = getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_create_branch_dialog, null);
		builder.setView(dialogView);

		TextInputEditText branchName_ = dialogView.findViewById(R.id.branch_name);
		TextInputEditText ref_ = dialogView.findViewById(R.id.ref);

		ref_.setText(Objects.requireNonNull(binding.newFileBranches.getText()).toString());

		builder.setPositiveButton(getString(R.string.newCreateButtonCopy), null);
		builder.setNeutralButton(getString(R.string.close), (dialog, which) -> dialog.dismiss());

		AlertDialog dialog = builder.create();
		dialog.show();

		Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
		positiveButton.setOnClickListener(
				v -> {
					String branchName =
							Objects.requireNonNull(branchName_.getText()).toString().trim();
					String ref = Objects.requireNonNull(ref_.getText()).toString().trim();

					if (branchName.isEmpty() || ref.isEmpty()) {
						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.create_branch_empty_fields));
					} else {
						createBranch(branchName, ref, dialog);
					}
				});
	}

	private void createBranch(String branchName, String ref, AlertDialog dialog) {

		CreateBranchRepoOption createBranchRepoOption = new CreateBranchRepoOption();
		createBranchRepoOption.setNewBranchName(branchName);
		createBranchRepoOption.setOldRefName(ref);

		Call<Branch> call =
				RetrofitClient.getApiInterface(ctx)
						.repoCreateBranch(
								repository.getOwner(),
								repository.getName(),
								createBranchRepoOption);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Branch> call, @NonNull Response<Branch> response) {
						switch (response.code()) {
							case 201:
								binding.newFileBranches.setText(branchName);
								SnackBar.success(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.branch_created));
								dialog.dismiss();
								break;
							case 401:
								AlertDialogs.authorizationTokenRevokedDialog(ctx);
								break;
							case 403:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.branch_error_archive_mirror));
								break;
							case 404:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.branch_error_ref_not_found));
								break;
							case 409:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.branch_error_exists, branchName));
								break;
							case 423:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.branch_error_repo_locked));
								break;
							default:
								SnackBar.error(
										ctx,
										findViewById(android.R.id.content),
										getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Branch> call, @NonNull Throwable t) {
						SnackBar.error(
								ctx,
								findViewById(android.R.id.content),
								getString(R.string.genericServerResponseError));
					}
				});
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
		createNewFileJsonStr.setBranch(branchName);

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
		deleteFileJsonStr.setBranch(branchName);

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
		editFileJsonStr.setBranch(branchName);

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

	private void getBranches() {

		Dialog progressDialog = new Dialog(ctx);
		progressDialog.setCancelable(false);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(ctx);
		View dialogView = getLayoutInflater().inflate(R.layout.custom_branches_dialog, null);
		dialogBuilder.setView(dialogView);

		RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		recyclerView.addItemDecoration(
				new RecyclerView.ItemDecoration() {
					@Override
					public void getItemOffsets(
							@NonNull Rect outRect,
							@NonNull View view,
							@NonNull RecyclerView parent,
							@NonNull RecyclerView.State state) {

						int position = parent.getChildAdapterPosition(view);
						int spacingSides = (int) ctx.getResources().getDimension(R.dimen.dimen16dp);
						int spacingTop = (int) ctx.getResources().getDimension(R.dimen.dimen12dp);

						outRect.right = spacingSides;
						outRect.left = spacingSides;

						if (position > 0) {
							outRect.top = spacingTop;
						}
					}
				});

		dialogBuilder.setNeutralButton(R.string.close, (dialog, which) -> dialog.dismiss());
		AlertDialog dialog = dialogBuilder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		final int[] page = {1};
		final int resultLimit = Constants.getCurrentResultLimit(ctx);
		final boolean[] isLoading = {false};
		final boolean[] isLastPage = {false};

		BranchAdapter adapter =
				new BranchAdapter(
						branchName -> {
							binding.newFileBranches.setText(branchName);
							dialog.dismiss();
						});
		recyclerView.setAdapter(adapter);

		Runnable fetchBranches =
				() -> {
					if (isLoading[0] || isLastPage[0]) return;
					isLoading[0] = true;

					Call<List<Branch>> call =
							RetrofitClient.getApiInterface(ctx)
									.repoListBranches(
											repository.getOwner(),
											repository.getName(),
											page[0],
											resultLimit);

					call.enqueue(
							new Callback<>() {
								@Override
								public void onResponse(
										@NonNull Call<List<Branch>> call,
										@NonNull Response<List<Branch>> response) {

									isLoading[0] = false;

									if (response.code() == 200 && response.body() != null) {
										List<Branch> newBranches = response.body();
										adapter.addBranches(newBranches);

										String totalCountStr =
												response.headers().get("X-Total-Count");

										if (totalCountStr != null) {

											int totalItems = Integer.parseInt(totalCountStr);
											int totalPages =
													(int)
															Math.ceil(
																	(double) totalItems
																			/ resultLimit);
											isLastPage[0] = page[0] >= totalPages;
										} else {
											isLastPage[0] = newBranches.size() < resultLimit;
										}
										page[0]++;

										if (page[0] == 2 && !dialog.isShowing()) {
											progressDialog.dismiss();
											dialog.show();
										}
									} else {
										progressDialog.dismiss();
									}
								}

								@Override
								public void onFailure(
										@NonNull Call<List<Branch>> call, @NonNull Throwable t) {
									isLoading[0] = false;
									progressDialog.dismiss();
								}
							});
				};

		recyclerView.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

						super.onScrolled(recyclerView, dx, dy);
						LinearLayoutManager layoutManager =
								(LinearLayoutManager) recyclerView.getLayoutManager();

						if (layoutManager != null) {

							int visibleItemCount = layoutManager.getChildCount();
							int totalItemCount = layoutManager.getItemCount();
							int firstVisibleItemPosition =
									layoutManager.findFirstVisibleItemPosition();

							if (!isLoading[0]
									&& !isLastPage[0]
									&& (visibleItemCount + firstVisibleItemPosition)
											>= totalItemCount - 5) {
								fetchBranches.run();
							}
						}
					}
				});

		adapter.clear();
		fetchBranches.run();
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
