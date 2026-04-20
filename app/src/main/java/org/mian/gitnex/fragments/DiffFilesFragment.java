package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DiffFilesAdapter;
import org.mian.gitnex.databinding.FragmentDiffFilesBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.viewmodels.PullRequestDiffViewModel;

/**
 * @author opyale
 * @author mmarif
 */
public class DiffFilesFragment extends Fragment {

	private FragmentDiffFilesBinding binding;
	private PullRequestDiffViewModel viewModel;
	private DiffFilesAdapter adapter;
	private Context ctx;

	public DiffFilesFragment() {}

	public static DiffFilesFragment newInstance() {
		return new DiffFilesFragment();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(view, dock, binding.diffFiles, null, binding.toolbarTitle);
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentDiffFilesBinding.inflate(inflater, container, false);
		ctx = requireContext();

		viewModel = new ViewModelProvider(requireActivity()).get(PullRequestDiffViewModel.class);

		setupRecyclerView();
		observeViewModel();

		IssueContext issue = IssueContext.fromIntent(requireActivity().getIntent());
		if (viewModel.getFiles().getValue() == null || viewModel.getFiles().getValue().isEmpty()) {
			viewModel.fetchPRFiles(
					ctx,
					issue.getRepository().getOwner(),
					issue.getRepository().getName(),
					issue.getIssueIndex(),
					getResources());
		}

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		IssueContext issue = IssueContext.fromIntent(requireActivity().getIntent());
		adapter =
				new DiffFilesAdapter(
						ctx,
						new ArrayList<>(),
						issue,
						issue.getRepository().getOwner(),
						issue.getRepository().getName(),
						null,
						"pull");

		binding.diffFiles.setHasFixedSize(true);
		binding.diffFiles.setLayoutManager(new LinearLayoutManager(ctx));
		binding.diffFiles.setAdapter(adapter);
	}

	private void observeViewModel() {

		viewModel
				.getFiles()
				.observe(
						getViewLifecycleOwner(),
						files -> {
							if (files != null) {
								adapter.updateList(files);
							}
						});

		viewModel
				.getFilesHeader()
				.observe(
						getViewLifecycleOwner(),
						header -> {
							if (header != null && !header.isEmpty()) {
								binding.toolbarTitle.setVisibility(View.VISIBLE);
								binding.toolbarTitle.setText(header);
							}
						});

		viewModel
				.getIsFilesLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							binding.diffFiles.setVisibility(loading ? View.GONE : View.VISIBLE);
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								Toasty.show(ctx, error);
							}
						});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
