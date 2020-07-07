package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.clients.AppApiService;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.interfaces.ApiInterface;
import org.mian.gitnex.models.Commits;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class CommitsActivity extends BaseActivity {

	final Context ctx = this;
	private Context appCtx;
	private View.OnClickListener onClickListener;
	private TextView noData;
	private ProgressBar progressBar;
	private String TAG = "CommitsActivity";
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private int pageSize = 1;

	private RecyclerView recyclerView;
	private List<Commits> commitsList;
	private CommitsAdapter adapter;
	private ApiInterface api;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_commits;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		TinyDB tinyDb = new TinyDB(appCtx);
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		String branchName = getIntent().getStringExtra("branchName");

		TextView toolbar_title = findViewById(R.id.toolbar_title);
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());
		toolbar_title.setText(branchName);

		ImageView closeActivity = findViewById(R.id.close);
		noData = findViewById(R.id.noDataCommits);
		progressBar = findViewById(R.id.progress_bar);
		SwipeRefreshLayout swipeRefresh = findViewById(R.id.pullToRefresh);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		// if gitea is 1.12 or higher use the new limit (resultLimitNewGiteaInstances)
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12")) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		recyclerView = findViewById(R.id.recyclerView);
		commitsList = new ArrayList<>();

		swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, branchName, resultLimit);
			adapter.notifyDataChanged();

		}, 200));

		adapter = new CommitsAdapter(ctx, commitsList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(commitsList.size() == resultLimit || pageSize == resultLimit) {

				int page = (commitsList.size() + resultLimit) / resultLimit;
				loadMore(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, page, branchName, resultLimit);

			}

		}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		recyclerView.setAdapter(adapter);

		api = AppApiService.createService(ApiInterface.class, instanceUrl, ctx);
		loadInitial(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, branchName, resultLimit);

	}

	private void loadInitial(String token, String repoOwner, String repoName, String branchName, int resultLimit) {

		Call<List<Commits>> call = api.getRepositoryCommits(token, repoOwner, repoName, 1, branchName, resultLimit);

		call.enqueue(new Callback<List<Commits>>() {

			@Override
			public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

				if(response.isSuccessful()) {

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

					progressBar.setVisibility(View.GONE);

				}
				else {

					Log.e(TAG, String.valueOf(response.code()));

				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
			}

		});

	}

	private void loadMore(String token, String repoOwner, String repoName, final int page, String branchName, int resultLimit) {

		//add loading progress view
		commitsList.add(new Commits("load"));
		adapter.notifyItemInserted((commitsList.size() - 1));

		Call<List<Commits>> call = api.getRepositoryCommits(token, repoOwner, repoName, page, branchName, resultLimit);

		call.enqueue(new Callback<List<Commits>>() {

			@Override
			public void onResponse(@NonNull Call<List<Commits>> call, @NonNull Response<List<Commits>> response) {

				if(response.isSuccessful()) {

					//remove loading view
					commitsList.remove(commitsList.size() - 1);

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

			}

			@Override
			public void onFailure(@NonNull Call<List<Commits>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

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
			getIntent().removeExtra("branchName");
			finish();
		};
	}

}


