package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.models.Commits;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CommitsActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private TextView noData;
	private ProgressBar progressBar;
	private final String TAG = "CommitsActivity";
	private int resultLimit = Constants.resultLimitOldGiteaInstances;
	private int pageSize = 1;

	private RecyclerView recyclerView;
	private List<Commits> commitsList;
	private CommitsAdapter adapter;
	private ProgressBar progressLoadMore;

	public RepositoryContext repository;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getClass().getName();

		ActivityCommitsBinding activityCommitsBinding = ActivityCommitsBinding.inflate(getLayoutInflater());
		setContentView(activityCommitsBinding.getRoot());

		Toolbar toolbar = activityCommitsBinding.toolbar;
		setSupportActionBar(toolbar);

		repository = RepositoryContext.fromIntent(getIntent());
		String branchName = repository.getBranchRef();

		TextView toolbar_title = activityCommitsBinding.toolbarTitle;
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());
		toolbar_title.setText(branchName);

		ImageView closeActivity = activityCommitsBinding.close;
		noData = activityCommitsBinding.noDataCommits;
		progressLoadMore = activityCommitsBinding.progressLoadMore;
		progressBar = activityCommitsBinding.progressBar;
		SwipeRefreshLayout swipeRefresh = activityCommitsBinding.pullToRefresh;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		// if gitea is 1.12 or higher use the new limit (resultLimitNewGiteaInstances)
		if(getAccount().requiresVersion("1.12")) {

			resultLimit = Constants.resultLimitNewGiteaInstances;
		}

		recyclerView = activityCommitsBinding.recyclerView;
		commitsList = new ArrayList<>();

		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(getAccount().getAuthorization(), repository.getOwner(), repository.getName(), branchName, resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new CommitsAdapter(ctx, commitsList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(commitsList.size() == resultLimit || pageSize == resultLimit) {

				int page = (commitsList.size() + resultLimit) / resultLimit;
				loadMore(getAccount().getAuthorization(), repository.getOwner(), repository.getName(), page, branchName, resultLimit);
			}
		}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		recyclerView.setAdapter(adapter);

		loadInitial(getAccount().getAuthorization(), repository.getOwner(), repository.getName(), branchName, resultLimit);
	}

	private void loadInitial(String token, String repoOwner, String repoName, String branchName, int resultLimit) {

		Call<List<Commits>> call = RetrofitClient.getApiInterface(ctx).getRepositoryCommits(token, repoOwner, repoName, 1, branchName, resultLimit);

		call.enqueue(new Callback<List<Commits>>() {

			@Override
			public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

				if(response.code() == 200) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						commitsList.clear();
						commitsList.addAll(response.body());
						adapter.notifyDataChanged();
						noData.setVisibility(View.GONE);
					}
					else {

						commitsList.clear();
						adapter.notifyDataChanged();
						noData.setVisibility(View.VISIBLE);
					}
				}
				if(response.code() == 409) {

					noData.setVisibility(View.VISIBLE);
				}
				else {

					Log.e(TAG, String.valueOf(response.code()));
				}

				progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getResources().getString(R.string.errorOnLogin));
			}

		});

	}

	private void loadMore(String token, String repoOwner, String repoName, final int page, String branchName, int resultLimit) {

		progressLoadMore.setVisibility(View.VISIBLE);

		Call<List<Commits>> call = RetrofitClient.getApiInterface(ctx).getRepositoryCommits(token, repoOwner, repoName, page, branchName, resultLimit);

		call.enqueue(new Callback<List<Commits>>() {

			@Override
			public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

				if(response.isSuccessful()) {

					List<Commits> result = response.body();
					assert result != null;

					if(result.size() > 0) {

						pageSize = result.size();
						commitsList.addAll(result);
					}
					else {

						adapter.setMoreDataAvailable(false);
					}

					adapter.notifyDataChanged();
				}
				else {

					Log.e(TAG, String.valueOf(response.code()));
				}

				progressLoadMore.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

				Toasty.error(ctx, getResources().getString(R.string.errorOnLogin));
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);

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
				return true;
			}

		});

		return super.onCreateOptionsMenu(menu);
	}

	private void filter(String text) {

		List<Commits> arr = new ArrayList<>();

		for(Commits d : commitsList) {

			if(d.getCommit().getMessage().toLowerCase().contains(text) || d.getSha().toLowerCase().contains(text)) {

				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

	private void initCloseListener() {

		onClickListener = view -> {

			finish();
		};
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}


