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

public class BottomSheetIssuesFilterFragment extends BottomSheetDialogFragment {

	private BottomSheetIssuesFilterFragment.BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.bottom_sheet_issues_filter, container, false);

		TextView openIssues = v.findViewById(R.id.openIssues);
		TextView closedIssues = v.findViewById(R.id.closedIssues);

		openIssues.setOnClickListener(v1 -> {
			bmListener.onButtonClicked("openIssues");
			dismiss();
		});

		closedIssues.setOnClickListener(v12 -> {
			bmListener.onButtonClicked("closedIssues");
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
			bmListener = (BottomSheetIssuesFilterFragment.BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(context.toString() + " must implement BottomSheetListener");
		}
	}

}
