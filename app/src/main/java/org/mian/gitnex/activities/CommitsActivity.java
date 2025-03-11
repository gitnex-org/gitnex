package org.mian.gitnex.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class CommitsActivity extends BaseActivity {

	private final String TAG = "CommitsActivity";
	public RepositoryContext repository;
	private View.OnClickListener onClickListener;
	private TextView noData;
	private ProgressBar progressBar;
	private int resultLimit;
	private int pageSize = 1;
	private RecyclerView recyclerView;
	private List<Commit> commitsList;
	private CommitsAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getClass().getName();

		ActivityCommitsBinding activityCommitsBinding =
				ActivityCommitsBinding.inflate(getLayoutInflater());
		setContentView(activityCommitsBinding.getRoot());

		Toolbar toolbar = activityCommitsBinding.toolbar;
		setSupportActionBar(toolbar);

		repository = RepositoryContext.fromIntent(getIntent());
		String branchName = repository.getBranchRef();

		Window window = getWindow();
		window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		window.setStatusBarColor(Color.TRANSPARENT);

		window.getDecorView()
				.setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

		int statusBarHeight = 36;
		int extraSpacing = (int) (36 * getResources().getDisplayMetrics().density);
		int totalTopMargin = statusBarHeight + extraSpacing;
		CoordinatorLayout.LayoutParams params =
				(CoordinatorLayout.LayoutParams) activityCommitsBinding.appbar.getLayoutParams();
		params.setMargins(0, totalTopMargin, 0, 0);
		activityCommitsBinding.appbar.setLayoutParams(params);

		activityCommitsBinding.appbar.post(
				() -> {
					int adjustedTopMargin =
							activityCommitsBinding.appbar.getHeight() + totalTopMargin;
					CoordinatorLayout.LayoutParams refreshParams =
							(CoordinatorLayout.LayoutParams)
									activityCommitsBinding.pullToRefresh.getLayoutParams();
					refreshParams.setMargins(0, adjustedTopMargin, 0, 0);
					activityCommitsBinding.pullToRefresh.setLayoutParams(refreshParams);

					CoordinatorLayout.LayoutParams progressParams =
							(CoordinatorLayout.LayoutParams)
									activityCommitsBinding.progressBar.getLayoutParams();
					progressParams.setMargins(0, adjustedTopMargin, 0, 0);
					activityCommitsBinding.progressBar.setLayoutParams(progressParams);
				});

		TextView toolbar_title = activityCommitsBinding.toolbarTitle;
		toolbar_title.setMovementMethod(new ScrollingMovementMethod());
		toolbar_title.setText(branchName);

		ImageView closeActivity = activityCommitsBinding.close;
		noData = activityCommitsBinding.noDataCommits;
		progressBar = activityCommitsBinding.progressBar;
		SwipeRefreshLayout swipeRefresh = activityCommitsBinding.pullToRefresh;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		resultLimit = Constants.getCurrentResultLimit(ctx);

		recyclerView = activityCommitsBinding.recyclerView;
		commitsList = new ArrayList<>();

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											swipeRefresh.setRefreshing(false);
											loadInitial(
													repository.getOwner(),
													repository.getName(),
													branchName,
													resultLimit);
											adapter.notifyDataChanged();
										},
										200));

		adapter = new CommitsAdapter(ctx, commitsList);
		adapter.setLoadMoreListener(
				() ->
						recyclerView.post(
								() -> {
									if (commitsList.size() == resultLimit
											|| pageSize == resultLimit) {

										int page = (commitsList.size() + resultLimit) / resultLimit;
										loadMore(
												repository.getOwner(),
												repository.getName(),
												page,
												branchName,
												resultLimit);
									}
								}));

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		recyclerView.setAdapter(adapter);

		loadInitial(repository.getOwner(), repository.getName(), branchName, resultLimit);
	}

	private void loadInitial(
			String repoOwner, String repoName, String branchName, int resultLimit) {

		Call<List<Commit>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGetAllCommits(
								repoOwner,
								repoName,
								branchName,
								null,
								true,
								false,
								true,
								1,
								resultLimit,
								"");

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Commit>> call,
							@NonNull Response<List<Commit>> response) {

						if (response.code() == 200) {

							assert response.body() != null;
							if (!response.body().isEmpty()) {

								commitsList.clear();
								commitsList.addAll(response.body());
								adapter.notifyDataChanged();
								noData.setVisibility(View.GONE);
							} else {

								commitsList.clear();
								adapter.notifyDataChanged();
								noData.setVisibility(View.VISIBLE);
							}
						}
						if (response.code() == 409) {

							noData.setVisibility(View.VISIBLE);
						} else {

							Log.e(TAG, String.valueOf(response.code()));
						}

						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<List<Commit>> call, @NonNull Throwable t) {

						Toasty.error(
								ctx, getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadMore(
			String repoOwner, String repoName, final int page, String branchName, int resultLimit) {

		progressBar.setVisibility(View.VISIBLE);

		Call<List<Commit>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGetAllCommits(
								repoOwner,
								repoName,
								branchName,
								null,
								true,
								false,
								true,
								page,
								resultLimit,
								"");

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Commit>> call,
							@NonNull Response<List<Commit>> response) {

						if (response.isSuccessful()) {

							List<Commit> result = response.body();
							assert result != null;

							if (!result.isEmpty()) {

								pageSize = result.size();
								commitsList.addAll(result);
							} else {

								adapter.setMoreDataAvailable(false);
							}

							adapter.notifyDataChanged();
						} else {

							Log.e(TAG, String.valueOf(response.code()));
						}

						progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<List<Commit>> call, @NonNull Throwable t) {

						Toasty.error(
								ctx, getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView =
				(androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(
				new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

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

		List<Commit> arr = new ArrayList<>();

		for (Commit d : commitsList) {

			if (d.getCommit().getMessage().toLowerCase().contains(text)
					|| d.getSha().toLowerCase().contains(text)) {

				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
