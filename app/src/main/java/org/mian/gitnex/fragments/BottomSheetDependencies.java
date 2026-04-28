package org.mian.gitnex.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Objects;
import org.mian.gitnex.adapters.DependencyAdapter;
import org.mian.gitnex.databinding.BottomsheetDependenciesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.DependenciesViewModel;

/**
 * @author mmarif
 */
public class BottomSheetDependencies extends BottomSheetDialogFragment {

	private BottomsheetDependenciesBinding binding;
	private DependenciesViewModel viewModel;
	private DependencyAdapter dependenciesAdapter;
	private DependencyAdapter searchResultsAdapter;
	private String owner;
	private String repo;
	private long issueIndex;

	public static BottomSheetDependencies newInstance(String owner, String repo, long issueIndex) {
		BottomSheetDependencies sheet = new BottomSheetDependencies();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		args.putLong("issue_index", issueIndex);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetDependenciesBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			owner = args.getString("owner");
			repo = args.getString("repo");
			issueIndex = args.getLong("issue_index");
		}

		viewModel = new ViewModelProvider(requireActivity()).get(DependenciesViewModel.class);

		binding.btnClose.setOnClickListener(
				v -> {
					viewModel.clearSearchResults();
					binding.searchInput.setText("");
					dismiss();
				});

		dependenciesAdapter = new DependencyAdapter(new ArrayList<>(), true);
		binding.dependenciesRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.dependenciesRecyclerView.setAdapter(dependenciesAdapter);
		dependenciesAdapter.setOnItemClickListener(
				(dependency, position) -> {
					viewModel.removeDependency(
							requireContext(), owner, repo, issueIndex, dependency);
				});

		searchResultsAdapter = new DependencyAdapter(new ArrayList<>(), false);
		binding.searchResultsRecyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));
		binding.searchResultsRecyclerView.setAdapter(searchResultsAdapter);
		searchResultsAdapter.setOnItemClickListener(
				(dependency, position) -> {
					viewModel.addDependency(requireContext(), owner, repo, issueIndex, dependency);
					binding.searchInput.setText("");
				});

		binding.searchInputLayout.setEndIconOnClickListener(
				v -> {
					String query =
							Objects.requireNonNull(binding.searchInput.getText()).toString().trim();
					if (!query.isEmpty()) {
						viewModel.searchIssues(
								requireContext(),
								owner,
								repo,
								query,
								issueIndex,
								dependenciesAdapter.getItems());
					}
				});

		observeViewModel();
		viewModel.loadDependencies(requireContext(), owner, repo, issueIndex);
	}

	private void observeViewModel() {
		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getDependencies()
				.observe(
						getViewLifecycleOwner(),
						deps -> {
							boolean loading =
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
							dependenciesAdapter.updateList(deps);
							boolean empty = deps.isEmpty();

							binding.dependenciesRecyclerView.setVisibility(
									!loading && !empty ? View.VISIBLE : View.GONE);
							binding.layoutEmpty
									.getRoot()
									.setVisibility(!loading && empty ? View.VISIBLE : View.GONE);
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getSearchResults()
				.observe(
						getViewLifecycleOwner(),
						results -> {
							if (results != null) {
								searchResultsAdapter.updateList(results);
								binding.searchResultsRecyclerView.setVisibility(
										results.isEmpty() ? View.GONE : View.VISIBLE);
							}
						});

		viewModel
				.getActionMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								viewModel.clearActionMessage();
							}
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								Toasty.show(requireContext(), error);
							}
						});
	}

	@Override
	public void onCancel(@NonNull DialogInterface dialog) {
		super.onCancel(dialog);
		viewModel.clearSearchResults();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applyFullScreenSheetStyle(dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
