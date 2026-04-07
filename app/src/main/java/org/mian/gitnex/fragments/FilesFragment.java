package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.BranchAdapter;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Path;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.FilesViewModel;

/**
 * @author mmarif
 */
public class FilesFragment extends Fragment implements FilesAdapter.FilesAdapterListener {

	private FragmentFilesBinding binding;
	private FilesViewModel viewModel;
	private FilesAdapter filesAdapter;
	private RepositoryContext repository;
	private final Path path = new Path();

	public static FilesFragment newInstance(RepositoryContext repository) {
		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
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

		String dir = requireActivity().getIntent().getStringExtra("dir");
		if (dir != null && path.size() == 0) {
			for (String segment : dir.split("/")) {
				path.addWithoutEncoding(segment);
			}
		}

		refresh();
		return binding.getRoot();
	}

	private void setupRecyclerView() {
		filesAdapter = new FilesAdapter(requireContext(), this);
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setAdapter(filesAdapter);
	}

	private void setupListeners() {
		binding.branchTitle.setText(repository.getBranchRef());

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					refresh();
					binding.pullToRefresh.setRefreshing(false);
				});

		// FAB Logic
		boolean canPush = repository.getPermissions().isPush();
		boolean archived = repository.getRepository().isArchived();
		// binding.newFile.setVisibility(!canPush || archived ? View.GONE : View.VISIBLE);
		// binding.newFile.setOnClickListener(v ->
		//	startActivity(repository.getIntent(getContext(), CreateFileActivity.class)));

		binding.switchBranch.setOnClickListener(v -> chooseBranch());

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerFiles(
						repoBranch -> {
							repository.setBranchRef(repoBranch);
							binding.branchTitle.setText(repoBranch);
							path.clear();
							refresh();
						});

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.files_switch_branches_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new androidx.appcompat.widget.SearchView
													.OnQueryTextListener() {
												@Override
												public boolean onQueryTextChange(String newText) {
													filesAdapter.getFilter().filter(newText);
													return false;
												}

												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}
											});
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
	}

	private void setupBackNavigation() {
		requireActivity()
				.getOnBackPressedDispatcher()
				.addCallback(
						getViewLifecycleOwner(),
						new OnBackPressedCallback(true) {
							@Override
							public void handleOnBackPressed() {
								if (path.size() > 0
										&& ((RepoDetailActivity) requireActivity())
														.viewPager.getCurrentItem()
												== 1) {
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

	@Override
	public void onResume() {
		super.onResume();
		if (RepoDetailActivity.updateFABActions) {
			refresh();
			RepoDetailActivity.updateFABActions = false;
		}
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
				Intent intent = repository.getIntent(getContext(), FileViewActivity.class);
				intent.putExtra("file", file);
				startActivity(intent);
				break;
			case "submodule":
				handleSubmodule(file);
				break;
		}
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
		viewModel.resetBranches();

		Dialog progressDialog = new Dialog(requireContext());
		progressDialog.setCancelable(false);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
		View dialogView = getLayoutInflater().inflate(R.layout.custom_branches_dialog, null);
		dialogBuilder.setView(dialogView);

		RecyclerView rvBranches = dialogView.findViewById(R.id.recyclerView);
		rvBranches.setLayoutManager(new LinearLayoutManager(requireContext()));

		BranchAdapter branchAdapter =
				new BranchAdapter(
						branchName -> {
							repository.setBranchRef(branchName);
							binding.branchTitle.setText(branchName);
							path.clear();
							refresh();
						});
		rvBranches.setAdapter(branchAdapter);

		dialogBuilder.setNeutralButton(R.string.close, (d, which) -> d.dismiss());
		AlertDialog branchDialog = dialogBuilder.create();

		viewModel
				.getBranches()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (list != null) {
								branchAdapter.setBranches(list);
								if (!list.isEmpty() && progressDialog.isShowing()) {
									progressDialog.dismiss();
									branchDialog.show();
								}
							}
						});

		rvBranches.addOnScrollListener(
				new RecyclerView.OnScrollListener() {
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
						super.onScrolled(recyclerView, dx, dy);

						LinearLayoutManager lm =
								(LinearLayoutManager) recyclerView.getLayoutManager();
						if (lm != null) {
							int visibleItemCount = lm.getChildCount();
							int totalItemCount = lm.getItemCount();
							int firstVisibleItemPosition = lm.findFirstVisibleItemPosition();

							boolean isLoading =
									Boolean.TRUE.equals(
											viewModel.getIsBranchesLoading().getValue());
							boolean isLastPage =
									Boolean.TRUE.equals(
											viewModel.getIsLastPageBranches().getValue());

							if (!isLoading && !isLastPage) {
								if ((visibleItemCount + firstVisibleItemPosition)
												>= totalItemCount - 5
										&& firstVisibleItemPosition >= 0) {

									viewModel.loadBranches(
											requireContext(),
											repository.getOwner(),
											repository.getName());
								}
							}
						}
					}
				});

		viewModel.loadBranches(requireContext(), repository.getOwner(), repository.getName());
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
