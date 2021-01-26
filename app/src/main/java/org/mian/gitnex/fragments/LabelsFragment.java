package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * Author M M Arif
 */

public class LabelsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private LabelsAdapter adapter;
    private TextView noData;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";
    private final String type = "repo";

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public LabelsFragment() {
    }

    public static LabelsFragment newInstance(String param1, String param2) {

        LabelsFragment fragment = new LabelsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

	    FragmentLabelsBinding fragmentLabelsBinding = FragmentLabelsBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        final SwipeRefreshLayout swipeRefresh = fragmentLabelsBinding.pullToRefresh;
        noData = fragmentLabelsBinding.noData;

        mRecyclerView = fragmentLabelsBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = fragmentLabelsBinding.progressBar;

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            LabelsViewModel.loadLabelsList(Authorization.get(getContext()), repoOwner, repoName, getContext());
        }, 200));

        fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName);

        return fragmentLabelsBinding.getRoot();
    }

    @Override
    public void onResume() {

        super.onResume();
        final TinyDB tinyDb = TinyDB.getInstance(getContext());

        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];

        if(tinyDb.getBoolean("labelsRefresh")) {

            LabelsViewModel.loadLabelsList(Authorization.get(getContext()), repoOwner, repoName, getContext());
            tinyDb.putBoolean("labelsRefresh", false);
        }
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

    private void fetchDataAsync(String instanceToken, String owner, String repo) {

        LabelsViewModel labelsModel = new ViewModelProvider(this).get(LabelsViewModel.class);

        labelsModel.getLabelsList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), labelsListMain -> {

            adapter = new LabelsAdapter(getContext(), labelsListMain, type, owner);

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

}
