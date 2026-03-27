package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class ExplorePublicOrganizationsFragment extends Fragment {

	private FragmentOrganizationsBinding binding;
	private OrganizationsViewModel viewModel;
	private OrganizationsListAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ViewCompat.setOnApplyWindowInsetsListener(
				view,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
					binding.recyclerView.setPadding(
							binding.recyclerView.getPaddingLeft(),
							systemBars.top,
							binding.recyclerView.getPaddingRight(),
							binding.recyclerView.getPaddingBottom());
					return windowInsets;
				});
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupUI();
		observeViewModel();
		refreshData();

		return binding.getRoot();
	}

	private void setupUI() {
		// binding.addNewOrganization.setVisibility(View.GONE);
		adapter = new OrganizationsListAdapter(requireContext(), new ArrayList<>());
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchAllPublicOrgs(
								requireContext(), page + 1, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);

		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void observeViewModel() {
		viewModel
				.getOrgs()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							adapter.updateList(list);
							binding.layoutEmpty
									.getRoot()
									.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
							binding.pullToRefresh.setRefreshing(false);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading ->
								binding.expressiveLoader.setVisibility(
										loading && adapter.getItemCount() == 0
												? View.VISIBLE
												: View.GONE));
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.fetchAllPublicOrgs(requireContext(), 1, resultLimit, true);
	}
}
