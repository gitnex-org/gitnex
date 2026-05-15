package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ExploreActivity;
import org.mian.gitnex.adapters.IssuesAdapter;
import org.mian.gitnex.databinding.BottomsheetExploreIssuesBinding;
import org.mian.gitnex.databinding.FragmentExploreIssuesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author mmarif
 */
public class ExploreIssuesFragment extends Fragment
		implements ExploreActivity.ExploreActionInterface {

	private IssuesViewModel issuesViewModel;
	private FragmentExploreIssuesBinding viewBinding;
	private IssuesAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private String currentQuery = "";
	private int resultLimit;
	private boolean isFirstLoad = true;
	private boolean isViewReady = false;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, viewBinding.recyclerView, viewBinding.pullToRefresh, null);
		isViewReady = true;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		viewBinding = FragmentExploreIssuesBinding.inflate(inflater, container, false);
		issuesViewModel = new ViewModelProvider(this).get(IssuesViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		return viewBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad && isViewReady) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		refreshData(currentQuery);
	}

	@Override
	public void onSearchTriggered() {
		showSearchBottomSheet();
	}

	private void setupRecyclerView() {
		adapter = new IssuesAdapter(requireContext(), new ArrayList<>(), "explore");
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		viewBinding.recyclerView.setLayoutManager(layoutManager);
		viewBinding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						issuesViewModel.fetchIssues(
								requireContext(),
								currentQuery,
								"open",
								null,
								null,
								null,
								null,
								page,
								resultLimit,
								false);
					}
				};
		viewBinding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		issuesViewModel
				.getIssues()
				.observe(
						getViewLifecycleOwner(),
						issues -> {
							adapter.updateList(issues);
							updateUiVisibility(
									Boolean.TRUE.equals(issuesViewModel.getIsLoading().getValue()));
						});

		issuesViewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);

		issuesViewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter.getItemCount() > 0;
		viewBinding.expressiveLoader.setVisibility(
				isLoading && !hasData ? View.VISIBLE : View.GONE);

		boolean hasLoadedOnce = Boolean.TRUE.equals(issuesViewModel.getHasLoadedOnce().getValue());
		viewBinding
				.layoutEmpty
				.getRoot()
				.setVisibility(!isLoading && !hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		viewBinding.pullToRefresh.setVisibility(
				!hasData && !isLoading && hasLoadedOnce ? View.GONE : View.VISIBLE);
	}

	private void refreshData(String query) {
		this.currentQuery = query;
		if (scrollListener != null) scrollListener.resetState();
		if (issuesViewModel == null) return;
		issuesViewModel.resetPagination();
		viewBinding.expressiveLoader.setVisibility(View.VISIBLE);
		issuesViewModel.fetchIssues(
				requireContext(), query, "open", null, null, null, null, 1, resultLimit, true);
	}

	private void setupSwipeRefresh() {
		viewBinding.pullToRefresh.setOnRefreshListener(
				() -> {
					viewBinding.pullToRefresh.setRefreshing(false);
					refreshData(currentQuery);
				});
	}

	private void showSearchBottomSheet() {
		SearchIssueBottomSheet sheet =
				SearchIssueBottomSheet.newInstance(
						currentQuery,
						new SearchIssueBottomSheet.SearchCallback() {
							@Override
							public void onSearchApplied(String query) {
								refreshData(query);
							}

							@Override
							public void onReset() {
								refreshData("");
							}
						});
		sheet.show(getChildFragmentManager(), "IssueSearchSheet");
	}

	public static class SearchIssueBottomSheet extends BottomSheetDialogFragment {
		private SearchCallback callback;
		private BottomsheetExploreIssuesBinding sheetBinding;

		public interface SearchCallback {
			void onSearchApplied(String query);

			void onReset();
		}

		public static SearchIssueBottomSheet newInstance(String query, SearchCallback cb) {
			SearchIssueBottomSheet fragment = new SearchIssueBottomSheet();
			Bundle args = new Bundle();
			args.putString("q", query);
			fragment.setArguments(args);
			fragment.callback = cb;
			return fragment;
		}

		@NonNull @Override
		public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
			BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
			AppUtil.applySheetStyle(dialog, true);
			return dialog;
		}

		@Nullable @Override
		public View onCreateView(
				@NonNull LayoutInflater inflater,
				@Nullable ViewGroup container,
				@Nullable Bundle savedInstanceState) {
			sheetBinding = BottomsheetExploreIssuesBinding.inflate(inflater, container, false);

			if (getArguments() != null) {
				sheetBinding.searchQueryEdit.setText(getArguments().getString("q", ""));
			}

			setupValidation();

			sheetBinding.btnApply.setOnClickListener(
					v -> {
						if (callback != null) {
							callback.onSearchApplied(
									Objects.requireNonNull(sheetBinding.searchQueryEdit.getText())
											.toString()
											.trim());
						}
						dismiss();
					});

			sheetBinding.btnClear.setOnClickListener(
					v -> {
						if (callback != null) {
							callback.onReset();
						}
						dismiss();
					});

			return sheetBinding.getRoot();
		}

		private void setupValidation() {
			validate();
			sheetBinding.searchQueryEdit.addTextChangedListener(
					new TextWatcher() {
						@Override
						public void beforeTextChanged(
								CharSequence s, int start, int count, int after) {}

						@Override
						public void onTextChanged(
								CharSequence s, int start, int before, int count) {
							validate();
						}

						@Override
						public void afterTextChanged(Editable s) {}
					});
		}

		private void validate() {
			String query =
					Objects.requireNonNull(sheetBinding.searchQueryEdit.getText())
							.toString()
							.trim();
			sheetBinding.btnApply.setEnabled(!query.isEmpty());
		}

		@Override
		public void onStart() {
			super.onStart();
			if (getDialog() instanceof BottomSheetDialog) {
				AppUtil.applySheetStyle((BottomSheetDialog) getDialog(), true);
			}
		}

		@Override
		public void onDestroyView() {
			super.onDestroyView();
			sheetBinding = null;
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		viewBinding = null;
	}
}
