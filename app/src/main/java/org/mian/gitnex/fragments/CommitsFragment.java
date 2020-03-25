package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.models.Commits;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.CommitsViewModel;

/**
 * Author M M Arif
 */

public class CommitsFragment extends Fragment {

    private CommitsViewModel commitsViewModel;
    private CommitsAdapter commitsAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    private int listLimit = 50;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.fragment_commits, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        TextView noDataCommits = v.findViewById(R.id.noDataCommits);
        ProgressBar progressBar = v.findViewById(R.id.progress_bar);
        swipeRefreshLayout = v.findViewById(R.id.pullToRefresh);
        progressBar.setVisibility(View.GONE);

        RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        //CommitsViewModel commitsViewModel = new ViewModelProvider(this).get(CommitsViewModel.class);

        commitsAdapter = new CommitsAdapter(getContext());

        commitsViewModel = ViewModelProviders.of(this, new ViewModelProvider.Factory() {

            @NonNull
            @Override
            public <VM extends ViewModel> VM create(@NonNull Class<VM> modelClass) {
                //noinspection unchecked
                return (VM) new CommitsViewModel (getContext(), instanceUrl, instanceToken, repoOwner, repoName, listLimit);
            }

        }).get(CommitsViewModel.class);

        swipeRefreshLayout.setOnRefreshListener(() -> commitsViewModel.refresh());

        //noinspection unchecked
        commitsViewModel.itemPagedList.observe(getViewLifecycleOwner(), new Observer<PagedList<Commits>>() {

            @Override
            public void onChanged(@Nullable PagedList<Commits> items) {

                commitsAdapter.submitList(items);
                swipeRefreshLayout.setRefreshing(false);

            }

        });

        recyclerView.setAdapter(commitsAdapter);
        return v;

    }

}
