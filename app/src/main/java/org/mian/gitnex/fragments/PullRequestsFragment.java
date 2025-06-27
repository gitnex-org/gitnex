package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreatePullRequestActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.PullRequestsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentPullRequestsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class PullRequestsFragment extends Fragment {

	public static boolean resumePullRequests = false;
	private FragmentPullRequestsBinding fragmentPullRequestsBinding;
	private List<PullRequest> prList;
	private PullRequestsAdapter adapter;
	private Context context;
	private int pageSize = Constants.prPageInit;
	private int resultLimit;
	private RepositoryContext repository;

	public static PullRequestsFragment newInstance(RepositoryContext repository) {
		PullRequestsFragment f = new PullRequestsFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		fragmentPullRequestsBinding =
				FragmentPullRequestsBinding.inflate(inflater, container, false);
		context = getContext();

		final SwipeRefreshLayout swipeRefresh = fragmentPullRequestsBinding.pullToRefresh;

		resultLimit = Constants.getCurrentResultLimit(context);
		prList = new ArrayList<>();
		repository = RepositoryContext.fromBundle(requireArguments());

		boolean archived = repository.getRepository().isArchived();

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											swipeRefresh.setRefreshing(false);
											loadInitial(
													repository.getOwner(),
													repository.getName(),
													pageSize,
													repository.getPrState().toString(),
													resultLimit);
											adapter.notifyDataChanged();
										},
										200));

		adapter = new PullRequestsAdapter(context, prList);
		adapter.setLoadMoreListener(
				() ->
						fragmentPullRequestsBinding.recyclerView.post(
								() -> {
									if (prList.size() == resultLimit || pageSize == resultLimit) {
										int page = (prList.size() + resultLimit) / resultLimit;
										loadMore(
												repository.getOwner(),
												repository.getName(),
												page,
												repository.getPrState().toString(),
												resultLimit);
									}
								}));

		fragmentPullRequestsBinding.recyclerView.setHasFixedSize(true);
		fragmentPullRequestsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentPullRequestsBinding.recyclerView.setAdapter(adapter);

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerPr(
						prState -> {
							prList.clear();
							adapter = new PullRequestsAdapter(context, prList);
							adapter.setLoadMoreListener(
									() ->
											fragmentPullRequestsBinding.recyclerView.post(
													() -> {
														if (prList.size() == resultLimit
																|| pageSize == resultLimit) {
															int page =
																	(prList.size() + resultLimit)
																			/ resultLimit;
															loadMore(
																	repository.getOwner(),
																	repository.getName(),
																	page,
																	repository
																			.getPrState()
																			.toString(),
																	resultLimit);
														}
													}));
							fragmentPullRequestsBinding.progressBar.setVisibility(View.VISIBLE);
							fragmentPullRequestsBinding.noData.setVisibility(View.GONE);
							loadInitial(
									repository.getOwner(),
									repository.getName(),
									pageSize,
									prState,
									resultLimit);
							fragmentPullRequestsBinding.recyclerView.setAdapter(adapter);
						});

		loadInitial(
				repository.getOwner(),
				repository.getName(),
				pageSize,
				repository.getPrState().toString(),
				resultLimit);

		if (archived) {
			fragmentPullRequestsBinding.createPullRequest.setVisibility(View.GONE);
		}

		if (repository.getRepository().isHasPullRequests() && !archived) {
			fragmentPullRequestsBinding.createPullRequest.setVisibility(View.VISIBLE);
			fragmentPullRequestsBinding.createPullRequest.setOnClickListener(
					v ->
							((RepoDetailActivity) requireActivity())
									.createPrLauncher.launch(
											repository.getIntent(
													context, CreatePullRequestActivity.class)));
		} else {
			fragmentPullRequestsBinding.createPullRequest.setVisibility(View.GONE);
		}

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.filter_menu_pr, menu);

								if (repository.getPrState().toString().equals("closed")) {
									menu.getItem(1).setIcon(R.drawable.ic_filter_closed);
								} else {
									menu.getItem(1).setIcon(R.drawable.ic_filter);
								}

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

		return fragmentPullRequestsBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (resumePullRequests) {
			loadInitial(
					repository.getOwner(),
					repository.getName(),
					pageSize,
					repository.getPrState().toString(),
					resultLimit);
			resumePullRequests = false;
		}
	}

	private void loadInitial(
			String repoOwner, String repoName, int page, String prState, int resultLimit) {
		Call<List<PullRequest>> call =
				RetrofitClient.getApiInterface(context)
						.repoListPullRequests(
								repoOwner,
								repoName,
								null,
								prState,
								null,
								null,
								null,
								null,
								page,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<PullRequest>> call,
							@NonNull Response<List<PullRequest>> response) {
						if (response.code() == 200) {
							assert response.body() != null;
							if (!response.body().isEmpty()) {
								prList.clear();
								prList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentPullRequestsBinding.noData.setVisibility(View.GONE);
							} else {
								prList.clear();
								adapter.notifyDataChanged();
								fragmentPullRequestsBinding.noData.setVisibility(View.VISIBLE);
							}
							fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
						} else if (response.code() == 404) {
							fragmentPullRequestsBinding.noData.setVisibility(View.VISIBLE);
							fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<PullRequest>> call, @NonNull Throwable t) {}
				});
	}

	private void loadMore(
			String repoOwner, String repoName, int page, String prState, int resultLimit) {
		fragmentPullRequestsBinding.progressBar.setVisibility(View.VISIBLE);
		Call<List<PullRequest>> call =
				RetrofitClient.getApiInterface(context)
						.repoListPullRequests(
								repoOwner,
								repoName,
								null,
								prState,
								null,
								null,
								null,
								null,
								page,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<PullRequest>> call,
							@NonNull Response<List<PullRequest>> response) {
						if (response.code() == 200) {
							prList.remove(prList.size() - 1);
							List<PullRequest> result = response.body();
							assert result != null;
							if (!result.isEmpty()) {
								pageSize = result.size();
								prList.addAll(result);
							} else {
								Toasty.info(context, getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							fragmentPullRequestsBinding.progressBar.setVisibility(View.GONE);
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<PullRequest>> call, @NonNull Throwable t) {}
				});
	}

	private void filter(String text) {
		List<PullRequest> arr = new ArrayList<>();
		for (PullRequest d : prList) {
			if (d == null || d.getTitle() == null || d.getBody() == null) {
				continue;
			}
			if (d.getTitle().toLowerCase().contains(text)
					|| d.getBody().toLowerCase().contains(text)
					|| String.valueOf(d.getNumber()).startsWith(text)) {
				arr.add(d);
			}
		}
		adapter.updateList(arr);
	}
}
