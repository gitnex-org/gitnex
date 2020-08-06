package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileEmailActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author M M Arif
 */

public class BottomSheetProfileFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_profile, container, false);

        TextView addNewEmailAddress = v.findViewById(R.id.addNewEmailAddress);

        addNewEmailAddress.setOnClickListener(v1 -> {

            startActivity(new Intent(getContext(), ProfileEmailActivity.class));
            dismiss();
        });

        return v;
    }

}
