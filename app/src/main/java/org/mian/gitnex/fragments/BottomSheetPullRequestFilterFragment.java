package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetPullRequestFilterBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * Author M M Arif
 */

public class BottomSheetPullRequestFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetPullRequestFilterBinding bottomSheetPullRequestFilterBinding = BottomSheetPullRequestFilterBinding.inflate(inflater, container, false);

		bottomSheetPullRequestFilterBinding.openPr.setOnClickListener(v1 -> {
			bmListener.onButtonClicked("openPr");
			dismiss();
		});

		bottomSheetPullRequestFilterBinding.closedPr.setOnClickListener(v12 -> {
			bmListener.onButtonClicked("closedPr");
			dismiss();
		});

		return bottomSheetPullRequestFilterBinding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			bmListener = (BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
		}
	}

}
