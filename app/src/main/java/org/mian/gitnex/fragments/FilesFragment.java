package org.mian.gitnex.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.search.SearchView;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitsActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.BottomsheetFileItemMenuBinding;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Path;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.viewmodels.FilesViewModel;
import retrofit2.Call;

/**
 * @author mmarif
 */
public class FilesFragment extends Fragment
		implements FilesAdapter.FilesAdapterListener, RepoDetailActivity.RepoHubProvider {

	private FragmentFilesBinding binding;
	private FilesViewModel viewModel;
	private FilesAdapter filesAdapter;
	private RepositoryContext repository;
	private final Path path = new Path();
	private boolean isFirstLoad = true;
	private String pendingAction = null;
	private RepoGetContentsList pendingFile = null;
	private RepoGetContentsList pendingDownloadFile;

	public static FilesFragment newInstance(RepositoryContext repository) {
		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repository = RepositoryContext.fromBundle(getArguments());
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentFilesBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(FilesViewModel.class);

		setupRecyclerView();
		setupListeners();
		setupBackNavigation();
		observeViewModel();
		observeFileContent();

		String dir = requireActivity().getIntent().getStringExtra("dir");
		if (dir != null && path.size() == 0) {
			for (String segment : dir.split("/")) {
				path.addWithoutEncoding(segment);
			}
		}

		refresh();
		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refresh();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		items.add(
				new RepositoryMenuItemModel(
						"FILES_COMMITS",
						R.string.commits,
						R.drawable.ic_commit,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		items.add(
				new RepositoryMenuItemModel(
						"FILES_SWITCH_BRANCH",
						R.string.branches,
						R.drawable.ic_branch,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		items.add(
				new RepositoryMenuItemModel(
						"FILES_SEARCH",
						R.string.search,
						R.drawable.ic_search,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		if (repository.getPermissions().isAdmin() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"FILES_ADD_NEW",
							R.string.pageTitleNewFile,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		switch (actionId) {
			case "FILES_COMMITS":
				startActivity(repository.getIntent(getContext(), CommitsActivity.class));
				break;

			case "FILES_SWITCH_BRANCH":
				chooseBranch();
				break;

			case "FILES_SEARCH":
				binding.searchView.show();
				break;

			case "FILES_ADD_NEW":
				BottomSheetCreateFile.newInstance(
								repository, FilesViewModel.FileAction.CREATE, null, null, null)
						.show(getChildFragmentManager(), "CREATE_FILE");
				break;
		}
	}

	public void refreshFromGlobal() {
		refresh();
	}

	@Override
	public void onMenuClick(RepoGetContentsList file) {
		showFileOptionsBottomSheet(file);
	}

	@Override
	public void onSearchFilterCompleted(int count) {
		boolean isSearching = binding.searchView.isShowing();
		if (isSearching) {
			binding.searchResultsRecycler.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
		} else {
			binding.recyclerView.setVisibility(count == 0 ? View.GONE : View.VISIBLE);
		}
	}

	private void setupRecyclerView() {
		filesAdapter = new FilesAdapter(requireContext(), this);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setAdapter(filesAdapter);

		binding.searchResultsRecycler.setHasFixedSize(true);
		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.searchResultsRecycler.setAdapter(filesAdapter);
	}

	private void setupListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refresh);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}

							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filesAdapter.getFilter().filter(s);
							}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState == SearchView.TransitionState.HIDDEN) {
						filesAdapter.getFilter().filter("");
						boolean hasFiles = filesAdapter.getItemCount() > 0;
						binding.recyclerView.setVisibility(hasFiles ? View.VISIBLE : View.GONE);
					}
				});
	}

	private void setupBackNavigation() {
		requireActivity()
				.getOnBackPressedDispatcher()
				.addCallback(
						getViewLifecycleOwner(),
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								if (binding.searchView.isShowing()) {
									binding.searchView.hide();
									return;
								}
								if (path.size() > 0) {
									path.remove(path.size() - 1);
									refresh();
								} else {
									setEnabled(false);
									requireActivity().getOnBackPressedDispatcher().onBackPressed();
								}
							}
						});
	}

	private void observeViewModel() {
		viewModel
				.getFiles()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							binding.pullToRefresh.setRefreshing(false);
							filesAdapter.setFiles(list);

							boolean isEmpty = list.isEmpty();
							binding.recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
							binding.filesFrame.setVisibility(View.VISIBLE);
							binding.layoutEmpty
									.getRoot()
									.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getIsFilesLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) {
								binding.recyclerView.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) Toasty.show(requireContext(), msg);
						});
	}

	public void refresh() {
		viewModel.loadFiles(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				repository.getBranchRef(),
				path.toString());
	}

	private void observeFileContent() {
		viewModel
				.getFileContent()
				.observe(
						getViewLifecycleOwner(),
						data -> {
							if (data != null && pendingFile != null) {
								String action = pendingAction;
								RepoGetContentsList file = pendingFile;

								viewModel.clearFileContent();
								pendingAction = null;
								pendingFile = null;

								binding.expressiveLoader.setVisibility(View.GONE);

								if ("view".equals(action)) {
									openViewer(file, data);
								} else if ("edit".equals(action)) {
									if (!data.isBinary && data.textContent != null) {
										BottomSheetCreateFile.newInstance(
														repository,
														FilesViewModel.FileAction.EDIT,
														file.getPath(),
														file.getSha(),
														data.textContent)
												.show(getChildFragmentManager(), "EDIT_FILE");
									}
								}
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null && pendingFile != null) {
								binding.expressiveLoader.setVisibility(View.GONE);
								pendingAction = null;
								pendingFile = null;
							}
						});
	}

	private boolean isBinaryFileType(AppUtil.FileType fileType) {
		return fileType != AppUtil.FileType.TEXT
				&& fileType != AppUtil.FileType.IMAGE
				&& fileType != AppUtil.FileType.UNKNOWN;
	}

	private boolean isEditableFileType(AppUtil.FileType fileType) {
		return fileType == AppUtil.FileType.TEXT;
	}

	@Override
	public void onClickFile(RepoGetContentsList file) {
		switch (file.getType()) {
			case "dir":
				path.addWithoutEncoding(file.getName());
				refresh();
				break;
			case "file":
			case "symlink":
				openFileViewer(file);
				break;
			case "submodule":
				handleSubmodule(file);
				break;
		}
	}

	public void openFileViewer(RepoGetContentsList file) {
		String fileExtension = FilenameUtils.getExtension(file.getName());
		AppUtil.FileType fileType = AppUtil.getFileType(fileExtension);

		if (isBinaryFileType(fileType)) {
			showFileOptionsBottomSheet(file);
			return;
		}

		pendingAction = "view";
		pendingFile = file;
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		boolean isImage = fileType == AppUtil.FileType.IMAGE;
		viewModel.fetchFileContent(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				file.getPath(),
				repository.getBranchRef(),
				isImage);
	}

	private void openFileForEdit(RepoGetContentsList file) {
		String fileExtension = FilenameUtils.getExtension(file.getName());
		AppUtil.FileType fileType = AppUtil.getFileType(fileExtension);

		if (!isEditableFileType(fileType)) {
			Toasty.show(requireContext(), R.string.fileTypeCannotBeEdited);
			return;
		}

		pendingAction = "edit";
		pendingFile = file;
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		viewModel.fetchFileContent(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				file.getPath(),
				repository.getBranchRef(),
				false);
	}

	public void openViewerLinkIntent(RepoGetContentsList file) {
		openFileViewer(file);
	}

	private void openViewer(RepoGetContentsList file, FilesViewModel.FileContentData data) {
		String fileExtension = FilenameUtils.getExtension(file.getName());
		String fileName = file.getName();
		AppUtil.FileType fileType = AppUtil.getFileType(fileExtension);

		binding.expressiveLoader.setVisibility(View.GONE);

		if (fileType == AppUtil.FileType.IMAGE && data.binaryContent != null) {
			BottomSheetContentViewer.newInstance(
							data.binaryContent,
							fileName,
							repository,
							BottomSheetContentViewer.Feature.IMAGE_PREVIEW,
							BottomSheetContentViewer.Feature.SHOW_TITLE,
							BottomSheetContentViewer.Feature.ALLOW_SHARE)
					.show(getChildFragmentManager(), "FILE_VIEWER");
			return;
		}

		String content = data.textContent != null ? data.textContent : "";

		if (fileExtension.equalsIgnoreCase("md")) {
			BottomSheetContentViewer.newInstance(
							content,
							fileName,
							repository,
							fileExtension,
							BottomSheetContentViewer.Feature.MARKDOWN_PREVIEW,
							BottomSheetContentViewer.Feature.START_IN_MARKDOWN,
							BottomSheetContentViewer.Feature.SHOW_TITLE,
							BottomSheetContentViewer.Feature.ALLOW_COPY,
							BottomSheetContentViewer.Feature.ALLOW_SHARE)
					.show(getChildFragmentManager(), "FILE_VIEWER");
			return;
		}

		if (fileType == AppUtil.FileType.TEXT) {
			BottomSheetContentViewer.newInstance(
							content,
							fileName,
							repository,
							fileExtension,
							BottomSheetContentViewer.Feature.SYNTAX_HIGHLIGHT,
							BottomSheetContentViewer.Feature.SHOW_TITLE,
							BottomSheetContentViewer.Feature.ALLOW_COPY,
							BottomSheetContentViewer.Feature.ALLOW_SHARE)
					.show(getChildFragmentManager(), "FILE_VIEWER");
			return;
		}

		BottomSheetContentViewer.newInstance(
						content,
						fileName,
						repository,
						null,
						BottomSheetContentViewer.Feature.SHOW_TITLE,
						BottomSheetContentViewer.Feature.ALLOW_COPY,
						BottomSheetContentViewer.Feature.ALLOW_SHARE)
				.show(getChildFragmentManager(), "FILE_VIEWER");
	}

	private void showFileOptionsBottomSheet(RepoGetContentsList file) {
		BottomsheetFileItemMenuBinding sheetBinding =
				BottomsheetFileItemMenuBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
		dialog.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(dialog, true);

		sheetBinding.fileName.setText(file.getName());

		String fileExtension = FilenameUtils.getExtension(file.getName());
		AppUtil.FileType fileType = AppUtil.getFileType(fileExtension);
		boolean canEdit =
				repository.getPermissions().isPush()
						&& !repository.getRepository().isArchived()
						&& isEditableFileType(fileType);

		sheetBinding.editFileCard.setVisibility(canEdit ? View.VISIBLE : View.GONE);
		sheetBinding.deleteFileCard.setVisibility(
				repository.getPermissions().isPush() && !repository.getRepository().isArchived()
						? View.VISIBLE
						: View.GONE);

		sheetBinding.editFile.setOnClickListener(
				v -> {
					dialog.dismiss();
					openFileForEdit(file);
				});

		sheetBinding.deleteFile.setOnClickListener(
				v -> {
					dialog.dismiss();
					BottomSheetCreateFile.newInstance(
									repository,
									FilesViewModel.FileAction.DELETE,
									file.getPath(),
									file.getSha(),
									null)
							.show(getChildFragmentManager(), "DELETE_FILE");
				});

		sheetBinding.downloadFile.setOnClickListener(
				v -> {
					dialog.dismiss();
					requestFileDownload(file);
				});

		sheetBinding.shareFile.setOnClickListener(
				v -> {
					dialog.dismiss();
					AppUtil.sharingIntent(requireContext(), file.getHtmlUrl());
				});

		sheetBinding.copyUrl.setOnClickListener(
				v -> {
					dialog.dismiss();
					AppUtil.copyToClipboard(
							requireContext(),
							file.getHtmlUrl(),
							getString(R.string.copied_to_clipboard));
				});

		sheetBinding.openBrowser.setOnClickListener(
				v -> {
					dialog.dismiss();
					AppUtil.openUrlInBrowser(requireContext(), file.getHtmlUrl());
				});

		dialog.show();
	}

	private void handleSubmodule(RepoGetContentsList file) {
		String rawUrl = file.getSubmoduleGitUrl();
		if (rawUrl == null) return;

		Uri url = AppUtil.getUriFromGitUrl(rawUrl);
		String host = url.getHost();
		if (host == null) return;

		UserAccountsApi userAccountsApi =
				BaseApi.getInstance(requireContext(), UserAccountsApi.class);

		if (userAccountsApi == null) return;

		List<UserAccount> userAccounts = userAccountsApi.loggedInUserAccounts();

		if (userAccounts == null || userAccounts.isEmpty()) {
			AppUtil.openUrlInBrowser(requireContext(), url.toString());
			return;
		}

		UserAccount targetAccount = null;

		for (UserAccount account : userAccounts) {
			Uri instanceUri = Uri.parse(account.getInstanceUrl());
			if (Objects.requireNonNull(instanceUri.getHost()).equalsIgnoreCase(host)) {
				targetAccount = account;
				if (!Objects.equals(url.getScheme(), instanceUri.getScheme())) {
					url = AppUtil.changeScheme(url, instanceUri.getScheme());
				}
				break;
			}
		}

		if (targetAccount != null) {
			AppUtil.switchToAccount(requireContext(), targetAccount, true);
			List<String> pathSegments = url.getPathSegments();
			if (pathSegments.size() < 2) {
				AppUtil.openUrlInBrowser(requireContext(), url.toString());
				return;
			}

			String owner = pathSegments.get(pathSegments.size() - 2);
			String repo = pathSegments.get(pathSegments.size() - 1);
			if (repo.endsWith(".git")) repo = repo.substring(0, repo.length() - 4);

			startActivity(
					new RepositoryContext(owner, repo, requireContext())
							.getIntent(requireContext(), RepoDetailActivity.class));
		} else {
			AppUtil.openUrlInBrowser(requireContext(), url.toString());
		}
	}

	private void chooseBranch() {
		BottomsheetBranchPicker picker =
				BottomsheetBranchPicker.newInstance(
						repository.getOwner(), repository.getName(), repository.getBranchRef());

		picker.setOnBranchSelectedListener(
				branchName -> {
					repository.setBranchRef(branchName);
					path.clear();
					refresh();
				});

		picker.show(getChildFragmentManager(), "branch_picker");
	}

	private void requestFileDownload(RepoGetContentsList file) {
		pendingDownloadFile = file;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, file.getName());
		intent.setType("*/*");
		downloadLauncher.launch(intent);
	}

	private final ActivityResultLauncher<Intent> downloadLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							Uri targetUri = result.getData().getData();
							if (targetUri != null && pendingDownloadFile != null) {
								downloadFileToUri(pendingDownloadFile, targetUri);
								pendingDownloadFile = null;
							}
						}
					});

	private void downloadFileToUri(RepoGetContentsList file, Uri targetUri) {
		NotificationManager notificationManager =
				(NotificationManager)
						requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
		int notificationId = Notifications.uniqueNotificationId(requireContext());

		NotificationCompat.Builder builder =
				new NotificationCompat.Builder(
								requireContext(), Constants.downloadNotificationChannelId)
						.setContentTitle(getString(R.string.download_started_title))
						.setContentText(getString(R.string.download_started_desc, file.getName()))
						.setSmallIcon(R.drawable.gitnex_transparent)
						.setPriority(NotificationCompat.PRIORITY_LOW)
						.setOngoing(true)
						.setProgress(100, 0, false);

		notificationManager.notify(notificationId, builder.build());

		new Thread(
						() -> {
							try {
								Call<ResponseBody> call =
										RetrofitClient.getWebInterface(requireContext())
												.getFileContents(
														repository.getOwner(),
														repository.getName(),
														repository.getBranchRef(),
														file.getPath());

								retrofit2.Response<ResponseBody> response = call.execute();

								if (response.isSuccessful() && response.body() != null) {
									try (OutputStream os =
											requireContext()
													.getContentResolver()
													.openOutputStream(targetUri)) {
										AppUtil.copyProgress(
												response.body().byteStream(),
												os,
												file.getSize(),
												progress -> {
													builder.setProgress(100, progress, false);
													notificationManager.notify(
															notificationId, builder.build());
												});

										builder.setContentTitle(
														getString(R.string.download_complete_title))
												.setContentText(
														getString(
																R.string.download_complete_desc,
																file.getName()))
												.setOngoing(false)
												.setProgress(0, 0, false);
										notificationManager.notify(notificationId, builder.build());

										requireActivity()
												.runOnUiThread(
														() ->
																Toasty.show(
																		requireContext(),
																		R.string
																				.downloadFileSaved));
									}
								} else {
									throw new IOException("Download failed: " + response.code());
								}
							} catch (Exception e) {
								builder.setContentTitle(getString(R.string.download_failed_title))
										.setContentText(
												getString(
														R.string.download_failed_desc,
														file.getName()))
										.setOngoing(false)
										.setProgress(0, 0, false);
								notificationManager.notify(notificationId, builder.build());

								requireActivity()
										.runOnUiThread(
												() ->
														Toasty.show(
																requireContext(),
																R.string.download_failed_title));
							}
						})
				.start();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
