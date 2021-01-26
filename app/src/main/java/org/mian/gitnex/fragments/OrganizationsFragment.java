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
import org.mian.gitnex.activities.CreateOrganizationActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.OrganizationListViewModel;

/**
 * Author M M Arif
 */

public class OrganizationsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private OrganizationsListAdapter adapter;
    private RecyclerView mRecyclerView;
    private ExtendedFloatingActionButton createNewOrganization;
    private TextView noDataOrg;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    FragmentOrganizationsBinding fragmentOrganizationsBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        final SwipeRefreshLayout swipeRefresh = fragmentOrganizationsBinding.pullToRefresh;

	    ((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navOrgs));

        mProgressBar = fragmentOrganizationsBinding.progressBar;
        noDataOrg = fragmentOrganizationsBinding.noDataOrg;
        mRecyclerView = fragmentOrganizationsBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewOrganization = fragmentOrganizationsBinding.addNewOrganization;

        createNewOrganization.setOnClickListener(view -> {

            Intent intent = new Intent(view.getContext(), CreateOrganizationActivity.class);
            startActivity(intent);
        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && createNewOrganization.isShown()) {
                    createNewOrganization.setVisibility(View.GONE);
                } else if (dy < 0 ) {
                    createNewOrganization.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            OrganizationListViewModel.loadOrgsList(Authorization.get(getContext()), getContext());

        }, 50));

        fetchDataAsync(Authorization.get(getContext()));

        return fragmentOrganizationsBinding.getRoot();

    }

    @Override
    public void onResume(){
        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("orgCreated")) {
            OrganizationListViewModel.loadOrgsList(Authorization.get(getContext()), getContext());
            tinyDb.putBoolean("orgCreated", false);
        }
    }

    private void fetchDataAsync(String instanceToken) {

        OrganizationListViewModel orgModel = new ViewModelProvider(this).get(OrganizationListViewModel.class);

        orgModel.getUserOrgs(instanceToken, getContext()).observe(getViewLifecycleOwner(), orgsListMain -> {
            adapter = new OrganizationsListAdapter(getContext(), orgsListMain);

            if(adapter.getItemCount() > 0) {
                mRecyclerView.setAdapter(adapter);
                noDataOrg.setVisibility(View.GONE);
            }
            else {
                adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);
                noDataOrg.setVisibility(View.VISIBLE);
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
