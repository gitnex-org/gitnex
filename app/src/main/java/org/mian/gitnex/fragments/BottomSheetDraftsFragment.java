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
import org.mian.gitnex.databinding.BottomSheetDraftsBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 */

public class BottomSheetDraftsFragment extends BottomSheetDialogFragment {

	private final String TAG = "BottomSheetDraftsFragment";
	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetDraftsBinding bottomSheetDraftsBinding = BottomSheetDraftsBinding.inflate(inflater, container, false);

		bottomSheetDraftsBinding.deleteAllDrafts.setOnClickListener(v1 -> {

			dismiss();
			bmListener.onButtonClicked("deleteDrafts");

		});

		return bottomSheetDraftsBinding.getRoot();
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		try {
			bmListener = (BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			Log.e(TAG, e.toString());
		}
	}

}
