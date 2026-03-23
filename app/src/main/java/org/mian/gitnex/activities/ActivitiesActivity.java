package org.mian.gitnex.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Activity;
import org.mian.gitnex.adapters.ActivitiesAdapter;
import org.mian.gitnex.databinding.ActivityActivitiesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.ActivitiesViewModel;

/**
 * @author mmarif
 */
public class ActivitiesActivity extends BaseActivity {

	private ActivitiesViewModel viewModel;
	private ActivityActivitiesBinding binding;
	private ActivitiesAdapter adapter;
	private List<Activity> activityList;
	private int page = 1;
	private String username;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityActivitiesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		resultLimit = Constants.getCurrentResultLimit(this);
		activityList = new ArrayList<>();

		viewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);
		viewModel.setResultLimit(resultLimit);

		if (getAccount() != null && getAccount().getAccount() != null) {
			username = getAccount().getAccount().getUserName();
		}

		setupUI();
		fetchDataAsync(username);
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											if (activityList != null) activityList.clear();
											binding.pullToRefresh.setRefreshing(false);
											binding.expressiveLoader.setVisibility(View.VISIBLE);
											fetchDataAsync(username);
										},
										250));
	}

	private void fetchDataAsync(String username) {
		viewModel
				.getActivitiesList(username, this, null)
				.observe(
						this,
						activityListMain -> {
							adapter = new ActivitiesAdapter(activityListMain, this);
							adapter.setLoadMoreListener(
									new ActivitiesAdapter.OnLoadMoreListener() {
										@Override
										public void onLoadMore() {
											page += 1;
											viewModel.loadMoreActivities(
													username,
													page,
													ActivitiesActivity.this,
													adapter,
													null);
											binding.expressiveLoader.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {
											binding.expressiveLoader.setVisibility(View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								binding.recyclerView.setAdapter(adapter);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}

							binding.expressiveLoader.setVisibility(View.GONE);
						});
	}
}
