package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author mmarif
 */
public class OrganizationRepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding binding;
	private RepositoriesViewModel viewModel;
	private ReposListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private OrganizationPermissions permissions;
	private String orgName;
	private int resultLimit;
	private boolean isSearching = false;

	public OrganizationRepositoriesFragment() {}

	public static OrganizationRepositoriesFragment newInstance(
			String orgName, OrganizationPermissions permissions) {
		OrganizationRepositoriesFragment fragment = new OrganizationRepositoriesFragment();
		Bundle args = new Bundle();
		args.putString("orgName", orgName);
		args.putSerializable("permissions", permissions);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString("orgName");
			permissions = (OrganizationPermissions) getArguments().getSerializable("permissions");
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepositoriesBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupMenu();
		observeViewModel();
		setupFab();

		refreshData();
		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new ReposListAdapter(new ArrayList<>(), requireContext());
		adapter.isUserOrg = true;

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (!isSearching) {
							viewModel.fetchRepos(
									requireContext(),
									"org",
									"",
									orgName,
									page,
									resultLimit,
									null,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupFab() {
		if (permissions != null && !permissions.isCanCreateRepository()) {
			binding.addNewRepo.setVisibility(View.GONE);
		}

		binding.addNewRepo.setOnClickListener(
				v -> {
					Intent intent = new Intent(getContext(), CreateRepoActivity.class);
					intent.putExtra("organizationAction", true);
					intent.putExtra("orgName", orgName);
					if (requireActivity().getIntent().getExtras() != null) {
						intent.putExtras(
								Objects.requireNonNull(requireActivity().getIntent().getExtras()));
					}
					startActivity(intent);
				});
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								MenuItem searchItem = menu.findItem(R.id.action_search);
								SearchView searchView = (SearchView) searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new SearchView.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													isSearching = !newText.isEmpty();
													adapter.getFilter().filter(newText);
													return true;
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

	private void observeViewModel() {
		viewModel
				.getRepos()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;
							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);
						});

		viewModel.getHasLoadedOnce().observe(getViewLifecycleOwner(), hasLoaded -> updateUiState());
	}

	private void updateUiState() {
		boolean isEmpty = adapter.getItemCount() == 0;
		boolean loaded = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());
		binding.layoutEmpty.getRoot().setVisibility(loaded && isEmpty ? View.VISIBLE : View.GONE);
	}

	private void refreshData() {
		if (scrollListener != null) scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchRepos(requireContext(), "org", "", orgName, 1, resultLimit, null, true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (OrganizationDetailActivity.updateOrgFABActions) {
			refreshData();
			OrganizationDetailActivity.updateOrgFABActions = false;
		}
	}
}
