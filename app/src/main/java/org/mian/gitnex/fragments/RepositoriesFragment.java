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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * Author M M Arif
 */

public class RepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private int resultLimit = Constants.resultLimitOldGiteaInstances;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding = FragmentRepositoriesBinding.inflate(inflater, container, false);

		setHasOptionsMenu(true);
		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navRepos));

		// if gitea is 1.12 or higher use the new limit
		if(((BaseActivity) requireActivity()).getAccount().requiresVersion("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		fragmentRepositoriesBinding.addNewRepo.setOnClickListener(view -> {
			Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
			startActivity(intent);
		});

		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentRepositoriesBinding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		fragmentRepositoriesBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			fragmentRepositoriesBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());
			fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());

		return fragmentRepositoriesBinding.getRoot();
	};

	private void fetchDataAsync(String instanceToken) {

		RepositoriesViewModel reposModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		reposModel.getRepositories(instanceToken, page, resultLimit, getContext()).observe(getViewLifecycleOwner(), reposListMain -> {

			adapter = new ReposListAdapter(reposListMain, getContext());
			adapter.setLoadMoreListener(new ReposListAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					RepositoriesViewModel.loadMoreRepos(instanceToken, page, resultLimit, getContext(), adapter);
					fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
			}

			fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
		});
	}

    /*private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ReposListAdapter adapter;
    private ExtendedFloatingActionButton createNewRepo;
    private TextView noDataRepo;
    private final int pageSize = 1;
    private final int resultLimit = 50;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    FragmentRepositoriesBinding fragmentRepositoriesBinding = FragmentRepositoriesBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        final SwipeRefreshLayout swipeRefresh = fragmentRepositoriesBinding.pullToRefresh;

	    ((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navRepos));

        noDataRepo = fragmentRepositoriesBinding.noData;
        mProgressBar = fragmentRepositoriesBinding.progressBar;
        mRecyclerView = fragmentRepositoriesBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewRepo = fragmentRepositoriesBinding.addNewRepo;

        createNewRepo.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
            startActivity(intent);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && createNewRepo.isShown()) {
                    createNewRepo.setVisibility(View.GONE);
                }
                else if (dy < 0 ) {
                    createNewRepo.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            RepositoriesListViewModel.loadReposList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), getContext(), pageSize, resultLimit);

        }, 50));

        fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), pageSize, resultLimit);
        return fragmentRepositoriesBinding.getRoot();

    }*/

    @Override
    public void onResume() {
        super.onResume();

        if(MainActivity.repoCreated) {
            RepositoriesViewModel.loadReposList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), page, resultLimit, getContext());
	        MainActivity.repoCreated = false;
        }
    }

    /*private void fetchDataAsync(String instanceToken, int pageSize, int resultLimit) {

        RepositoriesListViewModel repoModel = new ViewModelProvider(this).get(RepositoriesListViewModel.class);

        repoModel.getUserRepositories(instanceToken, getContext(), pageSize, resultLimit).observe(getViewLifecycleOwner(),
	        reposListMain -> {

	            adapter = new ReposListAdapter(getContext(), reposListMain);
	            if(adapter.getItemCount() > 0) {
	                mRecyclerView.setAdapter(adapter);
	                noDataRepo.setVisibility(View.GONE);
	            }
	            else {
	                adapter.notifyDataSetChanged();
	                mRecyclerView.setAdapter(adapter);
	                noDataRepo.setVisibility(View.VISIBLE);
	            }
	            mProgressBar.setVisibility(View.GONE);
	        });
    }*/

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
                if(fragmentRepositoriesBinding.recyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

}
