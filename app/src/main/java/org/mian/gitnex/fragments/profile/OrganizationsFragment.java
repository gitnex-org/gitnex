package org.mian.gitnex.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationsFragment extends Fragment {

	private static final String BUNDLE_USERNAME = "username";
	private FragmentOrganizationsBinding binding;
	private OrganizationsViewModel viewModel;
	private OrganizationsListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private String username;
	private int resultLimit;

	public static OrganizationsFragment newInstance(String username) {
		OrganizationsFragment fragment = new OrganizationsFragment();
		Bundle args = new Bundle();
		args.putString(BUNDLE_USERNAME, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) username = getArguments().getString(BUNDLE_USERNAME);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		setHasOptionsMenu(true);

		setupUI();
		observeViewModel();
		refreshData();

		return binding.getRoot();
	}

	private void setupUI() {
		// binding.addNewOrganization.setVisibility(View.GONE);
		adapter = new OrganizationsListAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUserOrgs(
								requireContext(), username, page + 1, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getOrgs()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							binding.layoutEmpty
									.getRoot()
									.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
							binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading ->
								binding.expressiveLoader.setVisibility(
										loading && adapter.getItemCount() == 0
												? View.VISIBLE
												: View.GONE));

		viewModel
				.getError()
				.observe(getViewLifecycleOwner(), msg -> Toasty.show(requireContext(), msg));
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.fetchUserOrgs(requireContext(), username, 1, resultLimit, true);
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
		inflater.inflate(R.menu.search_menu, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView =
				(androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(
				new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
					@Override
					public boolean onQueryTextSubmit(String query) {
						return false;
					}

					@Override
					public boolean onQueryTextChange(String newText) {
						adapter.getFilter().filter(newText);
						return false;
					}
				});
	}
}
