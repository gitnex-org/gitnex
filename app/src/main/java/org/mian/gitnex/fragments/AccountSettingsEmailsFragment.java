package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AccountSettingsEmailsAdapter;
import org.mian.gitnex.databinding.BottomsheetAddEmailBinding;
import org.mian.gitnex.databinding.FragmentAccountSettingsEmailsBinding;
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
public class AccountSettingsEmailsFragment extends Fragment {

	private FragmentAccountSettingsEmailsBinding binding;
	private AccountSettingsViewModel viewModel;
	private AccountSettingsEmailsAdapter adapter;
	private EndlessRecyclerViewScrollListener scrollListener;
	private int resultLimit;
	private BottomSheetDialog bottomSheetAddEmail;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAccountSettingsEmailsBinding.inflate(inflater, container, false);
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
				new AccountSettingsEmailsAdapter(
						new ArrayList<>(),
						requireContext(),
						(email, position) -> showDeleteConfirmation(email.getEmail(), position));

		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());

		binding.recyclerView.setLayoutManager(layoutManager);
		binding.recyclerView.setAdapter(adapter);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchEmails(requireContext(), page, resultLimit, false);
					}
				};

		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void observeViewModel() {
		viewModel
				.getEmails()
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
				.getAddEmailStatus()
				.observe(
						getViewLifecycleOwner(),
						status -> {
							if (status == -1) return;
							handlePostAddEmail(status);
							viewModel.resetAddEmailStatus();
						});

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) Toasty.show(requireContext(), error);
						});

		viewModel
				.getAddEmailStatus()
				.observe(
						getViewLifecycleOwner(),
						status -> {
							if (status == -1) return;
							handlePostAddEmail(status);
							viewModel.resetAddEmailStatus();
						});
	}

	private void showDeleteConfirmation(String email, int position) {
		new MaterialAlertDialogBuilder(
						requireContext(), R.style.ThemeOverlay_Material3_Dialog_Alert)
				.setMessage(String.format(getString(R.string.deleteEmailPopupText), email))
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) -> {
							viewModel.deleteEmail(requireContext(), email, position);
						})
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	public void showAddEmailDialog() {
		BottomsheetAddEmailBinding sheetBinding =
				BottomsheetAddEmailBinding.inflate(LayoutInflater.from(requireContext()));

		bottomSheetAddEmail = new BottomSheetDialog(requireContext());
		bottomSheetAddEmail.setContentView(sheetBinding.getRoot());

		AppUtil.applySheetStyle(bottomSheetAddEmail, false);

		viewModel
				.getIsAddingEmail()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							sheetBinding.save.setEnabled(!loading);
							sheetBinding.save.setText(
									loading ? "" : getString(R.string.saveButton));
							sheetBinding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							sheetBinding.userEmail.setEnabled(!loading);
						});

		sheetBinding.save.setOnClickListener(
				v -> {
					String email =
							Objects.requireNonNull(sheetBinding.userEmail.getText())
									.toString()
									.trim();

					if (email.isEmpty()) {
						Toasty.show(requireContext(), getString(R.string.emailErrorEmpty));
					} else if (!isValidEmail(email)) {
						Toasty.show(requireContext(), getString(R.string.userInvalidEmail));
					} else {
						viewModel.addNewEmail(requireContext(), email, resultLimit);
					}
				});

		sheetBinding.btnClose.setOnClickListener(v -> bottomSheetAddEmail.dismiss());
		bottomSheetAddEmail.show();
	}

	private void handlePostAddEmail(int code) {
		if (code == 201) {
			if (bottomSheetAddEmail != null && bottomSheetAddEmail.isShowing()) {
				bottomSheetAddEmail.dismiss();
			}
			Toasty.show(requireContext(), getString(R.string.emailAddedText));
			refreshData();
		} else if (code == 401) {
			TokenAuthorizationDialog.authorizationTokenRevokedDialog(requireContext());
		} else if (code == 422) {
			Toasty.show(requireContext(), getString(R.string.emailErrorInUse));
		} else {
			Toasty.show(requireContext(), getString(R.string.genericError));
		}
	}

	private boolean isValidEmail(String email) {
		return !TextUtils.isEmpty(email)
				&& android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
	}

	private void setupSwipeRefresh() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void refreshData() {
		scrollListener.resetState();
		viewModel.fetchEmails(requireContext(), 1, resultLimit, true);
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
