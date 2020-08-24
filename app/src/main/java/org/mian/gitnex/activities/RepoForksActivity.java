package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
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
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoForksAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.UserRepositories;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class RepoForksActivity extends BaseActivity {

	final Context ctx = this;
	private Context appCtx;
	private View.OnClickListener onClickListener;
	private TextView noData;
	private ProgressBar progressBar;
	private String TAG = "RepositoryForks";
	private int resultLimit = StaticGlobalVariables.resultLimitOldGiteaInstances;
	private int pageSize = 1;

	private RecyclerView recyclerView;
	private List<UserRepositories> forksList;
	private RepoForksAdapter adapter;
	private ProgressBar progressLoadMore;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_forks;
	}

	@SuppressLint("DefaultLocale")
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

		String repoFullNameForForks = getIntent().getStringExtra("repoFullNameForForks");
		assert repoFullNameForForks != null;
		String[] parts = repoFullNameForForks.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		TextView toolbar_title = findViewById(R.id.toolbar_title);
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());
		toolbar_title.setText(String.format("%s : %s", ctx.getResources().getString(R.string.infoTabRepoForksCount), repoName));

		ImageView closeActivity = findViewById(R.id.close);
		noData = findViewById(R.id.noData);
		progressLoadMore = findViewById(R.id.progressLoadMore);
		progressBar = findViewById(R.id.progress_bar);
		SwipeRefreshLayout swipeRefresh = findViewById(R.id.pullToRefresh);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		// if gitea is 1.12 or higher use the new limit (resultLimitNewGiteaInstances)
		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.12")) {
			resultLimit = StaticGlobalVariables.resultLimitNewGiteaInstances;
		}

		recyclerView = findViewById(R.id.recyclerView);
		forksList = new ArrayList<>();

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		recyclerView.addItemDecoration(dividerItemDecoration);

		swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			loadInitial(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, pageSize, resultLimit);
			adapter.notifyDataChanged();

		}, 200));

		adapter = new RepoForksAdapter(ctx, forksList);
		adapter.setLoadMoreListener(() -> recyclerView.post(() -> {

			if(forksList.size() == resultLimit || pageSize == resultLimit) {

				int page = (forksList.size() + resultLimit) / resultLimit;
				loadMore(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, page, resultLimit);

			}

		}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		recyclerView.setAdapter(adapter);

		loadInitial(instanceUrl, Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, pageSize, resultLimit);

	}

	private void loadInitial(String instanceUrl, String instanceToken, String repoOwner, String repoName, int pageSize, int resultLimit) {

		Call<List<UserRepositories>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getRepositoryForks(instanceToken, repoOwner, repoName, pageSize, resultLimit);

		call.enqueue(new Callback<List<UserRepositories>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						forksList.clear();
						forksList.addAll(response.body());
						adapter.notifyDataChanged();
						noData.setVisibility(View.GONE);

					}
					else {
						forksList.clear();
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
			public void onFailure(@NonNull Call<List<UserRepositories>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
			}

		});

	}

	private void loadMore(String instanceUrl, String instanceToken, String repoOwner, String repoName, int page, int resultLimit) {

		progressLoadMore.setVisibility(View.VISIBLE);

		Call<List<UserRepositories>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getRepositoryForks(instanceToken, repoOwner, repoName, page, resultLimit);

		call.enqueue(new Callback<List<UserRepositories>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {

					//remove loading view
					forksList.remove(forksList.size() - 1);

					List<UserRepositories> result = response.body();

					assert result != null;
					if(result.size() > 0) {

						pageSize = result.size();
						forksList.addAll(result);

					}
					else {

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
			public void onFailure(@NonNull Call<List<UserRepositories>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
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

		List<UserRepositories> arr = new ArrayList<>();

		for(UserRepositories d : forksList) {
			if(d.getName().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

	private void initCloseListener() {

		onClickListener = view -> {
			getIntent().removeExtra("repoFullNameForForks");
			finish();
		};
	}

}