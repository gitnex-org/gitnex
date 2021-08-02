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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.models.Emails;
import org.mian.gitnex.adapters.MyProfileEmailsAdapter;
import org.mian.gitnex.databinding.FragmentProfileEmailsBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.viewmodels.ProfileEmailsViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class MyProfileEmailsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private MyProfileEmailsAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataEmails;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public MyProfileEmailsFragment() {
    }

    public static MyProfileEmailsFragment newInstance(String param1, String param2) {
        MyProfileEmailsFragment fragment = new MyProfileEmailsFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

	    FragmentProfileEmailsBinding fragmentProfileEmailsBinding = FragmentProfileEmailsBinding.inflate(inflater, container, false);

        final SwipeRefreshLayout swipeRefresh = fragmentProfileEmailsBinding.pullToRefresh;

        noDataEmails = fragmentProfileEmailsBinding.noDataEmails;
        mRecyclerView = fragmentProfileEmailsBinding.recyclerView;

	    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = fragmentProfileEmailsBinding.progressBar;

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            ProfileEmailsViewModel.loadEmailsList(Authorization.get(getContext()), getContext());

        }, 200));

        fetchDataAsync(Authorization.get(getContext()));

        return fragmentProfileEmailsBinding.getRoot();

    }

    private void fetchDataAsync(String instanceToken) {

        ProfileEmailsViewModel profileEmailModel = new ViewModelProvider(this).get(ProfileEmailsViewModel.class);

        profileEmailModel.getEmailsList(instanceToken, getContext()).observe(getViewLifecycleOwner(), new Observer<List<Emails>>() {
            @Override
            public void onChanged(@Nullable List<Emails> emailsListMain) {
                adapter = new MyProfileEmailsAdapter(getContext(), emailsListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noDataEmails.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noDataEmails.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
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
