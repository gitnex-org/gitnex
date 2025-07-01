package org.mian.gitnex.fragments;

import android.content.Context;
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
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Activity;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ActivitiesAdapter;
import org.mian.gitnex.databinding.FragmentActivitiesBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.ActivitiesViewModel;

/**
 * @author M M Arif
 */
public class ActivitiesFragment extends Fragment {

	protected TinyDB tinyDB;
	private ActivitiesViewModel viewModel;
	private FragmentActivitiesBinding binding;
	private ActivitiesAdapter adapter;
	private List<Activity> activityList;
	private int page = 1;
	private String username;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentActivitiesBinding.inflate(inflater, container, false);

		Context ctx = getContext();

		activityList = new ArrayList<>();

		viewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);

		username = ((BaseActivity) requireActivity()).getAccount().getAccount().getUserName();

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new ActivitiesAdapter(activityList, ctx);

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											activityList.clear();
											binding.pullToRefresh.setRefreshing(false);
											binding.progressBar.setVisibility(View.VISIBLE);
											fetchDataAsync(username);
										},
										250));

		fetchDataAsync(username);

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		// fetchDataAsync(username);
	}

	private void fetchDataAsync(String username) {

		viewModel
				.getActivitiesList(username, getContext(), binding)
				.observe(
						getViewLifecycleOwner(),
						activityListMain -> {
							adapter = new ActivitiesAdapter(activityListMain, getContext());
							adapter.setLoadMoreListener(
									new ActivitiesAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											viewModel.loadMoreActivities(
													username, page, getContext(), adapter, binding);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								binding.recyclerView.setAdapter(adapter);
								binding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.noData.setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}
}
