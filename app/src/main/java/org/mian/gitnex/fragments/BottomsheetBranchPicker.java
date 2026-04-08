package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.adapters.BranchAdapter;
import org.mian.gitnex.databinding.BottomsheetBranchPickerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.FilesViewModel;

/**
 * @author mmarif
 */
public class BottomsheetBranchPicker extends BottomSheetDialogFragment {

	private BottomsheetBranchPickerBinding binding;
	private FilesViewModel viewModel;
	private String owner, repo, currentBranch;
	private BranchAdapter branchAdapter;
	private OnBranchSelectedListener listener;

	public interface OnBranchSelectedListener {
		void onSelected(String branchName);
	}

	public static BottomsheetBranchPicker newInstance(String owner, String repo, String current) {
		BottomsheetBranchPicker fragment = new BottomsheetBranchPicker();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		args.putString("current", current);
		fragment.setArguments(args);
		return fragment;
	}

	public void setOnBranchSelectedListener(OnBranchSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BottomsheetBranchPickerBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(FilesViewModel.class);

		Bundle args = getArguments();
		if (args != null) {
			owner = args.getString("owner");
			repo = args.getString("repo");
			currentBranch = args.getString("current");
		}

		setupRecyclerView();
		observeViewModel();

		viewModel.loadBranches(requireContext(), owner, repo);
		return binding.getRoot();
	}

	private void setupRecyclerView() {
		branchAdapter =
				new BranchAdapter(
						branchName -> {
							if (listener != null) listener.onSelected(branchName);
							dismiss();
						});

		LinearLayoutManager lm = new LinearLayoutManager(getContext());
		binding.recyclerView.setLayoutManager(lm);
		binding.recyclerView.setAdapter(branchAdapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(lm) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						boolean isLoading =
								Boolean.TRUE.equals(viewModel.getIsBranchesLoading().getValue());
						boolean isLastPage =
								Boolean.TRUE.equals(viewModel.getIsLastPageBranches().getValue());

						if (!isLoading && !isLastPage) {
							viewModel.loadBranches(requireContext(), owner, repo);
						}
					}
				};

		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getBranches()
				.observe(
						getViewLifecycleOwner(),
						branches -> {
							if (branches != null) {
								branchAdapter.setBranches(branches);
							}
						});

		viewModel
				.getIsBranchesLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean isLoading = Boolean.TRUE.equals(loading);
							boolean hasData = branchAdapter.getItemCount() > 0;

							if (isLoading) {
								binding.expressiveLoader.setVisibility(
										hasData ? View.GONE : View.VISIBLE);
							} else {
								binding.expressiveLoader.setVisibility(View.GONE);
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								if (branchAdapter.getItemCount() == 0) {
									binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
								}
							}
						});
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
		binding = null;
	}
}
