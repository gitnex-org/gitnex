package org.mian.gitnex.fragments;

import android.app.Dialog;
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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationTeamRepositoriesAdapter;
import org.mian.gitnex.databinding.BottomsheetAddTeamRepoBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrgTeamsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetAddTeamRepo extends BottomSheetDialogFragment {

	private BottomsheetAddTeamRepoBinding binding;
	private OrgTeamsViewModel viewModel;
	private OrganizationTeamRepositoriesAdapter adapter;
	private long teamId;
	private String orgName;

	public static BottomSheetAddTeamRepo newInstance(long teamId, String orgName) {
		BottomSheetAddTeamRepo fragment = new BottomSheetAddTeamRepo();
		Bundle args = new Bundle();
		args.putLong("teamId", teamId);
		args.putString("orgName", orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL, R.style.Custom_BottomSheet);
		if (getArguments() != null) {
			teamId = getArguments().getLong("teamId");
			orgName = getArguments().getString("orgName");
		}
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		LayoutInflater themedInflater = inflater.cloneInContext(requireActivity());
		binding = BottomsheetAddTeamRepoBinding.inflate(themedInflater, container, false);

		viewModel = new ViewModelProvider(this).get(OrgTeamsViewModel.class);
		setupRecyclerView();
		observeViewModel();

		viewModel.loadTeamRepos(requireContext(), teamId);
		viewModel.fetchOrgRepos(requireContext(), orgName, 1, 50);

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter =
				new OrganizationTeamRepositoriesAdapter(
						new ArrayList<>(), requireContext(), (int) teamId, orgName, viewModel);
		binding.rvRepos.setLayoutManager(new LinearLayoutManager(getContext()));
		binding.rvRepos.setAdapter(adapter);
	}

	private void observeViewModel() {
		viewModel
				.getRepositories()
				.observe(
						getViewLifecycleOwner(),
						repos -> {
							adapter.updateList(repos);
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsLoading().getValue()));
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);

		viewModel
				.getActionSuccessMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								Bundle result = new Bundle();
								result.putBoolean("shouldRefresh", true);
								getParentFragmentManager()
										.setFragmentResult("repo_changed", result);
							}
						});
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter.getItemCount() > 0;

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);
		binding.rvRepos.setVisibility(hasData ? View.VISIBLE : View.GONE);

		boolean showEmpty = !isLoading && !hasData;
		binding.layoutEmpty.getRoot().setVisibility(showEmpty ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applyFullScreenSheetStyle((BottomSheetDialog) dialog, true);
		}
	}
}
