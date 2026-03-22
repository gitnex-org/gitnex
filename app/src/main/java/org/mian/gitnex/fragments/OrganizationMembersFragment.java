package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationMembersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.UserListViewModel;

/**
 * @author mmarif
 */
public class OrganizationMembersFragment extends Fragment {

	private FragmentOrganizationMembersBinding binding;
	private UserListViewModel viewModel;
	private UsersAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private static final String BUNDLE_ORG_NAME = "org_name";
	private String orgName;
	private int resultLimit;

	public OrganizationMembersFragment() {}

	public static OrganizationMembersFragment newInstance(String orgName) {
		OrganizationMembersFragment fragment = new OrganizationMembersFragment();
		Bundle args = new Bundle();
		args.putString(BUNDLE_ORG_NAME, orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString(BUNDLE_ORG_NAME);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationMembersBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(UserListViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
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
								"org_members",
								orgName,
								null,
								null,
								page,
								resultLimit,
								false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
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
				requireContext(), "org_members", orgName, null, null, 1, resultLimit, true);
	}
}
