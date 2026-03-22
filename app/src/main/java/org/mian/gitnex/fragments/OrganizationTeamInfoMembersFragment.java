package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.mian.gitnex.activities.AddNewTeamMemberActivity;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationTeamInfoMembersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author opyale
 */
public class OrganizationTeamInfoMembersFragment extends Fragment {

	private FragmentOrganizationTeamInfoMembersBinding binding;
	private UserListViewModel viewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private Team team;
	private int resultLimit;
	public static boolean refreshMembers = false;

	public OrganizationTeamInfoMembersFragment() {}

	public static OrganizationTeamInfoMembersFragment newInstance(Team team) {
		OrganizationTeamInfoMembersFragment fragment = new OrganizationTeamInfoMembersFragment();
		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			team = (Team) getArguments().getSerializable("team");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentOrganizationTeamInfoMembersBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		setupFab();
		observeViewModel();

		if (viewModel.getUsers().getValue() == null || viewModel.getUsers().getValue().isEmpty()) {
			refreshData();
		}

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new UsersAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUsers(
								requireContext(),
								"team_members",
								null,
								String.valueOf(team.getId()),
								null,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupFab() {
		OrganizationPermissions permissions =
				(OrganizationPermissions)
						requireActivity().getIntent().getSerializableExtra("permissions");

		if (permissions != null && permissions.isIsOwner()) {
			binding.addNewMember.setVisibility(View.VISIBLE);
			binding.addNewMember.setOnClickListener(
					v -> {
						Intent intent =
								new Intent(requireContext(), AddNewTeamMemberActivity.class);
						intent.putExtra("teamId", team.getId());
						startActivity(intent);
					});
		} else {
			binding.addNewMember.setVisibility(View.GONE);
		}
	}

	private void observeViewModel() {
		viewModel
				.getUsers()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);
		binding.layoutEmpty
				.getRoot()
				.setVisibility(!isLoading && !hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		binding.recyclerView.setVisibility(
				!hasData && !isLoading && hasLoadedOnce ? View.GONE : View.VISIBLE);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchUsers(
				requireContext(),
				"team_members",
				null,
				String.valueOf(team.getId()),
				null,
				1,
				resultLimit,
				true);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (refreshMembers) {
			refreshData();
			refreshMembers = false;
		}
	}
}
