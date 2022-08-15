package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateOrganizationActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author M M Arif
 */

public class OrganizationsFragment extends Fragment {

	public static boolean orgCreated = false;
	private OrganizationsViewModel organizationsViewModel;
	private FragmentOrganizationsBinding fragmentOrganizationsBinding;
	private OrganizationsListAdapter adapter;
	private int page = 1;
	private int resultLimit;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentOrganizationsBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);

		setHasOptionsMenu(true);
		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navOrg));
		organizationsViewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(getContext());

		fragmentOrganizationsBinding.addNewOrganization.setOnClickListener(view -> {
			Intent intent = new Intent(view.getContext(), CreateOrganizationActivity.class);
			startActivity(intent);
		});

		fragmentOrganizationsBinding.recyclerView.setHasFixedSize(true);
		fragmentOrganizationsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		fragmentOrganizationsBinding.recyclerView.setPadding(0, 0, 0, 220);
		fragmentOrganizationsBinding.recyclerView.setClipToPadding(false);

		fragmentOrganizationsBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			fragmentOrganizationsBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync();
			fragmentOrganizationsBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync();

		return fragmentOrganizationsBinding.getRoot();
	}

	private void fetchDataAsync() {

		organizationsViewModel.getUserOrg(page, resultLimit, getContext()).observe(getViewLifecycleOwner(), orgListMain -> {

			adapter = new OrganizationsListAdapter(requireContext(), orgListMain);
			adapter.setLoadMoreListener(new OrganizationsListAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					organizationsViewModel.loadMoreOrgList(page, resultLimit, getContext(), adapter);
					fragmentOrganizationsBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentOrganizationsBinding.recyclerView.setAdapter(adapter);
				fragmentOrganizationsBinding.noDataOrg.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentOrganizationsBinding.recyclerView.setAdapter(adapter);
				fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
			}

			fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		if(orgCreated) {
			organizationsViewModel.loadOrgList(page, resultLimit, getContext());
			orgCreated = false;
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				if(fragmentOrganizationsBinding.recyclerView.getAdapter() != null) {
					adapter.getFilter().filter(newText);
				}
				return false;
			}
		});
	}

}
