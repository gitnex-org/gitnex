package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author M M Arif
 */
public class OrganizationRepositoriesFragment extends Fragment {

	private OrganizationPermissions permissions;
	private RepositoriesViewModel repositoriesViewModel;
	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private static final String getOrgName = null;
	private String orgName;

	public OrganizationRepositoriesFragment() {}

	public static OrganizationRepositoriesFragment newInstance(
			String orgName, OrganizationPermissions permissions) {
		OrganizationRepositoriesFragment fragment = new OrganizationRepositoriesFragment();
		Bundle args = new Bundle();
		args.putString(getOrgName, orgName);
		args.putSerializable("permissions", permissions);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString(getOrgName);
			permissions = (OrganizationPermissions) getArguments().getSerializable("permissions");
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding =
				FragmentRepositoriesBinding.inflate(inflater, container, false);
		repositoriesViewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(getContext());

		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(getContext()));

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											fragmentRepositoriesBinding.pullToRefresh.setRefreshing(
													false);
											fetchDataAsync();
											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.VISIBLE);
										},
										50));

		page = 1;
		fetchDataAsync();

		if (permissions != null) {
			if (!permissions.isCanCreateRepository()) {
				fragmentRepositoriesBinding.addNewRepo.setVisibility(View.GONE);
			}
		}

		fragmentRepositoriesBinding.addNewRepo.setOnClickListener(
				v12 -> {
					Intent intentRepo = new Intent(getContext(), CreateRepoActivity.class);
					intentRepo.putExtra("organizationAction", true);
					intentRepo.putExtra("orgName", orgName);
					intentRepo.putExtras(
							Objects.requireNonNull(requireActivity().getIntent().getExtras()));
					startActivity(intentRepo);
				});

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {

							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

								menuInflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {

											@Override
											public boolean onQueryTextSubmit(String query) {
												return false;
											}

											@Override
											public boolean onQueryTextChange(String newText) {
												if (fragmentRepositoriesBinding.recyclerView
																.getAdapter()
														!= null) {
													adapter.getFilter().filter(newText);
												}
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

		return fragmentRepositoriesBinding.getRoot();
	}

	private void fetchDataAsync() {

		repositoriesViewModel
				.getRepositories(
						page,
						resultLimit,
						"",
						"org",
						orgName,
						getContext(),
						fragmentRepositoriesBinding,
						null)
				.observe(
						getViewLifecycleOwner(),
						reposListMain -> {
							adapter = new ReposListAdapter(reposListMain, getContext());
							adapter.isUserOrg = true;
							adapter.setLoadMoreListener(
									new ReposListAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											repositoriesViewModel.loadMoreRepos(
													page,
													resultLimit,
													"",
													"org",
													orgName,
													getContext(),
													adapter,
													null);
											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentRepositoriesBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
								fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
								fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
							}

							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onResume() {

		super.onResume();

		if (OrganizationDetailActivity.updateOrgFABActions) {
			page = 1;
			fetchDataAsync();
			OrganizationDetailActivity.updateOrgFABActions = false;
		}
	}
}
