package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

public class BottomSheetOrganizationTeamsFragment extends BottomSheetDialogFragment {

	private BottomSheetOrganizationTeamsFragment.BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.bottom_sheet_organization_teams, container, false);

		TextView addNewMember = v.findViewById(R.id.addNewMember);

		addNewMember.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("newMember");
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
			bmListener = (BottomSheetOrganizationTeamsFragment.BottomSheetListener) context;
		}
		catch (ClassCastException e) {
			Log.e("BsOrganizationTeams", e.toString());
		}
	}

}
