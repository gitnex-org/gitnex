package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Rect;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import org.mian.gitnex.adapters.BranchAdapter;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.UserAccountsApi;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.databinding.FragmentFilesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
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

		MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
		View dialogView = getLayoutInflater().inflate(R.layout.custom_branches_dialog, null);
		dialogBuilder.setView(dialogView);

		RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

		recyclerView.addItemDecoration(
				new RecyclerView.ItemDecoration() {
					@Override
					public void getItemOffsets(
							@NonNull Rect outRect,
							@NonNull View view,
							@NonNull RecyclerView parent,
							@NonNull RecyclerView.State state) {

						int position = parent.getChildAdapterPosition(view);
						int spacing =
								(int)
										requireContext()
												.getResources()
												.getDimension(R.dimen.dimen20dp);

						outRect.right = spacing;
						outRect.left = spacing;

						if (position > 0) {
							outRect.top = spacing;
						}
					}
				});

		dialogBuilder.setNeutralButton(R.string.close, (dialog, which) -> dialog.dismiss());
		AlertDialog dialog = dialogBuilder.create();
		dialog.setCancelable(false);
		dialog.setCanceledOnTouchOutside(false);

		final int[] page = {1};
		final int resultLimit = Constants.getCurrentResultLimit(requireContext());
		final boolean[] isLoading = {false};
		final boolean[] isLastPage = {false};

		BranchAdapter adapter =
				new BranchAdapter(
						branchName -> {
							repository.setBranchRef(branchName);
							binding.branchTitle.setText(branchName);
							refresh();
							dialog.dismiss();
						});
		recyclerView.setAdapter(adapter);

		Runnable fetchBranches =
				() -> {
					if (isLoading[0] || isLastPage[0]) return;
					isLoading[0] = true;

					Call<List<Branch>> call =
							RetrofitClient.getApiInterface(requireContext())
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
}
