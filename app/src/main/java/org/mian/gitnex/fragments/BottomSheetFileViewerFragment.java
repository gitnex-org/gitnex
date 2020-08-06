package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class BottomSheetFileViewerFragment extends BottomSheetDialogFragment {

    private BottomSheetFileViewerFragment.BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.bottom_sheet_file_viewer, container, false);

        TextView downloadFile = v.findViewById(R.id.downloadFile);

        downloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bmListener.onButtonClicked("downloadFile");
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
            bmListener = (BottomSheetFileViewerFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
