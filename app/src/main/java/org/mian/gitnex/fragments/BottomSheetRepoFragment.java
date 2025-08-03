package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.actions.RepositoryActions;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomSheetRepoBinding;
import org.mian.gitnex.helpers.Version;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author mmarif
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

		// repo actions require gitea and version 1.24
		String provider = ((BaseActivity) requireContext()).getAccount().getAccount().getProvider();
		if (provider != null) {
			String serverVersion =
					((BaseActivity) requireContext()).getAccount().getAccount().getServerVersion();
			Version minVersion = new Version("1.24");
			Version currentVersion =
					Version.valid(serverVersion) ? new Version(serverVersion) : new Version("0.0");

			if ("gitea".equals(provider) && !currentVersion.less(minVersion)) {
				bottomSheetRepoBinding.repositoryActions.setVisibility(View.VISIBLE);
			} else {
				bottomSheetRepoBinding.repositoryActions.setVisibility(View.GONE);
			}
		} else {
			bottomSheetRepoBinding.repositoryActions.setVisibility(View.GONE);
		}

		if (repository.getPermissions().isAdmin()) {

			bottomSheetRepoBinding.repoSettings.setOnClickListener(
					repoSettingsView -> {
						bmListener.onButtonClicked("repoSettings");
						dismiss();
					});

			bottomSheetRepoBinding.repositoryActions.setOnClickListener(
					repoActions -> {
						bmListener.onButtonClicked("repoActions");
						dismiss();
					});
		} else {

			bottomSheetRepoBinding.repoSettings.setVisibility(View.GONE);
			bottomSheetRepoBinding.repositoryActions.setVisibility(View.GONE);
		}

		bottomSheetRepoBinding.shareRepository.setOnClickListener(
				v15 -> {
					bmListener.onButtonClicked("shareRepo");
					dismiss();
				});

		bottomSheetRepoBinding.openWebRepo.setOnClickListener(
				v16 -> {
					bmListener.onButtonClicked("openWebRepo");
					dismiss();
				});

		bottomSheetRepoBinding.copyRepoUrl.setOnClickListener(
				copyUrl -> {
					bmListener.onButtonClicked("copyRepoUrl");
					dismiss();
				});

		if (repository.isStarred()) {

			bottomSheetRepoBinding.starRepository.setVisibility(View.GONE);
			bottomSheetRepoBinding.unStarRepository.setOnClickListener(
					v18 -> {
						RepositoryActions.unStarRepository(getContext(), repository);
						bmListener.onButtonClicked("unstar");
						dismiss();
					});
		} else {

			bottomSheetRepoBinding.unStarRepository.setVisibility(View.GONE);
			bottomSheetRepoBinding.starRepository.setOnClickListener(
					v19 -> {
						RepositoryActions.starRepository(getContext(), repository);
						bmListener.onButtonClicked("star");
						dismiss();
					});
		}

		if (repository.isWatched()) {

			bottomSheetRepoBinding.watchRepository.setVisibility(View.GONE);
			bottomSheetRepoBinding.unWatchRepository.setOnClickListener(
					v110 -> {
						RepositoryActions.unWatchRepository(getContext(), repository);
						bmListener.onButtonClicked("unwatch");
						dismiss();
					});
		} else {

			bottomSheetRepoBinding.unWatchRepository.setVisibility(View.GONE);
			bottomSheetRepoBinding.watchRepository.setOnClickListener(
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
