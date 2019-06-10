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
import org.mian.gitnex.adapters.ProfileFollowersAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.ProfileFollowersViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileFollowersFragment extends Fragment {

    private ProgressBar mProgressBar;
    private ProfileFollowersAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataFollowers;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public ProfileFollowersFragment() {
    }

    public static ProfileFollowersFragment newInstance(String param1, String param2) {
        ProfileFollowersFragment fragment = new ProfileFollowersFragment();
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
        final View v = inflater.inflate(R.layout.fragment_profile_followers, container, false);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        noDataFollowers = v.findViewById(R.id.noDataFollowers);
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
                        ProfileFollowersViewModel.loadFollowersList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken));
                    }
                }, 200);
            }
        });

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken));

        return v;
    }

    private void fetchDataAsync(String instanceUrl, String instanceToken) {

        ProfileFollowersViewModel pfModel = ViewModelProviders.of(this).get(ProfileFollowersViewModel.class);

        pfModel.getFollowersList(instanceUrl, instanceToken).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> pfListMain) {
                adapter = new ProfileFollowersAdapter(getContext(), pfListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noDataFollowers.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noDataFollowers.setVisibility(View.VISIBLE);
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
