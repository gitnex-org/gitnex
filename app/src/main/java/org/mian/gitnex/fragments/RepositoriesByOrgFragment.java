package org.mian.gitnex.fragments;

import android.net.Uri;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.RepositoriesByOrgAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesByOrgBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.viewmodels.RepositoriesByOrgViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class RepositoriesByOrgFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RepositoriesByOrgAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noData;
    private static String orgNameF = "param2";
    private String orgName;
    private int pageSize = 1;
    private int resultLimit = 50;

    public RepositoriesByOrgFragment() {
    }

    public static RepositoriesByOrgFragment newInstance(String param1) {
        RepositoriesByOrgFragment fragment = new RepositoriesByOrgFragment();
        Bundle args = new Bundle();
        args.putString(orgNameF, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orgName = getArguments().getString(orgNameF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

	    FragmentRepositoriesByOrgBinding fragmentRepositoriesByOrgBinding = FragmentRepositoriesByOrgBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);
        noData = fragmentRepositoriesByOrgBinding.noData;

        final SwipeRefreshLayout swipeRefresh = fragmentRepositoriesByOrgBinding.pullToRefresh;

        mRecyclerView = fragmentRepositoriesByOrgBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = fragmentRepositoriesByOrgBinding.progressBar;

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            RepositoriesByOrgViewModel.loadOrgRepos(((BaseActivity) requireActivity()).getAccount().getAuthorization(), orgName, getContext(), pageSize, resultLimit);

        }, 200));

        fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), orgName, pageSize, resultLimit);

        return fragmentRepositoriesByOrgBinding.getRoot();
    }

    @Override
    public void onResume() {

        super.onResume();

        if(MainActivity.repoCreated) {
            RepositoriesByOrgViewModel.loadOrgRepos(((BaseActivity) requireActivity()).getAccount().getAuthorization(), orgName, getContext(), pageSize, resultLimit);
	        MainActivity.repoCreated = false;
        }

    }

    private void fetchDataAsync(String instanceToken, String owner, int pageSize, int resultLimit) {

        RepositoriesByOrgViewModel orgRepoModel = new ViewModelProvider(this).get(RepositoriesByOrgViewModel.class);

        orgRepoModel.getRepositoriesByOrg(instanceToken, owner, getContext(), pageSize, resultLimit).observe(getViewLifecycleOwner(), new Observer<List<UserRepositories>>() {
            @Override
            public void onChanged(@Nullable List<UserRepositories> orgReposListMain) {
                adapter = new RepositoriesByOrgAdapter(getContext(), orgReposListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noData.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noData.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        boolean connToInternet = AppUtil.hasNetworkConnection(requireContext());

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //searchView.setQueryHint(getContext().getString(R.string.strFilter));

        if(!connToInternet) {
            return;
        }

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
