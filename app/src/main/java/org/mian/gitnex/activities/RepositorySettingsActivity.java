package org.mian.gitnex.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.v2.models.EditRepoOption;
import org.gitnex.tea4j.v2.models.InternalTracker;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.TransferRepoOption;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.databinding.ActivityRepositorySettingsBinding;
import org.mian.gitnex.databinding.CustomRepositoryDeleteDialogBinding;
import org.mian.gitnex.databinding.CustomRepositoryEditPropertiesDialogBinding;
import org.mian.gitnex.databinding.CustomRepositoryTransferDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class RepositorySettingsActivity extends BaseActivity {

	private ActivityRepositorySettingsBinding viewBinding;
	private CustomRepositoryEditPropertiesDialogBinding propBinding;
	private CustomRepositoryDeleteDialogBinding deleteRepoBinding;
	private CustomRepositoryTransferDialogBinding transferRepoBinding;
	private Dialog dialogProp;
	private Dialog dialogDeleteRepository;
	private Dialog dialogTransferRepository;
	private View.OnClickListener onClickListener;

	private RepositoryContext repository;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityRepositorySettingsBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		repository = RepositoryContext.fromIntent(getIntent());

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		viewBinding.editProperties.setOnClickListener(editProperties -> showRepositoryProperties());

		viewBinding.deleteRepositoryFrame.setOnClickListener(deleteRepository -> showDeleteRepository());

		viewBinding.transferOwnerFrame.setOnClickListener(transferRepositoryOwnership -> showTransferRepository());
	}

	private void showTransferRepository() {

		dialogTransferRepository = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogTransferRepository.getWindow() != null) {

			dialogTransferRepository.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		transferRepoBinding = CustomRepositoryTransferDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = transferRepoBinding.getRoot();
		dialogTransferRepository.setContentView(view);

		transferRepoBinding.cancel.setOnClickListener(editProperties -> dialogTransferRepository.dismiss());

		transferRepoBinding.transfer.setOnClickListener(deleteRepo -> {

			String newOwner = String.valueOf(transferRepoBinding.ownerNameForTransfer.getText());
			String repoName = String.valueOf(transferRepoBinding.repoNameForTransfer.getText());

			if(!repository.getName().equals(repoName)) {

				Toasty.error(ctx, getString(R.string.repoSettingsDeleteError));
			}
			else if(newOwner.matches("")) {

				Toasty.error(ctx, getString(R.string.repoTransferOwnerError));
			}
			else {

				transferRepository(newOwner);
			}
		});

		dialogTransferRepository.setCancelable(false);
		dialogTransferRepository.show();
	}

	private void transferRepository(String newOwner) {

		TransferRepoOption repositoryTransfer = new TransferRepoOption();
		repositoryTransfer.setNewOwner(newOwner);

		Call<Repository> transferCall = RetrofitClient
			.getApiInterface(ctx)
			.repoTransfer(repositoryTransfer, repository.getOwner(), repository.getName());

		transferCall.enqueue(new Callback<Repository>() {

			@Override
			public void onResponse(@NonNull Call<Repository> call, @NonNull retrofit2.Response<Repository> response) {

				transferRepoBinding.transfer.setVisibility(View.GONE);
				transferRepoBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 202) {

					dialogTransferRepository.dismiss();
					Toasty.success(ctx, getString(R.string.repoTransferSuccess));

					finish();
					Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class)).deleteRepository(repository.getRepositoryId());
					Intent intent = new Intent(RepositorySettingsActivity.this, MainActivity.class);
					RepositorySettingsActivity.this.startActivity(intent);
				}
				else if (response.code() == 404) {

					transferRepoBinding.transfer.setVisibility(View.VISIBLE);
					transferRepoBinding.processingRequest.setVisibility(View.GONE);
					Toasty.error(ctx, getString(R.string.repoTransferError));
				}
				else {

					transferRepoBinding.transfer.setVisibility(View.VISIBLE);
					transferRepoBinding.processingRequest.setVisibility(View.GONE);
					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

				transferRepoBinding.transfer.setVisibility(View.VISIBLE);
				transferRepoBinding.processingRequest.setVisibility(View.GONE);
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	private void showDeleteRepository() {

		dialogDeleteRepository = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogDeleteRepository.getWindow() != null) {

			dialogDeleteRepository.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		deleteRepoBinding = CustomRepositoryDeleteDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = deleteRepoBinding.getRoot();
		dialogDeleteRepository.setContentView(view);

		deleteRepoBinding.cancel.setOnClickListener(editProperties -> dialogDeleteRepository.dismiss());

		deleteRepoBinding.delete.setOnClickListener(deleteRepo -> {

			if(!repository.getName().equals(String.valueOf(deleteRepoBinding.repoNameForDeletion.getText()))) {

				Toasty.error(ctx, getString(R.string.repoSettingsDeleteError));
			}
			else {

				deleteRepository();
			}
		});

		dialogDeleteRepository.setCancelable(false);
		dialogDeleteRepository.show();
	}

	private void deleteRepository() {

		Call<Void> deleteCall = RetrofitClient
			.getApiInterface(ctx)
			.repoDelete(repository.getOwner(), repository.getName());

		deleteCall.enqueue(new Callback<Void>() {

			@Override
			public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

				deleteRepoBinding.delete.setVisibility(View.GONE);
				deleteRepoBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 204) {

					dialogDeleteRepository.dismiss();
					Toasty.success(ctx, getString(R.string.repoDeletionSuccess));

					finish();
					Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class)).deleteRepository(repository.getRepositoryId());
					Intent intent = new Intent(RepositorySettingsActivity.this, MainActivity.class);
					RepositorySettingsActivity.this.startActivity(intent);
				}
				else {

					deleteRepoBinding.delete.setVisibility(View.VISIBLE);
					deleteRepoBinding.processingRequest.setVisibility(View.GONE);
					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

				deleteRepoBinding.delete.setVisibility(View.VISIBLE);
				deleteRepoBinding.processingRequest.setVisibility(View.GONE);
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	private void showRepositoryProperties() {

		dialogProp = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogProp.getWindow() != null) {

			dialogProp.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		propBinding = CustomRepositoryEditPropertiesDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = propBinding.getRoot();
		dialogProp.setContentView(view);

		propBinding.cancel.setOnClickListener(editProperties -> dialogProp.dismiss());
		Repository repoInfo = repository.getRepository();

		propBinding.progressBar.setVisibility(View.GONE);
		propBinding.mainView.setVisibility(View.VISIBLE);


		assert repoInfo != null;
		propBinding.repoName.setText(repoInfo.getName());
		propBinding.repoWebsite.setText(repoInfo.getWebsite());
		propBinding.repoDescription.setText(repoInfo.getDescription());
		propBinding.repoPrivate.setChecked(repoInfo.isPrivate());
		propBinding.repoAsTemplate.setChecked(repoInfo.isTemplate());

		propBinding.repoEnableIssues.setChecked(repoInfo.isHasIssues());

		propBinding.repoEnableIssues.setOnCheckedChangeListener((buttonView, isChecked) -> {

			if (isChecked) {
				propBinding.repoEnableTimer.setVisibility(View.VISIBLE);
			}
			else {
				propBinding.repoEnableTimer.setVisibility(View.GONE);
			}
		});

		if(repoInfo.getInternalTracker() != null) {

			propBinding.repoEnableTimer.setChecked(repoInfo.getInternalTracker().isEnableTimeTracker());
		}
		else {

			propBinding.repoEnableTimer.setVisibility(View.GONE);
		}

		propBinding.repoEnableWiki.setChecked(repoInfo.isHasWiki());
		propBinding.repoEnablePr.setChecked(repoInfo.isHasPullRequests());
		propBinding.repoEnableMerge.setChecked(repoInfo.isAllowMergeCommits());
		propBinding.repoEnableRebase.setChecked(repoInfo.isAllowRebase());
		propBinding.repoEnableSquash.setChecked(repoInfo.isAllowSquashMerge());
		propBinding.repoEnableForceMerge.setChecked(repoInfo.isAllowRebaseExplicit());

		propBinding.save.setOnClickListener(saveProperties -> saveRepositoryProperties(String.valueOf(propBinding.repoName.getText()),
			String.valueOf(propBinding.repoWebsite.getText()),
			String.valueOf(propBinding.repoDescription.getText()),
			propBinding.repoPrivate.isChecked(), propBinding.repoAsTemplate.isChecked(),
			propBinding.repoEnableIssues.isChecked(), propBinding.repoEnableWiki.isChecked(),
			propBinding.repoEnablePr.isChecked(), propBinding.repoEnableTimer.isChecked(),
			propBinding.repoEnableMerge.isChecked(), propBinding.repoEnableRebase.isChecked(),
			propBinding.repoEnableSquash.isChecked(), propBinding.repoEnableForceMerge.isChecked()));

		dialogProp.setCancelable(false);
		dialogProp.show();
	}

	private void saveRepositoryProperties(String repoName, String repoWebsite, String repoDescription,
		boolean repoPrivate, boolean repoAsTemplate, boolean repoEnableIssues, boolean repoEnableWiki,
		boolean repoEnablePr, boolean repoEnableTimer, boolean repoEnableMerge, boolean repoEnableRebase,
		boolean repoEnableSquash, boolean repoEnableForceMerge) {

		EditRepoOption repoProps = new EditRepoOption();
		repoProps.setName(repoName);
		repoProps.setWebsite(repoWebsite);
		repoProps.setDescription(repoDescription);
		repoProps.setPrivate(repoPrivate);
		repoProps.setTemplate(repoAsTemplate);
		repoProps.setHasIssues(repoEnableIssues);
		repoProps.setHasWiki(repoEnableWiki);
		repoProps.setHasPullRequests(repoEnablePr);
		repoProps.setInternalTracker(new InternalTracker().enableTimeTracker(repoEnableTimer));
		repoProps.setAllowMergeCommits(repoEnableMerge);
		repoProps.setAllowRebase(repoEnableRebase);
		repoProps.setAllowSquashMerge(repoEnableSquash);
		repoProps.setAllowRebaseExplicit(repoEnableForceMerge);

		Call<Repository> propsCall = RetrofitClient
			.getApiInterface(ctx)
			.repoEdit(repository.getOwner(), repository.getName(), repoProps);

		propsCall.enqueue(new Callback<Repository>() {

			@Override
			public void onResponse(@NonNull Call<Repository> call, @NonNull retrofit2.Response<Repository> response) {

				propBinding.save.setVisibility(View.GONE);
				propBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 200) {

					dialogProp.dismiss();
					Toasty.success(ctx, getString(R.string.repoPropertiesSaveSuccess));
					assert response.body() != null;
					repository.setRepository(response.body());

					if(!repository.getName().equals(repoName)) {
						Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class))
							.updateRepositoryOwnerAndName(repository.getOwner(), repoName, repository.getRepositoryId());
						Intent result = new Intent();
						result.putExtra("nameChanged", true);
						setResult(200, result);
						finish();
					}
				}
				else {

					propBinding.save.setVisibility(View.VISIBLE);
					propBinding.processingRequest.setVisibility(View.GONE);
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<Repository> call, @NonNull Throwable t) {

				propBinding.save.setVisibility(View.VISIBLE);
				propBinding.processingRequest.setVisibility(View.GONE);
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		repository.checkAccountSwitch(this);
	}

}
