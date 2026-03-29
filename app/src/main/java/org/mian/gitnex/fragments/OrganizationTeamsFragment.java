package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateTeamOption;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.adapters.OrganizationTeamsAdapter;
import org.mian.gitnex.databinding.BottomsheetOrgCreateTeamBinding;
import org.mian.gitnex.databinding.FragmentOrganizationTeamsBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationTeamsFragment extends Fragment
		implements OrganizationDetailActivity.OrgActionInterface {

	private FragmentOrganizationTeamsBinding binding;
	private OrganizationsViewModel viewModel;
	private OrganizationTeamsAdapter adapter;
	private String orgName;
	private boolean isSearching = false;
	private boolean isFirstLoad = true;

	public static OrganizationTeamsFragment newInstance(String orgName) {
		OrganizationTeamsFragment fragment = new OrganizationTeamsFragment();
		Bundle args = new Bundle();
		args.putString("orgName", orgName);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		int paddingTopPx = getResources().getDimensionPixelSize(R.dimen.dimen56dp);

		ViewCompat.setOnApplyWindowInsetsListener(
				view,
				(v, windowInsets) -> {
					Insets systemBars =
							windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
					binding.recyclerView.setPadding(
							binding.recyclerView.getPaddingLeft(),
							paddingTopPx,
							binding.recyclerView.getPaddingRight(),
							binding.recyclerView.getPaddingBottom());

					return windowInsets;
				});
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
		binding = FragmentOrganizationTeamsBinding.inflate(inflater, container, false);

		viewModel = new ViewModelProvider(requireActivity()).get(OrganizationsViewModel.class);

		setupRecyclerView();
		setupSearch();
		setupSwipeRefresh();
		observeViewModel();

		return binding.getRoot();
	}

	private void setupRecyclerView() {
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
	}

	private void observeViewModel() {
		viewModel
				.getTeams()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (adapter == null) {
								adapter =
										new OrganizationTeamsAdapter(
												requireContext(),
												new ArrayList<>(list),
												viewModel.getPermissions().getValue(),
												orgName);
								binding.recyclerView.setAdapter(adapter);
								binding.searchResultsRecycler.setAdapter(adapter);
							} else {
								adapter.updateTeams(list);
							}
							updateUiVisibility(
									Boolean.TRUE.equals(viewModel.getIsTeamsLoading().getValue()));
						});

		viewModel
				.getTeamMembersMap()
				.observe(
						getViewLifecycleOwner(),
						map -> {
							if (adapter != null) {
								adapter.updateMemberMap(map);
							}
						});

		viewModel.getIsTeamsLoading().observe(getViewLifecycleOwner(), this::updateUiVisibility);
	}

	private void updateUiVisibility(boolean isLoading) {
		boolean hasData = adapter != null && adapter.getItemCount() > 0;
		boolean hasLoadedOnce = Boolean.TRUE.equals(viewModel.getTeamsLoadedOnce().getValue());

		binding.expressiveLoader.setVisibility(isLoading && !hasData ? View.VISIBLE : View.GONE);

		if (isLoading) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			binding.layoutEmpty
					.getRoot()
					.setVisibility(!hasData && hasLoadedOnce ? View.VISIBLE : View.GONE);
		}

		binding.pullToRefresh.setVisibility(hasData ? View.VISIBLE : View.GONE);
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
														binding.layoutEmpty
																.getRoot()
																.setVisibility(
																		isSearching
																						&& adapter
																										.getItemCount()
																								== 0
																				? View.VISIBLE
																				: View.GONE);
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

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					viewModel.fetchTeams(requireContext(), orgName);
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
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
		viewModel.fetchTeams(requireContext(), orgName);
	}

	@Override
	public void onSearchTriggered() {
		binding.searchView.show();
	}

	@Override
	public void onAddRequested() {
		showCreateTeamBottomSheet();
	}

	@Override
	public boolean canAdd() {
		OrganizationPermissions perms = viewModel.getPermissions().getValue();
		return perms != null && perms.isIsOwner();
	}

	private void showCreateTeamBottomSheet() {
		BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
		BottomsheetOrgCreateTeamBinding sheetBinding =
				BottomsheetOrgCreateTeamBinding.inflate(getLayoutInflater());
		bottomSheetDialog.setContentView(sheetBinding.getRoot());

		AppUtil.applySheetStyle(bottomSheetDialog, false);

		sheetBinding.btnCreate.setOnClickListener(
				v -> {
					if (validateInput(sheetBinding)) {
						String name =
								Objects.requireNonNull(sheetBinding.teamName.getText())
										.toString()
										.trim();
						String desc =
								Objects.requireNonNull(sheetBinding.teamDesc.getText())
										.toString()
										.trim();
						String permission = getSelectedPermission(sheetBinding);
						List<String> units = getSelectedUnits(sheetBinding);

						CreateTeamOption options = new CreateTeamOption();
						options.setName(name);
						options.setDescription(desc);

						if (permission.equalsIgnoreCase("admin"))
							options.setPermission(CreateTeamOption.PermissionEnum.ADMIN);
						else if (permission.equalsIgnoreCase("write"))
							options.setPermission(CreateTeamOption.PermissionEnum.WRITE);
						else options.setPermission(CreateTeamOption.PermissionEnum.READ);

						options.setUnits(units);

						performCreateTeam(options, bottomSheetDialog, sheetBinding);
					}
				});

		sheetBinding.btnClose.setOnClickListener(v -> bottomSheetDialog.dismiss());
		bottomSheetDialog.show();
	}

	private List<String> getSelectedUnits(BottomsheetOrgCreateTeamBinding sheetBinding) {
		List<String> units = new ArrayList<>();
		if (sheetBinding.chipCode.isChecked()) units.add("repo.code");
		if (sheetBinding.chipIssues.isChecked()) units.add("repo.issues");
		if (sheetBinding.chipPulls.isChecked()) units.add("repo.pulls");
		if (sheetBinding.chipRelease.isChecked()) units.add("repo.releases");
		if (sheetBinding.chipWiki.isChecked()) units.add("repo.wiki");
		if (sheetBinding.chipExternalWiki.isChecked()) units.add("repo.ext_wiki");
		if (sheetBinding.chipExternalIssues.isChecked()) units.add("repo.ext_issues");
		return units;
	}

	private String getSelectedPermission(BottomsheetOrgCreateTeamBinding sheetBinding) {
		int id = sheetBinding.permissionChipGroup.getCheckedChipId();
		if (id == R.id.chip_perm_write) return "write";
		if (id == R.id.chip_perm_admin) return "admin";
		return "read";
	}

	private boolean validateInput(BottomsheetOrgCreateTeamBinding binding) {
		String name = Objects.requireNonNull(binding.teamName.getText()).toString().trim();
		String desc = Objects.requireNonNull(binding.teamDesc.getText()).toString().trim();

		if (name.isEmpty()) {
			Toasty.show(requireContext(), getString(R.string.teamNameEmpty));
			return false;
		}

		if (!AppUtil.checkStringsWithAlphaNumericDashDotUnderscore(name)) {
			Toasty.show(requireContext(), getString(R.string.teamNameError));
			return false;
		}

		if (!desc.isEmpty()) {
			if (!AppUtil.checkStrings(desc)) {
				Toasty.show(requireContext(), getString(R.string.teamDescError));
				return false;
			}
			if (desc.length() > 100) {
				Toasty.show(requireContext(), getString(R.string.teamDescLimit));
				return false;
			}
		}

		return true;
	}

	private void performCreateTeam(
			CreateTeamOption options,
			BottomSheetDialog dialog,
			BottomsheetOrgCreateTeamBinding binding) {

		viewModel
				.getIsCreatingTeam()
				.observe(
						getViewLifecycleOwner(),
						isLoading -> {
							binding.loadingIndicator.setVisibility(
									isLoading ? View.VISIBLE : View.GONE);
							binding.btnCreate.setEnabled(!isLoading);
							binding.btnCreate.setText(
									isLoading ? "" : getString(R.string.newCreateButtonCopy));
						});

		viewModel
				.getCreateTeamResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == -1) return;

							if (code == 201) {
								Toasty.show(requireContext(), getString(R.string.teamCreated));
								dialog.dismiss();
								viewModel.fetchTeams(requireContext(), orgName);
							} else if (code == 404) {
								Toasty.show(requireContext(), getString(R.string.apiNotFound));
							} else if (code == 401) {
								AlertDialogs.authorizationTokenRevokedDialog(requireContext());
							} else {
								Toasty.show(requireContext(), getString(R.string.genericError));
							}
						});

		viewModel.createTeam(requireContext(), orgName, options);
		viewModel.resetCreateTeamResult();
	}
}
