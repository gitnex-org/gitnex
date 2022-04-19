package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssuesFragment extends Fragment {

	public static boolean resumeIssues = false;

	private FragmentIssuesBinding fragmentIssuesBinding;
	private Context context;

	private Menu menu;
	private List<Issue> issuesList;
	private IssuesAdapter adapter;

	private int pageSize = Constants.issuesPageInit;
	private final String TAG = Constants.tagIssuesList;
	private int resultLimit;
	private final String requestType = Constants.issuesRequestType;

	private RepositoryContext repository;

	public static IssuesFragment newInstance(RepositoryContext repository) {
		IssuesFragment f = new IssuesFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		repository = RepositoryContext.fromBundle(requireArguments());

		resultLimit = Constants.getCurrentResultLimit(context);

		issuesList = new ArrayList<>();

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentIssuesBinding.pullToRefresh.setRefreshing(false);
			loadInitial(repository.getOwner(), repository.getName(), resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());
			adapter.notifyDataChanged();
		}, 200));

		adapter = new IssuesAdapter(context, issuesList);
		adapter.setLoadMoreListener(() -> fragmentIssuesBinding.recyclerView.post(() -> {
			if(issuesList.size() == resultLimit || pageSize == resultLimit) {
				int page = (issuesList.size() + resultLimit) / resultLimit;
				loadMore(repository.getOwner(), repository.getName(), page, resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentIssuesBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		fragmentIssuesBinding.recyclerView.setHasFixedSize(true);
		fragmentIssuesBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentIssuesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentIssuesBinding.recyclerView.setAdapter(adapter);

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListener(issueState -> {

			if(issueState.equals("closed")) {
				menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
			}
			else {
				menu.getItem(1).setIcon(R.drawable.ic_filter);
			}

			issuesList.clear();

			adapter = new IssuesAdapter(context, issuesList);
			adapter.setLoadMoreListener(() -> fragmentIssuesBinding.recyclerView.post(() -> {

				if(issuesList.size() == resultLimit || pageSize == resultLimit) {
					int page = (issuesList.size() + resultLimit) / resultLimit;
					loadMore(repository.getOwner(), repository.getName(), page, resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());
				}
			}));

			fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
			fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

			loadInitial(repository.getOwner(), repository.getName(), resultLimit, requestType, issueState, repository.getIssueMilestoneFilterName());
			fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		});

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerFilterIssuesByMilestone(filterIssueByMilestone -> {

			issuesList.clear();

			adapter = new IssuesAdapter(context, issuesList);
			adapter.setLoadMoreListener(() -> fragmentIssuesBinding.recyclerView.post(() -> {

				if(issuesList.size() == resultLimit || pageSize == resultLimit) {
					int page = (issuesList.size() + resultLimit) / resultLimit;
					loadMore(repository.getOwner(), repository.getName(), page, resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());
				}
			}));

			fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
			fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

			loadInitial(repository.getOwner(), repository.getName(), resultLimit, requestType, repository.getIssueState().toString(), filterIssueByMilestone);
			fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		});

		loadInitial(repository.getOwner(), repository.getName(), resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());

		return fragmentIssuesBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if(resumeIssues) {
			loadInitial(repository.getOwner(), repository.getName(), resultLimit, requestType, repository.getIssueState().toString(), repository.getIssueMilestoneFilterName());
			resumeIssues = false;
		}
	}

	private void loadInitial(String repoOwner, String repoName, int resultLimit, String requestType, String issueState, String filterByMilestone) {

		Call<List<Issue>> call = RetrofitClient.getApiInterface(context).issueListIssues(repoOwner, repoName, issueState, null, null, requestType,
			filterByMilestone, null, null, null, null, null, 1, resultLimit);

		call.enqueue(new Callback<List<Issue>>() {
			@Override
			public void onResponse(@NonNull Call<List<Issue>> call, @NonNull Response<List<Issue>> response) {

				if(response.code() == 200) {
					assert response.body() != null;
					if(response.body().size() > 0) {
						issuesList.clear();
						issuesList.addAll(response.body());
						adapter.notifyDataChanged();
						fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
					}
					else {
						issuesList.clear();
						adapter.notifyDataChanged();
						fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
					}
					fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
					fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String repoOwner, String repoName, int page, int resultLimit, String requestType, String issueState, String filterByMilestone) {

		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Issue>> call = RetrofitClient.getApiInterface(context).issueListIssues(repoOwner, repoName, issueState, null, null, requestType,
			filterByMilestone, null, null, null, null, null, page, resultLimit);
		call.enqueue(new Callback<List<Issue>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issue>> call, @NonNull Response<List<Issue>> response) {
				if(response.code() == 200) {
					List<Issue> result = response.body();
					assert result != null;
					if(result.size() > 0) {
						pageSize = result.size();
						issuesList.addAll(result);
					}
					else {
						SnackBar.info(context, fragmentIssuesBinding.getRoot(), getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);
					}
					adapter.notifyDataChanged();
					fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		this.menu = menu;
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.filter_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		if(repository.getIssueState().toString().equals("closed")) {
			menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
		}
		else {
			menu.getItem(1).setIcon(R.drawable.ic_filter);
		}

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

		List<Issue> arr = new ArrayList<>();

		for(Issue d : issuesList) {
			if(d == null || d.getTitle() == null || d.getBody() == null) {
				continue;
			}
			if(d.getTitle().toLowerCase().contains(text) || d.getBody().toLowerCase().contains(text) || String.valueOf(d.getNumber()).startsWith(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}
}
