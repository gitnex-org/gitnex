package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.LabelsViewModel;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationLabelsFragment extends Fragment
		implements OrganizationDetailActivity.OrgActionInterface {

	private FragmentLabelsBinding binding;
	private LabelsViewModel viewModel;
	private OrganizationsViewModel orgViewModel;
	private LabelsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private String orgName;
	private final String type = "org";
	private int resultLimit;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	public static OrganizationLabelsFragment newInstance(String orgName) {
		OrganizationLabelsFragment fragment = new OrganizationLabelsFragment();
		Bundle args = new Bundle();
		args.putString("orgName", orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) orgName = getArguments().getString("orgName");
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentLabelsBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);
		orgViewModel = new ViewModelProvider(requireActivity()).get(OrganizationsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && (isFirstLoad)) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) lazyLoad();
	}

	private void lazyLoad() {
		isFirstLoad = false;
		if (viewModel.getLabels().getValue() == null
				|| viewModel.getLabels().getValue().isEmpty()) {
			refreshData();
		}
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (!isSearching) {
							viewModel.fetchLabels(
									requireContext(),
									orgName,
									null,
									type,
									page,
									resultLimit,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getLabels()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter == null) {
								OrganizationPermissions perms =
										orgViewModel.getPermissions().getValue();
								boolean canEdit =
										perms != null && (perms.isIsOwner() || perms.isIsAdmin());

								adapter =
										new LabelsAdapter(
												requireContext(),
												list,
												canEdit,
												label -> {
													BottomSheetCreateLabelFragment.newInstance(
																	type, orgName, null, label)
															.show(
																	getChildFragmentManager(),
																	"EditLabel");
												},
												label -> {
													new MaterialAlertDialogBuilder(requireContext())
															.setTitle(R.string.labelDeleteTitle)
															.setMessage(
																	getString(
																			R.string
																					.labelDeleteConfirmText,
																			label.getName()))
															.setPositiveButton(
																	R.string.menuDeleteText,
																	(d, w) -> {
																		viewModel.deleteLabel(
																				requireContext(),
																				type,
																				orgName,
																				null,
																				label.getId());
																	})
															.setNegativeButton(
																	R.string.cancelButton, null)
															.show();
												});
								binding.recyclerView.setAdapter(adapter);
								binding.searchResultsRecycler.setAdapter(adapter);
							} else {
								adapter.updateList(list);
							}

							binding.pullToRefresh.setRefreshing(false);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == 200 || code == 201 || code == 204) {
								int messageRes;
								if (code == 201) {
									messageRes = R.string.labelCreated;
								} else if (code == 200) {
									messageRes = R.string.labelUpdated;
								} else {
									messageRes = R.string.labelDeleteText;
								}
								Toasty.show(requireContext(), messageRes);

								refreshData();
								new Handler(Looper.getMainLooper())
										.postDelayed(
												() -> {
													if (isAdded()) {
														viewModel.resetActionResult();
													}
												},
												100);
							}
						});

		orgViewModel
				.getPermissions()
				.observe(
						getViewLifecycleOwner(),
						perms -> {
							if (adapter != null && perms != null) {
								boolean canEdit = perms.isIsOwner() || perms.isIsAdmin();
								adapter.setCanEdit(canEdit);
							}
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);
		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getHasLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			binding.layoutEmpty
					.getRoot()
					.setVisibility(!hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		}

		boolean showEmpty = !isLoading && !hasData && hasLoadedOnce;
		binding.pullToRefresh.setVisibility(showEmpty ? View.GONE : View.VISIBLE);
	}

	private void setupSearch() {
		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								String query = s.toString();
								isSearching = !query.isEmpty();
								if (adapter != null) {
									adapter.getFilter()
											.filter(
													query,
													count1 -> {
														if (isSearching
																&& adapter.getItemCount() == 0) {
															binding.layoutEmpty
																	.getRoot()
																	.setVisibility(View.VISIBLE);
														} else {
															updateUiVisibility(
																	Boolean.TRUE.equals(
																			viewModel
																					.getIsLoading()
																					.getValue()));
														}
													});
								}
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						isSearching = false;
						if (adapter != null) adapter.getFilter().filter("");
						updateUiVisibility(false);
					}
				});
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchLabels(requireContext(), orgName, null, type, 1, resultLimit, true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onSearchTriggered() {
		binding.searchView.show();
	}

	@Override
	public void onAddRequested() {
		BottomSheetCreateLabelFragment sheet =
				BottomSheetCreateLabelFragment.newInstance("org", orgName, "", null);
		sheet.show(getChildFragmentManager(), "CreateLabelSheet");
	}

	@Override
	public boolean canAdd() {
		OrganizationPermissions perms = orgViewModel.getPermissions().getValue();
		return perms != null && (perms.isIsOwner() || perms.isIsAdmin());
	}
}
