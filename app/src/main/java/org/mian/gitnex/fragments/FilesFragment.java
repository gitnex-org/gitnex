package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import org.gitnex.tea4j.v2.models.Branch;
import org.gitnex.tea4j.v2.models.ContentsResponse;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateFileActivity;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Path;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.FilesViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class FilesFragment extends Fragment implements FilesAdapter.FilesAdapterListener {

	private final Path path = new Path();
	private FragmentFilesBinding binding;
	private RepositoryContext repository;
	private FilesAdapter filesAdapter;

	public FilesFragment() {}

	public static FilesFragment newInstance(RepositoryContext repository) {

		FilesFragment fragment = new FilesFragment();
		fragment.setArguments(repository.getBundle());

		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		repository = RepositoryContext.fromBundle(requireArguments());
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentFilesBinding.inflate(inflater, container, false);

		boolean canPush = repository.getPermissions().isPush();
		boolean archived = repository.getRepository().isArchived();

		filesAdapter = new FilesAdapter(getContext(), this);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.recyclerView.setAdapter(filesAdapter);

		binding.branchTitle.setText(repository.getBranchRef());

		binding.breadcrumbsView.setItems(
				new ArrayList<>(
						Collections.singletonList(
								BreadcrumbItem.createSimpleItem(repository.getBranchRef()))));
		// noinspection unchecked
		binding.breadcrumbsView.setCallback(
				new DefaultBreadcrumbsCallback<BreadcrumbItem>() {

					@SuppressLint("SetTextI18n")
					@Override
					public void onNavigateBack(BreadcrumbItem item, int position) {

						if (position == 0) {
							path.clear();
						} else {
							path.pop(path.size() - position);
						}
						refresh();
					}

					@Override
					public void onNavigateNewLocation(
							BreadcrumbItem newItem, int changedPosition) {}
				});

		requireActivity()
				.getOnBackPressedDispatcher()
				.addCallback(
						getViewLifecycleOwner(),
						new OnBackPressedCallback(true) {

							@Override
							public void handleOnBackPressed() {
								if (path.size() == 0
										|| ((RepoDetailActivity) requireActivity())
														.viewPager.getCurrentItem()
												!= 1) {
									requireActivity().finish();
									return;
								}
								path.remove(path.size() - 1);
								binding.breadcrumbsView.removeLastItem();
								if (path.size() == 0) {
									fetchDataAsync(
											repository.getOwner(),
											repository.getName(),
											repository.getBranchRef());
								} else {
									fetchDataAsyncSub(
											repository.getOwner(),
											repository.getName(),
											path.toString(),
											repository.getBranchRef());
								}
							}
						});

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					refresh();
					binding.pullToRefresh.setRefreshing(false);
				});

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerFiles(
						repoBranch -> {
							repository.setBranchRef(repoBranch);
							path.clear();
							binding.breadcrumbsView.setItems(
									new ArrayList<>(
											Collections.singletonList(
													BreadcrumbItem.createSimpleItem(
															repository.getBranchRef()))));
							refresh();
						});

		String dir = requireActivity().getIntent().getStringExtra("dir");
		if (dir != null) {
			for (String segment : dir.split("/")) {
				binding.breadcrumbsView.addItem(
						new BreadcrumbItem(Collections.singletonList(segment)));
				path.add(segment);
			}
		}
		refresh();

		if (!canPush || archived) {
			binding.newFile.setVisibility(View.GONE);
		}

		binding.newFile.setOnClickListener(
				v17 -> startActivity(repository.getIntent(getContext(), CreateFileActivity.class)));

		binding.switchBranch.setOnClickListener(switchBranch -> chooseBranch());

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
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new SearchView.OnQueryTextListener() {

											@Override
											public boolean onQueryTextChange(String newText) {

												if (binding.recyclerView.getAdapter() != null) {
													filesAdapter.getFilter().filter(newText);
												}

												return false;
											}

											@Override
											public boolean onQueryTextSubmit(String query) {
												return false;
											}
										});
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		return binding.getRoot();
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
	public void onClickFile(ContentsResponse file) {

		switch (file.getType()) {
			case "dir":
				path.addWithoutEncoding(file.getName());
				binding.breadcrumbsView.addItem(
						new BreadcrumbItem(Collections.singletonList(file.getName())));
				refresh();
				break;

			case "file":
			case "symlink":
				Intent intent = repository.getIntent(getContext(), FileViewActivity.class);
				intent.putExtra("file", file);

				requireContext().startActivity(intent);
				break;

			case "submodule":
				String rawUrl = file.getSubmoduleGitUrl();
				if (rawUrl == null) {
					return;
				}
				Uri url = AppUtil.getUriFromGitUrl(rawUrl);
				String host = url.getHost();

				UserAccountsApi userAccountsApi =
						BaseApi.getInstance(requireContext(), UserAccountsApi.class);
				assert userAccountsApi != null;
				List<UserAccount> userAccounts = userAccountsApi.loggedInUserAccounts();
				UserAccount account = null;

				for (UserAccount userAccount : userAccounts) {
					Uri instanceUri = Uri.parse(userAccount.getInstanceUrl());
					if (Objects.requireNonNull(instanceUri.getHost()).toLowerCase().equals(host)) {
						account = userAccount;
						// if scheme is wrong fix it
						if (!Objects.equals(url.getScheme(), instanceUri.getScheme())) {
							url = AppUtil.changeScheme(url, instanceUri.getScheme());
						}
						break;
					}
				}

				if (account != null) {
					AppUtil.switchToAccount(requireContext(), account, true);
					List<String> splittedUrl = url.getPathSegments();
					if (splittedUrl.size() < 2) {
						AppUtil.openUrlInBrowser(requireContext(), url.toString());
					}
					String owner = splittedUrl.get(splittedUrl.size() - 2);
					String repo = splittedUrl.get(splittedUrl.size() - 1);
					if (repo.endsWith(".git")) { // Git clone URL
						repo = repo.substring(0, repo.length() - 4);
					}

					startActivity(
							new RepositoryContext(owner, repo, requireContext())
									.getIntent(requireContext(), RepoDetailActivity.class));
				} else {
					AppUtil.openUrlInBrowser(requireContext(), url.toString());
				}
				break;
		}
	}

	public void refresh() {
		if (path.size() > 0) {
			fetchDataAsyncSub(
					repository.getOwner(),
					repository.getName(),
					path.toString(),
					repository.getBranchRef());
		} else {
			fetchDataAsync(repository.getOwner(), repository.getName(), repository.getBranchRef());
		}
	}

	private void fetchDataAsync(String owner, String repo, String ref) {

		binding.recyclerView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);

		filesModel
				.getFilesList(
						owner, repo, ref, getContext(), binding.progressBar, binding.noDataFiles)
				.observe(
						getViewLifecycleOwner(),
						filesListMain -> {
							filesAdapter.getOriginalFiles().clear();
							filesAdapter.getOriginalFiles().addAll(filesListMain);
							filesAdapter.notifyOriginalDataSetChanged();

							if (!filesListMain.isEmpty()) {

								AppUtil.setMultiVisibility(
										View.VISIBLE, binding.recyclerView, binding.filesFrame);
								binding.noDataFiles.setVisibility(View.GONE);

							} else {
								AppUtil.setMultiVisibility(
										View.VISIBLE,
										binding.recyclerView,
										binding.filesFrame,
										binding.noDataFiles);
							}

							binding.filesFrame.setVisibility(View.VISIBLE);
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void fetchDataAsyncSub(String owner, String repo, String filesDir, String ref) {

		binding.recyclerView.setVisibility(View.GONE);
		binding.progressBar.setVisibility(View.VISIBLE);

		FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);
		filesModel
				.getFilesList2(
						owner,
						repo,
						filesDir,
						ref,
						getContext(),
						binding.progressBar,
						binding.noDataFiles)
				.observe(
						getViewLifecycleOwner(),
						filesListMain2 -> {
							filesAdapter.getOriginalFiles().clear();
							filesAdapter.getOriginalFiles().addAll(filesListMain2);
							filesAdapter.notifyOriginalDataSetChanged();

							if (!filesListMain2.isEmpty()) {

								AppUtil.setMultiVisibility(
										View.VISIBLE, binding.recyclerView, binding.filesFrame);
								binding.noDataFiles.setVisibility(View.GONE);
							} else {
								AppUtil.setMultiVisibility(
										View.VISIBLE,
										binding.recyclerView,
										binding.filesFrame,
										binding.noDataFiles);
							}

							binding.filesFrame.setVisibility(View.VISIBLE);
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void chooseBranch() {

		Dialog progressDialog = new Dialog(requireContext());
		progressDialog.setCancelable(false);
		progressDialog.setContentView(R.layout.custom_progress_loader);
		progressDialog.show();

		MaterialAlertDialogBuilder materialAlertDialogBuilder =
				new MaterialAlertDialogBuilder(
						requireContext(), R.style.ThemeOverlay_Material3_Dialog_Alert);

		Call<List<Branch>> call =
				RetrofitClient.getApiInterface(requireContext())
						.repoListBranches(repository.getOwner(), repository.getName(), null, null);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Branch>> call,
							@NonNull Response<List<Branch>> response) {

						progressDialog.hide();
						if (response.code() == 200) {

							List<String> branchesList = new ArrayList<>();
							int selectedBranch = 0;
							assert response.body() != null;

							for (int i = 0; i < response.body().size(); i++) {

								Branch branches = response.body().get(i);
								branchesList.add(branches.getName());

								if (repository.getBranchRef().equals(branches.getName())) {
									selectedBranch = i;
								}
							}

							materialAlertDialogBuilder
									.setTitle(R.string.pageTitleChooseBranch)
									.setSingleChoiceItems(
											branchesList.toArray(new String[0]),
											selectedBranch,
											(dialogInterface, i) -> {
												repository.setBranchRef(branchesList.get(i));
												binding.branchTitle.setText(branchesList.get(i));

												refresh();
												dialogInterface.dismiss();
											})
									.setNeutralButton(R.string.cancelButton, null);
							materialAlertDialogBuilder.create().show();
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Branch>> call, @NonNull Throwable t) {
						progressDialog.hide();
					}
				});
	}
}
