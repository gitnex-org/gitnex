package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.IssueMeta;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DependencyAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetIssueDependenciesBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class BottomSheetIssueDependenciesFragment extends BottomSheetDialogFragment {

	private BottomSheetIssueDependenciesBinding binding;
	private IssueContext issue;
	private DependencyAdapter dependenciesAdapter;
	private DependencyAdapter searchResultsAdapter;
	private List<Issue> dependenciesList;
	private List<Issue> searchResultsList;

	public static BottomSheetIssueDependenciesFragment newInstance(IssueContext issue) {

		BottomSheetIssueDependenciesFragment fragment = new BottomSheetIssueDependenciesFragment();
		Bundle args = new Bundle();
		args.putSerializable(IssueContext.INTENT_EXTRA, issue);
		fragment.setArguments(args);
		return fragment;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		binding = BottomSheetIssueDependenciesBinding.inflate(inflater, container, false);

		if (getArguments() != null) {
			issue = (IssueContext) getArguments().getSerializable(IssueContext.INTENT_EXTRA);
		}
		if (issue == null) {
			throw new IllegalStateException("IssueContext is required");
		}

		dependenciesList = new ArrayList<>();
		dependenciesAdapter = new DependencyAdapter(dependenciesList, true);
		binding.dependenciesRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.dependenciesRecyclerView.setAdapter(dependenciesAdapter);
		dependenciesAdapter.setOnItemClickListener(this::deleteDependency);

		searchResultsList = new ArrayList<>();
		searchResultsAdapter = new DependencyAdapter(searchResultsList, false);
		binding.searchResultsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.searchResultsRecyclerView.setAdapter(searchResultsAdapter);
		searchResultsAdapter.setOnItemClickListener(this::addDependency);

		binding.searchInputLayout.setEndIconOnClickListener(
				v -> {
					String query =
							Objects.requireNonNull(binding.searchInput.getText()).toString().trim();
					if (!query.isEmpty()) {
						searchIssues(query);
					} else {
						clearSearchResults();
					}
				});

		loadDependencies();

		return binding.getRoot();
	}

	private void loadDependencies() {
		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueListIssueDependencies(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								String.valueOf(issue.getIssueIndex()),
								1,
								10);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						if (response.isSuccessful() && response.body() != null) {
							List<Issue> newDependencies = response.body();
							int oldSize = dependenciesList.size();

							dependenciesList.clear();
							if (oldSize > 0) {
								dependenciesAdapter.notifyItemRangeRemoved(0, oldSize);
							}

							dependenciesList.addAll(newDependencies);
							if (!newDependencies.isEmpty()) {
								dependenciesAdapter.notifyItemRangeInserted(
										0, newDependencies.size());
							}

							if (dependenciesList.isEmpty()) {
								binding.dependenciesRecyclerView.setVisibility(View.GONE);
								binding.noDependenciesText.setVisibility(View.VISIBLE);
							} else {
								binding.dependenciesRecyclerView.setVisibility(View.VISIBLE);
								binding.noDependenciesText.setVisibility(View.GONE);
							}
						} else {
							int oldSize = dependenciesList.size();
							dependenciesList.clear();
							if (oldSize > 0) {
								dependenciesAdapter.notifyItemRangeRemoved(0, oldSize);
							}
							binding.dependenciesRecyclerView.setVisibility(View.GONE);
							binding.noDependenciesText.setVisibility(View.VISIBLE);
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						int oldSize = dependenciesList.size();
						dependenciesList.clear();
						if (oldSize > 0) {
							dependenciesAdapter.notifyItemRangeRemoved(0, oldSize);
						}
						binding.dependenciesRecyclerView.setVisibility(View.GONE);
						binding.noDependenciesText.setVisibility(View.VISIBLE);
					}
				});
	}

	private void deleteDependency(Issue dependency, int position) {
		IssueMeta meta = new IssueMeta();
		meta.setOwner(issue.getRepository().getOwner());
		meta.setRepo(issue.getRepository().getName());
		meta.setIndex(dependency.getId());

		Call<Void> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueRemoveIssueDependencies2(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								String.valueOf(issue.getIssue().getId()),
								meta);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Void> call, @NonNull Response<Void> response) {
						if (response.isSuccessful()) {
							dependenciesList.remove(position);
							dependenciesAdapter.notifyItemRemoved(position);
							dependenciesAdapter.notifyItemRangeChanged(
									position, dependenciesList.size());

							if (dependenciesList.isEmpty()) {
								binding.dependenciesRecyclerView.setVisibility(View.GONE);
								binding.noDependenciesText.setVisibility(View.VISIBLE);
							}
							Toasty.success(
									requireContext(), getString(R.string.dependency_removed));
						} else {
							Toasty.error(
									requireContext(),
									getString(R.string.dependency_removal_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
						Toasty.error(
								requireContext(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void searchIssues(String query) {
		Call<List<Issue>> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueListIssues(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								"open",
								null,
								query,
								null,
								null,
								null,
								null,
								null,
								null,
								null,
								1,
								3);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<List<Issue>> call,
							@NonNull Response<List<Issue>> response) {
						if (response.isSuccessful() && response.body() != null) {
							int oldSize = searchResultsList.size();
							searchResultsList.clear();
							if (oldSize > 0) {
								searchResultsAdapter.notifyItemRangeRemoved(0, oldSize);
							}

							List<Issue> results = response.body();
							long currentIssueId = issue.getIssue().getId();
							List<Long> dependencyIds = new ArrayList<>();
							for (Issue dep : dependenciesList) {
								dependencyIds.add(dep.getId());
							}

							for (Issue result : results) {
								if (result.getId() != currentIssueId
										&& !dependencyIds.contains(result.getId())) {
									searchResultsList.add(result);
								}
							}

							if (searchResultsList.isEmpty()) {
								binding.searchResultsRecyclerView.setVisibility(View.GONE);
								Toasty.info(
										requireContext(),
										getString(R.string.no_dependency_search_results));
							} else {
								searchResultsAdapter.notifyItemRangeInserted(
										0, searchResultsList.size());
								binding.searchResultsRecyclerView.setVisibility(View.VISIBLE);
							}
						} else {
							int oldSize = searchResultsList.size();
							if (oldSize > 0) {
								searchResultsList.clear();
								searchResultsAdapter.notifyItemRangeRemoved(0, oldSize);
							}
							binding.searchResultsRecyclerView.setVisibility(View.GONE);
							Toasty.error(requireContext(), getString(R.string.search_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<List<Issue>> call, @NonNull Throwable t) {
						int oldSize = searchResultsList.size();
						if (oldSize > 0) {
							searchResultsList.clear();
							searchResultsAdapter.notifyItemRangeRemoved(0, oldSize);
						}
						binding.searchResultsRecyclerView.setVisibility(View.GONE);
						Toasty.error(
								requireContext(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void addDependency(Issue dependency, int position) {
		IssueMeta meta = new IssueMeta();
		meta.setOwner(issue.getRepository().getOwner());
		meta.setRepo(issue.getRepository().getName());
		meta.setIndex(dependency.getId());

		Call<Issue> call =
				RetrofitClient.getApiInterface(requireContext())
						.issueCreateIssueDependencies(
								issue.getRepository().getOwner(),
								issue.getRepository().getName(),
								String.valueOf(issue.getIssue().getId()),
								meta);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<Issue> call, @NonNull Response<Issue> response) {
						if (response.isSuccessful()) {
							binding.searchInput.setText("");
							int oldSize = searchResultsList.size();
							if (oldSize > 0) {
								searchResultsList.clear();
								searchResultsAdapter.notifyItemRangeRemoved(0, oldSize);
							}
							binding.searchResultsRecyclerView.setVisibility(View.GONE);

							loadDependencies();
							Toasty.success(requireContext(), getString(R.string.dependency_added));
						} else {
							Toasty.error(
									requireContext(), getString(R.string.dependency_add_failed));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Issue> call, @NonNull Throwable t) {
						Toasty.error(
								requireContext(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void clearSearchResults() {
		int oldSize = searchResultsList.size();
		if (oldSize > 0) {
			searchResultsList.clear();
			searchResultsAdapter.notifyItemRangeRemoved(0, oldSize);
		}
		binding.searchResultsRecyclerView.setVisibility(View.GONE);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
