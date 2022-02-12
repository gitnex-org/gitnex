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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.models.PullRequests;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.PullRequestsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentPullRequestsBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class PullRequestsFragment extends Fragment {

	private FragmentPullRequestsBinding fragmentPullRequestsBinding;
	private Menu menu;

	private List<PullRequests> prList;
	private PullRequestsAdapter adapter;
	private final String TAG = Constants.tagPullRequestsList;
	private Context context;
	private int pageSize = Constants.prPageInit;
	private int resultLimit;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentPullRequestsBinding = FragmentPullRequestsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		TinyDB tinyDb = TinyDB.getInstance(getContext());
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		final SwipeRefreshLayout swipeRefresh = fragmentPullRequestsBinding.pullToRefresh;

		resultLimit = Constants.getCurrentResultLimit(context);
		prList = new ArrayList<>();

		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			swipeRefresh.setRefreshing(false);
			loadInitial(instanceToken, repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new PullRequestsAdapter(getContext(), prList);
		adapter.setLoadMoreListener(() -> fragmentPullRequestsBinding.recyclerView.post(() -> {

			if(prList.size() == resultLimit || pageSize == resultLimit) {
				int page = (prList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.get(getContext()), repoOwner, repoName, page, tinyDb.getString("repoPrState"), resultLimit);
			}

		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentPullRequestsBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		fragmentPullRequestsBinding.recyclerView.setHasFixedSize(true);
		fragmentPullRequestsBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentPullRequestsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentPullRequestsBinding.recyclerView.setAdapter(adapter);

		((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerPr(prState -> {

			if(prState.equals("closed")) {
				menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
			}
			else {
				menu.getItem(1).setIcon(R.drawable.ic_filter);
			}

			prList.clear();

			adapter = new PullRequestsAdapter(context, prList);
			adapter.setLoadMoreListener(() -> fragmentPullRequestsBinding.recyclerView.post(() -> {

				if(prList.size() == resultLimit || pageSize == resultLimit) {
					int page = (prList.size() + resultLimit) / resultLimit;
					loadMore(Authorization.get(getContext()), repoOwner, repoName, page, tinyDb.getString("repoPrState"), resultLimit);
				}

			}));

			tinyDb.putString("repoPrState", prState);

			fragmentPullRequestsBinding.progressBar.setVisibility(View.VISIBLE);
			fragmentPullRequestsBinding.noData.setVisibility(View.GONE);

			loadInitial(Authorization.get(context), repoOwner, repoName, pageSize, prState, resultLimit);
			fragmentPullRequestsBinding.recyclerView.setAdapter(adapter);
		});

		loadInitial(Authorization.get(getContext()), repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);

		return fragmentPullRequestsBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = TinyDB.getInstance(getContext());

		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		if(tinyDb.getBoolean("resumePullRequests")) {
			loadInitial(Authorization.get(getContext()), repoOwner, repoName, pageSize, tinyDb.getString("repoPrState"), resultLimit);
			tinyDb.putBoolean("resumePullRequests", false);
			tinyDb.putBoolean("prMerged", false);
		}
	}

	private void loadInitial(String token, String repoOwner, String repoName, int page, String prState, int resultLimit) {

		Call<List<PullRequests>> call = RetrofitClient.getApiInterface(context).getPullRequests(token, repoOwner, repoName, page, prState, resultLimit);

		call.enqueue(new Callback<List<PullRequests>>() {

			@Override
			public void onResponse(@NonNull Call<List<PullRequests>> call, @NonNull Response<List<PullRequests>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					if(response.body().size() > 0) {
						prList.clear();
						prList.addAll(response.body());
						adapter.notifyDataChanged();
						fragmentPullRequestsBinding.noData.setVisibility(View.GONE);
					}
					else {
						prList.clear();
						adapter.notifyDataChanged();
						fragmentPullRequestsBinding.noData.setVisibility(View.VISIBLE);
					}
					fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					fragmentPullRequestsBinding.noData.setVisibility(View.VISIBLE);
					fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
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

		fragmentPullRequestsBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<PullRequests>> call = RetrofitClient.getApiInterface(context).getPullRequests(token, repoOwner, repoName, page, prState, resultLimit);

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
					fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
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

		TinyDB tinyDb = TinyDB.getInstance(context);

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
