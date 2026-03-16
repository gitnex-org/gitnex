package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CommitDetailActivity;
import org.mian.gitnex.activities.PullRequestDiffActivity;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.databinding.ActivityCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.viewmodels.PullRequestDiffViewModel;

/**
 * @author qwerty287
 * @author mmarif
 */
public class PullRequestCommitsFragment extends Fragment {

	private ActivityCommitsBinding binding;
	private PullRequestDiffViewModel viewModel;
	private CommitsAdapter adapter;
	private IssueContext issue;
	private int resultLimit;
	private Context ctx;

	public PullRequestCommitsFragment() {}

	public static PullRequestCommitsFragment newInstance() {
		return new PullRequestCommitsFragment();
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = ActivityCommitsBinding.inflate(inflater, container, false);
		ctx = requireContext();
		issue = IssueContext.fromIntent(requireActivity().getIntent());
		resultLimit = Constants.getCurrentResultLimit(ctx);

		viewModel = new ViewModelProvider(requireActivity()).get(PullRequestDiffViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		if (viewModel.getCommits().getValue() == null
				|| viewModel.getCommits().getValue().isEmpty()) {
			refreshData();
		}

		return binding.getRoot();
	}

	private void observeViewModel() {
		viewModel
				.getCommits()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							binding.pullToRefresh.setRefreshing(false);

							boolean isLoading =
									Boolean.TRUE.equals(viewModel.getIsCommitsLoading().getValue());
							binding.layoutEmpty
									.getRoot()
									.setVisibility(
											!isLoading && list.isEmpty()
													? View.VISIBLE
													: View.GONE);
						});

		viewModel
				.getIsCommitsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(ctx, msg);
								binding.pullToRefresh.setRefreshing(false);
							}
						});
	}

	private void setupRecyclerView() {
		adapter =
				new CommitsAdapter(
						ctx,
						new ArrayList<>(),
						commit -> {
							Intent intent =
									issue.getRepository()
											.getIntent(ctx, CommitDetailActivity.class);
							intent.putExtra("sha", commit.getSha());
							startActivity(intent);
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		binding.recyclerView.addOnScrollListener(
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchPRCommits(
								ctx,
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								issue.getIssueIndex(),
								resultLimit,
								false);
					}
				});
	}

	private void refreshData() {
		viewModel.fetchPRCommits(
				ctx,
				issue.getRepository().getOwner(),
				issue.getRepository().getName(),
				issue.getIssueIndex(),
				resultLimit,
				true);
	}

	private void setupSearch() {
		if (!(getActivity() instanceof PullRequestDiffActivity)) return;

		com.google.android.material.search.SearchView activitySearchView =
				getActivity().findViewById(R.id.search_view);
		RecyclerView activitySearchRecycler =
				getActivity().findViewById(R.id.search_results_recycler);

		activitySearchRecycler.setLayoutManager(new LinearLayoutManager(ctx));
		activitySearchRecycler.setAdapter(adapter);

		activitySearchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString());
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		activitySearchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						activitySearchView.setText("");
						filter("");
					}
				});
	}

	private void filter(String text) {
		if (binding == null) return;

		List<Commit> originalList = viewModel.getCommits().getValue();
		if (originalList == null) return;

		if (text.isEmpty()) {
			adapter.updateList(originalList);
			binding.layoutEmpty
					.getRoot()
					.setVisibility(originalList.isEmpty() ? View.VISIBLE : View.GONE);
			return;
		}

		List<Commit> filtered = new ArrayList<>();
		String query = text.toLowerCase().trim();
		for (Commit c : originalList) {
			String msg = (c.getCommit() != null) ? c.getCommit().getMessage().toLowerCase() : "";
			if (msg.contains(query) || c.getSha().toLowerCase().contains(query)) {
				filtered.add(c);
			}
		}

		adapter.updateList(filtered);

		binding.layoutEmpty.getRoot().setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
