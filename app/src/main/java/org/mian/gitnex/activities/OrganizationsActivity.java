package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.ActivityOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationsActivity extends BaseActivity {

	public static boolean orgCreated = false;
	private OrganizationsViewModel organizationsViewModel;
	private ActivityOrganizationsBinding binding;
	private OrganizationsListAdapter adapter;
	private int page = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityOrganizationsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		organizationsViewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(this);

		setupUI();
		setupSearch();
		fetchDataAsync();
	}

	private void setupUI() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnNewOrg.setOnClickListener(
				v -> {
					startActivity(new Intent(this, CreateOrganizationActivity.class));
				});

		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
		binding.recyclerView.setClipToPadding(false);

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											binding.pullToRefresh.setRefreshing(false);
											fetchDataAsync();
											binding.expressiveLoader.setVisibility(View.VISIBLE);
										},
										50));
	}

	private void setupSearch() {
		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								if (adapter != null) {
									adapter.getFilter().filter(s.toString().trim());
								}
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
	}

	private void fetchDataAsync() {
		organizationsViewModel
				.getUserOrg(page, resultLimit, this)
				.observe(
						this,
						orgListMain -> {
							adapter = new OrganizationsListAdapter(this, orgListMain);

							adapter.setLoadMoreListener(
									new OrganizationsListAdapter.OnLoadMoreListener() {
										@Override
										public void onLoadMore() {
											page += 1;
											organizationsViewModel.loadMoreOrgList(
													page,
													resultLimit,
													OrganizationsActivity.this,
													adapter);
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

	@Override
	public void onResume() {
		super.onResume();
		if (orgCreated) {
			organizationsViewModel.loadOrgList(page, resultLimit, this);
			orgCreated = false;
		}
	}
}
