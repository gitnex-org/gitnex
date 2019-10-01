package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.util.TinyDB;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author M M Arif
 */

public class RepoBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.repo_bottom_sheet_layout, container, false);

        final TinyDB tinyDb = new TinyDB(getContext());

        TextView createLabel = v.findViewById(R.id.createLabel);
        TextView createIssue = v.findViewById(R.id.createNewIssue);
        TextView createMilestone = v.findViewById(R.id.createNewMilestone);
        TextView addCollaborator = v.findViewById(R.id.addCollaborator);
        TextView createRelease = v.findViewById(R.id.createRelease);
        TextView openWebRepo = v.findViewById(R.id.openWebRepo);
        TextView newFile = v.findViewById(R.id.newFile);

        createLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("label");
                dismiss();
            }
        });

        if(tinyDb.getBoolean("hasIssues")) {
            createIssue.setVisibility(View.VISIBLE);
            createIssue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bmListener.onButtonClicked("newIssue");
                    dismiss();
                }
            });
        }
        else {
            createIssue.setVisibility(View.GONE);
        }

        createMilestone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("newMilestone");
                dismiss();
            }
        });

        addCollaborator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("addCollaborator");
                dismiss();
            }
        });

        createRelease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("createRelease");
                dismiss();
            }
        });

        openWebRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("openWebRepo");
                dismiss();
            }
        });

        newFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("newFile");
                dismiss();
            }
        });

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
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }

}
