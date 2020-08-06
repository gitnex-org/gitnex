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

/**
 * Author M M Arif
 */

public class BottomSheetAdminUsersFragment extends BottomSheetDialogFragment {

    private BottomSheetAdminUsersFragment.BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_admin_users, container, false);

        TextView createNewUser = v.findViewById(R.id.createNewUser);

        createNewUser.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("newUser");
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
            bmListener = (BottomSheetAdminUsersFragment.BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
        }
    }

}
