package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author M M Arif
 */

public class BottomSheetOrganizationFragment extends BottomSheetDialogFragment {

    private BottomSheetOrganizationFragment.BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

    	View v = inflater.inflate(R.layout.bottom_sheet_organization, container, false);

        TextView createTeam = v.findViewById(R.id.createTeam);
        TextView createRepository = v.findViewById(R.id.createRepository);
        TextView copyOrgUrl = v.findViewById(R.id.copyOrgUrl);
	    TextView createLabel = v.findViewById(R.id.createLabel);

        createTeam.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("team");
            dismiss();
        });

	    createLabel.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("label");
		    dismiss();
	    });

        createRepository.setOnClickListener(v12 -> {

            bmListener.onButtonClicked("repository");
            dismiss();
        });

	    copyOrgUrl.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("copyOrgUrl");
		    dismiss();
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
            bmListener = (BottomSheetOrganizationFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
