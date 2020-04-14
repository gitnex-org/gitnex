package org.mian.gitnex.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter;
import com.mikepenz.fastadapter.listeners.ItemFilterListener;
import com.mikepenz.fastadapter_extensions.items.ProgressItem;
import com.mikepenz.fastadapter_extensions.scroll.EndlessRecyclerOnScrollListener;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import static com.mikepenz.fastadapter.adapters.ItemAdapter.items;

/**
 * Author M M Arif
 */

public class IssuesOpenFragment extends Fragment implements ItemFilterListener<IssuesAdapter> {

	private ProgressBar mProgressBar;
	private boolean loadNextFlag = false;
	private String TAG = StaticGlobalVariables.tagIssuesListOpen;
	private TextView noDataIssues;
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private String requestType = StaticGlobalVariables.issuesRequestType;

	private List<IssuesAdapter> items = new ArrayList<>();
	private FastItemAdapter<IssuesAdapter> fastItemAdapter;
	private ItemAdapter footerAdapter;
	private EndlessRecyclerOnScrollListener endlessRecyclerOnScrollListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_issues, container, false);
		setHasOptionsMenu(true);

		TinyDB tinyDb = new TinyDB(getContext());
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		if (VersionCheck.compareVersion("1.12.0", tinyDb.getString("giteaVersion")) < 1) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		noDataIssues = v.findViewById(R.id.noDataIssues);
		mProgressBar = v.findViewById(R.id.progress_bar);
		final SwipeRefreshLayout swipeRefreshLayout = v.findViewById(R.id.pullToRefresh);

		RecyclerView recyclerView = v.findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setHasFixedSize(true);

		fastItemAdapter = new FastItemAdapter<>();
		fastItemAdapter.withSelectable(true);

		footerAdapter = items();
		//noinspection unchecked
		fastItemAdapter.addAdapter(StaticGlobalVariables.issuesPageInit, footerAdapter);

		fastItemAdapter.getItemFilter().withFilterPredicate((IItemAdapter.Predicate<IssuesAdapter>) (item, constraint) -> item.getIssueTitle().toLowerCase().contains(constraint.toString().toLowerCase()));

		fastItemAdapter.getItemFilter().withItemFilterListener(this);

		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(fastItemAdapter);

		endlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener(footerAdapter) {

			@Override
			public void onLoadMore(final int currentPage) {

				loadNext(instanceUrl, instanceToken, repoOwner, repoName, resultLimit, requestType, currentPage);

			}

		};

		swipeRefreshLayout.setOnRefreshListener(() -> {

			mProgressBar.setVisibility(View.VISIBLE);
			fastItemAdapter.clear();
			endlessRecyclerOnScrollListener.resetPageCount();
			swipeRefreshLayout.setRefreshing(false);

		});

		recyclerView.addOnScrollListener(endlessRecyclerOnScrollListener);

		loadInitial(instanceUrl, instanceToken, repoOwner, repoName, resultLimit, requestType);

		fastItemAdapter.withEventHook(new IssuesAdapter.IssueTitleClickEvent());

		assert savedInstanceState != null;
		fastItemAdapter.withSavedInstanceState(savedInstanceState);

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();
		TinyDB tinyDb = new TinyDB(getContext());

		if(tinyDb.getBoolean("resumeIssues")) {

			mProgressBar.setVisibility(View.VISIBLE);
			fastItemAdapter.clear();
			endlessRecyclerOnScrollListener.resetPageCount();
			tinyDb.putBoolean("resumeIssues", false);

		}

	}

	private void loadInitial(String instanceUrl, String token, String repoOwner, String repoName, int resultLimit, String requestType) {

		Call<List<Issues>> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().getIssues(token, repoOwner, repoName, 1, resultLimit, requestType);

		call.enqueue(new Callback<List<Issues>>() {

			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						if(response.body().size() == resultLimit) {
							loadNextFlag = true;
						}

						for(int i = 0; i < response.body().size(); i++) {
							items.add(new IssuesAdapter(getContext()).withNewItems(response.body().get(i).getTitle(), response.body().get(i).getNumber(), response.body().get(i).getUser().getAvatar_url(), response.body().get(i).getCreated_at(), response.body().get(i).getComments(), response.body().get(i).getUser().getFull_name(), response.body().get(i).getUser().getLogin()));
						}

						fastItemAdapter.add(items);
						noDataIssues.setVisibility(View.GONE);

					}
					else {
						noDataIssues.setVisibility(View.VISIBLE);
					}

					mProgressBar.setVisibility(View.GONE);

				}
				else {
					Log.i(TAG, String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
			}

		});

	}

	private void loadNext(String instanceUrl, String token, String repoOwner, String repoName, int resultLimit, String requestType, final int currentPage) {

		footerAdapter.clear();
		//noinspection unchecked
		footerAdapter.add(new ProgressItem().withEnabled(false));
		Handler handler = new Handler();

		handler.postDelayed(() -> {

			Call<List<Issues>> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().getIssues(token, repoOwner, repoName, currentPage + 1, resultLimit, requestType);

			call.enqueue(new Callback<List<Issues>>() {

				@Override
				public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {

					if(response.isSuccessful()) {

						assert response.body() != null;

						if(response.body().size() > 0) {

							loadNextFlag = response.body().size() == resultLimit;

							for(int i = 0; i < response.body().size(); i++) {

								fastItemAdapter.add(fastItemAdapter.getAdapterItemCount(), new IssuesAdapter(getContext()).withNewItems(response.body().get(i).getTitle(), response.body().get(i).getNumber(), response.body().get(i).getUser().getAvatar_url(), response.body().get(i).getCreated_at(), response.body().get(i).getComments(), response.body().get(i).getUser().getFull_name(), response.body().get(i).getUser().getLogin()));

							}

							footerAdapter.clear();
							noDataIssues.setVisibility(View.GONE);

						}
						else {
							footerAdapter.clear();
						}

						mProgressBar.setVisibility(View.GONE);

					}
					else {
						Log.i(TAG, String.valueOf(response.code()));
					}

				}

				@Override
				public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {

					Log.i(TAG, t.toString());
				}

			});

		}, 1000);

		if(!loadNextFlag) {
			footerAdapter.clear();
		}

	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.search_menu, menu);
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

				fastItemAdapter.filter(newText);
				return true;
			}

		});

		endlessRecyclerOnScrollListener.enable();

	}

	@Override
	public void itemsFiltered(@Nullable CharSequence constraint, @Nullable List<IssuesAdapter> results) {

		endlessRecyclerOnScrollListener.disable();
	}

	@Override
	public void onReset() {

		endlessRecyclerOnScrollListener.enable();
	}

}
