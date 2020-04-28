package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import org.mian.gitnex.clients.IssuesService;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	private ApiInterface api;
	private Context context;
	private int pageSize = StaticGlobalVariables.issuesPageInit;
	private ProgressBar mProgressBar;
	private String TAG = StaticGlobalVariables.tagIssuesList;
	private TextView noDataIssues;
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private String requestType = StaticGlobalVariables.issuesRequestType;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_issues, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		TinyDB tinyDb = new TinyDB(getContext());
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

		// if gitea is 1.12 or higher use the new limit
		if(VersionCheck.compareVersion("1.12.0", tinyDb.getString("giteaVersion")) >= 1) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		recyclerView = v.findViewById(R.id.recyclerView);
		issuesList = new ArrayList<>();

		mProgressBar = v.findViewById(R.id.progress_bar);
		noDataIssues = v.findViewById(R.id.noDataIssues);

		swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(instanceToken, repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));
			adapter.notifyDataChanged();

		}, 200));

		adapter = new IssuesAdapter(getContext(), issuesList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(issuesList.size() == resultLimit || pageSize == resultLimit) {

				int page = (issuesList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

			}

		}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(adapter);

		((RepoDetailActivity) Objects.requireNonNull(getActivity())).setFragmentRefreshListener(issueState -> {

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
					loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

				}

			}));

			tinyDb.putString("repoIssuesState", issueState);

			mProgressBar.setVisibility(View.VISIBLE);
			noDataIssues.setVisibility(View.GONE);

			loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, resultLimit, requestType, issueState);
			recyclerView.setAdapter(adapter);

		});

		api = IssuesService.createService(ApiInterface.class, instanceUrl, getContext());
		loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = new TinyDB(getContext());
		final String loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		if(tinyDb.getBoolean("resumeIssues")) {

			loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, resultLimit, requestType, tinyDb.getString("repoIssuesState"));
			tinyDb.putBoolean("resumeIssues", false);

		}

	}

	private void loadInitial(String token, String repoOwner, String repoName, int resultLimit, String requestType, String issueState) {

		Call<List<Issues>> call = api.getIssues(token, repoOwner, repoName, 1, resultLimit, requestType, issueState);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.isSuccessful()) {

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

		//add loading progress view
		issuesList.add(new Issues("load"));
		adapter.notifyItemInserted((issuesList.size() - 1));

		Call<List<Issues>> call = api.getIssues(token, repoOwner, repoName, page, resultLimit, requestType, issueState);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.isSuccessful()) {

					//remove loading view
					issuesList.remove(issuesList.size() - 1);

					List<Issues> result = response.body();

					assert result != null;
					if(result.size() > 0) {

						pageSize = result.size();
						issuesList.addAll(result);

					}
					else {

						Toasty.info(context, getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);

					}

					adapter.notifyDataChanged();

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

		TinyDB tinyDb = new TinyDB(context);

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
			if(d.getTitle().toLowerCase().contains(text) || d.getBody().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

}