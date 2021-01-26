package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetFileViewerBinding;

/**
 * Author M M Arif
 */

public class BottomSheetFileViewerFragment extends BottomSheetDialogFragment {

    private BottomSheetFileViewerFragment.BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetFileViewerBinding bottomSheetFileViewerBinding = BottomSheetFileViewerBinding.inflate(inflater, container, false);

	    bottomSheetFileViewerBinding.downloadFile.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("downloadFile");
            dismiss();
        });

	    bottomSheetFileViewerBinding.deleteFile.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("deleteFile");
		    dismiss();
	    });

	    bottomSheetFileViewerBinding.editFile.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("editFile");
		    dismiss();
	    });

        return bottomSheetFileViewerBinding.getRoot();
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {

            bmListener = (BottomSheetFileViewerFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
