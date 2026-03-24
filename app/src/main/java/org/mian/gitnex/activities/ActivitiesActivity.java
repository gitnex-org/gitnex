package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.adapters.ActivitiesAdapter;
import org.mian.gitnex.databinding.ActivityActivitiesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.ActivitiesViewModel;

/**
 * @author mmarif
 */
public class ActivitiesActivity extends BaseActivity {

	private ActivitiesViewModel viewModel;
	private ActivityActivitiesBinding binding;
	private ActivitiesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private String username;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityActivitiesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		viewModel = new ViewModelProvider(this).get(ActivitiesViewModel.class);
		int resultLimit = Constants.getCurrentResultLimit(this);
		viewModel.setResultLimit(resultLimit);

		if (getAccount() != null && getAccount().getAccount() != null) {
			username = getAccount().getAccount().getUserName();
		}

		setupUI();
		observeViewModel();
		refreshData();
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());

		adapter = new ActivitiesAdapter(new ArrayList<>(), this);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchActivities(
								ActivitiesActivity.this, username, page + 1, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getActivities()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							updateEmptyState(list.isEmpty());
							binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getIsLoading()
				.observe(
						this,
						loading -> {
							if (loading) {
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
								if (adapter.getItemCount() == 0) {
									binding.expressiveLoader.setVisibility(View.VISIBLE);
								}
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
							}
						});

		viewModel
				.getError()
				.observe(
						this,
						msg -> {
							Toasty.show(this, msg);
							binding.pullToRefresh.setRefreshing(false);
							binding.expressiveLoader.setVisibility(View.GONE);
						});
	}

	private void updateEmptyState(boolean isEmpty) {
		boolean loading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
		if (!loading) {
			binding.layoutEmpty.getRoot().setVisibility(isEmpty ? View.VISIBLE : View.GONE);
		}
	}

	private void refreshData() {
		scrollListener.resetState();
		binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		viewModel.fetchActivities(this, username, 1, true);
	}
}
