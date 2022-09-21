package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.actions.RepositoryActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomSheetRepoBinding;
import org.mian.gitnex.helpers.contexts.AccountContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 */
public class BottomSheetRepoFragment extends BottomSheetDialogFragment {

	private final RepositoryContext repository;
	private BottomSheetListener bmListener;

	public BottomSheetRepoFragment(RepositoryContext repository) {
		this.repository = repository;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		BottomSheetRepoBinding bottomSheetRepoBinding =
				BottomSheetRepoBinding.inflate(inflater, container, false);

		AccountContext account = ((BaseActivity) requireActivity()).getAccount();

		TextView createLabel = bottomSheetRepoBinding.createLabel;
		TextView createIssue = bottomSheetRepoBinding.createNewIssue;
		TextView createMilestone = bottomSheetRepoBinding.createNewMilestone;
		TextView addCollaborator = bottomSheetRepoBinding.addCollaborator;
		TextView createRelease = bottomSheetRepoBinding.createRelease;
		TextView openWebRepo = bottomSheetRepoBinding.openWebRepo;
		TextView newFile = bottomSheetRepoBinding.newFile;
		TextView starRepository = bottomSheetRepoBinding.starRepository;
		TextView unStarRepository = bottomSheetRepoBinding.unStarRepository;
		TextView watchRepository = bottomSheetRepoBinding.watchRepository;
		TextView unWatchRepository = bottomSheetRepoBinding.unWatchRepository;
		TextView shareRepository = bottomSheetRepoBinding.shareRepository;
		TextView copyRepoUrl = bottomSheetRepoBinding.copyRepoUrl;
		TextView repoSettings = bottomSheetRepoBinding.repoSettings;
		TextView createPullRequest = bottomSheetRepoBinding.createPullRequest;
		TextView createWiki = bottomSheetRepoBinding.createWiki;

		boolean canPush = repository.getPermissions().isPush();
		if (!canPush) {
			createMilestone.setVisibility(View.GONE);
			createLabel.setVisibility(View.GONE);
			createRelease.setVisibility(View.GONE);
			newFile.setVisibility(View.GONE);
		}

		if (!account.requiresVersion("1.16")) {
			createWiki.setVisibility(View.GONE);
		}

		boolean archived = repository.getRepository().isArchived();
		if (archived) {
			createIssue.setVisibility(View.GONE);
			createPullRequest.setVisibility(View.GONE);
			createMilestone.setVisibility(View.GONE);
			createLabel.setVisibility(View.GONE);
			createRelease.setVisibility(View.GONE);
			newFile.setVisibility(View.GONE);
			createWiki.setVisibility(View.GONE);
		}

		createLabel.setOnClickListener(
				v112 -> {
					bmListener.onButtonClicked("label");
					dismiss();
				});

		if (repository.getRepository().isHasIssues() && !archived) {

			createIssue.setVisibility(View.VISIBLE);
			createIssue.setOnClickListener(
					v12 -> {
						bmListener.onButtonClicked("newIssue");
						dismiss();
					});
		} else {

			createIssue.setVisibility(View.GONE);
		}

		if (repository.getRepository().isHasPullRequests() && !archived) {

			createPullRequest.setVisibility(View.VISIBLE);
			createPullRequest.setOnClickListener(
					vPr -> {
						bmListener.onButtonClicked("newPullRequest");
						dismiss();
					});
		} else {

			createPullRequest.setVisibility(View.GONE);
		}

		createMilestone.setOnClickListener(
				v13 -> {
					bmListener.onButtonClicked("newMilestone");
					dismiss();
				});

		if (repository.getPermissions().isAdmin()) {

			repoSettings.setOnClickListener(
					repoSettingsView -> {
						bmListener.onButtonClicked("repoSettings");
						dismiss();
					});

			addCollaborator.setOnClickListener(
					v1 -> {
						bmListener.onButtonClicked("addCollaborator");
						dismiss();
					});

			createWiki.setOnClickListener(
					v1 -> {
						bmListener.onButtonClicked("createWiki");
						dismiss();
					});
		} else {

			addCollaborator.setVisibility(View.GONE);
			repoSettings.setVisibility(View.GONE);
			createWiki.setVisibility(View.GONE);
		}

		createRelease.setOnClickListener(
				v14 -> {
					bmListener.onButtonClicked("createRelease");
					dismiss();
				});

		shareRepository.setOnClickListener(
				v15 -> {
					bmListener.onButtonClicked("shareRepo");
					dismiss();
				});

		openWebRepo.setOnClickListener(
				v16 -> {
					bmListener.onButtonClicked("openWebRepo");
					dismiss();
				});

		copyRepoUrl.setOnClickListener(
				copyUrl -> {
					bmListener.onButtonClicked("copyRepoUrl");
					dismiss();
				});

		newFile.setOnClickListener(
				v17 -> {
					bmListener.onButtonClicked("newFile");
					dismiss();
				});

		if (repository.isStarred()) {

			starRepository.setVisibility(View.GONE);
			unStarRepository.setOnClickListener(
					v18 -> {
						RepositoryActions.unStarRepository(getContext(), repository);
						bmListener.onButtonClicked("unstar");
						dismiss();
					});

		} else {

			unStarRepository.setVisibility(View.GONE);
			starRepository.setOnClickListener(
					v19 -> {
						RepositoryActions.starRepository(getContext(), repository);
						bmListener.onButtonClicked("star");
						dismiss();
					});
		}

		if (repository.isWatched()) {

			watchRepository.setVisibility(View.GONE);
			unWatchRepository.setOnClickListener(
					v110 -> {
						RepositoryActions.unWatchRepository(getContext(), repository);
						bmListener.onButtonClicked("unwatch");
						dismiss();
					});

		} else {

			unWatchRepository.setVisibility(View.GONE);
			watchRepository.setOnClickListener(
					v111 -> {
						RepositoryActions.watchRepository(getContext(), repository);
						bmListener.onButtonClicked("watch");
						dismiss();
					});
		}

		return bottomSheetRepoBinding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		try {
			bmListener = (BottomSheetListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}
}
