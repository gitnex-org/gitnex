package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetOrganizationBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * Author M M Arif
 */

public class BottomSheetOrganizationFragment extends BottomSheetDialogFragment {

    private BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetOrganizationBinding bottomSheetOrganizationBinding = BottomSheetOrganizationBinding.inflate(inflater, container, false);

	    bottomSheetOrganizationBinding.createTeam.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("team");
            dismiss();
        });

	    bottomSheetOrganizationBinding.createLabel.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("label");
		    dismiss();
	    });

	    bottomSheetOrganizationBinding.createRepository.setOnClickListener(v12 -> {

            bmListener.onButtonClicked("repository");
            dismiss();
        });

	    bottomSheetOrganizationBinding.copyOrgUrl.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("copyOrgUrl");
		    dismiss();
	    });

        return bottomSheetOrganizationBinding.getRoot();
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
