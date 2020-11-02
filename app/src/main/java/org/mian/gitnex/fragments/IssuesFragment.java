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
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.Issues;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class IssuesFragment extends Fragment {

	private Menu menu;
	private RecyclerView recyclerView;
	private List<Issues> issuesList;
	private IssuesAdapter adapter;
	private Context context;
	private int pageSize = StaticGlobalVariables.issuesPageInit;
	private ProgressBar mProgressBar;
	private String TAG = StaticGlobalVariables.tagIssuesList;
	private TextView noDataIssues;
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private String requestType = StaticGlobalVariables.issuesRequestType;
	private ProgressBar progressLoadMore;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_issues, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		TinyDB tinyDb = TinyDB.getInstance(getContext());
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

		// if gitea is 1.12 or higher use the new limit
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		recyclerView = v.findViewById(R.id.recyclerView);
		issuesList = new ArrayList<>();

		progressLoadMore = v.findViewById(R.id.progressLoadMore);
		mProgressBar = v.findViewById(R.id.progress_bar);
		noDataIssues = v.findViewById(R.id.noDataIssues);

		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(instanceToken, repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));
			adapter.notifyDataChanged();

		}, 200));

		adapter = new IssuesAdapter(getContext(), issuesList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(issuesList.size() == resultLimit || pageSize == resultLimit) {

				int page = (issuesList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.get(getContext()), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

			}

		}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(adapter);

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListener(issueState -> {

			if(issueState.equals("closed")) {
				menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
			}
			else {
				menu.getItem(1).setIcon(R.drawable.ic_filter);
			}

			issuesList.clear();

			adapter = new IssuesAdapter(getContext(), issuesList);
			adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

				if(issuesList.size() == resultLimit || pageSize == resultLimit) {

					int page = (issuesList.size() + resultLimit) / resultLimit;
					loadMore(Authorization.get(getContext()), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

				}

			}));

			tinyDb.putString("repoIssuesState", issueState);

			mProgressBar.setVisibility(View.VISIBLE);
			noDataIssues.setVisibility(View.GONE);

			loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, requestType, issueState);
			recyclerView.setAdapter(adapter);

		});

		loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = TinyDB.getInstance(getContext());

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		if(tinyDb.getBoolean("resumeIssues")) {

			loadInitial(Authorization.get(getContext()), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));
			tinyDb.putBoolean("resumeIssues", false);

		}

	}

	private void loadInitial(String token, String repoOwner, String repoName, int resultLimit, String requestType, String issueState) {

		Call<List<Issues>> call = RetrofitClient.getApiInterface(context).getIssues(token, repoOwner, repoName, 1, resultLimit, requestType, issueState);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						issuesList.clear();
						issuesList.addAll(response.body());
						adapter.notifyDataChanged();
						noDataIssues.setVisibility(View.GONE);

					}
					else {

						issuesList.clear();
						adapter.notifyDataChanged();
						noDataIssues.setVisibility(View.VISIBLE);

					}

					mProgressBar.setVisibility(View.GONE);

				}
				else if(response.code() == 404) {

					noDataIssues.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);

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

	private void loadMore(String token, String repoOwner, String repoName, int page, int resultLimit, String requestType, String issueState) {

		progressLoadMore.setVisibility(View.VISIBLE);

		Call<List<Issues>> call = RetrofitClient.getApiInterface(context).getIssues(token, repoOwner, repoName, page, resultLimit, requestType, issueState);

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

						Toasty.warning(context, getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);

					}

					adapter.notifyDataChanged();
					progressLoadMore.setVisibility(View.GONE);

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
			if(d.getTitle().toLowerCase().contains(text) || d.getBody().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

}
