package org.mian.gitnex.fragments;

import android.content.Context;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.databinding.FragmentMilestonesBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.MilestonesViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class MilestonesFragment extends Fragment {

    private FragmentMilestonesBinding viewBinding;
    private Menu menu;
    private List<Milestone> dataList;
    private MilestonesAdapter adapter;
    private RepositoryContext repository;
    private String milestoneId;
    private int page = 1;
	public String state = "open";

    public static MilestonesFragment newInstance(RepositoryContext repository) {
    	MilestonesFragment fragment = new MilestonesFragment();
    	fragment.setArguments(repository.getBundle());
    	return fragment;
    }

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		repository = RepositoryContext.fromBundle(requireArguments());
		super.onCreate(savedInstanceState);
	}

	@Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewBinding = FragmentMilestonesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);
		Context ctx = getContext();

		milestoneId = requireActivity().getIntent().getStringExtra("milestoneId");
        requireActivity().getIntent().removeExtra("milestoneId");

        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dataList = new ArrayList<>();

	    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
	    viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);
        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

	        page = 1;
            dataList.clear();
            viewBinding.pullToRefresh.setRefreshing(false);
	        fetchDataAsync(repository.getOwner(), repository.getName(), state);
        }, 50));

        ((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerMilestone(milestoneState -> {

	        state = milestoneState;
	        if(milestoneState.equals("open")) {
		        menu.getItem(1).setIcon(R.drawable.ic_filter);
	        }
	        else {
		        menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
	        }

	        page = 1;
            dataList.clear();
            viewBinding.progressBar.setVisibility(View.VISIBLE);
            viewBinding.noDataMilestone.setVisibility(View.GONE);

	        fetchDataAsync(repository.getOwner(), repository.getName(), milestoneState);
        });

		fetchDataAsync(repository.getOwner(), repository.getName(), state);
        return viewBinding.getRoot();
    }

	private void fetchDataAsync(String repoOwner, String repoName, String state) {

		MilestonesViewModel milestonesViewModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

		milestonesViewModel.getMilestonesList(repoOwner, repoName, state, getContext()).observe(getViewLifecycleOwner(), milestonesListMain -> {

			adapter = new MilestonesAdapter(getContext(), milestonesListMain, repository);
			adapter.setLoadMoreListener(new MilestonesAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					MilestonesViewModel.loadMoreMilestones(repoOwner, repoName, page, state, getContext(), adapter);
					viewBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					viewBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				viewBinding.recyclerView.setAdapter(adapter);
				viewBinding.noDataMilestone.setVisibility(View.GONE);
				dataList.addAll(milestonesListMain);
				if(milestoneId != null) {
					viewBinding.recyclerView.scrollToPosition(getMilestoneIndex(Integer.parseInt(milestoneId), milestonesListMain));
				}
			}
			else {
				adapter.notifyDataChanged();
				viewBinding.recyclerView.setAdapter(adapter);
				viewBinding.noDataMilestone.setVisibility(View.VISIBLE);
			}

			viewBinding.progressBar.setVisibility(View.GONE);
		});
	}

	private static int getMilestoneIndex(int milestoneId, List<Milestone> milestones) {
		for (Milestone milestone : milestones) {
			if(milestone.getId() == milestoneId) {
				return milestones.indexOf(milestone);
			}
		}
		return -1;
	}

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        this.menu = menu;
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.filter_menu_milestone, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                filter(newText);
                return false;
            }
        });
    }

    private void filter(String text) {

        List<Milestone> arr = new ArrayList<>();

        for(Milestone d : dataList) {
	        if(d == null || d.getTitle() == null || d.getDescription() == null) {
		        continue;
	        }
            if(d.getTitle().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
                arr.add(d);
            }
        }

        adapter.updateList(arr);
    }
}
