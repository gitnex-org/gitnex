package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetMilestonesFilterBinding;

/**
 * Author M M Arif
 */

public class BottomSheetMilestonesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetMilestonesFilterFragment.BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetMilestonesFilterBinding bottomSheetMilestonesFilterBinding = BottomSheetMilestonesFilterBinding.inflate(inflater, container, false);

		bottomSheetMilestonesFilterBinding.openMilestone.setOnClickListener(v1 -> {
			bmListener.onButtonClicked("openMilestone");
			dismiss();
		});

		bottomSheetMilestonesFilterBinding.closedMilestone.setOnClickListener(v12 -> {
			bmListener.onButtonClicked("closedMilestone");
			dismiss();
		});

		return bottomSheetMilestonesFilterBinding.getRoot();
	}

	public interface BottomSheetListener {

		void onButtonClicked(String text);

	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			bmListener = (BottomSheetMilestonesFilterFragment.BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			Log.e("MilestonesFilterBs", e.toString());
		}
	}

}
