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
import org.mian.gitnex.databinding.BottomSheetRepoBinding;
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

		TextView openWebRepo = bottomSheetRepoBinding.openWebRepo;
		TextView starRepository = bottomSheetRepoBinding.starRepository;
		TextView unStarRepository = bottomSheetRepoBinding.unStarRepository;
		TextView watchRepository = bottomSheetRepoBinding.watchRepository;
		TextView unWatchRepository = bottomSheetRepoBinding.unWatchRepository;
		TextView shareRepository = bottomSheetRepoBinding.shareRepository;
		TextView copyRepoUrl = bottomSheetRepoBinding.copyRepoUrl;
		TextView repoSettings = bottomSheetRepoBinding.repoSettings;

		if (repository.getPermissions().isAdmin()) {

			repoSettings.setOnClickListener(
					repoSettingsView -> {
						bmListener.onButtonClicked("repoSettings");
						dismiss();
					});
		} else {

			repoSettings.setVisibility(View.GONE);
		}

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
