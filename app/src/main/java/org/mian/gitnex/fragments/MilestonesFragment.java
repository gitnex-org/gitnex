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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Milestone;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateMilestoneActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.databinding.FragmentMilestonesBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.MilestonesViewModel;

/**
 * @author M M Arif
 */
public class MilestonesFragment extends Fragment {

	private MilestonesViewModel milestonesViewModel;
	private FragmentMilestonesBinding viewBinding;
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
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentMilestonesBinding.inflate(inflater, container, false);
		Context ctx = getContext();
		milestonesViewModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

		boolean canPush = repository.getPermissions().isPush();
		boolean archived = repository.getRepository().isArchived();

		milestoneId = requireActivity().getIntent().getStringExtra("milestoneId");
		requireActivity().getIntent().removeExtra("milestoneId");

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		dataList = new ArrayList<>();

		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											dataList.clear();
											viewBinding.pullToRefresh.setRefreshing(false);
											fetchDataAsync(
													repository.getOwner(),
													repository.getName(),
													state);
										},
										50));

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerMilestone(
						milestoneState -> {
							state = milestoneState;

							page = 1;
							dataList.clear();
							viewBinding.progressBar.setVisibility(View.VISIBLE);
							viewBinding.noDataMilestone.setVisibility(View.GONE);

							fetchDataAsync(
									repository.getOwner(), repository.getName(), milestoneState);
						});

		if (!canPush || archived) {
			viewBinding.createNewMilestone.setVisibility(View.GONE);
		}

		viewBinding.createNewMilestone.setOnClickListener(
				v13 -> startActivity(repository.getIntent(ctx, CreateMilestoneActivity.class)));

		fetchDataAsync(repository.getOwner(), repository.getName(), state);

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {

							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.filter_menu_milestone, menu);

								if (repository.getMilestoneState().toString().equals("open")) {
									menu.getItem(1).setIcon(R.drawable.ic_filter);
								} else {
									menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
								}

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {

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

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		return viewBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (RepoDetailActivity.updateFABActions) {
			page = 1;
			MilestonesViewModel.loadMilestonesList(
					repository.getOwner(), repository.getName(), state, requireContext());
			RepoDetailActivity.updateFABActions = false;
		}
	}

	private void fetchDataAsync(String repoOwner, String repoName, String state) {

		milestonesViewModel
				.getMilestonesList(repoOwner, repoName, state, getContext())
				.observe(
						getViewLifecycleOwner(),
						milestonesListMain -> {
							adapter =
									new MilestonesAdapter(
											getContext(), milestonesListMain, repository);
							adapter.setLoadMoreListener(
									new MilestonesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											milestonesViewModel.loadMoreMilestones(
													repoOwner,
													repoName,
													page,
													state,
													getContext(),
													adapter);
											viewBinding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											viewBinding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								viewBinding.recyclerView.setAdapter(adapter);
								viewBinding.noDataMilestone.setVisibility(View.GONE);
								dataList.addAll(milestonesListMain);
								if (milestoneId != null) {
									viewBinding.recyclerView.scrollToPosition(
											getMilestoneIndex(
													Integer.parseInt(milestoneId),
													milestonesListMain));
								}
							} else {
								adapter.notifyDataChanged();
								viewBinding.recyclerView.setAdapter(adapter);
								viewBinding.noDataMilestone.setVisibility(View.VISIBLE);
							}

							viewBinding.progressBar.setVisibility(View.GONE);
						});
	}

	private static int getMilestoneIndex(int milestoneId, List<Milestone> milestones) {
		for (Milestone milestone : milestones) {
			if (milestone.getId() == milestoneId) {
				return milestones.indexOf(milestone);
			}
		}
		return -1;
	}

	private void filter(String text) {

		List<Milestone> arr = new ArrayList<>();

		for (Milestone d : dataList) {
			if (d == null || d.getTitle() == null || d.getDescription() == null) {
				continue;
			}
			if (d.getTitle().toLowerCase().contains(text)
					|| d.getDescription().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}
}
