package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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
import org.gitnex.tea4j.models.Releases;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.adapters.TagsAdapter;
import org.mian.gitnex.databinding.FragmentReleasesBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.ReleasesViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class ReleasesFragment extends Fragment {

    private ProgressBar mProgressBar;
    private ReleasesAdapter adapter;
    private TagsAdapter tagsAdapter;
    private RecyclerView mRecyclerView;
    private TextView noDataReleases;

    private RepositoryContext repository;
    private String releaseTag;
    private int page = 1;
    private int pageReleases = 1;

    public ReleasesFragment() {
    }

    public static ReleasesFragment newInstance(RepositoryContext repository) {
        ReleasesFragment fragment = new ReleasesFragment();
        fragment.setArguments(repository.getBundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = RepositoryContext.fromBundle(requireArguments());
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
	        if(repository.isReleasesViewTypeIsTag()) {
		        ReleasesViewModel.loadTagsList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), getContext());
	        } else {
		        ReleasesViewModel.loadReleasesList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), getContext());
	        }
	        mProgressBar.setVisibility(View.VISIBLE);

        }, 50));

        fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName());

        setHasOptionsMenu(true);
	    ((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerReleases(type -> {
			if(type != null) repository.setReleasesViewTypeIsTag(type.equals("tags"));
			page = 1;
			pageReleases = 1;
		    if(repository.isReleasesViewTypeIsTag()) {
			    ReleasesViewModel.loadTagsList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), getContext());
		    } else {
			    ReleasesViewModel.loadReleasesList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName(), getContext());
		    }
		    mProgressBar.setVisibility(View.VISIBLE);
	    });

        return fragmentReleasesBinding.getRoot();

    }

    private void fetchDataAsync(String instanceToken, String owner, String repo) {

        ReleasesViewModel releasesModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

        releasesModel.getReleasesList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), releasesListMain -> {
	        if(!repository.isReleasesViewTypeIsTag()) {
		        adapter = new ReleasesAdapter(getContext(), releasesListMain);
		        adapter.setLoadMoreListener(new ReleasesAdapter.OnLoadMoreListener() {

			        @Override
			        public void onLoadMore() {
				        pageReleases += 1;
				        ReleasesViewModel.loadMoreReleases(instanceToken, owner, repo, pageReleases, getContext(), adapter);
				        mProgressBar.setVisibility(View.VISIBLE);
			        }

			        @Override
			        public void onLoadFinished() {
				        mProgressBar.setVisibility(View.GONE);
			        }
		        });
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

	    releasesModel.getTagsList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), tagList -> {
		    if(repository.isReleasesViewTypeIsTag()) {
			    tagsAdapter = new TagsAdapter(getContext(), tagList, owner, repo);
			    tagsAdapter.setLoadMoreListener(new TagsAdapter.OnLoadMoreListener() {

				    @Override
				    public void onLoadMore() {
					    page += 1;
					    ReleasesViewModel.loadMoreTags(instanceToken, owner, repo , page, getContext(), tagsAdapter);
					    mProgressBar.setVisibility(View.VISIBLE);
				    }

				    @Override
				    public void onLoadFinished() {
					    mProgressBar.setVisibility(View.GONE);
				    }
			    });
			    if(tagsAdapter.getItemCount() > 0) {
				    mRecyclerView.setAdapter(tagsAdapter);
				    noDataReleases.setVisibility(View.GONE);
			    }
			    else {
				    tagsAdapter.notifyDataSetChanged();
				    mRecyclerView.setAdapter(tagsAdapter);
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

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    	if(!((BaseActivity) requireActivity()).getAccount().requiresVersion("1.15.0"))
    		return;
		inflater.inflate(R.menu.filter_menu_releases, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
}
