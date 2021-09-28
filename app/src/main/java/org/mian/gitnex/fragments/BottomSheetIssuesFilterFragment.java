package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetIssuesFilterBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;

/**
 * Author M M Arif
 */

public class BottomSheetIssuesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetIssuesFilterFragment.BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetIssuesFilterBinding bottomSheetIssuesFilterBinding = BottomSheetIssuesFilterBinding.inflate(inflater, container, false);
		TinyDB tinyDb = TinyDB.getInstance(getContext());

		if(new Version(tinyDb.getString("giteaVersion")).higherOrEqual("1.14.0")) {
			bottomSheetIssuesFilterBinding.filterByMilestone.setVisibility(View.VISIBLE);
			bottomSheetIssuesFilterBinding.filterByMilestone.setOnClickListener(v1 -> {
				bmListener.onButtonClicked("filterByMilestone");
				dismiss();
			});
		}

		bottomSheetIssuesFilterBinding.openIssues.setOnClickListener(v1 -> {
			bmListener.onButtonClicked("openIssues");
			dismiss();
		});

		bottomSheetIssuesFilterBinding.closedIssues.setOnClickListener(v12 -> {
			bmListener.onButtonClicked("closedIssues");
			dismiss();
		});

		return bottomSheetIssuesFilterBinding.getRoot();
	}

	public interface BottomSheetListener {

		void onButtonClicked(String text);

	}

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			bmListener = (BottomSheetIssuesFilterFragment.BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
		}
	}

}
