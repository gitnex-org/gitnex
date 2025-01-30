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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.DashboardAdapter;
import org.mian.gitnex.databinding.FragmentDashboardBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.DashboardViewModel;

/**
 * @author M M Arif
 */
public class DashboardFragment extends Fragment {

	protected TinyDB tinyDB;
	private DashboardViewModel dashboardViewModel;
	private FragmentDashboardBinding binding;
	private DashboardAdapter adapter;
	private List<Activity> activityList;
	private int page = 1;
	private String username;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentDashboardBinding.inflate(inflater, container, false);

		Context ctx = getContext();

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.dashboard));

		activityList = new ArrayList<>();

		dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

		username = ((BaseActivity) requireActivity()).getAccount().getAccount().getUserName();

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new DashboardAdapter(activityList, ctx);

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

		dashboardViewModel
				.getActivitiesList(username, getContext(), binding)
				.observe(
						getViewLifecycleOwner(),
						activityListMain -> {
							adapter = new DashboardAdapter(activityListMain, getContext());
							adapter.setLoadMoreListener(
									new DashboardAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											dashboardViewModel.loadMoreActivities(
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
