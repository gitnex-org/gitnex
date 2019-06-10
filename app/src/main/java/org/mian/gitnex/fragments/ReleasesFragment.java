package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.Releases;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.ReleasesViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class ReleasesFragment extends Fragment {

    private ProgressBar mProgressBar;
    private ReleasesAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataReleases;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public ReleasesFragment() {
    }

    public static ReleasesFragment newInstance(String param1, String param2) {
        ReleasesFragment fragment = new ReleasesFragment();
        Bundle args = new Bundle();
        args.putString(repoOwnerF, param1);
        args.putString(repoNameF, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoName = getArguments().getString(repoNameF);
            repoOwner = getArguments().getString(repoOwnerF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_releases, container, false);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");
        noDataReleases = v.findViewById(R.id.noDataReleases);

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
                        ReleasesViewModel.loadReleasesList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName);
                    }
                }, 50);
            }
        });

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName);

        return v;

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

    private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, String repo) {

        ReleasesViewModel releasesModel = ViewModelProviders.of(this).get(ReleasesViewModel.class);

        releasesModel.getReleasesList(instanceUrl, instanceToken, owner, repo).observe(this, new Observer<List<Releases>>() {
            @Override
            public void onChanged(@Nullable List<Releases> releasesListMain) {
                adapter = new ReleasesAdapter(getContext(), releasesListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noDataReleases.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noDataReleases.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

}
