package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetUserProfileBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 * @author qwerty287
 */

public class BottomSheetUserProfileFragment extends BottomSheetDialogFragment {

	private final boolean following;

	public BottomSheetUserProfileFragment(boolean following) {
		this.following = following;
	}

    private BottomSheetListener bmListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

	    BottomSheetUserProfileBinding bottomSheetUserProfileBinding = BottomSheetUserProfileBinding.inflate(inflater, container, false);

	    if(following) {
		    bottomSheetUserProfileBinding.unfollowUser.setVisibility(View.VISIBLE);
		    bottomSheetUserProfileBinding.followUser.setVisibility(View.GONE);
	    }

        bottomSheetUserProfileBinding.followUser.setOnClickListener(v1 -> {

            bmListener.onButtonClicked("follow");
            dismiss();
        });

	    bottomSheetUserProfileBinding.unfollowUser.setOnClickListener(v1 -> {

		    bmListener.onButtonClicked("follow");
		    dismiss();
	    });

        return bottomSheetUserProfileBinding.getRoot();
    }

	@Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            bmListener = (BottomSheetListener) context;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(context + " must implement BottomSheetListener");
        }
    }
}
