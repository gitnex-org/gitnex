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
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateTeamByOrgActivity;
import org.mian.gitnex.adapters.OrganizationTeamsAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationTeamsBinding;
import org.mian.gitnex.viewmodels.TeamsByOrgViewModel;

/**
 * @author M M Arif
 */
public class OrganizationTeamsFragment extends Fragment {

	private TeamsByOrgViewModel teamsByOrgViewModel;
	public static boolean resumeTeams = false;

	private ProgressBar mProgressBar;
	private RecyclerView mRecyclerView;
	private TextView noDataTeams;
	private static final String orgNameF = "param2";
	private String orgName;
	private OrganizationPermissions permissions;
	private OrganizationTeamsAdapter adapter;

	public OrganizationTeamsFragment() {}

	public static OrganizationTeamsFragment newInstance(
			String param1, OrganizationPermissions permissions) {
		OrganizationTeamsFragment fragment = new OrganizationTeamsFragment();
		Bundle args = new Bundle();
		args.putString(orgNameF, param1);
		args.putSerializable("permissions", permissions);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString(orgNameF);
			permissions = (OrganizationPermissions) getArguments().getSerializable("permissions");
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		FragmentOrganizationTeamsBinding fragmentTeamsByOrgBinding =
				FragmentOrganizationTeamsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		teamsByOrgViewModel = new ViewModelProvider(this).get(TeamsByOrgViewModel.class);

		noDataTeams = fragmentTeamsByOrgBinding.noDataTeams;

		final SwipeRefreshLayout swipeRefresh = fragmentTeamsByOrgBinding.pullToRefresh;

		mRecyclerView = fragmentTeamsByOrgBinding.recyclerView;
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mProgressBar = fragmentTeamsByOrgBinding.progressBar;

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											swipeRefresh.setRefreshing(false);
											teamsByOrgViewModel.loadTeamsByOrgList(
													orgName,
													getContext(),
													noDataTeams,
													mProgressBar);
										},
										200));

		fetchDataAsync(orgName);

		if (!permissions.isIsOwner()) {
			fragmentTeamsByOrgBinding.createTeam.setVisibility(View.GONE);
		}

		fragmentTeamsByOrgBinding.createTeam.setOnClickListener(
				v1 -> {
					Intent intentTeam = new Intent(getContext(), CreateTeamByOrgActivity.class);
					intentTeam.putExtras(requireActivity().getIntent().getExtras());
					startActivity(intentTeam);
				});

		return fragmentTeamsByOrgBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (resumeTeams) {
			teamsByOrgViewModel.loadTeamsByOrgList(
					orgName, getContext(), noDataTeams, mProgressBar);
			resumeTeams = false;
		}
	}

	private void fetchDataAsync(String owner) {

		teamsByOrgViewModel
				.getTeamsByOrg(owner, getContext(), noDataTeams, mProgressBar)
				.observe(
						getViewLifecycleOwner(),
						orgTeamsListMain -> {
							adapter =
									new OrganizationTeamsAdapter(
											getContext(), orgTeamsListMain, permissions, orgName);
							if (adapter.getItemCount() > 0) {
								mRecyclerView.setAdapter(adapter);
								noDataTeams.setVisibility(View.GONE);
							} else {
								adapter.notifyDataSetChanged();
								mRecyclerView.setAdapter(adapter);
								noDataTeams.setVisibility(View.VISIBLE);
							}
							mProgressBar.setVisibility(View.GONE);
						});
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
						if (mRecyclerView.getAdapter() != null) {
							adapter.getFilter().filter(newText);
						}
						return false;
					}
				});
	}
}
