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
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * Author M M Arif
 */

public class BottomSheetRepoFragment extends BottomSheetDialogFragment {

    private BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetRepoBinding bottomSheetRepoBinding = BottomSheetRepoBinding.inflate(inflater, container, false);

        final TinyDB tinyDb = TinyDB.getInstance(getContext());

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
	    View repoSettingsDivider = bottomSheetRepoBinding.repoSettingsDivider;
	    TextView repoSettings = bottomSheetRepoBinding.repoSettings;
	    TextView createPullRequest = bottomSheetRepoBinding.createPullRequest;

        createLabel.setOnClickListener(v112 -> {

            bmListener.onButtonClicked("label");
            dismiss();
        });

        if(tinyDb.getBoolean("hasIssues")) {

            createIssue.setVisibility(View.VISIBLE);
            createIssue.setOnClickListener(v12 -> {

                bmListener.onButtonClicked("newIssue");
                dismiss();
            });
        }
        else {

            createIssue.setVisibility(View.GONE);
        }

	    if(tinyDb.getBoolean("hasPullRequests")) {

		    createPullRequest.setVisibility(View.VISIBLE);
		    createPullRequest.setOnClickListener(vPr -> {

			    bmListener.onButtonClicked("newPullRequest");
			    dismiss();
		    });
	    }
	    else {

		    createPullRequest.setVisibility(View.GONE);
	    }

        createMilestone.setOnClickListener(v13 -> {

            bmListener.onButtonClicked("newMilestone");
            dismiss();
        });

		if (tinyDb.getBoolean("isRepoAdmin")) {

			repoSettings.setOnClickListener(repoSettingsView -> {

				bmListener.onButtonClicked("repoSettings");
				dismiss();
			});

			addCollaborator.setOnClickListener(v1 -> {

				bmListener.onButtonClicked("addCollaborator");
				dismiss();
			});
		}
		else {

			addCollaborator.setVisibility(View.GONE);
			repoSettingsDivider.setVisibility(View.GONE);
			repoSettings.setVisibility(View.GONE);
		}

        createRelease.setOnClickListener(v14 -> {

            bmListener.onButtonClicked("createRelease");
            dismiss();
        });

        shareRepository.setOnClickListener(v15 -> {

            bmListener.onButtonClicked("shareRepo");
            dismiss();
        });

        openWebRepo.setOnClickListener(v16 -> {

            bmListener.onButtonClicked("openWebRepo");
            dismiss();
        });

	    copyRepoUrl.setOnClickListener(copyUrl -> {

		    bmListener.onButtonClicked("copyRepoUrl");
		    dismiss();
	    });

        newFile.setOnClickListener(v17 -> {

            bmListener.onButtonClicked("newFile");
            dismiss();
        });

        if(tinyDb.getInt("repositoryStarStatus") == 204) { // star a repo

            starRepository.setVisibility(View.GONE);
            unStarRepository.setOnClickListener(v18 -> {

                RepositoryActions.unStarRepository(getContext());
                tinyDb.putInt("repositoryStarStatus", 404);
                dismiss();

            });

        }
        else if(tinyDb.getInt("repositoryStarStatus") == 404) {

            unStarRepository.setVisibility(View.GONE);
            starRepository.setOnClickListener(v19 -> {

                RepositoryActions.starRepository(getContext());
                tinyDb.putInt("repositoryStarStatus", 204);
                dismiss();

            });

        }

        if(tinyDb.getBoolean("repositoryWatchStatus")) { // watch a repo

            watchRepository.setVisibility(View.GONE);
            unWatchRepository.setOnClickListener(v110 -> {

                RepositoryActions.unWatchRepository(getContext());
                tinyDb.putBoolean("repositoryWatchStatus", false);
                dismiss();

            });

        }
        else {

            unWatchRepository.setVisibility(View.GONE);
            watchRepository.setOnClickListener(v111 -> {

                RepositoryActions.watchRepository(getContext());
                tinyDb.putBoolean("repositoryWatchStatus", true);
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
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
