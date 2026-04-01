package org.mian.gitnex.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.ActivityOrganizationsBinding;
import org.mian.gitnex.fragments.BottomSheetCreateOrganization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationsActivity extends BaseActivity {

	private ActivityOrganizationsBinding binding;
	private OrganizationsViewModel viewModel;
	private OrganizationsListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityOrganizationsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(this);

		setupToolbar();
		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();
		setupSearch();

		refreshData();
	}

	private void setupToolbar() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.btnNewOrg.setOnClickListener(
				v ->
						BottomSheetCreateOrganization.newInstance()
								.show(getSupportFragmentManager(), "create_org"));
		binding.btnSearch.setOnClickListener(v -> binding.searchView.show());
	}

	private void setupRecyclerView() {
		adapter = new OrganizationsListAdapter(this, new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (binding.searchView.isShowing()) return;
						viewModel.fetchOrganizations(
								OrganizationsActivity.this, page + 1, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getOrgs()
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
		viewModel.fetchOrganizations(this, 1, resultLimit, true);
	}

	private void setupSearch() {

		OrganizationsListAdapter searchAdapter =
				new OrganizationsListAdapter(this, new ArrayList<>());
		binding.searchResultsRecycler.setLayoutManager(new LinearLayoutManager(this));
		binding.searchResultsRecycler.setAdapter(searchAdapter);

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.SHOWN) {
						List<Organization> currentOrgs = viewModel.getOrgs().getValue();
						if (currentOrgs != null) {
							searchAdapter.updateList(new ArrayList<>(currentOrgs));
						}
					} else if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						binding.searchView.setText("");
						searchAdapter.updateList(new ArrayList<>());
						binding.recyclerView.scrollToPosition(0);
					}
				});

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString().trim();
								List<Organization> fullList = viewModel.getOrgs().getValue();

								if (fullList == null) return;

								if (query.isEmpty()) {
									searchAdapter.updateList(new ArrayList<>(fullList));
									return;
								}

								List<Organization> filtered = new ArrayList<>();
								for (Organization org : fullList) {
									String name =
											org.getUsername() != null
													? org.getUsername().toLowerCase()
													: "";
									String desc =
											org.getDescription() != null
													? org.getDescription().toLowerCase()
													: "";
									String filterPattern = query.toLowerCase();

									if (name.contains(filterPattern)
											|| desc.contains(filterPattern)) {
										filtered.add(org);
									}
								}
								searchAdapter.updateList(filtered);
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView
				.getEditText()
				.setOnEditorActionListener(
						(v, actionId, event) -> {
							binding.searchView.hide();
							return false;
						});
	}
}
