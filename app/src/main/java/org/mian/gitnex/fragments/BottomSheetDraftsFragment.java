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
import org.mian.gitnex.helpers.StaticGlobalVariables;

/**
 * Author M M Arif
 */

public class BottomSheetDraftsFragment extends BottomSheetDialogFragment {

	private String TAG = StaticGlobalVariables.tagDraftsBottomSheet;
	private BottomSheetDraftsFragment.BottomSheetListener bmListener;

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

	public interface BottomSheetListener {
		void onButtonClicked(String text);
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		try {
			bmListener = (BottomSheetDraftsFragment.BottomSheetListener) context;
		}
		catch (ClassCastException e) {
			Log.e(TAG, e.toString());
		}
	}

}
