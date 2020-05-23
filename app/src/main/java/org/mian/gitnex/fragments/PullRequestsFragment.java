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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.PullRequestsAdapter;
import org.mian.gitnex.clients.AppApiService;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.models.PullRequests;
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

public class PullRequestsFragment extends Fragment {

	private Menu menu;
	private ProgressBar mProgressBar;
	private RecyclerView recyclerView;
	private List<PullRequests> prList;
	private PullRequestsAdapter adapter;
	private ApiInterface apiPR;
	private String TAG = StaticGlobalVariables.tagPullRequestsList;
	private Context context;
	private int pageSize = StaticGlobalVariables.prPageInit;
	private TextView noData;
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_pull_requests, container, false);
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
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12.0")) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		recyclerView = v.findViewById(R.id.recyclerView);
		prList = new ArrayList<>();

		mProgressBar = v.findViewById(R.id.progress_bar);
		noData = v.findViewById(R.id.noData);

		swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(instanceToken, repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);
			adapter.notifyDataChanged();

		}, 200));

		adapter = new PullRequestsAdapter(getContext(), prList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(prList.size() == 10 || pageSize == resultLimit) {

				int page = (prList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, tinyDb.getString("repoPrState"), resultLimit);

			}

		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		recyclerView.setHasFixedSize(true);
		recyclerView.addItemDecoration(dividerItemDecoration);
		recyclerView.setLayoutManager(new LinearLayoutManager(context));
		recyclerView.setAdapter(adapter);

		((RepoDetailActivity) Objects.requireNonNull(getActivity())).setFragmentRefreshListenerPr(prState -> {

			if(prState.equals("closed")) {
				menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
			}
			else {
				menu.getItem(1).setIcon(R.drawable.ic_filter);
			}

			prList.clear();

			adapter = new PullRequestsAdapter(context, prList);
			adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

				if(prList.size() == 10 || pageSize == resultLimit) {

					int page = (prList.size() + resultLimit) / resultLimit;
					loadMore(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, page, tinyDb.getString("repoPrState"), resultLimit);

				}

			}));

			tinyDb.putString("repoPrState", prState);

			mProgressBar.setVisibility(View.VISIBLE);
			noData.setVisibility(View.GONE);

			loadInitial(Authorization.returnAuthentication(context, loginUid, instanceToken), repoOwner, repoName, pageSize, prState, resultLimit);
			recyclerView.setAdapter(adapter);

		});

		apiPR = AppApiService.createService(ApiInterface.class, instanceUrl, context);
		loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);

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

		if(tinyDb.getBoolean("resumePullRequests")) {

			loadInitial(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);
			tinyDb.putBoolean("resumePullRequests", false);
			tinyDb.putBoolean("prMerged", false);

		}

	}

	private void loadInitial(String token, String repoOwner, String repoName, int page, String prState, int resultLimit) {

		Call<List<PullRequests>> call = apiPR.getPullRequests(token, repoOwner, repoName, page, prState, resultLimit);

		call.enqueue(new Callback<List<PullRequests>>() {

			@Override
			public void onResponse(@NonNull Call<List<PullRequests>> call, @NonNull Response<List<PullRequests>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						prList.clear();
						prList.addAll(response.body());
						adapter.notifyDataChanged();
						noData.setVisibility(View.GONE);

					}
					else {

						prList.clear();
						adapter.notifyDataChanged();
						noData.setVisibility(View.VISIBLE);

					}

					mProgressBar.setVisibility(View.GONE);

				}
				else if(response.code() == 404) {

					noData.setVisibility(View.VISIBLE);
					mProgressBar.setVisibility(View.GONE);

				}
				else {

					Log.i(TAG, String.valueOf(response.code()));

				}

				Log.i(TAG, String.valueOf(response.code()));

			}

			@Override
			public void onFailure(@NonNull Call<List<PullRequests>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
			}

		});

	}

	private void loadMore(String token, String repoOwner, String repoName, int page, String prState, int resultLimit) {

		//add loading progress view
		prList.add(new PullRequests("load"));
		adapter.notifyItemInserted((prList.size() - 1));

		Call<List<PullRequests>> call = apiPR.getPullRequests(token, repoOwner, repoName, page, prState, resultLimit);

		call.enqueue(new Callback<List<PullRequests>>() {

			@Override
			public void onResponse(@NonNull Call<List<PullRequests>> call, @NonNull Response<List<PullRequests>> response) {

				if(response.code() == 200) {

					//remove loading view
					prList.remove(prList.size() - 1);

					List<PullRequests> result = response.body();

					assert result != null;
					if(result.size() > 0) {

						pageSize = result.size();
						prList.addAll(result);

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
			public void onFailure(@NonNull Call<List<PullRequests>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		this.menu = menu;
		inflater.inflate(R.menu.search_menu, menu);
		inflater.inflate(R.menu.filter_menu_pr, menu);
		super.onCreateOptionsMenu(menu, inflater);

		TinyDB tinyDb = new TinyDB(context);

		if(tinyDb.getString("repoPrState").equals("closed")) {
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

		List<PullRequests> arr = new ArrayList<>();

		for(PullRequests d : prList) {
			if(d.getTitle().toLowerCase().contains(text) || d.getBody().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

}
