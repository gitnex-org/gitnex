package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.RepoForksAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityRepoForksBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class RepoForksActivity extends BaseActivity {

	private final String TAG = "RepositoryForks";
	private ActivityRepoForksBinding activityRepoForksBinding;
	private int resultLimit;
	private int pageSize = 1;
	private List<Repository> forksList;
	private RepoForksAdapter adapter;

	private RepositoryContext repository;

	@SuppressLint("DefaultLocale")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityRepoForksBinding = ActivityRepoForksBinding.inflate(getLayoutInflater());
		setContentView(activityRepoForksBinding.getRoot());

		Toolbar toolbar = activityRepoForksBinding.toolbar;
		setSupportActionBar(toolbar);

		repository = RepositoryContext.fromIntent(getIntent());
		final String repoOwner = repository.getOwner();
		final String repoName = repository.getName();

		activityRepoForksBinding.toolbarTitle.setText(ctx.getResources().getString(R.string.infoTabRepoForksCount));

		activityRepoForksBinding.close.setOnClickListener(v -> finish());
		resultLimit = Constants.getCurrentResultLimit(ctx);
		forksList = new ArrayList<>();

		activityRepoForksBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			pageSize = 1;
			activityRepoForksBinding.pullToRefresh.setRefreshing(false);
			loadInitial(repoOwner, repoName, pageSize, resultLimit);
			adapter.notifyDataChanged();

		}, 200));

		adapter = new RepoForksAdapter(ctx, forksList);
		adapter.setLoadMoreListener(() -> activityRepoForksBinding.recyclerView.post(() -> {

			if(forksList.size() == resultLimit || pageSize == resultLimit) {

				int page = (forksList.size() + resultLimit) / resultLimit;
				loadMore(repoOwner, repoName, page, resultLimit);
			}
		}));

		activityRepoForksBinding.recyclerView.setHasFixedSize(true);
		activityRepoForksBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		activityRepoForksBinding.recyclerView.setAdapter(adapter);

		loadInitial(repoOwner, repoName, pageSize, resultLimit);
	}

	private void loadInitial(String repoOwner, String repoName, int pageSize, int resultLimit) {

		Call<List<Repository>> call = RetrofitClient.getApiInterface(ctx).listForks(repoOwner, repoName, pageSize, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;

					if(response.body().size() > 0) {
						forksList.clear();
						forksList.addAll(response.body());
						adapter.notifyDataChanged();
						activityRepoForksBinding.noData.setVisibility(View.GONE);
					}
					else {
						forksList.clear();
						adapter.notifyDataChanged();
						activityRepoForksBinding.noData.setVisibility(View.VISIBLE);
					}

					activityRepoForksBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String repoOwner, String repoName, int page, int resultLimit) {

		activityRepoForksBinding.progressLoadMore.setVisibility(View.VISIBLE);

		Call<List<Repository>> call = RetrofitClient.getApiInterface(ctx).listForks(repoOwner, repoName, page, resultLimit);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {

				if(response.isSuccessful()) {

					//remove loading view
					forksList.remove(forksList.size() - 1);

					List<Repository> result = response.body();
					assert result != null;

					if(result.size() > 0) {
						pageSize = result.size();
						forksList.addAll(result);
					}
					else {
						adapter.setMoreDataAvailable(false);
					}

					adapter.notifyDataChanged();
					activityRepoForksBinding.progressLoadMore.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
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
		List<Repository> userRepositories = new ArrayList<>();

		for(Repository d : forksList) {
			if(d.getOwner().getLogin().contains(text) || d.getName().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {

				userRepositories.add(d);
			}
		}

		adapter.updateList(userRepositories);
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}
