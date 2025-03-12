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
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Commit;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.CommitsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCommitsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author qwerty287
 */
public class PullRequestCommitsFragment extends Fragment {

	private final String TAG = "PullRequestCommitsFragment";
	private final List<Commit> commitsList = new ArrayList<>();
	private ActivityCommitsBinding binding;
	private Context ctx;
	private int resultLimit;
	private int pageSize = 1;
	private CommitsAdapter adapter;

	public PullRequestCommitsFragment() {}

	public static PullRequestCommitsFragment newInstance() {
		return new PullRequestCommitsFragment();
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (binding != null) {
			ctx = requireContext();
			return binding.getRoot();
		}

		binding = ActivityCommitsBinding.inflate(inflater, container, false);
		ctx = requireContext();
		IssueContext issue = IssueContext.fromIntent(requireActivity().getIntent());
		binding.toolbar.setVisibility(View.GONE);

		resultLimit = Constants.getCurrentResultLimit(ctx);

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											binding.pullToRefresh.setRefreshing(false);
											loadInitial(issue, resultLimit);
											adapter.notifyDataChanged();
										},
										200));

		CoordinatorLayout.LayoutParams params =
				new CoordinatorLayout.LayoutParams(
						CoordinatorLayout.LayoutParams.MATCH_PARENT,
						CoordinatorLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 0);
		binding.pullToRefresh.setLayoutParams(params);

		CoordinatorLayout.LayoutParams paramsProgressBar =
				new CoordinatorLayout.LayoutParams(
						CoordinatorLayout.LayoutParams.MATCH_PARENT,
						CoordinatorLayout.LayoutParams.WRAP_CONTENT);
		paramsProgressBar.setMargins(0, 0, 0, 0);
		binding.progressBar.setLayoutParams(paramsProgressBar);

		adapter = new CommitsAdapter(ctx, commitsList);
		adapter.setLoadMoreListener(
				() ->
						binding.recyclerView.post(
								() -> {
									if (commitsList.size() == resultLimit
											|| pageSize == resultLimit) {

										int page = (commitsList.size() + resultLimit) / resultLimit;
										loadMore(page, issue, resultLimit);
									}
								}));

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);

		loadInitial(issue, resultLimit);

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {

							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {

								menuInflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {

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

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		return binding.getRoot();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		this.getClass().getName();
	}

	private void loadInitial(IssueContext issue, int resultLimit) {

		Call<List<Commit>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGetPullRequestCommits(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								1,
								resultLimit,
								null,
								null);

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
								binding.noDataCommits.setVisibility(View.GONE);
							} else {

								commitsList.clear();
								adapter.notifyDataChanged();
								binding.noDataCommits.setVisibility(View.VISIBLE);
							}
						}
						if (response.code() == 409) {

							binding.noDataCommits.setVisibility(View.VISIBLE);
						} else {

							Log.e(TAG, String.valueOf(response.code()));
						}

						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<List<Commit>> call, @NonNull Throwable t) {

						Toasty.error(
								ctx, getResources().getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadMore(final int page, IssueContext issue, int resultLimit) {

		binding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Commit>> call =
				RetrofitClient.getApiInterface(ctx)
						.repoGetPullRequestCommits(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								(long) issue.getIssueIndex(),
								page,
								resultLimit,
								null,
								null);

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

						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<List<Commit>> call, @NonNull Throwable t) {

						Toasty.error(
								ctx, getResources().getString(R.string.genericServerResponseError));
					}
				});
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
}
