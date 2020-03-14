package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepositoriesByOrgAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.RepositoriesByOrgViewModel;
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class RepositoriesByOrgFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

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

        View v = inflater.inflate(R.layout.fragment_repositories_by_org, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        noData = v.findViewById(R.id.noData);

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = v.findViewById(R.id.progress_bar);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        RepositoriesByOrgViewModel.loadOrgRepos(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), orgName, getContext(), pageSize, resultLimit);
                    }
                }, 200);
            }
        });

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), orgName, pageSize, resultLimit);

        return v;
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("repoCreated")) {
            RepositoriesByOrgViewModel.loadOrgRepos(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), orgName, getContext(), pageSize, resultLimit);
            tinyDb.putBoolean("repoCreated", false);
        }

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, int pageSize, int resultLimit) {

        RepositoriesByOrgViewModel orgRepoModel = new ViewModelProvider(this).get(RepositoriesByOrgViewModel.class);

        orgRepoModel.getRepositoriesByOrg(instanceUrl, instanceToken, owner, getContext(), pageSize, resultLimit).observe(getViewLifecycleOwner(), new Observer<List<UserRepositories>>() {
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

        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));

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

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
