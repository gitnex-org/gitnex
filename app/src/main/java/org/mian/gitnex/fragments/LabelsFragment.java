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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author mmarif
 */
public class LabelsFragment extends Fragment {

	private FragmentLabelsBinding binding;
	private LabelsViewModel viewModel;
	private LabelsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;

	private RepositoryContext repository;
	private final String type = "repo";
	private int resultLimit;
	private boolean isSearching = false;

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

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentLabelsBinding.inflate(inflater, container, false);

		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(LabelsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		setupSearch();
		observeViewModel();

		refreshData();

		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
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
									repository.getOwner(),
									repository.getName(),
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
								boolean canEdit =
										repository.getPermissions().isPush()
												&& !repository.getRepository().isArchived();

								adapter =
										new LabelsAdapter(
												requireContext(),
												list,
												canEdit,
												label -> {
													BottomSheetCreateLabelFragment.newInstance(
																	type,
																	repository.getOwner(),
																	repository.getName(),
																	label)
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
																				repository
																						.getOwner(),
																				repository
																						.getName(),
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

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
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
									adapter.getFilter().filter(query);
								}
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.fetchLabels(
				requireContext(),
				repository.getOwner(),
				repository.getName(),
				type,
				1,
				resultLimit,
				true);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					refreshData();
				});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
