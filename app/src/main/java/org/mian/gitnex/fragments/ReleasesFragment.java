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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.models.IssueComments;
import org.gitnex.tea4j.models.Releases;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.databinding.FragmentReleasesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
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
    private String releaseTag;

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
        releaseTag = requireActivity().getIntent().getStringExtra("releaseTagName");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentReleasesBinding fragmentReleasesBinding = FragmentReleasesBinding.inflate(inflater, container, false);

        noDataReleases = fragmentReleasesBinding.noDataReleases;

        final SwipeRefreshLayout swipeRefresh = fragmentReleasesBinding.pullToRefresh;

        mRecyclerView = fragmentReleasesBinding.recyclerView;
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = fragmentReleasesBinding.progressBar;

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            ReleasesViewModel.loadReleasesList(Authorization.get(getContext()), repoOwner, repoName, getContext());

        }, 50));

        fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName);

        return fragmentReleasesBinding.getRoot();

    }

    @Override
    public void onResume() {

        super.onResume();
        TinyDB tinyDb = TinyDB.getInstance(getContext());

        if(tinyDb.getBoolean("updateReleases")) {
            ReleasesViewModel.loadReleasesList(Authorization.get(getContext()), repoOwner, repoName, getContext());
            tinyDb.putBoolean("updateReleases", false);
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

        ReleasesViewModel releasesModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

        releasesModel.getReleasesList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), new Observer<List<Releases>>() {
            @Override
            public void onChanged(@Nullable List<Releases> releasesListMain) {
                adapter = new ReleasesAdapter(getContext(), releasesListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
	                if(releasesListMain != null && releaseTag != null) {
		                int index = getReleaseIndex(releaseTag, releasesListMain);
		                releaseTag = null;
		                if(index != -1) {
			                mRecyclerView.scrollToPosition(index);
		                }
	                }
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

	private static int getReleaseIndex(String tag, List<Releases> releases) {
		for (Releases release : releases) {
			if(release.getTag_name().equals(tag)) {
				return releases.indexOf(release);
			}
		}
		return -1;
	}

}
