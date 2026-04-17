package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.databinding.ActivityRepositorySettingsBinding;
import org.mian.gitnex.databinding.BottomsheetRepoDeleteBinding;
import org.mian.gitnex.databinding.BottomsheetRepoTransferBinding;
import org.mian.gitnex.fragments.BottomSheetRepoProperties;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepositorySettingsViewModel;

/**
 * @author mmarif
 */
public class RepositorySettingsActivity extends BaseActivity {

	private ActivityRepositorySettingsBinding binding;
	private RepositorySettingsViewModel viewModel;
	private RepositoryContext repository;
	private String repositoryName;
	private int repositoryId;

	private BottomSheetDialog currentSheet;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepositorySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, binding.scrollView, null, null);

		repository = RepositoryContext.fromIntent(getIntent());
		repositoryName = repository.getName();
		repositoryId = repository.getRepositoryId();

		viewModel = new ViewModelProvider(this).get(RepositorySettingsViewModel.class);

		viewModel.clearTransferredRepo();
		viewModel.clearTransferError();
		viewModel.clearRepoDeleted();
		viewModel.clearDeleteError();

		setupCards();
		setupListeners();
		observeViewModel();
	}

	private void setupCards() {
		binding.editProperties.cardIcon.setImageResource(R.drawable.ic_edit);
		binding.editProperties.cardTitle.setText(R.string.repoSettingsEditProperties);
		binding.editProperties.cardSubtext.setText(R.string.repoSettingsEditPropertiesHint);

		binding.transferOwnerFrame.cardIcon.setImageResource(R.drawable.ic_arrow_up);
		binding.transferOwnerFrame.cardTitle.setText(R.string.repoSettingsTransferOwnership);
		binding.transferOwnerFrame.cardSubtext.setText(R.string.repoSettingsTransferOwnershipHint);

		binding.deleteRepositoryFrame.cardIcon.setImageResource(R.drawable.ic_delete);
		binding.deleteRepositoryFrame.cardTitle.setText(R.string.repoSettingsDelete);
		binding.deleteRepositoryFrame.cardSubtext.setText(R.string.repoSettingsDeleteHint);
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> finish());
		binding.editProperties.getRoot().setOnClickListener(v -> showRepoPropertiesBottomSheet());
		binding.transferOwnerFrame.getRoot().setOnClickListener(v -> showTransferBottomSheet());
		binding.deleteRepositoryFrame.getRoot().setOnClickListener(v -> showDeleteBottomSheet());
	}

	private void observeViewModel() {
		viewModel.getIsTransferring().observe(this, isTransferring -> {});

		viewModel
				.getTransferredRepo()
				.observe(
						this,
						transferredRepo -> {
							if (transferredRepo != null) {
								if (currentSheet != null && currentSheet.isShowing()) {
									currentSheet.dismiss();
								}
								Toasty.show(this, R.string.repoTransferSuccess);
								deleteLocalRepositoryAndExit();
								viewModel.clearTransferredRepo();
							}
						});

		viewModel
				.getTransferError()
				.observe(
						this,
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(this, error);
								viewModel.clearTransferError();
							}
						});

		viewModel.getIsDeleting().observe(this, isDeleting -> {});

		viewModel
				.getRepoDeleted()
				.observe(
						this,
						deleted -> {
							if (deleted != null && deleted) {
								if (currentSheet != null && currentSheet.isShowing()) {
									currentSheet.dismiss();
								}
								Toasty.show(this, R.string.repoDeletionSuccess);
								deleteLocalRepositoryAndExit();
								viewModel.clearRepoDeleted();
							}
						});

		viewModel
				.getDeleteError()
				.observe(
						this,
						error -> {
							if (error != null && !error.isEmpty()) {
								Toasty.show(this, error);
								viewModel.clearDeleteError();
							}
						});
	}

	private void deleteLocalRepositoryAndExit() {
		Objects.requireNonNull(BaseApi.getInstance(this, RepositoriesApi.class))
				.deleteRepository(repositoryId);
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	private void showRepoPropertiesBottomSheet() {
		BottomSheetRepoProperties.newInstance(repository.getOwner(), repository.getName())
				.show(getSupportFragmentManager(), "REPO_PROPERTIES");
	}

	private void showTransferBottomSheet() {
		BottomsheetRepoTransferBinding sheetBinding =
				BottomsheetRepoTransferBinding.inflate(LayoutInflater.from(this));
		currentSheet = new BottomSheetDialog(this);
		currentSheet.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(currentSheet, false);

		sheetBinding.btnClose.setOnClickListener(v -> currentSheet.dismiss());

		viewModel
				.getIsTransferring()
				.observe(
						this,
						isTransferring -> {
							if (currentSheet != null && currentSheet.isShowing()) {
								sheetBinding.transfer.setVisibility(
										isTransferring ? View.GONE : View.VISIBLE);
								sheetBinding.processingRequest.setVisibility(
										isTransferring ? View.VISIBLE : View.GONE);
							}
						});

		sheetBinding.transfer.setOnClickListener(
				v -> {
					String newOwner =
							sheetBinding.ownerNameForTransfer.getText() != null
									? sheetBinding.ownerNameForTransfer.getText().toString().trim()
									: "";
					String repoNameInput =
							sheetBinding.repoNameForTransfer.getText() != null
									? sheetBinding.repoNameForTransfer.getText().toString().trim()
									: "";

					if (!repositoryName.equals(repoNameInput)) {
						Toasty.show(this, R.string.repoSettingsDeleteError);
						return;
					}

					if (newOwner.isEmpty()) {
						Toasty.show(this, R.string.repoTransferOwnerError);
						return;
					}

					viewModel.transferRepository(
							this, repository.getOwner(), repositoryName, newOwner);
				});

		currentSheet.setOnDismissListener(
				dialog -> {
					currentSheet = null;
				});

		currentSheet.show();
	}

	private void showDeleteBottomSheet() {
		BottomsheetRepoDeleteBinding sheetBinding =
				BottomsheetRepoDeleteBinding.inflate(LayoutInflater.from(this));
		currentSheet = new BottomSheetDialog(this);
		currentSheet.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(currentSheet, false);

		sheetBinding.btnClose.setOnClickListener(v -> currentSheet.dismiss());

		viewModel
				.getIsDeleting()
				.observe(
						this,
						isDeleting -> {
							if (currentSheet != null && currentSheet.isShowing()) {
								sheetBinding.delete.setVisibility(
										isDeleting ? View.GONE : View.VISIBLE);
								sheetBinding.processingRequest.setVisibility(
										isDeleting ? View.VISIBLE : View.GONE);
							}
						});

		sheetBinding.delete.setOnClickListener(
				v -> {
					String repoNameInput =
							sheetBinding.repoNameForDeletion.getText() != null
									? sheetBinding.repoNameForDeletion.getText().toString().trim()
									: "";

					if (!repositoryName.equals(repoNameInput)) {
						Toasty.show(this, R.string.repoSettingsDeleteError);
						return;
					}

					viewModel.deleteRepository(this, repository.getOwner(), repositoryName);
				});

		currentSheet.setOnDismissListener(
				dialog -> {
					currentSheet = null;
				});

		currentSheet.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}
}
