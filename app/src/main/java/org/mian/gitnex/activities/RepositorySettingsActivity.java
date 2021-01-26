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
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.databinding.ActivityRepositorySettingsBinding;
import org.mian.gitnex.databinding.CustomRepositoryDeleteDialogBinding;
import org.mian.gitnex.databinding.CustomRepositoryEditPropertiesDialogBinding;
import org.mian.gitnex.databinding.CustomRepositoryTransferDialogBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.models.RepositoryTransfer;
import org.mian.gitnex.models.UserRepositories;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
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

	private String loginUid;
	private String instanceToken;

	private String repositoryOwner;
	private String repositoryName;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		viewBinding = ActivityRepositorySettingsBinding.inflate(getLayoutInflater());
		setContentView(viewBinding.getRoot());

		loginUid = tinyDB.getString("loginUid");
		String repoFullName = tinyDB.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		repositoryOwner = parts[0];
		repositoryName = parts[1];
		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		// require gitea 1.12 or higher
		if(new Version(tinyDB.getString("giteaVersion")).higherOrEqual("1.12.0")) {

			viewBinding.transferOwnerFrame.setVisibility(View.VISIBLE);
		}

		viewBinding.editProperties.setOnClickListener(editProperties -> showRepositoryProperties());

		viewBinding.deleteRepository.setOnClickListener(deleteRepository -> showDeleteRepository());

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

			if(!repositoryName.equals(repoName)) {

				Toasty.error(ctx, getString(R.string.repoSettingsDeleteError));
			}
			else if(newOwner.matches("")) {

				Toasty.error(ctx, getString(R.string.repoTransferOwnerError));
			}
			else {

				transferRepository(newOwner);
			}
		});

		dialogTransferRepository.show();
	}

	private void transferRepository(String newOwner) {

		RepositoryTransfer repositoryTransfer = new RepositoryTransfer(newOwner);

		Call<JsonElement> transferCall = RetrofitClient
			.getApiInterface(ctx)
			.transferRepository(instanceToken, repositoryOwner, repositoryName, repositoryTransfer);

		transferCall.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				transferRepoBinding.transfer.setVisibility(View.GONE);
				transferRepoBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 202) {

					dialogTransferRepository.dismiss();
					Toasty.success(ctx, getString(R.string.repoTransferSuccess));

					finish();
					RepositoriesApi.deleteRepository((int) tinyDB.getLong("repositoryId", 0));
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
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

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

			if(!repositoryName.equals(String.valueOf(deleteRepoBinding.repoNameForDeletion.getText()))) {

				Toasty.error(ctx, getString(R.string.repoSettingsDeleteError));
			}
			else {

				deleteRepository();
			}
		});

		dialogDeleteRepository.show();
	}

	private void deleteRepository() {

		Call<JsonElement> deleteCall = RetrofitClient
			.getApiInterface(ctx)
			.deleteRepository(instanceToken, repositoryOwner, repositoryName);

		deleteCall.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull retrofit2.Response<JsonElement> response) {

				deleteRepoBinding.delete.setVisibility(View.GONE);
				deleteRepoBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 204) {

					dialogDeleteRepository.dismiss();
					Toasty.success(ctx, getString(R.string.repoDeletionSuccess));

					finish();
					RepositoriesApi.deleteRepository((int) tinyDB.getLong("repositoryId", 0));
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
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

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

		Call<UserRepositories> call = RetrofitClient
			.getApiInterface(ctx)
			.getUserRepository(instanceToken, repositoryOwner, repositoryName);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				propBinding.progressBar.setVisibility(View.GONE);
				propBinding.mainView.setVisibility(View.VISIBLE);

				if (response.code() == 200) {

					assert repoInfo != null;
					propBinding.repoName.setText(repoInfo.getName());
					propBinding.repoWebsite.setText(repoInfo.getWebsite());
					propBinding.repoDescription.setText(repoInfo.getDescription());
					propBinding.repoPrivate.setChecked(repoInfo.getPrivateFlag());
					propBinding.repoAsTemplate.setChecked(repoInfo.isTemplate());

					propBinding.repoEnableIssues.setChecked(repoInfo.getHas_issues());

					propBinding.repoEnableIssues.setOnCheckedChangeListener((buttonView, isChecked) -> {

						if (isChecked) {

							propBinding.repoEnableTimer.setVisibility(View.VISIBLE);
						}
						else {

							propBinding.repoEnableTimer.setVisibility(View.GONE);
						}
					});

					if(repoInfo.getInternal_tracker() != null) {

						propBinding.repoEnableTimer.setChecked(repoInfo.getInternal_tracker().isEnable_time_tracker());
					}
					else {

						propBinding.repoEnableTimer.setVisibility(View.GONE);
					}

					propBinding.repoEnableWiki.setChecked(repoInfo.isHas_wiki());
					propBinding.repoEnablePr.setChecked(repoInfo.isHas_pull_requests());
					propBinding.repoEnableMerge.setChecked(repoInfo.isAllow_merge_commits());
					propBinding.repoEnableRebase.setChecked(repoInfo.isAllow_rebase());
					propBinding.repoEnableSquash.setChecked(repoInfo.isAllow_squash_merge());
					propBinding.repoEnableForceMerge.setChecked(repoInfo.isAllow_rebase_explicit());

				}
				else {

					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

		propBinding.save.setOnClickListener(saveProperties -> saveRepositoryProperties(String.valueOf(propBinding.repoName.getText()),
			String.valueOf(propBinding.repoWebsite.getText()),
			String.valueOf(propBinding.repoDescription.getText()),
			propBinding.repoPrivate.isChecked(), propBinding.repoAsTemplate.isChecked(),
			propBinding.repoEnableIssues.isChecked(), propBinding.repoEnableWiki.isChecked(),
			propBinding.repoEnablePr.isChecked(), propBinding.repoEnableTimer.isChecked(),
			propBinding.repoEnableMerge.isChecked(), propBinding.repoEnableRebase.isChecked(),
			propBinding.repoEnableSquash.isChecked(), propBinding.repoEnableForceMerge.isChecked()));

		dialogProp.show();
	}

	private void saveRepositoryProperties(String repoName, String repoWebsite, String repoDescription,
		boolean repoPrivate, boolean repoAsTemplate, boolean repoEnableIssues, boolean repoEnableWiki,
		boolean repoEnablePr, boolean repoEnableTimer, boolean repoEnableMerge, boolean repoEnableRebase,
		boolean repoEnableSquash, boolean repoEnableForceMerge) {

		UserRepositories.internalTimeTrackerObject repoPropsTimeTracker = new UserRepositories.internalTimeTrackerObject(repoEnableTimer);

		UserRepositories repoProps;

		if(!repoEnableIssues) {

			repoProps = new UserRepositories(repoName, repoWebsite, repoDescription, repoPrivate, repoAsTemplate, repoEnableIssues, repoEnableWiki, repoEnablePr, repoEnableMerge,
				repoEnableRebase, repoEnableSquash, repoEnableForceMerge);
		}
		else {

			repoProps = new UserRepositories(repoName, repoWebsite, repoDescription, repoPrivate, repoAsTemplate, repoEnableIssues, repoEnableWiki, repoEnablePr, repoPropsTimeTracker, repoEnableMerge,
				repoEnableRebase, repoEnableSquash, repoEnableForceMerge);
		}

		Call<UserRepositories> propsCall = RetrofitClient
			.getApiInterface(ctx)
			.updateRepositoryProperties(instanceToken, repositoryOwner, repositoryName, repoProps);

		propsCall.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				propBinding.save.setVisibility(View.GONE);
				propBinding.processingRequest.setVisibility(View.VISIBLE);

				if (response.code() == 200) {

					tinyDB.putBoolean("hasIssues", repoEnableIssues);
					tinyDB.putBoolean("hasPullRequests", repoEnablePr);

					dialogProp.dismiss();
					Toasty.success(ctx, getString(R.string.repoPropertiesSaveSuccess));

					if(!repositoryName.equals(repoName)) {

						finish();
						RepositoriesApi.updateRepositoryOwnerAndName(repositoryOwner, repoName, (int) tinyDB.getLong("repositoryId", 0));
						Intent intent = new Intent(RepositorySettingsActivity.this, MainActivity.class);
						RepositorySettingsActivity.this.startActivity(intent);
					}
				}
				else {

					propBinding.save.setVisibility(View.VISIBLE);
					propBinding.processingRequest.setVisibility(View.GONE);
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				propBinding.save.setVisibility(View.VISIBLE);
				propBinding.processingRequest.setVisibility(View.GONE);
				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
