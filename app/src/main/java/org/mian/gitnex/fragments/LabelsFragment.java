package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author M M Arif
 */
public class LabelsFragment extends Fragment {

	private FragmentLabelsBinding fragmentLabelsBinding;
	private LabelsViewModel labelsViewModel;
	private LabelsAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private RepositoryContext repository;

	public LabelsFragment() {}

	public static LabelsFragment newInstance(RepositoryContext repository) {

		LabelsFragment fragment = new LabelsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentLabelsBinding = FragmentLabelsBinding.inflate(inflater, container, false);

		boolean canPush = repository.getPermissions().isPush();
		boolean archived = repository.getRepository().isArchived();

		final SwipeRefreshLayout swipeRefresh = fragmentLabelsBinding.pullToRefresh;

		labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		fragmentLabelsBinding.recyclerView.setHasFixedSize(true);
		fragmentLabelsBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											swipeRefresh.setRefreshing(false);
											LabelsViewModel.loadLabelsList(
													repository.getOwner(),
													repository.getName(),
													"repo",
													requireContext(),
													null,
													page,
													resultLimit);
										},
										200));

		fetchDataAsync(repository.getOwner(), repository.getName());

		if (!canPush || archived) {
			fragmentLabelsBinding.createLabel.setVisibility(View.GONE);
		}

		fragmentLabelsBinding.createLabel.setOnClickListener(
				v112 -> {
					startActivity(
							repository.getIntent(requireContext(), CreateLabelActivity.class));
				});

		return fragmentLabelsBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();

		if (CreateLabelActivity.refreshLabels) {

			page = 1;
			LabelsViewModel.loadLabelsList(
					repository.getOwner(),
					repository.getName(),
					"repo",
					requireContext(),
					null,
					page,
					resultLimit);
			CreateLabelActivity.refreshLabels = false;
		}
	}

	private void fetchDataAsync(String owner, String repo) {

		labelsViewModel
				.getLabelsList(
						owner,
						repo,
						"repo",
						requireContext(),
						fragmentLabelsBinding,
						page,
						resultLimit)
				.observe(
						getViewLifecycleOwner(),
						mainList -> {
							adapter = new LabelsAdapter(requireContext(), mainList, "repo", owner);
							adapter.setLoadMoreListener(
									new LabelsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											labelsViewModel.loadMore(
													owner,
													repo,
													"repo",
													requireContext(),
													fragmentLabelsBinding,
													page,
													resultLimit,
													adapter);
											fragmentLabelsBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentLabelsBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								fragmentLabelsBinding.recyclerView.setAdapter(adapter);
								fragmentLabelsBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentLabelsBinding.recyclerView.setAdapter(adapter);
								fragmentLabelsBinding.noData.setVisibility(View.VISIBLE);
							}

							fragmentLabelsBinding.progressBar.setVisibility(View.GONE);
						});
	}
}
