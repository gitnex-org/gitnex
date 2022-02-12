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
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssuesFragment extends Fragment {

	private FragmentIssuesBinding fragmentIssuesBinding;
	private Context context;

	private Menu menu;
	private List<Issues> issuesList;
	private IssuesAdapter adapter;

	private int pageSize = Constants.issuesPageInit;
	private final String TAG = Constants.tagIssuesList;
	private int resultLimit = Constants.resultLimitOldGiteaInstances;
	private final String requestType = Constants.issuesRequestType;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		TinyDB tinyDb = TinyDB.getInstance(context);
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		// if gitea is 1.12 or higher use the new limit
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		issuesList = new ArrayList<>();

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentIssuesBinding.pullToRefresh.setRefreshing(false);
			loadInitial(instanceToken, repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"), "");
			adapter.notifyDataChanged();
		}, 200));

		adapter = new IssuesAdapter(context, issuesList);
		adapter.setLoadMoreListener(() -> fragmentIssuesBinding.recyclerView.post(() -> {
			if(issuesList.size() == resultLimit || pageSize == resultLimit) {
				int page = (issuesList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.get(context), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"), "");
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
					loadMore(Authorization.get(context), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"), "");
				}
			}));

			tinyDb.putString("repoIssuesState", issueState);

			fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
			fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

			loadInitial(Authorization.get(context), repoOwner, repoName, resultLimit, requestType, issueState, "");
			fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		});

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerFilterIssuesByMilestone(filterIssueByMilestone -> {

			issuesList.clear();

			adapter = new IssuesAdapter(context, issuesList);
			adapter.setLoadMoreListener(() -> fragmentIssuesBinding.recyclerView.post(() -> {

				if(issuesList.size() == resultLimit || pageSize == resultLimit) {
					int page = (issuesList.size() + resultLimit) / resultLimit;
					loadMore(Authorization.get(context), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"), tinyDb.getString("issueMilestoneFilterId"));
				}
			}));

			tinyDb.putString("issueMilestoneFilterId", filterIssueByMilestone);

			fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
			fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);

			loadInitial(Authorization.get(context), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"), filterIssueByMilestone);
			fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		});

		loadInitial(Authorization.get(context), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"), tinyDb.getString("issueMilestoneFilterId"));

		return fragmentIssuesBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = TinyDB.getInstance(context);

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		if(tinyDb.getBoolean("resumeIssues")) {
			loadInitial(Authorization.get(context), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"), tinyDb.getString("issueMilestoneFilterId"));
			tinyDb.putBoolean("resumeIssues", false);
		}
	}

	private void loadInitial(String token, String repoOwner, String repoName, int resultLimit, String requestType, String issueState, String filterByMilestone) {

		Call<List<Issues>> call = RetrofitClient.getApiInterface(context).getIssues(token, repoOwner, repoName, 1, resultLimit, requestType, issueState, filterByMilestone);

		call.enqueue(new Callback<List<Issues>>() {
			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

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
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String token, String repoOwner, String repoName, int page, int resultLimit, String requestType, String issueState, String filterByMilestone) {

		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Issues>> call = RetrofitClient.getApiInterface(context).getIssues(token, repoOwner, repoName, page, resultLimit, requestType, issueState, filterByMilestone);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {
				if(response.code() == 200) {
					List<Issues> result = response.body();
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
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
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

		TinyDB tinyDb = TinyDB.getInstance(context);

		if(tinyDb.getString("repoIssuesState").equals("closed")) {
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

		List<Issues> arr = new ArrayList<>();

		for(Issues d : issuesList) {
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
