package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.AdminGetUsersActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Author M M Arif
 */

public class BottomSheetNavSubMenuFragment extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_nav_sub_menu_layout, container, false);

        TextView adminUsers = v.findViewById(R.id.adminUsers);

        adminUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), AdminGetUsersActivity.class));
                dismiss();
            }
        });

        return v;
    }

}
