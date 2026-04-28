package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.CreateKeyOption;
import org.gitnex.tea4j.v2.models.PublicKey;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AccountSettingsSSHKeysAdapter;
import org.mian.gitnex.databinding.BottomsheetAddSshKeyBinding;
import org.mian.gitnex.databinding.FragmentAccountSettingsSshKeysBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.AccountSettingsViewModel;

/**
 * @author mmarif
 */
public class AccountSettingsSSHKeysFragment extends Fragment {

	private FragmentAccountSettingsSshKeysBinding binding;
	private AccountSettingsViewModel viewModel;
	private AccountSettingsSSHKeysAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private BottomSheetDialog bottomSheetAddKey;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAccountSettingsSshKeysBinding.inflate(inflater, container, false);
		resultLimit = Constants.getCurrentResultLimit(requireContext());
		viewModel = new ViewModelProvider(requireActivity()).get(AccountSettingsViewModel.class);

		setupRecyclerView();
		setupSwipeRefresh();
		observeViewModel();

		refreshData();
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		View dock = requireActivity().findViewById(R.id.docked_toolbar);
		UIHelper.applyInsets(
				binding.getRoot(), dock, binding.recyclerView, binding.pullToRefresh, null);
	}

	private void setupRecyclerView() {
		adapter =
				new AccountSettingsSSHKeysAdapter(
						new ArrayList<>(), requireContext(), this::showDeleteConfirmation);

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchSshKeys(requireContext(), page, resultLimit, false);
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.fetchSshKeys(requireContext(), 1, resultLimit, true);
	}

	private void observeViewModel() {
		viewModel
				.getSshKeys()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							binding.pullToRefresh.setRefreshing(false);
							adapter.updateList(list);
							updateUiState();
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							boolean hasData = adapter.getItemCount() > 0;

							binding.expressiveLoader.setVisibility(
									loading && !hasData ? View.VISIBLE : View.GONE);

							if (loading && !hasData) {
								binding.recyclerView.setVisibility(View.GONE);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								updateUiState();
							}
						});

		viewModel
				.getAddKeyStatus()
				.observe(
						getViewLifecycleOwner(),
						status -> {
							if (status == -1) return;
							handlePostAddKey(status);
							viewModel.resetAddKeyStatus();
						});

		viewModel
				.getDeleteKeyStatus()
				.observe(
						getViewLifecycleOwner(),
						status -> {
							if (status == -1) return;
							if (status == 204 || status == 200) {
								Toasty.show(
										requireContext(), getString(R.string.sshKeyDeleteSuccess));
							}
							viewModel.resetDeleteKeyStatus();
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								binding.pullToRefresh.setRefreshing(false);
								Toasty.show(requireContext(), error);
							}
						});
	}

	public void showNewSSHKeyDialog() {
		BottomsheetAddSshKeyBinding sheetBinding =
				BottomsheetAddSshKeyBinding.inflate(LayoutInflater.from(requireContext()));

		bottomSheetAddKey = new BottomSheetDialog(requireContext());
		bottomSheetAddKey.setContentView(sheetBinding.getRoot());

		sheetBinding.key.setOnTouchListener(
				(v, event) -> {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
					} else if (event.getAction() == MotionEvent.ACTION_UP
							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
						v.getParent().requestDisallowInterceptTouchEvent(false);
						v.performClick();
					}
					return false;
				});

		AppUtil.applySheetStyle(bottomSheetAddKey, false);

		sheetBinding.keyStatus.setOnCheckedChangeListener(
				(buttonView, isChecked) -> {
					sheetBinding.keyStatus.setText(
							isChecked
									? getString(R.string.sshKeyStatusReadWrite)
									: getString(R.string.sshKeyStatusReadOnly));
				});

		viewModel
				.getIsAddingKey()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							sheetBinding.save.setEnabled(!loading);
							sheetBinding.save.setText(
									loading ? "" : getString(R.string.saveButton));
							sheetBinding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							sheetBinding.keyTitle.setEnabled(!loading);
							sheetBinding.key.setEnabled(!loading);
							sheetBinding.keyStatus.setEnabled(!loading);
						});

		sheetBinding.save.setOnClickListener(
				v -> {
					String title =
							Objects.requireNonNull(sheetBinding.keyTitle.getText())
									.toString()
									.trim();
					String key =
							Objects.requireNonNull(sheetBinding.key.getText()).toString().trim();

					if (title.isEmpty() || key.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.emptyFields));
					} else {
						CreateKeyOption option = new CreateKeyOption();
						option.setTitle(title);
						option.setKey(key);
						option.setReadOnly(!sheetBinding.keyStatus.isChecked());
						viewModel.addNewSshKey(requireContext(), option, resultLimit);
					}
				});

		sheetBinding.btnClose.setOnClickListener(v -> bottomSheetAddKey.dismiss());
		bottomSheetAddKey.show();
	}

	private void handlePostAddKey(int code) {
		if (code == 201 || code == 202) {
			if (bottomSheetAddKey != null && bottomSheetAddKey.isShowing()) {
				bottomSheetAddKey.dismiss();
			}
			Toasty.show(requireContext(), getString(R.string.sshKeySuccess));
			refreshData();
		} else if (code == 401) {
			TokenAuthorizationDialog.authorizationTokenRevokedDialog(requireContext());
		} else if (code == 422) {
			Toasty.show(requireContext(), getString(R.string.sshKeyError));
		} else {
			Toasty.show(requireContext(), getString(R.string.genericError));
		}
	}

	private void showDeleteConfirmation(PublicKey key, int position) {
		new MaterialAlertDialogBuilder(
						requireContext(), R.style.ThemeOverlay_Material3_Dialog_Alert)
				.setMessage(
						String.format(getString(R.string.deleteSshKeyPopupText), key.getTitle()))
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) -> {
							viewModel.deleteSshKey(requireContext(), key.getId(), position);
						})
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	private void updateUiState() {
		boolean isLoading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());
		boolean isEmpty = adapter.getItemCount() == 0;

		if (isLoading && isEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.GONE);
		} else if (isEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		}
	}
}
