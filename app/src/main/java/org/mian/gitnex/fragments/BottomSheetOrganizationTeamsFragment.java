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
import org.mian.gitnex.databinding.BottomSheetOrganizationTeamsBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * Author M M Arif
 */

public class BottomSheetOrganizationTeamsFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetOrganizationTeamsBinding bottomSheetOrganizationTeamsBinding = BottomSheetOrganizationTeamsBinding.inflate(inflater, container, false);

		bottomSheetOrganizationTeamsBinding.addNewMember.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("newMember");
			dismiss();

		});

		return bottomSheetOrganizationTeamsBinding.getRoot();

	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);

		try {
			bmListener = (BottomSheetListener) context;
		}
		catch (ClassCastException e) {
			Log.e("BsOrganizationTeams", e.toString());
		}
	}

}
