package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationAddUserToTeamMemberAdapter;
import org.mian.gitnex.databinding.BottomsheetAddTeamMemberBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrgTeamsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetAddTeamMember extends BottomSheetDialogFragment {

	private BottomsheetAddTeamMemberBinding binding;
	private OrgTeamsViewModel viewModel;
	private OrganizationAddUserToTeamMemberAdapter adapter;
	private long teamId;

	public static BottomSheetAddTeamMember newInstance(long teamId) {
		BottomSheetAddTeamMember fragment = new BottomSheetAddTeamMember();
		Bundle args = new Bundle();
		args.putLong("teamId", teamId);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL, R.style.Custom_BottomSheet);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		LayoutInflater themedInflater = inflater.cloneInContext(requireActivity());
		binding = BottomsheetAddTeamMemberBinding.inflate(themedInflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			teamId = args.getLong("teamId", -1);
		} else {
			teamId = -1;
			dismiss();
		}

		viewModel = new ViewModelProvider(this).get(OrgTeamsViewModel.class);

		if (teamId != -1) {
			viewModel.loadCurrentMembers(requireContext(), teamId);
		}

		setupRecyclerView();
		setupListeners();
		observeViewModel();
	}

	private void setupRecyclerView() {
		adapter =
				new OrganizationAddUserToTeamMemberAdapter(
						new ArrayList<>(), requireContext(), (int) teamId, viewModel);
		binding.rvUserSearch.setLayoutManager(new LinearLayoutManager(requireContext()));
		binding.rvUserSearch.setAdapter(adapter);
	}

	private void setupListeners() {
		binding.searchInputLayout.setEndIconOnClickListener(v -> performSearch());

		binding.searchInput.setOnEditorActionListener(
				(v, actionId, event) -> {
					if (actionId == EditorInfo.IME_ACTION_SEARCH) {
						performSearch();
						return true;
					}
					return false;
				});
	}

	private void performSearch() {
		String query = Objects.requireNonNull(binding.searchInput.getText()).toString().trim();
		if (query.length() > 1) {
			AppUtil.hideKeyboard(requireActivity());
			binding.searchInput.clearFocus();
			viewModel.fetchUsers(
					requireContext(),
					query,
					1,
					Constants.getCurrentResultLimit(requireContext()),
					true);
		}
	}

	private void observeViewModel() {
		viewModel
				.getUsers()
				.observe(
						getViewLifecycleOwner(),
						users -> {
							if (users != null && !users.isEmpty()) {
								adapter.updateList(users);
								binding.rvUserSearch.setVisibility(View.VISIBLE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.rvUserSearch.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
						});

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
										.setFragmentResult("member_changed", result);
							}
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) binding.layoutEmpty.getRoot().setVisibility(View.GONE);
						});
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
