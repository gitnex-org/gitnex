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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AddNewTeamRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author M M Arif
 */
public class OrganizationTeamInfoReposFragment extends Fragment {

	public static boolean repoAdded = false;
	private RepositoriesViewModel repositoriesViewModel;
	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private int resultLimit;

	private Team team;

	public static OrganizationTeamInfoReposFragment newInstance(Team team) {
		OrganizationTeamInfoReposFragment fragment = new OrganizationTeamInfoReposFragment();

		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		bundle.putBoolean("showRepo", !team.isIncludesAllRepositories());
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding =
				FragmentRepositoriesBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(getContext());
		setHasOptionsMenu(true);
		team = (Team) requireArguments().getSerializable("team");

		repositoriesViewModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		fragmentRepositoriesBinding.addNewRepo.setText(R.string.pageTitleAddRepository);

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

		fetchDataAsync();

		OrganizationPermissions permissions =
			(OrganizationPermissions) requireActivity().getIntent().getSerializableExtra("permissions");

		if (!requireArguments().getBoolean("showRepo") || !permissions.isIsOwner()) {
			fragmentRepositoriesBinding.addNewRepo.setVisibility(View.GONE);
		}
		fragmentRepositoriesBinding.addNewRepo.setOnClickListener(
			v1 -> {
				Intent intent =
					new Intent(getContext(), AddNewTeamRepoActivity.class);
				intent.putExtra("teamId", team.getId());
				intent.putExtra("teamName", team.getName());
				intent.putExtra("orgName", requireActivity().getIntent().getStringExtra("orgName"));
				startActivity(intent);
			});

		return fragmentRepositoriesBinding.getRoot();
	}

	private void fetchDataAsync() {

		repositoriesViewModel
				.getRepositories(
						page, resultLimit, String.valueOf(team.getId()), "team", null, getContext(), fragmentRepositoriesBinding)
				.observe(
						getViewLifecycleOwner(),
						reposListMain -> {
							adapter = new ReposListAdapter(reposListMain, getContext());
							adapter.setLoadMoreListener(
									new ReposListAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											repositoriesViewModel.loadMoreRepos(
													page,
													resultLimit,
													String.valueOf(team.getId()),
													"team",
													null,
													getContext(),
													adapter);
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

		if (repoAdded) {
			page = 1;
			fetchDataAsync();
			MainActivity.reloadRepos = false;
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView =
				(androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(
				new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						if (fragmentRepositoriesBinding.recyclerView.getAdapter() != null) {
							adapter.getFilter().filter(newText);
						}
						return false;
					}
				});
	}
}
