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
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateIssueActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class IssuesFragment extends Fragment {

	public static boolean resumeIssues = false;
	private final String requestType = Constants.issuesRequestType;
	private FragmentIssuesBinding fragmentIssuesBinding;
	private Context context;
	private List<Issue> issuesList;
	private List<Issue> pinnedIssuesList;
	private IssuesAdapter adapter;
	private IssuesAdapter adapterPinned;
	private int pageSize = Constants.issuesPageInit;
	private int resultLimit;
	private RepositoryContext repository;
	private String selectedLabels = null;
	private String mentionedBy;

	public static IssuesFragment newInstance(RepositoryContext repository) {
		IssuesFragment f = new IssuesFragment();
		f.setArguments(repository.getBundle());
		return f;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);
		context = getContext();

		repository = RepositoryContext.fromBundle(requireArguments());
		mentionedBy = repository.getMentionedBy();

		boolean archived = repository.getRepository().isArchived();

		resultLimit = Constants.getCurrentResultLimit(context);

		issuesList = new ArrayList<>();
		pinnedIssuesList = new ArrayList<>();

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											fragmentIssuesBinding.pullToRefresh.setRefreshing(
													false);
											loadInitial(
													repository.getOwner(),
													repository.getName(),
													resultLimit,
													requestType,
													repository.getIssueState().toString(),
													repository.getIssueMilestoneFilterName(),
													null,
													selectedLabels,
													mentionedBy);
											adapter.notifyDataChanged();
										},
										200));

		adapter = new IssuesAdapter(context, issuesList, "");
		adapter.setLoadMoreListener(
				() ->
						fragmentIssuesBinding.recyclerView.post(
								() -> {
									if (issuesList.size() == resultLimit
											|| pageSize == resultLimit) {
										int page = (issuesList.size() + resultLimit) / resultLimit;
										loadMore(
												repository.getOwner(),
												repository.getName(),
												page,
												resultLimit,
												requestType,
												repository.getIssueState().toString(),
												repository.getIssueMilestoneFilterName(),
												selectedLabels,
												mentionedBy);
									}
								}));

		fragmentIssuesBinding.recyclerView.setHasFixedSize(true);
		fragmentIssuesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentIssuesBinding.recyclerView.setAdapter(adapter);

		adapterPinned = new IssuesAdapter(context, pinnedIssuesList, "pinned");
		LinearLayoutManager horizontalLayoutManager =
				new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
		fragmentIssuesBinding.rvPinnedIssues.setLayoutManager(horizontalLayoutManager);
		fragmentIssuesBinding.rvPinnedIssues.setAdapter(adapterPinned);

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListener(
						issueState -> refreshIssues(issueState, selectedLabels, mentionedBy));

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerFilterIssuesByMilestone(
						filterIssueByMilestone ->
								refreshIssues(
										repository.getIssueState().toString(),
										selectedLabels,
										mentionedBy,
										filterIssueByMilestone));

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerFilterIssuesByLabels(
						filterLabels -> {
							selectedLabels = filterLabels;
							refreshIssues(
									repository.getIssueState().toString(),
									selectedLabels,
									mentionedBy);
						});

		((RepoDetailActivity) requireActivity())
				.setFragmentRefreshListenerFilterIssuesByMentions(
						username -> {
							mentionedBy = username;
							repository.setMentionedBy(username);
							refreshIssues(
									repository.getIssueState().toString(),
									selectedLabels,
									mentionedBy);
						});

		loadInitial(
				repository.getOwner(),
				repository.getName(),
				resultLimit,
				requestType,
				repository.getIssueState().toString(),
				repository.getIssueMilestoneFilterName(),
				null,
				selectedLabels,
				mentionedBy);

		getPinnedIssues(repository.getOwner(), repository.getName());

		if (archived) {
			fragmentIssuesBinding.createNewIssue.setVisibility(View.GONE);
		}

		if (repository.getRepository().isHasIssues() && !archived) {
			fragmentIssuesBinding.createNewIssue.setVisibility(View.VISIBLE);
			fragmentIssuesBinding.createNewIssue.setOnClickListener(
					v12 -> {
						((RepoDetailActivity) requireActivity())
								.createIssueLauncher.launch(
										repository.getIntent(
												getContext(), CreateIssueActivity.class));
					});
		} else {
			fragmentIssuesBinding.createNewIssue.setVisibility(View.GONE);
		}

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.filter_menu, menu);

								if (repository.getIssueState().toString().equals("closed")) {
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
												loadInitial(
														repository.getOwner(),
														repository.getName(),
														resultLimit,
														requestType,
														repository.getIssueState().toString(),
														repository.getIssueMilestoneFilterName(),
														query,
														selectedLabels,
														mentionedBy);
												searchView.setQuery(null, false);
												searchItem.collapseActionView();
												return false;
											}

											@Override
											public boolean onQueryTextChange(String newText) {
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

		return fragmentIssuesBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (resumeIssues) {
			loadInitial(
					repository.getOwner(),
					repository.getName(),
					resultLimit,
					requestType,
					repository.getIssueState().toString(),
					repository.getIssueMilestoneFilterName(),
					"",
					selectedLabels,
					mentionedBy);
			getPinnedIssues(repository.getOwner(), repository.getName());
			resumeIssues = false;
		}
	}

	private void refreshIssues(String issueState, String labels, String mentionedBy) {
		issuesList.clear();
		adapter = new IssuesAdapter(context, issuesList, "");
		adapter.setLoadMoreListener(
				() ->
						fragmentIssuesBinding.recyclerView.post(
								() -> {
									if (issuesList.size() == resultLimit
											|| pageSize == resultLimit) {
										int page = (issuesList.size() + resultLimit) / resultLimit;
										loadMore(
												repository.getOwner(),
												repository.getName(),
												page,
												resultLimit,
												requestType,
												issueState,
												repository.getIssueMilestoneFilterName(),
												labels,
												mentionedBy);
									}
								}));
		fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
		fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
		loadInitial(
				repository.getOwner(),
				repository.getName(),
				resultLimit,
				requestType,
				issueState,
				repository.getIssueMilestoneFilterName(),
				null,
				labels,
				mentionedBy);
	}

	private void refreshIssues(
			String issueState, String labels, String mentionedBy, String filterByMilestone) {
		issuesList.clear();
		adapter = new IssuesAdapter(context, issuesList, "");
		adapter.setLoadMoreListener(
				() ->
						fragmentIssuesBinding.recyclerView.post(
								() -> {
									if (issuesList.size() == resultLimit
											|| pageSize == resultLimit) {
										int page = (issuesList.size() + resultLimit) / resultLimit;
										loadMore(
												repository.getOwner(),
												repository.getName(),
												page,
												resultLimit,
												requestType,
												issueState,
												filterByMilestone,
												labels,
												mentionedBy);
									}
								}));
		fragmentIssuesBinding.recyclerView.setAdapter(adapter);
		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
		fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
		loadInitial(
				repository.getOwner(),
				repository.getName(),
				resultLimit,
				requestType,
				issueState,
				filterByMilestone,
				null,
				labels,
				mentionedBy);
	}

	private void getPinnedIssues(String repoOwner, String repoName) {
		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(context).repoListPinnedIssues(repoOwner, repoName);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						if (response.code() == 200) {
							assert response.body() != null;
							if (!response.body().isEmpty()) {
								fragmentIssuesBinding.pinnedIssuesFrame.setVisibility(View.VISIBLE);
								pinnedIssuesList.clear();
								pinnedIssuesList.addAll(response.body());
								adapterPinned.notifyDataChanged();
							} else {
								pinnedIssuesList.clear();
								adapterPinned.notifyDataChanged();
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadInitial(
			String repoOwner,
			String repoName,
			int resultLimit,
			String requestType,
			String issueState,
			String filterByMilestone,
			String query,
			String labels,
			String mentionedBy) {

		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(context)
						.issueListIssues(
								repoOwner,
								repoName,
								issueState,
								labels,
								query,
								requestType,
								filterByMilestone,
								null, // since
								null, // before
								null, // created_by
								null, // assigned_by
								mentionedBy, // mentioned_by
								1,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						if (response.code() == 200) {
							assert response.body() != null;
							if (!response.body().isEmpty()) {
								issuesList.clear();
								issuesList.addAll(response.body());
								adapter.notifyDataChanged();
								adapterPinned.notifyDataChanged();
								fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
							} else {
								issuesList.clear();
								adapter.notifyDataChanged();
								adapterPinned.notifyDataChanged();
								fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
							}
							fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
						} else if (response.code() == 404) {
							fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
							fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
						} else {
							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadMore(
			String repoOwner,
			String repoName,
			int page,
			int resultLimit,
			String requestType,
			String issueState,
			String filterByMilestone,
			String labels,
			String mentionedBy) {

		fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(context)
						.issueListIssues(
								repoOwner,
								repoName,
								issueState,
								labels,
								null,
								requestType,
								filterByMilestone,
								null, // since
								null, // before
								null, // created_by
								null, // assigned_by
								mentionedBy, // mentioned_by
								page,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						if (response.code() == 200) {
							List<Issue> result = response.body();
							assert result != null;
							if (!result.isEmpty()) {
								pageSize = result.size();
								issuesList.addAll(result);
							} else {
								SnackBar.info(
										context,
										fragmentIssuesBinding.getRoot(),
										getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
								adapterPinned.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							adapterPinned.notifyDataChanged();
							fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
						} else {
							Toasty.error(context, getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						Toasty.error(context, getString(R.string.genericServerResponseError));
					}
				});
	}
}
