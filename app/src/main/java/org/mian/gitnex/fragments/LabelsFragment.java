package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author M M Arif
 */
public class LabelsFragment extends Fragment {

	private ProgressBar mProgressBar;
	private RecyclerView mRecyclerView;
	private LabelsAdapter adapter;
	private TextView noData;

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

		FragmentLabelsBinding fragmentLabelsBinding =
				FragmentLabelsBinding.inflate(inflater, container, false);

		boolean canPush = repository.getPermissions().isPush();
		boolean archived = repository.getRepository().isArchived();

		final SwipeRefreshLayout swipeRefresh = fragmentLabelsBinding.pullToRefresh;
		noData = fragmentLabelsBinding.noData;

		mRecyclerView = fragmentLabelsBinding.recyclerView;
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		mProgressBar = fragmentLabelsBinding.progressBar;

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											swipeRefresh.setRefreshing(false);
											LabelsViewModel.loadLabelsList(
													repository.getOwner(),
													repository.getName(),
													getContext());
										},
										200));

		fetchDataAsync(repository.getOwner(), repository.getName());

		if (!canPush || archived) {
			fragmentLabelsBinding.createLabel.setVisibility(View.GONE);
		}

		fragmentLabelsBinding.createLabel.setOnClickListener(
				v112 -> {
					startActivity(repository.getIntent(getContext(), CreateLabelActivity.class));
				});

		return fragmentLabelsBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();

		if (CreateLabelActivity.refreshLabels) {

			LabelsViewModel.loadLabelsList(
					repository.getOwner(), repository.getName(), getContext());
			CreateLabelActivity.refreshLabels = false;
		}
	}

	private void fetchDataAsync(String owner, String repo) {

		LabelsViewModel labelsModel = new ViewModelProvider(this).get(LabelsViewModel.class);

		labelsModel
				.getLabelsList(owner, repo, getContext())
				.observe(
						getViewLifecycleOwner(),
						labelsListMain -> {
							adapter =
									new LabelsAdapter(getContext(), labelsListMain, "repo", owner);

							if (adapter.getItemCount() > 0) {

								mRecyclerView.setAdapter(adapter);
								noData.setVisibility(View.GONE);
							} else {

								adapter.notifyDataChanged();
								mRecyclerView.setAdapter(adapter);
								noData.setVisibility(View.VISIBLE);
							}

							mProgressBar.setVisibility(View.GONE);
						});
	}
}
