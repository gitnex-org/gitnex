package org.mian.gitnex.fragments;

import android.content.Intent;
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
import org.mian.gitnex.databinding.FragmentStarredRepositoriesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.StarredRepositoriesViewModel;

/**
 * Author M M Arif
 */

public class StarredRepositoriesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private ReposListAdapter adapter;
    private ExtendedFloatingActionButton createNewRepo;
    private TextView noData;
    private int pageSize = 1;
    private int resultLimit = 50;

    private OnFragmentInteractionListener mListener;

    public StarredRepositoriesFragment() {
    }

    public static StarredRepositoriesFragment newInstance(String param1, String param2) {
        StarredRepositoriesFragment fragment = new StarredRepositoriesFragment();
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
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

	    FragmentStarredRepositoriesBinding fragmentStarredRepositoriesBinding = FragmentStarredRepositoriesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        final SwipeRefreshLayout swipeRefresh = fragmentStarredRepositoriesBinding.pullToRefresh;

	    ((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navStarredRepos));

        noData = fragmentStarredRepositoriesBinding.noData;
        mProgressBar = fragmentStarredRepositoriesBinding.progressBar;
        mRecyclerView = fragmentStarredRepositoriesBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewRepo = fragmentStarredRepositoriesBinding.addNewRepo;

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
            StarredRepositoriesViewModel.loadStarredReposList(Authorization.get(getContext()), getContext(), pageSize, resultLimit);

        }, 50));

        fetchDataAsync(Authorization.get(getContext()), pageSize, resultLimit);

        return fragmentStarredRepositoriesBinding.getRoot();
    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("repoCreated")) {
            StarredRepositoriesViewModel.loadStarredReposList(Authorization.get(getContext()), getContext(), pageSize, resultLimit);
            tinyDb.putBoolean("repoCreated", false);
        }
    }

    private void fetchDataAsync(String instanceToken, int pageSize, int resultLimit) {

        StarredRepositoriesViewModel starredRepoModel = new ViewModelProvider(this).get(StarredRepositoriesViewModel.class);

        starredRepoModel.getUserStarredRepositories(instanceToken, getContext(), pageSize, resultLimit).observe(getViewLifecycleOwner(),
	        starredReposListMain -> {

	            adapter = new ReposListAdapter(getContext(), starredReposListMain);
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
