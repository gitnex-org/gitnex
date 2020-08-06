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
import org.mian.gitnex.R;
import org.mian.gitnex.actions.RepositoryActions;
import org.mian.gitnex.helpers.TinyDB;

/**
 * Author M M Arif
 */

public class BottomSheetRepoFragment extends BottomSheetDialogFragment {

    private BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_repo, container, false);

        final TinyDB tinyDb = new TinyDB(getContext());

        TextView createLabel = v.findViewById(R.id.createLabel);
        TextView createIssue = v.findViewById(R.id.createNewIssue);
        TextView createMilestone = v.findViewById(R.id.createNewMilestone);
        TextView addCollaborator = v.findViewById(R.id.addCollaborator);
        TextView createRelease = v.findViewById(R.id.createRelease);
        TextView openWebRepo = v.findViewById(R.id.openWebRepo);
        TextView newFile = v.findViewById(R.id.newFile);
        TextView starRepository = v.findViewById(R.id.starRepository);
        TextView unStarRepository = v.findViewById(R.id.unStarRepository);
        TextView watchRepository = v.findViewById(R.id.watchRepository);
        TextView unWatchRepository = v.findViewById(R.id.unWatchRepository);
        TextView shareRepository = v.findViewById(R.id.shareRepository);
	    TextView copyRepoUrl = v.findViewById(R.id.copyRepoUrl);

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

        createMilestone.setOnClickListener(v13 -> {

            bmListener.onButtonClicked("newMilestone");
            dismiss();
        });

		if (tinyDb.getBoolean("isRepoAdmin")) {
			addCollaborator.setOnClickListener(v1 -> {

				bmListener.onButtonClicked("addCollaborator");
				dismiss();
			});
		} else {
			addCollaborator.setVisibility(View.GONE);
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

        return v;
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
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
