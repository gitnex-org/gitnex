package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.search.SearchView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitsActivity;
import org.mian.gitnex.activities.CreateFileActivity;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Path;
import org.mian.gitnex.helpers.RepositoryMenuItemModel;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.FilesViewModel;

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

	public static FilesFragment newInstance(RepositoryContext repository) {
		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
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
							R.string.addButton,
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
				startActivity(repository.getIntent(getContext(), CreateFileActivity.class));
				break;
		}
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

	@Override
	public void onResume() {
		super.onResume();
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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
