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
import org.mian.gitnex.adapters.MyReposListAdapter;
import org.mian.gitnex.databinding.FragmentMyRepositoriesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.MyRepositoriesViewModel;

/**
 * Author M M Arif
 */

public class MyRepositoriesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private MyReposListAdapter adapter;
    private ExtendedFloatingActionButton createNewRepo;
    private TextView noDataMyRepo;

    private int pageSize = 1;
    private int resultLimit = 50;

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public MyRepositoriesFragment() {
    }

    public static MyRepositoriesFragment newInstance(String param1, String param2) {
        MyRepositoriesFragment fragment = new MyRepositoriesFragment();
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

	    FragmentMyRepositoriesBinding fragmentMyRepositoriesBinding = FragmentMyRepositoriesBinding.inflate(inflater, container, false);

        setHasOptionsMenu(true);

        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String userLogin =  tinyDb.getString("userLogin");
        tinyDb.putBoolean("isRepoAdmin", true);

        final SwipeRefreshLayout swipeRefresh = fragmentMyRepositoriesBinding.pullToRefresh;

	    ((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navMyRepos));

        noDataMyRepo = fragmentMyRepositoriesBinding.noDataMyRepo;
        mProgressBar = fragmentMyRepositoriesBinding.progressBar;
        mRecyclerView = fragmentMyRepositoriesBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewRepo = fragmentMyRepositoriesBinding.addNewRepo;
        createNewRepo.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
            startActivity(intent);

        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

                if (dy > 0 && createNewRepo.isShown()) {
                    createNewRepo.setVisibility(View.GONE);
                } else if (dy < 0) {
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
            MyRepositoriesViewModel.loadMyReposList(Authorization.get(getContext()), userLogin, getContext(),  pageSize, resultLimit);

        }, 50));

        fetchDataAsync(Authorization.get(getContext()), userLogin, pageSize, resultLimit);

        return fragmentMyRepositoriesBinding.getRoot();

    }

    @Override
    public void onResume() {
        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String userLogin =  tinyDb.getString("userLogin");

        if(tinyDb.getBoolean("repoCreated")) {
            MyRepositoriesViewModel.loadMyReposList(Authorization.get(getContext()), userLogin, getContext(),  pageSize, resultLimit);
            tinyDb.putBoolean("repoCreated", false);
        }

    }

    private void fetchDataAsync(String instanceToken, String userLogin, int  pageSize, int resultLimit) {

        MyRepositoriesViewModel myRepoModel = new ViewModelProvider(this).get(MyRepositoriesViewModel.class);

        myRepoModel.getCurrentUserRepositories(instanceToken, userLogin, getContext(), pageSize, resultLimit).observe(getViewLifecycleOwner(),
	        myReposListMain -> {

	            adapter = new MyReposListAdapter(getContext(), myReposListMain);
	            if(adapter.getItemCount() > 0) {
	                mRecyclerView.setAdapter(adapter);
	                noDataMyRepo.setVisibility(View.GONE);
	            }
	            else {
	                adapter.notifyDataSetChanged();
	                mRecyclerView.setAdapter(adapter);
	                noDataMyRepo.setVisibility(View.VISIBLE);
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
