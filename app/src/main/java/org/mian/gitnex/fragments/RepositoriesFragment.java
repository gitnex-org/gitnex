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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.RepositoriesListViewModel;

/**
 * Author M M Arif
 */

public class RepositoriesFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ReposListAdapter adapter;
    private ExtendedFloatingActionButton createNewRepo;
    private TextView noDataRepo;
    private int pageSize = 1;
    private int resultLimit = 50;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_repositories, container, false);
        setHasOptionsMenu(true);

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

	    ((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navRepos));

        noDataRepo = v.findViewById(R.id.noData);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewRepo = v.findViewById(R.id.addNewRepo);

        createNewRepo.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
            startActivity(intent);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && createNewRepo.isShown()) {
                    createNewRepo.setVisibility(View.GONE);
                } else if (dy < 0 ) {
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
            RepositoriesListViewModel.loadReposList(Authorization.get(getContext()), getContext(), pageSize, resultLimit);

        }, 50));

        fetchDataAsync(Authorization.get(getContext()), pageSize, resultLimit);
        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("repoCreated")) {
            RepositoriesListViewModel.loadReposList(Authorization.get(getContext()), getContext(), pageSize, resultLimit);
            tinyDb.putBoolean("repoCreated", false);
        }
    }

    private void fetchDataAsync(String instanceToken, int pageSize, int resultLimit) {

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

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //searchView.setQueryHint(getContext().getString(R.string.strFilter));

        /*if(!connToInternet) {
            return;
        }*/

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mRecyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

    }

}
