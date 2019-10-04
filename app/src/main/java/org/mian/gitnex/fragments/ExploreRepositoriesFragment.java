package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.MyReposListAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.ExploreRepoListViewModel;
import java.util.List;
import java.util.Objects;

/**
+ * Template Author M M Arif
+ * Author 6543
+ */

public class ExploreRepositoriesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MyReposListAdapter adapter;
    private TextView noData;
    private String searchKeyword = "test";  //test value

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ExploreRepositoriesFragment() {
    }

    public static ExploreRepositoriesFragment newInstance(String param1, String param2) {
        ExploreRepositoriesFragment fragment = new ExploreRepositoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));

        final View v = inflater.inflate(R.layout.fragment_explore_repo, container, false);
        setHasOptionsMenu(true);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleExplore));

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        noData = v.findViewById(R.id.noData);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        if(connToInternet) {

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeRefresh.setRefreshing(false);
                            ExploreRepoListViewModel.loadReposList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword);
                        }
                    }, 50);
                }
            });

            fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword);

        }
        else {
            mProgressBar.setVisibility(View.GONE);
        }

        return v;

    }

    @Override
    public void onResume() {
        super.onResume();
        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ExploreRepoListViewModel.loadReposList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword);

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String searchKeyword) {

        searchKeyword = this.searchKeyword; //test

        ExploreRepoListViewModel RepoModel = new ViewModelProvider(this).get(ExploreRepoListViewModel.class);

        RepoModel.getUserRepositories(instanceUrl, instanceToken, searchKeyword).observe(this, new Observer<List<UserRepositories>>() {
            @Override
            public void onChanged(@Nullable List<UserRepositories> myReposListMain) {
                adapter = new MyReposListAdapter(getContext(), myReposListMain);
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
        searchView.setQueryHint(getContext().getString(R.string.strFilter));

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
